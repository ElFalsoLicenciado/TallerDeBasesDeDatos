package Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
                continueButton.requestFocus();
                continueButton.fire();
            }
        });
    }

    public void continuePressed() {
        System.out.println("Usuario: " + userField.getText() + "\n" + "Contraseña: " + passField.getText());
        // 1. Validar credenciales (simulado o con tu DAO)
        boolean loginExitoso = true; // Suponiendo que validaste user/pass

        if (loginExitoso) {
            abrirVentanaPrincipal();
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

            // Opcional: Cargar CSS global si lo tienes
            // scene.getStylesheets().add(getClass().getResource("/View/CSS/Style.css").toExternalForm());

            // D. Cambiar la escena y centrar la ventana
            stage.setScene(scene);
            stage.setTitle("Lua's Place - Sistema de Gestión");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error al cargar la ventana principal.");
        }
    }
}
