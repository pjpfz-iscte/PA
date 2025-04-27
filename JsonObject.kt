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
}