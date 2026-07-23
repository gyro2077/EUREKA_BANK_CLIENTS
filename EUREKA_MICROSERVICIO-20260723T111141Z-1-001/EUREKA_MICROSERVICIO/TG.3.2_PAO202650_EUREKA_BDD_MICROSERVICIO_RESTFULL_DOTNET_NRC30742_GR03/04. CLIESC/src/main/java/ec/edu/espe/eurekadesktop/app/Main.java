package ec.edu.espe.eurekadesktop.app;

import ec.edu.espe.eurekadesktop.factory.ServicioFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class Main extends Application {
    private static Stage primaryStage;
    private static Scene scene;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        Properties config = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                config.load(is);
                ServicioFactory.setConfig(config);
            }
        } catch (Exception e) {
            System.err.println("Error cargando configuración: " + e.getMessage());
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        loadView("ec/edu/espe/eurekadesktop/views/LoginView.fxml");
        primaryStage.setTitle("Eureka Bank - Cliente Desktop");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void loadView(String fxmlPath) {
        try {
            System.out.println("Cargando: " + fxmlPath);
            URL url = Main.class.getClassLoader().getResource(fxmlPath);
            System.out.println("URL: " + url);
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            scene = new Scene(root, 800, 600);
            URL cssUrl = Main.class.getClassLoader().getResource("ec/edu/espe/eurekadesktop/styles/app.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            primaryStage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Error cargando vista: " + fxmlPath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }
}