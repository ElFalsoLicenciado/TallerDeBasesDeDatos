package org.example.View.Components;

import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class CoffeeToggle extends StackPane {

    private final BooleanProperty selected = new SimpleBooleanProperty(false);
    private final StackPane knob;
    private final Rectangle knobShape;

    // Dimensiones Exactas para ocultar texto
    private static final double WIDTH = 70;
    private static final double HEIGHT = 30;
    private static final double KNOB_WIDTH = 38; // Un poco más ancho que la mitad para tapar bien

    // Colores del Tema
    private static final Color COLOR_BG = Color.web("#EBE0D0");       // Fondo del carril
    private static final Color COLOR_KNOB_OFF = Color.web("#A68B7C"); // Beige Oscuro (Apagado)
    private static final Color COLOR_KNOB_ON = Color.web("#6F4E37");  // Café Espresso (Encendido)
    private static final Color TEXT_COLOR = Color.web("#4A332A");     // Texto del fondo

    public CoffeeToggle() {
        this.setMinSize(WIDTH, HEIGHT);
        this.setMaxSize(WIDTH, HEIGHT);
        this.setCursor(Cursor.HAND);

        // 1. Fondo (El carril)
        Rectangle background = new Rectangle(WIDTH, HEIGHT);
        background.setArcWidth(4);
        background.setArcHeight(4);
        background.setFill(COLOR_BG);
        background.setStroke(Color.web("#DCC7AA"));

        // 2. Etiquetas de FONDO (Estáticas)
        // Estas etiquetas están fijas en el carril. El Knob las tapará al moverse.

        // 2. Etiquetas de FONDO
        Label lblNo = new Label("NO");
        // USAMOS setStyle PARA FORZAR EL COLOR (Gana sobre tu CSS global)
        lblNo.setStyle("-fx-text-fill: #45272d; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
        lblNo.setTranslateX(16);

        Label lblSi = new Label("SÍ");
        // USAMOS setStyle PARA FORZAR EL COLOR
        lblSi.setStyle("-fx-text-fill: #45272d; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: 'Segoe UI';");
        lblSi.setTranslateX(-16);

        // 3. El Knob (La tapa deslizante SOLIDA sin texto)
        knob = new StackPane();
        knob.setMinSize(KNOB_WIDTH, HEIGHT);
        knob.setMaxSize(KNOB_WIDTH, HEIGHT);

        knobShape = new Rectangle(KNOB_WIDTH, HEIGHT);
        knobShape.setArcWidth(4);
        knobShape.setArcHeight(4);
        knobShape.setFill(COLOR_KNOB_OFF); // Color inicial (OFF)
        knobShape.setStroke(Color.web("#8B735B")); // Borde sutil para definición

        knob.getChildren().add(knobShape);

        // Posición Inicial: A la IZQUIERDA (Tapando el "SÍ")
        // Al tapar el "SÍ", solo se ve el "NO" de la derecha.
        double initialPos = - (WIDTH / 2) + (KNOB_WIDTH / 2);
        knob.setTranslateX(initialPos);

        // 4. Orden de apilado (Importante: El Knob va al final para estar ARRIBA)
        this.getChildren().addAll(background, lblSi, lblNo, knob);

        // 5. Lógica de Click
        this.setOnMouseClicked(e -> setSelected(!isSelected()));
        selected.addListener((obs, oldVal, newVal) -> animate(newVal));
    }

    private void animate(boolean active) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(200), knob);

        // Coordenadas
        double leftPos = - (WIDTH / 2) + (KNOB_WIDTH / 2);  // Tapa izquierda
        double rightPos = (WIDTH / 2) - (KNOB_WIDTH / 2);   // Tapa derecha

        if (active) {
            // MOVER A DERECHA -> Tapa el "NO", revela el "SÍ"
            tt.setToX(rightPos);
            knobShape.setFill(COLOR_KNOB_ON); // Se pone color Café
        } else {
            // MOVER A IZQUIERDA -> Tapa el "SÍ", revela el "NO"
            tt.setToX(leftPos);
            knobShape.setFill(COLOR_KNOB_OFF); // Se pone color Beige
        }
        tt.play();
    }

    // Getters y Setters necesarios para JavaFX
    public boolean isSelected() { return selected.get(); }
    public void setSelected(boolean val) { selected.set(val); }
    public BooleanProperty selectedProperty() { return selected; }

    // Para guardar metadata (ID del permiso)
    private Object userData;
    public void setUserData(Object data) { this.userData = data; }
    public Object getUserData() { return this.userData; }
}