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
    
    def dateFormat    = "MM/dd/yyyy" //---- Determina o formato da data
  
    //-------------- UID dos beneficios --------------------------------------------

    // message.setProperty("p_bn_alimentacao", "b26803a7-3a7c-48d1-8a66-70a5e59f68fe")
    // message.setProperty("p_bn_odonto",      "3f4e1ee3-c225-49df-9b17-3a4444dd59e0")
    // message.setProperty("p_bn_medica",      "47dcc73a-1985-40f5-98bd-07e00b9bf08a")
    // message.setProperty("p_bn_vida",        "b62f3492-3dcd-41e7-b644-40f4f72c753f")
    // message.setProperty("p_bn_transporte",  "e3a19b9a-333d-4db9-b203-24baf0f3d984")

    def bn_alimentacao = headers.get("h_bn_alimentacao")
    def bn_odonto      = headers.get("h_bn_odonto")
    def bn_medica      = headers.get("h_bn_medica")
    def bn_vida        = headers.get("h_bn_vida")
    def bn_transporte  = headers.get("h_bn_transporte")
    
    //------------------------------------------------------------------------------

    jsonObject.benefits.data.forEach{e ->

        if(e.type == bn_alimentacao){ // Beneficio de Alimentação
            
            mapBnefAliment(message, JsonOutput.toJson(e), body, dateFormat)
        }
        else if(e.type == bn_odonto){ // Beneficio de Assistência Odontológica

            mapBnefOdonto(message, JsonOutput.toJson(e), body, dateFormat)
        }
        else if(e.type == bn_medica){ // Beneficio de Assistência Médica

            mapBnefMedica(message, JsonOutput.toJson(e), body, dateFormat)
        }
        else if(e.type == bn_vida){ // Beneficio de Seguro de Vida

            mapBnefVida(message, JsonOutput.toJson(e), body, dateFormat)
        }
        else if(e.type == bn_transporte){ // Beneficio de Transporte

            mapBnefTransp(message, JsonOutput.toJson(e), body, dateFormat)
        }

    }

    return message;

}


