package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
//import android.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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
import com.google.firebase.storage.UploadTask;
import org.jetbrains.annotations.NotNull;
import androidx.appcompat.widget.Toolbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class PostActivity extends AppCompatActivity {
    private Toolbar mToolBar;
    private ProgressDialog loadingBar;
    private ImageButton SelectPostImage;
    private Button UpdatePostButton;
    private EditText PostDescription;

    private static final int Gallery_pick = 1;
    private Uri ImageUri;
    private String Description;
    private StorageReference PostImagesReference;
    private DatabaseReference UsersRef,PostsRef ;
    private FirebaseAuth mAuth;
    private long countPosts = 0;
    private byte[] mBytes;
    private static final double MB = 1000000.0;
    private static final double MB_THRESHHOLD = 5.0;



    private String saveCurrentDate, saveCurrentTime, postRandomName, downloadUrl, current_user_id ;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mAuth = FirebaseAuth.getInstance();
        current_user_id = mAuth.getCurrentUser().getUid();
        PostImagesReference = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("users");
        PostsRef = FirebaseDatabase.getInstance("https://socialnetwork-80b16-default-rtdb.firebaseio.com/").getReference().child("posts");
        SelectPostImage = (ImageButton) findViewById(R.id.select_post_image);
        UpdatePostButton = (Button) findViewById(R.id.update_post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);
        loadingBar = new ProgressDialog(this);


        changeStatusBarColor();
        mToolBar=(Toolbar)findViewById(R.id.update_post_page_toolbar);
        setSupportActionBar(mToolBar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Update Post");

        SelectPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                OpenGallery();
            }
        });

        UpdatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadNewPhoto(ImageUri);
            }
        });
    }

    private void ValidatePostInfo()
    {
        Description = PostDescription.getText().toString();

        if(ImageUri==null)
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please select post image...", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this, "Please select post image...", Toast.LENGTH_SHORT).show();
        }

        else if(TextUtils.isEmpty(Description))
        {
            Snackbar.make(findViewById(android.R.id.content),
                    "Please say something about your image...", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                    .setBackgroundTint(Color.WHITE).show();
            //Toast.makeText(this, "Please say something about your image...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Add New Post");
            loadingBar.setMessage("Please wait, while we are updating your new post...");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);
            StoringImageToFirebaseStorage();
        }
    }

    private void StoringImageToFirebaseStorage()
    {
        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        saveCurrentTime = currentTime.format(calForDate.getTime());

        postRandomName = saveCurrentDate + saveCurrentTime;

        StorageReference filepath = PostImagesReference.child("Postimages").child(ImageUri.getLastPathSegment() + postRandomName + ".jpg");
        filepath.putFile(ImageUri).addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {
            downloadUrl = uri.toString();
            if(downloadUrl!=null)
            {

                Snackbar.make(findViewById(android.R.id.content),
                        "image uploaded successfully to storage", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                        .setBackgroundTint(Color.WHITE).show();
                //Toast.makeText(PostActivity.this,"image uploaded successfully to storage",Toast.LENGTH_SHORT).show();
//                Bitmap bitmapPhoto=getBitmapFromURL(downloadUrl);
//                compressImage(bitmapPhoto);
//               Uri uri1=getImageUri(this,bitmapPhoto);
//               downloadUrl=uri1.toString();
                SavingPostInformationToDatabase();

            }

        }));
