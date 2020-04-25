package control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static javafx.application.Platform.exit;

public class MainWindow implements Initializable {
    private Scene scene;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    public void setScene(Scene sc){
        scene=sc;
    }

    public void changeScreenButtonPushed(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/menuWindow.fxml"));
            Parent menuParent = loader.load();
            Scene menuScene = new Scene(menuParent);

            Stage menuWindow = (Stage) scene.getWindow();

            MenuWindow window = loader.getController();
            window.setScene(menuScene);

            menuWindow.setScene(menuScene);
            menuWindow.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
