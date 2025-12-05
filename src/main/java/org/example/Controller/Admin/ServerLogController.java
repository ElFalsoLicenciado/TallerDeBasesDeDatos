package org.example.Controller.Admin;

import org.example.Model.DAO.ServerLogDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServerLogController {

    @FXML private TextArea txtLog;

    @FXML
    public void initialize() {
        cargarLog();
    }

    @FXML
    public void cargarLog() {
        ServerLogDAO dao = new ServerLogDAO();
        String contenido = dao.obtenerLogDelServidor();

        txtLog.setText(contenido);

        // Hacer scroll hasta el final (donde está lo más reciente)
        txtLog.setScrollTop(Double.MAX_VALUE);
        txtLog.positionCaret(txtLog.getText().length());
    }
}