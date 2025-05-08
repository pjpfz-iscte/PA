import org.junit.Assert.*
import org.junit.Test

class Test {

    @Test
    fun testConstructor(){
        val jsonElement = JsonString("Mundo");
        val content: MutableMap<String, JsonElement> = mutableMapOf("Olá" to jsonElement) // Tenho de perguntar isto ao prof

        val jsonObject = JsonObject(content)
    }

    @Test
    fun testGetJsonText(){
        val jsonString = JsonString("String")
        val jsonArray = JsonArray(mutableListOf(jsonString, jsonString))
        val jsonInt = JsonNumber(2)
        val jsonFloat = JsonNumber(2.4)
        val jsonBoolean = JsonBoolean(true)
        val jsonNull = JsonNull
        val jsonObject = JsonObject(mutableMapOf("Teste" to jsonString))
        val content: MutableMap<String, JsonElement> = mutableMapOf<String, JsonElement>(
            "jsonString" to jsonString,
            "jsonArray" to jsonArray,
            "jsonInt" to jsonInt,
            "jsonFloat" to jsonFloat,
            "jsonBoolean" to jsonBoolean,
            "jsonNull" to jsonNull,
            "jsonObject" to jsonObject
        )

        val jsonTextTest = JsonObject(content)
        val obtainedText = jsonTextTest.getText()
        val expectedText = "{\n" +
                "\t\"jsonString\":\"String\",\n" +
                "\t\"jsonArray\":[\n" +
                "\t\t\"String\",\n" +
                "\t\t\"String\"\n" +
                "\t],\n" +
                "\t\"jsonInt\":2,\n" +
                "\t\"jsonFloat\":2.4,\n" +
                "\t\"jsonBoolean\":true,\n" +
                "\t\"jsonNull\":null,\n" +
                "\t\"jsonObject\":{\n" +
                "\t\t\"Teste\":\"String\"\n" +
                "\t}\n" +
                "}"
        assertEquals(obtainedText, expectedText)
    }

    @Test
    fun testeFilterJsonArray(){
        val json = JsonArray(
            mutableListOf(
            JsonNumber(1),
            JsonString("hello"),
            JsonNumber(4),
            JsonArray(mutableListOf(JsonNumber(5), JsonNumber(3)))
        )
        )

        val resultado = json.filter { it is JsonNumber }
        println(resultado.getText())
        val expected = "[\n"+ "\t1,"+ "\n\t4" +"\n]"
        assertEquals(expected, resultado.getText())
    }

    @Test
    fun testeFilterJsonObject(){
        val obj = JsonObject(
            mutableMapOf(
                "name" to JsonString("Alice"),
                "age" to JsonNumber(30),
                "location" to JsonString("aqui"),
                "codigo-postal" to JsonNull,
                "outro" to JsonBoolean(true),
            )
        )
        val resultado = obj.filter { s, jsonElement ->  jsonElement is JsonNumber}
        val expected = "{\n"+ "\t\"age\":30"+ "\n}"
        println(resultado.getText())
        assertEquals(expected, resultado.getText())
    }

    @Test
    fun testeMapJsonArray(){
        val json = JsonArray(
            mutableListOf(JsonNumber(1),
            JsonString("olá"),
            JsonNumber(2))
        )

        val resultado = json.map { if (it is JsonNumber) JsonNumber(it.content.toInt() * 2)
        else it}
        print(resultado)
        val expected = "[\n"+ "\t2,\n" + "\t\"olá\",\n" + "\t4" + "\n]"
        println(resultado.getText())
        assertEquals(expected, resultado.getText())
    }

    @Test
    fun testIsValidJsonObject(){
        val jsonObject1 = JsonObject(mutableMapOf("Olá" to JsonString("Mundo")))
        print(jsonObject1.isValid())
        assertTrue(jsonObject1.isValid())
        val jsonNull = JsonNull
        val jsonObject2 = JsonObject(mutableMapOf("Olá" to JsonString("Mundo"), "jsonArray" to JsonArray<JsonElement>(
            mutableListOf(jsonNull,jsonNull,jsonNull)
        )))
        assertFalse(jsonObject2.isValid())
    }

    @Test
    fun testIsValidJsonArray(){
        val jsonString = JsonString("String")
        val jsonNull = JsonNull
        val jsonObject = JsonObject(mutableMapOf("Teste" to jsonString))

        val jsonArray1 = JsonArray<JsonElement>(mutableListOf(jsonString))
        assertTrue(jsonArray1.isValid())
        val jsonArray2 = JsonArray<JsonElement>(mutableListOf(jsonObject,jsonObject))
        assertTrue(jsonArray2.isValid())
        val jsonArray3 = JsonArray<JsonElement>(mutableListOf(jsonArray1,jsonArray1,jsonArray1))
        assertTrue(jsonArray3.isValid())
        val jsonArray4 = JsonArray<JsonElement>(mutableListOf(jsonNull,jsonNull,jsonNull))
        assertFalse(jsonArray4.isValid())
        val jsonArray5 = JsonArray<JsonElement>(mutableListOf(jsonString,jsonObject,jsonArray1))
        assertFalse(jsonArray5.isValid())
    }


    data class Course(
        val name: String,
        val credits: Int,
        val evaluation: List<EvalItem>
    )


    data class EvalItem(
        val name: String,
        val percentage: Double,
        val mandatory: Boolean,
        val type: EvalType?
    )


    enum class EvalType {
        TEST, PROJECT, EXAM
    }

    @Test
    fun testJsonObjectFromObject() {
        val course = Course(
            "PA", 6, listOf(
                EvalItem("quizzes", .2, false, null),
                EvalItem("project", .8, true, EvalType.PROJECT)
            )
        )

        val expectedText = """{
                 "name": "PA",
                 "credits": 6,
                 "evaluation": [
                   {
                     "name": "quizzes",
                     "percentage": 0.2,
                     "mandatory": false,
                     "type": null
                   },
                   {
                     "name": "project",
                     "percentage": 0.8,
                     "mandatory": true,
                     "type": "PROJECT"
                   }
                 ]
                }
             """

        assertEquals(JsonNull, jsonElementFromObject(null))
        val string = "a"
        assertEquals(JsonString(string), jsonElementFromObject(string))
        val int = 1
        val double = 1.2
        assertEquals(JsonNumber(int), jsonElementFromObject(int))
        assertEquals(JsonNumber(double), jsonElementFromObject(double))
        val boolean = true
        assertEquals(JsonBoolean(boolean), jsonElementFromObject(boolean))
    }




}