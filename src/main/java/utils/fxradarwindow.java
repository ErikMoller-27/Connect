package utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Map;

public class fxradarwindow {
    private static volatile boolean started = false;

    public static void show(Map<String,Integer> scores) {
        ensureFX();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.setTitle("Career Radar");
            radarview view = new radarview();
            view.setScores(scores);
            Scene scene = new Scene(view, 720, 560, Color.WHITE);
            stage.setScene(scene);
            stage.show();
            view.play(); // animate
        });
    }

    private static void ensureFX() {
        if (started) return;
        synchronized (fxradarwindow.class) {
            if (started) return;
            Platform.startup(() -> {}); // initialize JavaFX toolkit once
            started = true;
        }
    }
}
