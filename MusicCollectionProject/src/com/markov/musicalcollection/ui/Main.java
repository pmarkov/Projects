package com.markov.musicalcollection.ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.markov.musicalcollection.datamodel.DataSource;

import java.io.File;

public class Main extends Application {

    private boolean openSuccessfully = false;

    @Override
    public void start(Stage primaryStage) throws Exception{

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Database", "*.db"));
        File file = fileChooser.showOpenDialog(primaryStage);
        if(file != null) {
            if(DataSource.getInstance().open(file.getAbsolutePath())) {
                openSuccessfully = true;
            }
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainwindow.fxml"));
        Parent root = loader.load();
        ArtistsWindowController controller = loader.getController();
        controller.queryArtists();
        primaryStage.setTitle("Music Collection");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();

        if(!openSuccessfully) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("ERROR LOADING THE DATABASE");
            alert.showAndWait();
            Platform.exit();
        }
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        DataSource.getInstance().close();
    }
}
