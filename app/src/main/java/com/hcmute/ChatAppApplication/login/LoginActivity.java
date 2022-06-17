package com.hcmute.ChatAppApplication.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hcmute.ChatAppApplication.ExceptionNetworkActivity;
import com.hcmute.ChatAppApplication.MainActivity;
import com.hcmute.ChatAppApplication.R;
import com.hcmute.ChatAppApplication.common.Util;
import com.hcmute.ChatAppApplication.password.ResetPasswordActivity;
import com.hcmute.ChatAppApplication.signUp.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.installations.InstallationTokenResult;

public class LoginActivity extends AppCompatActivity {


    private TextInputEditText email, password;
    private Button loginBtn;
    private TextView frgtPswd, signUp;
    private FirebaseAuth mAuth;
    private FirebaseDatabase firebaseDatabase;
    private View customProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(" ");
        bar.setElevation(0);

        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        email = findViewById(R.id.loginUserName);
        password = findViewById(R.id.loginPassword);
        loginBtn = findViewById(R.id.login);
        signUp = findViewById(R.id.signUp);
        frgtPswd = findViewById(R.id.forgetPaswrd);
        customProgressbar = findViewById(R.id.progressbarView);


        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(Util.checkInternetConnection(LoginActivity.this)){
                    customProgressbar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            customProgressbar.setVisibility(View.GONE);

                            if(task.isSuccessful()){
                                Toast.makeText(LoginActivity.this, R.string.sign_in, Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(LoginActivity.this, R.string.not_sign_in, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    startActivity(new Intent(LoginActivity.this, ExceptionNetworkActivity.class));
                }

            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        frgtPswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        if(mAuth.getCurrentUser()!=null){

            FirebaseInstallations.getInstance().getToken(false).addOnSuccessListener(new OnSuccessListener<InstallationTokenResult>() {
                @Override
                public void onSuccess(InstallationTokenResult installationTokenResult) {
                    Util.updateDeviceTokken(LoginActivity.this, installationTokenResult.getToken());
                }
            });
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

    }
}