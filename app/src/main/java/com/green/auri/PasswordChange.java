package com.green.auri;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// change password in settings
public class PasswordChange extends AppCompatActivity {
    private FirebaseUser user;

    private EditText edt_new_password;
    private Button btn_confirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_change);

        edt_new_password = (EditText) findViewById(R.id.edt_new_password);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String new_password = edt_new_password.getText().toString();
                // Error check the passwords
                if (new_password.length() < 6){
                    Toast.makeText(PasswordChange.this, "Please make sure it is an Email & Password must be at least 6 characters!",
                            Toast.LENGTH_LONG).show();
                }

                // Password change through Firebase Auth
                else {
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    user.updatePassword(new_password);
                    Toast.makeText(PasswordChange.this, "Password has been changed!",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
