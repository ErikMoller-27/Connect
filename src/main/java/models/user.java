package models;

import java.util.HashMap;
import java.util.Map;

public class user {
    private int userId;
    private String firstName;
    private Map<String, Integer> skills = new HashMap<>();

    public user(int userId, String firstName) {
        this.userId = userId;
        this.firstName = firstName;
    }

    public void addSkills(Map<String, Integer> skills) {
        this.skills.putAll(skills);
    }

    public int getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public Map<String, Integer> getSkills() {
        return skills;
    }
}
