import java.security.cert.X509Certificate
import java.util.Base64
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import com.sap.gateway.ip.core.customdev.util.Message

def processData(Message message) {
    try {
        def factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        def socket = factory.createSocket("viacep.com.br", 443) as SSLSocket

        // Connect to the peer
        def session = socket.getSession()
        X509Certificate cert = session.peerCertificates[0] as X509Certificate

        def sDNName = cert.issuerDN.name // Server's DN Name
        def sDEREncoded = Base64.getEncoder().encodeToString(cert.encoded)

        // Set Properties
        message.setProperty("sDNName", sDNName)
        message.setProperty("sDEREncoded", sDEREncoded)

        return message
    } catch (SSLPeerUnverifiedException e) {
        throw new Exception("graph.facebook.com did not present a valid cert.")
    }
}
