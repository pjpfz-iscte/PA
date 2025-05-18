import com.sun.net.httpserver.HttpServer
import com.sun.net.httpserver.HttpExchange
import kotlin.reflect.*
import kotlin.reflect.full.*
import java.net.InetSocketAddress
import java.net.URI

/**
 * Base controller class to define API endpoints
 *
 * The GetJson framework uses reflection to detect functions with annotations, such as @Mapping,
 * and bind them to specific HTTP routes automatically
 *
 * @constructor Creates an instance of a controller. No parameters required
 */

class GetJson(
    private val controller: KClass<*>
){
    private var server: HttpServer? = null
    private val controllerInstance = controller.createInstance()

    /**
     * Initializes the controller by printing the base path and all method endpoints annotated with @Mapping
     */
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

    /**
     * Starts the HTTP serer on the specified port and registers all controller routes
     *
     * @param port the port number the server will listen on
     *
     * It initializes the serve, collects all methods in the controller annotated with @Mapping, builds route information
     * including regex matching for dynimic paths, and handles incoming requets by routing them to the appropriate method
     */
    fun start(port : Int){
        server = HttpServer.create(InetSocketAddress(port), 0)

        val basePath = controller.findAnnotation<Mapping>()?.value ?: ""
        val methods = controller.declaredMemberFunctions.filter { it.findAnnotation<Mapping>() != null }

        val routes = methods.map{
            val branchPath = it.findAnnotation<Mapping>()!!.value
            val fullPath = normalizePath("/$basePath/$branchPath")
            val pahtInfo = buildPathRegex(fullPath)
            Route(it, pahtInfo)
        }

        server!!.createContext("/") { request ->
            val path = request.requestURI.path
            val route = routes.firstOrNull { it.pathInfo.regex.matches(path)}

            if(route == null){
                sendResponse(request, 404, "Endpoint Not Found")
            }else{
                handleRequest(request, route.method, route.pathInfo)
            }
        }

        server!!.executor = null
        server!!.start()
    }

    /**
     * Stops the running HTTP server
     *
     * Shuts down the server immediately if it's running and sets the server reference to null
     */
    fun stop(){
        server?.stop(0)
        server = null
    }


    /**
     * Handles an incoming HTTP GET request, invoking the corresponding controller method, extracting
     * path and query parameters, and sending back the JSON response
     */
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

    /**
     * Sends an HTTP response with the given status code and message body
     *
     * @param request The HttpExchange representing the client request
     * @param status The HTTP status code to send in the response
     * @param message The response body content as a string
     */
    private fun sendResponse(request: HttpExchange, status: Int, message: String){
        val response = message.toByteArray()
        request.sendResponseHeaders(status, response.size.toLong())
        request.responseBody.write(response)
        request.responseBody.close()
    }

    /**
     *Normalizes a URL path by replacing multiple slashes with a single slash and removing
     * any trailing slash
     *
     * @param path The raw path string to normalize
     * @return A normalized path string without trailing slashes and with single separators
     */
    private fun normalizePath(path: String): String =
        path.replace(Regex("/+"), "/").trimEnd('/')

    /**
     * Builds a regex pattern to match paths with variables
     *
     * @param path the string possibly containing variables in {var} format
     * @return A [PathInfo] object containing the compiled regex, list of variable names, and the
     * regex pattern as a string
     */
    private fun buildPathRegex(path: String): PathInfo {
        val pathVars = Regex("\\{(\\w+)}").findAll(path).map { it.groupValues[1] }.toList()
        val pattern = path.replace(Regex("\\{\\w+}"), "([^/]+)")
        return PathInfo(Regex("^$pattern$"), pathVars, pattern)
    }

    /**
     * Parses the query parameter from a URI into a map
     *
     * @param uri the URI containing the query string
     * @return a map of query parameter names to their values, or an empty map if none exist
     */
    private fun parseQueryParams(uri: URI): Map<String, String> =
        uri.query?.split("&")?.mapNotNull {
            val (key,value) = it.split("=").takeIf { it.size == 2 } ?: return@mapNotNull null
            key to value
        }?.toMap() ?: emptyMap()

    /**
     * Constructs a list of arguments to pass to a controller method by resolving path and query
     * parameters based on annotations
     *
     * @param method the function to be invoked
     * @param pathArgs map of path variable names to their values
     * @param queryParams map of query parameter names to their values
     * @return a list of arguments in the correct order for the method call
     *
     * @throws IllegalArgumentException if a required parameter is missing
     */
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

    /**
     * Converts a string value into an instance of the specified KType
     *
     * @param value the string representation of the value
     * @param type the expected Kotlin type
     * @return the converted value as an instance of the specified type
     *
     * @throws IllegalArgumentException if the type is not supported
     */
    private fun convertParam(value: String, type: KType): Any{
        val cls = type.classifier as? KClass<*> ?: throw IllegalArgumentException("Unsupported Type")
        return when (cls){
            Int::class -> value!!.toInt()
            String::class -> value!!
            else -> throw IllegalArgumentException("Unsupported parameter type")
        }
    }


    /**
     * Represents parsed information about a path pattern
     *
     * @property regex the compiled regex used to match incoming paths
     * @property pathVars the list of variable names extracted from the path
     * @property regexPattern the string form of the regex used in matching
     */
    private data class PathInfo(
        val regex: Regex,
        val pathVars: List<String>,
        val regexPattern: String
    )

    /**
     * Holds a route's handler method and its associated path information
     *
     * @property method the function to invoke when this route is matched
     * @property pathInfo the parsed path pattern and path variables
     */
    private data class Route(
        val method: KFunction<*>,
        val pathInfo: PathInfo
    )

}


