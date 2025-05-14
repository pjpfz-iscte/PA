import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import java.net.http.HttpRequest
import java.net.http.HttpClient

class GetJson(
    private val controller: KClass<*>
){
    private var port: Int? = null

    init {
        val basePath = controller.findAnnotation<Mapping>()?.value ?: ""
        println("Base Path: $basePath")

        for (apiPoint in controller.memberFunctions){
            val path = apiPoint.findAnnotation<Mapping>()?.value
            if (path != null){
                val apiPointPath = "$basePath/$path"
                println("Endpoint: $apiPointPath")
            }
        }
    }


    fun start(port : Int): Boolean{
        // inicializa endpoints e retorna um boolean de estado
        // if(tudo correr bem){
        this.port = port
        val cont = Controller()
        Data.data = JsonObject(
            mutableMapOf(
                "name" to JsonString("Alice"),
                "age" to JsonNumber(30),
                "age2" to JsonNumber(15),
                "age3" to JsonNumber(5),
                "location" to JsonString("aqui"),
                "codigo-postal" to JsonNull,
                "outro" to JsonBoolean(true),
            )
        )
        println(cont.filterObject(key = "name").getText())
        println(cont.filterObject(null, type = "JsonNumber", op = ">=", value = "15").getText())
        return true
    }

    fun stop(): Boolean{
        // fecha endpoints e retorna um boolean de estado
        TODO()
    }

    // temos de estar à espera de um pedido http, quando recebermos o pedido ver se há alguma anotação que corresponda a esse pedido,
    // invocar a função e converter o resultado para json e enviar
}