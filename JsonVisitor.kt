import kotlin.reflect.KClass

/**
 * Interface for visiting JSON elements.
 */
interface JsonVisitor {
    /**
     * Visits a [JsonPrimitive].
     *
     * @param primitive the JSON primitive to visit.
     */
    fun visit(primitive: JsonPrimitive)

    /**
     * Visits a [JsonArray].
     *
     * @param array the JSON Array to visit.
     */
    fun visit(array: JsonArray<*>)

    /**
     * Visits a [JsonObject].
     *
     * @param obj the JSON Object to visit.
     */
    fun visit(obj: JsonObject)
}
/**
 * A visitor that validates JSON elements.
 *
 * Specifically, it checks if a [JsonArray] contains elements of the same type (and different from JSON Null). If mixed types are found,the array is considered invalid.
 */
class JsonValidator : JsonVisitor {
    private var isValid = true

    /**
     * Visits a [JsonPrimitive]. No validation is performed in this case because all JSON Primitives are valid.
     *
     * @param primitive the primitive to visit.
     */
    override fun visit(primitive: JsonPrimitive) {}

    /**
     * Validates a [JsonArray] by checking if all elements are of the same type and different from JSON Null.
     *
     * @param array The array to validate.
     */
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

    /**
     * Visits a [JsonObject]. No validation is performed in this case.
     *
     * @param obj The object to visit.
     */
    override fun visit(obj: JsonObject) {}

    /**
     * Returns whether the visited content is considered valid JSON.
     *
     * @return True if valid, false otherwise.
     */
    fun isValidJson(): Boolean = isValid
}