package com.green.auri;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.green.auri.login.LoginActivity;
import com.green.auri.login.EditPasswordActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
    /* Constants */
    private static final int PICK_IMAGE = 1;
    private String email;
    private String photo_toString;

    /* Components */
    private TextView logout;
    private TextView account;
    private TextView changeProfile;
    private TextView txt_change_password;
    private SharedPreferences sp;
    private ImageView profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set status bar
        getSupportActionBar().setTitle(R.string.settings);

        setContentView(R.layout.activity_settings);

        sp = getSharedPreferences("login", MODE_PRIVATE);

        Intent i = getIntent();
        email = i.getStringExtra("email");
        TextView account = findViewById(R.id.account);
        account.setText(email);

        // log out
        logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> logout());

        // show image gallery and upload the firebase
        profile_image = findViewById(R.id.profile_image);
        String photo = sp.getString("profile","");
        if (!photo.equals("")){
            byte[] encodeByte = Base64.decode(photo, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            profile_image.setImageBitmap(bitmap);
        }

        changeProfile = findViewById(R.id.changeProfile);
        changeProfile.setOnClickListener(v -> {
            Intent pictureIntent = new Intent();
            pictureIntent.setType("image/*");
            pictureIntent.setAction(Intent.ACTION_PICK);
            startActivityForResult(Intent.createChooser(pictureIntent, "Select Picture"), PICK_IMAGE);
        });

        // change the password
        txt_change_password = findViewById(R.id.txt_change_password);
        txt_change_password.setOnClickListener(v -> {
            Intent i1 = new Intent(SettingsActivity.this, EditPasswordActivity.class);
            startActivity(i1);
        });
    }

    private void logout(){
        // sign out of this user and go to the log in page
        FirebaseAuth.getInstance().signOut();
        Intent logoutIntent = new Intent(SettingsActivity.this, LoginActivity.class);
        logoutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(logoutIntent);
        sp.edit().putBoolean("logged",false).apply();
        finish();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                        profile_image.setImageBitmap(bitmap);

                        // change photo bitmap to string
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.PNG, 10, baos);
                        byte[] b = baos.toByteArray();
                        photo_toString = Base64.encodeToString(b, Base64.DEFAULT);
                        sp.edit().putString("profile", photo_toString).apply();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == RESULT_CANCELED)  {
                Toast.makeText(getBaseContext(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
