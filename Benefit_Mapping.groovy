import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import java.lang.RuntimeException;
import groovy.json.*;
import com.sap.it.api.ITApiFactory
import com.sap.it.api.mapping.ValueMappingApi

def Message processData(Message message) {

    final String body = message.getBody(java.lang.String);
    def jsonParser    = new JsonSlurper()
    def jsonObject    = jsonParser.parseText(body)
    def headers       = message.getHeaders()
  
    //-------------- UID dos beneficios --------------------------------------------

    def bn_alimentacao = headers.get("h_bn_alimentacao")
    def bn_odonto      = headers.get("h_bn_odonto")
    def bn_medica      = headers.get("h_bn_medica")
    def bn_vida        = headers.get("h_bn_vida")
    def bn_transporte  = headers.get("h_bn_transporte")
    
    //------------------------------------------------------------------------------

    jsonObject.benefits.data.forEach{e ->

        if(e.type == bn_alimentacao){ // Beneficio de Alimentação
            
            mapBnefAliment(message, JsonOutput.toJson(e))
        }
        else if(e.type == bn_odonto){ // Beneficio de Assistência Odontológica

            mapBnefOdonto(message, JsonOutput.toJson(e), body)
        }
        else if(e.type == bn_medica){ // Beneficio de Assistência Médica

            mapBnefMedica(message, JsonOutput.toJson(e), body)
        }
        else if(e.type == bn_vida){ // Beneficio de Seguro de Vida

            mapBnefVida(message, JsonOutput.toJson(e), body)
        }
        else if(e.type == bn_transporte){ // Beneficio de Transporte

            mapBnefTransp(message, JsonOutput.toJson(e))
        }

    }

    return message;

}

// Função que mapeia os dados do Beneficio de Alimentação
//------------------------------------------------------------------------------------------------------------------------------------
def void mapBnefAliment(Message message, String obj) {
    def jsonParser = new JsonSlurper()
    def jsonObject = jsonParser.parseText(obj)

    // message.setBody(JsonOutput.prettyPrint(json))
}

// Função que mapeia os dados do Beneficio de Assistência Odontológica
//------------------------------------------------------------------------------------------------------------------------------------
def void mapBnefOdonto(Message message, String obj, String body) {
    def valueMap = ITApiFactory.getApi(ValueMappingApi.class, null)
    def jsonParser   = new JsonSlurper()
    def jsonObject   = jsonParser.parseText(obj)
    def jsonObj_body = jsonParser.parseText(body)
    def jsonDep      = []
    String plan
    def json
    def jsonPlan
    def dep

    json = JsonOutput.toJson(//-- Busca dados do beneficiado
 
            Effectivestartdate: jsonObj_body.admission_date, ///------------------------------
            Plan:               jsonObject.benefits[0].benefit.signature.modelName,
            Provider:           jsonObject.benefits[0].benefit.data.provedor,
            enrolleeOptions:    jsonObject.benefits[0].benefit.data.planos[0].nome,
            Coverage:           jsonObject.benefits[0].benefit.data.provedor + " " +
                                jsonObject.benefits[0].benefit.data.planos[0].nome
        
    )
    jsonPlan = JsonOutput.toJson(//-- Cria raiz do beneficio
 
        BenefitInsurancePlanEnrollmentDetails: jsonParser.parseText(json)
        
    )

    if(jsonObject.includeDependent == true){ //-- Possui dependentes?

        def array_uid_dep = []
        def id

        jsonObject.benefits[0].avaiablePersons.forEach {e ->

            def uid_dep = JsonOutput.toJson(//-- Busca UID dos dependentes
                uid: e.uid
            )

            array_uid_dep.push(uid_dep)

        }

        def obj_uid_dep = jsonParser.parseText(array_uid_dep.toString())
        
        obj_uid_dep.forEach {p ->

            id = p.uid
            
            jsonObj_body.persons.forEach {x -> //-- Busca dados do dependente
                if(x.personId == id){
                    
                    dep = JsonOutput.toJson(
    
                        dependentName:    x.personData.nome,
                        dateOfBirth:      x.personData.dataNascimento,
                        RelationShipType: valueMap.getMappedValue("AcessoRH", "RelationshipType", x.dependent.parentesco, "EC", "RelationshipType"),
                        cust_termoAceite: null

                    )

                }
            
            }

            jsonDep.push(dep) 

        }
                                                                                // Cria um sub nivel com os dados do dependente
        def jsonPlan2 = jsonParser.parseText(jsonPlan.toString())
        jsonPlan2.BenefitInsurancePlanEnrollmentDetails.BenefitPensionDependentNominees = jsonParser.parseText(jsonDep.toString())
        jsonDep = JsonOutput.toJson(jsonPlan2)

    }else{
        jsonDep = ''
    }

    message.setProperty("p_benefit_odonto_data",        JsonOutput.prettyPrint(jsonPlan))
    message.setProperty("p_benefit_odonto_depend_data", JsonOutput.prettyPrint(jsonDep.toString()))

    // message.setBody(JsonOutput.prettyPrint(json))
}

