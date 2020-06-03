package net.machina.ftp.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.machina.ftp.network.FTPMiddleman;
import net.machina.ftp.network.ObservableFTPFile;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class MainController extends BaseController {

    @FXML TableView<ObservableFTPFile> tableFiles;
    @FXML TextField txtPath;
    @FXML Button btnNewDir;
    @FXML Button btnRename;
    @FXML Button btnRemove;
    @FXML Button btnDownload;
    @FXML Button btnUploadDir;
    @FXML Button btnUploadFile;
    @FXML Button btnLogout;
    @FXML Button btnUp;

    private FTPMiddleman middleman;
    private List<FTPFile> files;
    private ObservableFTPFile selectedFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        middleman = FTPMiddleman.getInstance();
        files = new ArrayList<>();
        txtPath.setEditable(false);
        createTable();
        refreshData();
    }

    public void createTable() {
        tableFiles.setEditable(false);
        tableFiles.setPlaceholder(new Label("Ten katalog jest pusty"));
        tableFiles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<ObservableFTPFile, String> colType = new TableColumn<>("Typ");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colType.setMaxWidth(1f * Integer.MAX_VALUE * 7);
        TableColumn<ObservableFTPFile, String> colPermissions = new TableColumn<>("Uprawnienia");
        colPermissions.setCellValueFactory(new PropertyValueFactory<>("permissions"));
        colPermissions.setMaxWidth(1f * Integer.MAX_VALUE * 13);
        colPermissions.setSortable(false);
        TableColumn<ObservableFTPFile, String> colName = new TableColumn<>("Nazwa pliku");
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colName.setMaxWidth(1f * Integer.MAX_VALUE * 46);
        TableColumn<ObservableFTPFile, String> colSize = new TableColumn<>("Rozmiar");
        colSize.setCellValueFactory(new PropertyValueFactory<>("size"));
        colSize.setMaxWidth(1f * Integer.MAX_VALUE * 14);
        TableColumn<ObservableFTPFile, String> colUOwner = new TableColumn<>("Właściciel");
        colUOwner.setCellValueFactory(new PropertyValueFactory<>("uOwner"));
        colUOwner.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        TableColumn<ObservableFTPFile, String> colGOwner = new TableColumn<>("Grupa");
        colGOwner.setCellValueFactory(new PropertyValueFactory<>("gOwner"));
        colGOwner.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        tableFiles.getColumns().addAll(colType, colPermissions, colName, colSize, colUOwner, colGOwner);

        tableFiles.setRowFactory(tv -> {
            TableRow<ObservableFTPFile> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == MouseButton.PRIMARY) {
                    if (event.getClickCount() == 1) selectedFile = row.getItem();
                    else if (event.getClickCount() == 2 && selectedFile.getType().contains("DIR")) {
                        middleman.goToDirectory(selectedFile.getName());
                        refreshData();
                        selectedFile = null;
                    }
                }
            });
            return row;
        });

        tableFiles.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }

    public void refreshData() {
        if (middleman.isConnected()) {
            txtPath.setText(middleman.getCwd());
            files.clear();
            files.addAll(Arrays.asList(middleman.listFiles()));
            List<ObservableFTPFile> observableFTPFileList = new ArrayList<>();
            for (FTPFile file : files) {
                observableFTPFileList.add(new ObservableFTPFile(file));
            }
            ObservableList<ObservableFTPFile> filesObservable = FXCollections.observableArrayList(observableFTPFileList);
            tableFiles.setItems(filesObservable);
            tableFiles.refresh();
            for (FTPFile file : files) {
                System.err.println(file);
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Nie udało się pobrać danych. Spróbuj ponownie.");
        }
    }

    public void handleUpClicked(ActionEvent actionEvent) {
        if (middleman.isConnected()) {
            middleman.toParent();
            refreshData();
        }
    }


    public void handleNewDir(ActionEvent actionEvent) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nowy katalog");
        dialog.setHeaderText(null);
        dialog.setContentText("Nazwa katalogu do utworzenia:");
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(dir -> {
            if (middleman.isConnected()) {
                if (!middleman.makeDirectory(dir)) {
                    showAlert(Alert.AlertType.WARNING, "Nie udało się utworzyć katalogu.");
                } else {
                    refreshData();
                }
            }
        });
    }

    public void handleRename(ActionEvent actionEvent) {
        if (selectedFile != null) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Zmień nazwę / Przenieś");
            dialog.setHeaderText(null);
            dialog.setContentText("Nowa nazwa / ścieżka:");
            dialog.getEditor().setText(selectedFile.getName());
            Optional<String> result = dialog.showAndWait();

            result.ifPresent(name -> {
                if (middleman.isConnected()) {
                    if (!middleman.rename(selectedFile.getName(), name)) {
                        showAlert(Alert.AlertType.WARNING, "Nie udało się zmienić nazwy lub przenieść pliku/katalogu.");
                    } else {
                        refreshData();
                    }
                }
            });
        }
    }

    public void handleRemove(ActionEvent actionEvent) {
        if (selectedFile != null) {
            final ButtonType btnYes = new ButtonType("Tak", ButtonBar.ButtonData.YES);
            final ButtonType btnNo = new ButtonType("Nie", ButtonBar.ButtonData.NO);
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Usuwanie");
            alert.setHeaderText(null);
            alert.setContentText("Czy na pewno chcesz usunąć " + (selectedFile.getType().contains("DIR") ? "katalog" : "plik")
                    + " o nazwie " + selectedFile.getName() + "?");
            alert.getButtonTypes().clear();
            alert.getButtonTypes().addAll(btnYes, btnNo);
            Optional<ButtonType> result = alert.showAndWait();

            result.ifPresent(buttonType -> {
                if (buttonType == btnYes) {
                    if (middleman.isConnected()) {
                        if (selectedFile.getType().contains("DIR")) {
                            if (!middleman.removeDirectory(selectedFile.getName())) {
                                showAlert(Alert.AlertType.WARNING, "Nie można usunąć katalogu. Sprawdź, czy jest pusty i spróbuj ponownie.");
                            }
                        } else {
                            if (!middleman.removeFile(selectedFile.getName())) {
                                showAlert(Alert.AlertType.WARNING, "Nie można usunąć pliku.");
                            }
                        }
                        refreshData();
                    }
                }
            });
        }
    }

    public void handleDownload(ActionEvent actionEvent) {
        if (middleman.isConnected() && selectedFile != null) {
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedDir = chooser.showDialog(stage);
            if (selectedFile.getType().contains("DIR")) {
                downloadDirectory(selectedFile.getName(), selectedDir.getAbsolutePath());
                refreshData();
            } else {
                if (middleman.downloadFile(selectedFile.getName(), selectedDir.getAbsolutePath() + "/" + selectedFile.getName()))
                    showAlert(Alert.AlertType.INFORMATION, "Pobieranie zakończone");
                else showAlert(Alert.AlertType.WARNING, "Nie udało się pobrać pliku");
            }
        }
    }

    public void handleUploadFile(ActionEvent actionEvent) {
        if (middleman.isConnected()) {
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            FileChooser chooser = new FileChooser();
            File selectedFile = chooser.showOpenDialog(stage);
            if (middleman.sendFile(selectedFile.getAbsolutePath(), selectedFile.getName())) refreshData();
            else showAlert(Alert.AlertType.WARNING, "Nie udało się wysłać pliku");
        }
    }

    public void handleUploadDir(ActionEvent actionEvent) {
        if (middleman.isConnected()) {
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedDir = chooser.showDialog(stage);
            uploadDirectory(selectedDir.getAbsolutePath());
            refreshData();
        }
    }

    public void handleLogout(ActionEvent actionEvent) throws IOException {
        if (middleman.isConnected()) {
            middleman.disconnect();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Parent newRoot = FXMLLoader.load(getClass().getResource("/res/layout/connectview.fxml"));
            stage.setScene(new Scene(newRoot));
        }
    }

    public void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Uwaga");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    public void downloadDirectory(String dirName, String localRoot) {
        middleman.goToDirectory(dirName);
        System.err.println("Downloading " + dirName + " to " + localRoot);
        FTPFile[] files = middleman.listFiles();
        if (files.length < 1) {
            middleman.toParent();
            return;
        }
        File newRoot = new File(localRoot + "/" + dirName);
        System.err.println(newRoot.getAbsolutePath());
        if (!newRoot.exists()) newRoot.mkdirs();
        for (FTPFile fileToDownload : files) {
            if (fileToDownload.isFile()) {
                middleman.downloadFile(fileToDownload.getName(), newRoot.getAbsolutePath() + "/" + fileToDownload.getName());
            } else if (fileToDownload.isDirectory()) {
                downloadDirectory(fileToDownload.getName(), newRoot.getAbsolutePath());
            }
        }
        middleman.toParent();
    }

    public void uploadDirectory(String localRoot) {
        File currentRoot = new File(localRoot);
        System.err.println("Uploading " + localRoot);
        File[] files = currentRoot.listFiles();
        if (files == null || files.length < 1) return;
        if (middleman.makeDirectory(currentRoot.getName())) {
            middleman.goToDirectory(currentRoot.getName());
            for (File file : files) {
                if (file.isFile()) middleman.sendFile(file.getAbsolutePath(), file.getName());
                else if (file.isDirectory()) uploadDirectory(file.getAbsolutePath());
            }
            middleman.toParent();
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Nie można było utworzyć katalogu " + currentRoot.getName());
        }
    }
}
