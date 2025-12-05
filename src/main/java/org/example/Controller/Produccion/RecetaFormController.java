package org.example.Controller.Produccion;

import org.example.Model.DAO.IngredienteDAO;
import org.example.Model.DAO.RecetaDAO;
import org.example.Model.Entities.Ingrediente;
import org.example.Util.AlertUtils;
import org.example.Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;

public class RecetaFormController {

    @FXML private TextField txtNombre, txtCantidadProd, txtUnidadProd, txtTiempo, txtCantIngrediente;
    @FXML private TextArea txtInstrucciones;
    @FXML private ComboBox<Ingrediente> cmbIngrediente;
    @FXML private Label lblUnidadIng;
    @FXML private ListView<String> listaIngredientes;

    // Mapa para guardar Ingrediente -> Cantidad mientras se edita
    private Map<Ingrediente, Double> mapaIngredientes = new HashMap<>();
    private ObservableList<String> itemsVista = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Cargar ingredientes disponibles
        IngredienteDAO ingDao = new IngredienteDAO();
        cmbIngrediente.getItems().setAll(ingDao.listarTodos());

        // Listener para unidad
        cmbIngrediente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) lblUnidadIng.setText(newVal.getUnidad());
        });

        listaIngredientes.setItems(itemsVista);
    }

    @FXML
    public void agregarIngrediente() {
        Ingrediente ing = cmbIngrediente.getValue();
        if (ing == null || txtCantIngrediente.getText().isEmpty()) {
            AlertUtils.mostrar(Alert.AlertType.WARNING, "Datos faltantes", "Seleccione ingrediente y cantidad.");
            return;
        }

        try {
            double cant = Double.parseDouble(txtCantIngrediente.getText());
            if (cant <= 0) throw new NumberFormatException();

            mapaIngredientes.put(ing, cant);
            actualizarListaVista();

            txtCantIngrediente.clear();
            cmbIngrediente.requestFocus();

        } catch (NumberFormatException e) {
            AlertUtils.mostrar(Alert.AlertType.ERROR, "Error", "Cantidad inválida.");
        }
    }

    @FXML
    public void quitarIngrediente() {
        int idx = listaIngredientes.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            // Truco: Como el map no tiene orden, buscamos por nombre en el string de la lista
            // Una forma más robusta sería usar un objeto wrapper en el ListView, pero esto funciona para MVP
            String itemTexto = itemsVista.get(idx);
            // Eliminamos del mapa (esto es simplificado, asume nombres únicos)
            mapaIngredientes.keySet().removeIf(k -> itemTexto.startsWith(k.getNombre()));
            actualizarListaVista();
        }
    }

    private void actualizarListaVista() {
        itemsVista.clear();
        for (Map.Entry<Ingrediente, Double> entry : mapaIngredientes.entrySet()) {
            itemsVista.add(entry.getKey().getNombre() + " - " + entry.getValue() + " " + entry.getKey().getUnidad());
        }
    }

    @FXML
    public void guardar() {
        if (txtNombre.getText().isEmpty() || mapaIngredientes.isEmpty()) {
            AlertUtils.mostrar(Alert.AlertType.WARNING, "Datos incompletos", "La receta debe tener nombre y al menos un ingrediente.");
            return;
        }

        try {
            String nombre = txtNombre.getText();
            String instruc = txtInstrucciones.getText();
            int tiempo = Integer.parseInt(txtTiempo.getText());
            double cantProd = Double.parseDouble(txtCantidadProd.getText());
            String unidad = txtUnidadProd.getText();
            int idUsuario = SessionManager.getInstance().getUsuarioActual().getId();

            RecetaDAO dao = new RecetaDAO();
            if (dao.guardarReceta(nombre, instruc, tiempo, cantProd, unidad, idUsuario, mapaIngredientes)) {
                AlertUtils.mostrar(Alert.AlertType.INFORMATION, "Éxito", "Receta creada correctamente.");
                cerrar();
            } else {
                AlertUtils.mostrar(Alert.AlertType.ERROR, "Error", "No se pudo guardar la receta.");
            }

        } catch (NumberFormatException e) {
            AlertUtils.mostrar(Alert.AlertType.ERROR, "Error de formato", "Tiempo y Rendimiento deben ser números.");
        }
    }

    @FXML
    public void cerrar() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}