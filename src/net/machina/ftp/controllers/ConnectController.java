package net.machina.ftp.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.machina.ftp.network.FTPMiddleman;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConnectController extends BaseController {

    @FXML TextField txtAddress;
    @FXML TextField txtPort;
    @FXML TextField txtUsername;
    @FXML PasswordField txtPassword;
    @FXML Button btnConnect;
    private FTPMiddleman middleman;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.err.println(location);
        middleman = FTPMiddleman.getInstance();
        txtPort.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtPort.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    public void handleConnect(ActionEvent event) {
        boolean loggedin = middleman.connect(
                txtAddress.getText(),
                Integer.parseInt(txtPort.getText()),
                txtUsername.getText(),
                txtPassword.getText()
        );
        if(loggedin) {
            try {
                Stage stage = (Stage) ((Node)event.getSource()).getScene().getWindow();
                Parent newRoot = FXMLLoader.load(getClass().getResource("/res/layout/mainview.fxml"));
                stage.setScene(new Scene(newRoot));
                stage.setResizable(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Error while logging in");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd");
            alert.setHeaderText(null);
            alert.setContentText("Nie udało się zalogować. Sprawdź dane i spróbuj ponownie.");
            alert.show();
        }
    }
}