import kotlin.io.path.fileVisitor
import kotlin.reflect.KClass

// Usei o data class porque só guardamos dados
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
}

data class JsonString(val content: String) : JsonElement() {
    override fun getText(identLevel: Int): String {
        return "\"${content}\""
    }
}

data class JsonNumber(val content: Number) : JsonElement() {
    override fun getText(identLevel: Int): String {
        return content.toString()
    }
}

data class JsonBoolean(val content: Boolean) : JsonElement() {
    override fun getText(identLevel: Int): String {
        return content.toString()
    }
}


// Usei o object para criar um sigleton, não faz sentido criar várias instâncias de um objeto vazio
object JsonNull : JsonElement() {
    override fun getText(identLevel: Int): String {
        return "null"
    }
}

sealed class JsonElement {
    fun accept(visitor: JsonVisitor) {
        when (this) {
            is JsonArray<*> -> {
                visitor.visit(this)
                this.content.forEach { it.accept(visitor) }
            }
            is JsonObject -> {
                visitor.visit(this)
                this.map.values.forEach { it.accept(visitor) }
            }
            else -> visitor.visit(this)
        }
    }

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

