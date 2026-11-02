
package com.example.readyfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    private EditText editAge;
    private EditText editWeight;
    private EditText editHeight;
    private EditText editRunTime;

    private Button btnContinue;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // אתחול Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // חיבור לרכיבי המסך
        editAge = findViewById(R.id.editAge);
        editWeight = findViewById(R.id.editWeight);
        editHeight = findViewById(R.id.editHeight);
        editRunTime = findViewById(R.id.editRunTime);
        btnContinue = findViewById(R.id.btnContinue);

        // טעינת נתונים קיימים
        loadUserData();

        // לחיצה על כפתור המשך
        btnContinue.setOnClickListener(v -> saveDataAndContinue());
    }

    // טעינת נתוני המשתמש מ-Firestore
    private void loadUserData() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "יש להתחבר מחדש", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        Object age = document.get("age");
                        Object weight = document.get("weight");
                        Object height = document.get("height");
                        Object runTime = document.get("runTime");

                        if (age != null) {
                            editAge.setText(String.valueOf(age));
                        }

                        if (weight != null) {
                            editWeight.setText(String.valueOf(weight));
                        }

                        if (height != null) {
                            editHeight.setText(String.valueOf(height));
                        }

                        if (runTime != null) {
                            editRunTime.setText(String.valueOf(runTime));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                MainActivity2.this,
                                "טעינת הנתונים נכשלה: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    // שמירת נתוני המשתמש והמשך למסך הבא
    private void saveDataAndContinue() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "יש להתחבר מחדש", Toast.LENGTH_SHORT).show();
            return;
        }

        String age = editAge.getText().toString().trim();
        String weight = editWeight.getText().toString().trim();
        String height = editHeight.getText().toString().trim();
        String runTime = editRunTime.getText().toString().trim();

        // בדיקה שכל השדות מלאים
        if (age.isEmpty() || weight.isEmpty() ||
                height.isEmpty() || runTime.isEmpty()) {

            Toast.makeText(
                    this,
                    "נא למלא את כל השדות",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        // יצירת הנתונים לשמירה
        Map<String, Object> data = new HashMap<>();

        data.put("age", age);
        data.put("weight", weight);
        data.put("height", height);
        data.put("runTime", runTime);
        data.put("updatedAt", FieldValue.serverTimestamp());

        btnContinue.setEnabled(false);

        // שמירה ב-Firestore
        db.collection("users")
                .document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            MainActivity2.this,
                            "הנתונים נשמרו בהצלחה",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent intent = new Intent(
                            MainActivity2.this,
                            TargetUnitActivity.class
                    );

                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {

                    btnContinue.setEnabled(true);

                    Toast.makeText(
                            MainActivity2.this,
                            "שמירת הנתונים נכשלה: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
