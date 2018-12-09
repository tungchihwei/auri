package com.green.auri;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.green.auri.arview.ARActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SettingsActivity extends AppCompatActivity {
//    private FirebaseUser user;
    private TextView logout;
    private TextView account;
    private SharedPreferences sp;
    private String email;
    private ImageView profile_image;
    private TextView changeProfile;
    private static final int PICK_IMAGE = 1;
    private String photo_toString;
    private TextView txt_change_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar
        getSupportActionBar().setTitle("Settings");

        setContentView(R.layout.activity_settings);

        sp = getSharedPreferences("login",MODE_PRIVATE);

        logout = findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

        Intent i = getIntent();
        email = i.getStringExtra("email");
        TextView account = findViewById(R.id.account);
        account.setText(email);

        // show image gallery and upload on firebase
        profile_image = findViewById(R.id.profile_image);
        String photo = sp.getString("profile","");
        if (!photo.equals("")){
            byte[] encodeByte = Base64.decode(photo, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            profile_image.setImageBitmap(bitmap);
        }

        changeProfile = findViewById(R.id.changeProfile);
        changeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_PICK);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
            }
        });

        txt_change_password = findViewById(R.id.txt_change_password);
        txt_change_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(SettingsActivity.this, PasswordChange.class);
                startActivity(i);
            }
        });
    }

    private void logout(){
        // sign out of this user and go to the log in page
        Log.i("!!!logout", String.valueOf(FirebaseAuth.getInstance()));
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
