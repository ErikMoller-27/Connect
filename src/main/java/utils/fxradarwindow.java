package utils;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

public final class fxradarwindow {
    private static volatile boolean fxStarted = false;

    private static void ensureFxStarted() {
        if (fxStarted) return;
        CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(() -> { fxStarted = true; latch.countDown(); });
        try { latch.await(); } catch (InterruptedException ignored) {}
    }

    /** Show the radar and block until the window is closed. */
    public static void showAndWait(Map<String, Integer> scores) {
        ensureFxStarted();
        CountDownLatch closed = new CountDownLatch(1);

        Platform.runLater(() -> {
            Stage stage = new Stage();
            radarview view = new radarview();
            view.setScores(scores);

            StackPane root = new StackPane(view);
            Scene scene = new Scene(root, 820, 620, Color.WHITE);

            stage.setTitle("Career Radar");
            stage.setScene(scene);
            stage.setOnShown(e -> view.play());     // animate on show
            stage.setOnCloseRequest(e -> closed.countDown());
            stage.show();
        });

        try { closed.await(); } catch (InterruptedException ignored) {}
    }

    private fxradarwindow() {}
}
