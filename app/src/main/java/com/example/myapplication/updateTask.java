package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.net.InternetDomainName;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.UUID;


public class updateTask extends AppCompatActivity {
    private TextInputEditText title;
    private TextInputEditText description;
    private TextInputEditText deadline;
    private ImageView imageView;
    private MaterialButton imageSelect;
    private Button button;
    private StorageReference storageReference;
    private Uri image;
    private static final String TAG = "updateTask";
    private static final String TASKS_COLLECTION = "Tasks";
    private static final String TITLE_FIELD = "Title";
    private static final String DESCRIPTION_FIELD = "Description";
    private static final String DEADLINE_FIELD = "Deadline";
    private static final String IMAGE_FIELD = "Image";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_task);
        initializeViews();
        setOnClickListeners();
    }


    private void initializeViews() {
        Intent intent = getIntent();
        title = findViewById(R.id.titleUpdate);
        title.setText(intent.getStringExtra("title"));
        description = findViewById(R.id.descriptionUpdate);
        description.setText(intent.getStringExtra("description"));
        deadline = findViewById(R.id.deadlineUpdate);
        deadline.setText(intent.getStringExtra("deadline"));
        button = findViewById(R.id.Update);
        imageView=findViewById(R.id.ImageView1);
        imageSelect=findViewById(R.id.selectImage1);
        storageReference = FirebaseStorage.getInstance().getReference();
        String imageUrl = intent.getStringExtra("imageUrl");

        Glide.with(updateTask.this).load(imageUrl).into(imageView);


    }


    private void setOnClickListeners() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage(image);
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
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                if (result.getData() != null) {
                    image = result.getData().getData();
                    Glide.with(updateTask.this).load(image).into(imageView);
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
                    .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(downloadUri -> UpdateTask(downloadUri)))
                    .addOnFailureListener(e -> showToast("Failed!" + e.getMessage()));
        } else {
            showToast("No image selected");
        }
    }

    private void UpdateTask(Uri downloadUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        String clickedTitle = intent.getStringExtra("title");
        String clickedDescription = intent.getStringExtra("description");
        String clickedDeadline = intent.getStringExtra("deadline");

        db.collection(TASKS_COLLECTION)
                .whereEqualTo(TITLE_FIELD, clickedTitle)
                .whereEqualTo(DESCRIPTION_FIELD, clickedDescription)
                .whereEqualTo(DEADLINE_FIELD, clickedDeadline)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String taskId = document.getId();
                            HashMap<String, Object> map = new HashMap<>();
                            map.put(TITLE_FIELD, title.getText().toString());
                            map.put(DEADLINE_FIELD, deadline.getText().toString());
                            map.put(DESCRIPTION_FIELD, description.getText().toString());
                            if (downloadUri != null)
                                map.put(IMAGE_FIELD, downloadUri.toString());
                            db.collection(TASKS_COLLECTION).document(taskId).update(map)
                                    .addOnSuccessListener(aVoid -> {
                                        showToast("Task updated successfully");
                                        goToMainActivity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d(TAG, "Error updating task: ", e);
                                        showToast("Failed to update task");
                                    });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        showToast("Failed to retrieve task");
                    }
                });
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
