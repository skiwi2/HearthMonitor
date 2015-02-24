package com.github.skiwi2.hearthmonitor.view;

import com.github.skiwi2.hearthmonitor.HearthMonitor;
import com.github.skiwi2.hearthmonitor.model.Game;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * @author Frank van Heeswijk
 */
public class HearthMonitorApplication extends Application {
    private static final String LOGFILE_DIRECTORY = "LOGFILE_DIRECTORY";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        Preferences preferences = Preferences.userNodeForPackage(HearthMonitorApplication.class);

        FileChooser fileChooser = new FileChooser();
        String initialDirectoryString = preferences.get(LOGFILE_DIRECTORY, null);
        if (initialDirectoryString != null) {
            fileChooser.setInitialDirectory(Paths.get(initialDirectoryString).toFile());
        }
        fileChooser.setTitle("Open HearthStone log file");
        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        preferences.put(LOGFILE_DIRECTORY, selectedFile.toPath().getParent().normalize().toString());

        List<Game> games = HearthMonitor.readGamesFromLog(selectedFile.toPath());

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("Game.fxml"));
        fxmlLoader.setController(new GameController(games.get(0)));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root);
        primaryStage.setTitle("HearthMonitor");
        primaryStage.setMaximized(true);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