// Método que mapeia os dados do Beneficio de Alimentação
//------------------------------------------------------------------------------------------------------------------------------------
def void mapBnefAliment(Message message, String obj, String body, String dateFormat) {
    def valueMap     = ITApiFactory.getApi(ValueMappingApi.class, null)
    def jsonParser   = new JsonSlurper()
    def jsonObject   = jsonParser.parseText(obj)
    def jsonObj_body = jsonParser.parseText(body)
    def conpany_name = message.getProperties().get("p_conpany_name")

    def startdate         = message.getHeaders().get("h_data")
    def efetiveStartdate  = Date.parse("yyyy-MM-dd", jsonObj_body.admission_date).format(dateFormat)
    def externalCode_va   = "ALIM_U"
    def externalCode_vr   = "REFE_U"
    def workerId          = message.getHeaders().get("h_id")
    def currency_code     = "Benefício"
    def effectiveStatus   = "A"
    def schedulePeriod_id = "BENE"
    def amount_va         = jsonObject.option.data.va
    def amount_vr         = jsonObject.option.data.vr
    def entitlementAmount = 1 //jsonObject.benefits[0].benefit.data.valorTotal
    String csv_Benefit_va
    String csv_Benefit_vr
    String csv_Benefit_va_vr
    def id_va
    def id_vr

//  Verifica se o candidato é estagiario
//------------------------------------------------------------------------------------------------------------------------------------
    if(jsonObj_body.pagamento.vinculo == "estagio"){ // Estagiário - Auxilio Alimentação
        
        if(conpany_name == "Passei Direto S.A"){ // Estagiário - Passei Direto
            
            externalCode_va   = "ALIM_UPE"
            externalCode_vr   = "REFE_UPE" 
            
        }else{
            
            externalCode_va   = "ALIM_UE"
            externalCode_vr   = "REFE_UE" 
            
        }
        
    }else{ // Não é estagiário - Vale Auxilio Alimentação
        
        externalCode_va   = "ALIM_U"
        externalCode_vr   = "REFE_U"
        
    }
    
    id_va = "$workerId" + "_$externalCode_va"
    id_vr = "$workerId" + "_$externalCode_vr"

//  Mapeamento do arquivo Benefit_Enrollment.csv
//------------------------------------------------------------------------------------------------------------------------------------

    if (amount_va > 0){ //-- Cria CSV com os dados de VA
    
        def amount = 1
        
        csv_Benefit_va = ",\"$id_va\",\"$efetiveStartdate\",\"$externalCode_va\",\"$workerId\"," +
                         "\"$amount\",\"$currency_code\",\"$effectiveStatus\",," + 
                         "\"$entitlementAmount\",\"$schedulePeriod_id\",,,,,,,,,," +
                         "\"$startdate\",,,\"$externalCode_va\",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,"

    }else{

        csv_Benefit_va = ""

    }

    if (amount_vr > 0){ //-- Cria CSV com os dados de VR
    
        def amount = 1
        
        csv_Benefit_vr = ",\"$id_vr\",\"$efetiveStartdate\",\"$externalCode_vr\",\"$workerId\"," +
                         "\"$amount\",\"$currency_code\",\"$effectiveStatus\",," +  
                         "\"$entitlementAmount\",\"$schedulePeriod_id\",,,,,,,,,," +
                         "\"$startdate\",,,\"$externalCode_vr\",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,"

    }else{

        csv_Benefit_vr = ""

    }

    switch(amount_va){
        case 0:   //-- Caso valor seja 0 para VA terá apenas o VR
            csv_Benefit_va_vr = "$csv_Benefit_vr"
            break
        
        case 1:   //-- Caso valor seja 1 para VA terá apenas o combo VA/VR
            csv_Benefit_va_vr = "$csv_Benefit_va\n$csv_Benefit_vr"
            break
        
        case 2:   //-- Caso valor seja 2 para VA terá apenas o VA
            csv_Benefit_va_vr = "$csv_Benefit_va"
            break
    }

    message.setProperty("p_va_Benefit_Enrollment",   csv_Benefit_va_vr)
    
}


