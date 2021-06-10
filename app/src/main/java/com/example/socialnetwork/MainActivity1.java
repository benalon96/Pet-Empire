package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity1 extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private String currentUserID;
    BottomNavigationView bottomNavigationView;
    private static StorageReference UserProfileImageRef;
    private FirebaseAuth mAuth;
    private CircleImageView NavProfileImage;
    private TextView NavProfileUserImage;
    private BottomNavigationView bottomToolbar;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference UserRef, PostsRef,LikesRef;
    private NavController navController;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private ImageButton AddNewPostButton;
    private NavigationView navigationView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mAuth= FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users");
        navigationView = findViewById(R.id.navigation_view);
        getSupportFragmentManager().
                beginTransaction().
                add(R.id.frame_layout, new MainActivity()).
                commit();
        bottomToolbar = findViewById(R.id.bottomToolbar);
        bottomToolbar.setOnNavigationItemSelectedListener(bottomListener);
        View navView=navigationView.inflateHeaderView(R.layout.navigation_header);
        NavProfileImage=(CircleImageView)navView.findViewById(R.id.nav_profile_image);
        NavProfileUserImage=(TextView)navView.findViewById(R.id.nav_user_full_name);
        actionBarDrawerToggle = new ActionBarDrawerToggle(MainActivity1.this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        actionBarDrawerToggle.syncState();
        mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }
        if (currentUserID!=null){

            UserRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.exists()){
                        if(dataSnapshot.hasChild("fullname")){
                            String fullName=(dataSnapshot.child("fullname").getValue().toString());
                            NavProfileUserImage.setText(fullName);
                        }
                        if(dataSnapshot.hasChild("profileimage")){
                            String image=(dataSnapshot.child("profileimage").getValue().toString());
                            Glide.with(getApplicationContext())
                                    .applyDefaultRequestOptions(new RequestOptions()
                                            .placeholder(R.drawable.profile)
                                            .error(R.drawable.profile))
                                    .load(image).into(NavProfileImage);

                        }
                        else
                        {
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Profile do not exists... ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                    .setBackgroundTint(Color.WHITE).show();
                            //Toast.makeText(getApplicationContext(), "Profile do not exists...", Toast.LENGTH_SHORT).show();
                        }


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                UserMenuSelector(item);
                return false;
            }
        });
    }

        private BottomNavigationView.OnNavigationItemSelectedListener bottomListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment selectedFragment = null;
                    switch (menuItem.getItemId()){
                        case R.id.action_home:
                            selectedFragment = new MainActivity();
                            break;

                        case R.id.action_profile:
                            selectedFragment = new ProfileActivity();
                        break;


                        case R.id.action_message:
                            selectedFragment = new FriendsActivity();
                            break;

                        case R.id.action_settings:
                            selectedFragment = new FindFriendsActivity();
                            break;

                    }


                   getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,selectedFragment).commit();

                    return true;
                }
            };
    private void UserMenuSelector(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.nav_post:
                SendUserTOPostActivity();
                break;
            case R.id.nav_profile:
                SendTOProfileActivity();
                break;
            case R.id.nav_home:
                Fragment selectedFragment=new MainActivity();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,selectedFragment).commit();
                break;
            case R.id.nav_friends:
                SendTOFriendsActivity();
                break;
            case R.id.nav_find_friends:
                SendTOFindFriendsActivity();

                break;
            case R.id.nev_messages:
                Fragment selectedFragment1=new FriendsActivity();
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout,selectedFragment1).commit();
                break;
            case R.id.nav_settings:
                SendTOMenuSettingsActivity();
                break;
            case R.id.nav_Logout:
                updateUserStatus("offline");
                mAuth.signOut();
                SendUserToLoginActivity();
                break;



        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull @NotNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        return true;
    }

    private void SendUserTOPostActivity() {
        Intent addNewPostIntent = new Intent(MainActivity1.this, PostActivity.class);
        startActivity(addNewPostIntent);
    }

    private void SendTOFriendsActivity() {
        getSupportFragmentManager().
                beginTransaction().
                add(R.id.frame_layout, new FindFriendsActivity()).
                commit();
    }

    private void SendTOSettingsActivity() {
        Intent SettingsIntent = new Intent(MainActivity1.this, SettingsActivity.class);
        startActivity(SettingsIntent);
    }

    private void SendTOMenuSettingsActivity() {
        Intent SettingsIntent1 = new Intent(MainActivity1.this, MenuSettingsActivity.class);
        startActivity(SettingsIntent1);
    }

    private void SendTOFindFriendsActivity() {
        getSupportFragmentManager().
                beginTransaction().
                add(R.id.frame_layout, new FindFriendsActivity()).
                commit();
    }

    private void SendTOProfileActivity() {
//        Fragment selectedFragment = null;
//        selectedFragment=new ProfileActivity();
//        bottomNavigationView.setSelectedItemId(R.id.action_profile);
//       bottomNavigationView.setSelectedItemId(R.id.action_profile);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomToolbar);
//        Integer indexItem = 4;
//        navigation.getMenu().findItem(R.id.action_profile).setChecked(true);
//        bottomListener.onNavigationItemSelected(navigation.getMenu().getItem(R.id.action_profile));
//        bottomListener.onNavigationItemSelected(new MenuItem(R.id.action_profile));
////        View view = bottomListener(new MenuItem(R.id.action_profile) );
//        view.performClick();
//        bottomToolbar.setSelectedItemId(R.id.profile_toolbar);

//        View view = bottomToolbar.findViewById(R.id.profile_toolbar);
//        view.performClick();
        getSupportFragmentManager().
                beginTransaction().
                add(R.id.frame_layout, new ProfileActivity()).
                commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull @NotNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_post:
                SendUserTOPostActivity();
                break;
            case R.id.nav_profile:
                SendTOProfileActivity();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_friends:
                SendTOFriendsActivity();
                break;
            case R.id.nav_find_friends:
                SendTOFindFriendsActivity();

                break;
            case R.id.nev_messages:
                Toast.makeText(this, "Messages", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
                SendTOMenuSettingsActivity();
                break;
            case R.id.nav_Logout:
//                updateUserStatus("offline");
//                mAuth.signOut();
//                SendUserToLoginActivity();
                break;


        }
        return true;
    }
    public void updateUserStatus(String state) {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentTime);
        currentStateMap.put("type", state);

        UserRef.child(currentUserID).child("usersState")
                .updateChildren(currentStateMap);


    }
    private void SendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(MainActivity1.this,LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);


    }
    private void CheckUserExistence()
    {
        if(mAuth.getCurrentUser().getUid()!=null) {
            final String current_user_id = mAuth.getCurrentUser().getUid();

            UserRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot dataSnapshot)
                {
                    if(!dataSnapshot.hasChild(current_user_id))
                    {
                        SendUserToSetupActivity();
                    }

                }



                @Override
                public void onCancelled( DatabaseError databaseError) {

                }
            });
        }
        else {
            SendUserToLoginActivity();
        }
    }

    private void SendUserToSetupActivity()
    {
        Intent setupIntent=new Intent(MainActivity1.this,SetupActivity.class);
        setupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);

    }
    @Override
    public void onStart()
    {
        super.onStart();
        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null)
        {
            SendUserToLoginActivity();
        }
        if(currentUser!=null)
        {
            CheckUserExistence();
        }

    }
}