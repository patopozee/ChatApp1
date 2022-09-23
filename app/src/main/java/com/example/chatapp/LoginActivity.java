package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn, phoneLoginBtn;
    private EditText userEmail, userPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.Grey));
        }

        mAuth = FirebaseAuth.getInstance();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");

        InitializeFields();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });

    }

    private void AllowUserToLogin() {
        String email = userEmail.getText().toString();
        String password = userPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email...",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Password can't be empty..",Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Signing in...");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();
            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                          if (task.isSuccessful()) {
                              String currentUserID = mAuth.getCurrentUser().getUid();
                              FirebaseMessaging.getInstance().getToken()
                                              .addOnCompleteListener(new OnCompleteListener<String>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<String> task) {
                                                      String deviceToken = task.getResult();
                                                      UsersRef.child(currentUserID).child("device_token")
                                                              .setValue(deviceToken)
                                                              .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                  @Override
                                                                  public void onComplete(@NonNull Task<Void> task) {
                                                                      if (task.isSuccessful()){
                                                                          Toast.makeText(LoginActivity.this,"Logged in successfully...",Toast.LENGTH_SHORT).show();
                                                                          Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                                                          i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                          startActivity(i);
                                                                          finish();
                                                                          loadingBar.dismiss();
                                                                      }
                                                                  }
                                                              });
                                                  }
                                              });




                          }
                          else {
                              String message = task.getException().toString();
                              Toast.makeText(LoginActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();
                              loadingBar.dismiss();
                          }
                        }
                    });
        }
    }

    private void InitializeFields() {
        loginBtn = findViewById(R.id.login_button);
        phoneLoginBtn = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainActivity);
        finish();
    }
    public void signup_link(View view){
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);

    }

    public void PhoneLogin (View view){
        Intent i = new Intent(LoginActivity.this, PhoneLoginActivity.class);
        startActivity(i);
        finish();
    }

}