// Método que mapeia os dados do Beneficio de Assistência Odontológica
//------------------------------------------------------------------------------------------------------------------------------------
def void mapBnefOdonto(Message message, String obj, String body, String dateFormat) {
    def valueMap     = ITApiFactory.getApi(ValueMappingApi.class, null)
    def jsonParser   = new JsonSlurper()
    def jsonObject   = jsonParser.parseText(obj)
    def jsonObj_body = jsonParser.parseText(body)
    def dep_lenght   = message.getProperties().get("p_dep_lenght")

    def startdate         = message.getHeaders().get("h_data")
    def efetiveStartdate  = Date.parse("yyyy-MM-dd", jsonObj_body.admission_date).format(dateFormat) //------------
    def externalCode      = "ASOD_BC" //---- Manter somente esse com no EC
    def externalCode_bnDt = "#1"
    def workerId          = message.getHeaders().get("h_id")
    def currency_code     = "BRL"
    def effectiveStatus   = "A"
    def schedulePeriod_id = "BENE"
    def plan_id           = valueMap.getMappedValue("Unico", "plan",            jsonObject.option.data.plano.nome, "EC", "plan") //----
    def providerId        = valueMap.getMappedValue("Unico", "provider",        jsonObject.option.data.plano.nome, "EC", "provider") //----
    def enrolleeOptions   = valueMap.getMappedValue("Unico", "enrolleeOptions", jsonObject.option.data.plano.nome, "EC", "enrolleeOptions") //----
    def coverage          = valueMap.getMappedValue("Unico", "coverage",        jsonObject.option.data.plano.nome, "EC", "coverage") //----
    def dependentName    
    def dateOfBirth      
    def relationShipType 
    def cust_termoAceite 
    
    String csv_Enrollment_Dependent = ""
    String csv_BenefitEnrollment    = ""
    String csv_Enrollment_Details   = ""

    def id = "$workerId" + "_$externalCode"
    
//  Verifica se o candidato nao é menor aprendiz
//------------------------------------------------------------------------------------------------------------------------------------
    if(jsonObj_body.pagamento.vinculo != "aprendiz"){ 
        
    //  Mapeamento do arquivo Benefit_Enrollment.csv
    //------------------------------------------------------------------------------------------------------------------------------------
        csv_BenefitEnrollment = ",\"$id\",\"$efetiveStartdate\",\"$externalCode\",\"$workerId\",,\"$currency_code" + 
                                "\",\"$effectiveStatus\",,,\"$schedulePeriod_id\",,,,,,,,,,\"$startdate\"," +
                                ",,\"$externalCode\",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
        
    //  Mapeamento do arquivo Benefit_Enrollment-Insurance_Plan_Enrollment_Details.csv
    //------------------------------------------------------------------------------------------------------------------------------------
        csv_Enrollment_Details = ",\"$id\",\"$efetiveStartdate\",\"$externalCode_bnDt\",\"$plan_id\",\"$providerId\"," +
                                 "\"$enrolleeOptions\",\"$coverage\",,,,,"
    
    //  Busca dados dos dependentes
    //------------------------------------------------------------------------------------------------------------------------------------
        if(jsonObject.option.data.incluirDependentes && dep_lenght != 0){ //-- Incluir dependentes?
    
            def jsonDep       = jsonParser.parseText(message.getProperties().get("p_dep_personId"))
            def array_uid_dep = []
            def uid
    
            jsonObject.benefits[0].avaiablePersons.forEach {e ->
            
                if(e.eligible){ //-- O dependente está elegível?
                    
                    def uid_dep = JsonOutput.toJson( //-- Busca UID dos dependentes
                        uid: e.uid
                    )
                    array_uid_dep.push(uid_dep)
    
                }
    
            }
    
            def obj_uid_dep = jsonParser.parseText(array_uid_dep.toString())
            
            obj_uid_dep.forEach {p ->
    
                uid = p.uid
                
                jsonDep.depData.forEach {y -> //-- Busca personId do dependente
                    if(y.uid == uid){
        
                        dependentName = y.personId
    
                    }
                
                }
                
                jsonObj_body.persons.forEach {x -> //-- Busca dados do dependente
                    if(x.personId == uid && x.personType == "dependent"){
                                   
                        dateOfBirth            = Date.parse("yyyy-MM-dd", x.personData.dataNascimento).format(dateFormat) //-------------------                
                        relationShipType_value = valueMap.getMappedValue("AcessoRH", "RelationshipType", x.dependent.parentesco, "EC", "RelationshipType")
                        relationShipType       = valueMap.getMappedValue("EC - Value", "relationShipType", relationShipType_value, "EC - ID", "relationShipType")
                        cust_termoAceite       = "aceite" 
    
                    }
                
                }
                
                //  Mapeamento do arquivo Benefit_Enrollment-Dependentes.csv
                //----------------------------------------------------------------------------------------------------
                if (csv_Enrollment_Dependent == null || csv_Enrollment_Dependent == ""){
    
                    csv_Enrollment_Dependent = ",\"$id\",\"$efetiveStartdate\",\"$dependentName\"," +
                                               "\"$dateOfBirth\",\"$relationShipType\",\"$cust_termoAceite\"," + 
           
                                               "\"Ao submeter seus dados ou dados de terceiros, você declara estar " +
                                               "ciente e de acordo com nossa Política de Privacidade " +
                                               "(https://sobreuol.noticias.uol.com.br/normas-de-seguranca-e-privacidade), " +
                                               "bem como ter obtido os devidos consentimentos e autorizações.\""
    
                }else{
                    
                    csv_Enrollment_Dependent = csv_Enrollment_Dependent + "\n" +
                                               ",\"$id\",\"$efetiveStartdate\",\"$dependentName\"," +
                                               "\"$dateOfBirth\",\"$relationShipType\",\"$cust_termoAceite\"," +
           
                                               "\"Ao submeter seus dados ou dados de terceiros, você declara estar " +
                                               "ciente e de acordo com nossa Política de Privacidade " +
                                               "(https://sobreuol.noticias.uol.com.br/normas-de-seguranca-e-privacidade), " +
                                               "bem como ter obtido os devidos consentimentos e autorizações.\""               
    
                }
    
            }
    
        }
        else{
    
            csv_Enrollment_Dependent = ""
    
        }
        
    }


    message.setProperty("p_od_Benefit_Enrollment",   csv_BenefitEnrollment)
    message.setProperty("p_od_Enrollment_Details",   csv_Enrollment_Details)
    message.setProperty("p_od_Enrollment_Dependent", csv_Enrollment_Dependent)

}


