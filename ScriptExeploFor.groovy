import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;
import java.text.DecimalFormat 

def Message processData(Message message) {
    
    def body     = message.getBody(String)
    def jsonFile = new JsonSlurper()
    def jsonObj  = jsonFile.parseText(body)

     def json
    def array = []
    
    for(int i = 0; i < jsonObj.d.results.size(); i++){
        json = JsonOutput.toJson(
                'Primeiro nome' : jsonObj.d.results[i].firstName,
                Titulo : jsonObj.d.results[i].title,
                Email : jsonObj.d.results[i].email,
                Localização: jsonObj.d.results[i].location
            )
            def jsonObj2  = jsonFile.parseText(json)
        for(int x = 0; x < jsonObj.d.results[i].empInfo.personNav.phoneNav.results.size(); x++){
            def tx = x + 1
            
            jsonObj2."Telefone $tx" = jsonObj.d.results[i].empInfo.personNav.phoneNav.results[x].phoneNumber

        }
        json = JsonOutput.toJson(jsonObj2)
        array.push(json)
    }
    
    // message.setBody(JsonOutput.prettyPrint(array.toString()))
    message.setBody(array.toString())
    // message.setBody(json)
    
    message.setHeader("Content-Type", "application/json")

    return message;
}
