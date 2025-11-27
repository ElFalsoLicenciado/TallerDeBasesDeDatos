package Controller.MainView;

import Util.Navigation;
import Util.SessionManager;
import Model.Entities.Usuario;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button; // Importante
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SidebarController {

    @FXML private VBox sidebar;
    @FXML private Label lblUsuario;

    // --- NUEVO: Necesitas inyectar los botones para poder deshabilitarlos ---
    @FXML private Button btnVentas;
    @FXML private Button btnInventario;
    @FXML private Button btnRH;
    // -----------------------------------------------------------------------

    private final double ANCHO_CONTRAIDO = 85.0;
    private final double ANCHO_EXPANDIDO = 220.0;
    private final Duration DURACION = Duration.millis(300);
    private Timeline animacion;

    @FXML
    public void initialize() {
        // 1. Configuración Visual
        sidebar.setPrefWidth(ANCHO_CONTRAIDO);
        sidebar.setMinWidth(ANCHO_CONTRAIDO);
        sidebar.setMaxWidth(ANCHO_CONTRAIDO);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sidebar.widthProperty());
        clip.heightProperty().bind(sidebar.heightProperty());
        sidebar.setClip(clip);

        // 2. Configurar Usuario
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario != null) {
            // Asegúrate de que getNombreCompleto() exista en tu entidad Usuario
            // Si no existe, usa usuario.getUsuario() por mientras
            lblUsuario.setText("Hola, " + usuario.getNombreCompleto());
        }

        // 3. Aplicar Seguridad (DESCOMENTADO)
        aplicarSeguridad();
    }

    private void aplicarSeguridad() {
        SessionManager sesion = SessionManager.getInstance();

        // Si el botón es null, significa que no tiene fx:id en el FXML
        // Verifica Sidebar.fxml si esto da NullPointerException

        if (btnVentas != null && !sesion.tienePermiso("ventas.crear")) {
            btnVentas.setDisable(true);
            btnVentas.setOpacity(0.5);
        }

        if (btnInventario != null && !sesion.tienePermiso("inventario.ajustar")
                && !sesion.tienePermiso("inventario.merma")) {
            btnInventario.setDisable(true);
            btnInventario.setOpacity(0.5);
        }

        if (btnRH != null && !sesion.tienePermiso("usuarios.ver")) {
            btnRH.setDisable(true);
            btnRH.setOpacity(0.5);
        }
    }

    // ... (Métodos de animación y navegación siguen igual) ...

    @FXML public void expandirSidebar() {
        sidebar.setMaxWidth(ANCHO_EXPANDIDO);
        ejecutarAnimacion(ANCHO_EXPANDIDO);
    }

    @FXML public void contraerSidebar() {
        ejecutarAnimacion(ANCHO_CONTRAIDO);
    }

    private void ejecutarAnimacion(double anchoObjetivo) {
        if (animacion != null) animacion.stop();
        animacion = new Timeline();
        KeyValue valorAncho = new KeyValue(sidebar.prefWidthProperty(), anchoObjetivo, Interpolator.EASE_BOTH);
        KeyValue valorMinAncho = new KeyValue(sidebar.minWidthProperty(), anchoObjetivo, Interpolator.EASE_BOTH);
        KeyFrame frame = new KeyFrame(DURACION, valorAncho, valorMinAncho);
        animacion.getKeyFrames().add(frame);
        animacion.play();
    }

    @FXML public void irAVentas() { Navigation.cambiarVista("/View/Ventas/PantallaVentas.fxml"); }
    @FXML
    public void irAInventario() {
        Util.Navigation.cambiarVista("/View/Inventario/PantallaInventario.fxml");
    }
    @FXML public void irARH() { /* ... */ }
    @FXML void irAHome() { Util.Navigation.cambiarVista("/View/Home.fxml"); }

    @FXML public void cerrarSesion() {
        try {
            SessionManager.getInstance().logout();
            Stage stageActual = (Stage) sidebar.getScene().getWindow();
            stageActual.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/LogIn/LogIn.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}