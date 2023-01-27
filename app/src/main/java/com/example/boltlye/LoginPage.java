package com.example.boltlye;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginPage extends AppCompatActivity {

    EditText etLoginEmailId, etLoginPassword;
    Button btnSignIn;
    ProgressBar progressBar;
    FirebaseAuth authProfile;
    static final String TAG = "LoginPage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //will hide the title
        getSupportActionBar().hide(); // hide the title bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN); //enable full screen
        setContentView(R.layout.activity_login_page);

        etLoginEmailId = findViewById(R.id.etLoginEmailId);
        etLoginPassword = findViewById(R.id.etLoginPassword);
        btnSignIn = findViewById(R.id.btnSignIn);
        progressBar = findViewById(R.id.progressBarL);

        authProfile = FirebaseAuth.getInstance();

        //Login

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String textEmail = etLoginEmailId.getText().toString();
                String textPassword = etLoginPassword.getText().toString();

                if(TextUtils.isEmpty(textEmail)){
                    Toast.makeText(LoginPage.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    etLoginEmailId.setError("Email is Required");
                    etLoginEmailId.requestFocus();
                } else if(!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()){
                    Toast.makeText(LoginPage.this, "Please re-enter your email", Toast.LENGTH_LONG).show();
                    etLoginEmailId.setError("Valid Email is Required");
                    etLoginEmailId.requestFocus();
                } else if(TextUtils.isEmpty(textPassword)){
                    Toast.makeText(LoginPage.this, "Please enter your Password", Toast.LENGTH_LONG).show();
                    etLoginPassword.setError("Password is Required");
                    etLoginPassword.requestFocus();
                } else{
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail, textPassword);
                }
            }
        });
    }

    private void loginUser(String textEmail, String textPassword) {
        authProfile.signInWithEmailAndPassword(textEmail, textPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    //Get instance of the current user
                    FirebaseUser firebaseUser = authProfile.getCurrentUser();

                    //Check if email is verified before user can access their profile.
                    if(firebaseUser.isEmailVerified()){
                        Toast.makeText(LoginPage.this, "Login Successful", Toast.LENGTH_LONG).show();

                        //Open User Profile
                    } else{
                        firebaseUser.sendEmailVerification();
                        authProfile.signOut();
                        showAlertDialog();
                    }
                } else{
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e){
                        etLoginEmailId.setError("User does not exist. Please Register");
                        etLoginEmailId.requestFocus();
                    } catch (FirebaseAuthInvalidCredentialsException e){
                        etLoginEmailId.setError("Invalid Credentials. Kindly Check");
                        etLoginEmailId.requestFocus();
                    } catch (Exception e){
                        Log.e(TAG, e.getMessage());
                        Toast.makeText(LoginPage.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void showAlertDialog() {
        //Setup Alert Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginPage.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Please verify your email. You cannot login without email verification.");

        //Open Email apps if user clicks continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //to open email app in new window
                startActivity(intent);
            }
        });

        //Create the Alert Dialog
        AlertDialog alertDialog = builder.create();

        //Show alert dialog
        alertDialog.show();
    }

    //Check if the user is already logged in or not.
    @Override
    protected void onStart() {
        super.onStart();
        if(authProfile.getCurrentUser() != null){
            Toast.makeText(LoginPage.this, "Already Logged IN", Toast.LENGTH_LONG).show();

            //Start the user profile activity
            startActivity(new Intent(LoginPage.this, HomePage.class));
            finish(); //Close Login activity
        }
    }
}