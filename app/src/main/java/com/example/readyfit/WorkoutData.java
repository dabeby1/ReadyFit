
package com.example.readyfit;

import java.util.List;

public class WorkoutData {

    private String title;
    private String description;
    private String duration;
    private String difficulty;
    private String category;
    private List<String> exercises;

    public WorkoutData(String title,
                       String description,
                       String duration,
                       String difficulty,
                       String category,
                       List<String> exercises) {

        this.title = title;
        this.description = description;
        this.duration = duration;
        this.difficulty = difficulty;
        this.category = category;
        this.exercises = exercises;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDuration() {
        return duration;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getCategory() {
        return category;
    }

    public List<String> getExercises() {
        return exercises;
    }
}
