class JsonObject (val map: MutableMap<String,JsonElement>):  JsonElement(){
    fun getJsonText(): String{
        var jsontext = "{\n"
        map.forEach{
            entry -> jsontext+="\t${entry.key}: ${entry.value.toString()}\n"
        }
        jsontext += "}"

        return jsontext
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