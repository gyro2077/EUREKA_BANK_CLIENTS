package ec.edu.espe.eurekadesktop.controllers;

import ec.edu.espe.eurekadesktop.context.ContextoBackend;
import ec.edu.espe.eurekadesktop.services.interfaces.ServicioBancario;
import ec.edu.espe.eurekadesktop.utils.ConsolaDebug;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class TransferenciaController {
    @FXML private TextField txtCuentaOrigen;
    @FXML private TextField txtCuentaDestino;
    @FXML private TextField txtImporte;
    @FXML private Label lblMensaje;

    @FXML
    private void initialize() {
        txtCuentaOrigen.setOnAction(e -> txtCuentaDestino.requestFocus());
        txtCuentaDestino.setOnAction(e -> txtImporte.requestFocus());
        txtImporte.setOnAction(e -> handleTransferir());
    }

    @FXML
    private void handleTransferir() {
        lblMensaje.setText("");
        
        String cuentaOrigen = txtCuentaOrigen.getText().trim();
        String cuentaDestino = txtCuentaDestino.getText().trim();
        String importeStr = txtImporte.getText().trim();

        if (cuentaOrigen.isEmpty() || cuentaDestino.isEmpty() || importeStr.isEmpty()) {
            lblMensaje.setText("Por favor complete todos los campos");
            return;
        }

        double importe;
        try {
            importe = Double.parseDouble(importeStr);
            if (importe <= 0) {
                lblMensaje.setText("El importe debe ser mayor a cero");
                return;
            }
        } catch (NumberFormatException ex) {
            lblMensaje.setText("El importe debe ser un número válido");
            return;
        }

        try {
            ServicioBancario servicio = ContextoBackend.getInstance().getServicio();
            String token = ContextoBackend.getInstance().getUsuario().getToken();
            
            ConsolaDebug.info("Iniciando transferencia: " + cuentaOrigen + " -> " + cuentaDestino + " = $" + importe);
            
            String resultado = servicio.transferencia(token, cuentaOrigen, cuentaDestino, importe);
            
            lblMensaje.setTextFill(javafx.scene.paint.Color.valueOf("#22C55E"));
            lblMensaje.setText("Transferencia exitosa: " + resultado);
            
            txtCuentaOrigen.clear();
            txtCuentaDestino.clear();
            txtImporte.clear();
            
        } catch (Exception ex) {
            ConsolaDebug.error("TRANSFERENCIA FALLIDA", ex.getMessage());
            lblMensaje.setTextFill(javafx.scene.paint.Color.valueOf("#EF4444"));
            lblMensaje.setText("Error: " + ex.getMessage());
        }
    }

    @FXML
    private void handleRegresar() {
        MainController.loadMainView();
    }
}