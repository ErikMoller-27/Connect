package models;

import java.util.*;

public class user {
    private final int userId;
    private final String firstName;
    private final Map<String, Integer> skills;

    public user(int userId, String firstName) {
        this.userId = userId;
        this.firstName = firstName;
        this.skills = new HashMap<>();
    }

    // Single skill addition
    public void addSkill(String subject, int percentage) {
        skills.put(subject, percentage);
    }

    // Bulk skills addition
    public void addSkills(Map<String, Integer> skills) {
        this.skills.putAll(skills);
    }

    // Getters
    public int getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public Map<String, Integer> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    @Override
    public String toString() {
        return String.format("User[ID: %d, Name: %s, Skills: %s]",
                userId, firstName, skills);
    }
}