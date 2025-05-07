import javax.sql.rowset.Predicate
import kotlin.reflect.KClass

interface JsonVisitor {
    fun visit(element: JsonElement)
    fun visit(array: JsonArray<*>)
    fun visit(obj: JsonObject)
}

class JsonValidator : JsonVisitor {
    private var isValid = true
    override fun visit(element: JsonElement) {}

    override fun visit(array: JsonArray<*>) {
        var type : KClass<out JsonElement>? = null
        array.content.forEach {
            if(it is JsonNull || (type!=null && it::class!=type)) {
                isValid = false
                return
            }
            type = it::class
        }
    }

    override fun visit(obj: JsonObject) {}

    fun isValidJson(): Boolean = isValid
}