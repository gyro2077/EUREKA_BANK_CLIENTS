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

public class ServicioRestJava implements ServicioBancario {
    private final String endpoint;
    private final Properties config;
    private final Gson gson = new Gson();

    public ServicioRestJava(Properties config) {
        this.config = config;
        this.endpoint = config.getProperty("rest.java.url", "http://209.145.48.25:8090/resources/corebancario");
    }

    @Override
    public Usuario login(String username, String password) throws Exception {
        String json = "{\"usuario\":\"" + username + "\",\"password\":\"" + password + "\"}";
        ConsolaDebug.log("REST JAVA - LOGIN REQUEST", json);

        String response = sendRequest("/login", "POST", json);
        ConsolaDebug.log("REST JAVA - LOGIN RESPONSE", response);

        LoginResponse loginResp = gson.fromJson(response, LoginResponse.class);
        if (loginResp.resultado == null || !loginResp.resultado.equals("Exitoso")) {
            throw new Exception("Login fallido: " + response);
        }
        return new Usuario(username, "NO_TOKEN_NEEDED", Backend.REST_JAVA);
    }

    @Override
    public List<Movimiento> obtenerMovimientos(String token, String cuenta) throws Exception {
        String response = sendRequest("/movimientos/" + cuenta, "GET", null, token);
        ConsolaDebug.log("REST JAVA - MOVIMIENTOS RESPONSE", response);

        java.lang.reflect.Type listType = new TypeToken<List<Movimiento>>(){}.getType();
        return gson.fromJson(response, listType);
    }

    @Override
    public Deposito registrarDeposito(String token, String cuenta, double importe) throws Exception {
        String json = "{\"cuenta\":\"" + cuenta + "\",\"importe\":" + importe + "}";
        ConsolaDebug.log("REST JAVA - DEPOSITO REQUEST", json);

        String response = sendRequest("/deposito", "POST", json, token);
        ConsolaDebug.log("REST JAVA - DEPOSITO RESPONSE", response);

        return gson.fromJson(response, Deposito.class);
    }

    @Override
    public String transferencia(String token, String cuentaOrigen, String cuentaDestino, double importe) throws Exception {
        String json = "{\"cuentaOrigen\":\"" + cuentaOrigen + "\",\"cuentaDestino\":\"" + cuentaDestino + "\",\"importe\":" + importe + "}";
        ConsolaDebug.log("REST JAVA - TRANSFERENCIA REQUEST", json);

        String response = sendRequest("/transferencia", "POST", json, token);
        ConsolaDebug.log("REST JAVA - TRANSFERENCIA RESPONSE", response);

        return response;
    }

    @Override
    public String getEndpoint() {
        return endpoint;
    }

    @Override
    public Backend getBackend() {
        return Backend.REST_JAVA;
    }

    private String sendRequest(String path, String method, String body) throws Exception {
        return sendRequest(path, method, body, null);
    }

    private String sendRequest(String path, String method, String body, String token) throws Exception {
        java.net.URL url = new java.net.URL(endpoint + path);
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
        ConsolaDebug.info("HTTP Response Code: " + code);
        
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

    private static class MovimientosResponse {
        List<Movimiento> movimientos = new ArrayList<>();
    }
}