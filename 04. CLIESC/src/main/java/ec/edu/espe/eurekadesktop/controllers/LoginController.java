package ec.edu.espe.eurekadesktop.controllers;

import ec.edu.espe.eurekadesktop.context.ContextoBackend;
import ec.edu.espe.eurekadesktop.factory.ServicioFactory;
import ec.edu.espe.eurekadesktop.models.Backend;
import ec.edu.espe.eurekadesktop.models.Usuario;
import ec.edu.espe.eurekadesktop.services.interfaces.ServicioBancario;
import ec.edu.espe.eurekadesktop.utils.ConsolaDebug;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class LoginController {
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<Backend> cmbBackend;
    @FXML private Label lblError;

    @FXML
    private void initialize() {
        txtUsuario.setOnKeyTyped(this::onEnter);
        txtPassword.setOnKeyTyped(this::onEnter);
        cmbBackend.setItems(FXCollections.observableArrayList(Backend.values()));
        cmbBackend.setValue(Backend.REST_JAVA);
    }

    private void onEnter(KeyEvent e) {
        if (e.getCharacter().equals("\r")) {
            handleLogin();
        }
    }

    @FXML
    private void handleLogin() {
        lblError.setText("");
        
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();
        Backend backend = cmbBackend.getValue();

        if (backend == null) {
            lblError.setText("Por favor seleccione un backend");
            return;
        }

        if (usuario.isEmpty() || password.isEmpty()) {
            lblError.setText("Por favor complete todos los campos");
            return;
        }

        try {
            ServicioBancario servicio = ServicioFactory.crearServicio(backend);
            ConsolaDebug.info("Conectando al backend: " + backend.getDisplayName());
            
            Usuario user = servicio.login(usuario, password);
            user.setBackend(backend);
            
            ContextoBackend.setInstance(user, servicio);
            
            MainController.loadMainView();
            
        } catch (Exception ex) {
            ConsolaDebug.error("LOGIN FALLIDO", ex.getMessage());
            lblError.setText("Error de autenticación:\n" + ex.getMessage());
        }
    }
}