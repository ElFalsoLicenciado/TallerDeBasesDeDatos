package org.example.Controller.RH;

import org.example.Model.DAO.UsuarioDAO;
import org.example.Model.Entities.Empleado;
import org.example.Model.Entities.Usuario;
import org.example.Util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.util.Map;

public class RHCredencialesController {

    @FXML private Label lblEmpleado;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPass, txtPassConfirm;
    @FXML private ComboBox<RolItem> cmbRol;
    @FXML private CheckBox chkActivo;

    private static Empleado empleadoObjetivo;
    public static void setEmpleadoObjetivo(Empleado e) { empleadoObjetivo = e; }

    private UsuarioDAO dao;
    private Usuario usuarioExistente;

    @FXML
    public void initialize() {
        dao = new UsuarioDAO();
        if (empleadoObjetivo == null) {
            cerrar(); // Seguridad
            return;
        }

        lblEmpleado.setText("Empleado: " + empleadoObjetivo.getNombres() + " " + empleadoObjetivo.getApellidos());
        cargarRoles();

        // Buscar si ya tiene usuario
        usuarioExistente = dao.buscarPorIdEmpleado(empleadoObjetivo.getId());

        if (usuarioExistente != null) {
            // Cargar datos existentes
            txtUsuario.setText(usuarioExistente.getUsuario());
            chkActivo.setSelected(usuarioExistente.isActivo());
            // Seleccionar rol en combo
            for (RolItem item : cmbRol.getItems()) {
                if (item.id == usuarioExistente.getIdRol()) {
                    cmbRol.setValue(item);
                    break;
                }
            }
            txtPass.setPromptText("Dejar vacío para no cambiar");
            txtPassConfirm.setPromptText("Dejar vacío para no cambiar");
        }
    }

    private void cargarRoles() {
        Map<Integer, String> roles = dao.obtenerRoles();
        for (Map.Entry<Integer, String> entry : roles.entrySet()) {
            cmbRol.getItems().add(new RolItem(entry.getKey(), entry.getValue()));
        }
    }

    @FXML
    public void guardar() {
        // Validaciones
        if (txtUsuario.getText().isEmpty() || cmbRol.getValue() == null) {
            mostrarAlerta("Error", "Usuario y Rol son obligatorios.");
            return;
        }

        String p1 = txtPass.getText();
        String p2 = txtPassConfirm.getText();
        boolean esNuevo = (usuarioExistente == null);

        // Si es nuevo, la contraseña es obligatoria. Si edita, es opcional.
        if (esNuevo && p1.isEmpty()) {
            mostrarAlerta("Error", "Debe asignar una contraseña inicial.");
            return;
        }

        if (!p1.equals(p2)) {
            mostrarAlerta("Error", "Las contraseñas no coinciden.");
            return;
        }

        // Preparar objeto
        Usuario u = (usuarioExistente == null) ? new Usuario() : usuarioExistente;
        u.setIdEmpleado(empleadoObjetivo.getId());
        u.setUsuario(txtUsuario.getText());
        u.setIdRol(cmbRol.getValue().id);
        u.setActivo(chkActivo.isSelected());

        if (dao.guardarUsuarioSistema(u, esNuevo, p1)) {
            mostrarAlerta("Éxito", "Credenciales guardadas correctamente.");
            cerrar();
        } else {
            mostrarAlerta("Error", "No se pudo guardar. Es posible que el usuario ya exista.");
        }
    }

    @FXML
    public void cerrar() {
        Stage stage = (Stage) txtUsuario.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String contenido) {
        AlertUtils.mostrar(Alert.AlertType.ERROR, titulo, contenido);
    }

    private static class RolItem {
        int id; String nombre;
        public RolItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}