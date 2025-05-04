class JsonObject (val map: MutableMap<String,JsonElement>):  JsonElement(){
    fun getJsonText(identLevel: Int = 0): String {
        var jsonText = "{\n"
        map.forEach{
            entry -> jsonText += "${"\t".repeat(identLevel + 1)}\"${entry.key}\":${entry.value.getText(identLevel + 1)},\n"
        }
        jsonText = jsonText.removeSuffix(",\n")
        jsonText += "\n${"\t".repeat(identLevel)}}"
        return jsonText
    }

    override fun toString(): String {
        return getJsonText()
    }

    override fun accept(visitor: (JsonElement) -> Unit){
        visitor(this)
        map.values.forEach{
            value -> value.accept(visitor)
        }
    }

    override fun getText(identLevel: Int): String {
        return getJsonText(identLevel)
    }

    fun filter(predicate: (JsonElement) -> Boolean): JsonObject{
        val filteredMap = mutableMapOf<String, JsonElement>()
        map.forEach{ (key, value) ->
            value.accept {
                if (predicate(it))
                    filteredMap[key] = value
                }
        }
        return JsonObject(filteredMap)
    }
}