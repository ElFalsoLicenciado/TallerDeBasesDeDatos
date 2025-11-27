package Controller;
import Util.Navigation;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainLayoutController {
    @FXML private BorderPane mainContainer;
    @FXML
    public void initialize() {
        Navigation.setMainLayout(mainContainer);
    }
}