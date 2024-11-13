import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// https://api19preview.sapsf.com/odata/v2/upsert endpoint padrão para inserção de dados no SF via HTTP

long convertDateTimeToMilliseconds(String dateTimeString) {
    LocalDateTime dateTime = LocalDateTime.parse(dateTimeString)
    return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli()
}

//def String removeAcentos(String inputString){
//   def normalizedString = java.text.Normalizer.normalize(inputString, java.text.Normalizer.Form.NFD)
 //   def outputString = normalizedString.replaceAll(/\p{M}/, '')
 
//    return outputString
//}


def Message processData(Message message) {
    def body = message.getBody(String)
    def jsonParser = new JsonSlurper()
    def jsonObject = jsonParser.parseText(body)
    def actionValidation
   // def starDate = "/Date("+ convertDateTimeToMilliseconds(jsonObject.position.position.effectiveStartDate)+")/"
    def lv_parentPosition = jsonObject.position.position.parentPosition
    def lv_parentStartDate = "datetime'"+jsonObject.position.position.parentStartDate+"'"
   // String division1 = removeAcentos(jsonObject.position.position.division).toString()
   // String comment1 = removeAcentos(jsonObject.position.position.comment)

   def builder = new JsonBuilder()
    def json = builder {    
        

        "__metadata" {
        "uri" "Position"
        "type" "SFOData.Position"
    }

    
    "code" jsonObject.position.position.code
    "effectiveStartDate" "/Date("+ convertDateTimeToMilliseconds(jsonObject.position.position.effectiveStartDate)+")/"
    "jobCode"    jsonObject.position.position.jobCodeNav_externalCode
    "cust_union" jsonObject.position.position.cust_union
    "division" jsonObject.position.position.division
    "costCenter" jsonObject.position.position.costCenter
    "cust_vaga_congelada" jsonObject.position.position.cust_vaga_congelada =="True" ? true :false
    "cust_locationGroup" jsonObject.position.position.cust_locationGroup
    "businessUnit" jsonObject.position.position.businessUnit
    "cust_data_fim" "/Date("+ convertDateTimeToMilliseconds(jsonObject.position.position.cust_data_fim)+")/"
    "cust_confidencial" jsonObject.position.position.cust_confidencial =="True" ? true :false
    "cust_vaga_confidencial_rh" jsonObject.position.position.cust_vaga_confidencial_rh =="True" ? true :false
    "cust_contractType" jsonObject.position.position.cust_contractType
    "effectiveStatus" jsonObject.position.position.effectiveStatus
    "cust_companyCost" jsonObject.position.position.cust_companyCost
    println jsonObject.position.position.abrirVaga
    if(jsonObject.position.position.abrirVaga == "True" )   { 
        "cust_codigovaga" jsonObject.position.position.cust_codigovaga }    
    "cust_group" "2"
    "cust_salario" jsonObject.position.position.cust_salario
    "cust_horasMensais" jsonObject.position.position.cust_horasMensais
    "description" jsonObject.position.position.description
    "vacant" jsonObject.position.position.vacant =="True" ? true :false
    "company" jsonObject.position.position.company
    "department" jsonObject.position.position.department
    "changeReason" jsonObject.position.position.changeReason
    //"cust_nome_ocup_atual_posicao" jsonObject.position.position.ocupante
    "location"jsonObject.position.position.locationFilial
    "cust_jornComplementar" jsonObject.position.position.cust_jornComplementar
    "comment" jsonObject.position.position.comment
    "cust_PCD" jsonObject.position.position.cust_PCD =="True" ? true :false
    "cust_quantidade_vagas" "1"
    "cust_MetasUOL" jsonObject.position.position.cust_MetasUOL
    "cust_payGroup" jsonObject.position.position.payGroup
    "parentPosition"                {
        "__metadata" {
          "type" "SFOData.Position"
          "uri" "Position(code='$lv_parentPosition',effectiveStartDate=$lv_parentStartDate)"
        }
    }


  

    }

    
   message.setProperty("p_matriculaValidation",jsonObject.position.empEmployment.userId)
   message.setProperty("p_salario",jsonObject.position.position.cust_salario)
   message.setProperty("p_actionValidation",actionValidation.toString())
   message.setProperty("p_starDate","/Date("+ convertDateTimeToMilliseconds(jsonObject.position.position.effectiveStartDate)+")/")
   message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(json)))
    
 

	return message
}


