sealed class JsonElement {
    // Usei o data class porque só guardamos dados
    data class JsonArray<T>(val content: Array<T>){
        override fun toString(): String {
            return content.joinToString (", " , "[", "]" )
        }
    }
    data class JsonString(val content: String){
        override fun toString(): String {
            return "\"$content\""
        }
    }
    data class JsonNumber(val content: Number){
        override fun toString(): String {
            return content.toString()
        }
    }
    data class JsonBoolean(val content: Boolean) {
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


}