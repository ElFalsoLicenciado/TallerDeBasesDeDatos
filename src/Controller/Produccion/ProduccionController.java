package Controller.Produccion;

import Model.DAO.ProduccionDAO;
import Model.Entities.Producto;
import Model.Entities.RecetaDetalle;
import Model.Entities.Usuario;
import Util.AlertUtils;
import Util.SessionManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.List;

public class ProduccionController {

    @FXML private ListView<Producto> listaProductos;
    @FXML private Label lblProductoSel;
    @FXML private TextField txtCantidad;
    @FXML private Button btnProducir;

    @FXML private TableView<RecetaDetalle> tablaRequisitos;
    @FXML private TableColumn<RecetaDetalle, String> colIngrediente;
    @FXML private TableColumn<RecetaDetalle, String> colRequerido;
    @FXML private TableColumn<RecetaDetalle, String> colDisponible;
    @FXML private TableColumn<RecetaDetalle, String> colEstado;

    private ProduccionDAO dao;
    private ObservableList<RecetaDetalle> listaDetalles;
    private Producto productoSeleccionado;

    @FXML
    public void initialize() {
        dao = new ProduccionDAO();
        listaDetalles = FXCollections.observableArrayList();

        // Configuración inicial segura
        txtCantidad.setText("1");
        configurarTabla();

        // Cargar productos
        List<Producto> productos = dao.listarProductosConReceta();
        listaProductos.getItems().setAll(productos);

        // Listeners
        listaProductos.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) seleccionarProducto(newVal);
        });

        txtCantidad.textProperty().addListener((obs, oldVal, newVal) -> calcularRequisitos());
    }

    private void configurarTabla() {
        // Usamos Lambdas para asegurar que los datos se lean correctamente
        colIngrediente.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getIngrediente()));

        colRequerido.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.2f %s", cell.getValue().getCantidadRequeridaTotal(), cell.getValue().getUnidad())));

        colDisponible.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.2f %s", cell.getValue().getStockDisponible(), cell.getValue().getUnidad())));

        colEstado.setCellValueFactory(cell -> {
            boolean ok = cell.getValue().esSuficiente();
            return new SimpleStringProperty(ok ? "OK" : "FALTA");
        });

        // Colores de fila
        tablaRequisitos.setRowFactory(tv -> new TableRow<RecetaDetalle>() {
            @Override
            protected void updateItem(RecetaDetalle item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (!item.esSuficiente()) {
                    setStyle("-fx-background-color: #ffcccc;"); // Rojo claro
                } else {
                    setStyle("-fx-background-color: #d4edda;"); // Verde claro
                }
            }
        });

        tablaRequisitos.setItems(listaDetalles);
    }

    private void seleccionarProducto(Producto p) {
        productoSeleccionado = p;
        lblProductoSel.setText(p.getNombre());
        System.out.println("Producto seleccionado: " + p.getNombre() + " (ID: " + p.getId() + ")");
        calcularRequisitos();
    }

    @FXML
    public void calcularRequisitos() {
        if (productoSeleccionado == null) return;

        double cantidad = 1;
        try {
            // Si el campo está vacío o inválido, asumimos 1 para mostrar la receta base
            // en lugar de dejar la tabla vacía o dar error.
            if (!txtCantidad.getText().trim().isEmpty()) {
                cantidad = Double.parseDouble(txtCantidad.getText());
            }
            if (cantidad <= 0) cantidad = 1;
        } catch (NumberFormatException e) {
            cantidad = 1;
        }

        // Consultar BD
        List<RecetaDetalle> detalles = dao.obtenerDetallesRecetaGlobal(productoSeleccionado.getId());

        System.out.println("Ingredientes encontrados: " + detalles.size()); // DEBUG

        boolean posible = !detalles.isEmpty();

        for (RecetaDetalle det : detalles) {
            det.calcularTotal(cantidad);
            if (!det.esSuficiente()) posible = false;
        }

        listaDetalles.setAll(detalles);
        btnProducir.setDisable(!posible);
    }

    @FXML
    public void confirmarProduccion() {
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText());
            Usuario u = SessionManager.getInstance().getUsuarioActual();

            if (dao.registrarProduccion(productoSeleccionado.getId(), cantidad, u.getId(), u.getIdSucursal())) {
                AlertUtils.mostrar(Alert.AlertType.INFORMATION, "Producción Exitosa",
                        "Se han generado " + cantidad + " unidades de " + productoSeleccionado.getNombre() + ".");
                calcularRequisitos(); // Recargar para ver el stock bajar visualmente
            } else {
                AlertUtils.mostrar(Alert.AlertType.ERROR, "Error", "No se pudo registrar la producción.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}