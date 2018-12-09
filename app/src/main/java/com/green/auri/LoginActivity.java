package com.green.auri;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.green.auri.onboarding.Intro;

public class LoginActivity extends AppCompatActivity {
    /* Constants */
    boolean isFirstStart;

    /* Components */
    private EditText password;
    private EditText email;
    private TextView button_register;
    private Button button_login;
    private NestedScrollView nestedScrollView;
    private SharedPreferences sp;
    private SharedPreferences getPrefs;

    /* Firebase */
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init_intro();

        // hide status bar
        getSupportActionBar().hide();

        // Initialize the views
        nestedScrollView = findViewById(R.id.nestedScrollView);
        email = findViewById(R.id.textInputEditTextEmail);
        password = findViewById(R.id.textInputEditTextPassword);
        button_login = findViewById(R.id.appCompatButtonLogin);
        button_register = findViewById(R.id.textViewLinkRegister);
        mAuth = FirebaseAuth.getInstance();

        // Share preference to save the login mode info
        sp = getSharedPreferences("login", MODE_PRIVATE);

        if(sp.getBoolean("logged",false)){
            Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
            intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP| Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent2);
            finish();
        }


        // Perform Log In
        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == button_login) {
                    LoginUser();
                }
            }
        });
    }

    // Perform the click Register textView link
    public void perform_action(View v)
    {
        if (v == button_register) {
            Intent intent1 = new Intent(LoginActivity.this, RegActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent1);
            finish();
        }
    }

    public void LoginUser(){ // Perform the Login task
        String Email = email.getText().toString();
        String Password = password.getText().toString();

        if (TextUtils.isEmpty(Email)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(Password)){
            Toast.makeText(this, "A Field is Empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // Use Firebase Auth to do the login task
        // Check if the username and password match with the ones in the Firebase
        mAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            currentUser = mAuth.getCurrentUser();

                            // saved account name, so that after login can get the account name
                            sp.edit().putString("account", currentUser.getUid()).apply();
                            sp.edit().putString("email", Email).apply();

                            // if login successful, then enter the main activity page
                            Intent intent2 = new Intent(LoginActivity.this, MainActivity.class);
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent2);
                            sp.edit().putBoolean("logged",true).apply();
                            finish();

                        }else {
                            // else toast that there's an error
                            Toast.makeText(LoginActivity.this, "Couldn't Log In, Please check your info!",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    void init_intro(){
        // Tutorial intro part of the app
        // Performs the first time the user downloaded the app and use it for the first time
        getPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        //  Create a new boolean and preference and set it to true
        isFirstStart = getPrefs.getBoolean("firstStart", true);

        //  Declare a new thread to do a preference check
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                //  If the activity has never started before...
                //  Launch app intro
                Intent i = new Intent(LoginActivity.this, Intro.class);
                startActivity(i);

                //  Make a new preferences editor
                SharedPreferences.Editor e = getPrefs.edit();

                //  Edit preference to make it false because we don't want this to run again
                e.putBoolean("firstStart", false);

                //  Apply changes
                e.apply();
            }
        });

        if(isFirstStart){
            t.start();
        }
    }

}
