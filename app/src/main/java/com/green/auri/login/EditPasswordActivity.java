package com.green.auri.login;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.green.auri.R;

// change password in settings
public class EditPasswordActivity extends AppCompatActivity {
    private static final int MIN_PASSWORD_LENGTH = 6;

    private FirebaseUser user;
    private EditText edt_new_password;
    private Button btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        edt_new_password = (EditText) findViewById(R.id.edt_new_password);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(v -> {
            String new_password = edt_new_password.getText().toString();

            // Error check the passwords
            if (new_password.length() < MIN_PASSWORD_LENGTH){
                Toast.makeText(EditPasswordActivity.this, "Please make sure it is an Email & Password must be at least " + MIN_PASSWORD_LENGTH + " characters!",
                        Toast.LENGTH_LONG).show();
            }

            // Password change through Firebase Auth
            else {
                user = FirebaseAuth.getInstance().getCurrentUser();
                user.updatePassword(new_password);
                Toast.makeText(EditPasswordActivity.this, "Password has been changed!",
                        Toast.LENGTH_LONG).show();
            }
        });
    }
}
