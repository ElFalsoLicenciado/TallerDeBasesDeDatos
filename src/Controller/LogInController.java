package Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

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
    }
}
