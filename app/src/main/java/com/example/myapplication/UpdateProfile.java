package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.net.InternetDomainName;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.UUID;

public class UpdateProfile extends AppCompatActivity {
    private  TextInputEditText UserName;
    private  TextInputEditText Email;
    private Button saveE;
    private Button saveU;
    private FirebaseFirestore db;
    private ImageView imageView;
    private MaterialButton imageSelect;
    private Button button;
    private StorageReference storageReference;
    private Uri image;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        initializeView();
        setOnclickListener();
        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("TAG", user1.getEmail());

    }
    private void initializeView(){
        UserName=findViewById(R.id.UserName);
        Email=findViewById(R.id.Email);
        saveE=findViewById(R.id.saveE);
        saveU=findViewById(R.id.saveU);
        button=findViewById(R.id.Update1);
        imageView=findViewById(R.id.ImageView11);
        imageSelect=findViewById(R.id.selectImage11);
        storageReference = FirebaseStorage.getInstance().getReference();
    }
    private void setOnclickListener(){
        saveE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEmail();

            }
        });
        saveU.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateName();

            }
        });
        imageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(image);
            }
        });

    }
    @SuppressLint("RestrictedApi")
    private void updateName() {
        db = FirebaseFirestore.getInstance();
        String username = UserName.getText().toString(); // Ensure UserName is initialized and contains the correct value
        db.collection("users").whereEqualTo("username", getIntent().getStringExtra("username")).get().addOnCompleteListener(
                user -> {
                    if (user.isSuccessful()) {
                        if (!user.getResult().isEmpty()) { // Check if the query result contains any documents
                            for (QueryDocumentSnapshot document : user.getResult()) {
                                String userId = document.getId();
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("username", username);

                                db.collection("users").document(userId).update(map)
                                        .addOnSuccessListener(aVoid -> {
                                            showToast("Username updated successfully");
                                            goToMainActivity();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "Error updating username: ", e);
                                            showToast("Failed to update username");
                                        });
                            }
                        } else {
                            showToast("No user found with the provided username");
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", user.getException());
                        showToast("Failed to retrieve username");
                    }
                }
        );
    }


    @SuppressLint("RestrictedApi")
    private void updateEmail() {
        db = FirebaseFirestore.getInstance();
        String email = Email.getText().toString(); // Ensure Email is initialized and contains the correct value
        FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
        if(user1!=null){
            user1.updateEmail(email);
        }

        db.collection("users").whereEqualTo("email", getIntent().getStringExtra("email")).get().addOnCompleteListener(
                user -> {
                    if (user.isSuccessful()) {
                        if (!user.getResult().isEmpty()) { // Check if the query result contains any documents
                            for (QueryDocumentSnapshot document : user.getResult()) {
                                String userId = document.getId();
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("email", email);

                                db.collection("users").document(userId).update(map)
                                        .addOnSuccessListener(aVoid -> {
                                            showToast("Email updated successfully");
                                            goToMainActivity();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "Error updating email: ", e);
                                            showToast("Failed to update email");
                                        });
                            }
                        } else {
                            showToast("No user found with the provided email");
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", user.getException());
                        showToast("Failed to retrieve email");
                    }
                }
        );
    }
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    image = result.getData().getData();
                    Glide.with(UpdateProfile.this).load(image).into(imageView);
                }
            } else {
                showToast("Please select an image");
            }
        }
    });

    private void uploadImage(Uri file) {
        if (file != null) {

            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(file)
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> UpdateImage(downloadUri)))
                    .addOnFailureListener(e -> showToast("Failed!" + e.getMessage()));
        } else {
            showToast("No image selected");
        }
    }
    @SuppressLint("RestrictedApi")
    private void UpdateImage(Uri image){
        db = FirebaseFirestore.getInstance();
        String username = UserName.getText().toString(); // Ensure UserName is initialized and contains the correct value
        db.collection("users").whereEqualTo("username", getIntent().getStringExtra("username")).get().addOnCompleteListener(
                user -> {
                    if (user.isSuccessful()) {
                        if (!user.getResult().isEmpty()) { // Check if the query result contains any documents
                            for (QueryDocumentSnapshot document : user.getResult()) {
                                String userId = document.getId();
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("image", image.toString());

                                db.collection("users").document(userId).update(map)
                                        .addOnSuccessListener(aVoid -> {
                                            showToast("Image updated successfully");
                                            goToMainActivity();
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.d(TAG, "Error updating username: ", e);
                                            showToast("Failed to update Image");
                                        });
                            }
                        } else {
                            showToast("No user found with the provided image");
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", user.getException());
                        showToast("Failed to retrieve username");
                    }
                }
        );

    }

    private void goToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}
