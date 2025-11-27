package Controller;

import Util.Navigation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class SidebarController {

    @FXML private VBox sidebar;

    // Dimensiones
    private final double ANCHO_CONTRAIDO = 80.0;
    private final double ANCHO_EXPANDIDO = 220.0;
    private final Duration DURACION = Duration.millis(300);

    private Timeline animacion;

    @FXML
    public void initialize() {
        // 1. Configuracion Inicial
        // IMPORTANTE: Desbloquear el ancho máximo para permitir la expansión
        sidebar.setPrefWidth(ANCHO_CONTRAIDO);
        sidebar.setMinWidth(ANCHO_CONTRAIDO);
        sidebar.setMaxWidth(ANCHO_CONTRAIDO); // Empezamos bloqueados en pequeño

        // 2. Crear máscara de recorte (Clip)
        // Esto hace que el texto desaparezca visualmente cuando la sidebar se encoge
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(sidebar.widthProperty());
        clip.heightProperty().bind(sidebar.heightProperty());
        sidebar.setClip(clip);
    }

    @FXML
    public void expandirSidebar() {
        // Al expandir, liberamos el ancho máximo para que pueda crecer
        sidebar.setMaxWidth(ANCHO_EXPANDIDO);
        ejecutarAnimacion(ANCHO_EXPANDIDO);
    }

    @FXML
    public void contraerSidebar() {
        ejecutarAnimacion(ANCHO_CONTRAIDO);
        // (Opcional) Podríamos volver a bloquear el MaxWidth al terminar,
        // pero con la animación de prefWidth es suficiente visualmente.
    }

    private void ejecutarAnimacion(double anchoObjetivo) {
        // Detener animación previa si existe
        if (animacion != null) {
            animacion.stop();
        }

        animacion = new Timeline();

        // Usamos EASE_BOTH para que se sienta suave (acelera y frena)
        KeyValue valorAncho = new KeyValue(sidebar.prefWidthProperty(), anchoObjetivo, Interpolator.EASE_BOTH);
        // También animamos el minWidth para evitar "saltos" si el layout padre intenta comprimirlo
        KeyValue valorMinAncho = new KeyValue(sidebar.minWidthProperty(), anchoObjetivo, Interpolator.EASE_BOTH);

        KeyFrame frame = new KeyFrame(DURACION, valorAncho, valorMinAncho);

        animacion.getKeyFrames().add(frame);
        animacion.play();
    }

    // --- Métodos de Navegación ---
    @FXML public void irAVentas() {
        System.out.println("Navegando a Ventas...");
        // Navigation.cambiarVista("/View/Ventas/PantallaVentas.fxml");
    }

    @FXML public void irAInventario() { System.out.println("Navegando a Inventario..."); }
    @FXML public void irARH() { System.out.println("Navegando a RH..."); }
}