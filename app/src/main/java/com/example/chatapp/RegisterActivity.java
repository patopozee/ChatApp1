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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class RegisterActivity extends AppCompatActivity {

    private Button registerBtn, phoneBtn;
    private EditText registerEmail, registerPassword;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://chatapp1-9d739-default-rtdb.firebaseio.com/");
    DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        if (Build.VERSION.SDK_INT >= 21) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(this.getResources().getColor(R.color.Grey));
        }

        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RegisterAccount();
            }
        });
    }

    private void RegisterAccount() {
        String email = registerEmail.getText().toString();
        String password = registerPassword.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this,"Please enter email...",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(this,"Password can't be empty..",Toast.LENGTH_SHORT).show();
        }
        else {
            loadingBar.setTitle("Registering...");
            loadingBar.setMessage("Please wait...");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful()){
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                String deviceToken = task.getResult();
                                                String currentUserID = mAuth.getCurrentUser().getUid();
                                                RootRef.child("users").child(currentUserID).setValue("");

                                                RootRef.child("users").child(currentUserID).child("device_token")
                                                        .setValue(deviceToken);


                                                Toast.makeText(RegisterActivity.this,"Account created successfully...",Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(i);
                                                finish();
                                            }
                                        });
                            }
                            else {
                                String message = task.getException().toString();
                                Toast.makeText(RegisterActivity.this,"Error: " + message,Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void InitializeFields() {
        registerBtn= findViewById(R.id.register_button);
        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);

        loadingBar = new ProgressDialog(this);
    }

    public void login_link (View view){
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
    }
    public void backBtn1 (View view){
        Intent backBtn = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(backBtn);
    }
}