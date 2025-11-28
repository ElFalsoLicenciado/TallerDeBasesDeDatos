package Controller.RH;

import Model.DAO.EmpleadoDAO;
import Model.Entities.Empleado;
import Util.AlertUtils;
import Util.Navigation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

public class RHListaController {

    @FXML private ListView<Empleado> listaViewEmpleados;

    @FXML
    public void initialize() {
        EmpleadoDAO dao = new EmpleadoDAO();
        // Carga la lista
        listaViewEmpleados.setItems(FXCollections.observableArrayList(dao.listarTodos()));
    }

    @FXML
    public void irAEditar() {
        Empleado seleccionado = listaViewEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor seleccione un empleado de la lista.");
            return;
        }

        // Pasamos el empleado al controlador del formulario
        RHFormularioController.setEmpleadoAEditar(seleccionado);
        Navigation.cambiarVista("/View/RH/RH_Formulario.fxml");
    }

    @FXML
    public void volverAlMenu() {
        Navigation.cambiarVista("/View/RH/RH_Menu.fxml");
    }

    private void mostrarAlerta(String titulo, String contenido) {
        AlertUtils.mostrar(Alert.AlertType.ERROR, titulo, contenido);
    }
}