//
//        filepath.putFile(ImageUri).addOnCompleteListener(task -> {
//         if (task.isSuccessful())
//         {
//             downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
//             Toast.makeText(PostActivity.this,"image uploaded successfully to storage",Toast.LENGTH_SHORT).show();
//
//             SavingPostInformationToDatabase();
//         }
//         else
//         {
//             String message = task.getException().getMessage();
//             Toast.makeText(PostActivity.this,"Error occured",Toast.LENGTH_SHORT).show();
//         }
//        });
    }


    private void SavingPostInformationToDatabase()
    {
        PostsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot)
            {
                if (snapshot.exists())
                {
                    countPosts = snapshot.getChildrenCount();
                }
                else
                {
                    countPosts = 0;
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

        UsersRef.child(current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String userProfileImage;
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    if(dataSnapshot.child("profileimage").getValue().toString()!=null){
                      userProfileImage = dataSnapshot.child("profileimage").getValue().toString();
                    }
                    else{
                        userProfileImage = "drawable://" + R.drawable.profile;
                    }

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", current_user_id);
                    postsMap.put("date", saveCurrentDate);
                    postsMap.put("time", saveCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postimage", downloadUrl);
                    postsMap.put("profileimage", userProfileImage);
                    postsMap.put("fullname", userFullName);
                    postsMap.put("counter", countPosts);
                    PostsRef.child(current_user_id + postRandomName).updateChildren(postsMap)
                            .addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull @NotNull Task task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        SendUserMainActivity();
                                        Snackbar.make(findViewById(android.R.id.content),
                                                "New Post is updated successfully", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                                .setBackgroundTint(Color.WHITE).show();
                                        //Toast.makeText(PostActivity.this,"New Post is updated successfully", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                    else
                                    {
                                        Snackbar.make(findViewById(android.R.id.content),
                                                "Error Occured while updating your post.", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                                                .setBackgroundTint(Color.WHITE).show();
                                        //Toast.makeText(PostActivity.this,"Error Occured while updating your post.", Toast.LENGTH_SHORT).show();
                                        loadingBar.dismiss();
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }

    private void OpenGallery()
    {
        Intent galleryIntent=new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,Gallery_pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==Gallery_pick && resultCode==RESULT_OK && data!=null)
        {
            ImageUri = data.getData();
            SelectPostImage.setImageURI(ImageUri);

        }
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        int id=item.getItemId();
        if (id==android.R.id.home)
        {
            SendUserMainActivity();
        }
        return super.onOptionsItemSelected(item);
    }

    private void SendUserMainActivity()
    {
//        Intent mainIntent=new Intent(PostActivity.this,MainActivity1.class);
//        startActivity(mainIntent);
        finish();
    }

    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setBackgroundDrawableResource(R.drawable.profile_bk);
        }
    }
    private  Bitmap compressImage(Bitmap image) {

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
    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 50, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    public void uploadNewPhoto(Uri imageUri){
        /*
            upload a new profile photo to firebase storage
         */
//        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");

        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        PostActivity.BackgroundImageResize resize = new PostActivity.BackgroundImageResize(null);
        resize.execute(imageUri);
    }

    /**
     * Uploads a new profile photo to Firebase Storage using a @param ***imageBitmap***
     * @param imageBitmap
     */
    public void uploadNewPhoto(Bitmap imageBitmap){
        /*
            upload a new profile photo to firebase storage
         */
//        Log.d(TAG, "uploadNewPhoto: uploading new profile photo to firebase storage.");

        //Only accept image sizes that are compressed to under 5MB. If thats not possible
        //then do not allow image to be uploaded
        PostActivity.BackgroundImageResize resize = new PostActivity.BackgroundImageResize(imageBitmap);
        Uri uri = null;
        resize.execute(uri);
    }





    /**
     * 1) doinBackground takes an imageUri and returns the byte array after compression
     * 2) onPostExecute will print the % compression to the log once finished
     */
    public class BackgroundImageResize extends AsyncTask<Uri, Integer, byte[]> {

        Bitmap mBitmap;
        public BackgroundImageResize(Bitmap bm) {
            if(bm != null){
                mBitmap = bm;
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //  showDialog();
//            Log.d(TAG,"Compressing image");
        }

        @Override
        protected byte[] doInBackground(Uri... params ) {
//            Log.d(TAG, "doInBackground: started.");

            if(mBitmap == null){

                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(PostActivity.this.getContentResolver(), params[0]);
//                    Log.d(TAG, "doInBackground: bitmap size: megabytes: " + mBitmap.getByteCount()/MB + " MB");
                } catch (IOException e) {
//                    Log.e(TAG, "doInBackground: IOException: ", e.getCause());
                }
            }

            byte[] bytes = null;
            for (int i = 1; i < 11; i++){
                if(i == 10){
                    Snackbar.make(findViewById(android.R.id.content),
                            "That image is too large.", Snackbar.LENGTH_LONG).setTextColor(Color.BLACK)
                            .setBackgroundTint(Color.WHITE).show();
                    //Toast.makeText(PostActivity.this, "That image is too large.", Toast.LENGTH_SHORT).show();
                    break;
                }
                bytes = getBytesFromBitmap(mBitmap,100/i);
//                Log.d(TAG, "doInBackground: megabytes: (" + (11-i) + "0%) "  + bytes.length/MB + " MB");
                if(bytes.length/MB  < MB_THRESHHOLD){
                    return bytes;
                }
            }
            return bytes;
        }






        @Override
        protected void onPostExecute(byte[] bytes) {
            super.onPostExecute(bytes);
            // hideDialog();
            mBytes = bytes;
            //execute the upload
            ValidatePostInfo();

        }
    }
    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.copy(bitmap.getConfig(),true).compress(Bitmap.CompressFormat.JPEG, 50, stream);
        return stream.toByteArray();
    }

}
