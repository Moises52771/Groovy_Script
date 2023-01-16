import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;

def format (name, value){
    if(value == "null"){
        return ""
    }
    def definition = [
        cd_transp : { x -> "cd_transp = $x"},
        app_dt_lastupdate : {x -> "app_dt_lastupdate >= to_date('$x', 'yyyy-MM-dd')"},        
        cd_empresa : {x -> "(" + x.split(",").collect{it -> "cd_empresa = $it"}.join(" or ") + ")"},        
        fg_controle : {x -> "fg_controle = '$x'"}        
    ]
    return definition[name](value)
}

def joinWithAnd(List<String> list){  
    return list.findAll{it -> it != ""}.join(" and ")
}

def Message processData(Message message){   
    
    def headers = message.getHeaders()
    String app_dt_lastupdate = headers.get("lastUpdate").toString()
    String cd_transp = headers.get("cdTransp").toString()
    String cd_empresa = headers.get("cdEmpresas").toString()
    String fg_controle = headers.get("fgControle").toString()       

    def joinedParams = joinWithAnd([
        format("cd_transp", cd_transp),
        format("app_dt_lastupdate", app_dt_lastupdate),
        format("cd_empresa", cd_empresa),
        format("fg_controle", fg_controle)
    ])  
    
    if (joinedParams != ""){
        
        message.setProperty("where", "Where $joinedParams")
        
    }
    

       

    println !'' 
    
    return message;
}
