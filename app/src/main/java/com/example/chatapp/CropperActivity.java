package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class CropperActivity extends AppCompatActivity {

    private String result;
    private Uri fileUri;
    private FirebaseAuth mAuth;
    private StorageReference UserProfileImagesRef;
    private String currentUserID;

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReferenceFromUrl("https://chatapp1-9d739-default-rtdb.firebaseio.com/");

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cropper);

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("profile Images");
        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        loadingBar = new ProgressDialog(this);

        readIntent();

        String dest_uri = new StringBuffer(UUID.randomUUID().toString()).append(".jpg").toString();

        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);

        UCrop.of(fileUri, Uri.fromFile(new File(getCacheDir(),dest_uri)))
                .withOptions(options)
                .withAspectRatio(0,0)
                .useSourceImageAspectRatio()
                .withMaxResultSize(2000, 2000)
                .start(CropperActivity.this);
    }

    private void readIntent() {
        Intent intent = getIntent();
        if (intent.getExtras()!=null){
            result=intent.getStringExtra("DATA");
            fileUri = Uri.parse(result);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode==RESULT_OK && requestCode==UCrop.REQUEST_CROP) {
            loadingBar.setTitle("Set profile image");
            loadingBar.setMessage("Please wait");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            final Uri resultUri = UCrop.getOutput(data);
            Intent returnIntent = new Intent();
            returnIntent.putExtra("RESULT", resultUri + "");
            setResult(-1, returnIntent);
            StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");

            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    filePath.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            final String downloadUrl = task.getResult().toString();
                            databaseReference.child("users").child(currentUserID).child("image")
                                    .setValue(downloadUrl);
                            Toast.makeText(CropperActivity.this, "Image saved in database", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

            finish();


        }
        else if(resultCode==UCrop.RESULT_ERROR)
        {
            final Throwable cropError = UCrop.getError(data);
        }
    }
}