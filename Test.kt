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
        val jsonElement = JsonString("Mundo");
        val content: MutableMap<String, JsonElement> = mutableMapOf("Olá" to jsonElement) // Tenho de perguntar isto ao prof

        val jsonObject = JsonObject(content)
        assertEquals(jsonObject.getJsonText(), "{\n\tOlá: \"Mundo\"\n}")
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

}