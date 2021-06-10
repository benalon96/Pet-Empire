package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends Fragment {
private TextView userName,userProfName,userStatus,userCountry,userGender,userRelation,userDOB,userAnimal;
    private FirebaseAuth mAuth;
    private int countFriends = 0, countPosts = 0;

    private RecyclerView myPostsList;

    private DatabaseReference PostsRef, UsersRef, LikesRef;
    static String currentUserID;

    Boolean LikeChecker = false;
    private String currentUserId;
    private StorageReference UserProfileImageRef;
    private CircleImageView userProImage;
    private TextView edit_profile;
    private DatabaseReference ProfileUserRef, FriendsRef ;
    private Button MyPosts, MyFriends;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.activity_profile, container, false);
//        changeStatusBarColor();

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        PostsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        UsersRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        myPostsList = (RecyclerView)v.findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        myPostsList.setNestedScrollingEnabled(false);
        float density = getResources().getDisplayMetrics().density;
        myPostsList.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ((int) (density + 30)) * 50));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
//        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
//        linearLayoutManager.setAutoMeasureEnabled( true );
//        myPostsList.setLayoutManager(new LinearLayoutManager(myPostsList.getContext()));
        myPostsList.setLayoutManager(linearLayoutManager);

        DisplayMyAllPosts();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        currentUserId=mAuth.getCurrentUser().getUid();
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("profile Images");
        ProfileUserRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users").child(currentUserId);
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends");
        edit_profile=(TextView)v.findViewById(R.id.edit_profile);
        userName=(TextView) v.findViewById(R.id.my_username);
        userProfName=(TextView)v.findViewById(R.id.my_profile_full_name);
        userStatus=(TextView)v.findViewById(R.id.my_status);
        userCountry=(TextView)v.findViewById(R.id.my_country);
        userGender=(TextView)v.findViewById(R.id.my_gender);
        userRelation=(TextView)v.findViewById(R.id.my_relationship_status);
        userDOB=(TextView)v.findViewById(R.id.my_dob);
        userAnimal=(TextView)v.findViewById(R.id.my_Animal);
        userProImage=(CircleImageView)v.findViewById(R.id.my_profile_pic);
        myPostsList.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });


        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendTOSettingsActivity();
            }
        });
        ProfileUserRef.addValueEventListener(new ValueEventListener() {
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
                    Picasso.get().load(myProfileImage).into(userProImage);
//                    Glide.with(getContext())
//                            .applyDefaultRequestOptions(new RequestOptions()
//                                    .placeholder(R.drawable.profile)
//                                    .error(R.drawable.profile))
//                            .load(myProfileImage).into(userProImage);
                    userName.setText("@"+myUserName);
                    userProfName.setText(myUserProfName);
                    userStatus.setText(myUserStatus);
                    userCountry.setText(myUserCountry);
                    userGender.setText(myUserGender);
                    userRelation.setText(myUserRelation);
                    userDOB.setText(myUserDOB);
//                    userAnimal.setText("Animal Lover:"+myUserAnimal);




                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    return v;
    }
//    private void changeStatusBarColor() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setBackgroundDrawableResource(R.drawable.profile_bk);
//        }
//    }
    private void SendTOSettingsActivity()
    {
        Intent SettingsIntent=new Intent(getActivity(),SettingsActivity.class);
        startActivity(SettingsIntent);
    }

    private void SendTOFriendsActivity()
    {
        Intent FriendsIntent=new Intent(getActivity(),FriendsActivity.class);
        startActivity(FriendsIntent);
    }

//    private void SendTOMyPostsActivity()
//    {
//        Intent FriendsIntent=new Intent(ProfileActivity.this,MyPostsActivity.class);
//        startActivity(FriendsIntent);
//    }
    private void DisplayMyAllPosts() {
        Query myPostsQuery =  PostsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff");
        FirebaseRecyclerAdapter<Posts, ProfileActivity.MyPostsViewHolder> firebaseRecyclerAdapter
                =new FirebaseRecyclerAdapter<Posts, ProfileActivity.MyPostsViewHolder>
                (
                        Posts.class,
                        R.layout.all_posts_layout,
                        ProfileActivity.MyPostsViewHolder.class,
                        myPostsQuery

                )
        {
            @Override
            protected void populateViewHolder(ProfileActivity.MyPostsViewHolder myPostsViewHolder, Posts model, int position)
            {
                final String Postkey=getRef(position).getKey();

                myPostsViewHolder.setFullname(model.getFullname());
                myPostsViewHolder.setTime(model.getTime());
                myPostsViewHolder.setDate(model.getDate());
                myPostsViewHolder.setDescription(model.getDescription());
                myPostsViewHolder.setProfileimage(getContext(),model.getProfileimage());
                myPostsViewHolder.setPostimage(getContext(),model.getPostimage());

                myPostsViewHolder.setLikeButtonStatus(Postkey);

                myPostsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent clickPostIntent=new Intent(getActivity(),ClickPostActivity.class);
                        //if (clickPostIntent.getExtras()==null)
                        clickPostIntent.putExtra("Postkey",Postkey);
                        startActivity(clickPostIntent);
                    }
                });
                myPostsViewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        Intent commentsIntent=new Intent(getActivity(),ClickPostActivity.class);
                        commentsIntent.putExtra("Postkey",Postkey);
                        startActivity(commentsIntent);

                    }
                });

                myPostsViewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v)
                    {
                        LikeChecker = true;
                        LikesRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot)
                            {
                                if (LikeChecker.equals(true))
                                {
                                    if((snapshot).child(Postkey).hasChild(currentUserID))
                                    {
                                        LikesRef.child(Postkey).child(currentUserID).removeValue();
                                        LikeChecker = false;
                                    }
                                    else
                                    {
                                        LikesRef.child(Postkey).child(currentUserID).setValue(true);
                                        LikeChecker = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled( DatabaseError error)
                            {

                            }
                        });
                    }
                });
            }
        };
        myPostsList.setAdapter(firebaseRecyclerAdapter);
    }
    public static class MyPostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes;
        int countLikes;
        DatabaseReference LikesRef;

        public MyPostsViewHolder(@NonNull @NotNull View itemView)
        {
            super(itemView);

            mView = itemView;

            LikePostButton = (ImageButton)mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton)mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView)mView.findViewById(R.id.display_number_of_likes);
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        public void setLikeButtonStatus(final String Postkey)
        {
            LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot snapshot)
                {
                    if (snapshot.child(Postkey).hasChild(currentUserID))
                    {
                        countLikes = (int) snapshot.child(Postkey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+("Likes")));
                    }
                    else
                    {
                        countLikes = (int) snapshot.child(Postkey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.heart);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+("Likes")));
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });
        }

        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileimage(Context ctx, String profileimage)
        {
            ImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);

            Glide.with(mView.getContext())
                    .applyDefaultRequestOptions(new RequestOptions()
                            .placeholder(R.drawable.profile)
                            .error(R.drawable.profile))
                    .load(profileimage).into(image);


        }

        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("   " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("   " +  date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(Context ctx1, String postimage)
        {
            //final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            //Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
//            Picasso.get().load(postimage).into(PostImage);
            Glide.with(ctx1).load(postimage).into(PostImage);

//            Picasso.get(ctx1).load(postimage).into(PostImage);

        }
    }

}
