package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.se.omapi.Channel;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupActivity extends AppCompatActivity {
    private EditText UserName, FullName, CountryName;
    private Button SaveInformationButton;
    private CircleImageView ProfileImage;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference UsersRef;
    private StorageReference UserProfileImageRef;
    final static int Gallery_pick=1;
    String currentUserID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);
        changeStatusBarColor();
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        loadingBar=new ProgressDialog(this);
        UsersRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users").child(currentUserID);
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile Images");
        UserName = (EditText) findViewById(R.id.setup_userName);
        FullName = (EditText) findViewById(R.id.setup_full_name);
        CountryName = (EditText) findViewById(R.id.setup_country);
        SaveInformationButton = (Button) findViewById(R.id.setup_save_btn);
        ProfileImage = (CircleImageView) findViewById(R.id.setup_image_profile);
        GoogleSignInAccount signInAccount= GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount!=null)
        {
            FullName.setText(signInAccount.getDisplayName());

        }

        SaveInformationButton.setOnClickListener(v -> SaveAccountSetupInformation());
        ProfileImage.setOnClickListener(v -> {
            Intent galleryIntent=new Intent();
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
            galleryIntent.setType("image/*");
            startActivityForResult(galleryIntent,Gallery_pick);
        });

        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    if(dataSnapshot.hasChild("profileimage"))
                    {

                        String image = dataSnapshot.child("profileimage").getValue().toString();
                        Picasso.get().load(image).placeholder(R.drawable.profile).into(ProfileImage);
                    }
                    else
                    {
                        Snackbar.make(findViewById(android.R.id.content),
                                "Please select profile image first..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                .setBackgroundTint(Color.WHITE).show();
                        //Toast.makeText(SetupActivity.this, "Please select profile image first..", Toast.LENGTH_SHORT).show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = "drawable://" + R.drawable.profile;
                    if(!uri.toString().isEmpty())
                    {
                        downloadUrl = uri.toString();

                    }
                    UsersRef.child("profileimage").setValue(downloadUrl).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){



                                Intent selfIntent = new Intent(SetupActivity.this, SetupActivity.class);
                                startActivity(selfIntent);
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Image Stored", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                        .setBackgroundTint(Color.WHITE).show();
                                //Toast.makeText(SetupActivity.this, "Image Stored", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else {
                                String message = task.getException().getMessage();
                                Snackbar.make(findViewById(android.R.id.content),
                                        "Error:", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                        .setBackgroundTint(Color.WHITE).show();
                                //Toast.makeText(SetupActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
                }));
            }
            else
            {
                Snackbar.make(findViewById(android.R.id.content),
                        "Error Occured:", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                        .setBackgroundTint(Color.WHITE).show();
                //Toast.makeText(this, "Error Occured: Image can not be cropped. Try Again.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }


    private void SaveAccountSetupInformation()
    {
        String username=UserName.getText().toString();
        String fullName=FullName.getText().toString();
        String country=CountryName.getText().toString();
        if(TextUtils.isEmpty(username))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your username..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this, "Please write your username..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(fullName))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your fullName..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this, "Please write your fullName..", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(country))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please write your country..", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this, "Please write your country..", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Saving Information");
            loadingBar.setMessage("Please wait,while we are creating your new Account...");
            loadingBar.show();

            loadingBar.setCanceledOnTouchOutside(true);
            HashMap userMap=new HashMap();
            userMap.put("username",username);
            userMap.put("fullname",fullName);
            userMap.put("status","Hey there i am using Poster Social Network");
            userMap.put("gender","none");
            userMap.put("dob","none");
            userMap.put("country",country);
            userMap.put("animal","Dog");
            userMap.put("relationshipstatus","none");
            UsersRef.updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if(task.isSuccessful())
                    {
                        SendUserToMainActivity();
                        Snackbar.make(findViewById(android.R.id.content),
                                "your Account is created successfully", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                .setBackgroundTint(Color.WHITE).show();
                        //Toast.makeText(SetupActivity.this,"your Account is created successfully",Toast.LENGTH_LONG).show();
                        loadingBar.dismiss();
                    }
                    else {
                        String message=task.getException().getMessage();
                        Snackbar.make(findViewById(android.R.id.content),
                                "Error Occured: ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                .setBackgroundTint(Color.WHITE).show();
                        //Toast.makeText(SetupActivity.this, "Error Occured: "+message, Toast.LENGTH_SHORT).show();
                        loadingBar.dismiss();
                    }
                }
            });
        }


    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.register_bk_color));
        }
    }
    private void SendUserToMainActivity()
    {
        Intent setupIntent=new Intent(SetupActivity.this,MainActivity1.class);
        setupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }
    private Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//Compression quality, here 100 means no compression, the storage of compressed data to baos
        int options = 90;
        while (baos.toByteArray().length / 1024 > 400) {  //Loop if compressed picture is greater than 400kb, than to compression
            baos.reset();//Reset baos is empty baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//The compression options%, storing the compressed data to the baos
            options -= 10;//Every time reduced by 10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//The storage of compressed data in the baos to ByteArrayInputStream
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//The ByteArrayInputStream data generation
        return bitmap;
    }

public static Bitmap getBitmapFromURL(String src) {
    try {
        URL url = new URL(src);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap myBitmap = BitmapFactory.decodeStream(input);
        return myBitmap;
    } catch (IOException e) {
        // Log exception
        return null;
    }
}
}
