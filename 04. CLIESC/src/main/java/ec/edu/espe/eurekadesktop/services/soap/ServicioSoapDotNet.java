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

public class ServicioSoapDotNet implements ServicioBancario {
    private final String endpoint;
    private final Properties config;

    public ServicioSoapDotNet(Properties config) {
        this.config = config;
        this.endpoint = config.getProperty("soap.dotnet.url", "http://209.145.48.25:8092/CoreBancarioWS");
    }

    @Override
    public Usuario login(String username, String password) throws Exception {
        String xmlRequest = buildLoginRequest(username, password);
        ConsolaDebug.log("SOAP DOTNET - LOGIN REQUEST", xmlRequest);

        String xmlResponse = sendSoapRequest(xmlRequest, "Login");
        ConsolaDebug.log("SOAP DOTNET - LOGIN RESPONSE", xmlResponse);

        return parseLoginResponse(xmlResponse);
    }

    @Override
    public List<Movimiento> obtenerMovimientos(String token, String cuenta) throws Exception {
        String xmlRequest = buildMovimientosRequest(cuenta);
        ConsolaDebug.log("SOAP DOTNET - MOVIMIENTOS REQUEST", xmlRequest);

        String xmlResponse = sendSoapRequest(xmlRequest, "ObtenerMovimientos");
        ConsolaDebug.log("SOAP DOTNET - MOVIMIENTOS RESPONSE", xmlResponse);

        return parseMovimientosResponse(xmlResponse);
    }

    @Override
    public Deposito registrarDeposito(String token, String cuenta, double importe) throws Exception {
        String xmlRequest = buildDepositoRequest(cuenta, importe);
        ConsolaDebug.log("SOAP DOTNET - DEPOSITO REQUEST", xmlRequest);

        String xmlResponse = sendSoapRequest(xmlRequest, "RegistrarDeposito");
        ConsolaDebug.log("SOAP DOTNET - DEPOSITO RESPONSE", xmlResponse);

        return parseDepositoResponse(xmlResponse);
    }

    @Override
    public String transferencia(String token, String cuentaOrigen, String cuentaDestino, double importe) throws Exception {
        String xmlRequest = buildTransferenciaRequest(cuentaOrigen, cuentaDestino, importe);
        ConsolaDebug.log("SOAP DOTNET - TRANSFERENCIA REQUEST", xmlRequest);

        String xmlResponse = sendSoapRequest(xmlRequest, "RegistrarTransferencia");
        ConsolaDebug.log("SOAP DOTNET - TRANSFERENCIA RESPONSE", xmlResponse);

        return parseTransferenciaResponse(xmlResponse);
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public Backend getBackend() {
        return Backend.SOAP_DOTNET;
    }

    private String buildLoginRequest(String username, String password) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ws=\"http://ws.monster.edu.ec/\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ws:Login>" +
               "<ws:usuario>" + XmlUtils.escapeXml(username) + "</ws:usuario>" +
               "<ws:password>" + XmlUtils.escapeXml(password) + "</ws:password>" +
               "</ws:Login>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String buildMovimientosRequest(String cuenta) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ws=\"http://ws.monster.edu.ec/\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ws:ObtenerMovimientos>" +
               "<ws:cuenta>" + XmlUtils.escapeXml(cuenta) + "</ws:cuenta>" +
               "</ws:ObtenerMovimientos>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String buildDepositoRequest(String cuenta, double importe) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ws=\"http://ws.monster.edu.ec/\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ws:RegistrarDeposito>" +
               "<ws:cuenta>" + XmlUtils.escapeXml(cuenta) + "</ws:cuenta>" +
               "<ws:importe>" + importe + "</ws:importe>" +
               "</ws:RegistrarDeposito>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String buildTransferenciaRequest(String cuentaOrigen, String cuentaDestino, double importe) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
               "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
               "xmlns:ws=\"http://ws.monster.edu.ec/\">" +
               "<soapenv:Header/>" +
               "<soapenv:Body>" +
               "<ws:RegistrarTransferencia>" +
               "<ws:cuentaOrigen>" + XmlUtils.escapeXml(cuentaOrigen) + "</ws:cuentaOrigen>" +
               "<ws:cuentaDestino>" + XmlUtils.escapeXml(cuentaDestino) + "</ws:cuentaDestino>" +
               "<ws:importe>" + importe + "</ws:importe>" +
               "</ws:RegistrarTransferencia>" +
               "</soapenv:Body>" +
               "</soapenv:Envelope>";
    }

