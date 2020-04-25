package control;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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

public class MenuWindow implements Initializable {
    private Scene scene;

    @FXML
    Button buttonSalir;
    @FXML
    Button buttonIniciar;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
    public void setScene(Scene sc){
        scene=sc;
    }

    public void changeScreenButtonPushed(ActionEvent actionEvent) {

        if(actionEvent.getSource() == buttonIniciar){
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("fxml/gameWindow.fxml"));
                Parent gameParent = loader.load();
                Scene gameScene = new Scene(gameParent);

                Stage gameWindow = (Stage) scene.getWindow();

                GameWindow window = loader.getController();
                window.setScene(gameScene);

                gameWindow.setScene(gameScene);
                gameWindow.show();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if(actionEvent.getSource() == buttonSalir){
            exit();
        }
    }
}
