import kotlin.reflect.full.*

/**
 * Represents a JSON Array.
 *
 * @param T the type of elements, which must be [JsonElement]
 * @property content a list of JSON Elements.
 *
 */
data class JsonArray<T : JsonElement>(val content: List<T>) : JsonElement() {
    override fun getText(identLevel: Int): String {
        var jsonArrayText = "[\n"
        content.forEach { element ->
            jsonArrayText += "${"\t".repeat(identLevel + 1)}${element.getText(identLevel + 1)},\n"
        }
        jsonArrayText = jsonArrayText.removeSuffix(",\n")
        jsonArrayText += "\n${"\t".repeat(identLevel)}]"
        return jsonArrayText
    }

    /**
     * Filters the array elements based on a predicate.
     *
     * @param predicate a function to test each element.
     * @return a new JSON Array with elements that match the predicate.
     *
     * @sample
     * ```kotlin
     * val jsonArray = JsonArray(listOf(JsonString("apple"), JsonString("banana"), JsonString("cherry")))
     * val filtered = jsonArray.filter { (it as JsonString).content.startsWith("b") }
     * println(filtered) // JsonArray with "banana"
     * ```
     */
    fun filter(predicate: (JsonElement) -> Boolean): JsonArray<JsonElement> {
        val filteredList = mutableListOf<JsonElement>()
        content.forEach { element ->
            if (predicate(element)) filteredList.add(element)
        }
        return JsonArray(filteredList)
    }


    /**
     * Transforms each element using the given function.
     *
     * @param predicate a function to apply to each element.
     * @return a new JSON Array with the mapped elements.
     *
     * @sample
     * ```kotlin
     * val jsonArray = JsonArray(listOf(JsonString("apple"), JsonString("banana")))
     * val mapped = jsonArray.map {
     *     val str = (it as JsonString).content.uppercase()
     *     JsonString(str)
     * }
     * println(mapped) // JsonArray with "APPLE", "BANANA"
     * ```
     */
    fun map(predicate: (JsonElement) -> JsonElement): JsonArray<JsonElement> {
        val map = mutableListOf<JsonElement>()
        content.forEach { element ->
            map.add(predicate(element))
        }
        return JsonArray(map)
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visit(this)
        this.content.forEach { it.accept(visitor) }
    }

    override fun toString() = getText()
}

/**
 * Represents a JSON String Value
 *
 * @property content the string content.
 */
data class JsonString(val content: String) : JsonPrimitive() {
    override fun getText(identLevel: Int): String {
        return "\"${content}\""
    }

    override fun toString() = getText()
}

/**
 * Represents a JSON Number value.
 *
 * @property content the numeric value.
 */
data class JsonNumber(val content: Number) : JsonPrimitive() {
    override fun getText(identLevel: Int): String {
        return content.toString()
    }

    override fun toString() = getText()
}

/**
 * Represents a JSON Boolean value.
 *
 * @property content the boolean value.
 */
data class JsonBoolean(val content: Boolean) : JsonPrimitive() {
    override fun getText(identLevel: Int): String {
        return content.toString()
    }

    override fun toString() = getText()
}


/**
 * Represents the JSON Null value.
 */
object JsonNull : JsonPrimitive() {
    override fun getText(identLevel: Int): String {
        return "null"
    }

    override fun toString() = getText()
}


/**
 * Represents a JSON Object composed of key-value pairs.
 *
 * @property map a map with keys as strings and values as JSON elements.
 */
data class JsonObject(val map: Map<String, JsonElement>) : JsonElement() {
    override fun getText(identLevel: Int): String {
        var jsonText = "{\n"
        map.forEach { entry ->
            jsonText += "${"\t".repeat(identLevel + 1)}\"${entry.key}\":${entry.value.getText(identLevel + 1)},\n"
        }
        jsonText = jsonText.removeSuffix(",\n")
        jsonText += "\n${"\t".repeat(identLevel)}}"
        return jsonText
    }

