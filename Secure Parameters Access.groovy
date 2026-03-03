import com.sap.gateway.ip.core.customdev.util.Message
import com.sap.it.api.ITApiFactory
import com.sap.it.api.securestore.SecureStoreService
import com.sap.it.api.securestore.AccessTokenAndUser

def Message processData(Message message) {
    def apikey_alias = message.getProperty("ApiKeyAlias")
    def secureStorageService =  ITApiFactory.getService(SecureStoreService.class, null)
    try{
        def secureParameter = secureStorageService.getUserCredential(apikey_alias)
        def apikey = secureParameter.getPassword().toString()
        message.setProperty("api-key", apikey)
    } catch(Exception e){
        throw new SecureStoreException("Secure Parameter not available")
    }
    return message;
}

def Message processData(Message message) {
    def CredentialAlias = message.getProperty("CredentialAlias")
    def secureStorageService =  ITApiFactory.getService(SecureStoreService.class, null)
    def credential = secureStorageService.getUserCredential(CredentialAlias)
    def credentialProperties = credential.getCredentialProperties()
    message.setProperty("credentialProperties", credentialProperties)
    return message;
}

def Message processData(Message message) {
        SecureStoreService secureStoreService = ITApiFactory.getService(SecureStoreService.class, null);
     AccessTokenAndUser accessTokenAndUser = secureStoreService.getAccesTokenForOauth2AuthorizationCodeCredential(credential_name);
    String token = accessTokenAndUser.getAccessToken();
    String user = accessTokenAndUser.getUser();   
    message.setHeader("Authorization", "Bearer "+token);
    return message;
}
