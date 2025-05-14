@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Mapping(val value: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param

class Controller(){

    @Mapping("object")
    fun getObject() : JsonObject{
        return Data.data
    }

    @Mapping("filter/object")
    fun filterObject(
        @Param key: String? = null, @Param type: String? = null, @Param op: String? = null, @Param value: String? = null
    ) : JsonObject {
        val json = Data.data

        return try {
            json.filter { k, v ->
                val matchKey = key == null || k == key
                val matchType = matchType(v, type)
                val matchValue = matchValue(v,op,value)

                matchKey && matchType && matchValue
            }
        } catch (e : Exception){
            error("Error calling the filterObject, problem with the parameters")
            JsonObject(mutableMapOf())
        }
    }

    @Mapping("filter/array")
    fun filterArray(@Param type: String? = null, @Param op: String? = null, @Param value: String? = null) : JsonArray<JsonElement>{
        // como ir buscar o array?
        val jsonArray = Data.array
        return try{
            jsonArray.filter { element ->
                val matchType = matchType(element, type)
                val matchValue = matchValue(element,op,value)

                matchType && matchValue
            }

        }catch(e : Exception){
            error("Error calling the filterArray, problem with the parameters")
            JsonArray(mutableListOf())
        }

    }

    @Mapping("map/array")
    fun mapArray(
        @Param op: String,
        @Param value: Int
    ) : JsonArray<JsonElement>{
        val jsonArray = Data.array
        return jsonArray.map { element ->
            if(element is JsonNumber){
                mapOperation(element, op, value)
            }else{
                JsonNull
            }
        }
    }

    @Mapping("validate/object")
    fun validateObject(): Boolean{
        val json = Data.data

        return json.isValid()
    }

    @Mapping("validate/array")
    fun validateArray(): Boolean{
        val jsonArray = Data.array

        return jsonArray.isValid()
    }

    @Mapping("toString")
    fun toJsonText(): String{
        return Data.data.getText()
    }

    private fun matchType(element : JsonElement, type: String?): Boolean{
        return type == null || element::class.simpleName == type
    }

    private fun matchValue(element: JsonElement, op:String?, value: String?) : Boolean{
        return when {
            value == null -> true
            element is JsonString && op == "=" -> element.content == value
            element is JsonNumber && op == ">" -> element.content.toDouble() > value.toDouble()
            element is JsonNumber && op == ">=" -> element.content.toDouble() >= value.toDouble()
            element is JsonNumber && op == "<" -> element.content.toDouble() < value.toDouble()
            element is JsonNumber && op == "<=" -> element.content.toDouble() <= value.toDouble()
            element is JsonNumber && op == "=" -> element.content.toDouble() == value.toDouble()
            else -> false
        }
    }

    private fun mapOperation(element : JsonNumber, op: String, value: Int) : JsonElement{
        return when {
            op == "+" -> JsonNumber(element.content.toDouble() + value.toDouble())
            op == "-" -> JsonNumber(element.content.toDouble() - value.toDouble())
            op == "*" -> JsonNumber(element.content.toDouble() * value.toDouble())
            op == "/" -> JsonNumber(element.content.toDouble() / value.toDouble())
            else -> JsonNull
        }
    }
}