package com.example.moetaz.chathub.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.moetaz.chathub.R;
import com.example.moetaz.chathub.help.Utilities;
import com.firebase.client.Firebase;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;

import static com.example.moetaz.chathub.help.FirebaseConstants.USERINFO_NODE;

public class ProfileActivity extends AppCompatActivity {
    private static final int REQUET_CODE = 1;
    ImageView imageView;
    Firebase mCheck;
    TextView textViewEmail,textViewUsername;
    ProgressDialog progressDialog;
    private StorageReference storageReference;
    DatabaseReference DatabaseRef;
    private Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        progressDialog = new ProgressDialog(this);
          toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        mCheck = new Firebase("https://chathub-635f9.firebaseio.com/usersinfo/"+Utilities.getUserId());
        storageReference = FirebaseStorage.getInstance().getReference();
        imageView = findViewById(R.id.profile_pic);
        textViewEmail = findViewById(R.id.textView_email);
        textViewUsername = findViewById(R.id.textView_username);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Profile");
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,REQUET_CODE);
            }
        });

          DatabaseRef = FirebaseDatabase.getInstance().getReference().child(USERINFO_NODE)
                .child(Utilities.getUserId());
        DatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StorageReference filepath = storageReference.child("usersProfilePic/" + Utilities.getUserId() + ".jpg");
                if (dataSnapshot.hasChild("hasProfilePic")) {
                    Glide.with(getApplicationContext()).using(new FirebaseImageLoader())
                            .load(filepath).into(imageView);
                }else {
                    imageView.setBackgroundResource(R.drawable.avatar);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        setEmailInfo();
        setNameInfo();

    }

    private void setNameInfo() {

        DatabaseRef.child("userName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textViewUsername.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setEmailInfo() {
        DatabaseRef.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                textViewEmail.setText(dataSnapshot.getValue(String.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUET_CODE && resultCode == RESULT_OK) {
           progressDialog.setMessage("Upload");
           progressDialog.show();

            Bundle bundle = data.getExtras();
            Bitmap bitmap = (Bitmap) bundle.get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bData = baos.toByteArray();
            imageView.setImageBitmap(bitmap);

            StorageReference filepath = storageReference.child("usersProfilePic/" + Utilities.getUserId() + ".jpg");
            filepath.putBytes(bData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Firebase childRef3 = mCheck.child("hasProfilePic");
                    childRef3.setValue("1");
                    progressDialog.dismiss();
                    Uri downloadurl = taskSnapshot.getDownloadUrl();
                    Picasso.with(getApplicationContext()).load(downloadurl).into(imageView);
                    //GlobalVariables.message(getActivity(), "Finished");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });
        }
    }
}