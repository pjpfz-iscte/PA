/**
 * Starts the GerJson in the given port
 */
fun main(){
    var app = GetJson(Controller::class)
    app.start(8080)
}

