import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.io.File

class Test {

    @Test
    fun testConstructor(){
        val jsonElement = JsonElement.JsonString("Mundo");
        val content: MutableMap<String, JsonElement> = mutableMapOf("Olá" to jsonElement) as MutableMap<String, JsonElement> // Tenho de perguntar isto ao prof

        val jsonObject = JsonObject(content)
    }

    @Test
    fun testGetJsonText(){
        val jsonElement = JsonElement.JsonString("Mundo");
        val content: MutableMap<String, JsonElement> = mutableMapOf("Olá" to jsonElement) as MutableMap<String, JsonElement> // Tenho de perguntar isto ao prof

        val jsonObject = JsonObject(content)
        assertEquals(jsonObject.getJsonText(), "{\n\tOlá: \"Mundo\"\n}")
    }

    @Test
    fun testeFilterJsonArray(){
        val json = JsonElement.JsonArray(arrayOf(
            JsonElement.JsonNumber(1),
            JsonElement.JsonString("hello"),
            JsonElement.JsonArray(arrayOf(
                JsonElement.JsonNumber(99),
                JsonElement.JsonBoolean(true)
            ))
        ))

        val resultado = json.filter { it is JsonElement.JsonNumber }
        println(resultado)
    }

}