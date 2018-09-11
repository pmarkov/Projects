package com.markov.musicalcollection.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import com.markov.musicalcollection.datamodel.DataSource;
import com.markov.musicalcollection.datamodel.Song;
import java.io.IOException;
import java.util.Optional;

public class SongsWindowController {

    @FXML
    private TableView<Song> songView;

    @FXML
    private TextField songTextField;

    @FXML
    private Label infoLabel;

    private int currArtistId;
    private int currAlbumId;

    public void listSongs(int artistId, int albumId) {
        currArtistId = artistId;
        currAlbumId = albumId;

        Task<ObservableList<Song>> task = new Task<ObservableList<Song>>() {
            @Override
            protected ObservableList<Song> call() {
                return FXCollections.observableArrayList(
                        DataSource.getInstance().querySongs(currAlbumId));
            }
        };

        songView.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();

        String currAlbum = DataSource.getInstance().getCurrAlbum(currAlbumId);
        String currArtist = DataSource.getInstance().getCurrArtist(currArtistId);
        infoLabel.setText("Songs in \"" + currAlbum + "\" by " + currArtist);
    }

    @FXML
    private void searchSongByName() {
        Task<ObservableList<Song>> task = new Task<ObservableList<Song>>() {
            @Override
            protected ObservableList<Song> call() {
                return FXCollections.observableArrayList(
                        DataSource.getInstance().querySongsByName(songTextField.getText(), currAlbumId));
            }
        };

        songView.itemsProperty().bind(task.valueProperty());

        new Thread(task).start();
    }

    @FXML
    private void addSong() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(songView.getScene().getWindow());

        dialog.setTitle("Add Song");

        GridPane pane = new GridPane();
        pane.setHgap(10);
        pane.setVgap(10);
        Label trackLabel = new Label("Enter the track: ");
        TextField trackField = new TextField();
        trackField.setPrefWidth(50);
        Label titleLabel = new Label("Enter the title: ");
        TextField titleField = new TextField();

        pane.add(trackLabel, 0, 0);
        pane.add(trackField, 1, 0);
        pane.add(titleLabel, 0, 1);
        pane.add(titleField, 1, 1);

        dialog.getDialogPane().setContent(pane);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.APPLY) {

            int track;
            try {
                track = Integer.parseInt(trackField.getText());
            } catch (NumberFormatException e) {
                Alert alert = createInformationAlert("Failed", "", "Invalid Track Input");
                alert.showAndWait();
                return;
            }
            String title = titleField.getText().trim();
            if(title.isEmpty() || track < 1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid Input");
                alert.showAndWait();
            } else {
                String alertTitle, content;
                int id;
                if((id = DataSource.getInstance().addSong(track, title)) != -1) {
                    Song song = new Song();
                    song.setTitle(title);
                    song.setId(id);
                    song.setTrack(track);
                    song.setAlbumId(currAlbumId);
                    alertTitle = "Added";
                    content = title + " added to Albums successfully!";
                    songView.getItems().add(song);
                    songView.refresh();
                } else {
                    alertTitle = "Failed";
                    content = "Error occurred while adding " + title + " to Artists";
                }
                Alert alert = createInformationAlert(alertTitle, "", content);
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void updateSongName() {
        Song song = songView.getSelectionModel().getSelectedItem();

        if(song == null) {
            Alert alert = createInformationAlert("Error Updating Name", "No Song Selected",
                    "You must choose the song which title you want to change!");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(songView.getScene().getWindow());
        dialog.setTitle("Update Song Name");
        dialog.setHeaderText("Enter the new name: ");
        TextField nameField = new TextField();
        dialog.getDialogPane().setContent(nameField);

        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.APPLY) {
            String title = nameField.getText().trim();

            if (title.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setHeaderText("Empty Name");
                alert.showAndWait();
            } else {
                if (DataSource.getInstance().updateSongTitle(song.getId(), title)) {
                    song.setTitle(title);
                    songView.refresh();
                }
            }
        }
    }

    @FXML
    private void deleteSong() {
        Song song = songView.getSelectionModel().getSelectedItem();

        if(song == null) {
            Alert alert = createInformationAlert("Error Deleting Song", "No Song Selected",
                    "Please select the song that you want to delete!");
            alert.showAndWait();
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Delete Song");
        dialog.setContentText("Are you sure you want to delete this song?");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = dialog.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.YES) {
            String title, content;

            if(DataSource.getInstance().deleteSong(song.getId())) {
                title = "Deleted";
                content = "The song was deleted successfully!";
                songView.getItems().remove(song);
                songView.refresh();
            } else {
                title = "Failed";
                content = "Error occurred while deleting the song!";
            }
            Alert alert = createInformationAlert(title, "", content);
            alert.showAndWait();
        }
    }

    @FXML
    private void goToAlbums() {
        Scene currScene = songView.getScene();
        Stage mainStage = (Stage) currScene.getWindow();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("albumswindow.fxml"));
            Parent root = loader.load();
            AlbumsWindowController controller = loader.getController();
            controller.listAlbums(currArtistId);
            mainStage.setScene(new Scene(root, currScene.getWidth(), currScene.getHeight()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void clearTextField() {
        if(songTextField.getText().equals("Search for song...")) {
            songTextField.setText("");
        }
    }

    @FXML
    private void restoreTextField() {
        if(songTextField.getText().equals("")) {
            songTextField.setText("Search for song...");
        }
    }

    private Alert createInformationAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }

}
