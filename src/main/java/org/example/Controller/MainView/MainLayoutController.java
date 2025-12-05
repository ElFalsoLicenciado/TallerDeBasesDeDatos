package org.example.Controller.MainView;

import org.example.Util.AlertUtils;
import org.example.Util.Navigation;
import org.example.Util.SessionManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.input.InputEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class MainLayoutController {

    @FXML private BorderPane mainContainer;

    private PauseTransition timerInactividad;

    @FXML
    public void initialize() {
        Navigation.setMainLayout(mainContainer);
        Navigation.cambiarVista("/View/Home.fxml");

        iniciarTimerInactividad();

        mainContainer.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        // Cuando la ventana se oculte o cierre (por LogOut manual o X), detenemos el timer
                        newWindow.setOnHiding(event -> {
                            System.out.println("DEBUG: Ventana cerrándose, deteniendo timer de inactividad.");
                            if (timerInactividad != null) {
                                timerInactividad.stop();
                            }
                        });
                    }
                });
            }
        });
    }

    private void iniciarTimerInactividad() {
        // 90 segundos = 1.5 minutos
        timerInactividad = new PauseTransition(Duration.seconds(900));
        timerInactividad.setOnFinished(event -> cerrarSesionPorTimeout());

        mainContainer.addEventFilter(InputEvent.ANY, event -> reiniciarTimer());

        timerInactividad.play();
    }

    private void reiniciarTimer() {
        if (timerInactividad != null) {
            timerInactividad.playFromStart();
        }
    }

    private void cerrarSesionPorTimeout() {
        // --- DOBLE VERIFICACIÓN DE SEGURIDAD ---
        // Si el usuario ya es null (ya se salió), no hacemos nada.
        if (SessionManager.getInstance().getUsuarioActual() == null) {
            if (timerInactividad != null) timerInactividad.stop();
            return;
        }

        Platform.runLater(() -> {
            System.out.println("⚠️ TIEMPO DE ESPERA AGOTADO: Cerrando sesión...");

            try {
                // 1. Detener el timer para siempre
                if (timerInactividad != null) timerInactividad.stop();

                // 2. Limpiar sesión
                SessionManager.getInstance().logout();

                // 3. Cerrar ventana actual
                if (mainContainer.getScene() != null && mainContainer.getScene().getWindow() != null) {
                    ((Stage) mainContainer.getScene().getWindow()).close();
                }

                // 4. Abrir Login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/LogIn/LogIn.fxml"));
                Parent root = loader.load();

                Image icon = new Image("/View/Images/icon.png");

                Stage loginStage = new Stage();
                loginStage.setScene(new Scene(root));
                loginStage.getIcons().add(icon);
                loginStage.setTitle("Lua's Place - Acceso");
                loginStage.setResizable(false);
                loginStage.show();

                AlertUtils.mostrar(Alert.AlertType.WARNING,
                        "Sesión Caducada",
                        "Tu sesión se ha cerrado automáticamente por inactividad.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}