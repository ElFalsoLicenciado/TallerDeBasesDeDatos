package Controller.Inventario;

import Model.DAO.InventarioDAO;
import Model.Entities.InventarioItem;
import Model.Entities.Usuario;
import Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class InventarioController {

    @FXML private TableView<InventarioItem> tablaInventario;
    @FXML private TableColumn<InventarioItem, String> colCodigo;
    @FXML private TableColumn<InventarioItem, String> colProducto;
    @FXML private TableColumn<InventarioItem, String> colTipo;
    @FXML private TableColumn<InventarioItem, Double> colPrecio;
    @FXML private TableColumn<InventarioItem, Integer> colStock;
    @FXML private TableColumn<InventarioItem, Integer> colMinimo;
    @FXML private TableColumn<InventarioItem, String> colEstado;

    private ObservableList<InventarioItem> listaInventario = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarColoresFilas(); // <--- Aquí ocurre la magia visual
        cargarInventario();
    }

    private void configurarColumnas() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colMinimo.setCellValueFactory(new PropertyValueFactory<>("minimo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaInventario.setItems(listaInventario);
    }

    private void configurarColoresFilas() {
        tablaInventario.setRowFactory(tv -> new TableRow<InventarioItem>() {
            @Override
            protected void updateItem(InventarioItem item, boolean empty) {
                super.updateItem(item, empty);

                // Limpiar estilos previos
                getStyleClass().removeAll("fila-agotado", "fila-critico", "fila-bajo", "fila-adecuado");

                if (item == null || empty) {
                    return;
                }

                // Aplicar estilo según el estado que viene de la BD
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
    public void cargarInventario() {
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario != null) {
            InventarioDAO dao = new InventarioDAO();
            var datos = dao.obtenerInventarioPorSucursal(usuario.getIdSucursal());
            listaInventario.setAll(datos);
        }
    }
}