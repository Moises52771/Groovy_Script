/*
 The integration developer needs to create the method processData 
 This method takes Message object of package com.sap.gateway.ip.core.customdev.util 
which includes helper methods useful for the content developer:
The methods available are:
    public java.lang.Object getBody()
	public void setBody(java.lang.Object exchangeBody)
    public java.util.Map<java.lang.String,java.lang.Object> getHeaders()
    public void setHeaders(java.util.Map<java.lang.String,java.lang.Object> exchangeHeaders)
    public void setHeader(java.lang.String name, java.lang.Object value)
    public java.util.Map<java.lang.String,java.lang.Object> getProperties()
    public void setProperties(java.util.Map<java.lang.String,java.lang.Object> exchangeProperties) 
    public void setProperty(java.lang.String name, java.lang.Object value)
    public java.util.List<com.sap.gateway.ip.core.customdev.util.SoapHeader> getSoapHeaders()
    public void setSoapHeaders(java.util.List<com.sap.gateway.ip.core.customdev.util.SoapHeader> soapHeaders) 
       public void clearSoapHeaders()
 */
import com.sap.gateway.ip.core.customdev.util.Message;
import java.util.HashMap;
import groovy.xml.XmlUtil;

def Message processData(Message message) {

    String body = message.getBody(java.lang.String);

    Node root = new XmlParser().parseText(body);

    NodeList cust_main_list = root.cust_espelho_ponto_main;

    LinkedHashMap<String, NodeList> records = new LinkedHashMap<String, NodeList>();

    for(Node cust_main : cust_main_list) {

        String curr_external_code = cust_main.externalCode.text();

        if(!records[curr_external_code]) {

            records[curr_external_code] = new NodeList();

            Node cust_employee_id  = new Node(null, "cust_employeeId", cust_main.cust_employeeId.text());
            Node cust_periodo      = new Node(null, "cust_periodo", cust_main.cust_periodo.text());
            Node cust_ponto_parent = new Node(null, "cust_toEspelhoPontoDetail");

            records[curr_external_code].add(cust_employee_id);
            records[curr_external_code].add(cust_periodo);
            records[curr_external_code].add(cust_ponto_parent);

        }

        NodeList cust_ponto_child_list = cust_main.cust_toEspelhoPontoDetail.cust_espellho_ponto_detail;

        for(Node cust_ponto_child : cust_ponto_child_list)
            records[curr_external_code][2].append(cust_ponto_child);

    }

    Node new_root    = new Node(null, "cust_espelho_ponto_main");

    Set<String> keys = records.keySet();

    for(String key : keys) {

        Node new_cust_ponto_main   = new_root.appendNode("cust_espelho_ponto_main");

        new_cust_ponto_main.appendNode("externalCode"   , key);
        new_cust_ponto_main.appendNode("cust_employeeId", records[key][0].text());
        new_cust_ponto_main.appendNode("cust_periodo"   , records[key][1].text());

        Node new_cust_ponto_parent = new_cust_ponto_main.appendNode("cust_toEspelhoPontoDetail");

        List<Node> cust_ponto_child_list = records[key][2].children();

        for(Node cust_ponto_child : cust_ponto_child_list)
            new_cust_ponto_parent.append(cust_ponto_child);

    }

    message.setBody(XmlUtil.serialize(new_root));

    return message;

}
