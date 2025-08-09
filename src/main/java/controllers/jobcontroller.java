package controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;

public class jobcontroller {

    @FXML private TextField searchfield;
    @FXML private ListView<Job> joblist;
    @FXML private Label jobTitle;
    @FXML private Label jobCompany;
    @FXML private TextArea jobDescription;
    @FXML private Canvas radialCanvas;

    @FXML
    public void initialize() {
        List<Job> jobs = Arrays.asList(
                new Job("Software Engineer","Tanda","Brisbane",
                        "Help build backend services and APIs.", new double[]{85,70,60,40,90,55}),
                new Job("Data Analyst","Macquarie","Sydney",
                        "Analyze datasets and build dashboards.", new double[]{60,88,72,55,40,65}),
                new Job("UX Researcher","QUT","Brisbane",
                        "Run user studies and design research.", new double[]{50,65,85,70,60,45}),
                new Job("Product Manager","Google","Sydney",
                        "Lead product development cycles.", new double[]{75,60,70,80,65,70}),
                new Job("DevOps Engineer","Amazon","Melbourne",
                        "Manage cloud infrastructure.", new double[]{80,85,75,60,70,65}),
                new Job("Marketing Specialist","Facebook","Sydney",
                        "Drive marketing campaigns.", new double[]{60,70,65,75,80,55}),
                new Job("Sales Associate","Apple","Brisbane",
                        "Manage client accounts.", new double[]{65,60,70,80,75,60}),
                new Job("HR Manager","Microsoft","Sydney",
                        "Lead HR initiatives.", new double[]{70,75,80,65,60,70}),
                new Job("Business Analyst","Deloitte","Melbourne",
                        "Analyze business processes.", new double[]{65,70,60,75,80,70}),
                new Job("Graphic Designer","Adobe","Sydney",
                        "Create visual content.", new double[]{85,70,60,55,65,75}),
                new Job("ACS ICT Professional","Australian Computer Society","Brisbane",
                        "Assess ICT skills and provide certification aligned with Australian standards.",
                        new double[]{75, 80, 70, 65, 60, 55}),
                new Job("Cybersecurity Analyst","Cisco","Sydney",
                        "Protect systems and networks from cyber threats and vulnerabilities.",
                        new double[]{85, 90, 80, 75, 70, 65}),
                new Job("Cloud Solutions Architect","AWS","Melbourne",
                        "Design and implement scalable cloud infrastructure and services.",
                        new double[]{90, 85, 75, 80, 70, 60})
                );


                joblist.setItems(FXCollections.observableArrayList(jobs));
        joblist.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Job job, boolean empty) {
                super.updateItem(job, empty);
                setText(empty || job == null ? null : job.getTitle() + " — " + job.getCompany());
            }
        });

        joblist.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) showJobDetails(newVal);
        });

        if (!jobs.isEmpty()) joblist.getSelectionModel().select(0);
    }

    private void showJobDetails(Job job) {
        jobTitle.setText(job.getTitle());
        jobCompany.setText(job.getCompany() + " — " + job.getLocation());
        jobDescription.setText(job.getDescription());
        drawRadialChart(job.getKeywordPercentages());
    }

    private void drawRadialChart(double[] percentages) {
        if (radialCanvas == null) return;
        GraphicsContext gc = radialCanvas.getGraphicsContext2D();
        double w = radialCanvas.getWidth();
        double h = radialCanvas.getHeight();
        gc.clearRect(0,0,w,h);

        double cx = w/2, cy = h/2;
        double maxR = Math.min(w,h)/2 - 10;
        int segments = percentages.length;

        // grid circles
        gc.setStroke(Color.LIGHTGRAY);
        for (int i = 1; i <= 5; i++) {
            double r = maxR * i / 5.0;
            gc.strokeOval(cx - r, cy - r, r*2, r*2);
        }

        // radial lines
        gc.setStroke(Color.GRAY);
        for (int i = 0; i < segments; i++) {
            double angle = 2*Math.PI*i/segments - Math.PI/2;
            double x = cx + Math.cos(angle)*maxR;
            double y = cy + Math.sin(angle)*maxR;
            gc.strokeLine(cx, cy, x, y);
        }

        // filled polygon
        gc.setFill(Color.rgb(0,150,255,0.4));
        gc.beginPath();
        for (int i = 0; i < segments; i++) {
            double angle = 2*Math.PI*i/segments - Math.PI/2;
            double r = (percentages[i]/100.0) * maxR;
            double x = cx + Math.cos(angle)*r;
            double y = cy + Math.sin(angle)*r;
            if (i == 0) gc.moveTo(x,y); else gc.lineTo(x,y);
        }
        gc.closePath();
        gc.fill();

        // outline
        gc.setStroke(Color.DARKBLUE);
        gc.stroke();
    }

    @FXML
    private void handlesearch() {
        String q = searchfield.getText();
        if (q == null || q.isBlank()) {
            // show everything (no filter). If you keep a master list, restore it here.
            joblist.setItems(joblist.getItems());
            return;
        }
        String lower = q.toLowerCase();
        joblist.setItems(joblist.getItems().filtered(j ->
                j.getTitle().toLowerCase().contains(lower) || j.getCompany().toLowerCase().contains(lower)
        ));
    }

    // Simple inner Job model — you can move this to its own file later
    public static class Job {
        private final String title, company, location, description;
        private final double[] keywordPercentages;
        public Job(String t, String c, String l, String d, double[] p) { title=t; company=c; location=l; description=d; keywordPercentages=p; }
        public String getTitle() { return title; }
        public String getCompany() { return company; }
        public String getLocation() { return location; }
        public String getDescription() { return description; }
        public double[] getKeywordPercentages() { return keywordPercentages; }
        @Override public String toString(){ return title + " — " + company; }
    }
}
