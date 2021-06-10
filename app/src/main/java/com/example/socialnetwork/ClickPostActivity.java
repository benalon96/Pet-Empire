package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
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

import de.hdodenhof.circleimageview.CircleImageView;


public class ClickPostActivity extends AppCompatActivity {
    private ImageView PostImage;
    private Button DeletePostBtn,EditPostBtn;
    private DatabaseReference UsersRef, PostsRef;
    private String currentUserID;
    private CircleImageView userNameImage;
    private RecyclerView CommentsList;
    Boolean LikeChecker;
    Boolean CommentChecker;
    private String userNameCom,timeCom,dateCom,profileCom;
    private ImageButton PostCommentButton;
    private TextView PostUserName,TimePost,DateCom,PostDescriptionCom;
    private EditText CommentInputText;
    private FirebaseAuth mAuth;
    private Toolbar mToolBar;
    private  String imageProfile;
    ImageButton LikePostButton, CommentPostButton;
    TextView DisplayNoOfLikes;
    TextView DisplayNoOfComments;
    int countLikes;
    int countComments;
    DatabaseReference LikesRef, CommentsRef;
    private String PostKey,databaseUserID,description,image;
    private DatabaseReference ClickPostRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_post);
        changeStatusBarColor();
        mAuth=FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();
        PostKey=getIntent().getExtras().get("Postkey").toString();
        PostImage=(ImageView)findViewById(R.id.post_image_com);
        PostUserName=(TextView)findViewById(R.id.post_user_name_com);
        TimePost=(TextView)findViewById(R.id.post_time_com);
        DateCom=(TextView)findViewById(R.id.post_date_com);
        PostDescriptionCom=(TextView)findViewById(R.id.post_description_com);
        LikePostButton=(ImageButton) findViewById(R.id.like_button);
        userNameImage=(CircleImageView)findViewById(R.id.post_profile_image);
        CommentPostButton=(ImageButton)findViewById(R.id.comment_button);
        DisplayNoOfLikes =(TextView) findViewById(R.id.display_number_of_likes);
        DisplayNoOfComments =(TextView) findViewById(R.id.display_number_of_comments);
        LikesRef = FirebaseDatabase.getInstance().getReference().child("Likes");
        CommentsRef = FirebaseDatabase.getInstance().getReference().child("comments");
        currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        PostsRef = FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("posts").child(PostKey).child("comments");
        ClickPostRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("posts").child(PostKey);
        UsersRef= FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users");
        DeletePostBtn=(Button)findViewById(R.id.click_delete_post);
        EditPostBtn=(Button)findViewById(R.id.click_edit_post);
        DeletePostBtn.setVisibility(View.INVISIBLE);
        CommentsList = (RecyclerView) findViewById(R.id.recyclerView);
        CommentsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mToolBar=(Toolbar)findViewById(R.id.post_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Post");
        CommentsList.setLayoutManager(linearLayoutManager);
        CommentInputText = (EditText) findViewById(R.id.comment_input);
        PostCommentButton = (ImageButton)findViewById(R.id.post_comment_button);
        PostUserName.setOnClickListener(v -> {
            SendTOPersonProfileActivity();

        });
        userNameImage.setOnClickListener(v -> {
            SendTOPersonProfileActivity();

        });
        PostDescriptionCom.setOnClickListener(v -> {
            SendTOPersonProfileActivity();

        });
        EditPostBtn.setVisibility(View.INVISIBLE);
        LikePostButton.setOnClickListener(new View.OnClickListener() {
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
                                         if((snapshot).child(PostKey).hasChild(currentUserID))
                                         {
                                             LikesRef.child(PostKey).child(currentUserID).removeValue();
                                             LikeChecker = false;
                                         }
                                        else
                                         {
                                             LikesRef.child(PostKey).child(currentUserID).setValue(true);
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

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CommentChecker = true;
                CommentsRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot)
                    {
                        if (CommentChecker.equals(true))
                        {
                            if((snapshot).child(PostKey).hasChild(currentUserID))
                            {
                                CommentsRef.child(PostKey).child(currentUserID).removeValue();
                                CommentChecker = false;
                            }
                            else
                            {
                                CommentsRef.child(PostKey).child(currentUserID).setValue(true);
                                CommentChecker = false;
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





        LikesRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange( DataSnapshot snapshot)
                {
                    if (snapshot.child(PostKey).hasChild(currentUserID))
                    {
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.like);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+("Likes")));
                    }
                    else
                    {
                        countLikes = (int) snapshot.child(PostKey).getChildrenCount();
                        LikePostButton.setImageResource(R.drawable.heart);
                        DisplayNoOfLikes.setText((Integer.toString(countLikes)+("Likes")));
                    }
                }

                @Override
                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                }
            });

        PostCommentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                UsersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot)
                    {
                        if (snapshot.exists())
                        {
                             imageProfile=snapshot.child("profileimage").getValue().toString();
                            String userName = snapshot.child("username").getValue().toString();
                            ValidateComment(userName,imageProfile);
                            CommentInputText.setText("");
                            DisplayNoOfComments.setText((Integer.toString(countComments)+("comments")));
                        }
                    }


                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        ClickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange( DataSnapshot snapshot)
            {
                if(snapshot.exists())
                {
                    description=snapshot.child("description").getValue().toString();
                    image=snapshot.child("postimage").getValue().toString();
                    timeCom=snapshot.child("time").getValue().toString();
                    dateCom=snapshot.child("date").getValue().toString();
                    userNameCom=snapshot.child("fullname").getValue().toString();
                    profileCom=snapshot.child("profileimage").getValue().toString();
                    databaseUserID=snapshot.child("uid").getValue().toString();
                    PostDescriptionCom.setText(description);
                    TimePost.setText(timeCom);
                    DateCom.setText(dateCom);
                    Picasso.get().load(profileCom).into(userNameImage);
                    PostUserName.setText(userNameCom);
//                    Picasso.get().load(image).into(PostImage);
                    Glide.with(getApplicationContext()).load(image).into(PostImage);
                    if(currentUserID.equals(databaseUserID))
                    {
                        DeletePostBtn.setVisibility(View.VISIBLE);
                        EditPostBtn.setVisibility(View.VISIBLE);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
        DeletePostBtn.setOnClickListener(v -> DeleteCurrentPost());
          EditPostBtn.setOnClickListener(v -> {
           EditCurrentPost(description);

    });
    }
    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseRecyclerAdapter<Comments, ClickPostActivity.CommentsViewHolder>firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Comments, ClickPostActivity.CommentsViewHolder>
                (
                        Comments.class,
                        R.layout.all_comments_layout,
                        ClickPostActivity.CommentsViewHolder.class,
                        PostsRef

                )
        {
            @Override
            protected void populateViewHolder(ClickPostActivity.CommentsViewHolder viewHolder, Comments model, int position1)
            {
                viewHolder.setUsername(model.getUsername());
                viewHolder.setComment(model.getComment());
                viewHolder.setDate(model.getDate());
                viewHolder.setTime(model.getTime());
                viewHolder.setProfileimage(ClickPostActivity.this, model.getProfileimage());
//                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String visit_user_id = getRef(position1).getKey();
//                        Intent profileIntent1 = new Intent(ClickPostActivity.this,PersonProfileActivity.class);
//                        profileIntent1.putExtra("visit_user_id", visit_user_id);
//                        startActivity(profileIntent1);
//                    }
//                });
            }
        };
        CommentsList.setAdapter(firebaseRecyclerAdapter);
    }

    private void ValidateComment(String userName,String imageProfile)
    {
        String commentText = CommentInputText.getText().toString();
        if (TextUtils.isEmpty(commentText))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "please write text to comment... ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this,"please write text to comment...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
            final String saveCurrentDate = currentDate.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
            final String saveCurrentTime = currentTime.format(calForDate.getTime());

            final String RandomKey = currentUserID + saveCurrentDate + saveCurrentTime;

            HashMap commentMap = new HashMap();
            commentMap.put("uid", currentUserID);
            commentMap.put("comment", commentText);
            commentMap.put("date", saveCurrentDate);
            commentMap.put("time", saveCurrentTime);
            commentMap.put("profileimage",imageProfile);
            commentMap.put("username", userName);
            if (PostsRef!=null)
                PostsRef.child(RandomKey).updateChildren(commentMap)
                        .addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete( @NonNull Task task)
                            {
                                if (task.isSuccessful())
                                {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "you have commented successfully... ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                            .setBackgroundTint(Color.WHITE).show();

                                }
                                else
                                {
                                    Snackbar.make(findViewById(android.R.id.content),
                                            "Error occured, try again... ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                            .setBackgroundTint(Color.WHITE).show();

                                }
                            }
                        });
        }
    }


    public static class CommentsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public CommentsViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }
        public void setUsername(String username)
        {
            TextView myUserName = (TextView) mView.findViewById(R.id.comment_username);
            myUserName.setText("@"+username+"  ");
        }
        public void setComment(String comment)
        {
            TextView myComment = (TextView) mView.findViewById(R.id.comment_text);
            myComment.setText(comment);
        }
        public void setDate(String date)
        {
            TextView myDate = (TextView) mView.findViewById(R.id.comment_date);
            myDate.setText("  Date: "+date);
        }
        public void setTime(String time)
        {
            TextView myTime = (TextView) mView.findViewById(R.id.comment_time);
            myTime.setText("  Time: "+time);
        }
        public void setProfileimage(Context ctx, String profileimage)
        {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.comment_username_prof);
            if (profileimage!=null)
                Picasso.get().load(profileimage).into(myImage);

        }
    }
    private void EditCurrentPost(String description)
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(ClickPostActivity.this);
        builder.setTitle("Edit Post");
        final EditText editText=new EditText(ClickPostActivity.this);
        editText.setText(description);
        builder.setView(editText);
        builder.setPositiveButton("Update", (dialog, which) -> {
            ClickPostRef.child("description").setValue(editText.getText().toString());

            Snackbar.make(findViewById(android.R.id.content),
                    "Post Updated successfully... ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(ClickPostActivity.this,"Post Updated successfully...",Toast.LENGTH_SHORT).show();

        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
               dialog.cancel();

            }
        });
        Dialog dialog=builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.white);
    }


    private void DeleteCurrentPost()
    {
       ClickPostRef.removeValue();
       SendUserTOMainActivity();
        Snackbar.make(findViewById(android.R.id.content),
                "Post has been deleted. ", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                .setBackgroundTint(Color.WHITE).show();
        //Toast.makeText(this,"Post has been deleted.",Toast.LENGTH_SHORT).show();

    }
    private void SendUserTOMainActivity()
    {
        Intent mainIntent=new Intent(ClickPostActivity.this,MainActivity1.class);
        mainIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setBackgroundDrawableResource(R.drawable.profile_bk);
        }
    }
    private void SendTOPersonProfileActivity()
    {
        Intent PersonProfileIntent=new Intent(ClickPostActivity.this,PersonProfileActivity.class);
        PersonProfileIntent.putExtra("visit_user_id",databaseUserID);
        startActivity(PersonProfileIntent);
    }

}