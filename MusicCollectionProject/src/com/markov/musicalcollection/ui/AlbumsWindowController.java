package com.markov.musicalcollection.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.markov.musicalcollection.datamodel.Album;
import com.markov.musicalcollection.datamodel.DataSource;

import java.io.IOException;
import java.util.Optional;

public class AlbumsWindowController {

    @FXML
    TableView<Album> albumView;

    @FXML
    TextField albumTextField;

    @FXML
    Label infoLabel;

    private int currArtist;

    public void listAlbums(int artistId) {
        currArtist = artistId;

        Task<ObservableList<Album>> task = new Task<ObservableList<Album>>() {
            @Override
            protected ObservableList<Album> call() {
                return FXCollections.observableArrayList(
                        DataSource.getInstance().queryAlbums(currArtist));
            }
        };
        albumView.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();

        updateInfoLabel();
    }

    @FXML
    private void searchAlbumByName() {
        Task<ObservableList<Album>> task = new Task<ObservableList<Album>>() {
            @Override
            protected ObservableList<Album> call() {
                return FXCollections.observableArrayList(
                        DataSource.getInstance().queryAlbumsByName(albumTextField.getText()));
            }
        };
        albumView.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();
    }

    @FXML
    private void addAlbum() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(albumView.getScene().getWindow());

        dialog.setTitle("Add Album");
        dialog.setHeaderText("Enter the album's name: ");
        TextField nameField = new TextField();

        dialog.getDialogPane().setContent(nameField);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.APPLY) {
            String name = nameField.getText().trim();
            if(name.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Empty Name");
                alert.showAndWait();
            } else {
                String title, content;
                int id;
                if((id = DataSource.getInstance().addAlbum(name)) != -1) {
                    Album album = new Album();
                    album.setArtistId(currArtist);
                    album.setName(name);
                    album.setId(id);
                    title = "Added";
                    content = name + " added to Albums successfully!";
                    albumView.getItems().add(album);
                    albumView.refresh();
                } else {
                    title = "Failed";
                    content = "Error occurred while adding " + name + " to Artists";
                }
                Alert alert = createInformationAlert(title, "", content);
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void updateAlbumName() {
        Album album = albumView.getSelectionModel().getSelectedItem();

        if(album == null) {
            Alert alert = createInformationAlert("Error Updating Name", "No Album Selected",
                    "You must choose the album which name you want to change!");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(albumView.getScene().getWindow());
        dialog.setTitle("Update Album Name");
        dialog.setHeaderText("Enter the new name: ");
        TextField nameField = new TextField();
        dialog.getDialogPane().setContent(nameField);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.APPLY) {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Empty Name");
                alert.showAndWait();
            } else {
                if (DataSource.getInstance().updateAlbumName(album.getId(), name)) {
                    album.setName(name);
                    albumView.refresh();
                }
            }
        }
    }

    @FXML
    private void deleteAlbum() {
        Album album = albumView.getSelectionModel().getSelectedItem();

        if(album == null) {
            Alert alert = createInformationAlert("Error Deleting Album", "No Album Selected",
                    "Please select the album that you want to delete!");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Album");
        dialog.setHeaderText("This operation will delete ALL the songs in this album!");
        dialog.setContentText("Would you like to proceed?");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.YES) {
            String title, content;

            if(DataSource.getInstance().deleteAlbum(album.getId())) {
                title = "Deleted";
                content = "The album was deleted successfully!";
                albumView.getItems().remove(album);
                albumView.refresh();
            } else {
                title = "Failed";
                content = "Error occurred while deleting the album!";
            }
            Alert alert = createInformationAlert(title, "", content);
            alert.showAndWait();
        }
    }

    @FXML
    private void goToArtists() {
        Scene currScene = albumView.getScene();
        Stage mainStage = (Stage) currScene.getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainwindow.fxml"));
        try {
            Parent root = loader.load();
            mainStage.setScene(new Scene(root, currScene.getWidth(), currScene.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void listSongsInAlbum() {
        Album album = albumView.getSelectionModel().getSelectedItem();

        if(album == null) {
            Alert alert = createInformationAlert("Error Showing Songs", "No Album Selected",
                    "You must choose the album which songs you want to check!");
            alert.showAndWait();
            return;
        }

        Scene currScene = albumView.getScene();
        Stage mainStage = (Stage) currScene.getWindow();

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("songswindow.fxml"));
            Parent root = loader.load();
            SongsWindowController controller = loader.getController();
            controller.listSongs(currArtist, album.getId());
            mainStage.setScene(new Scene(root, currScene.getWidth(), currScene.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    private void clearTextField() {
        if(albumTextField.getText().equals("Search for album...")) {
            albumTextField.setText("");
        }
    }

    @FXML
    private void restoreTextField() {
        if(albumTextField.getText().equals("")) {
            albumTextField.setText("Search for album...");
        }
    }

    private Alert createInformationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

    private void updateInfoLabel() {
        String artist = DataSource.getInstance().getCurrArtist(currArtist);

        infoLabel.setText("Albums by " + artist);
    }

}
