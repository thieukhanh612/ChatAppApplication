package com.hcmute.ChatAppApplication.profile;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.NodeNames;
import com.hcmute.ChatAppApplication.login.LoginActivity;
import com.hcmute.ChatAppApplication.password.ChangePasswordActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
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
//declare class for profile activity
public class ProfileActivity extends AppCompatActivity {
    //declare button for save and logout
    private Button save, logout;
    //declare text view for email and change password
    private TextView emailView, chngPswrd;
    //declare edit text for username
    private TextInputEditText edtUsername;
    //declare image view for user photo
    private CircleImageView edtProfilePic;
    //declare view for custom progress bar
    private View customProgressbar;
    //defined request code
    public static final int REQUEST_CODE_PERMISSION_GRANTED = 10;
    public static final int REQUEST_CODE_IMAGE_SELECTOR = 1000;

    //declare firebase authorization
    FirebaseAuth mAuth;
    //declare firebase user
    private FirebaseUser currentUser;
    //declare url for server and local photo
    private Uri serverPhotoUri, localPhotoUri;
    //declare reference for storage
    private StorageReference storageReference;
    //declare reference for database
    private DatabaseReference reference;

    @Override
    //action when create class
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.crimson)));
        bar.setDisplayShowTitleEnabled(false);  // required to force redraw, without, gray color
        bar.setDisplayShowTitleEnabled(true);
        emailView = findViewById(R.id.edtEmailTxt);
        edtUsername = findViewById(R.id.editUserName);
        edtProfilePic = findViewById(R.id.edtProfileViewCircularImage);
        save = findViewById(R.id.saveBtn);
        logout = findViewById(R.id.logoutBtn);
        chngPswrd = findViewById(R.id.chngPasswrdTxtVw);
        customProgressbar = findViewById(R.id.progressbarView);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        if(currentUser!= null){

            edtUsername.setText(currentUser.getDisplayName());
            emailView.setText(currentUser.getEmail());
            serverPhotoUri = currentUser.getPhotoUrl();

            if(serverPhotoUri!= null){
                Glide.with(this)
                        .load(serverPhotoUri)
                        .placeholder(R.drawable.profile_image_default)
                        .error(R.drawable.profile_image_default)
                        .into(edtProfilePic);

            }
            //set event on edit profile
            edtProfilePic.setOnClickListener(new View.OnClickListener() {
                @Override
                //action when click button
                public void onClick(View view) {
                    if(serverPhotoUri == null){
                        selectImage();
                    }
                    else{
                        PopupMenu popupMenu = new PopupMenu(ProfileActivity.this, view);
                        popupMenu.getMenuInflater().inflate(R.menu.update_pic_menu, popupMenu.getMenu());
                        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {

                                switch (menuItem.getItemId()){
                                    case R.id.changePic:
                                        selectImage();
                                        break;

                                    case R.id.removePic:
                                        removePic();
                                        break;
                                }
                                return false;
                            }
                        });
                        popupMenu.show();
                   }
                }
            });
            //set event on save
            save.setOnClickListener(new View.OnClickListener() {
                @Override
                //action when click
                public void onClick(View view) {
                    if(localPhotoUri!= null){
                        updateNameAndPhoto();

                    }else {
                        updateUserName();
                    }
                }
            });

        }
        //set event on logout
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ProfileActivity.this, LoginActivity.class));
                finish();
            }
        });
        //set event on change password
        chngPswrd.setOnClickListener(new View.OnClickListener() {
            @Override
            //action when click
            public void onClick(View view) {
                startActivity(new Intent(ProfileActivity.this, ChangePasswordActivity.class));

            }
        });

    }

    //func update name and photo
    private void updateNameAndPhoto(){

        String filePath = currentUser.getUid()+".jpg";

        final StorageReference fileReference = storageReference.child("images/"+filePath);

        customProgressbar.setVisibility(View.VISIBLE);
        //set event on push file to filestorage
        fileReference.putFile(localPhotoUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            //action when complete
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){

                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            serverPhotoUri = uri;
                            UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(edtUsername.getText().toString())
                                    .setPhotoUri(uri)
                                    .build();
                            //set event on update profile
                            currentUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                //action when complete
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful()){
                                        String userId = currentUser.getUid();
                                        reference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
                                        HashMap<String, String> hashMap = new HashMap<>();
                                        hashMap.put(NodeNames.NAME, edtUsername.getText().toString());
                                        hashMap.put(NodeNames.EMAIL, emailView.getText().toString());
                                        hashMap.put(NodeNames.ONLINE,"true");
                                        hashMap.put(NodeNames.PHOTO, serverPhotoUri.toString());
                                        reference.child(userId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                customProgressbar.setVisibility(View.GONE);
                                                Toast.makeText(ProfileActivity.this, getString(R.string.pdofile_pic_update), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else {
                                        customProgressbar.setVisibility(View.GONE);
                                        Toast.makeText(ProfileActivity.this, "Failed to update profile:  ", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                }else{
                    Toast.makeText(ProfileActivity.this, getString(R.string.profile_pic_cant_updated), Toast.LENGTH_SHORT).show();

                }
            }
        });
    }


    //func to update username
    private void updateUserName(){

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(edtUsername.getText().toString())
                .build();
        //set event on update profile
        currentUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            //action when complete update profile
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    String userId = currentUser.getUid();
                    reference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(NodeNames.NAME, edtUsername.getText().toString());

                    hashMap.put(NodeNames.EMAIL, emailView.getText().toString());
                    hashMap.put(NodeNames.ONLINE,"true");
                    hashMap.put(NodeNames.PHOTO, "");


                    reference.child(userId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this, R.string.sign_up_success, Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile:  ", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
    //action when select image
    private void selectImage(){

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityIfNeeded(intent, REQUEST_CODE_IMAGE_SELECTOR);
    }



    @Override
    //action when get result from request permission
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_PERMISSION_GRANTED && grantResults.length>0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();

            }else {
                Toast.makeText(ProfileActivity.this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    //action when get resilt
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==REQUEST_CODE_IMAGE_SELECTOR && resultCode==RESULT_OK && data!=null){

            localPhotoUri = data.getData();
            if(localPhotoUri != null){
                try{
                    //Trying to get the selected image
//                    InputStream inputStream = getContentResolver().openInputStream(localImageUri);
//                    bitmap = BitmapFactory.decodeStream(inputStream);
//                    profilePicUser.setImageBitmap(bitmap);
                    edtProfilePic.setImageURI(localPhotoUri);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
    //func to remove picture
    private void removePic(){

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(edtUsername.getText().toString())
                .setPhotoUri(null)
                .build();
        //set event to update profile
        currentUser.updateProfile(request).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            //action when complete update
            public void onComplete(@NonNull Task<Void> task) {

                if(task.isSuccessful()){
                    String userId = currentUser.getUid();
                    reference = FirebaseDatabase.getInstance().getReference().child(NodeNames.USERS);
                    HashMap<String, String> hashMap = new HashMap<>();
                    hashMap.put(NodeNames.NAME, edtUsername.getText().toString());
                    hashMap.put(NodeNames.EMAIL, emailView.getText().toString());
                    hashMap.put(NodeNames.ONLINE,"true");
                    hashMap.put(NodeNames.PHOTO, "");

                    reference.child(userId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(ProfileActivity.this, R.string.profile_pic_removed, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    Toast.makeText(ProfileActivity.this, "Failed to update profile:  ", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}