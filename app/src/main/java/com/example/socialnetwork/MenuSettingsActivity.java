package com.example.socialnetwork;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class MenuSettingsActivity extends AppCompatActivity
{
    private RelativeLayout ShowProfile;
    private RecyclerView CommentsList;
    private ImageButton PostCommentButton;
    private EditText CommentInputText;
    private CircleImageView UserNameImage;
    private TextView EditProfile,Logout,UserName,usernameTextView;
    private String Postkey, current_user_id;
    private DatabaseReference UsersRef, PostsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_settings);
        mAuth = FirebaseAuth.getInstance();
        changeStatusBarColor();
        current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users").child(current_user_id);
        ShowProfile=(RelativeLayout)findViewById(R.id.show_profile);
        EditProfile=(TextView)findViewById(R.id.edit_profile);
        Logout=(TextView)findViewById(R.id.logout);
        UserNameImage=(CircleImageView)findViewById(R.id.profileCircleImageView);
        UserName=(TextView)findViewById(R.id.usernameTextView);
        usernameTextView=(TextView)findViewById(R.id.textview1);
        EditProfile.setOnClickListener(v -> {
            SendTOSettingsActivity();
        });
        Logout.setOnClickListener(v -> {
            mAuth.signOut();
            SendUserToLoginActivity();

        });
        usernameTextView.setOnClickListener(v -> {
            SendTOProfileActivity();
        });
        UsersRef.addValueEventListener(new ValueEventListener() {
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
                    String myUserName=snapshot.child("username").getValue().toString();;
                    Glide.with(getApplicationContext())
                            .applyDefaultRequestOptions(new RequestOptions()
                                    .placeholder(R.drawable.profile)
                                    .error(R.drawable.profile))
                            .load(myProfileImage).into(UserNameImage);
                    UserName.setText(myUserName);




                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });


    }
    private void SendTOProfileActivity()
    {
//        Intent ProfileIntent=new Intent(MenuSettingsActivity.this,ProfileActivity.class);
////        startActivity(ProfileIntent);
//        Fragment fr = new ProfileActivity();
//        fr.setArguments(args);
//        FragmentManager fm = getFragmentManager();
//        FragmentTransaction fragmentTransaction = fm.beginTransaction();
//        fragmentTransaction.replace(R.id.fragment_place, fr);
//        fragmentTransaction.commit();
//        Fragment mFragment = null;
//        mFragment = new ProfileActivity();
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
//

    }
    private void SendTOSettingsActivity()
    {
        Intent SettingsIntent=new Intent(MenuSettingsActivity.this,SettingsActivity.class);
        startActivity(SettingsIntent);
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(MenuSettingsActivity.this,LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();

    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setBackgroundDrawableResource(R.drawable.profile_bk);
        }
    }
}