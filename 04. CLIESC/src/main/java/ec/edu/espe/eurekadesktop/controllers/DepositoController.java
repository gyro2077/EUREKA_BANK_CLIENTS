package ec.edu.espe.eurekadesktop.controllers;

import ec.edu.espe.eurekadesktop.context.ContextoBackend;
import ec.edu.espe.eurekadesktop.models.Deposito;
import ec.edu.espe.eurekadesktop.utils.ConsolaDebug;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class DepositoController {
    @FXML private TextField txtCuenta;
    @FXML private TextField txtImporte;
    @FXML private Button btnProcesar;
    @FXML private Button btnRegresar;
    @FXML private VBox panelResultado;
    @FXML private Label lblResultado;
    @FXML private Label lblDetalle;
    @FXML private Label lblError;

    @FXML
    private void initialize() {
        panelResultado.setVisible(false);
    }

    @FXML
    private void handleRegresar() {
        MainController.loadMainView();
    }

    @FXML
    private void handleProcesar() {
        lblError.setText("");
        panelResultado.setVisible(false);

        String cuenta = txtCuenta.getText().trim();
        String importeStr = txtImporte.getText().trim();

        if (cuenta.isEmpty() || importeStr.isEmpty()) {
            lblError.setText("Por favor complete todos los campos");
            return;
        }

        double importe;
        try {
            importe = Double.parseDouble(importeStr);
            if (importe <= 0) {
                lblError.setText("El importe debe ser mayor a cero");
                return;
            }
        } catch (NumberFormatException e) {
            lblError.setText("Ingrese un importe válido");
            return;
        }

        try {
            var contexto = ContextoBackend.getInstance();
            Deposito deposito = contexto.getServicio().registrarDeposito(
                    contexto.getUsuario().getToken(), cuenta, importe);

            panelResultado.setVisible(true);
            if (deposito.isExitoso()) {
                lblResultado.setText("Deposito procesado exitosamente");
                lblResultado.setStyle("-fx-text-fill: #22C55E;");
                lblDetalle.setText("Cuenta: " + cuenta + "\nImporte: $" + importe);
            } else {
                lblResultado.setText("Deposito no procesado");
                lblResultado.setStyle("-fx-text-fill: #F59E0B;");
                lblDetalle.setText(deposito.getResultado());
            }

            txtCuenta.clear();
            txtImporte.clear();

        } catch (Exception ex) {
            ConsolaDebug.error("DEPOSITO FALLIDO", ex.getMessage());
            lblError.setText("Error al procesar deposito:\n" + ex.getMessage());
        }
    }
}