// Método que mapeia os dados do Beneficio de Assistência Médica
//--------------------------------------------------------------------------------
def void mapBnefMedica(Message message, String obj, String body, String dateFormat) {
    def valueMap     = ITApiFactory.getApi(ValueMappingApi.class, null)
    def jsonParser   = new JsonSlurper()
    def jsonObject   = jsonParser.parseText(obj)
    def jsonObj_body = jsonParser.parseText(body)
    def dep_lenght   = message.getProperties().get("p_dep_lenght")
    def conpany_name = message.getProperties().get("p_conpany_name")

    def startdate             = message.getHeaders().get("h_data")
    def efetiveStartdate      = Date.parse("yyyy-MM-dd", jsonObj_body.admission_date).format(dateFormat) //--------------
    def externalCode            //---- (ASME_U1 | ASME_BC)
    def externalCode_bnDt     = "#1"
    def workerId              = message.getHeaders().get("h_id")
    def currency_code         = "BRL"
    def effectiveStatus       = "A"
    def schedulePeriod_id     = "BENE"
    def plan_id               = valueMap.getMappedValue("Unico", "plan",            jsonObject.option.data.plano.nome, "EC", "plan") //----
    def providerId            = valueMap.getMappedValue("Unico", "provider",        jsonObject.option.data.plano.nome, "EC", "provider") //----
    def enrolleeOptions       = valueMap.getMappedValue("Unico", "enrolleeOptions", jsonObject.option.data.plano.nome, "EC", "enrolleeOptions") //----
    def coverage              = valueMap.getMappedValue("Unico", "coverage",        jsonObject.option.data.plano.nome, "EC", "coverage") //----
    def cust_motivo_AssistMed = "1"
    def dependentName    
    def dateOfBirth      
    def relationShipType 
    def cust_termoAceite 
    String csv_Enrollment_Dependent = ""
    def id

//  Verifica se o candidato esta vinculado a filial Boa Compra
//----------------------------------------------------------------------------------------------------------------------------------
    if(conpany_name == "BOA COMPRA"){ 
            
        externalCode = "ASME_BC"
            
    }else{
            
        externalCode = "ASME_U1"
            
    }
    
    id = "$workerId" + "_$externalCode"
    
//  Mapeamento do arquivo Benefit_Enrollment.csv
//------------------------------------------------------------------------------------------------------------------------------------
    String csv_BenefitEnrollment = ",\"$id\",\"$efetiveStartdate\",\"$externalCode\",\"$workerId\",,\"$currency_code" + 
                                   "\",\"$effectiveStatus\",,,\"$schedulePeriod_id\",,,,,,,,,,\"$startdate\"," +
                                   ",,\"$externalCode\",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,\"$cust_motivo_AssistMed\",,,,,,,,";                     //$cust_motivo_AssistMed
    
//  Mapeamento do arquivo Benefit_Enrollment-Insurance_Plan_Enrollment_Details.csv
//------------------------------------------------------------------------------------------------------------------------------------
    String csv_Enrollment_Details = ",\"$id\",\"$efetiveStartdate\",\"$externalCode_bnDt\",\"$plan_id\",\"$providerId\"," +
                                    "\"$enrolleeOptions\",\"$coverage\",,,,,"

//  Busca dados dos dependentes
//------------------------------------------------------------------------------------------------------------------------------------
    if(jsonObject.option.data.incluirDependentes && dep_lenght != 0){ //-- Incluir dependentes?

        def jsonDep       = jsonParser.parseText(message.getProperties().get("p_dep_personId"))
        def array_uid_dep = []
        def uid

        jsonObject.benefits[0].avaiablePersons.forEach {e ->
        
            if(e.eligible){ //-- O dependente está elegível?
                
                def uid_dep = JsonOutput.toJson( //-- Busca UID dos dependentes
                    uid: e.uid
                )
                array_uid_dep.push(uid_dep)

            }

        }

        def obj_uid_dep = jsonParser.parseText(array_uid_dep.toString())
        
        obj_uid_dep.forEach {p ->

            uid = p.uid
            
            jsonDep.depData.forEach {y -> //-- Busca personId do dependente
                if(y.uid == uid){
    
                    dependentName = y.personId

                }
            
            }
            
            jsonObj_body.persons.forEach {x -> //-- Busca dados do dependente
                if(x.personId == uid && x.personType == "dependent"){
                               
                    dateOfBirth            = Date.parse("yyyy-MM-dd", x.personData.dataNascimento).format(dateFormat) //-------------------                
                    relationShipType_value = valueMap.getMappedValue("AcessoRH", "RelationshipType", x.dependent.parentesco, "EC", "RelationshipType")
                    relationShipType       = valueMap.getMappedValue("EC - Value", "relationShipType", relationShipType_value, "EC - ID", "relationShipType")
                    cust_termoAceite       = "aceite" 

                }
            
            }
            
            
            //  Mapeamento do arquivo Benefit_Enrollment-Dependentes.csv
            //----------------------------------------------------------------------------------------------------
            if (csv_Enrollment_Dependent == null || csv_Enrollment_Dependent == ""){

                csv_Enrollment_Dependent =  ",\"$id\",\"$efetiveStartdate\",\"$dependentName\"," +
                                            "\"$dateOfBirth\",\"$relationShipType\",\"$cust_termoAceite\"," + 
        
                                            "\"Ao submeter seus dados ou dados de terceiros, você declara estar " +
                                            "ciente e de acordo com nossa Política de Privacidade " +
                                            "(https://sobreuol.noticias.uol.com.br/normas-de-seguranca-e-privacidade), " +
                                            "bem como ter obtido os devidos consentimentos e autorizações.\""

            }else{
                
                csv_Enrollment_Dependent =  csv_Enrollment_Dependent + "\n" +
                                            ",\"$id\",\"$efetiveStartdate\",\"$dependentName\"," +
                                            "\"$dateOfBirth\",\"$relationShipType\",\"$cust_termoAceite\"," + 
        
                                            "\"Ao submeter seus dados ou dados de terceiros, você declara estar " +
                                            "ciente e de acordo com nossa Política de Privacidade " +
                                            "(https://sobreuol.noticias.uol.com.br/normas-de-seguranca-e-privacidade), " +
                                            "bem como ter obtido os devidos consentimentos e autorizações.\""               

            }

        }

    }
    else{

        csv_Enrollment_Dependent = ""

    }

    print csv_Enrollment_Dependent
    message.setProperty("p_me_Benefit_Enrollment",   csv_BenefitEnrollment)
    message.setProperty("p_me_Enrollment_Details",   csv_Enrollment_Details)
    message.setProperty("p_me_Enrollment_Dependent", csv_Enrollment_Dependent)

}


