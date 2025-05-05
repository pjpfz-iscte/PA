import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

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
        val jsonArray = JsonArray(arrayOf(jsonString, jsonString))
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
        val obtainedText = jsonTextTest.getJsonText()
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
        println(obtainedText)
        assertEquals(obtainedText, expectedText)
    }

    @Test
    fun testeFilterJsonArray(){
        val json = JsonArray(arrayOf(
            JsonNumber(1),
            JsonString("hello"),
            JsonNumber(4)
        ))

        val resultado = json.filter { it is JsonNumber }
        println(resultado)
    }

    @Test
    fun testeFilterJsonObject(){
        val obj = JsonObject(
            mutableMapOf(
                "name" to JsonString("Alice"),
                "age" to JsonNumber(30),
                "location" to JsonString("aqui"),
                "codigo-postal" to JsonNull,
                "outro" to JsonBoolean(true)
            )
        )

        val resultado = obj.filter { it is JsonString }
        println(resultado)
    }

    @Test
    fun testeMapJsonArray(){
        val json = JsonArray(arrayOf(
            JsonNumber(1),
            JsonNumber(10),
            JsonNumber(4)
        ))

        val resultado = json.map { if (it is JsonNumber) JsonNumber(it.content.toInt() * 2)
        else it}
        println(resultado)
    }
}