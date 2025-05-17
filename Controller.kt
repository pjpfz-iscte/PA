@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class Mapping(val value: String)

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Path

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Param

@Mapping("api")
class Controller{

    @Mapping("ints")
    fun demo(): List<Int> = listOf(1, 2, 3)

    @Mapping("pair")
    fun obj(): Pair<String, String> = Pair("um", "dois")

    @Mapping("path/{pathvar}")
    fun path(
        @Path pathvar: String
    ): String = pathvar + "!"

    @Mapping("args")
    fun args(
        @Param n: Int,
        @Param text: String
    ): Map<String, String> = mapOf(text to text.repeat(n))

}