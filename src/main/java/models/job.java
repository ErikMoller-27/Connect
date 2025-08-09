package models;

import java.util.ArrayList;
import java.util.List;

public class job {
    private final int companyId;
    private final String companyName;
    private final List<JobListing> jobs;

    public job(int companyId, String companyName) {
        this.companyId = companyId;
        this.companyName = companyName;
        this.jobs = new ArrayList<>();
    }

    public static class JobListing {
        private final String title;
        private final String description;
        private double avgPercentage;
        private int userCount;

        public JobListing(String title, String description, double avgPercentage) {
            this.title = title;
            this.description = description;
            this.avgPercentage = avgPercentage;
            this.userCount = 1;
        }

        // Getters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public double getAvgPercentage() { return avgPercentage; }
        public int getUserCount() { return userCount; }

        // For updating averages
        public void updateAverage(double newPercentage) {
            this.avgPercentage = ((avgPercentage * userCount) + newPercentage) / (userCount + 1);
            userCount++;
        }
    }

    public void addJob(String title, String description, double avgPercentage) {
        jobs.stream()
                .filter(j -> j.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .ifPresentOrElse(
                        job -> job.updateAverage(avgPercentage),
                        () -> jobs.add(new JobListing(title, description, avgPercentage))
                );
    }

    public List<JobListing> getJobs() {
        return new ArrayList<>(jobs);
    }

    public int getCompanyId() { return companyId; }
    public String getCompanyName() { return companyName; }
}