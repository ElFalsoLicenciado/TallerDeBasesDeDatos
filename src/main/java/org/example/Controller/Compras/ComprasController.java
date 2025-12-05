package org.example.Controller.Compras;

import org.example.Model.DAO.CompraDAO;
import org.example.Model.DAO.IngredienteDAO;
import org.example.Model.DAO.ProveedorDAO;
import org.example.Model.Entities.Ingrediente;
import org.example.Model.Entities.Proveedor;
import org.example.Util.AlertUtils;
import org.example.Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComprasController {

    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private ComboBox<Ingrediente> cmbIngrediente;
    @FXML private TextField txtCantidad;
    @FXML private Label lblUnidad, lblTotalCompra;
    @FXML private TableView<RenglonCompra> tablaDetalle;

    @FXML private TableColumn<RenglonCompra, String> colIngrediente;
    @FXML private TableColumn<RenglonCompra, Double> colCantidad;
    @FXML private TableColumn<RenglonCompra, Double> colCosto;
    @FXML private TableColumn<RenglonCompra, Double> colTotal;

    private ObservableList<RenglonCompra> listaDetalle = FXCollections.observableArrayList();
    private Map<Ingrediente, Double> mapaCarrito = new HashMap<>();

    // Para controlar si estamos cambiando de proveedor con items en el carrito
    private boolean ignorarListener = false;

    @FXML
    public void initialize() {
        cargarProveedores();
        configurarTabla();

        // Estado inicial: Ingredientes deshabilitados hasta elegir proveedor
        cmbIngrediente.setDisable(true);

        // LISTENER DE PROVEEDOR (Lógica Principal)
        cmbProveedor.getSelectionModel().selectedItemProperty().addListener((obs, viejoProv, nuevoProv) -> {
            if (ignorarListener) return;

            // Si ya hay cosas en el carrito y cambiamos de proveedor...
            if (!mapaCarrito.isEmpty() && viejoProv != null && nuevoProv != null) {
                // ...preguntar al usuario
                Optional<ButtonType> respuesta = AlertUtils.confirmar(
                        "Cambiar Proveedor",
                        "Se vaciará el carrito actual.",
                        "Una orden de compra solo puede ser para un único proveedor. ¿Desea continuar?",
                        ButtonType.YES, ButtonType.CANCEL
                );

                if (respuesta.isPresent() && respuesta.get() == ButtonType.CANCEL) {
                    // Si cancela, volvemos al proveedor anterior sin disparar eventos
                    ignorarListener = true;
                    cmbProveedor.setValue(viejoProv);
                    ignorarListener = false;
                    return;
                } else {
                    // Si acepta, limpiamos el carrito
                    limpiarCarritoInterno();
                }
            }

            // Cargar ingredientes del nuevo proveedor
            if (nuevoProv != null) {
                cargarIngredientes(nuevoProv.getId());
                cmbIngrediente.setDisable(false);
            } else {
                cmbIngrediente.getItems().clear();
                cmbIngrediente.setDisable(true);
            }
        });

        // Listener para unidad de medida
        cmbIngrediente.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) lblUnidad.setText(newVal.getUnidad());
            else lblUnidad.setText("unidades");
        });
    }

    private void cargarProveedores() {
        cmbProveedor.getItems().setAll(new ProveedorDAO().listarTodos());
    }

    private void cargarIngredientes(int idProveedor) {
        IngredienteDAO dao = new IngredienteDAO();
        // Usamos el NUEVO método filtrado
        cmbIngrediente.getItems().setAll(dao.listarPorProveedor(idProveedor));
    }

    private void configurarTabla() {
        colIngrediente.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colCosto.setCellValueFactory(new PropertyValueFactory<>("costoUnitario"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        colCosto.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("$%.2f", item));
            }
        });
        colTotal.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("$%.2f", item));
            }
        });

        tablaDetalle.setItems(listaDetalle);
    }

    @FXML
    public void agregar() {
        Ingrediente ing = cmbIngrediente.getValue();
        if (ing == null || txtCantidad.getText().isEmpty()) {
            AlertUtils.mostrar(Alert.AlertType.WARNING, "Datos faltantes", "Seleccione ingrediente y cantidad.");
            return;
        }

        try {
            double cant = Double.parseDouble(txtCantidad.getText());
            if (cant <= 0) throw new NumberFormatException();

            mapaCarrito.put(ing, mapaCarrito.getOrDefault(ing, 0.0) + cant);
            actualizarTabla();
            txtCantidad.clear();
            cmbIngrediente.requestFocus();

        } catch (NumberFormatException e) {
            AlertUtils.mostrar(Alert.AlertType.ERROR, "Error", "La cantidad debe ser un número válido mayor a 0.");
        }
    }

    private void actualizarTabla() {
        listaDetalle.clear();
        double granTotal = 0;

        for (Map.Entry<Ingrediente, Double> entry : mapaCarrito.entrySet()) {
            RenglonCompra row = new RenglonCompra(entry.getKey(), entry.getValue());
            listaDetalle.add(row);
            granTotal += row.getTotal();
        }
        lblTotalCompra.setText(String.format("$%.2f", granTotal));
    }

    @FXML
    public void confirmarCompra() {
        if (cmbProveedor.getValue() == null) {
            AlertUtils.mostrar(Alert.AlertType.WARNING, "Atención", "Debe seleccionar un proveedor.");
            return;
        }
        if (mapaCarrito.isEmpty()) {
            AlertUtils.mostrar(Alert.AlertType.WARNING, "Atención", "El pedido está vacío.");
            return;
        }

        double total = listaDetalle.stream().mapToDouble(RenglonCompra::getTotal).sum();
        int idUser = SessionManager.getInstance().getUsuarioActual().getId();

        CompraDAO dao = new CompraDAO();
        if (dao.registrarCompra(idUser, cmbProveedor.getValue().getId(), total, mapaCarrito)) {
            AlertUtils.mostrar(Alert.AlertType.INFORMATION, "Éxito", "Compra registrada. El inventario ha sido actualizado.");
            limpiarCompleto();
        } else {
            AlertUtils.mostrar(Alert.AlertType.ERROR, "Error", "No se pudo registrar la compra.");
        }
    }

    @FXML
    public void limpiar() {
        limpiarCompleto();
    }

    // Limpia todo (Botón Limpiar o tras compra exitosa)
    private void limpiarCompleto() {
        ignorarListener = true; // Evitar disparar el listener al limpiar el proveedor
        cmbProveedor.getSelectionModel().clearSelection();
        ignorarListener = false;

        cmbIngrediente.getItems().clear();
        cmbIngrediente.setDisable(true);
        limpiarCarritoInterno();
    }

    // Limpia solo la parte derecha (al cambiar de proveedor)
    private void limpiarCarritoInterno() {
        mapaCarrito.clear();
        listaDetalle.clear();
        lblTotalCompra.setText("$0.00");
        txtCantidad.clear();
        lblUnidad.setText("unidades");
    }

    // Clase interna para la tabla
    public static class RenglonCompra {
        private Ingrediente ing;
        private double cantidad;

        public RenglonCompra(Ingrediente ing, double cantidad) { this.ing = ing; this.cantidad = cantidad; }

        public String getNombre() { return ing.getNombre(); }
        public double getCantidad() { return cantidad; }
        public double getCostoUnitario() { return ing.getCosto(); }
        public double getTotal() { return cantidad * ing.getCosto(); }
    }
}