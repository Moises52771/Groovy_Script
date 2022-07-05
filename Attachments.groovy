import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;

def Message processData(Message message) {

    def body = message.getBody(java.lang.String) as String;
    def header = message.getHeaders() as String;
    def property = message.getProperties() as String;
    def map = message.getProperties();
    def messageLog = messageLogFactory.getMessageLog(message);
    def localError = map.get("ERROR_Local") as String;
    def msgErro = map.get("p_processStatus");
    
    message.setProperty("p_processStatus", "ERROR \n" + msgErro );
    
    if(messageLog != null){
        
        messageLog.setStringProperty("Logging#1","Printing Payload As Attachment")
        messageLog.addAttachmentAsString("ERROR_Local:", localError, "text/plain");
        messageLog.addAttachmentAsString("ERROR_ResponsePayload:", body, "text/plain");
        messageLog.addAttachmentAsString("ERROR_ResponseHeader:", header, "text/plain");
        messageLog.addAttachmentAsString("ERROR_Properties:", property, "text/plain");
        
    }
     
    return message;

}