// Função que mapeia os dados do Beneficio de Assistência Médica
//---------------------------------------------------------------------------------------------------------------------------------
def void mapBnefMedica(Message message, String obj, String body) {
    def valueMap = ITApiFactory.getApi(ValueMappingApi.class, null)
    def jsonParser   = new JsonSlurper()
    def jsonObject   = jsonParser.parseText(obj)
    def jsonObj_body = jsonParser.parseText(body)
    def jsonDep      = []
    def jsonPlan
    String plan
    def json
    def dep

    json = JsonOutput.toJson(//-- Busca dados do beneficiado

        Effectivestartdate: null,
        Plan:               jsonObject.benefits[0].benefit.signature.modelName,
        Provider:           jsonObject.benefits[0].benefit.data.provedor,
        enrolleeOptions:    jsonObject.benefits[0].benefit.data.planos[0].nome,
        Coverage:           null
    )
    jsonPlan = JsonOutput.toJson(//-- Cria raiz do beneficio
 
        BenefitInsurancePlanEnrollmentDetails: jsonParser.parseText(json)
        
    )

    if(jsonObject.includeDependent == true){ //-- Possui dependentes?

        def array_uid_dep = []
        def id

        jsonObject.benefits[0].avaiablePersons.forEach {e ->

            def uid_dep = JsonOutput.toJson(//-- Busca UID dos dependentes
                uid: e.uid
            )

            array_uid_dep.push(uid_dep)

        }

        def obj_uid_dep = jsonParser.parseText(array_uid_dep.toString())
        
        obj_uid_dep.forEach {p ->

            id = p.uid
            
            jsonObj_body.persons.forEach {x -> //-- Busca dados do dependente
                if(x.personId == id){
                    
                    dep = JsonOutput.toJson(
    
                        dependentName:    x.personData.nome,
                        dateOfBirth:      x.personData.dataNascimento,
                        RelationShipType: valueMap.getMappedValue("AcessoRH", "RelationshipType", x.dependent.parentesco, "EC", "RelationshipType"),
                        cust_termoAceite: null

                    )

                }
            
            }

            jsonDep.push(dep) 

        }
                                                                                // Cria um sub nivel com os dados do dependente
        def jsonPlan2 = jsonParser.parseText(jsonPlan.toString())
        jsonPlan2.BenefitInsurancePlanEnrollmentDetails.BenefitPensionDependentNominees = jsonParser.parseText(jsonDep.toString())
        jsonDep = JsonOutput.toJson(jsonPlan2)


    }else{
        jsonDep = []
    }

    message.setProperty("p_benefit_med_data",        JsonOutput.prettyPrint(jsonPlan))
    message.setProperty("p_benefit_med_depend_data", JsonOutput.prettyPrint(jsonDep.toString()))

    // message.setBody(JsonOutput.prettyPrint(json))

}

// Função que mapeia os dados do Beneficio de Seguro de Vida
//------------------------------------------------------------------------------------------------------------------------------------
def void mapBnefVida(Message message, String obj, String body) {
    def valueMap = ITApiFactory.getApi(ValueMappingApi.class, null)
    def jsonParser   = new JsonSlurper()
    def jsonObject   = jsonParser.parseText(obj)
    def jsonObj_body = jsonParser.parseText(body)
    def jsonDep      = []
    String plan
    def json
    def jsonPlan
    def dep

    json = JsonOutput.toJson(//-- Busca dados do beneficiado
 
            Effectivestartdate: null,
            Plan:               jsonObject.benefits[0].benefit.signature.modelName,
            Provider:           jsonObject.benefits[0].benefit.data.provedor,
            enrolleeOptions:    jsonObject.benefits[0].benefit.data.planos[0].nome,
            Coverage:           null
        
    )
    jsonPlan = JsonOutput.toJson(//-- Cria raiz do beneficio
 
        BenefitInsurancePlanEnrollmentDetails: jsonParser.parseText(json)
        
    )

    if(jsonObject.includeDependent == true){ //-- Possui dependentes?

        def array_uid_dep = []
        def id

        jsonObject.benefits[0].avaiablePersons.forEach {e ->

            def uid_dep = JsonOutput.toJson(//-- Busca UID dos dependentes
                uid: e.uid
            )

            array_uid_dep.push(uid_dep)

        }

        def obj_uid_dep = jsonParser.parseText(array_uid_dep.toString())
        
        obj_uid_dep.forEach {p ->

            id = p.uid
            
            jsonObj_body.persons.forEach {x -> //-- Busca dados do dependente
                if(x.personId == id){
                    
                    dep = JsonOutput.toJson(
    
                        dependentName:    x.personData.nome,
                        dateOfBirth:      x.personData.dataNascimento,
                        RelationShipType: valueMap.getMappedValue("AcessoRH", "RelationshipType", x.dependent.parentesco, "EC", "RelationshipType"),
                        Gender:           x.personData.sexo

                    )

                }
            
            }

            jsonDep.push(dep) 

        }
                                                                                // Cria um sub nivel com os dados do dependente
        def jsonPlan2 = jsonParser.parseText(jsonPlan.toString())
        jsonPlan2.BenefitInsurancePlanEnrollmentDetails.BenefitPensionDependentNominees = jsonParser.parseText(jsonDep.toString())
        jsonDep = JsonOutput.toJson(jsonPlan2)

    }else{
        jsonDep = []
    }

    message.setProperty("p_benefit_vida_data",        JsonOutput.prettyPrint(jsonPlan))
    message.setProperty("p_benefit_vida_depend_data", JsonOutput.prettyPrint(jsonDep.toString()))

    // message.setBody(JsonOutput.prettyPrint(jsonPlan))
}

// Função que mapeia os dados do Beneficio de Transporte
//--------------------------------------------------------------------------------------------------------------------------------
def void mapBnefTransp(Message message, String obj) {
    def jsonParser = new JsonSlurper()
    def jsonObject = jsonParser.parseText(obj)

    // message.setBody(JsonOutput.prettyPrint(json))
}
