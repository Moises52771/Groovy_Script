import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;

def ArrayList guaranteArray(input) { // Garante que o payload será um array
    
    if (input.getClass() != java.util.ArrayList) return [input]       

    return input
}

def Message processData(Message message){       

    def body              = message.getBody(String)
    def bodyDateFormatted = body.replaceAll(/(?<=\d{4}-\d{2}-\d{2}) (?=\d{2}:\d{2}:\d{2}\.\d+)/, "T")
    def jsonParser        = new JsonSlurper()
    def jsonObject        = jsonParser.parseText(bodyDateFormatted)

    def array = []
   

    if(jsonObject.mt_rest_response == ""){
        
       message.setBody("[]")
       
    } else {

        def mainJson = guaranteArray(jsonObject.mt_rest_response.row)

        mainJson.each{ e ->
        
            def json = jsonParser.parseText('{}')
       
            e.each { key, value -> 
                json[key.toLowerCase()] = value
            }
            array.push(json)

        }
        message.setBody(JsonOutput.toJson(array))
    
    }        
    
    return message;
}
