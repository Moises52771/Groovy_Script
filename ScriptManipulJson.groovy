import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;
import java.nio.charset.StandardCharsets
import java.util.Date
import java.text.SimpleDateFormat

def Message processData(Message message) {
    manipulaJson(message)
    return message;
}

def void manipulaJson(Message message){
    def body     = message.getBody(String)
    def jsonFile = new JsonSlurper()
    def jsonObj  = jsonFile.parseText(body)

    jsonObj.remove('ibge')
    jsonObj.remove('gia')
    jsonObj.remove('siafi')

    
    Date date = new Date()
    date.setTime(date.getTime() - (3*3600000))
    SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")

    jsonObj.data = formatador.format(date)


    def json = new JsonBuilder(jsonObj).toPrettyString()
        json = json.replace("logradouro", "rua")

    def json2 = JsonOutput.toJson(
            'zip code' : jsonObj.cep,
            street : jsonObj.logradouro,
            complement : jsonObj.complemento,
            district : jsonObj.bairro,
            locality : jsonObj.localidade,
            state : jsonObj.uf,
            ddd: jsonObj.ddd,
            date: jsonObj.data
    )

    message.setBody(JsonOutput.prettyPrint(json2))

    message.setBody(json2) 

}
