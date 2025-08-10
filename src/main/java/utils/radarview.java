package utils;

import javafx.scene.text.Text;
import javafx.animation.*;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.LinkedHashMap;
import java.util.Map;

public class radarview extends Pane {
    private static final String[] KEYS = {
            "Education","ProgrammingSkills","Certifications","Projects","Collaboration","Experience"
    };
    private final Map<String,Integer> targets = new LinkedHashMap<>();
    private final DoubleProperty[] progress = new DoubleProperty[6]; // 0..1 per segment

    private final Canvas canvas = new Canvas(720, 560);

    public radarview() {
        getChildren().add(canvas);
        for (int i = 0; i < 6; i++) {
            progress[i] = new SimpleDoubleProperty(0);
            progress[i].addListener((obs, o, n) -> draw());
        }
        // default zeros
        for (String k : KEYS) targets.put(k, 0);

        // keep canvas sized to pane
        widthProperty().addListener((o, ov, nv) -> { canvas.setWidth(nv.doubleValue()); draw(); });
        heightProperty().addListener((o, ov, nv) -> { canvas.setHeight(nv.doubleValue()); draw(); });
    }

    public void setScores(Map<String,Integer> scores) {
        targets.clear();
        for (String k : KEYS) targets.put(k, clamp(scores.getOrDefault(k, 0), 0, 100));
        draw();
    }

    /** Animate all six segments from 0 to their target values. */
    public void play(Duration duration) {
        for (int i = 0; i < 6; i++) progress[i].set(0);
        Timeline tl = new Timeline();
        for (int i = 0; i < 6; i++) {
            double to = targets.get(KEYS[i]) / 100.0;
            // slight stagger looks nice
            Duration start = duration.multiply(0.05 * i);
            Duration end   = start.add(duration);
            tl.getKeyFrames().add(new KeyFrame(start,  new KeyValue(progress[i], 0)));
            tl.getKeyFrames().add(new KeyFrame(end,    new KeyValue(progress[i], to, Interpolator.EASE_BOTH)));
        }
        tl.play();
    }

    public void play() { play(Duration.seconds(3)); }

    // --- drawing ---
    private void draw() {
        double w = canvas.getWidth(), h = canvas.getHeight();
        GraphicsContext g = canvas.getGraphicsContext2D();

        // clear
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);

        double cx = w / 2.0, cy = h / 2.0;
        double outer = Math.min(w, h) * 0.40;   // outer radius
        double innerBase = outer * 0.30;        // minimum inner radius
        double maxThickness = outer - innerBase;
        double segAngle = 360.0 / 6.0;

        // grid circles
        g.setStroke(Color.rgb(0,0,0,0.15));
        g.setLineWidth(1.0);
        for (int r = 1; r <= 4; r++) {
            double rr = innerBase + (maxThickness * r / 4.0);
            g.strokeOval(cx - rr, cy - rr, rr*2, rr*2);
        }

        // background track wedges
        for (int i = 0; i < 6; i++) {
            double start = -90 + i * segAngle; // start angle degrees (top, clockwise)
            // track: full thickness
            g.setFill(Color.rgb(0,0,0,0.12)); // light grey (12% opacity)
            fillRingWedge(g, cx, cy, outer, innerBase, start, segAngle);
        }

        // data wedges (grow from innerBase outward)
        for (int i = 0; i < 6; i++) {
            double p = clamp(progress[i].get(), 0, 1);
            if (p <= 0) continue;
            double start = -90 + i * segAngle;

            // thickness scales with p, extend OUTWARD from innerBase
            double t = maxThickness * p;
            double outerFilled = innerBase + t;  // outward growth
            double inner = innerBase;

            g.setFill(Color.rgb(30,144,255, 0.65));
            fillRingWedge(g, cx, cy, outerFilled, inner, start, segAngle);

            g.setStroke(Color.rgb(30,144,255));
            g.setLineWidth(2);
            strokeRingWedge(g, cx, cy, outerFilled, inner, start, segAngle);
        }


        // separators
        g.setStroke(Color.rgb(0,0,0,0.20));
        g.setLineWidth(1.0);
        for (int i = 0; i < 6; i++) {
            double angRad = Math.toRadians(-90 + i * segAngle);
            double x = cx + outer * Math.cos(angRad);
            double y = cy + outer * Math.sin(angRad);
            g.strokeLine(cx, cy, x, y);
        }

        // labels
        g.setFill(Color.rgb(0,0,0,0.85));
        g.setFont(javafx.scene.text.Font.font("SansSerif", 13));
        for (int i = 0; i < 6; i++) {
            String key = KEYS[i];
            int val = targets.getOrDefault(key, 0);
            String label = key + " (" + val + ")";
            double angRad = Math.toRadians(-90 + (i + 0.5) * segAngle);
            double rx = cx + (outer + 24) * Math.cos(angRad);
            double ry = cy + (outer + 24) * Math.sin(angRad);

            // Measure text using public API
            Text t = new Text(label);
            t.setFont(g.getFont());
            double tw = t.getLayoutBounds().getWidth();
            double th = t.getLayoutBounds().getHeight();

            double lx = rx, ly = ry;
            if (Math.cos(angRad) < -0.2) lx -= tw;            // left side align left
            if (Math.abs(Math.cos(angRad)) <= 0.2) ly += th/2; // near top/bottom
            g.fillText(label, lx, ly);
        }
    }

    private static void fillRingWedge(GraphicsContext g, double cx, double cy, double outerR, double innerR, double startDeg, double extentDeg) {
        // Draw outer sector, then punch inner hole by overdrawing with background
        g.fillArc(cx - outerR, cy - outerR, outerR*2, outerR*2, startDeg, extentDeg, javafx.scene.shape.ArcType.ROUND);
        // punch hole
        g.setFill(Color.WHITE);
        g.fillArc(cx - innerR, cy - innerR, innerR*2, innerR*2, startDeg, extentDeg, javafx.scene.shape.ArcType.ROUND);
        // restore fill color left to caller on next call
    }

    private static void strokeRingWedge(GraphicsContext g, double cx, double cy, double outerR, double innerR, double startDeg, double extentDeg) {
        // outline outer and inner arcs + the two radial edges
        g.strokeArc(cx - outerR, cy - outerR, outerR*2, outerR*2, startDeg, extentDeg, javafx.scene.shape.ArcType.OPEN);
        g.strokeArc(cx - innerR, cy - innerR, innerR*2, innerR*2, startDeg, extentDeg, javafx.scene.shape.ArcType.OPEN);

        // edge lines
        double s = Math.toRadians(startDeg);
        double e = Math.toRadians(startDeg + extentDeg);
        g.strokeLine(cx + innerR*Math.cos(s), cy + innerR*Math.sin(s),
                cx + outerR*Math.cos(s), cy + outerR*Math.sin(s));
        g.strokeLine(cx + innerR*Math.cos(e), cy + innerR*Math.sin(e),
                cx + outerR*Math.cos(e), cy + outerR*Math.sin(e));
    }

    private static int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }
    private static double clamp(double v, double lo, double hi) { return Math.max(lo, Math.min(hi, v)); }
}