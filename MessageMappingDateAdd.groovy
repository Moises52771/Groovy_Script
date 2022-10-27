import com.sap.it.api.mapping.*;
import java.text.SimpleDateFormat;

/*Add MappingContext parameter to read or set headers and properties
def String customFunc1(String P1,String P2,MappingContext context) {
         String value1 = context.getHeader(P1);
         String value2 = context.getProperty(P2);
         return value1+value2;
}

Add Output parameter to assign the output value.
def void custFunc2(String[] is,String[] ps, Output output, MappingContext context) {
        String value1 = context.getHeader(is[0]);
        String value2 = context.getProperty(ps[0]);
        output.addValue(value1);
        output.addValue(value2);
}*/

def String customFunc(String date, String numDays, MappingContext context){
    
    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS")
    Calendar dateMap = Calendar.getInstance();
    
    dateMap.setTime(formato.parse(date));
    dateMap.add(Calendar.DATE, numDays.toInteger());  // número de dias a adicionar
    
    date = formato.format(dateMap.getTime());
    
	return date 
}
