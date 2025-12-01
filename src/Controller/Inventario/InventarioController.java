package Controller.Inventario;

import Model.DAO.InventarioDAO;
import Model.DAO.RecetaDAO;
import Model.Entities.InventarioItem;
import Model.Entities.Usuario;
import Util.AlertUtils;
import Util.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;

import java.io.InputStream;
import java.util.List;

public class InventarioController {

    @FXML private FlowPane gridProductos;

    @FXML
    public void initialize() {
        cargarInventario();
    }

    @FXML
    public void cargarInventario() {
        gridProductos.getChildren().clear(); // Limpiar grid anterior

        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario != null) {
            InventarioDAO dao = new InventarioDAO();
            List<InventarioItem> items = dao.obtenerInventarioPorSucursal(usuario.getIdSucursal());

            // Crear tarjeta por cada producto
            for (InventarioItem item : items) {
                // Solo mostramos productos (tipo pan, bebida, postre), no ingredientes sueltos
                // Opcional: Filtro en Java o en SQL
                if (!item.getTipo().equals("Ingrediente")) {
                    gridProductos.getChildren().add(crearTarjetaProducto(item));
                }
            }
        }
    }

    private VBox crearTarjetaProducto(InventarioItem item) {
        // 1. Contenedor de la Tarjeta
        VBox card = new VBox();
        card.setPrefSize(200, 280); // Tamaño fijo
        card.getStyleClass().add("producto-card");

        // 2. Imagen
        ImageView imgView = new ImageView();
        imgView.setFitHeight(120);
        imgView.setFitWidth(120);
        imgView.setPreserveRatio(true);

        // Intentar cargar imagen específica, sino usar default
        String imagePath = "/View/Images/" + item.getCodigo() + ".png";
        InputStream is = getClass().getResourceAsStream(imagePath);
        if (is == null) {
            is = getClass().getResourceAsStream("/View/Images/default.png"); // IMAGEN POR DEFECTO
        }
        if (is != null) imgView.setImage(new Image(is));

        // 3. Nombre
        Label lblNombre = new Label(item.getProducto());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-alignment: CENTER;");
        lblNombre.setWrapText(true);

        // 4. Cantidad y Estado
        Label lblStock = new Label(item.getCantidad() + " unidades");
        lblStock.getStyleClass().add("texto-cantidad");

        // Colores según estado
        switch (item.getEstado()) {
            case "AGOTADO": lblStock.getStyleClass().add("stock-critico"); break;
            case "CRÍTICO": lblStock.getStyleClass().add("stock-critico"); break;
            case "BAJO":    lblStock.getStyleClass().add("stock-bajo"); break;
            default:        lblStock.getStyleClass().add("stock-ok"); break;
        }

        // 5. Botón Ver Receta
        Button btnReceta = new Button("Ver Receta");
        btnReceta.getStyleClass().add("boton-latte");
        btnReceta.setStyle("-fx-font-size: 12px; -fx-padding: 5 15;");
        btnReceta.setOnAction(e -> mostrarReceta(item));

        // Espaciador para empujar botón abajo
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        // Armar tarjeta
        card.getChildren().addAll(imgView, lblNombre, lblStock, spacer, btnReceta);
        return card;
    }

    private void mostrarReceta(InventarioItem item) {
        // Ejecutar en hilo de fondo si la consulta es pesada, pero aquí es rápida
        RecetaDAO recetaDao = new RecetaDAO();
        String contenidoReceta = recetaDao.obtenerRecetaPorProducto(item.getCodigo());

        // Usamos un Alert personalizado con TextArea para mostrar la receta
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receta: " + item.getProducto());
        alert.setHeaderText(null);

        // Usar TextArea para que el texto largo se lea bien
        TextArea textArea = new TextArea(contenidoReceta);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        // Estilizar el diálogo usando tu utilidad (si quieres)
        // O hacerlo manual aquí para el TextArea
        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(400, 400);

        // Inyectar CSS global
        try {
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/View/CSS/Style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("dialog-pane");
        } catch (Exception ignored) {}

        alert.showAndWait();
    }
}