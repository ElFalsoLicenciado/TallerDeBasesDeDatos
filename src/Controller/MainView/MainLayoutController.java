package Controller.MainView;
import Util.Navigation;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainLayoutController {
    @FXML private BorderPane mainContainer;
    @FXML
    public void initialize() {
        Util.Navigation.setMainLayout(mainContainer);
        Util.Navigation.cambiarVista("/View/Home.fxml");
    }
}