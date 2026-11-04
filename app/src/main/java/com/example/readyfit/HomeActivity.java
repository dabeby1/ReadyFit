
package com.example.readyfit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {

    private ImageView imgUnit;

    private TextView txtTargetUnit;
    private TextView txtAge;
    private TextView txtWeight;
    private TextView txtRunTime;

    private TextView txtWorkoutTitle;
    private TextView txtWorkoutDescription;
    private TextView txtWorkoutDuration;

    private TextView txtAnalysisTitle;
    private TextView txtAnalysisExplanation;

    private Button btnStartWorkout;
    private Button btnSettings;
    private Button btnWorkoutHistory;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private String currentTargetUnit = "לא נבחר";
    private String currentRunTime = "10:00";
    private String currentRecommendation = "שמירה על הרמה";

    private boolean userDataLoaded = false;
    private boolean analysisLoaded = false;
    private boolean workoutGenerated = false;

    private WorkoutData currentWorkout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        connectViews();

        btnStartWorkout.setEnabled(false);

        btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
        });

        btnWorkoutHistory.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, WorkoutHistoryActivity.class));
        });

        btnStartWorkout.setOnClickListener(v -> startWorkout());

        requestNotificationPermission();
    }

    private void connectViews() {
        imgUnit = findViewById(R.id.imgUnit);

        txtTargetUnit = findViewById(R.id.txtTargetUnit);
        txtAge = findViewById(R.id.txtAge);
        txtWeight = findViewById(R.id.txtWeight);
        txtRunTime = findViewById(R.id.txtRunTime);

        txtWorkoutTitle = findViewById(R.id.txtWorkoutTitle);
        txtWorkoutDescription = findViewById(R.id.txtWorkoutDescription);
        txtWorkoutDuration = findViewById(R.id.txtWorkoutDuration);

        txtAnalysisTitle = findViewById(R.id.txtAnalysisTitle);
        txtAnalysisExplanation = findViewById(R.id.txtAnalysisExplanation);

        btnStartWorkout = findViewById(R.id.btnStartWorkout);
        btnSettings = findViewById(R.id.btnSettings);
        btnWorkoutHistory = findViewById(R.id.btnWorkoutHistory);
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT >= 33) {

            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        100
                );
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // תזמון תזכורות האימון
        WorkoutReminderScheduler.scheduleDailyReminders(this);

        userDataLoaded = false;
        analysisLoaded = false;
        workoutGenerated = false;
        currentWorkout = null;

        btnStartWorkout.setEnabled(false);

        txtAnalysisTitle.setText("מנתח את המשובים שלך...");
        txtAnalysisExplanation.setText("רגע אחד, אנחנו מתאימים את האימון עבורך");

        loadUserData();
        loadFeedbackAnalysis();
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "יש להתחבר מחדש", Toast.LENGTH_LONG).show();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {
                        txtAge.setText("גיל: " + getValue(document, "age", "לא הוזן"));
                        txtWeight.setText("משקל: " + getValue(document, "weight", "לא הוזן") + " ק״ג");

                        currentRunTime = getValue(document, "runTime", "10:00");
                        currentTargetUnit = getValue(document, "targetUnit", "לא נבחר");

                        txtRunTime.setText("תוצאת 2000 מטר: " + currentRunTime);
                        txtTargetUnit.setText(currentTargetUnit);

                        setUnitImage(currentTargetUnit);

                    } else {
                        txtAge.setText("גיל: לא הוזן");
                        txtWeight.setText("משקל: לא הוזן");
                        txtRunTime.setText("תוצאת 2000 מטר: לא הוזנה");
                        txtTargetUnit.setText("לא נבחר");
                    }

                    userDataLoaded = true;
                    updateWorkoutIfReady();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "לא הצלחנו לטעון את פרטי המשתמש", Toast.LENGTH_SHORT).show();

                    userDataLoaded = true;
                    updateWorkoutIfReady();
                });
    }

    private void loadFeedbackAnalysis() {
        WorkoutFeedbackAnalyzer.analyzeLatestFeedback(
                new WorkoutFeedbackAnalyzer.AnalysisCallback() {

                    @Override
                    public void onSuccess(String recommendation, String explanation) {

                        currentRecommendation = recommendation;

                        txtAnalysisTitle.setText(recommendation);
                        txtAnalysisExplanation.setText(explanation);

                        analysisLoaded = true;
                        updateWorkoutIfReady();
                    }

                    @Override
                    public void onFailure(String error) {

                        currentRecommendation = "שמירה על הרמה";

                        txtAnalysisTitle.setText("שמירה על הרמה");
                        txtAnalysisExplanation.setText(
                                "לא הצלחנו לנתח את המשובים כרגע. האימון יישאר ברמה הנוכחית."
                        );

                        analysisLoaded = true;
                        updateWorkoutIfReady();
                    }
                }
        );
    }

    private void updateWorkoutIfReady() {

        if (!userDataLoaded || !analysisLoaded || workoutGenerated) {
            return;
        }

        workoutGenerated = true;

        currentWorkout = WorkoutGenerator.generate(
                currentTargetUnit,
                currentRunTime,
                currentRecommendation
        );

        if (currentWorkout == null) {
            Toast.makeText(this, "לא הצלחנו ליצור אימון", Toast.LENGTH_LONG).show();
            return;
        }

        txtWorkoutTitle.setText(currentWorkout.getTitle());
        txtWorkoutDescription.setText(currentWorkout.getDescription());
        txtWorkoutDuration.setText("משך האימון: " + currentWorkout.getDuration());

        btnStartWorkout.setEnabled(true);
    }

    private void startWorkout() {

        if (currentWorkout == null) {
            Toast.makeText(this, "האימון עדיין נטען", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(HomeActivity.this, WorkoutActivity.class);

        intent.putExtra("workoutTitle", currentWorkout.getTitle());
        intent.putExtra("workoutDescription", currentWorkout.getDescription());
        intent.putExtra("workoutDuration", currentWorkout.getDuration());
        intent.putExtra("workoutLevel", currentWorkout.getDifficulty());

        ArrayList<String> exercises = new ArrayList<>(
                currentWorkout.getExercises()
        );

        intent.putStringArrayListExtra("workoutExercises", exercises);

        startActivity(intent);
    }

    private String getValue(DocumentSnapshot document, String field, String defaultValue) {

        Object value = document.get(field);

        if (value == null) {
            return defaultValue;
        }

        return String.valueOf(value);
    }

    private void setUnitImage(String targetUnit) {

        if (targetUnit.contains("שייטת")) {
            imgUnit.setImageResource(R.drawable.shayetet);

        } else if (targetUnit.contains("מטכ")) {
            imgUnit.setImageResource(R.drawable.matkal);

        } else if (targetUnit.contains("שלדג")) {
            imgUnit.setImageResource(R.drawable.shaldag);

        } else if (targetUnit.contains("קומנדו")) {
            imgUnit.setImageResource(R.drawable.commando);
        }
    }
}
