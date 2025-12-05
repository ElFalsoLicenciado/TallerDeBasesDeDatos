package org.example.Controller.RH;

import org.example.Model.DAO.EmpleadoDAO;
import org.example.Model.Entities.Empleado;
import org.example.Model.Entities.Usuario;
import org.example.Util.AlertUtils;
import org.example.Util.Navigation;
import org.example.Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;

public class RHListaController {

    @FXML private ListView<Empleado> listaViewEmpleados;

    @FXML
    public void initialize() {
        EmpleadoDAO dao = new EmpleadoDAO();
        Usuario usuarioActual = SessionManager.getInstance().getUsuarioActual();

        // LÓGICA DE SEGURIDAD
        if (SessionManager.getInstance().tienePermiso("sys.full_access")) {
            // Si es Super Admin, ve todo
            listaViewEmpleados.setItems(FXCollections.observableArrayList(dao.listarTodos()));
        } else {
            // Si es Gerente (o cualquier otro rol), solo ve su sucursal
            listaViewEmpleados.setItems(FXCollections.observableArrayList(
                    dao.listarPorSucursal(usuarioActual.getIdSucursal())
            ));
        }

        listaViewEmpleados.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Si hizo doble clic
                // Verificar que realmente haya algo seleccionado (y no doble clic en blanco)
                if (listaViewEmpleados.getSelectionModel().getSelectedItem() != null) {
                    irAEditar();
                }
            }
        });
    }

    @FXML
    public void irAEditar() {
        Empleado seleccionado = listaViewEmpleados.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            // Usando tu utilidad de alertas estilizadas
            AlertUtils.mostrar(Alert.AlertType.WARNING, "Selección requerida", "Por favor seleccione un empleado de la lista.");
            return;
        }

        RHFormularioController.setEmpleadoAEditar(seleccionado);
        Navigation.cambiarVista("/View/RH/RH_Formulario.fxml");
    }

    @FXML
    public void volverAlMenu() {
        Navigation.cambiarVista("/View/RH/RH_Menu.fxml");
    }
}