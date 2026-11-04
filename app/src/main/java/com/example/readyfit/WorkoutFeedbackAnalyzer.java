
package com.example.readyfit;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkoutFeedbackAnalyzer {

    public interface AnalysisCallback {
        void onSuccess(String recommendation, String explanation);
        void onFailure(String error);
    }

    public static void analyzeLatestFeedback(AnalysisCallback callback) {

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            callback.onFailure("אין משתמש מחובר");
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("workoutFeedback")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        callback.onSuccess(
                                "אין מספיק נתונים",
                                "בצע כמה אימונים ותן משוב על התרגילים."
                        );
                        return;
                    }

                    int easyCount = 0;
                    int suitableCount = 0;
                    int hardCount = 0;
                    int painCount = 0;

                    List<String> painExercises = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        String difficulty = doc.getString("difficulty");
                        String exercise = doc.getString("exercise");

                        Boolean pain = doc.getBoolean("pain");

                        if ("קל".equals(difficulty)) {
                            easyCount++;
                        } else if ("מתאים".equals(difficulty)) {
                            suitableCount++;
                        } else if ("קשה".equals(difficulty)) {
                            hardCount++;
                        }

                        if (Boolean.TRUE.equals(pain)) {
                            painCount++;

                            if (exercise != null &&
                                    !painExercises.contains(exercise)) {
                                painExercises.add(exercise);
                            }
                        }
                    }

                    int total = snapshot.size();

                    // כאב מקבל עדיפות על כל המלצה להעלאת עומס
                    if (painCount > 0) {

                        String exercises = String.join(", ", painExercises);

                        callback.onSuccess(
                                "עצירת העלאת עומס",
                                "נמצאו " + painCount +
                                        " דיווחי כאב בעשרת המשובים האחרונים. " +
                                        "אין להעלות עומס. יש להפסיק את התרגילים " +
                                        "הקשורים לכאב ולברר את הסיבה לפני חזרה אליהם. " +
                                        "תרגילים שדווח בהם כאב: " + exercises
                        );

                        return;
                    }

                    // בדיקה אם רוב המשובים קשים
                    if (hardCount >= 4 && hardCount > easyCount) {

                        callback.onSuccess(
                                "הפחתת עומס",
                                "נמצאו " + hardCount +
                                        " משובים קשים מתוך " + total +
                                        ". מומלץ להוריד מעט את העומס " +
                                        "ולבדוק שוב את התחושה באימונים הבאים."
                        );

                        return;
                    }

                    // בדיקה אם רוב המשובים קלים
                    if (easyCount >= 7 && easyCount > hardCount) {

                        callback.onSuccess(
                                "אפשר לשקול התקדמות",
                                "נמצאו " + easyCount +
                                        " משובים קלים מתוך " + total +
                                        ". אפשר לשקול תוספת קטנה והדרגתית " +
                                        "בעומס, בתנאי שאין כאב ויש התאוששות טובה."
                        );

                        return;
                    }

                    // ברירת מחדל
                    callback.onSuccess(
                            "שמירה על הרמה",
                            "נמצאו " + suitableCount +
                                    " משובים מתאימים, " + easyCount +
                                    " קלים ו-" + hardCount +
                                    " קשים. מומלץ להמשיך ברמה הנוכחית " +
                                    "ולאסוף משובים נוספים."
                    );

                })
                .addOnFailureListener(e ->
                        callback.onFailure(
                                "שגיאה בניתוח המשובים: " + e.getMessage()
                        )
                );
    }
}
