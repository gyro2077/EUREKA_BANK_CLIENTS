package ec.edu.espe.eurekadesktop.controllers;

import ec.edu.espe.eurekadesktop.app.Main;
import ec.edu.espe.eurekadesktop.context.ContextoBackend;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainController {
    @FXML private Label lblUsuario;
    @FXML private Label lblBackend;
    @FXML private Button btnDeposito;
    @FXML private Button btnConsulta;
    @FXML private Button btnTransferencia;
    @FXML private Button btnSalir;

    @FXML
    private void initialize() {
        if (ContextoBackend.getInstance() != null) {
            lblUsuario.setText("Usuario: " + ContextoBackend.getInstance().getUsuario().getUsername());
            lblBackend.setText("Backend: " + ContextoBackend.getInstance().getUsuario().getBackend().getDisplayName());
        }
    }

    @FXML
    private void handleDeposito() {
        Main.loadView("ec/edu/espe/eurekadesktop/views/DepositoView.fxml");
    }

    @FXML
    private void handleConsulta() {
        Main.loadView("ec/edu/espe/eurekadesktop/views/ConsultaView.fxml");
    }

    @FXML
    private void handleTransferencia() {
        Main.loadView("ec/edu/espe/eurekadesktop/views/TransferenciaView.fxml");
    }

    @FXML
    private void handleSalir() {
        ContextoBackend.limpiar();
        Main.loadView("ec/edu/espe/eurekadesktop/views/LoginView.fxml");
    }

    public static void loadMainView() {
        Main.loadView("ec/edu/espe/eurekadesktop/views/MainView.fxml");
    }
}