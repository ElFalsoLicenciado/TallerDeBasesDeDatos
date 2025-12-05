package org.example.Controller.MainView;

import org.example.Util.AlertUtils;
import org.example.Util.Navigation;
import org.example.Util.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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

    // Usamos Timeline en lugar de PauseTransition para un chequeo periódico
    private Timeline loopVerificacion;
    private long ultimaActividadMillis;

    // Configuración del tiempo (90 segundos * 1000 ms)
    private static final long TIEMPO_LIMITE_MS = 90 * 1000;

    @FXML
    public void initialize() {
        Navigation.setMainLayout(mainContainer);
        Navigation.cambiarVista("/View/Home.fxml");

        // 1. Inicializamos la marca de tiempo actual
        ultimaActividadMillis = System.currentTimeMillis();

        // 2. Configurar el listener de la escena
        // Es vital esperar a que la escena exista para agregar el filtro global
        mainContainer.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene != null) {
                setupActivityListener(newScene);

                // Listener para la ventana (para detener el timer si cierran la app)
                newScene.windowProperty().addListener((obsWindow, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.setOnHiding(event -> detenerTimer());
                    }
                });
            }
        });

        // 3. Iniciar el ciclo de verificación
        iniciarCicloVerificacion();
    }

    /**
     * Agrega el filtro de eventos a la ESCENA completa.
     * Esto detecta actividad en cualquier parte de la ventana, incluyendo popups.
     */
    private void setupActivityListener(Scene scene) {
        // InputEvent.ANY captura mouse, teclado, toques, etc.
        scene.addEventFilter(InputEvent.ANY, event -> {
            // Actualizamos la variable. Es una operación muy ligera (barata)
            // a diferencia de reiniciar una animación.
            ultimaActividadMillis = System.currentTimeMillis();
        });
    }

    /**
     * Crea un ciclo infinito que revisa cada 1 segundo si ya expiró el tiempo.
     */
    private void iniciarCicloVerificacion() {
        // Revisamos cada 1 segundo
        loopVerificacion = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            long tiempoActual = System.currentTimeMillis();

            // Si la diferencia entre ahora y la última actividad es mayor al límite...
            if ((tiempoActual - ultimaActividadMillis) > TIEMPO_LIMITE_MS) {
                cerrarSesionPorTimeout();
            }
        }));

        loopVerificacion.setCycleCount(Timeline.INDEFINITE);
        loopVerificacion.play();
    }

    private void detenerTimer() {
        if (loopVerificacion != null) {
            System.out.println("DEBUG: Deteniendo monitor de inactividad.");
            loopVerificacion.stop();
        }
    }

    private void cerrarSesionPorTimeout() {
        // --- DOBLE VERIFICACIÓN ---
        if (SessionManager.getInstance().getUsuarioActual() == null) {
            detenerTimer();
            return;
        }

        // Ejecutar en el hilo de UI
        Platform.runLater(() -> {
            System.out.println("⚠️ TIEMPO DE ESPERA AGOTADO: Cerrando sesión...");

            try {
                // 1. Detener el ciclo
                detenerTimer();

                // 2. Limpiar sesión
                SessionManager.getInstance().logout();

                // 3. Cerrar ventana actual
                if (mainContainer.getScene() != null && mainContainer.getScene().getWindow() != null) {
                    ((Stage) mainContainer.getScene().getWindow()).close();
                }

                // 4. Abrir Login
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/LogIn/LogIn.fxml"));
                Parent root = loader.load();
                Image icon = new Image(getClass().getResourceAsStream("/View/Images/icon.png")); // Ajuste ruta segura

                Stage loginStage = new Stage();
                loginStage.setScene(new Scene(root));
                loginStage.getIcons().add(icon);
                loginStage.setTitle("Lua's Place - Acceso");
                loginStage.setResizable(false);
                loginStage.show();

                // Mostrar alerta
                AlertUtils.mostrar(Alert.AlertType.WARNING,
                        "Sesión Caducada",
                        "Tu sesión se ha cerrado automáticamente por inactividad.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}