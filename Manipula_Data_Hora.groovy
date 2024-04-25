import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.text.SimpleDateFormat;

    
//  operate = HOUR
//  operate = DATE
//  operate = MONTH
//  operate = YEAR

def String customDateTime(String date, String numAdd, String operate){
    
    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS") // Formato que a data virá
    Calendar dateMap         = Calendar.getInstance();
    
    if (operate == "HOUR"){
        
        dateMap.setTime(formato.parse(date));
        dateMap.add(Calendar.HOUR, numAdd.toInteger());  // Número a ser adicionado as horas
    
    }else if (operate == "DATE"){
        
        dateMap.setTime(formato.parse(date));
        dateMap.add(Calendar.DATE, numAdd.toInteger());  // Número a ser adicionado ao dia
    
    }else if (operate == "MONTH"){
        
        dateMap.setTime(formato.parse(date));
        dateMap.add(Calendar.MONTH, numAdd.toInteger());  // Número a ser adicionado ao mes
    
    }else if (operate == "YEAR"){
        
        dateMap.setTime(formato.parse(date));
        dateMap.add(Calendar.YEAR, numAdd.toInteger());  // Número a ser adicionado ao ano
    
    }

    date = formato.format(dateMap.getTime());
    
    return date
}


def Message processData(Message message){       
    def String dateTest = "2022-10-01T10:20:10.001"

    def date = customDateTime(dateTest, "2", "HOUR")
   
    print date

    return message;
}
