package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.LinkedList;

public class TaskDetail extends AppCompatActivity {
    private static final String TAG = "TaskDetail";

    private TextView Title;
    private TextView Description;
    private TextView Deadline;
    private ImageView image;
    private Button button;
    private Button button1;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        initializeViews();
        displayTaskDetails();
        setupButtonsVisibility();
    }

    private void initializeViews() {
        Title = findViewById(R.id.TitleTask);
        Description = findViewById(R.id.DescriptionTask);
        Deadline = findViewById(R.id.DeadlineTask);
        image = findViewById(R.id.Image1234);
        button = findViewById(R.id.Delete);
        button1 = findViewById(R.id.Update);
    }

    private void displayTaskDetails() {
        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String deadline = intent.getStringExtra("deadline");
            String imageUrl = intent.getStringExtra("imageUrl");

            Title.setText(title);
            Description.setText(description);
            Deadline.setText(deadline);
            Glide.with(TaskDetail.this).load(imageUrl).into(image);
        }
    }

    private void setupButtonsVisibility() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("email", currentUser.getEmail())
                .whereEqualTo("role", 1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        button.setVisibility(View.VISIBLE);
                        button1.setVisibility(View.VISIBLE);
                        setOnClickListeners();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void setOnClickListeners() {
        button.setOnClickListener(v -> deleteTask());

        button1.setOnClickListener(v -> {
            String clickedTitle = Title.getText().toString();
            String clickedDescription = Description.getText().toString();
            String clickedDeadline = Deadline.getText().toString();
            Intent intent1 = getIntent();
            String imageUrl = intent1.getStringExtra("imageUrl");

            Intent intent = new Intent(getApplicationContext(), updateTask.class);
            intent.putExtra("title", clickedTitle);
            intent.putExtra("description", clickedDescription);
            intent.putExtra("deadline", clickedDeadline);
            intent.putExtra("imageUrl", imageUrl);
            startActivity(intent);
            finish();
        });
    }

    private void deleteTask() {
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String deadline = intent.getStringExtra("deadline");
        String imageUrl = intent.getStringExtra("imageUrl");
        db = FirebaseFirestore.getInstance();

        db.collection("Tasks")
                .whereEqualTo("Deadline", deadline)
                .whereEqualTo("Description", description)
                .whereEqualTo("Image", imageUrl)
                .whereEqualTo("Title", title)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.exists()) {
                                db.collection("Tasks").document(document.getId()).delete();
                                goToMainActivity();
                            } else {
                                showToast("document not exist");
                            }
                        }
                        showToast("Task deleted successfully");
                        finish();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        showToast("Failed to delete task");
                    }
                });
    }

    private void showToast(String message) {
        Toast.makeText(TaskDetail.this, message, Toast.LENGTH_SHORT).show();
    }
    private void goToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}