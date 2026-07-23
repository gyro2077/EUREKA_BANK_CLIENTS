package ec.edu.espe.eurekadesktop.services.rest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import ec.edu.espe.eurekadesktop.models.Backend;
import ec.edu.espe.eurekadesktop.models.Deposito;
import ec.edu.espe.eurekadesktop.models.Movimiento;
import ec.edu.espe.eurekadesktop.models.Usuario;
import ec.edu.espe.eurekadesktop.services.interfaces.ServicioBancario;
import ec.edu.espe.eurekadesktop.utils.ConsolaDebug;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ServicioRestDotNet implements ServicioBancario {
    private final Properties config;
    private final Gson gson = new Gson();

    public ServicioRestDotNet(Properties config) {
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
        String url = getBaseHost() + ":8093/resources/corebancario/login";
        String json = "{\"usuario\":\"" + username + "\",\"password\":\"" + password + "\"}";
        ConsolaDebug.log("REST DOTNET MS - LOGIN REQUEST to " + url, json);

        String response = sendRequestToUrl(url, "POST", json, null);
        ConsolaDebug.log("REST DOTNET MS - LOGIN RESPONSE", response);

        LoginResponse loginResp = gson.fromJson(response, LoginResponse.class);
        if (loginResp == null || loginResp.resultado == null || !loginResp.resultado.equalsIgnoreCase("Exitoso")) {
            throw new Exception("Login fallido: " + response);
        }
        return new Usuario(username, "NO_TOKEN_NEEDED", Backend.REST_DOTNET);
    }

    @Override
    public List<Movimiento> obtenerMovimientos(String token, String cuenta) throws Exception {
        String url = getBaseHost() + ":8098/resources/corebancario/movimientos/" + cuenta;
        String response = sendRequestToUrl(url, "GET", null, token);
        ConsolaDebug.log("REST DOTNET MS - MOVIMIENTOS RESPONSE", response);

        java.lang.reflect.Type listType = new TypeToken<List<Movimiento>>(){}.getType();
        List<Movimiento> list = gson.fromJson(response, listType);
        return list != null ? list : new ArrayList<>();
    }

    @Override
    public Deposito registrarDeposito(String token, String cuenta, double importe) throws Exception {
        String url = getBaseHost() + ":8097/resources/corebancario/deposito";
        String json = "{\"cuenta\":\"" + cuenta + "\",\"importe\":" + importe + "}";
        ConsolaDebug.log("REST DOTNET MS - DEPOSITO REQUEST to " + url, json);

        String response = sendRequestToUrl(url, "POST", json, token);
        ConsolaDebug.log("REST DOTNET MS - DEPOSITO RESPONSE", response);

        return gson.fromJson(response, Deposito.class);
    }

    @Override
    public String transferencia(String token, String cuentaOrigen, String cuentaDestino, double importe) throws Exception {
        String url = getBaseHost() + ":8099/resources/corebancario/transferencia";
        String json = "{\"cuentaOrigen\":\"" + cuentaOrigen + "\",\"cuentaDestino\":\"" + cuentaDestino + "\",\"importe\":" + importe + "}";
        ConsolaDebug.log("REST DOTNET MS - TRANSFERENCIA REQUEST to " + url, json);

        String response = sendRequestToUrl(url, "POST", json, token);
        ConsolaDebug.log("REST DOTNET MS - TRANSFERENCIA RESPONSE", response);

        return response;
    }

    @Override
    public String getEndpoint() {
        return getBaseHost() + ":8093/resources/corebancario";
    }

    @Override
    public Backend getBackend() {
        return Backend.REST_DOTNET;
    }

    private String sendRequestToUrl(String fullUrl, String method, String body, String token) throws Exception {
        java.net.URL url = new java.net.URL(fullUrl);
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
        conn.setRequestMethod(method);
        conn.setDoOutput(body != null);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(Integer.parseInt(config.getProperty("timeout.connection", "10000")));
        conn.setReadTimeout(Integer.parseInt(config.getProperty("timeout.read", "10000")));
        
        if (token != null && !token.isEmpty()) {
            conn.setRequestProperty("Authorization", "Bearer " + token);
        }

        if (body != null) {
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes("UTF-8"));
            }
        }

        int code = conn.getResponseCode();
        ConsolaDebug.info("HTTP Response Code (" + fullUrl + "): " + code);
        
        java.io.InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        
        if (is == null) {
            throw new Exception("HTTP " + code + ": Sin cuerpo de respuesta");
        }

        try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static class LoginResponse {
        String resultado;
    }
}