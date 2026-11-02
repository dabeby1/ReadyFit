package com.example.readyfit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class TargetUnitActivity extends AppCompatActivity {

    private TextView cardShayetet, cardMatkal, cardShaldag, cardCommando;
    private TextView txtSelectedUnit;
    private Button btnContinueHome;

    private String selectedUnit = "";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_target_unit);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        cardShayetet = findViewById(R.id.cardShayetet);
        cardMatkal = findViewById(R.id.cardMatkal);
        cardShaldag = findViewById(R.id.cardShaldag);
        cardCommando = findViewById(R.id.cardCommando);
        txtSelectedUnit = findViewById(R.id.txtSelectedUnit);
        btnContinueHome = findViewById(R.id.btnContinueHome);

        loadSelectedUnit();

        cardShayetet.setOnClickListener(v -> selectUnit("שייטת 13", cardShayetet));
        cardMatkal.setOnClickListener(v -> selectUnit("סיירת מטכ״ל", cardMatkal));
        cardShaldag.setOnClickListener(v -> selectUnit("שלדג", cardShaldag));
        cardCommando.setOnClickListener(v -> selectUnit("חטיבת הקומנדו", cardCommando));

        btnContinueHome.setOnClickListener(v -> saveUnitAndContinue());
    }

    private void selectUnit(String unit, TextView selectedCard) {
        selectedUnit = unit;

        resetCardColors();
        selectedCard.setBackgroundColor(
                getResources().getColor(R.color.olive_green)
        );

        txtSelectedUnit.setText("היעד שלך: " + unit);
    }

    private void resetCardColors() {
        int defaultColor = getResources().getColor(android.R.color.darker_gray);

        cardShayetet.setBackgroundColor(defaultColor);
        cardMatkal.setBackgroundColor(defaultColor);
        cardShaldag.setBackgroundColor(defaultColor);
        cardCommando.setBackgroundColor(defaultColor);
    }

    private void loadSelectedUnit() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            txtSelectedUnit.setText("בחר אחת מהיחידות");
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String savedUnit = document.getString("targetUnit");

                        if (savedUnit != null && !savedUnit.isEmpty()) {
                            selectedUnit = savedUnit;
                            txtSelectedUnit.setText("היעד שלך: " + savedUnit);
                            showSelectedCard(savedUnit);
                        } else {
                            txtSelectedUnit.setText("בחר אחת מהיחידות");
                        }
                    } else {
                        txtSelectedUnit.setText("בחר אחת מהיחידות");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "לא הצלחנו לטעון את היחידה", Toast.LENGTH_SHORT).show()
                );
    }

    private void showSelectedCard(String unit) {
        resetCardColors();

        if (unit.equals("שייטת 13")) {
            cardShayetet.setBackgroundColor(getResources().getColor(R.color.olive_green));
        } else if (unit.equals("סיירת מטכ״ל")) {
            cardMatkal.setBackgroundColor(getResources().getColor(R.color.olive_green));
        } else if (unit.equals("שלדג")) {
            cardShaldag.setBackgroundColor(getResources().getColor(R.color.olive_green));
        } else if (unit.equals("חטיבת הקומנדו")) {
            cardCommando.setBackgroundColor(getResources().getColor(R.color.olive_green));
        }
    }

    private void saveUnitAndContinue() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "יש להתחבר מחדש", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUnit.isEmpty()) {
            Toast.makeText(this, "בחר יחידה לפני שממשיכים", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("targetUnit", selectedUnit);
        data.put("updatedAt", FieldValue.serverTimestamp());

        btnContinueHome.setEnabled(false);

        db.collection("users")
                .document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Intent intent = new Intent(TargetUnitActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnContinueHome.setEnabled(true);
                    Toast.makeText(this, "שמירת היחידה נכשלה: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}