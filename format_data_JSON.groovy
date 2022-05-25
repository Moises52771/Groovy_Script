import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;

def Message processData(Message message) {
    
    def body = message.getBody(String)
    def jsonParser = new JsonSlurper()
    def jsonObject = jsonParser.parseText(body)
    
    def json_ecc = JsonOutput.toJson(
        
        Codigo : jsonObject.Code,
        Descricao : jsonObject.Description,
        GrupoMateial : jsonObject.MaterialGroup,
        CriadoPor : jsonObject.CreatedBy,
        ModificadoPor : jsonObject.ModifiedBy, 
        UltimaModificacao : Date.parse("dd-MM-yyyy", jsonObject."Last Modified").format("yyyyMMdd"), 
        Ativo : jsonObject.Active 
        );
        
    message.setBody(JsonOutput.prettyPrint(json_ecc))  
        
    
    return message;
}
