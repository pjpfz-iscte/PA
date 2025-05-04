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

    override fun accept(visitor: (JsonElement) -> Unit) {
        visitor(this)
        content.forEach { it.accept(visitor) }
    }

    fun filter(predicate: (JsonElement) -> Boolean): JsonArray<JsonElement> {
        val filteredList = mutableListOf<JsonElement>()
        accept { e ->
            if (predicate(e))
                filteredList.add(e)
        }
        return JsonArray(filteredList.toTypedArray())
    }

    fun map(predicate: (JsonElement) -> JsonElement): JsonArray<JsonElement>{
        val map = mutableListOf<JsonElement>()
        accept { e ->
            if(e !is JsonArray<*>){
                map.add(predicate(e))
            }
        }
        return JsonArray(map.toTypedArray())
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
    open fun accept(visitor: (JsonElement) -> Unit) {
        visitor(this)
    }

    abstract fun getText(identLevel: Int): String
}