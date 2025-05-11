import javax.lang.model.type.PrimitiveType
import kotlin.io.path.fileVisitor
import kotlin.reflect.KClass
import kotlin.reflect.full.*

data class JsonArray<T : JsonElement>(val content: List<T>) : JsonElement() {
    override fun getText(identLevel: Int): String {
        var jsonArrayText = "[\n"
        content.forEach {
            element -> jsonArrayText += "${"\t".repeat(identLevel + 1)}${element.getText(identLevel + 1)},\n"
        }
        jsonArrayText = jsonArrayText.removeSuffix(",\n")
        jsonArrayText += "\n${"\t".repeat(identLevel)}]"
        return jsonArrayText
    }

    fun filter(predicate: (JsonElement) -> Boolean): JsonArray<JsonElement>{
        val filteredList = mutableListOf<JsonElement>()
        content.forEach {
            element -> if(predicate(element)) filteredList.add(element)
        }
        return JsonArray(filteredList)
    }

    fun map(predicate: (JsonElement) -> JsonElement): JsonArray<JsonElement>{
        val map = mutableListOf<JsonElement>()
        content.forEach {
            element -> map.add(predicate(element))
        }
        return JsonArray(map)
    }

    override fun toString(): String {
        return getText()
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visit(this)
        this.content.forEach { it.accept(visitor) }
    }
}

data class JsonString(val content: String) : JsonPrimitive() {
    override fun getText(identLevel: Int): String {
        return "\"${content}\""
    }
}

data class JsonNumber(val content: Number) : JsonPrimitive() {
    override fun getText(identLevel: Int): String {
        return content.toString()
    }
}

data class JsonBoolean(val content: Boolean) : JsonPrimitive() {
    override fun getText(identLevel: Int): String {
        return content.toString()
    }
}

object JsonNull : JsonPrimitive() {
    override fun getText(identLevel: Int): String {
        return "null"
    }
}

data class JsonObject (val map: Map<String,JsonElement>):  JsonElement(){
    override fun getText(identLevel: Int): String {
        var jsonText = "{\n"
        map.forEach{
                entry -> jsonText += "${"\t".repeat(identLevel + 1)}\"${entry.key}\":${entry.value.getText(identLevel + 1)},\n"
        }
        jsonText = jsonText.removeSuffix(",\n")
        jsonText += "\n${"\t".repeat(identLevel)}}"
        return jsonText
    }

    fun filter(predicate: (String, JsonElement) -> Boolean): JsonObject{
        val filteredMap = mutableMapOf<String, JsonElement>()
        map.forEach{
            (key,value) -> if(predicate(key,value)) filteredMap[key] = value
        }
        return JsonObject(filteredMap)
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visit(this)
        map.values.forEach{ it.accept(visitor) }
    }

}

sealed class JsonPrimitive : JsonElement(){
    override fun accept(visitor: JsonVisitor) {
        visitor.visit(this)
    }
}

sealed class JsonElement {
    abstract fun accept(visitor: JsonVisitor)

    abstract fun getText(identLevel: Int=0): String

    override fun toString(): String {
        return getText()
    }

    open fun isValid(): Boolean {
        val jsonValidator = JsonValidator()
        this.accept(jsonValidator)
        return jsonValidator.isValidJson()
    }
}

fun jsonElementFromObject(value: Any?): JsonElement{
  if (value==null)
      return JsonNull

 return when(value){
      is String ->  JsonString(value)
      is Number ->  JsonNumber(value)
      is Boolean ->  JsonBoolean(value)
      is List<*> -> { JsonArray(value.map{jsonElementFromObject(it)})}
      is Enum<*> ->  JsonString(value.name.uppercase())
      else -> {
          val clazz = value::class
          val constructorParams = clazz.primaryConstructor?.parameters.orEmpty()
          val map = constructorParams.mapNotNull { param ->
              val property = clazz.memberProperties.find { it.name == param.name }
              property?.name?.let { name ->
                  name to jsonElementFromObject(property.call(value))
              }
          }.toMap()
          JsonObject(map)
      }
  }
}