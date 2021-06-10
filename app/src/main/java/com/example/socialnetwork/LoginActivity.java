package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class LoginActivity extends AppCompatActivity {
private Button LoginButton;
private ProgressDialog loadingBar;
private ImageView googleSignInButton;
private FirebaseAuth mAuth;
private Boolean emailAddressChecker;
private GoogleSignInClient mGoogleSignInClient;
//private GoogleSignInAccount mGoogleSignInClient;
private final static int RC_SIGN_IN=123;
private static final String TAG="LoginActivity";
private EditText UserEmail,UserPassword;
private TextView NeedNewAccountLink, ForgetPasswordLink;

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser User = mAuth.getCurrentUser();
        if(User!=null){
            Intent intent=new Intent(this,MainActivity1.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        setContentView(R.layout.activity_login);
        mAuth=FirebaseAuth.getInstance();
        loadingBar=new ProgressDialog(this);
        changeStatusBarColor();
        NeedNewAccountLink=(TextView)findViewById(R.id.register_account_link);
        UserEmail=(EditText)findViewById(R.id.login_Email);
        UserPassword=(EditText)findViewById(R.id.login_password);
        LoginButton=(Button)findViewById(R.id.login_button);
        ForgetPasswordLink = (TextView)findViewById(R.id.forget_password_link);
        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserForRegister();

            }
        });

        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });
        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowingUserToLogin();
            }
        });


    }



    private void AllowingUserToLogin()
    {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        if(TextUtils.isEmpty(email))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your Email. ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();

            //Toast.makeText(this, "Please write your Email..", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your Password.. ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this, "Please write your Password..", Toast.LENGTH_SHORT).show();
        }
       else
        {
            loadingBar.setTitle("Login");
            loadingBar.setMessage("Please wait,while we are allowing you login into your Account...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
        mAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful()){
                            VerifyEmailAddress();
                            loadingBar.dismiss();

                        }
                        else
                        {
                            String message=task.getException().getMessage();
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Error Occured: ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                    .setBackgroundTint(Color.WHITE).show();
                            //Toast.makeText(LoginActivity.this, "Error Occured: "+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();

                        }
                    }
                });
        }
    }

    private void VerifyEmailAddress()
    {
        FirebaseUser user = mAuth.getCurrentUser();

        emailAddressChecker = user.isEmailVerified();

        if (emailAddressChecker)
        {
            SendUserTOMainActivity();
        }
        else 
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please verify your Account first... ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this, "Please verify your Account first... ", Toast.LENGTH_SHORT).show();
            SendUserTOMainActivity();
//            mAuth.signOut();
        }
    }


    private void SendUserTOMainActivity()
    {
        Intent mainIntent=new Intent(LoginActivity.this,MainActivity1.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    public void onLoginClick(View View){
        startActivity(new Intent(this,RegisterActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.stay);

    }

    private void SendUserForRegister()
    {
        Intent registerIntent=new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);

    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setBackgroundDrawableResource(R.drawable.profile_bk);
        }
    }
}