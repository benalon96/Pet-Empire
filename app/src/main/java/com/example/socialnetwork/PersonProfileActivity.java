package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonProfileActivity extends AppCompatActivity
{
    private TextView userName, userProfName, userStatus, userGender, userRelation, userDOB,userCountry,userAnimal;
    private CircleImageView userProfileImage;
    private TextView SendFriendRequestButton, DeclineFriendRequestButton;
    private Toolbar mToolbar;
    private CircleImageView userProImage;
    private DatabaseReference FriendRequestRef, UsersRef,FriendsRef;
    private FirebaseAuth mAuth;
    private String senderUserId, receiverUserId, CURRENT_STATE,saveCurrentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person_profile);
        changeStatusBarColor();
        mAuth = FirebaseAuth.getInstance();
        senderUserId = mAuth.getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("users");
        FriendRequestRef = FirebaseDatabase.getInstance().getReference().child("FriendRequests");
        FriendsRef=FirebaseDatabase.getInstance().getReference().child("Friends");
        InitializeFields();
        UsersRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
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
                    userName.setText("@" +myUserName);
                    userProfName.setText(myUserProfName);
                    userStatus.setText(myUserStatus);
                    userCountry.setText(myUserCountry);
                    userGender.setText(myUserGender);
                    userRelation.setText(myUserRelation);
                    userDOB.setText(myUserDOB);
                    userAnimal.setText(myUserAnimal);
                   MaintananceofButtons();
                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
        DeclineFriendRequestButton.setEnabled(false);

        if (!senderUserId.equals(receiverUserId))
        {
            SendFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    SendFriendRequestButton.setEnabled(false);
                    if (CURRENT_STATE.equals("not_friends"))
                    {
                        SendFriendRequestToaPerson();
                    }
                    if (CURRENT_STATE.equals("request_sent"))
                    {
                       CancelFriendRequest();
                    }
                    if (CURRENT_STATE.equals("request_received"))
                    {
                        AcceptFriendRequest();
                    }
                    if (CURRENT_STATE.equals("friends"))
                    {
                        UnFriendAnExistingFriend();
                    }
                }


            });
        }
        else
        {
            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
            SendFriendRequestButton.setVisibility(View.INVISIBLE);
        }

   }

    private void UnFriendAnExistingFriend()
    {
        FriendsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        FriendsRef.child(receiverUserId).child(senderUserId)
                                .removeValue()
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful())
                                    {
                                        SendFriendRequestButton.setEnabled(true);
                                        CURRENT_STATE = "not_friends";
                                        SendFriendRequestButton.setText("Send Friend Request");

                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                        DeclineFriendRequestButton.setEnabled(false);
                                    }
                                });
                    }
                });
    }

    private void AcceptFriendRequest()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());
        FriendsRef.child(senderUserId).child(receiverUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<Void> task)
            {
              if(task.isSuccessful())
              {
                  FriendsRef.child(receiverUserId).child(senderUserId).child("date").setValue(saveCurrentDate).addOnCompleteListener(new OnCompleteListener<Void>() {
                      @Override
                      public void onComplete(@NonNull @NotNull Task<Void> task)
                      {
                          if(task.isSuccessful())
                          {
                              FriendRequestRef.child(senderUserId).child(receiverUserId)
                                      .removeValue()
                                      .addOnCompleteListener(task1 -> {
                                          if (task1.isSuccessful())
                                          {
                                              FriendRequestRef.child(receiverUserId).child(senderUserId)
                                                      .removeValue()
                                                      .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                          @Override
                                                          public void onComplete(@NonNull @NotNull Task<Void> task)
                                                          {
                                                              if (task.isSuccessful())
                                                              {
                                                                  SendFriendRequestButton.setEnabled(true);
                                                                  CURRENT_STATE = "friends";
                                                                  SendFriendRequestButton.setText("Unfriend this Person");

                                                                  DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                                  DeclineFriendRequestButton.setEnabled(false);
                                                              }
                                                          }
                                                      });
                                          }
                                      });
                          }
                      }
                  });
              }
            }
        });

    }

    private void CancelFriendRequest()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        FriendRequestRef.child(receiverUserId).child(senderUserId)
                                .removeValue()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull @NotNull Task<Void> task)
                                    {
                                        if (task.isSuccessful())
                                        {
                                            SendFriendRequestButton.setEnabled(true);
                                            CURRENT_STATE = "not_friends";
                                            SendFriendRequestButton.setText("Send Friend Request");

                                            DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                            DeclineFriendRequestButton.setEnabled(false);
                                        }
                                    }
                                });
                    }
                });
    }

    private void MaintananceofButtons()
    {
        FriendRequestRef.child(senderUserId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
                    {
                        if (snapshot.hasChild(receiverUserId))
                        {
                            String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent"))
                            {
                                CURRENT_STATE = "request_sent";
                                SendFriendRequestButton.setText("Cancel Friend request");

                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                DeclineFriendRequestButton.setEnabled(false);
                            }
                            else if(request_type.equals("received"))
                            {
                                CURRENT_STATE="request_received";
                                SendFriendRequestButton.setText("Accept friend Request");
                                DeclineFriendRequestButton.setVisibility(View.VISIBLE);
                                DeclineFriendRequestButton.setEnabled(true);
                                DeclineFriendRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        CancelFriendRequest();
                                    }
                                });
                            }
                        }
                        else{
                            FriendsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
                                        {
                                            if(snapshot.hasChild(receiverUserId))
                                            {
                                                CURRENT_STATE="friends";
                                                SendFriendRequestButton.setText("Unfriend this Person");
                                                DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineFriendRequestButton.setEnabled(false);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                    }
                });
    }

    private void SendFriendRequestToaPerson()
    {
        FriendRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            FriendRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull @NotNull Task<Void> task)
                                                {
                                                    if (task.isSuccessful())
                                                    {
                                                        SendFriendRequestButton.setEnabled(true);
                                                        CURRENT_STATE = "request_sent";
                                                        SendFriendRequestButton.setText("Cancel friend request");

                                                        DeclineFriendRequestButton.setVisibility(View.INVISIBLE);
                                                        DeclineFriendRequestButton.setEnabled(false);
                                                    }
                                                }
                                            });
                        }
                    }
                });
    }

    private void InitializeFields()
    {
        mToolbar=(Toolbar)findViewById(R.id.profile_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        userName=(TextView) findViewById(R.id.my_username);
        userProfName=(TextView)findViewById(R.id.my_profile_full_name);
        userStatus=(TextView)findViewById(R.id.my_status);
        userCountry=(TextView)findViewById(R.id.my_country);
        userGender=(TextView)findViewById(R.id.my_gender);
        userRelation=(TextView)findViewById(R.id.my_relationship_status);
        userDOB=(TextView)findViewById(R.id.my_dob);
        userAnimal=(TextView)findViewById(R.id.my_Animal);
        userProImage=(CircleImageView)findViewById(R.id.my_profile_pic);
        SendFriendRequestButton=(TextView) findViewById(R.id.person_send_friend_request_btn);
        DeclineFriendRequestButton=(TextView)findViewById(R.id.person_decline_friend_request);
        CURRENT_STATE = "not_friends";
    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setBackgroundDrawableResource(R.drawable.profile_bk);
        }
    }
}