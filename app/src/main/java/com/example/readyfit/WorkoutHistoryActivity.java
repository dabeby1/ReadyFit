
package com.example.readyfit;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class WorkoutHistoryActivity extends AppCompatActivity {

    private LinearLayout historyContainer;
    private TextView txtWorkoutCount;
    private TextView txtStatus;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_history);

        db = FirebaseFirestore.getInstance();

        historyContainer = findViewById(R.id.historyContainer);
        txtWorkoutCount = findViewById(R.id.txtWorkoutCount);
        txtStatus = findViewById(R.id.txtStatus);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            txtStatus.setText("יש להתחבר מחדש");
            return;
        }

        txtStatus.setText("טוען את היסטוריית האימונים...");

        db.collection("users")
                .document(user.getUid())
                .collection("workoutFeedback")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {

                    historyContainer.removeAllViews();

                    if (snapshot.isEmpty()) {
                        txtWorkoutCount.setText("מספר אימונים שהושלמו: 0");
                        txtStatus.setText("עדיין אין משובים שמורים");
                        return;
                    }

                    java.util.HashSet<String> workoutIds =
                            new java.util.HashSet<>();

                    int feedbackCount = 0;

                    for (com.google.firebase.firestore.DocumentSnapshot doc
                            : snapshot.getDocuments()) {

                        feedbackCount++;

                        String workoutId = doc.getString("workoutId");

                        if (workoutId != null && !workoutId.isEmpty()) {
                            workoutIds.add(workoutId);
                        }

                        addFeedbackCard(
                                doc.getString("exercise"),
                                doc.getString("difficulty"),
                                Boolean.TRUE.equals(doc.getBoolean("pain")),
                                doc.getString("dateText")
                        );
                    }

                    txtWorkoutCount.setText(
                            "מספר אימונים שהושלמו: " + workoutIds.size()
                    );

                    txtStatus.setText(
                            "נמצאו " + feedbackCount + " משובים"
                    );

                })
                .addOnFailureListener(e -> {
                    txtStatus.setText(
                            "טעינת ההיסטוריה נכשלה: " + e.getMessage()
                    );
                });
    }

    private void addFeedbackCard(
            String exercise,
            String difficulty,
            boolean pain,
            String date
    ) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(35, 28, 35, 28);
        card.setBackgroundColor(Color.rgb(43, 48, 39));

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(0, 0, 0, 22);
        card.setLayoutParams(cardParams);

        TextView exerciseText = createText(
                exercise == null ? "תרגיל ללא שם" : exercise,
                19,
                Color.WHITE
        );
        exerciseText.setGravity(Gravity.RIGHT);
        card.addView(exerciseText);

        TextView dateText = createText(
                date == null ? "תאריך לא זמין" : date,
                14,
                Color.LTGRAY
        );
        dateText.setGravity(Gravity.RIGHT);
        card.addView(dateText);

        String difficultyValue =
                difficulty == null ? "לא צוין" : difficulty;

        TextView difficultyText = createText(
                "רמת קושי: " + difficultyValue,
                16,
                Color.rgb(166, 196, 103)
        );
        difficultyText.setGravity(Gravity.RIGHT);
        card.addView(difficultyText);

        TextView painText = createText(
                pain ? "⚠ דווח כאב" : "✓ לא דווח כאב",
                16,
                pain ? Color.rgb(255, 100, 85) : Color.rgb(140, 210, 130)
        );
        painText.setGravity(Gravity.RIGHT);
        card.addView(painText);

        historyContainer.addView(card);
    }

    private TextView createText(String text, int size, int color) {

        TextView view = new TextView(this);
        view.setText(text);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setPadding(0, 7, 0, 7);

        return view;
    }
}
