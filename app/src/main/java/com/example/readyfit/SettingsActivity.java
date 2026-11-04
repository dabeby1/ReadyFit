
package com.example.readyfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    private EditText editAge;
    private EditText editWeight;
    private EditText editHeight;
    private EditText editRunTime;

    private Spinner spinnerTargetUnit;

    private Button btnSave;
    private Button btnBack;
    private Button btnLogout;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private final String[] units = {
            "שייטת 13",
            "סיירת מטכ״ל",
            "שלדג",
            "חטיבת הקומנדו"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editAge = findViewById(R.id.editSettingsAge);
        editWeight = findViewById(R.id.editSettingsWeight);
        editHeight = findViewById(R.id.editSettingsHeight);
        editRunTime = findViewById(R.id.editSettingsRunTime);

        spinnerTargetUnit = findViewById(R.id.spinnerTargetUnit);

        btnSave = findViewById(R.id.btnSaveSettings);
        btnBack = findViewById(R.id.btnBackHome);
        btnLogout = findViewById(R.id.btnLogout);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                units
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerTargetUnit.setAdapter(adapter);

        loadUserData();

        btnSave.setOnClickListener(v -> saveChanges());

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {

            auth.signOut();

            Intent intent = new Intent(
                    SettingsActivity.this,
                    MainActivity.class
            );

            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK
            );

            startActivity(intent);
            finish();
        });
    }

    private String getUserId() {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            return null;
        }

        return user.getUid();
    }

    // טעינת הנתונים מ-Firestore
    private void loadUserData() {

        String uid = getUserId();

        if (uid == null) {
            Toast.makeText(
                    this,
                    "לא נמצא משתמש מחובר",
                    Toast.LENGTH_SHORT
            ).show();
            finish();
            return;
        }

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        Toast.makeText(
                                this,
                                "לא נמצאו נתוני משתמש",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    editAge.setText(
                            getValue(document.get("age"))
                    );

                    editWeight.setText(
                            getValue(document.get("weight"))
                    );

                    editHeight.setText(
                            getValue(document.get("height"))
                    );

                    editRunTime.setText(
                            getValue(document.get("runTime"))
                    );

                    String targetUnit = getValue(
                            document.get("targetUnit")
                    );

                    for (int i = 0; i < units.length; i++) {

                        if (units[i].equals(targetUnit)) {
                            spinnerTargetUnit.setSelection(i);
                            break;
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "טעינת הנתונים נכשלה: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    private String getValue(Object value) {

        if (value == null) {
            return "";
        }

        return String.valueOf(value);
    }

    // שמירת הנתונים ב-Firestore
    private void saveChanges() {

        String uid = getUserId();

        if (uid == null) {
            Toast.makeText(
                    this,
                    "לא נמצא משתמש מחובר",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        String age = editAge.getText().toString().trim();
        String weight = editWeight.getText().toString().trim();
        String height = editHeight.getText().toString().trim();
        String runTime = editRunTime.getText().toString().trim();

        String targetUnit = spinnerTargetUnit
                .getSelectedItem()
                .toString();

        if (age.isEmpty() ||
                weight.isEmpty() ||
                height.isEmpty() ||
                runTime.isEmpty()) {

            Toast.makeText(
                    this,
                    "יש למלא את כל הנתונים",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnSave.setEnabled(false);

        Map<String, Object> data = new HashMap<>();

        data.put("age", age);
        data.put("weight", weight);
        data.put("height", height);
        data.put("runTime", runTime);
        data.put("targetUnit", targetUnit);
        data.put("updatedAt", FieldValue.serverTimestamp());

        db.collection("users")
                .document(uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            SettingsActivity.this,
                            "הנתונים עודכנו בהצלחה!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e -> {

                    btnSave.setEnabled(true);

                    Toast.makeText(
                            SettingsActivity.this,
                            "שמירת הנתונים נכשלה: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}
