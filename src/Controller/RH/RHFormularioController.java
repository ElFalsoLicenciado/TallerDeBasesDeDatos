package Controller.RH;

import Model.DAO.EmpleadoDAO;
import Model.Entities.Empleado;
import Util.AlertUtils;
import Util.Navigation;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.ButtonType;

import java.sql.Date;
import java.util.Map;

public class RHFormularioController {

    private static Empleado empleadoAEditar;
    public static void setEmpleadoAEditar(Empleado e) { empleadoAEditar = e; }
    // -------------------------------------------------------

    @FXML private Label lblTitulo;
    @FXML private TextField txtNombres, txtApellidos, txtLugarNac, txtTelefono, txtCorreo, txtDireccion;
    @FXML private DatePicker dpFechaNac;
    @FXML private ComboBox<String> cmbTipo;
    @FXML private ComboBox<SucursalItem> cmbSucursal;
    @FXML private Button btnBaja;
    @FXML private Button btnCredenciales;

    private EmpleadoDAO dao;

    @FXML
    public void initialize() {
        dao = new EmpleadoDAO();
        cmbTipo.getItems().addAll("Administrativo", "Produccion");
        cargarSucursales();

        if (empleadoAEditar != null) { // MODO EDICIÓN
            lblTitulo.setText("Editar Empleado: " + empleadoAEditar.getCodigo());
            btnBaja.setVisible(true);
            btnCredenciales.setVisible(true);
            cargarDatos(empleadoAEditar);
        } else { // MODO NUEVO
            lblTitulo.setText("Nuevo Registro de Personal");
            btnBaja.setVisible(false);
            btnCredenciales.setVisible(false);
        }
    }

    private void cargarDatos(Empleado e) {
        txtNombres.setText(e.getNombres());
        txtApellidos.setText(e.getApellidos());
        txtLugarNac.setText(e.getLugarNacimiento());
        txtTelefono.setText(e.getTelefono());
        txtCorreo.setText(e.getCorreo());
        txtDireccion.setText(e.getDireccion());
        if (e.getFechaNacimiento() != null) dpFechaNac.setValue(e.getFechaNacimiento().toLocalDate());
        cmbTipo.setValue(e.getTipo());

        for (SucursalItem s : cmbSucursal.getItems()) {
            if (s.id == e.getIdSucursal()) {
                cmbSucursal.setValue(s);
                break;
            }
        }
    }

    @FXML
    public void guardar() {
        // 1. Validaciones básicas
        if (txtNombres.getText().isEmpty() || txtApellidos.getText().isEmpty()) {
            mostrarAlerta("Error", "Nombre y Apellido son obligatorios.");
            return;
        }

        // 2. Detectar estado inicial
        boolean esNuevo = (empleadoAEditar == null);
        Empleado e = esNuevo ? new Empleado() : empleadoAEditar;

        // 3. Mapeo de campos del formulario al objeto
        e.setNombres(txtNombres.getText());
        e.setApellidos(txtApellidos.getText());
        e.setLugarNacimiento(txtLugarNac.getText());
        e.setTelefono(txtTelefono.getText());
        e.setCorreo(txtCorreo.getText());
        e.setDireccion(txtDireccion.getText());
        e.setTipo(cmbTipo.getValue());
        if (dpFechaNac.getValue() != null) e.setFechaNacimiento(Date.valueOf(dpFechaNac.getValue()));
        if (cmbSucursal.getValue() != null) e.setIdSucursal(cmbSucursal.getValue().id);

        // 4. Guardar en Base de Datos
        if (dao.guardar(e)) {
            empleadoAEditar = e;

            btnCredenciales.setVisible(true);
            lblTitulo.setText("Editar Empleado: " + e.getCodigo());

            if (esNuevo) {
                mostrarConfirmacionCrearUsuario(e);
            } else {
                mostrarAlerta("Éxito", "Información actualizada correctamente.");
            }
        } else {
            mostrarAlerta("Error", "No se pudo guardar en la base de datos.");
        }
    }

    @FXML
    public void abrirCredenciales() {
        Empleado target = empleadoAEditar;

        // Validación de seguridad
        if (target == null || target.getId() == 0) {
            mostrarAlerta("Atención", "Primero debe guardar los datos del empleado para generar un ID.");
            return;
        }

        try {
            // 1. Pasar el empleado objetivo al controlador de credenciales
            RHCredencialesController.setEmpleadoObjetivo(target);

            // 2. Cargar la vista modal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/RH/RH_Credenciales.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana de atrás
            stage.setTitle("Gestión de Acceso");
            stage.setResizable(false);

            // 3. Mostrar y esperar
            stage.showAndWait();

        } catch (Exception ex) {
            ex.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de credenciales.");
        }
    }

    private void mostrarConfirmacionCrearUsuario(Empleado e) {
        // Definimos los botones
        ButtonType btnSi = new ButtonType("Sí, crear usuario");
        ButtonType btnNo = new ButtonType("No, salir");

        // Usamos la utilidad estilizada
        AlertUtils.confirmar(
                "Empleado Creado",
                "El empleado se guardó correctamente.",
                "¿Deseas crearle un usuario de sistema ahora mismo?",
                btnSi, btnNo
        ).ifPresent(response -> {
            if (response == btnSi) {
                abrirCredenciales();
                cancelar();
            } else {
                cancelar();
            }
        });
    }

    @FXML
    public void darDeBaja() {
        if (empleadoAEditar == null) return;

        if (dao.eliminar(empleadoAEditar.getId())) {
            mostrarAlerta("Baja Exitosa", "El empleado ha sido desactivado.");
            cancelar(); // Volver
        } else {
            mostrarAlerta("Error", "No se pudo dar de baja.");
        }
    }

    @FXML
    public void cancelar() {
        // Lógica de navegación inteligente
        if (empleadoAEditar != null) {
            Navigation.cambiarVista("/View/RH/RH_Lista.fxml");
        } else {
            Navigation.cambiarVista("/View/RH/RH_Menu.fxml");
        }
    }

    private void cargarSucursales() {
        Map<Integer, String> sucursales = dao.obtenerSucursales();
        for (Map.Entry<Integer, String> entry : sucursales.entrySet()) {
            cmbSucursal.getItems().add(new SucursalItem(entry.getKey(), entry.getValue()));
        }
    }

    private void mostrarAlerta(String titulo, String contenido) {
        AlertUtils.mostrar(Alert.AlertType.ERROR, titulo, contenido);
    }

    // Clase auxiliar para llenar el ComboBox
    private static class SucursalItem {
        int id; String nombre;
        public SucursalItem(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
}