import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.text.SimpleDateFormat;

def String customDateTime(String date, String numAdd){
    
    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS") // Formato que a data virá
    Calendar dateMap         = Calendar.getInstance();
    
    dateMap.setTime(formato.parse(date));
    dateMap.add(Calendar.HOUR, numAdd.toInteger());  // Número à ser adicionado a data
    //          Calendar.HOUR
    //          Calendar.DATE
    //          Calendar.MONTH
    //          Calendar.YEAR

    date = formato.format(dateMap.getTime());
    
    return date
}

def Message processData(Message message){       
    def String dateTest = "2022-10-01T10:20:10.001"

    def date = customDateTime(dateTest, "2")
   
    print date

    return message;
}
