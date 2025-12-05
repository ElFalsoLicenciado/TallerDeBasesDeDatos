package org.example.Controller.MainView;

import org.example.Model.DAO.DashboardDAO;
import org.example.Model.Entities.Usuario;
import org.example.Util.Navigation;
import org.example.Util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HomeController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblVentasDia;
    @FXML private Label lblStockBajo;

    // Botones de Acceso Rápido
    @FXML private Button btnQuickVentas;
    @FXML private Button btnQuickCompras;
    @FXML private Button btnQuickProduccion;
    @FXML private Button btnQuickInventario;
    @FXML private Button btnQuickRH;
    @FXML private Button btnQuickLogs;

    @FXML
    public void initialize() {
        cargarDatos();
        aplicarSeguridad();
    }

    private void aplicarSeguridad() {
        SessionManager sesion = SessionManager.getInstance();

        // Ocultar botones según permisos (Misma lógica que Sidebar)

        // 1. VENTAS
        if (!sesion.tienePermiso("ventas.crear")) {
            ocultarBoton(btnQuickVentas);
        }

        // 2. COMPRAS
        if (!sesion.tienePermiso("inventario.ajustar")) { // O permiso específico de compras
            ocultarBoton(btnQuickCompras);
        }

        // 3. PRODUCCIÓN
        if (!sesion.tienePermiso("produccion.crear")) {
            ocultarBoton(btnQuickProduccion);
        }

        // 4. INVENTARIO
        if (!sesion.tienePermiso("inventario.ajustar") && !sesion.tienePermiso("inventario.merma")) {
            ocultarBoton(btnQuickInventario);
        }

        // 5. RH
        if (!sesion.tienePermiso("usuarios.ver")) {
            ocultarBoton(btnQuickRH);
        }

        // 6. LOGS (Solo Admin)
        if (!sesion.tienePermiso("sys.full_access")) {
            ocultarBoton(btnQuickLogs);
        }
    }

    private void ocultarBoton(Button btn) {
        if (btn != null) {
            btn.setVisible(false);
            btn.setManaged(false); // IMPORTANTE: Para que no ocupe espacio en el Grid
        }
    }

    @FXML
    public void cargarDatos() {
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario == null) return;

        DashboardDAO dao = new DashboardDAO();
        int idSucursal = usuario.getIdSucursal();

        // 1. Configurar Mensaje de Bienvenida
        if (idSucursal == 0) {
            lblBienvenida.setText("Bienvenido a Lua's Place");
            lblRol.setText("Vista Global Corporativa");
        } else {
            String nombreSucursal = dao.obtenerNombreSucursal(idSucursal);
            lblBienvenida.setText("Bienvenido a " + nombreSucursal);
            lblRol.setText("Panel de Operaciones");
        }

        // 2. Cargar Métricas
        double ventas = dao.obtenerVentasDelDia(idSucursal);
        int stockBajo = dao.obtenerConteoStockBajo(idSucursal);

        lblVentasDia.setText(String.format("$%,.2f", ventas));
        lblStockBajo.setText(String.valueOf(stockBajo));
    }

    // --- MÉTODOS DE NAVEGACIÓN ---
    // Reutilizamos la clase Navigation para mantener consistencia
    @FXML public void irAVentas() { Navigation.cambiarVista("/View/Ventas/PantallaVentas.fxml"); }
    @FXML public void irACompras() { Navigation.cambiarVista("/View/Compras/PantallaCompras.fxml"); }
    @FXML public void irAProduccion() { Navigation.cambiarVista("/View/Produccion/PantallaProduccion.fxml"); }
    @FXML public void irAInventario() { Navigation.cambiarVista("/View/Inventario/PantallaInventario.fxml"); }
    @FXML public void irARH() { Navigation.cambiarVista("/View/RH/RH_Menu.fxml"); }
    @FXML public void irALogs() { Navigation.cambiarVista("/View/Admin/PantallaServerLog.fxml"); }
}