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


}