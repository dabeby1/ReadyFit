
package com.example.readyfit;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private EditText editEmail;
    private EditText editPassword;

    private Button btnLogin;
    private Button btnOpenRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);

        btnLogin = findViewById(R.id.btnLogin);
        btnOpenRegister = findViewById(R.id.btnOpenRegister);

        btnLogin.setOnClickListener(v -> loginUser());

        btnOpenRegister.setOnClickListener(v -> openRegisterDialog());
    }

    // התחברות למשתמש קיים
    private void loginUser() {

        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "יש למלא אימייל וסיסמה",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        Toast.makeText(this, "התחברת בהצלחה",
                                Toast.LENGTH_SHORT).show();

                        checkUserData();

                    } else {

                        btnLogin.setEnabled(true);

                        Toast.makeText(this,
                                "האימייל או הסיסמה שגויים",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // בדיקת הנתונים של המשתמש ב-Firestore
    private void checkUserData() {

        if (auth.getCurrentUser() == null) {
            return;
        }

        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(document -> {

                    if (!document.exists()) {
                        openPersonalDataScreen();
                        return;
                    }

                    String age = document.getString("age");
                    String weight = document.getString("weight");
                    String height = document.getString("height");
                    String runTime = document.getString("runTime");
                    String targetUnit = document.getString("targetUnit");

                    boolean hasPersonalData =
                            age != null && !age.isEmpty()
                                    && weight != null && !weight.isEmpty()
                                    && height != null && !height.isEmpty()
                                    && runTime != null && !runTime.isEmpty();

                    boolean hasTargetUnit =
                            targetUnit != null && !targetUnit.isEmpty();

                    if (hasPersonalData && hasTargetUnit) {

                        openHomeScreen();

                    } else if (hasPersonalData) {

                        openTargetUnitScreen();

                    } else {

                        openPersonalDataScreen();
                    }
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(this,
                            "לא הצלחנו לבדוק את הנתונים: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();

                    if (btnLogin != null) {
                        btnLogin.setEnabled(true);
                    }
                });
    }

    // פתיחת חלונית הרשמה
    private void openRegisterDialog() {

        Dialog dialog = new Dialog(MainActivity.this);

        dialog.setContentView(R.layout.dialog_register);

        EditText registerEmail =
                dialog.findViewById(R.id.registerEmail);

        EditText registerPassword =
                dialog.findViewById(R.id.registerPassword);

        EditText registerConfirmPassword =
                dialog.findViewById(R.id.registerConfirmPassword);

        Button btnRegister =
                dialog.findViewById(R.id.btnRegister);

        Button btnCancel =
                dialog.findViewById(R.id.btnCancel);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnRegister.setOnClickListener(v -> {

            String email = registerEmail.getText().toString().trim();
            String password = registerPassword.getText().toString();
            String confirmPassword =
                    registerConfirmPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()
                    || confirmPassword.isEmpty()) {

                Toast.makeText(this, "יש למלא את כל השדות",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {

                Toast.makeText(this, "הסיסמאות אינן זהות",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {

                Toast.makeText(this,
                        "הסיסמה חייבת להכיל לפחות 6 תווים",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            btnRegister.setEnabled(false);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(this, "נרשמת בהצלחה",
                                    Toast.LENGTH_SHORT).show();

                            dialog.dismiss();

                            checkUserData();

                        } else {

                            btnRegister.setEnabled(true);

                            String error = "ההרשמה נכשלה";

                            if (task.getException() != null) {
                                error = task.getException().getMessage();
                            }

                            Toast.makeText(this, error,
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    // מעבר למסך הנתונים האישיים
    private void openPersonalDataScreen() {

        Intent intent = new Intent(
                MainActivity.this,
                MainActivity2.class
        );

        startActivity(intent);
        finish();
    }

    // מעבר למסך בחירת היחידה
    private void openTargetUnitScreen() {

        Intent intent = new Intent(
                MainActivity.this,
                TargetUnitActivity.class
        );

        startActivity(intent);
        finish();
    }

    // מעבר למסך הבית
    private void openHomeScreen() {

        Intent intent = new Intent(
                MainActivity.this,
                HomeActivity.class
        );

        startActivity(intent);
        finish();
    }
}
