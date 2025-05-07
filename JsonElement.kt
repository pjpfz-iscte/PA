import kotlin.io.path.fileVisitor
import kotlin.reflect.KClass

data class JsonArray<T : JsonElement>(val content: Array<T>) : JsonElement() {
    override fun getText(identLevel: Int): String {
        var jsonArrayText = "[\n"
        content.forEach {
            element -> jsonArrayText += "${"\t".repeat(identLevel + 1)}${element.getText(identLevel + 1)},\n"
        }
        jsonArrayText = jsonArrayText.removeSuffix(",\n")
        jsonArrayText += "\n${"\t".repeat(identLevel)}]"
        return jsonArrayText
    }


    /*fun filter(predicate: (JsonElement) -> Boolean): JsonArray<JsonElement> {
        val filteredList = mutableListOf<JsonElement>()
        accept { e ->
            if (predicate(e))
                filteredList.add(e)
        }
        return JsonArray(filteredList.toTypedArray())
    }*/
    fun filter(predicate: (JsonElement) -> Boolean): JsonArray<JsonElement>{
        val filteredList = content.mapNotNull {
            when (it) {
                is JsonArray<*> ->{
                    val filteredArray = it.filter(predicate)
                    if(filteredArray.content.isNotEmpty()) filteredArray else null
                }
                is JsonObject -> {
                    val filteredObject = it.filter(predicate)
                    if(filteredObject.map.isNotEmpty()) filteredObject else null
                }
                else -> if(predicate(it)) it else null
            }
        }
        return JsonArray(filteredList.toTypedArray())
    }

    /*fun map(predicate: (JsonElement) -> JsonElement): JsonArray<JsonElement>{
        val map = mutableListOf<JsonElement>()
        accept { e ->
            if(e !is JsonArray<*>){
                map.add(predicate(e))
            }
        }
        return JsonArray(map.toTypedArray())
    }*/
    fun map(predicate: (JsonElement) -> JsonElement): JsonArray<JsonElement>{
        val map = content.map {
            when(it){
                is JsonArray<*> -> it.map(predicate)
                is JsonObject -> it.map(predicate)
                else -> predicate(it)
            }
        }
        return JsonArray(map.toTypedArray())
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

data class JsonObject (val map: MutableMap<String,JsonElement>):  JsonElement(){
    override fun getText(identLevel: Int): String {
        var jsonText = "{\n"
        map.forEach{
                entry -> jsonText += "${"\t".repeat(identLevel + 1)}\"${entry.key}\":${entry.value.getText(identLevel + 1)},\n"
        }
        jsonText = jsonText.removeSuffix(",\n")
        jsonText += "\n${"\t".repeat(identLevel)}}"
        return jsonText
    }

    fun filter(predicate: (JsonElement) -> Boolean): JsonObject{
        val filteredMap = mutableMapOf<String, JsonElement>()
        /*map.forEach{ (key, value) ->
            value.accept {
                if (predicate(it))
                    filteredMap[key] = value
                }
        }*/
        map.forEach { (key, value) ->
            when (value) {
                is JsonArray<*> -> {
                    val filtered = value.filter(predicate)
                    if (filtered.content.isNotEmpty()) {
                        filteredMap[key] = filtered
                    }
                }

                is JsonObject -> {
                    val filtered = value.filter(predicate)
                    if (filtered.map.isNotEmpty()) {
                        filteredMap[key] = filtered
                    }
                }

                else -> {
                    if (predicate(value)) {
                        filteredMap[key] = value
                    }
                }
            }
        }
        return JsonObject(filteredMap)
    }

    fun map(predicate: (JsonElement) -> JsonElement) : JsonObject{
        val mapped = mutableMapOf<String, JsonElement>()
        map.forEach{ (key, value) ->
            val mappedValue = when (value) {
                is JsonArray<*> -> value.map(predicate)
                is JsonObject -> value.map(predicate)
                else -> predicate(value)
            }
            mapped[key] = mappedValue
        }
        return JsonObject(mapped)
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

