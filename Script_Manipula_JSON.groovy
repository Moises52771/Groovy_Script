import com.sap.gateway.ip.core.customdev.util.Message;
    import java.util.HashMap;
    import groovy.json.*;
    
    def Message processData(Message message) {
        json_File(message)
    }

    def Message json_File(Message message){

        def body = message.getBody(String)
        def jsonFile = new JsonSlurper()
        def jsonObj = jsonFile.parseText(body)
        jsonObj.remove('ibge')
        jsonObj.remove('gia')
        jsonObj.remove('siafi')

        def json = new JsonBuilder(jsonObj).toPrettyString();  
        def json1 = json.replace("logradouro", "rua")

        println (json1)

        message.setBody(json1);

        return message; 
    }

    def String fibonach() {
     //--------- Teste Fibonach
        def num1 = 0
        def num2 = 1
        def pxNum 

        def fib = num1 + ", " + num2

        for (int i = 0; i < 30; i++) {
            pxNum = num1 + num2

            num1 = num2
            num2 = pxNum

            fib = fib + ", " + pxNum
        }

        println (fib)
    }
