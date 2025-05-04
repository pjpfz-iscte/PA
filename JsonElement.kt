
    // Usei o data class porque só guardamos dados
data class JsonArray<T : JsonElement>(val content: Array<T>) : JsonElement(){
    override fun toString(): String {
        return content.joinToString (", " , "[", "]" )
    }

    override fun accept(visitor: (JsonElement) -> Unit){
        visitor(this)
        content.forEach{ it.accept(visitor)}
    }

    fun filter(predicate: (JsonElement) -> Boolean): JsonArray<JsonElement>{
        val filteredList = mutableListOf<JsonElement>()
        accept{ e ->
            if(predicate(e))
                filteredList.add(e)
        }
        return JsonArray(filteredList.toTypedArray())
    }
}

data class JsonString(val content: String) : JsonElement(){
    override fun toString(): String { return "\"$content\""
    }
}

data class JsonNumber(val content: Number) : JsonElement(){
    override fun toString(): String {
        return content.toString()
    }
}

data class JsonBoolean(val content: Boolean) : JsonElement(){
    override fun toString(): String {
        return content.toString()
    }
}


// Usei o object para criar um sigleton, não faz sentido criar várias instâncias de um objeto vazio
object JsonNull : JsonElement() {
    override fun toString(): String {
        return "null"
    }
}

sealed class JsonElement {
    open fun accept(visitor: (JsonElement) -> Unit){
        visitor(this)
    }
}