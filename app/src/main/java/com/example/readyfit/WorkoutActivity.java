
package com.example.readyfit;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WorkoutActivity extends AppCompatActivity {

    private TextView txtWorkoutName, txtWorkoutLevel;
    private TextView txtWorkoutDuration, txtWarmup, txtCooldown;
    private TextView txtExerciseName, txtExerciseTime;
    private TextView txtExerciseDetails, txtExerciseProgress;
    private TextView txtStopwatch;

    private Button btnNextExercise, btnFinishWorkout;

    private ArrayList<String> exercises;
    private int currentExercise = 0;
    private int totalMinutes = 15;

    private Handler handler = new Handler(Looper.getMainLooper());
    private long startTime;
    private long elapsedBeforePause = 0;
    private boolean stopwatchRunning = false;
    private boolean savingFeedback = false;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String workoutId = String.valueOf(System.currentTimeMillis());

    private final Runnable stopwatchRunnable = new Runnable() {
        @Override
        public void run() {
            if (stopwatchRunning) {
                long elapsed = SystemClock.elapsedRealtime()
                        - startTime + elapsedBeforePause;

                int seconds = (int) (elapsed / 1000);
                int minutes = seconds / 60;
                seconds %= 60;

                txtStopwatch.setText(String.format(
                        Locale.getDefault(), "%02d:%02d",
                        minutes, seconds));

                handler.postDelayed(this, 1000);
            }
        }
    };

    private static class ExerciseInfo {
        String details;
        int minutes;

        ExerciseInfo(String details, int minutes) {
            this.details = details;
            this.minutes = minutes;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        txtWorkoutName = findViewById(R.id.txtWorkoutName);
        txtWorkoutLevel = findViewById(R.id.txtWorkoutLevel);
        txtWorkoutDuration = findViewById(R.id.txtWorkoutDuration);
        txtWarmup = findViewById(R.id.txtWarmup);
        txtCooldown = findViewById(R.id.txtCooldown);
        txtExerciseName = findViewById(R.id.txtExerciseName);
        txtExerciseTime = findViewById(R.id.txtExerciseTime);
        txtExerciseDetails = findViewById(R.id.txtExerciseDetails);
        txtExerciseProgress = findViewById(R.id.txtExerciseProgress);
        txtStopwatch = findViewById(R.id.txtStopwatch);

        btnNextExercise = findViewById(R.id.btnNextExercise);
        btnFinishWorkout = findViewById(R.id.btnFinishWorkout);

        String title = getIntent().getStringExtra("workoutTitle");
        String description = getIntent().getStringExtra("workoutDescription");
        String level = getIntent().getStringExtra("workoutLevel");

        exercises = getIntent().getStringArrayListExtra("workoutExercises");

        if (exercises == null || exercises.isEmpty()) {
            Toast.makeText(this, "לא נמצאו תרגילים באימון",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        txtWorkoutName.setText(title == null ? "אימון ReadyFit" : title);

        txtWorkoutLevel.setText(
                (level == null ? "" : level) + "\n" +
                        (description == null ? "" : description));

        txtWarmup.setText(
                "🔥 חימום – 10 דקות\n\n" +
                        "• 3 דקות הליכה מהירה או ריצה קלה\n" +
                        "• 3 דקות תנועתיות מפרקים\n" +
                        "• 2 דקות תנועות דינמיות\n" +
                        "• 2 דקות הגברת קצב הדרגתית");

        txtCooldown.setText(
                "בסיום האימון:\n" +
                        "5 דקות הליכה קלה ומתיחות עדינות.");

        calculateTotalDuration();
        showExercise();
        startStopwatch();

        btnNextExercise.setText("סיום התרגיל ומתן משוב");

        btnNextExercise.setOnClickListener(v -> {
            if (!savingFeedback) {
                showFeedbackDialog();
            }
        });

        btnFinishWorkout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("סיום האימון")
                    .setMessage("לסיים את האימון עכשיו בלי משוב לתרגיל הנוכחי?")
                    .setPositiveButton("כן, סיים", (dialog, which) -> {
                        stopStopwatch();
                        finish();
                    })
                    .setNegativeButton("המשך אימון", null)
                    .show();
        });
    }

    private void startStopwatch() {
        if (!stopwatchRunning) {
            startTime = SystemClock.elapsedRealtime();
            stopwatchRunning = true;
            handler.post(stopwatchRunnable);
        }
    }

    private void stopStopwatch() {
        if (stopwatchRunning) {
            elapsedBeforePause += SystemClock.elapsedRealtime() - startTime;
            stopwatchRunning = false;
            handler.removeCallbacks(stopwatchRunnable);
        }
    }

    private void calculateTotalDuration() {
        totalMinutes = 15;

        for (String exercise : exercises) {
            totalMinutes += getExerciseInfo(exercise).minutes;
        }

        txtWorkoutDuration.setText(totalMinutes + " דקות");
    }

    private void showExercise() {
        String exercise = exercises.get(currentExercise);
        ExerciseInfo info = getExerciseInfo(exercise);

        txtExerciseName.setText(exercise);
        txtExerciseDetails.setText(info.details);
        txtExerciseTime.setText("משך משוער: " + info.minutes + " דקות");

        txtExerciseProgress.setText(
                "תרגיל " + (currentExercise + 1) +
                        " מתוך " + exercises.size());

        btnNextExercise.setText("סיום התרגיל ומתן משוב");
    }

    private void showFeedbackDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(35, 15, 35, 10);

        TextView question = new TextView(this);
        question.setText("איך הרגיש לך התרגיל?");
        question.setTextSize(18);
        question.setTextColor(Color.BLACK);
        layout.addView(question);

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(RadioGroup.VERTICAL);

        RadioButton easy = new RadioButton(this);
        easy.setText("קל");
        easy.setId(View.generateViewId());

        RadioButton suitable = new RadioButton(this);
        suitable.setText("מתאים");
        suitable.setId(View.generateViewId());

        RadioButton hard = new RadioButton(this);
        hard.setText("קשה");
        hard.setId(View.generateViewId());

        group.addView(easy);
        group.addView(suitable);
        group.addView(hard);
        layout.addView(group);

        CheckBox pain = new CheckBox(this);
        pain.setText("הרגשתי כאב במהלך התרגיל");
        pain.setTextColor(Color.BLACK);
        layout.addView(pain);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("משוב על התרגיל")
                .setView(layout)
                .setPositiveButton("שליחת משוב", null)
                .setNegativeButton("ביטול", null)
                .create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        int selected = group.getCheckedRadioButtonId();

                        if (selected == -1) {
                            Toast.makeText(this,
                                    "בחר אם התרגיל היה קל, מתאים או קשה",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        RadioButton selectedButton =
                                group.findViewById(selected);

                        saveFeedback(
                                selectedButton.getText().toString(),
                                pain.isChecked(),
                                dialog);
                    });
        });

        dialog.show();
    }

    private void saveFeedback(String difficulty, boolean pain,
                              AlertDialog dialog) {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            new AlertDialog.Builder(this)
                    .setTitle("אין משתמש מחובר")
                    .setMessage("צריך להתחבר מחדש כדי לשמור את המשוב.")
                    .setPositiveButton("אישור", null)
                    .show();
            return;
        }

        savingFeedback = true;
        btnNextExercise.setEnabled(false);
        btnFinishWorkout.setEnabled(false);

        String dateText = new SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
        ).format(new Date());

        Map<String, Object> feedback = new HashMap<>();

        feedback.put("exercise", exercises.get(currentExercise));
        feedback.put("difficulty", difficulty);
        feedback.put("pain", pain);
        feedback.put("exerciseNumber", currentExercise + 1);
        feedback.put("workoutId", workoutId);
        feedback.put("date", FieldValue.serverTimestamp());
        feedback.put("dateText", dateText);
        feedback.put("createdAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(user.getUid())
                .collection("workoutFeedback")
                .add(feedback)
                .addOnSuccessListener(reference -> {

                    dialog.dismiss();
                    savingFeedback = false;

                    if (pain) {
                        stopStopwatch();

                        new AlertDialog.Builder(this)
                                .setTitle("דיווח על כאב נשמר")
                                .setMessage("האימון נעצר. אל תמשיך בתרגיל שגרם לכאב.")
                                .setPositiveButton("אישור", (d, w) -> finish())
                                .setCancelable(false)
                                .show();
                        return;
                    }

                    btnNextExercise.setEnabled(true);
                    btnFinishWorkout.setEnabled(true);

                    if (currentExercise < exercises.size() - 1) {
                        currentExercise++;
                        showExercise();
                    } else {
                        stopStopwatch();

                        // סימון האימון כהושלם וביטול התזכורות להיום
                        WorkoutReminderScheduler.markWorkoutCompleted(this);

                        Toast.makeText(this,
                                "כל הכבוד! סיימת את האימון והמשובים נשמרו.",
                                Toast.LENGTH_LONG).show();

                        finish();
                    }
                })
                .addOnFailureListener(e -> {

                    savingFeedback = false;
                    btnNextExercise.setEnabled(true);
                    btnFinishWorkout.setEnabled(true);

                    String errorMessage = e.getMessage();

                    if (errorMessage == null) {
                        errorMessage = "לא התקבל פירוט מהשרת.";
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("שגיאה בשמירת המשוב")
                            .setMessage(
                                    "פרטי השגיאה:\n\n" + errorMessage +
                                            "\n\nקוד שגיאה:\n" +
                                            e.getClass().getSimpleName())
                            .setPositiveButton("אישור", null)
                            .show();
                });
    }

    private ExerciseInfo getExerciseInfo(String name) {

        String n = name.toLowerCase();

        if (n.contains("שחיי") || n.contains("במים")) {
            return new ExerciseInfo(
                    "מטרה: שיפור טכניקת השחייה.\n\n" +
                            "• בצע 4 בריכות קצרות בקצב קל.\n" +
                            "• התמקד בתנועת ידיים ובתנוחת גוף.\n" +
                            "• נוח 30–45 שניות בין הבריכות.\n" +
                            "• בצע בבריכה ובפיקוח מדריך.\n" +
                            "• אין לבצע עצירות נשימה או תרגילי צלילה.",
                    8);
        }

        if (n.contains("ריצ") || n.contains("אינטרוול") ||
                n.contains("ספרינט") || n.contains("האצה")) {
            return new ExerciseInfo(
                    "מטרה: שיפור סיבולת ומהירות.\n\n" +
                            "• בריצה קלה שמור על קצב שמאפשר לדבר.\n" +
                            "• באינטרוולים בצע 4 חזרות של דקה בקצב מהיר אך נשלט.\n" +
                            "• נוח בהליכה 90 שניות בין החזרות.",
                    10);
        }

        if (n.contains("שכיב")) {
            return new ExerciseInfo(
                    "מטרה: חיזוק החזה והידיים.\n\n" +
                            "• בצע 3 סטים של 8–12 חזרות.\n" +
                            "• שמור על גוף ישר.\n" +
                            "• נוח 60–90 שניות בין הסטים.",
                    6);
        }

        if (n.contains("מתח") || n.contains("חתירה") ||
                n.contains("משיכה")) {
            return new ExerciseInfo(
                    "מטרה: חיזוק הגב והידיים.\n\n" +
                            "• בצע 3 סטים של 6–10 חזרות.\n" +
                            "• השתמש בגומייה או בעזרה אם צריך.\n" +
                            "• נוח 90 שניות בין הסטים.",
                    7);
        }

        if (n.contains("סקוואט") || n.contains("כריע") ||
                n.contains("מכרע") || n.contains("לאנג")) {
            return new ExerciseInfo(
                    "מטרה: חיזוק הרגליים.\n\n" +
                            "• בצע 3 סטים של 10–12 חזרות.\n" +
                            "• שמור על גב ניטרלי.\n" +
                            "• נוח 60–90 שניות בין הסטים.",
                    7);
        }

        if (n.contains("פלאנק") || n.contains("plank")) {
            return new ExerciseInfo(
                    "מטרה: חיזוק שרירי הליבה.\n\n" +
                            "• בצע 3 סטים של 20–40 שניות.\n" +
                            "• שמור על גוף ישר ונשימה רגילה.\n" +
                            "• נוח 45–60 שניות בין הסטים.",
                    5);
        }

        if (n.contains("סחיב") || n.contains("נשיא") ||
                n.contains("farmer") || n.contains("תיק")) {
            return new ExerciseInfo(
                    "מטרה: שיפור כוח אחיזה ויציבות.\n\n" +
                            "• בצע 3 סבבים של 20 מטר עם משקל קל.\n" +
                            "• שמור על גב יציב.\n" +
                            "• נוח 60–90 שניות בין הסבבים.",
                    7);
        }

        if (n.contains("זריז") || n.contains("קונוס") ||
                n.contains("שינוי כיוון") || n.contains("סולם")) {
            return new ExerciseInfo(
                    "מטרה: שיפור זריזות.\n\n" +
                            "• בצע 4 סבבים של 20 שניות.\n" +
                            "• עבוד בתנועות קצרות ומבוקרות.\n" +
                            "• נוח 40 שניות בין הסבבים.",
                    6);
        }

        if (n.contains("זחיל") || n.contains("bear walk") ||
                n.contains("הליכת דוב")) {
            return new ExerciseInfo(
                    "מטרה: שיפור כוח ושליטה בגוף.\n\n" +
                            "• בצע 3 סבבים של 10 מטר.\n" +
                            "• שמור על תנועה מבוקרת.\n" +
                            "• נוח 60 שניות בין הסבבים.",
                    6);
        }

        if (n.contains("מתיח") || n.contains("שחרור") ||
                n.contains("התאוששות") || n.contains("הליכה קלה")) {
            return new ExerciseInfo(
                    "מטרה: התאוששות.\n\n" +
                            "• בצע את התנועה באיטיות וללא כאב.\n" +
                            "• שמור על נשימה רגועה.",
                    5);
        }

        if (n.contains("dead bug") || n.contains("bird dog") ||
                n.contains("גשר") || n.contains("יציב")) {
            return new ExerciseInfo(
                    "מטרה: חיזוק שרירי הליבה.\n\n" +
                            "• בצע 3 סטים של 8–10 חזרות לכל צד.\n" +
                            "• בצע את התנועה באיטיות.\n" +
                            "• נוח 45–60 שניות בין הסטים.",
                    6);
        }

        return new ExerciseInfo(
                "מטרה: שיפור הכושר הגופני הכללי.\n\n" +
                        "• בצע 2–3 סטים של 8–12 חזרות או 30 שניות עבודה.\n" +
                        "• שמור על טכניקה נכונה.\n" +
                        "• נוח 60 שניות בין הסטים.\n" +
                        "• עצור אם מופיע כאב או סחרחורת.",
                5);
    }

    @Override
    protected void onDestroy() {
        stopwatchRunning = false;
        handler.removeCallbacks(stopwatchRunnable);
        super.onDestroy();
    }
}
