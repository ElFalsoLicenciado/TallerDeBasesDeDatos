package Controller;

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

    /**
     * Este método debe estar asociado al onAction del botón en el FXML.
     */
    public void continuePressed() {
        String usuarioTexto = userField.getText().trim();
        String passTexto = passField.getText();

        // 1. Validar campos vacíos
        if (usuarioTexto.isEmpty() || passTexto.isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Campos vacíos", "Por favor ingresa usuario y contraseña.");
            return;
        }

        // 2. Llamar al DAO para verificar en la Base de Datos
        UsuarioDAO dao = new UsuarioDAO();
        Usuario usuarioEncontrado = dao.login(usuarioTexto, passTexto);

        if (usuarioEncontrado != null) {
            // 3. Éxito: Guardar en sesión
            SessionManager.getInstance().login(usuarioEncontrado);
            System.out.println("Login exitoso para: " + usuarioEncontrado.getUsuario());

            // 4. Cambiar a la ventana principal
            abrirVentanaPrincipal();
        } else {
            // 5. Fallo: Credenciales incorrectas
            mostrarAlerta(Alert.AlertType.ERROR, "Acceso Denegado", "Usuario o contraseña incorrectos.");
        }
    }

    private void abrirVentanaPrincipal() {
        try {
            // A. Cargar el FXML del Layout Principal (que ya incluye la sidebar)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/MainLayout.fxml"));
            Parent root = loader.load();

            // B. Obtener el Stage (ventana) actual desde el botón
            Stage stage = (Stage) continueButton.getScene().getWindow();

            // C. Configurar la nueva escena
            Scene scene = new Scene(root);

            // D. Cambiar la escena y centrar la ventana
            stage.setScene(scene);
            stage.setTitle("Lua's Place - Sistema de Gestión");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta(Alert.AlertType.ERROR, "Error Crítico", "No se pudo cargar el sistema principal.\n" + e.getMessage());
        }
    }

    // Método helper para mostrar alertas fácilmente
    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}