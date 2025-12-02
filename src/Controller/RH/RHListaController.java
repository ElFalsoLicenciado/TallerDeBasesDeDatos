package Controller.RH;

import Model.DAO.EmpleadoDAO;
import Model.Entities.Empleado;
import Model.Entities.Usuario;
import Util.AlertUtils;
import Util.Navigation;
import Util.SessionManager;
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