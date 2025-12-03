package Controller.Inventario;

import Model.DAO.ProductoDAO;
import Model.DAO.RecetaDAO;
import Model.Entities.Producto;
import Util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Map;

public class ProductoFormController {

    @FXML private TextField txtNombre, txtPrecio, txtSabor;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<RecetaItem> cmbReceta;

    @FXML
    public void initialize() {
        // Llenar Tipos (Deben coincidir con el ENUM de Postgres)
        cmbTipo.getItems().addAll("Bebida", "Pan", "Postre", "Otros");

        // Llenar Recetas
        cargarRecetas();
    }

    private void cargarRecetas() {
        RecetaDAO dao = new RecetaDAO();
        Map<Integer, String> recetas = dao.obtenerListaSimple();
        for (Map.Entry<Integer, String> entry : recetas.entrySet()) {
            cmbReceta.getItems().add(new RecetaItem(entry.getKey(), entry.getValue()));
        }
    }

    @FXML
    public void guardar() {
        if (txtNombre.getText().isEmpty() || txtPrecio.getText().isEmpty() ||
                cmbTipo.getValue() == null || cmbReceta.getValue() == null) {
            AlertUtils.mostrar(Alert.AlertType.WARNING, "Datos Incompletos",
                    "Nombre, Precio, Tipo y Receta son obligatorios.");
            return;
        }

        try {
            double precio = Double.parseDouble(txtPrecio.getText());
            if (precio <= 0) throw new NumberFormatException();

            Producto p = new Producto();
            p.setNombre(txtNombre.getText());
            p.setPrecio(precio);
            p.setTipo(cmbTipo.getValue());
            p.setIdReceta(cmbReceta.getValue().id);
            p.setSabor(txtSabor.getText());
            p.setDescripcion(txtDescripcion.getText());

            ProductoDAO dao = new ProductoDAO();
            if (dao.guardar(p)) {
                AlertUtils.mostrar(Alert.AlertType.INFORMATION, "Éxito", "Producto agregado al catálogo.");
                cerrar();
            } else {
                AlertUtils.mostrar(Alert.AlertType.ERROR, "Error", "No se pudo guardar. Verifique conexión.");
            }

        } catch (NumberFormatException e) {
            AlertUtils.mostrar(Alert.AlertType.ERROR, "Precio Inválido", "Ingrese un número válido mayor a 0.");
        }
    }

    @FXML
    public void cerrar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }

    // Clase auxiliar para el combo
    private static class RecetaItem {
        int id; String nombre;
        public RecetaItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}