// Método que mapeia os dados do Beneficio de Seguro de Vida
//------------------------------------------------------------------------------------------------------------------------------------
def void mapBnefVida(Message message, String obj, String body, String dateFormat) {
    def valueMap = ITApiFactory.getApi(ValueMappingApi.class, null)
    def jsonParser   = new JsonSlurper()
    def jsonObject   = jsonParser.parseText(obj)
    def jsonObj_body = jsonParser.parseText(body)

    def startdate             = message.getHeaders().get("h_data")
    def efetiveStartdate      = Date.parse("yyyy-MM-dd", jsonObj_body.admission_date).format(dateFormat) //--------------
    def externalCode          = "SVGR_U" //----
    def externalCode_bnDt     = "#1"
    def workerId              = message.getHeaders().get("h_id")
    def currency_code         = "BRL"
    def effectiveStatus       = "A"
    def schedulePeriod_id     = "BENE"
    def plan_id               = valueMap.getMappedValue("Unico", "plan",     jsonObject.option.data.plano.nome, "EC", "plan") //----
    def providerId            = valueMap.getMappedValue("Unico", "provider", jsonObject.option.data.plano.nome, "EC", "provider") //----
    def enrolleeOptions         //---- Tratar internamente ( SVGR, SVGR_E )
    def coverage                //---- Tratar internamente ( SVGR, SVGR_E )
    def employeeContribution  = 0
    def dependentName_0       = ""
    def dependentName_1       = ""
    def dependentName_2       = ""
    def dependentName_3       = ""
    def dependentName_4       = ""
    def dateOfBirth_0         = ""
    def dateOfBirth_1         = ""
    def dateOfBirth_2         = ""
    def dateOfBirth_3         = ""
    def dateOfBirth_4         = ""
    def cust_percentual_0     = ""
    def cust_percentual_1     = ""
    def cust_percentual_2     = ""
    def cust_percentual_3     = ""
    def cust_percentual_4     = ""
    def cust_contBeneficiario = 0
    String csv_Enrollment_Beneficiarios

    def id = "$workerId" + "_$externalCode"

//  Trata se o beneficio é ou não para um estágiario    
//------------------------------------------------------------------------------------------------------------------------------------
    if (jsonObj_body.pagamento.vinculo == "estagio"){
        
        enrolleeOptions = "SVGR_E"
        coverage        = "SVGR_E"
        
    }else{

        enrolleeOptions = "SVGR"
        coverage        = "SVGR"

    }
    
//  Mapeamento do arquivo Benefit_Enrollment.csv
//------------------------------------------------------------------------------------------------------------------------------------
    String csv_BenefitEnrollment = ",\"$id\",\"$efetiveStartdate\",\"$externalCode\",\"$workerId\",,\"$currency_code" + 
                                   "\",\"$effectiveStatus\",,,\"$schedulePeriod_id\",,,,,,,,,,\"$startdate\"," +
                                   ",,\"$externalCode\",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,";
  
//  Mapeamento do arquivo Benefit_Enrollment-Insurance_Plan_Enrollment_Details.csv
//------------------------------------------------------------------------------------------------------------------------------------
    String csv_Enrollment_Details = ",\"$id\",\"$efetiveStartdate\",\"$externalCode_bnDt\",\"$plan_id\",\"$providerId\"," +
                                    "\"$enrolleeOptions\",\"$coverage\",\"$employeeContribution\",,,,"

//  Busca dados dos dependentes
//------------------------------------------------------------------------------------------------------------------------------------
    if(jsonObject.option.data.incluirBeneficiarios){ //-- Incluir beneficiários?

        def uid
        def index = 0 //-- Contador de beneficiários

        jsonObject.benefits[0].avaiablePersons.forEach {e ->
            
            if(e.eligible){ //-- O beneficiário está elegível?
                
                //-- Busca UID dos beneficiários
                uid = e.uid
                int i

                for(i = 0; i < jsonObj_body.persons.size() && i < 5; i++){
                    
                    if(jsonObj_body.persons[i].personId == uid && jsonObj_body.persons[i].personType == "dependent"){
                        
                        switch(index) { 
                            case 0: 
                                dependentName_0 = jsonObj_body.persons[i].personData.nome
                                dateOfBirth_0   = Date.parse("yyyy-MM-dd", jsonObj_body.persons[i].personData.dataNascimento).format(dateFormat)
                                break; 
                            case 1: 
                                dependentName_1 = jsonObj_body.persons[i].personData.nome
                                dateOfBirth_1   = Date.parse("yyyy-MM-dd", jsonObj_body.persons[i].personData.dataNascimento).format(dateFormat) 
                                break; 
                            case 2: 
                                dependentName_2 = jsonObj_body.persons[i].personData.nome
                                dateOfBirth_2   = Date.parse("yyyy-MM-dd", jsonObj_body.persons[i].personData.dataNascimento).format(dateFormat) 
                                break; 
                            case 3: 
                                dependentName_3 = jsonObj_body.persons[i].personData.nome
                                dateOfBirth_3   = Date.parse("yyyy-MM-dd", jsonObj_body.persons[i].personData.dataNascimento).format(dateFormat) 
                                break; 
                            case 4: 
                                dependentName_4 = jsonObj_body.persons[i].personData.nome
                                dateOfBirth_4   = Date.parse("yyyy-MM-dd", jsonObj_body.persons[i].personData.dataNascimento).format(dateFormat) 
                                break; 
                        }

                    }
                
                }
                
                cust_contBeneficiario = index + 1
                def percentual        = 100 / (index + 1)  //-- Calcula percentual de divisão do seguro de vida (divisão igual para todos os beneficiários)

                switch (index) {
                    case 0:
                        cust_percentual_0 = percentual
                        break;
                    case 1:
                        cust_percentual_0 = percentual
                        cust_percentual_1 = percentual
                        break;
                    case 2:
                        cust_percentual_0 = percentual
                        cust_percentual_1 = percentual
                        cust_percentual_2 = percentual
                        break;
                    case 3:
                        cust_percentual_0 = percentual
                        cust_percentual_1 = percentual
                        cust_percentual_2 = percentual
                        cust_percentual_3 = percentual
                        break;
                    case 4:
                        cust_percentual_0 = percentual
                        cust_percentual_1 = percentual
                        cust_percentual_2 = percentual
                        cust_percentual_3 = percentual
                        cust_percentual_4 = percentual
                        break;

                }
                
                index ++ //-- Acrece contador de beneficiários

            }

        }


    //  Mapeamento do arquivo Benefit_Enrollment-Beneficiarios.csv
    //---------------------------------------------------------------------------------------------------------
        // csv_Enrollment_Beneficiarios = ",\"$id\",\"$efetiveStartdate\",," +
        //                                "\"$dependentName_0\",,\"$dateOfBirth_0\",\"$cust_percentual_0\"," +
        //                                "\"$dependentName_1\",,\"$dateOfBirth_1\",\"$cust_percentual_1\"," +
        //                                "\"$dependentName_2\",,\"$dateOfBirth_2\",\"$cust_percentual_2\",\"100.0\"," +
        //                                "\"$dependentName_3\",,\"$dateOfBirth_3\",\"$cust_percentual_3\"," +
        //                                "\"$dependentName_4\",,\"$dateOfBirth_4\",\"$cust_percentual_4\",\"$cust_contBeneficiario\""
        csv_Enrollment_Beneficiarios = ",\"$id\",\"$efetiveStartdate\",," +
                                       "\"$dependentName_0\",,\"$dateOfBirth_0\",," +
                                       "\"$dependentName_1\",,\"$dateOfBirth_1\",," +
                                       "\"$dependentName_2\",,\"$dateOfBirth_2\",,\"100.0\"," +
                                       "\"$dependentName_3\",,\"$dateOfBirth_3\",," +
                                       "\"$dependentName_4\",,\"$dateOfBirth_4\",,\"$cust_contBeneficiario\""
    
    }else{

        csv_Enrollment_Beneficiarios = ""

    }
    
    message.setProperty("p_sv_Benefit_Enrollment",       csv_BenefitEnrollment)
    message.setProperty("p_sv_Enrollment_Details",       csv_Enrollment_Details)
    message.setProperty("p_sv_Enrollment_Beneficiarios", csv_Enrollment_Beneficiarios)

}


