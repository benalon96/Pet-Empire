package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
private Toolbar mToolbar;
private ProgressDialog loadingBar;
final static int Gallery_pick=1;
private Button UpdateAccountBtn;
private CircleImageView userProImage;
private DatabaseReference SettingsUserRef;
private FirebaseAuth mAuth;
private StorageReference UserProfileImageRef;
private String currentUserId;
private EditText userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB,userAnimal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        changeStatusBarColor();
        mAuth=FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile Images");
        SettingsUserRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users").child(currentUserId);
        loadingBar=new ProgressDialog(this);
        userName=(EditText)findViewById(R.id.settings_username);
        userProfName=(EditText)findViewById(R.id.settings_fullname);
        userStatus=(EditText)findViewById(R.id.settings_status);
        userCountry=(EditText)findViewById(R.id.settings_country);
        userGender=(EditText)findViewById(R.id.settings_gender);
        userRelation=(EditText)findViewById(R.id.settings_relationship_status);
        userDOB=(EditText)findViewById(R.id.settings_dob);
        userAnimal=(EditText)findViewById(R.id.settings_animal);
        userProImage=(CircleImageView)findViewById(R.id.settings_profile_image);
        UpdateAccountBtn=(Button)findViewById(R.id.update_account_settings_button);

        SettingsUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    String myProfileImage ="drawable://" + R.drawable.profile;
                    if(snapshot.child("profileimage").exists())
                    {
                     myProfileImage=snapshot.child("profileimage").getValue().toString();
                    }
                    String myUserName=snapshot.child("username").getValue().toString();
                    String myUserProfName=snapshot.child("fullname").getValue().toString();
                    String myUserStatus=snapshot.child("status").getValue().toString();
                    String myUserCountry=snapshot.child("country").getValue().toString();
                    String myUserGender=snapshot.child("gender").getValue().toString();
                    String myUserRelation=snapshot.child("relationshipstatus").getValue().toString();
                    String myUserDOB=snapshot.child("dob").getValue().toString();
                    String myUserAnimal=snapshot.child("animal").getValue().toString();
//                    Picasso.get().load(myProfileImage).into(userProImage);
                    Glide.with(getApplicationContext())
                            .applyDefaultRequestOptions(new RequestOptions()
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile))
                            .load(myProfileImage).into(userProImage);
                    userName.setText(myUserName);
                    userProfName.setText(myUserProfName);
                    userStatus.setText(myUserStatus);
                    userCountry.setText(myUserCountry);
                    userGender.setText(myUserGender);
                    userRelation.setText(myUserRelation);
                    userDOB.setText(myUserDOB);
                    userAnimal.setText(myUserAnimal);



                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        UpdateAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                ValidateAccountInfo();
            }
        });

        userProImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,Gallery_pick);
            }
        });
    }

    private void ValidateAccountInfo()
    {
        String username=userName.getText().toString();
        String profName=userProfName.getText().toString();
        String status=userStatus.getText().toString();
        String country=userCountry.getText().toString();
        String gender=userGender.getText().toString();
        String relation=userRelation.getText().toString();
        String DOB=userDOB.getText().toString();
        String animal=userAnimal.getText().toString();
        if(TextUtils.isEmpty(username))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your username..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"Please write your username..",Toast.LENGTH_SHORT).show();
        }
      else if(TextUtils.isEmpty(profName))
      {
          Snackbar.make(findViewById(android.R.id.content),
                  "Please write your profile Name..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                  .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"Please write your profName..",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(status))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your status..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"Please write your status..",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(country))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your country..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"Please write your country..",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(gender))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your gender..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"Please write your gender..",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(relation))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your relation..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"Please write your relation..",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(DOB)){
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your DOB..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"Please write your DOB..",Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(animal)){
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your animal..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"Please write your animal..",Toast.LENGTH_SHORT).show();
        }
        else
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();
                UpdateAccountInfo(username,profName,status,country,gender,relation,DOB,animal);

        }


    }

    private void UpdateAccountInfo(String username, String profName, String status, String country, String gender, String relation, String dob, String animal)
    {
        HashMap userMap=new HashMap();
        userMap.put("username",username);
        userMap.put("fullname",profName);
        userMap.put("status",status);
        userMap.put("country",country);
        userMap.put("gender",gender);
        userMap.put("relationshipstatus",relation);
        userMap.put("dob",dob);
        userMap.put("animal",animal);
        SettingsUserRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull @NotNull Task task)
            {
                if(task.isSuccessful()){
                    SendUserTOMainActivity();
                    Toast.makeText(SettingsActivity.this,"Account Settings Updated Successfully...",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }
                else{
                    Toast.makeText(SettingsActivity.this,"Error Occured,while updating account setting info...",Toast.LENGTH_SHORT).show();
                    loadingBar.dismiss();
                }

            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==Gallery_pick && resultCode==RESULT_OK && data!=null)
        {
            Uri ImageUri=data.getData();
            CropImage.activity(ImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON).
                    setAspectRatio(1,1)
                    .start(this);
        }
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Profile Image");
                loadingBar.setMessage("Please wait, while we updating your profile image...");
                loadingBar.setCanceledOnTouchOutside(true);
                loadingBar.show();


                Uri resultUri = result.getUri();

                final StorageReference filePath = UserProfileImageRef.child(currentUserId + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    final String downloadUrl = uri.toString();
                    SettingsUserRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){



                                Intent selfIntent = new Intent(SettingsActivity.this, SettingsActivity.class);
                                startActivity(selfIntent);
                                Toast.makeText(SettingsActivity.this, "Image Stored", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else {
                                String message = task.getException().getMessage();
                                Toast.makeText(SettingsActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }));
            }
            else
            {
                Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setBackgroundDrawableResource(R.drawable.profile_bk);
        }
    }
    private void SendUserTOMainActivity()
    {
        Intent mainIntent=new Intent(SettingsActivity.this,MainActivity1.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}