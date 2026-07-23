package ec.edu.espe.eurekadesktop.controllers;

import ec.edu.espe.eurekadesktop.app.Main;
import ec.edu.espe.eurekadesktop.context.ContextoBackend;
import ec.edu.espe.eurekadesktop.models.Movimiento;
import ec.edu.espe.eurekadesktop.utils.ConsolaDebug;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import java.util.List;

public class ConsultaController {
    @FXML private TextField txtCuenta;
    @FXML private TableView<Movimiento> tablaMovimientos;
    @FXML private TableColumn<Movimiento, String> colFecha;
    @FXML private TableColumn<Movimiento, String> colTipo;
    @FXML private TableColumn<Movimiento, Double> colMonto;
    @FXML private TableColumn<Movimiento, String> colDescripcion;
    @FXML private Label lblMensaje;

    private ObservableList<Movimiento> movimientosData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        
        formatColumn(colFecha);
        formatColumn(colTipo);
        formatColumn(colMonto);
        formatColumn(colDescripcion);
        
        tablaMovimientos.setItems(movimientosData);
    }
    
    private <T> void formatColumn(TableColumn<Movimiento, T> column) {
        column.setCellFactory(new Callback<TableColumn<Movimiento, T>, TableCell<Movimiento, T>>() {
            @Override
            public TableCell<Movimiento, T> call(TableColumn<Movimiento, T> param) {
                return new TableCell<Movimiento, T>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.toString());
                        }
                        setStyle("-fx-text-fill: black; -fx-alignment: CENTER-LEFT;");
                    }
                };
            }
        });
    }

    @FXML
    private void handleRegresar() {
        MainController.loadMainView();
    }

    @FXML
    private void handleConsultar() {
        lblMensaje.setText("");
        movimientosData.clear();

        String cuenta = txtCuenta.getText().trim();
        if (cuenta.isEmpty()) {
            lblMensaje.setText("Por favor ingrese un número de cuenta");
            return;
        }

        try {
            var contexto = ContextoBackend.getInstance();
            List<Movimiento> movimientos = contexto.getServicio().obtenerMovimientos(
                    contexto.getUsuario().getToken(), cuenta);

            if (movimientos.isEmpty()) {
                lblMensaje.setText("No se encontraron movimientos para la cuenta " + cuenta);
            } else {
                for (Movimiento m : movimientos) {
                    ConsolaDebug.info("Movimiento: fecha=" + m.getFecha() + " tipo=" + m.getTipo() + " monto=" + m.getMonto() + " desc=" + m.getDescripcion());
                }
                movimientosData.addAll(movimientos);
                lblMensaje.setText("Se encontraron " + movimientos.size() + " movimiento(s)");
            }

        } catch (Exception ex) {
            ConsolaDebug.error("CONSULTA FALLIDA", ex.getMessage());
            lblMensaje.setText("Error al consultar: " + ex.getMessage());
        }
    }
}