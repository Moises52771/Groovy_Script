import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.json.*;

def Message processData(Message message) {

    def body     = message.getBody(String)
    def jsonFile = new JsonSlurper()
    def jsonObj  = jsonFile.parseText(body)

    def array_no_os = []
    def json        = jsonFile.parseText("{}")

    //  Cria lista de todos os valores de "no_os"
    //--------------------------------------------------------
    jsonObj.each{ p -> 
        def no_os = p.no_os
        
        if(!array_no_os.contains(no_os)){ 

            //  Header do objeto, dividido por "no_os" 
            //---------------------------------------------------------
            json."$no_os" = jsonFile.parseText("{}")

            json."$no_os".instancia         = p.instancia
            json."$no_os".no_os             = p.no_os
            json."$no_os".cd_safra          = p.cd_safra
            json."$no_os".de_safra          = p.de_safra
            json."$no_os".cd_ccusto         = p.cd_ccusto
            json."$no_os".cd_operacao       = p.cd_operacao
            json."$no_os".de_operacao       = p.de_operacao
            json."$no_os".app_dt_lastupdate = p.app_dt_lastupdate
            json."$no_os".fg_controle       = p.fg_controle
            json."$no_os".fg_situacao       = p.fg_situacao

            json."$no_os".fazendas = buscaFazendas(no_os, jsonObj) // Atribui as fazendas por "no_os"

            array_no_os.add(no_os)

        }
    }

    message.setBody(JsonOutput.prettyPrint(JsonOutput.toJson(json)))
    return message;
}


//  Função que busca as fazendas por "no_os"
//------------------------------------------------------------------------------
def ArrayList buscaFazendas(def id, def json) {

    def array_fazendas = []
    def jsonFile       = new JsonSlurper()

    //  Cria lista de todas as fazendas
    //--------------------------------------------------------
    def array_id_fazendas = []

    json.each{ f -> 

        if(id == f.no_os){
            
            def fa = f.cd_upnivel1 // id fazenda
            
            if(!array_id_fazendas.contains(fa)){ 
                def fazenda_obj    = jsonFile.parseText("{}")

                fazenda_obj.cd_upnivel1 = f.cd_upnivel1
                fazenda_obj.de_upnivel1 = f.de_upnivel1
                fazenda_obj.talhao      = buscaTalhoes(id, fa, json) // Atribui os talhões por fazenda

                array_id_fazendas.add(fa)
                array_fazendas.add(fazenda_obj)
            }
        }
    }


    return array_fazendas
}


//  Função que busca os talhões por fazendas
//------------------------------------------------------------------------------
def ArrayList buscaTalhoes(def id, def id_fazenda, def json) {

    def array_talhoes = []
    def jsonFile      = new JsonSlurper()

    //  Cria lista de todos os talhões
    //--------------------------------------------------------
    def array_id_talhoes = []

    json.each{ t -> 

        if(id == t.no_os && id_fazenda == t.cd_upnivel1){
            
            def ta = t.cd_upnivel3 // num talhão
            def talhoes_obj

            if(!array_id_talhoes.contains(ta)){ 
                
                talhoes_obj = jsonFile.parseText("{}")

                talhoes_obj."cd_upnivel2"    = t.cd_upnivel2
                talhoes_obj."cd_upnivel3"    = t.cd_upnivel3
                talhoes_obj."de_upnivel2"    = t.de_upnivel2
                talhoes_obj."cd_reduz1"      = t.cd_reduz1
                talhoes_obj."cd_reduz2"      = t.cd_reduz2
                talhoes_obj."qt_area"        = t.qt_area
                talhoes_obj."qt_area_execut" = t.qt_area_execut

                array_id_talhoes.add(ta)
            }
            
            array_talhoes.add(talhoes_obj)
        }
    }

    return array_talhoes
}
