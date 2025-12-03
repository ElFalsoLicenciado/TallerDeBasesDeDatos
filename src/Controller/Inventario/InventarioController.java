package Controller.Inventario;

import Model.DAO.InventarioDAO;
import Model.DAO.RecetaDAO;
import Model.Entities.InventarioItem;
import Model.Entities.Usuario;
import Util.AlertUtils;
import Util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.InputStream;
import java.util.List;

public class InventarioController {

    @FXML private FlowPane gridProductos;
    @FXML private ScrollPane viewProductos;

    @FXML private TableView<InventarioItem> tablaInsumos;
    @FXML private TableColumn<InventarioItem, String> colCodigo;
    @FXML private TableColumn<InventarioItem, String> colNombre;
    @FXML private TableColumn<InventarioItem, Double> colCosto;
    @FXML private TableColumn<InventarioItem, Integer> colStock;
    @FXML private TableColumn<InventarioItem, String> colEstado;

    @FXML private Label lblTituloInv;
    @FXML private ToggleButton tglProductos;

    @FXML
    public void initialize() {
        // Configurar columnas de la tabla de Insumos
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("producto")); // Usa el getter getProducto()
        colCosto.setCellValueFactory(new PropertyValueFactory<>("precio"));    // Usa el getter getPrecio() (costo)
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        // Configurar los colores de alerta
        configurarColoresFilas();

        // Cargar datos iniciales
        cargarInventario();
    }

    @FXML
    public void cambiarVista() {
        if (tglProductos.isSelected()) {
            // Mostrar Productos (Grid)
            viewProductos.setVisible(true);
            tablaInsumos.setVisible(false);
            lblTituloInv.setText("Inventario de Productos");
        } else {
            // Mostrar Insumos (Tabla)
            viewProductos.setVisible(false);
            tablaInsumos.setVisible(true);
            lblTituloInv.setText("Inventario de Materia Prima");
        }
        cargarInventario(); // Recargar datos
    }

    @FXML
    public void cargarInventario() {
        InventarioDAO dao = new InventarioDAO();

        if (tglProductos.isSelected()) {
            // --- CARGA DE PRODUCTOS (GRID) ---
            gridProductos.getChildren().clear();
            Usuario usuario = SessionManager.getInstance().getUsuarioActual();

            if (usuario != null) {
                // Obtener inventario específico de la sucursal del usuario
                List<InventarioItem> items = dao.obtenerInventarioPorSucursal(usuario.getIdSucursal());

                for (InventarioItem item : items) {
                    // Filtrar solo productos (no ingredientes sueltos)
                    // Asumimos que los productos tienen foto o son de tipo específico
                    if (!item.getTipo().equals("Ingrediente")) {
                        gridProductos.getChildren().add(crearTarjetaProducto(item));
                    }
                }
            }
        } else {
            // --- CARGA DE INSUMOS (TABLA) ---
            List<InventarioItem> insumos = dao.obtenerIngredientesGlobales();
            tablaInsumos.getItems().setAll(insumos);
        }
    }

    // --- MÉTODOS VISUALES ---

    private VBox crearTarjetaProducto(InventarioItem item) {
        VBox card = new VBox();
        card.setPrefSize(200, 280);
        card.getStyleClass().add("producto-card");

        // Imagen
        ImageView imgView = new ImageView();
        imgView.setFitHeight(120);
        imgView.setFitWidth(120);
        imgView.setPreserveRatio(true);

        String imagePath = "/View/Images/" + item.getCodigo() + ".png";
        InputStream is = getClass().getResourceAsStream(imagePath);
        if (is == null) {
            is = getClass().getResourceAsStream("/View/Images/default.png");
        }
        if (is != null) imgView.setImage(new Image(is));

        // Etiquetas
        Label lblNombre = new Label(item.getProducto());
        lblNombre.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-alignment: CENTER;");
        lblNombre.setWrapText(true);

        Label lblStock = new Label(item.getCantidad() + " unidades");
        lblStock.getStyleClass().add("texto-cantidad");

        switch (item.getEstado()) {
            case "AGOTADO": lblStock.getStyleClass().add("stock-critico"); break;
            case "CRÍTICO": lblStock.getStyleClass().add("stock-critico"); break;
            case "BAJO":    lblStock.getStyleClass().add("stock-bajo"); break;
            default:        lblStock.getStyleClass().add("stock-ok"); break;
        }

        // Botón Receta
        Button btnReceta = new Button("Ver Receta");
        btnReceta.getStyleClass().add("boton-latte");
        btnReceta.setStyle("-fx-font-size: 12px; -fx-padding: 5 15;");
        btnReceta.setOnAction(e -> mostrarReceta(item));

        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        card.getChildren().addAll(imgView, lblNombre, lblStock, spacer, btnReceta);
        return card;
    }

    private void mostrarReceta(InventarioItem item) {
        RecetaDAO recetaDao = new RecetaDAO();
        String contenidoReceta = recetaDao.obtenerRecetaPorProducto(item.getCodigo());

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Receta: " + item.getProducto());
        alert.setHeaderText(null);

        TextArea textArea = new TextArea(contenidoReceta);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        alert.getDialogPane().setContent(textArea);
        alert.getDialogPane().setPrefSize(400, 400);

        try {
            alert.getDialogPane().getStylesheets().add(
                    getClass().getResource("/View/CSS/Style.css").toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("dialog-pane");
        } catch (Exception ignored) {}

        alert.showAndWait();
    }

    private void configurarColoresFilas() {
        tablaInsumos.setRowFactory(tv -> new TableRow<InventarioItem>() {
            @Override
            protected void updateItem(InventarioItem item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().removeAll("fila-agotado", "fila-critico", "fila-bajo", "fila-adecuado");

                if (item == null || empty) {
                    return;
                }

                switch (item.getEstado()) {
                    case "AGOTADO":
                        getStyleClass().add("fila-agotado");
                        break;
                    case "CRÍTICO":
                        getStyleClass().add("fila-critico");
                        break;
                    case "BAJO":
                        getStyleClass().add("fila-bajo");
                        break;
                    default:
                        getStyleClass().add("fila-adecuado");
                        break;
                }
            }
        });
    }

    @FXML
    public void abrirNuevoProducto() {
        try {
            // Verificar permisos si lo deseas (ej. solo Admin/Gerente)
            if (!SessionManager.getInstance().tienePermiso("inventario.ajustar")) {
                AlertUtils.mostrar(Alert.AlertType.WARNING, "Acceso Denegado", "No tienes permiso para crear productos.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/View/Inventario/ProductoForm.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Catálogo");
            stage.setResizable(false);
            stage.showAndWait();

            // Al cerrar, recargar para ver el nuevo producto
            cargarInventario();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}