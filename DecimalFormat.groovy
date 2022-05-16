import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;
import java.text.DecimalFormat 

def Message processData(Message message) {
    
    def body     = message.getBody(String)
    def jsonFile = new JsonSlurper()
    def jsonObj  = jsonFile.parseText(body)

    def alt = jsonObj.altura
    def peso = jsonObj.peso

    def imc = peso / (alt * alt)
    def resul

    if (imc < 18.5){
        resul = ("ERRO")
    }else if(imc > 18.5 && imc < 24.9){
        resul = ("Peso normal!")
    }else if(imc >25.0 && imc < 29.9){
        resul =  ("Sobrepeso!")
    }else if(imc > 30.0 && imc < 39.9){
        resul = ("Obeso!")
    }else if(imc > 40.0){
        resul = ("Obesidade grave!")
    }
    
    

    def df = new DecimalFormat("#0.00")
    def formatted = df.format(imc)

    message.setBody(resul + "   Seu IMC é: " + formatted)
    println (resul + "   Seu IMC é: " + formatted)

    return message;
}
