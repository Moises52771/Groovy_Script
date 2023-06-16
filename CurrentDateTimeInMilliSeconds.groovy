import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.time.LocalDateTime
import java.time.ZoneOffset

def String currentDateTimeMilliSeconds(){ // Função que retorna data atual em milesegundos

    LocalDateTime dateTime = LocalDateTime.parse(
                                LocalDateTime.now()
                                .toString()
                            )
    
    long dateTimeMilliSeconds = dateTime
                                .toInstant(
                                    ZoneOffset.UTC
                                )
                                .toEpochMilli()
    

    final String date = "/Date(" + dateTimeMilliSeconds + ")/"

    return date
    
}
    
def Message processData(Message message) {
 
    println currentDateTimeMilliSeconds()

    return message;
    
}
