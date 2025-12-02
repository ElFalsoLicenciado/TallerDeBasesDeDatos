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
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SidebarController {

    @FXML private VBox sidebar;
    @FXML private Label lblUsuario;

    @FXML private Button btnVentas;
    @FXML private Button btnCompras;
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
            lblUsuario.setText("Hola, " + usuario.getNombreCompleto());
        }

        aplicarSeguridad();
    }

    private void aplicarSeguridad() {
        SessionManager sesion = SessionManager.getInstance();

        // 1. VENTAS
        if (btnVentas != null && !sesion.tienePermiso("ventas.crear")) {
            ocultarBoton(btnVentas);
        }

        // 2. INVENTARIO
        if (btnInventario != null && !sesion.tienePermiso("inventario.ajustar")
                && !sesion.tienePermiso("inventario.merma")) {
            ocultarBoton(btnInventario);
        }

        // 3. COMPRAS
        // Asumiendo que el permiso sea 'inventario.ajustar' o uno nuevo 'compras.crear'
        if (btnCompras != null && !sesion.tienePermiso("inventario.ajustar")) {
            ocultarBoton(btnCompras);
        }

        // 4. RH
        if (btnRH != null && !sesion.tienePermiso("usuarios.ver")) {
            ocultarBoton(btnRH);
        }
    }

    /**
     * Método auxiliar para desaparecer completamente un botón.
     */
    private void ocultarBoton(javafx.scene.control.Button btn) {
        btn.setVisible(false); // Lo hace invisible
        btn.setManaged(false); // Hace que el VBox no le reserve espacio
    }

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
    @FXML public void irACompras() { Util.Navigation.cambiarVista("/View/Compras/PantallaCompras.fxml"); }
    @FXML public void irAInventario() { Util.Navigation.cambiarVista("/View/Inventario/PantallaInventario.fxml"); }
    @FXML public void irARH() { Util.Navigation.cambiarVista("/View/RH/RH_Menu.fxml"); }
    @FXML void irAHome() { Util.Navigation.cambiarVista("/View/Home.fxml"); }

    @FXML public void cerrarSesion() {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/View/Images/icon.png"));

            SessionManager.getInstance().logout();
            Stage stageActual = (Stage) sidebar.getScene().getWindow();
            stageActual.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/LogIn/LogIn.fxml"));
            Parent root = loader.load();
            Stage loginStage = new Stage();
            loginStage.getIcons().add(icon);
            loginStage.setScene(new Scene(root));
            loginStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}