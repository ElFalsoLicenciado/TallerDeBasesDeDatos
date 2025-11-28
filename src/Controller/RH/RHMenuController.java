package Controller.RH;

import Util.Navigation;
import javafx.fxml.FXML;

public class RHMenuController {

    @FXML
    public void irAListaEmpleados() {
        Navigation.cambiarVista("/View/RH/RH_Lista.fxml");
    }

    @FXML
    public void irANuevoEmpleado() {
        // Le indicamos al formulario que vamos en modo "CREAR" (null)
        RHFormularioController.setEmpleadoAEditar(null);
        Navigation.cambiarVista("/View/RH/RH_Formulario.fxml");
    }
}