package com.markov.musicalcollection.ui;

import com.markov.musicalcollection.datamodel.Artist;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import com.markov.musicalcollection.datamodel.DataSource;

import java.io.IOException;
import java.util.Optional;

public class ArtistsWindowController {

    @FXML
    public TableView<Artist> artistView;
    @FXML
    public TextField artistTextField;

    public void initialize() {
        queryArtists();
    }

    public void queryArtists() {
        Task<ObservableList<Artist>> task = new Task<ObservableList<Artist>>() {
            @Override
            protected ObservableList<Artist> call() {
                return FXCollections.observableArrayList(
                        DataSource.getInstance().queryArtists());
            }
        };

        artistView.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();
    }

    @FXML
    private void searchArtistByName() {
        Task<ObservableList<Artist>> task = new Task<ObservableList<Artist>>() {
            @Override
            protected ObservableList<Artist> call() {
                return FXCollections.observableArrayList(
                        DataSource.getInstance().queryArtistsByName(artistTextField.getText()));
            }
        };

        artistView.itemsProperty().bind(task.valueProperty());
        new Thread(task).start();
    }

    @FXML
    private void addArtist() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(artistView.getScene().getWindow());

        dialog.setTitle("Add Artist");
        dialog.setHeaderText("Enter the artist's name: ");
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
                if((id = DataSource.getInstance().addArtist(name)) != -1) {
                    Artist artist = new Artist();
                    artist.setName(name);
                    artist.setId(id);
                    title = "Added";
                    content = name + " added to Artists successfully!";

                  artistView.getItems().add(artist);
                  artistView.refresh();
//                    queryArtists();
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
    private void updateArtistName() {
        Artist artist = artistView.getSelectionModel().getSelectedItem();

        if(artist == null) {
            Alert alert = createInformationAlert("Error Updating Name", "No Artist Selected",
                    "You must choose an artist whose name you would like to change!");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(artistView.getScene().getWindow());
        dialog.setTitle("Update Artist Name");
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
                if (DataSource.getInstance().updateArtistName(artist.getId(), name)) {
                    artist.setName(name);
                    artistView.refresh();
                }
            }
        }
    }

    @FXML
    private void deleteArtist() {
        Artist artist = artistView.getSelectionModel().getSelectedItem();

        if(artist == null) {
            Alert alert = createInformationAlert("Error Deleting Artist", "No Artist Selected",
                    "Please select the artist that you want to delete!");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Artist");
        dialog.setHeaderText("This operation will delete ALL the albums AND songs by this artist!");
        dialog.setContentText("Would you like to proceed?");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.YES) {
            String title, content;

            if(DataSource.getInstance().deleteArtist(artist.getId())) {
                title = "Deleted";
                content = "The artist was deleted successfully!";
                artistView.getItems().remove(artist);
                artistView.refresh();
            } else {
                title = "Failed";
                content = "Error occurred while deleting the artist!";
            }
            Alert alert = createInformationAlert(title, "", content);
            alert.showAndWait();
        }
    }

    @FXML
    private void listAlbumsForArtist() {
        Artist artist = artistView.getSelectionModel().getSelectedItem();

        if(artist == null) {
            Alert alert = createInformationAlert("Error Showing Albums", "No Artist Selected",
                    "You must choose an artist whose albums you want to check!");
            alert.showAndWait();
            return;
        }

        Scene currScene = artistView.getScene();
        Stage mainStage = (Stage) currScene.getWindow();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("albumswindow.fxml"));
            Parent root = loader.load();
            Scene newScene = new Scene(root, currScene.getWidth(), currScene.getHeight());
            AlbumsWindowController controller = loader.getController();
            controller.listAlbums(artist.getId());
            mainStage.setScene(newScene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Alert createInformationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        return alert;
    }

    @FXML
    private void clearTextField() {
        if(artistTextField.getText().equals("Search for artist...")) {
            artistTextField.setText("");
        }
    }

    @FXML
    private void restoreTextField() {
        if(artistTextField.getText().equals("")) {
            artistTextField.setText("Search for artist...");
        }
    }
}
