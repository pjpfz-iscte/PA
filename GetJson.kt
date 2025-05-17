import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpExchange
import kotlin.reflect.*
import kotlin.reflect.full.*
import java.net.InetSocketAddress
import java.net.URI

class GetJson(
    private val controller: KClass<*>
){
    private var server: HttpServer? = null
    private val controllerInstance = controller.createInstance()

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


    fun start(port : Int){
        server = HttpServer.create(InetSocketAddress(port), 0)

        val basePath = controller.findAnnotation<Mapping>()?.value ?: ""
        val methods = controller.declaredMemberFunctions.filter { it.findAnnotation<Mapping>() != null }

        for(method in methods){
            registerEndpoint(method, basePath)
        }

        server!!.executor = null
        server!!.start()
    }

    fun stop(){
        server?.stop(0)
        server = null
    }


    private fun registerEndpoint(method: KFunction<*>, basePath: String){
        val branchPath = method.findAnnotation<Mapping>()!!.value
        val fullPath = normalizePath("/$basePath/$branchPath")
        val pathInfo = buildPathRegex(fullPath)

        server!!.createContext(pathInfo.regexPattern.replace("\\", "")){ request ->
            handleRequest(request, method, pathInfo)
        }
    }

    private fun handleRequest(request: HttpExchange, method: KFunction<*>, pathInfo: PathInfo){
        if(request.requestMethod != "GET"){
            sendResponse(request, 405, "Method Must Be GET")
            return
        }

        val pathMatch = pathInfo.regex.matchEntire(request.requestURI.path)
        if(pathMatch == null){
            sendResponse(request, 404, "Not Found")
            return
        }

        val pathArgs = pathInfo.pathVars.zip(pathMatch.groupValues.drop(1)).toMap()
        val queryParams = parseQueryParams(request.requestURI)

        val args = buildArguments(method, pathArgs, queryParams)

        try{
            val result = method.call(*args.toTypedArray())
            val resultToJsonElement = createJsonElementFromObject(result).getText()
            sendResponse(request, 200, resultToJsonElement)
        }catch(e : Exception){
            sendResponse(request, 500, "Server Error: $e")
        }
    }

    private fun sendResponse(request: HttpExchange, status: Int, message: String){
        val response = message.toByteArray()
        request.sendResponseHeaders(status, response.size.toLong())
        request.responseBody.write(response)
        request.responseBody.close()
    }


    private fun normalizePath(path: String): String =
        path.replace(Regex("/+"), "/").trimEnd('/')

    private fun buildPathRegex(path: String): PathInfo {
        val pathVars = Regex("\\{(\\w+)}").findAll(path).map { it.groupValues[1] }.toList()
        val pattern = path.replace(Regex("\\{\\w+}"), "([^/]+)")
        return PathInfo(Regex("^$pattern$"), pathVars, pattern)
    }

    private fun parseQueryParams(uri: URI): Map<String, String> =
        uri.query?.split("&")?.mapNotNull {
            val (key,value) = it.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
            key to value
        }?.toMap() ?: emptyMap()

    private fun buildArguments(method: KFunction<*>, pathArgs: Map<String, String>, queryParams : Map<String, String>) : List<*>{
        val args = mutableListOf<Any?>()

        for ( parameter in method.parameters){
            when(parameter.kind){
                KParameter.Kind.INSTANCE -> args += controllerInstance
                else -> when{
                    parameter.findAnnotation<Path>() != null -> args += pathArgs[parameter.name]
                    parameter.findAnnotation<Param>() != null -> args += convertParam(queryParams[parameter.name] ?: error("Missing parameters"), parameter.type)
                    else -> null
                }
            }
        }
        return args
    }

    private fun convertParam(value: String, type: KType): Any{
        val cls = type.classifier as? KClass<*> ?: throw IllegalArgumentException("Unsupported Type")
        return when (cls){
            Int::class -> value!!.toInt()
            String::class -> value!!
            else -> throw IllegalArgumentException("Unsupported parameter type")
        }
    }



    private data class PathInfo(
        val regex: Regex,
        val pathVars: List<String>,
        val regexPattern: String
    )



    // temos de estar à espera de um pedido http, quando recebermos o pedido ver se há alguma anotação que corresponda a esse pedido,
    // invocar a função e converter o resultado para json e enviar

}


