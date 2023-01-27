package com.example.boltlye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    EditText etFullName, etEmailId, etMobile, etPassword, etConfirmPassword;
    TextView txtLogin;
    Button btnSignUp;
    ProgressBar progressBar;
    private static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_main);

        etMobile = findViewById(R.id.etMobile);
        etEmailId = findViewById(R.id.etEmailId);
        etFullName = findViewById(R.id.etFullName);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        txtLogin = findViewById(R.id.txtLogin);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBarM);

        //if already a member then login
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LoginPage.class));
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Obtain the data into string
                String txtFullName = etFullName.getText().toString();
                String txtEmailId = etEmailId.getText().toString();
                String txtMobile = etMobile.getText().toString();
                String txtPassword = etPassword.getText().toString();
                String txtConfirmPassword = etConfirmPassword.getText().toString();

                //Validate Mobile Number using Matcher and Pattern (Regular Expression)
                String mobileRegex = "[6-9][0-9]{9}"; //First no. can be {6,8,9} and rest 9 nos. can be any no.
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(txtMobile);

                //Conditions for Checking each field
                if(TextUtils.isEmpty(txtFullName)){
                    Toast.makeText(MainActivity.this, "Please Enter your full name", Toast.LENGTH_LONG).show();
                    etFullName.setError("Full name is Required");
                    etFullName.requestFocus();
                } else if(TextUtils.isEmpty(txtEmailId)){
                    Toast.makeText(MainActivity.this, "PLease Enter Email Id ", Toast.LENGTH_LONG).show();
                    etEmailId.setError("Email is Required");
                    etEmailId.requestFocus();
                } else if(TextUtils.isEmpty(txtMobile)){
                    Toast.makeText(MainActivity.this, "Please Enter Mobile Number", Toast.LENGTH_LONG).show();
                    etMobile.setError("Mobile Number is Required");
                    etMobile.requestFocus();
                } else if(txtMobile.length() != 10){
                    Toast.makeText(MainActivity.this, "Please re-enter your mobile no", Toast.LENGTH_LONG).show();
                    etMobile.setError("Mobile No. should be 10 digits");
                    etMobile.requestFocus();
                } else if(!mobileMatcher.find()){
                    Toast.makeText(MainActivity.this, "Please re-enter your mobile no", Toast.LENGTH_LONG).show();
                    etMobile.setError("Mobile No. is Invalid");
                    etMobile.requestFocus();
                } else if(TextUtils.isEmpty(txtPassword)){
                    Toast.makeText(MainActivity.this, "Please Enter Password", Toast.LENGTH_LONG).show();
                    etPassword.setError("Password is Required");
                    etPassword.requestFocus();
                } else if(txtPassword.length()<6){
                    Toast.makeText(MainActivity.this, "Please should be at least 6 digits", Toast.LENGTH_LONG).show();
                    etPassword.setError("Password too weak");
                    etPassword.requestFocus();
                } else if(TextUtils.isEmpty(txtConfirmPassword)){
                    Toast.makeText(MainActivity.this, "Please Enter Confirming Password", Toast.LENGTH_LONG).show();
                    etConfirmPassword.setError("Confirming Password is Required");
                    etConfirmPassword.requestFocus();
                } else if(!txtConfirmPassword.equals(txtPassword)){
                    Toast.makeText(MainActivity.this, "Please Enter same Password", Toast.LENGTH_LONG).show();
                    etConfirmPassword.setError("Password does not match");
                    etConfirmPassword.requestFocus();
                    //Clear entered Password
                    etConfirmPassword.clearComposingText();
                    etPassword.clearComposingText();
                } else {

                    registerUser(txtFullName, txtEmailId, txtMobile, txtPassword);
                }

            }

            private void registerUser(String txtFullName, String txtEmailId, String txtMobile, String txtPassword) {
                FirebaseAuth auth = FirebaseAuth.getInstance();

                //Create User Profile
                auth.createUserWithEmailAndPassword(txtEmailId, txtPassword).addOnCompleteListener(MainActivity.this,
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    //getting the current user
                                    FirebaseUser firebaseUser = auth.getCurrentUser();

                                    //Enter User data into the Firebase Realtime Database
                                    ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(txtFullName, txtEmailId, txtMobile, txtPassword);

                                    //Extracting user reference from Database for "Registered Users"
                                    DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered Users");

                                    referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            progressBar.setVisibility(View.VISIBLE);

                                            if(task.isSuccessful()){

                                                //Sending email verification
                                                firebaseUser.sendEmailVerification();
                                                Toast.makeText(MainActivity.this, "Registration Successful. Please Verify Email", Toast.LENGTH_SHORT).show();

                                                //Open user profile after successful registration
                                                Intent intent = new Intent(MainActivity.this, HomePage.class);
                                                //To prevent user from returning back to register activity on pressing back button
                                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                startActivity(intent);
                                                finish(); // to close Register activity


                                            } else{
                                                Toast.makeText(MainActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();

                                            }

                                            progressBar.setVisibility(View.GONE);

                                        }
                                    });

                                }
                                // conditions for if user already exists.
                                else{
                                    progressBar.setVisibility(View.VISIBLE);
                                    try {
                                        throw task.getException();
                                    } catch (FirebaseAuthWeakPasswordException e){
                                        etPassword.setError("Password too weak");
                                        etPassword.requestFocus();
                                    } catch (FirebaseAuthInvalidCredentialsException e){
                                        etPassword.setError("Your Email is invalid or already in use. Kindly re-enter");
                                        etPassword.requestFocus();
                                    } catch (FirebaseAuthUserCollisionException e){
                                        etPassword.setError("User is already registered with this email. Use another Email.");
                                        etPassword.requestFocus();
                                    } catch (Exception e){
                                        Log.e(TAG, e.getMessage());
                                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });

    }

    //Check if the user is already logged in or not.
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth authProfile = FirebaseAuth.getInstance();
        if(authProfile.getCurrentUser() != null){
            Toast.makeText(MainActivity.this, "Already Logged IN", Toast.LENGTH_LONG).show();

            //Start the user profile activity
            startActivity(new Intent(MainActivity.this, HomePage.class));
            finish(); //Close Login activity
        }
    }
}