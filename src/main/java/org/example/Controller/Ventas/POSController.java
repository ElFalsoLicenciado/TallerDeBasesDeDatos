package org.example.Controller.Ventas;

import org.example.Model.DAO.ClienteDAO;
import org.example.Model.DAO.ProductoDAO;
import org.example.Model.DAO.VentaDAO;
import org.example.Model.Entities.Cliente;
import org.example.Model.Entities.Producto;
import org.example.Model.Entities.Usuario;
import org.example.Util.AlertUtils;
import org.example.Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.Map;

public class POSController {

    @FXML private TextField campoBusqueda;
    @FXML private ComboBox<Cliente> cmbClientes;
    @FXML private TableView<Producto> tablaProductos;
    @FXML private TableView<RenglonCarrito> tablaCarrito;

    // Columnas inyectadas
    @FXML private TableColumn<Producto, String> colProdNombre;
    @FXML private TableColumn<Producto, Double> colProdPrecio;
    @FXML private TableColumn<Producto, Integer> colProdStock;
    @FXML private TableColumn<RenglonCarrito, String> colCarrProducto;
    @FXML private TableColumn<RenglonCarrito, Integer> colCarrCantidad;
    @FXML private TableColumn<RenglonCarrito, Double> colCarrTotal;

    @FXML private Label lblSubtotal, lblImpuestos, lblTotal;
    @FXML private Button btnCobrar;

    private ObservableList<Producto> listaCatalogo = FXCollections.observableArrayList();
    private ObservableList<RenglonCarrito> listaCarrito = FXCollections.observableArrayList();
    private Map<Producto, Integer> mapaCarrito = new HashMap<>();

    @FXML
    public void initialize() {
        colProdNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProdPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colProdStock.setCellValueFactory(new PropertyValueFactory<>("existencia"));

        colCarrProducto.setCellValueFactory(new PropertyValueFactory<>("nombreProducto"));
        colCarrCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCarrTotal.setCellValueFactory(new PropertyValueFactory<>("importe"));

        tablaProductos.setItems(listaCatalogo);
        tablaCarrito.setItems(listaCarrito);

        cargarClientes();
        buscarProducto();
    }

    private void cargarClientes() {
        ClienteDAO dao = new ClienteDAO();
        var clientes = dao.listarClientes();
        Cliente publicoGeneral = new Cliente(0, "Público", "General", "");
        cmbClientes.getItems().add(publicoGeneral);
        cmbClientes.getItems().addAll(clientes);
        cmbClientes.getSelectionModel().selectFirst();
    }

    @FXML
    public void buscarProducto() {
        String query = campoBusqueda.getText();
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();

        if (usuario != null) {
            ProductoDAO dao = new ProductoDAO();
            var productos = dao.buscarPorNombreOCodigo(query, usuario.getIdSucursal());
            listaCatalogo.setAll(productos);
        }
    }

    @FXML
    public void agregarAlCarrito() {
        Producto seleccionado = tablaProductos.getSelectionModel().getSelectedItem();
        if (seleccionado == null || seleccionado.getExistencia() <= 0) return;

        int cantActual = mapaCarrito.getOrDefault(seleccionado, 0);
        if (cantActual >= seleccionado.getExistencia()) {
            mostrarAlerta("Stock Límite", "No puedes agregar más de lo que hay en inventario.");
            return;
        }
        mapaCarrito.put(seleccionado, cantActual + 1);
        actualizarTablaCarrito();
        calcularTotales();
    }

    private void actualizarTablaCarrito() {
        listaCarrito.clear();
        for (Map.Entry<Producto, Integer> entry : mapaCarrito.entrySet()) {
            listaCarrito.add(new RenglonCarrito(entry.getKey(), entry.getValue()));
        }
        tablaCarrito.refresh();
    }

    private void calcularTotales() {
        double total = listaCarrito.stream().mapToDouble(RenglonCarrito::getImporte).sum();
        lblTotal.setText(String.format("$%.2f", total));
        lblSubtotal.setText(String.format("$%.2f", total / 1.16));
        lblImpuestos.setText(String.format("$%.2f", total - (total / 1.16)));
    }

    @FXML
    public void cancelarVenta() {
        mapaCarrito.clear();
        listaCarrito.clear();
        calcularTotales();
        campoBusqueda.clear();
        cmbClientes.getSelectionModel().selectFirst();
    }

    @FXML
    public void realizarCobro() {
        if (mapaCarrito.isEmpty()) return;

        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        double total = listaCarrito.stream().mapToDouble(RenglonCarrito::getImporte).sum();

        Cliente clienteSeleccionado = cmbClientes.getSelectionModel().getSelectedItem();
        Integer idCliente = (clienteSeleccionado != null && clienteSeleccionado.getId() != 0)
                ? clienteSeleccionado.getId() : null;

        VentaDAO dao = new VentaDAO();
        boolean exito = dao.registrarVenta(usuario.getId(), usuario.getIdSucursal(), total, mapaCarrito, idCliente);

        if (exito) {
            mostrarAlerta("Venta Exitosa", "La venta se registró correctamente.");
            cancelarVenta();
            buscarProducto();
        } else {
            mostrarAlerta("Error", "No se pudo registrar la venta.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        AlertUtils.mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    public static class RenglonCarrito {
        private Producto p;
        private int cantidad;

        public RenglonCarrito(Producto p, int cantidad) {
            this.p = p;
            this.cantidad = cantidad;
        }
        public String getNombreProducto() { return p.getNombre(); }
        public int getCantidad() { return cantidad; }
        public double getImporte() { return p.getPrecio() * cantidad; }
    }
}