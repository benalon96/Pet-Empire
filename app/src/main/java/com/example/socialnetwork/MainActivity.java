package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends Fragment {
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private RecyclerView postList;
    private Context context;
    private String currentUserID;
    private StorageReference PostImage;
    private ImageButton AddNewPostButton;
    private CircleImageView NavProfileImage;
    private ImageView NavPostImage;
    private TextView NavProfileUserImage;
    private static StorageReference UserProfileImageRef;
    private FirebaseAuth mAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference UserRef, PostsRef,LikesRef,commentsRef;

    Boolean LikeChecker = false;
    private Toolbar  mToolbar;
    public MainActivity(){}
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        View v = inflater.inflate(R.layout.activity_main, container, false);
        mToolbar = (Toolbar) v.findViewById(R.id.main_page_toolBar);
        AddNewPostButton=(ImageButton)v.findViewById(R.id.add_new_post_button);
        mAuth=FirebaseAuth.getInstance();
        UserRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("posts");
        UserProfileImageRef= FirebaseStorage.getInstance().getReference().child("posts");
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        commentsRef = FirebaseDatabase.getInstance().getReference().child("posts").child("comments");

         mFirebaseUser = mAuth.getCurrentUser();
        if(mFirebaseUser != null) {
            currentUserID = mFirebaseUser.getUid();
        }
        navigationView=(NavigationView)v.findViewById(R.id.navigation_view);

        postList = (RecyclerView)v.findViewById(R.id.all_users_post_list);
        postList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        postList.setLayoutManager(linearLayoutManager);
        DisplayAllUsersPosts();
        AddNewPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserTOPostActivity();
            }
        });
        return v;
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

//        UserRef.child(currentUserID).child("usersState")
//                .updateChildren(currentStateMap);


    }

    private void DisplayAllUsersPosts()
    {
        Query SortPostsInDecendingOrder = PostsRef.orderByChild("counter");
        FirebaseRecyclerAdapter<Posts, PostsViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Posts, PostsViewHolder>
                        (
                                Posts.class,
                                R.layout.all_posts_layout,
                                PostsViewHolder.class,
                                SortPostsInDecendingOrder
                        )
                {
                    @Override
                    protected void populateViewHolder(PostsViewHolder viewHolder, Posts model, int position)
                    {
                       final String Postkey=getRef(position).getKey();
                        viewHolder.setFullname(model.getFullname());
                        viewHolder.setTime(model.getTime());
                        viewHolder.setDate(model.getDate());
                        viewHolder.setDescription(model.getDescription());
                        viewHolder.setProfileimage(getContext(),model.getProfileimage());
                        viewHolder.setPostimage(getContext(),model.getPostimage());

                        viewHolder.setLikeButtonStatus(Postkey);

                        viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent clickPostIntent=new Intent(getActivity(),ClickPostActivity.class);
                                clickPostIntent.putExtra("Postkey",Postkey);
                                startActivity(clickPostIntent);
                            }
                        });
                        viewHolder.CommentPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Intent commentsIntent=new Intent(getActivity(),ClickPostActivity.class);
                                commentsIntent.putExtra("Postkey",Postkey);
                                startActivity(commentsIntent);

                            }
                        });
                        viewHolder.LikePostButton.setOnClickListener(new View.OnClickListener() {
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
        firebaseRecyclerAdapter.startListening();
        postList.setAdapter(firebaseRecyclerAdapter);

        updateUserStatus("online");
    }

    public static class PostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        private String currentUserID;


        ImageButton LikePostButton, CommentPostButton;
        TextView DisplayNoOfLikes,DisplayNoOfComments;
        int countLikes;
        int countComment;
        DatabaseReference LikesRef;


        public PostsViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            LikePostButton = (ImageButton)mView.findViewById(R.id.like_button);
            CommentPostButton = (ImageButton)mView.findViewById(R.id.comment_button);
            DisplayNoOfLikes = (TextView)mView.findViewById(R.id.display_number_of_likes);
            LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
            DisplayNoOfComments = (TextView)mView.findViewById(R.id.display_number_of_comments);
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
                        if(snapshot.child(Postkey).child("comments").exists()){
                            countComment=(int)snapshot.child(Postkey).child("comments").getChildrenCount();
                            DisplayNoOfComments.setText(String.valueOf(countComment)+"Comments");
                        }
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
            final StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            //Picasso.with(MainActivity.this).load(image).placeholder(R.drawable.profile).into(NavProfileImage);
//            Picasso.get().load(postimage).into(PostImage);
            Glide.with(ctx1).load(postimage).into(PostImage);

//            Picasso.get(ctx1).load(postimage).into(PostImage);

        }
    }

    private void SendUserTOPostActivity()
    {
        Intent addNewPostIntent=new Intent(getActivity(),PostActivity.class);
        startActivity(addNewPostIntent);
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
        Intent setupIntent=new Intent(getActivity(),SetupActivity.class);
        setupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);

    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent=new Intent(getActivity(),LoginActivity.class);
        loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);


    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

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

               Toast.makeText(getContext(),"Home",Toast.LENGTH_SHORT).show();
               break;
           case R.id.nav_friends:
               SendTOFriendsActivity();
               break;
           case R.id.nav_find_friends:
            SendTOFindFriendsActivity();

               break;
           case R.id.nev_messages:
               Toast.makeText(getContext(),"Messages",Toast.LENGTH_SHORT).show();
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
    private void SendTOFriendsActivity()
    {
        Intent FriendsIntent=new Intent(getActivity(),FriendsActivity.class);
        startActivity(FriendsIntent);
    }
    private void SendTOSettingsActivity()
    {
        Intent SettingsIntent=new Intent(getActivity(),SettingsActivity.class);
        startActivity(SettingsIntent);
    }
    private void SendTOMenuSettingsActivity()
    {
        Intent SettingsIntent1=new Intent(getActivity(),MenuSettingsActivity.class);
        startActivity(SettingsIntent1);
    }

    private void SendTOFindFriendsActivity()
    {
        Intent SettingsIntent=new Intent(getActivity(),FindFriendsActivity.class);
        startActivity(SettingsIntent);
    }

    private void SendTOProfileActivity()
    {
        Intent ProfileIntent=new Intent(getActivity(),ProfileActivity.class);
        startActivity(ProfileIntent);
    }

//    private void changeStatusBarColor() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//           window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setBackgroundDrawableResource(R.drawable.profile_bk);
//        }
//    }




}