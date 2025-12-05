package org.example.Util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;

public class Navigation {

    // Referencia estática al contenedor principal (el BorderPane de MainLayout.fxml)
    private static BorderPane mainLayout;

    /**
     * Este método se llama desde MainLayoutController.initialize()
     * para "registrar" el contenedor principal.
     */
    public static void setMainLayout(BorderPane layout) {
        mainLayout = layout;
    }

    /**
     * Intenta cargar un FXML y ponerlo en el centro.
     * Si el archivo no existe, muestra un mensaje de "En construcción".
     * @param fxmlPath La ruta del archivo (ej: "/View/Ventas/PantallaVentas.fxml")
     */
    public static void cambiarVista(String fxmlPath) {
        // Validación de seguridad
        if (mainLayout == null) {
            System.err.println("ERROR: El MainLayout no ha sido inicializado.");
            return;
        }

        try {
            // 1. Intentamos obtener la URL del archivo
            URL resource = Navigation.class.getResource(fxmlPath);

            // 2. Si el archivo NO existe, mostramos un placeholder (útil para prototipar)
            if (resource == null) {
                mostrarPlaceholder("Vista no encontrada:\n" + fxmlPath);
                return;
            }

            // 3. Si existe, lo cargamos y lo ponemos en el centro
            Parent vista = FXMLLoader.load(resource);
            mainLayout.setCenter(vista);

        } catch (IOException e) {
            // Si hay un error en el código del FXML, lo mostramos en consola
            e.printStackTrace();
            mostrarPlaceholder("Error al cargar la vista:\n" + e.getMessage());
        }
    }

    /**
     * Método auxiliar para mostrar un mensaje temporal en el centro
     * cuando la vista real no está lista.
     */
    private static void mostrarPlaceholder(String mensaje) {
        Label label = new Label(mensaje);
        label.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d; -fx-text-alignment: CENTER;");

        StackPane placeholder = new StackPane(label);
        placeholder.setStyle("-fx-background-color: #ecf0f1;");

        mainLayout.setCenter(placeholder);
    }
}