    private String sendSoapRequest(String xml, String action) throws Exception {
        java.net.URL url = new java.net.URL(endpoint);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
        conn.setRequestProperty("SOAPAction", "\"http://ws.monster.edu.ec/CoreBancarioWS/" + action + "\"");
        conn.setConnectTimeout(Integer.parseInt(config.getProperty("timeout.connection", "10000")));
        conn.setReadTimeout(Integer.parseInt(config.getProperty("timeout.read", "10000")));

        try (java.io.OutputStream os = conn.getOutputStream()) {
            os.write(xml.getBytes("UTF-8"));
        }

        int responseCode = conn.getResponseCode();
        ConsolaDebug.info("HTTP Response Code: " + responseCode);
        
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

        if (xml.contains("LoginResult") && (xml.contains("1") || xml.contains("Exitoso") || xml.contains("true"))) {
            return new Usuario("MONSTER", "token_dotnet", Backend.SOAP_DOTNET);
        }
        
        if (xml.contains("<return>") && (xml.contains("1") || xml.contains("Exitoso") || xml.contains("true"))) {
            return new Usuario("MONSTER", "token_dotnet", Backend.SOAP_DOTNET);
        }

        throw new Exception("Login fallido");
    }

    private List<Movimiento> parseMovimientosResponse(String xml) {
        List<Movimiento> movimientos = new ArrayList<>();

        if (xml.contains("soap:Fault") || xml.contains("Fault")) {
            return movimientos;
        }

        int startResult = xml.indexOf("<ObtenerMovimientosResult>");
        if (startResult == -1) return movimientos;
        
        int endResult = xml.indexOf("</ObtenerMovimientosResult>", startResult);
        if (endResult == -1) return movimientos;
        
        String resultBlock = xml.substring(startResult, endResult);
        
        String[] movimientoBlocks = resultBlock.split("<Movimiento>");
        for (int i = 1; i < movimientoBlocks.length; i++) {
            String movBlock = movimientoBlocks[i];
            
            Movimiento m = new Movimiento();
            m.setCuenta(extractValueCaseInsensitive(movBlock, "Cuenta"));
            m.setTipo(extractValueCaseInsensitive(movBlock, "Tipo"));
            m.setFecha(extractValueCaseInsensitive(movBlock, "Fecha"));
            m.setDescripcion(extractValueCaseInsensitive(movBlock, "Accion"));
            
            String importeStr = extractValueCaseInsensitive(movBlock, "Importe");
            try {
                m.setMonto(Double.parseDouble(importeStr));
            } catch (Exception e) {
                m.setMonto(0);
            }
            
            String nromovStr = extractValueCaseInsensitive(movBlock, "Nromov");
            try {
                m.setNromov(Integer.parseInt(nromovStr));
            } catch (Exception e) {}
            
            if (m.getCuenta() != null && !m.getCuenta().isEmpty()) {
                movimientos.add(m);
            }
        }

        return movimientos;
    }

    private String extractValueCaseInsensitive(String xml, String tag) {
        String startTag = "<" + tag + ">";
        String endTag = "</" + tag + ">";
        int start = xml.indexOf(startTag);
        if (start == -1) return "";
        int end = xml.indexOf(endTag, start);
        if (end == -1) return "";
        return xml.substring(start + startTag.length(), end).trim();
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

        if (xml.contains("estado") && xml.contains("1")) {
            deposito.setResultado("Depósito exitoso");
            deposito.setExitoso(true);
        } else {
            deposito.setResultado("Depósito procesado");
            deposito.setExitoso(true);
        }
        return deposito;
    }

    private String parseTransferenciaResponse(String xml) throws Exception {
        if (xml.contains("soap:Fault") || xml.contains("Fault")) {
            String fault = XmlUtils.extractTag(xml, "faultstring");
            throw new Exception(fault != null ? fault : "Error en transferencia");
        }

        if (xml.contains("estado") && xml.contains("1")) {
            return "Transferencia exitosa";
        }
        return "Transferencia procesada";
    }
}