package Controller.MainView;

import Model.DAO.DashboardDAO;
import Model.Entities.Usuario;
import Util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML private Label lblBienvenida;
    @FXML private Label lblRol;
    @FXML private Label lblVentasDia;
    @FXML private Label lblStockBajo;

    @FXML
    public void initialize() {
        cargarDatos();
    }

    @FXML
    public void cargarDatos() {
        Usuario usuario = SessionManager.getInstance().getUsuarioActual();
        if (usuario == null) return;

        DashboardDAO dao = new DashboardDAO();
        int idSucursal = usuario.getIdSucursal();

        // 1. Configurar Mensaje de Bienvenida
        if (idSucursal == 0) {
            // Es Super Admin o Corporativo
            lblBienvenida.setText("Bienvenido a Lua's Place");
            lblRol.setText("Vista Global Corporativa");
        } else {
            // Es personal de sucursal
            String nombreSucursal = dao.obtenerNombreSucursal(idSucursal);
            lblBienvenida.setText("Bienvenido a " + nombreSucursal);
            // Mostramos el rol para contexto (ej. "Gerente Sucursal")
            // (Asumimos que tienes un método para obtener el nombre del rol, o lo dejamos genérico)
            lblRol.setText("Panel de Operaciones");
        }

        // 2. Cargar Métricas
        double ventas = dao.obtenerVentasDelDia(idSucursal);
        int stockBajo = dao.obtenerConteoStockBajo(idSucursal);

        lblVentasDia.setText(String.format("$%,.2f", ventas));
        lblStockBajo.setText(String.valueOf(stockBajo));
    }
}