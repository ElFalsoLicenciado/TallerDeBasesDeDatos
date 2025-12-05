package org.example.Util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;

public class AlertUtils {

    /**
     * Muestra una alerta informativa o de error (solo botón Aceptar).
     */
    public static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);

        estilizarDialogo(alert); // <--- Aplicamos el estilo
        alert.showAndWait();
    }

    /**
     * Muestra un diálogo de confirmación con botones personalizados.
     * Retorna la opción elegida por el usuario.
     */
    public static Optional<ButtonType> confirmar(String titulo, String header, String mensaje, ButtonType... botones) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(mensaje);

        // Si se pasan botones personalizados, los usamos
        if (botones.length > 0) {
            alert.getButtonTypes().setAll(botones);
        }

        estilizarDialogo(alert); // <--- Aplicamos el mismo estilo
        return alert.showAndWait();
    }

    /**
     * Método privado que inyecta el CSS y el Ícono a cualquier alerta.
     */
    private static void estilizarDialogo(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();

        // 1. Cargar CSS
        try {
            dialogPane.getStylesheets().add(
                    Objects.requireNonNull(AlertUtils.class.getResource("/View/CSS/Style.css")).toExternalForm()
            );
            dialogPane.getStyleClass().add("dialog-pane");
        } catch (Exception e) {
            System.err.println("No se pudo cargar el CSS para la alerta.");
        }

        // 2. Cargar Ícono de ventana
        try {
            Stage stage = (Stage) dialogPane.getScene().getWindow();
            stage.getIcons().add(new Image(Objects.requireNonNull(AlertUtils.class.getResourceAsStream("/View/Images/icon.png"))));
        } catch (Exception ignored) {}
    }
}