import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import javax.swing.text.MaskFormatter;

def Message processData(Message message) {
    MaskFormatter mask
    def num = "12345678910"
           
    mask = new MaskFormatter("###.###.###-##")
    mask.setPlaceholderCharacter((char)"*"); // Caracter default se desejado
    mask.setValueContainsLiteralCharacters(false)
    num = mask.valueToString(num)

    message.setBody(num)
    return message;
}
    
