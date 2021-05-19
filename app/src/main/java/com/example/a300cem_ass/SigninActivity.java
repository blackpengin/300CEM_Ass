package com.example.a300cem_ass;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SigninActivity extends AppCompatActivity {

    private static final String TAG = "SigninActivity";


    private FirebaseAuth mAuth;
    private Button mSigninBtn, mEnterRegister;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        mAuth = FirebaseAuth.getInstance();
        mSigninBtn = (Button) findViewById(R.id.signinBtn);
        mEnterRegister = (Button) findViewById(R.id.enterRegisterBtn);

        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //reload();
        }
    }

    private void init(){
        mSigninBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Signin();
            }
        });

        mEnterRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EnterRegister();
            }
        });
    }

    private void Signin(){
        Log.d(TAG, "Register: sign in button clicked");
        String email = ((EditText) findViewById(R.id.email_login)).getText().toString();
        String password = ((EditText) findViewById(R.id.password_login)).getText().toString();

        if(!password.equals("")){
                AuthRegister(email, password);
        }else{
            Toast.makeText(SigninActivity.this, "Password cannot be empty.", Toast.LENGTH_SHORT).show();
        }
    }

    private void AuthRegister(String email, String password){
        Log.d(TAG, "AuthRegister: Email: " + email + ", Password: " + password);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            Toast.makeText(SigninActivity.this, "Sign in success!",
                                    Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            // Enter Main Menu
                            EnterMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SigninActivity.this, "Sign in failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void EnterRegister(){
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void EnterMainActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
