package com.hcmute.ChatAppApplication.password;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.hcmute.ChatAppApplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private TextInputEditText chngPswdedt, confChngPswdedt;
    private Button chngPswd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ActionBar bar = getSupportActionBar();
        bar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.crimson)));
        bar.setDisplayShowTitleEnabled(false);  // required to force redraw, without, gray color
        bar.setDisplayShowTitleEnabled(true);

        chngPswdedt = findViewById(R.id.edtChngPassword);
        confChngPswdedt = findViewById(R.id.edtChngCnfrmPassword);

        chngPswd = findViewById(R.id.changePasswordBtn);

        chngPswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chngPswdedt.getText().toString().trim().equals("") ){

                    chngPswdedt.setError(getString(R.string.valid_password));

                }else if(confChngPswdedt.getText().toString().trim().equals("")){
                    confChngPswdedt.setError(getString(R.string.valid_password));

                }else if(!chngPswdedt.getText().toString().equals(confChngPswdedt.getText().toString())){
                    confChngPswdedt.setError(getString(R.string.conf_password));

                }else{

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    FirebaseUser user = auth.getCurrentUser();

                    if(user != null){
                        user.updatePassword(confChngPswdedt.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(ChangePasswordActivity.this, getString(R.string.updt_pswd), Toast.LENGTH_SHORT).show();
                                }else {
                                    Log.d("reset",task.getException().toString());
                                    Toast.makeText(ChangePasswordActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        });
    }
}