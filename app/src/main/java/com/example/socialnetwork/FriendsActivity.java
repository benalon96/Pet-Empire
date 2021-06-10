package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends Fragment {
private RecyclerView myFriendList;
private DatabaseReference FriendsRef,UsersRef;
private FirebaseAuth mAuth;
String online_user_id;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.activity_friends, container, false);
        changeStatusBarColor();
        mAuth = FirebaseAuth.getInstance();
        online_user_id=mAuth.getCurrentUser().getUid();
        FriendsRef= FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef= FirebaseDatabase.getInstance().getReference().child("users");
        myFriendList=(RecyclerView)v.findViewById(R.id.friends_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);


        DisplayAllFriends();
        return v;
    }

    public void updateUserStatus(String state) {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentTime);
        currentStateMap.put("type", state);

        UsersRef.child(online_user_id).child("usersState")
                .updateChildren(currentStateMap);


    }

    @Override
    public void onStart()
    {
        super.onStart();
        updateUserStatus("online");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        updateUserStatus("offline");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        updateUserStatus("offline");
    }

    private void DisplayAllFriends()
    {
        FirebaseRecyclerAdapter<Friends,friendsViewView> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Friends, friendsViewView>
                (
                        Friends.class,
                        R.layout.all_users_display_layout,
                        friendsViewView.class,
                        FriendsRef
                 )
        {
            @Override
            protected void populateViewHolder(friendsViewView viewHolder, Friends friends, int i)
            {
                viewHolder.setDate(friends.getDate());
            final String usersIDs=getRef(i).getKey();
            UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
                {
                    if(snapshot.exists())
                    {
                        final String userName=snapshot.child("fullname").getValue().toString();
                        final String profileImage=snapshot.child("profileimage").getValue().toString();
                        final String type;
                        if (snapshot.hasChild("usersState"))
                        {
                            type = snapshot.child("usersState").child("type").getValue().toString();

                            if (type.equals("online"))
                            {
                                viewHolder.onlineStatusView.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                viewHolder.onlineStatusView.setVisibility(View.INVISIBLE);
                            }
                        }

                        viewHolder.setFullname(userName);
                        viewHolder.setProfileimage(getActivity(),profileImage);
                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                CharSequence options[]=new CharSequence[]
                                        {
                                                userName+"'s Profile",
                                                "Send Message"
                                        };
                                AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                                builder.setTitle("Select Option");

                                builder.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if(which==0)
                                        {
                                            Intent FriendsIntent = new Intent(getActivity(),PersonProfileActivity.class);
                                            FriendsIntent.putExtra("visit_user_id", usersIDs);
                                            startActivity(FriendsIntent);

                                        }
                                        if(which==1)
                                        {
                                            Intent ChatIntent = new Intent(getActivity(),ChatActivity.class);
                                            ChatIntent.putExtra("visit_user_id", usersIDs);
                                            ChatIntent.putExtra("userName", userName);
                                            startActivity(ChatIntent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

            }
        };
        myFriendList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class friendsViewView extends RecyclerView.ViewHolder{
        View mView;
        ImageView onlineStatusView;
        public friendsViewView(@NonNull @NotNull View itemView) {
            super(itemView);
            mView=itemView;
            onlineStatusView = (ImageView) itemView.findViewById(R.id.all_user_online_icon);
        }
        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            if (profileimage!=null)
                Picasso.get().load(profileimage).into(myImage);
        }
        public void setFullname(String fullname)
        {
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setDate(String date)
        {
            TextView FriendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            FriendsDate.setText("Friends Since: "+date);
        }

    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setBackgroundDrawableResource(R.drawable.profile_bk);
        }
    }
}