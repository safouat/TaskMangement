package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddTask extends AppCompatActivity {
    private TextInputEditText title;
    private TextInputEditText description;
    private TextInputEditText deadline;
    private Button button;
    private MaterialButton imageSelect;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView imageView;
    private StorageReference storageReference; // Declare storageReference here
    private Uri image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference(); // Initialize storageReference

        title = findViewById(R.id.title1);
        description = findViewById(R.id.description1);
        deadline = findViewById(R.id.deadline1);
        imageSelect = findViewById(R.id.selectImage);
        imageView = findViewById(R.id.ImageView);

        button = findViewById(R.id.Add);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titleValue = title.getText().toString();
                String descriptionValue = description.getText().toString();
                String deadlineValue = deadline.getText().toString();
                AddData(titleValue, descriptionValue, deadlineValue);
                uploadImage(image);
            }
        });

        imageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch image picker intent
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });
    }

    // Activity result launcher for image picker
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    // Load selected image into ImageView using Glide
                    image = result.getData().getData();
                    Glide.with(AddTask.this).load(image).into(imageView);
                }
            } else {
                Toast.makeText(AddTask.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    });

    protected void AddData(String title, String description, String deadline) {
        Map<String, Object> data = new HashMap<>();
        data.put("Title", title);
        data.put("Description", description);
        data.put("Deadline", deadline); // Corrected the spelling of "Deadline"
        String rand = UUID.randomUUID().toString();

        // Add data to Firestore
        db.collection("Tasks")
                .document(rand)
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddTask.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(AddTask.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Handle errors
                            Toast.makeText(AddTask.this, "Task not added successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void uploadImage(Uri file) {
        if (file != null) {
            // Upload selected image to Firebase Storage
            StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
            ref.putFile(file).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(AddTask.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(AddTask.this, "Failed!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(AddTask.this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }
}