// Método que mapeia os dados do Beneficio de Vale Transporete
//------------------------------------------------------------------------------------------------------------------------------------
def void mapBnefTransp(Message message, String obj, String body, String dateFormat) {
    def valueMap     = ITApiFactory.getApi(ValueMappingApi.class, null)
    def jsonParser   = new JsonSlurper()
    def jsonObject   = jsonParser.parseText(obj)
    def jsonObj_body = jsonParser.parseText(body)
    
    def startdate                = message.getHeaders().get("h_data")
    def efetiveStartdate         = Date.parse("yyyy-MM-dd", jsonObj_body.admission_date).format(dateFormat) //----------------
    def externalCode               //---- "VT_U | VT_E_U" 
    def workerId                 = message.getHeaders().get("h_id")
    def currency_code            = "UN"
    def effectiveStatus          = "A"
    def schedulePeriod_id        = "BENE"
    def amount                   = "1" 
    def entitlementAmount        = "1" 
    def cust_vtmotivoSolicitacao = "01"
    def cust_estacionamentoUOL
    def cust_optante_Code 
    def cust_alteracaoveiculos
    String csv_BenefitEnrollment
    def id
    
    def uid_estacionamneto = "0d672cdd-4719-421e-8ae6-31f9ef5e34c1"
    
//  Verifica se o candidato aceitou o Beneficio
//------------------------------------------------------------------------------------------------------------------------------------
    if(jsonObject.option.accept){
         
        cust_optante_Code = "01" // Sim para o beneficio
        
    }else{
        
        cust_optante_Code = "02" // Não para o beneficio
        
    }
    
//  Verifica se o candidato é estagiario
//------------------------------------------------------------------------------------------------------------------------------------
    if(jsonObj_body.pagamento.vinculo == "estagio"){
         
        externalCode = "VT_E_U"  // Estagiário - Auxilio Transporte
        
    }else{
        
        externalCode = "VT_U"  // Não é estagiário - Vale Transporte
        
    }
    
    id = "$workerId" + "_$externalCode"

//  Mapeamento do arquivo Benefit_Enrollment.csv
//------------------------------------------------------------------------------------------------------------------------------------

    // Verifica se o candidato escolheu a opção de transporte ou estacionamento
    //--------------------------------------------------------------------------------
    def benefit_opt = jsonObject.option.benefit
    def benefit_type
    
    jsonObject.benefits.forEach {p -> 
        if (p.benefit.uid == benefit_opt){
            
            benefit_type = p.benefit.type
            
        }
    }
    if(cust_optante_Code == "01"){ //---- Testa se o candidato optou pelo benefico
        
        if (benefit_type == uid_estacionamneto){ 
            
            cust_estacionamentoUOL = "1"
            externalCode           = "ESTA_U"
            id                     = "$workerId" + "_$externalCode"
            cust_alteracaoveiculos = "1"
            currency_code          = "BRL"
            
            csv_BenefitEnrollment =  ",\"$id\",\"$efetiveStartdate\",\"$externalCode\",\"$workerId\"," +
                                     "\"$amount\",\"$currency_code\",\"$effectiveStatus\",,\"$entitlementAmount\"," +
                                     "\"$schedulePeriod_id\",,,,,,,,,,\"$startdate\",,,\"$externalCode\"" +
                                     ",,,,\"$cust_optante_Code\",\"Importante: para cancelamentos solicitados" +
                                     " após o dia 7 de cada mês, a vigência será o 1º dia do mês " +
                                     "seguinte. Exemplo: cancelamento em 10/abr, vigência em 01/mai.\"," +
                                     ",,,,,,,,\"\",,,,,,,,,,,,,,,,,,\"$cust_alteracaoveiculos\",,,,\"$cust_estacionamentoUOL\",,"
            
        }else{
            
            csv_BenefitEnrollment =  ",\"$id\",\"$efetiveStartdate\",\"$externalCode\",\"$workerId\"," +
                                     "\"$amount\",\"$currency_code\",\"$effectiveStatus\",,\"$entitlementAmount\"," +
                                     "\"$schedulePeriod_id\",,,,,,,,,,\"$startdate\",,,\"$externalCode\"" +
                                     ",,,,\"$cust_optante_Code\",\"Importante: para cancelamentos solicitados" +
                                     " após o dia 7 de cada mês, a vigência será o 1º dia do mês " +
                                     "seguinte. Exemplo: cancelamento em 10/abr, vigência em 01/mai.\"," +
                                     ",,,,,,,,\"$cust_vtmotivoSolicitacao\",,,,,,,,,,,,,,,,,,,,,,,,"    
                                        
        }
    
    }
                                    
    // println csv_BenefitEnrollment
    message.setProperty("p_vt_Benefit_Enrollment",   csv_BenefitEnrollment)
    
}

