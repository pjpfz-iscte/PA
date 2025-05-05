class JsonObject (val map: MutableMap<String,JsonElement>):  JsonElement(){
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
        map.forEach{ (key, value) ->
            value.accept {
                if (predicate(it))
                    filteredMap[key] = value
                }
        }
        return JsonObject(filteredMap)
    }

    fun isValid(): Boolean{
        return true
    }
}