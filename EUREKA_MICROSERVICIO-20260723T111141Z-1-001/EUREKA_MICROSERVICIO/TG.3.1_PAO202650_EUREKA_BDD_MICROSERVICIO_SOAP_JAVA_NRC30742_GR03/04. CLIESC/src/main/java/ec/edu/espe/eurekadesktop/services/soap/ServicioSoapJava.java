package ec.edu.espe.eurekadesktop.services.soap;

import ec.edu.espe.eurekadesktop.models.Backend;
import ec.edu.espe.eurekadesktop.models.Deposito;
import ec.edu.espe.eurekadesktop.models.Movimiento;
import ec.edu.espe.eurekadesktop.models.Usuario;
import ec.edu.espe.eurekadesktop.services.interfaces.ServicioBancario;
import ec.edu.espe.eurekadesktop.utils.ConsolaDebug;
import ec.edu.espe.eurekadesktop.utils.XmlUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ServicioSoapJava implements ServicioBancario {
    private final Properties config;

    public ServicioSoapJava(Properties config) {
        this.config = config;
    }

    private String getBaseHost() {
        String host = config.getProperty("server.host", "http://209.145.48.25");
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        return host;
    }

    @Override
    public Usuario login(String username, String password) throws Exception {
        String targetEndpoint = getBaseHost() + ":8091/ROOT/CoreBancarioWS";
        String xmlRequest = buildLoginRequest(username, password);
        ConsolaDebug.log("SOAP JAVA MS - LOGIN REQUEST to " + targetEndpoint, xmlRequest);

        String xmlResponse = sendSoapRequest(targetEndpoint, xmlRequest);
        ConsolaDebug.log("SOAP JAVA MS - LOGIN RESPONSE", xmlResponse);

        return parseLoginResponse(xmlResponse);
    }

    @Override
    public List<Movimiento> obtenerMovimientos(String token, String cuenta) throws Exception {
        String targetEndpoint = getBaseHost() + ":8095/ROOT/CoreBancarioWS";
        String xmlRequest = buildMovimientosRequest(token, cuenta);
        ConsolaDebug.log("SOAP JAVA MS - MOVIMIENTOS REQUEST to " + targetEndpoint, xmlRequest);

        String xmlResponse = sendSoapRequest(targetEndpoint, xmlRequest);
        ConsolaDebug.log("SOAP JAVA MS - MOVIMIENTOS RESPONSE", xmlResponse);

        return parseMovimientosResponse(xmlResponse);
    }

    @Override
    public Deposito registrarDeposito(String token, String cuenta, double importe) throws Exception {
        String targetEndpoint = getBaseHost() + ":8094/ROOT/CoreBancarioWS";
        String xmlRequest = buildDepositoRequest(token, cuenta, importe);
        ConsolaDebug.log("SOAP JAVA MS - DEPOSITO REQUEST to " + targetEndpoint, xmlRequest);

        String xmlResponse = sendSoapRequest(targetEndpoint, xmlRequest);
        ConsolaDebug.log("SOAP JAVA MS - DEPOSITO RESPONSE", xmlResponse);

        return parseDepositoResponse(xmlResponse);
    }

    @Override
    public String transferencia(String token, String cuentaOrigen, String cuentaDestino, double importe) throws Exception {
        String targetEndpoint = getBaseHost() + ":8096/ROOT/CoreBancarioWS";
        String xmlRequest = buildTransferenciaRequest(token, cuentaOrigen, cuentaDestino, importe);
        ConsolaDebug.log("SOAP JAVA MS - TRANSFERENCIA REQUEST to " + targetEndpoint, xmlRequest);

        String xmlResponse = sendSoapRequest(targetEndpoint, xmlRequest);
        ConsolaDebug.log("SOAP JAVA MS - TRANSFERENCIA RESPONSE", xmlResponse);

        return parseTransferenciaResponse(xmlResponse);
    }

    @Override
    public String getEndpoint() {
        return getBaseHost() + ":8091/ROOT/CoreBancarioWS";
    }

    @Override
    public Backend getBackend() {
        return Backend.SOAP_JAVA;
    }

    private String buildLoginRequest(String username, String password) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ws=\"http://ws.monster.edu.ec/\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ws:login>" +
               "<usuario>" + XmlUtils.escapeXml(username) + "</usuario>" +
               "<password>" + XmlUtils.escapeXml(password) + "</password>" +
               "</ws:login>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String buildMovimientosRequest(String token, String cuenta) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ws=\"http://ws.monster.edu.ec/\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ws:obtenerMovimientos>" +
               "<cuenta>" + XmlUtils.escapeXml(cuenta) + "</cuenta>" +
               "</ws:obtenerMovimientos>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String buildDepositoRequest(String token, String cuenta, double importe) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ws=\"http://ws.monster.edu.ec/\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ws:registrarDeposito>" +
               "<cuenta>" + XmlUtils.escapeXml(cuenta) + "</cuenta>" +
               "<importe>" + importe + "</importe>" +
               "</ws:registrarDeposito>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String buildTransferenciaRequest(String token, String cuentaOrigen, String cuentaDestino, double importe) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ws=\"http://ws.monster.edu.ec/\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ws:registrarTransferencia>" +
               "<cuentaOrigen>" + XmlUtils.escapeXml(cuentaOrigen) + "</cuentaOrigen>" +
               "<cuentaDestino>" + XmlUtils.escapeXml(cuentaDestino) + "</cuentaDestino>" +
               "<importe>" + importe + "</importe>" +
               "</ws:registrarTransferencia>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String sendSoapRequest(String targetEndpoint, String xml) throws Exception {
        java.net.URL url = new java.net.URL(targetEndpoint);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        conn.setRequestProperty("SOAPAction", "\"\"");
        conn.setConnectTimeout(Integer.parseInt(config.getProperty("timeout.connection", "10000")));
        conn.setReadTimeout(Integer.parseInt(config.getProperty("timeout.read", "10000")));

        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(xml.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        ConsolaDebug.info("HTTP Response Code (" + targetEndpoint + "): " + responseCode);
        
        java.io.InputStream is = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
        
        if (is == null) {
            throw new Exception("HTTP " + responseCode + ": Sin cuerpo de respuesta");
        }
        
        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(is, "UTF-8"))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private Usuario parseLoginResponse(String xml) throws Exception {
        if (xml.contains("soap:Fault") || xml.contains("Fault")) {
            String fault = XmlUtils.extractTag(xml, "faultstring");
            throw new Exception(fault != null ? fault : "Error en login");
        }

        String token = XmlUtils.extractTag(xml, "return");
        if (token == null || token.isEmpty()) {
            throw new Exception("Token no recibido");
        }

        return new Usuario("usuario", token, Backend.SOAP_JAVA);
    }

    private List<Movimiento> parseMovimientosResponse(String xml) {
        List<Movimiento> movimientos = new ArrayList<>();

        if (xml.contains("soap:Fault") || xml.contains("Fault")) {
            return movimientos;
        }

        String[] returns = xml.split("<return>");
        for (int i = 1; i < returns.length; i++) {
            String returnBlock = returns[i];
            
            Movimiento m = new Movimiento();
            m.setCuenta(extractValue(returnBlock, "cuenta"));
            m.setTipo(extractValue(returnBlock, "tipo"));
            m.setFecha(extractValue(returnBlock, "fecha"));
            m.setDescripcion(extractValue(returnBlock, "accion"));
            
            String importeStr = extractValue(returnBlock, "importe");
            try {
                m.setMonto(Double.parseDouble(importeStr));
            } catch (Exception e) {
                m.setMonto(0);
            }
            
            String nromovStr = extractValue(returnBlock, "nromov");
            try {
                m.setNromov(Integer.parseInt(nromovStr));
            } catch (Exception e) {}
            
            movimientos.add(m);
        }

        return movimientos;
    }

    private String extractValue(String xml, String tag) {
        String startTag = "<" + tag + ">";
        String endTag = "</" + tag + ">";
        int start = xml.indexOf(startTag);
        if (start == -1) return "";
        int end = xml.indexOf(endTag, start);
        if (end == -1) return "";
        return xml.substring(start + startTag.length(), end).trim();
    }

    private Deposito parseDepositoResponse(String xml) throws Exception {
        Deposito deposito = new Deposito();

        if (xml.contains("soap:Fault") || xml.contains("Fault")) {
            String fault = XmlUtils.extractTag(xml, "faultstring");
            deposito.setResultado(fault != null ? fault : "Error en depósito");
            deposito.setExitoso(false);
            return deposito;
        }

        String resultado = XmlUtils.extractTag(xml, "return");
        deposito.setResultado(resultado != null ? resultado : "Depósito exitoso");
        deposito.setExitoso(true);
        return deposito;
    }

    private String parseTransferenciaResponse(String xml) throws Exception {
        if (xml.contains("soap:Fault") || xml.contains("Fault")) {
            String fault = XmlUtils.extractTag(xml, "faultstring");
            throw new Exception(fault != null ? fault : "Error en transferencia");
        }

        String resultado = XmlUtils.extractTag(xml, "return");
        return resultado != null ? resultado : "Transferencia exitosa";
    }
}