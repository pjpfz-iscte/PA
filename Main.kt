fun main(){
    var app = GetJson(Controller::class)
    app.start(0)

}


object Data{
    lateinit var data: JsonObject
    lateinit var array: JsonArray<*>
}


