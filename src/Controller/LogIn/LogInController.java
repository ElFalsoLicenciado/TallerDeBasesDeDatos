package Controller.LogIn;

import Model.DAO.UsuarioDAO;
import Model.Entities.Usuario;
import Util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Set; // <--- Importante
import Util.AlertUtils;

public class LogInController {
    @FXML Button continueButton;
    @FXML TextField userField;
    @FXML PasswordField passField;

    @FXML
    public void initialize() {
        userField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                passField.requestFocus();
            }
        });

        passField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                continueButton.fire();
            }
        });
    }

    public void continuePressed() {
        String usuarioTexto = userField.getText().trim();
        String passTexto = passField.getText();

        // 1. Validar campos vacíos
        if (usuarioTexto.isEmpty() || passTexto.isEmpty()) {
            AlertUtils.mostrar(Alert.AlertType.WARNING, "Campos vacíos", "Por favor ingresa usuario y contraseña.");
            return;
        }

        // 2. Verificar credenciales
        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuarioEncontrado = dao.login(usuarioTexto, passTexto);

        if (usuarioEncontrado != null) {
            // 3. Obtener los permisos del rol del usuario
            Set<String> permisos = dao.obtenerPermisos(usuarioEncontrado.getIdRol());

            // 4. Guardar usuario Y permisos en la sesión
            SessionManager.getInstance().login(usuarioEncontrado, permisos);

            System.out.println("Login exitoso para: " + usuarioEncontrado.getNombreCompleto());
            System.out.println("Permisos cargados: " + permisos.size());

            // 5. Cambiar a la ventana principal
            abrirVentanaPrincipal();

        } else {
            AlertUtils.mostrar(Alert.AlertType.ERROR, "Acceso Denegado", "Usuario o contraseña incorrectos.");
        }
    }

    private void abrirVentanaPrincipal() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MainView/MainLayout.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) continueButton.getScene().getWindow();
            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Lua's Place - Sistema de Gestión");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            AlertUtils.mostrar(Alert.AlertType.ERROR, "Error Crítico", "No se pudo cargar el sistema principal.\n" + e.getMessage());
        }
    }
}