    /**
     * Filters the key-value pairs based on a predicate.
     *
     * @param predicate a function that receives key and value and returns true if the entry should be kept.
     * @return a new JSON Object with the filtered entries.
     *
     * @sample
     * ```kotlin
     * val jsonObject = JsonObject(mapOf(
     *     "name" to JsonString("Filipa"),
     *     "age" to JsonNumber(28)
     * ))
     * val filtered = jsonObject.filter { _, value -> value is JsonString }
     * println(filtered) // JsonObject with only "name" entry
     * ```
     */
    fun filter(predicate: (String, JsonElement) -> Boolean): JsonObject {
        val filteredMap = mutableMapOf<String, JsonElement>()
        map.forEach { (key, value) ->
            if (predicate(key, value)) filteredMap[key] = value
        }
        return JsonObject(filteredMap)
    }

    override fun accept(visitor: JsonVisitor) {
        visitor.visit(this)
        map.values.forEach { it.accept(visitor) }
    }

    override fun toString() = getText()
}

/**
 * Represents a primitive JSON Element (string, number, boolean or null).
 */
sealed class JsonPrimitive : JsonElement() {
    override fun accept(visitor: JsonVisitor) {
        visitor.visit(this)
    }
}


/**
 * Represents a generic Json Element
 */
sealed class JsonElement {
    /**
     * Accepts a visitor to traverse the JSON structure.
     *
     * @param visitor the JsonVisitor.
     */
    abstract fun accept(visitor: JsonVisitor)

    /**
     * Returns the JSON Element serialized to a string, following the standard. For more information about the standard, see [json.org](https://www.json.org/).
     *
     * @param identLevel the ident level of the current JsonElement
     * @return the string which represents the JsonElement
     */
    internal abstract fun getText(identLevel: Int = 0): String


    /**
     * Validates this JSON Element using the [JsonValidator], which leverages the Visitor pattern.
     *
     * @return True if valid JSON, false otherwise.
     */
    open fun isValid(): Boolean {
        val jsonValidator = JsonValidator()
        this.accept(jsonValidator)
        return jsonValidator.isValidJson()
    }

    override fun toString() = getText()
}

/**
 * Builds a JSON Element from any Kotlin object. This function is only supported for the following Kotlin types:
 * - Int
 * - Double
 * - Boolean
 * - String
 * - List< supported type >
 * - Enums
 * - null
 * - data classes with properties whose type is supported
 * - maps (Map) that associate Strings (keys) to any of the above Kotlin elements
 *
 *
 * @param value the value to be converted.
 * @return a JSON Element representing the value.
 *
 * @sample
 * ```kotlin
 * data class Person(val name: String, val age: Int)
 * val person = Person("Eduardo", 30)
 * val personJson = createJsonElementFromObject(person)
 * println(personJson)
 * ```
 */
fun createJsonElementFromObject(value: Any?): JsonElement {
    if (value == null) return JsonNull
    return when (value) {
        is String -> JsonString(value)
        is Number -> JsonNumber(value)
        is Boolean -> JsonBoolean(value)
        is List<*> -> {
            JsonArray(value.map { createJsonElementFromObject(it) })
        }

        is Enum<*> -> JsonString(value.name.uppercase())
        is Map<*, *> -> {
            if (!value.keys.all { it is String }) {
                throw IllegalArgumentException("It is not possible to create a JsonObject from a map where the keys are not strings.")
            } else {
                val map = value.entries.associate {
                    val key = it.key as String
                    val jsonValue = createJsonElementFromObject(it.value)
                    key to jsonValue
                }
                JsonObject(map)
            }
        }

        else -> {
            val clazz = value::class
            val constructorParams = clazz.primaryConstructor?.parameters.orEmpty()
            val map = constructorParams.mapNotNull { param ->
                val property = clazz.memberProperties.find { it.name == param.name }
                property?.name?.let { name ->
                    name to createJsonElementFromObject(property.call(value))
                }
            }.toMap()
            JsonObject(map)
        }
    }
}