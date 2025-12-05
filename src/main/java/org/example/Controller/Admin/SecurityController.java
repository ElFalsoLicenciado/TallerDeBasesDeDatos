package org.example.Controller.Admin;

import org.example.Model.DAO.SecurityDAO;
import org.example.Model.Entities.Permiso;
import org.example.Model.Entities.Rol;
import org.example.View.Components.CoffeeToggle; // IMPORTANTE: Tu nuevo componente

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox; // Usado para agrupar Toggle + Texto
import javafx.scene.layout.VBox;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.geometry.Pos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityController {

    @FXML private VBox rolesContainer;
    @FXML private FlowPane permissionsContainer;
    @FXML private Label lblSelectedRole;
    @FXML private Label lblFeedback;

    private SecurityDAO dao = new SecurityDAO();
    private List<Permiso> allPermissions;
    private Rol currentRole;

    // CAMBIO: Ahora el mapa guarda CoffeeToggle en lugar de CheckBox
    private Map<Integer, CoffeeToggle> permissionToggles = new HashMap<>();

    @FXML
    public void initialize() {
        allPermissions = dao.getAllPermissions();
        loadRoles();
    }

    private void loadRoles() {
        List<Rol> roles = dao.getAllRoles();
        rolesContainer.getChildren().clear();

        for (Rol role : roles) {
            VBox card = new VBox(5);
            card.getStyleClass().add("role-card");
            card.setPrefWidth(250);

            Label name = new Label(role.getNombre());
            name.getStyleClass().add("role-name");
            name.setMouseTransparent(true);

            Label desc = new Label(role.getDescripcion());
            desc.getStyleClass().add("role-desc");
            desc.setMouseTransparent(true);

            card.getChildren().addAll(name, desc);

            card.setOnMouseClicked(e -> selectRole(role, card));

            rolesContainer.getChildren().add(card);
        }
    }

    private void selectRole(Rol role, VBox cardUI) {
        this.currentRole = role;
        lblSelectedRole.setText("Editando permisos: " + role.getNombre());
        lblFeedback.setText("");

        rolesContainer.getChildren().forEach(node -> node.getStyleClass().remove("selected"));
        cardUI.getStyleClass().add("selected");

        loadPermissionsUI(role.getId());
    }

    private void loadPermissionsUI(int roleId) {
        permissionsContainer.getChildren().clear();
        permissionToggles.clear();

        List<Integer> activePerms = dao.getPermissionIdsByRole(roleId);

        for (Permiso p : allPermissions) {
            // 1. Crear el Toggle Personalizado
            CoffeeToggle toggle = new CoffeeToggle();

            // 2. Establecer estado inicial
            boolean isActive = activePerms.contains(p.getId());
            toggle.setSelected(isActive);

            // Hack visual: forzar actualización gráfica inmediata si empieza activado
            if (isActive) {
                toggle.setSelected(false);
                toggle.setSelected(true);
            }

            // 3. Crear el contenedor de la fila (Toggle + Texto)
            HBox row = new HBox(15);
            row.setAlignment(Pos.CENTER_LEFT);
            // Asignamos una clase CSS para estilizar el contenedor blanco
            row.getStyleClass().add("permission-row");

            // 4. Etiqueta del permiso
            Label lblDesc = new Label(p.getDescripcion());
            lblDesc.setStyle("-fx-text-fill: #4A332A; -fx-font-size: 14px; -fx-font-weight: bold;");

            row.getChildren().addAll(toggle, lblDesc);

            // 5. Guardar referencia y añadir a la vista
            permissionToggles.put(p.getId(), toggle);
            permissionsContainer.getChildren().add(row);
        }
    }

    @FXML
    private void saveChanges() {
        if (currentRole == null) {
            lblFeedback.setText("⚠ Selecciona un rol primero");
            return;
        }

        List<Integer> selectedIds = new ArrayList<>();

        // Iteramos sobre nuestros CoffeeToggles
        permissionToggles.forEach((id, toggle) -> {
            if (toggle.isSelected()) {
                selectedIds.add(id);
            }
        });

        dao.updateRolePermissions(currentRole.getId(), selectedIds);

        lblFeedback.setText("✔ Permisos actualizados correctamente");
        PauseTransition pause = new PauseTransition(Duration.seconds(3));
        pause.setOnFinished(e -> lblFeedback.setText(""));
        pause.play();
    }
}