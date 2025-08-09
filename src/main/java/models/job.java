package models;

import java.util.*;

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

        // Smart average update
        public void updateAverage(double newPercentage) {
            this.avgPercentage =
                    ((avgPercentage * userCount) + newPercentage) / (userCount + 1);
            userCount++;
        }

        // Getters
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public double getAvgPercentage() { return avgPercentage; }
        public int getUserCount() { return userCount; }

        @Override
        public String toString() {
            return String.format("%s (%.1f%%, %d users): %s",
                    title, avgPercentage, userCount,
                    description.substring(0, Math.min(30, description.length())) + "...");
        }
    }

    // Add or update job
    public void addOrUpdateJob(String title, String description, double percentage) {
        jobs.stream()
                .filter(j -> j.getTitle().equalsIgnoreCase(title))
                .findFirst()
                .ifPresentOrElse(
                        job -> job.updateAverage(percentage),
                        () -> jobs.add(new JobListing(title, description, percentage))
                );
    }

    // Get jobs sorted by best match
    public List<JobListing> getJobsSorted() {
        jobs.sort(Comparator.comparingDouble(JobListing::getAvgPercentage).reversed());
        return Collections.unmodifiableList(jobs);
    }

    // Getters
    public int getCompanyId() { return companyId; }
    public String getCompanyName() { return companyName; }

    @Override
    public String toString() {
        return String.format("Company[ID: %d, Name: %s, Jobs: %s]",
                companyId, companyName, jobs);
    }
}