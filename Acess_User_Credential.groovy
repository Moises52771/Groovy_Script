import com.sap.it.api.ITApi
import com.sap.it.api.ITApiFactory
import com.sap.it.api.securestore.*;
import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;

def Message processData(Message message) {
    //Body 
       def body = message.getBody();
       String password;
       String _output="";
       def service = ITApiFactory.getApi(SecureStoreService.class, null);

       //Name of secure parameter
       def credential = service.getUserCredential("XXX"); 
        if (credential == null)
        { throw new IllegalStateException("No credential found for alias 'XXX'");             
        }
        else{
            password= new String(credential.getPassword());
            }

        //store it in property which can be used in later stage of your integration process.
        message.setProperty("P_Password", password);
       return message;
}
