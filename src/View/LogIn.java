package View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class LogIn extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent logInRoot = FXMLLoader.load(Objects.requireNonNull(LogIn.class.getResource("/View/LogIn.fxml")));
        Scene logInScene = new Scene(logInRoot);
        Stage logInStage = new Stage();

        Image icon = new Image("/View/Images/icon.png");

        logInStage.setScene(logInScene);
        logInStage.setTitle("LogIn");
        logInStage.getIcons().add(icon);
        logInStage.show();
    }
}


/*
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage emisor) throws Exception {
        Parent rootEmisor = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("Vista/EmisorPage.fxml")));
        Scene sceneEmisor = new Scene(rootEmisor);
        Image iconEmisor = new Image("Vista/Iconos/iconEmisor.png");

        Parent rootReceptor = FXMLLoader.load(Objects.requireNonNull(Main.class.getResource("Vista/ReceptorPage.fxml")));
        Scene sceneReceptor = new Scene(rootReceptor);
        Image iconReceptor = new Image("Vista/Iconos/iconReceptor.png");

        Stage receptor = new Stage();
        receptor.setScene(sceneReceptor);
        receptor.getIcons().add(iconReceptor);
        receptor.setTitle("Receptor");
        emisor.setScene(sceneEmisor);
        emisor.getIcons().add(iconEmisor);
        emisor.setTitle("Emisor");

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = bounds.getWidth();
        double emisorWidth = sceneEmisor.getWidth();
        double receptorWidth = sceneReceptor.getWidth();
        double targetCenterX_emisor = screenWidth  / 8.0;
        double targetCenterX_receptor = (screenWidth * 4.0) / 7.0;

        emisor.setX(targetCenterX_emisor - (emisorWidth / 2.0));

        receptor.setX(targetCenterX_receptor - (receptorWidth / 2.0));

        receptor.show();
        emisor.show();
    }
}

 */