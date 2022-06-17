package com.hcmute.ChatAppApplication.signUp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hcmute.ChatAppApplication.ExceptionNetworkActivity;
import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.NodeNames;
import com.hcmute.ChatAppApplication.common.Util;
import com.hcmute.ChatAppApplication.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpActivity extends AppCompatActivity {

    public static final int REQUEST_CODE_PERMISSION_GRANTED = 10;
    public static final int REQUEST_CODE_IMAGE_SELECTOR = 1000;


    private TextInputEditText email, password, confirmPswd, username;
    private Button signUpBtn;
    private TextView loginUp;
    private CircleImageView profilePicUser;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference reference;
    private Bitmap bitmap;
    private StorageReference storageReference;
    private Uri localImageUri, serverImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_);

        username = findViewById(R.id.signUpUserName);
        email = findViewById(R.id.signUpEmail);
        password = findViewById(R.id.signUpPassword);
        confirmPswd = findViewById(R.id.signUpConfirmPassword);
        profilePicUser = findViewById(R.id.profileCircularImage);
        firebaseDatabase = FirebaseDatabase.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        signUpBtn = findViewById(R.id.doSignUp);
        loginUp = findViewById(R.id.loginUp);

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Util.checkInternetConnection(SignUpActivity.this)){

                    if(username.getText().toString() == null){
                        username.setError(getString(R.string.valid_username));
                    }else if(!Patterns.EMAIL_ADDRESS.matcher(email.getText().toString()).matches() && email.getText().toString()==""){
                        email.setError(getString(R.string.valid_email));
                    }else if(password.getText().toString()=="" && password.length()>0 && password.length()<8){
                        password.setError(getString(R.string.valid_password));
                    }else if(!confirmPswd.getText().toString().equals(password.getText().toString())){
                        confirmPswd.setError(getString(R.string.conf_password));
                    }else{
                        mAuth = FirebaseAuth.getInstance();

                        mAuth.createUserWithEmailAndPassword(email.getText().toString(), confirmPswd.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    user = mAuth.getCurrentUser();

                                    if(localImageUri!= null){
                                        updateNameAndPhoto();
                                    }else {
                                        updateUserName();
                                    }


                                }else{
                                    Toast.makeText(SignUpActivity.this,
                                            getString(R.string.account_failed)+task.getException() ,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }else {
                    startActivity(new Intent(SignUpActivity.this, ExceptionNetworkActivity.class));
                }

            }
        });

        profilePicUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(SignUpActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

                    ActivityCompat.requestPermissions(SignUpActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_PERMISSION_GRANTED);
                }else {
                    selectImage();
                }
            }
        });

    }


    private void updateUserName(){

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(username.getText().toString())
                .build();


        user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    String userId = user.getUid();
                    reference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(NodeNames.NAME, username.getText().toString());
                    hashMap.put(NodeNames.EMAIL, email.getText().toString());
                    hashMap.put(NodeNames.ONLINE,"true");
                    hashMap.put(NodeNames.PHOTO, "");
                    reference.child(userId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(SignUpActivity.this, R.string.sign_up_success, Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                }else {
                    Toast.makeText(SignUpActivity.this, "Failed to update profile:  ", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    private void selectImage(){

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityIfNeeded(intent, REQUEST_CODE_IMAGE_SELECTOR);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_PERMISSION_GRANTED && grantResults.length>0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();

            }else {
                Toast.makeText(SignUpActivity.this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE_IMAGE_SELECTOR && resultCode==RESULT_OK && data!=null){

            localImageUri = data.getData();
            if(localImageUri != null){
                try{
                    //Trying to get the selected image
//                    InputStream inputStream = getContentResolver().openInputStream(localImageUri);
//                    bitmap = BitmapFactory.decodeStream(inputStream);
//                    profilePicUser.setImageBitmap(bitmap);
                    profilePicUser.setImageURI(localImageUri);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateNameAndPhoto(){

        String filePath = user.getUid()+".jpg";

        final StorageReference fileReference = storageReference.child("images/"+filePath);

        fileReference.putFile(localImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    Toast.makeText(SignUpActivity.this, getString(R.string.pdofile_pic_update), Toast.LENGTH_SHORT).show();
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                                serverImageUri = uri;
                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username.getText().toString())
                                    .setPhotoUri(uri)
                                    .build();

                            user.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        String userId = user.getUid();
                                        reference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put(NodeNames.NAME, username.getText().toString());
                                        hashMap.put(NodeNames.EMAIL, email.getText().toString());
                                        hashMap.put(NodeNames.ONLINE,"true");
                                        hashMap.put(NodeNames.PHOTO, serverImageUri.toString());
                                        reference.child(userId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(SignUpActivity.this, R.string.sign_up_success, Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                        });
                                    }else {
                                        Toast.makeText(SignUpActivity.this, "Failed to update profile:  ", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                }else{
                    Toast.makeText(SignUpActivity.this, "Profile Pic Cannot Be Updated ", Toast.LENGTH_SHORT).show();

                }
            }
        });


    }
}