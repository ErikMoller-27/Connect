package models;

import java.util.*;

public class user {
    private final int userId;
    private final String firstName;
    private final List<Skill> skills;

    public user(int userId, String firstName) {
        this.userId = userId;
        this.firstName = firstName;
        this.skills = new ArrayList<>();
    }

    public static class Skill {
        private final String subject;
        private final int percentage;

        public Skill(String subject, int percentage) {
            this.subject = subject;
            this.percentage = percentage;
        }

        public String getSubject() { return subject; }
        public int getPercentage() { return percentage; }

        @Override
        public String toString() {
            return String.format("%s: %d%%", subject, percentage);
        }
    }

    // Bulk skill addition
    public void addSkills(Map<String, Integer> skills) {
        skills.forEach((subject, percentage) ->
                this.skills.add(new Skill(subject, percentage))
        );
    }

    // Immutable view of skills
    public List<Skill> getSkills() {
        return Collections.unmodifiableList(skills);
    }

    // Skill search
    public Optional<Skill> findSkill(String subject) {
        return skills.stream()
                .filter(s -> s.getSubject().equalsIgnoreCase(subject))
                .findFirst();
    }

    // Getters
    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }

    @Override
    public String toString() {
        return String.format("User[ID: %d, Name: %s, Skills: %s]",
                userId, firstName, skills);
    }
}