package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.myapplication.Tasks;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;


// Import statements

public class MainActivity extends AppCompatActivity   {

    private Button button;
    private TextView text;
    private FirebaseAuth mAuth;
    private static FirebaseFirestore db;
    private TextInputEditText input;
    private Button button1;
    private Button button2;

    private Context context;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeFirebase();

        // Check if the user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserName(currentUser.getEmail());
            fetchTasks();



        } else {
            goToLoginActivity();
        }


        setupButtonClick();
        db.collection("users").whereEqualTo("email", currentUser.getEmail()).whereEqualTo("role", 1).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                // Assuming you have a setupButtonClick1() method to set up the button click
                button1.setVisibility(View.VISIBLE); // Show the button
                setupButtonClick1();
            }
        }).addOnFailureListener(e -> {
            // Handle any errors here
            Log.e(TAG, "Error fetching users: " + e.getMessage());
        });
        setupButtonClick2(currentUser.getEmail().toString());
    }

    @SuppressLint("RestrictedApi")
    private void initializeViews() {
        button = findViewById(R.id.logout);
        text = findViewById(R.id.home);
        button1 = findViewById(R.id.floatingActionButton); // Add this line
        button2=findViewById(R.id.UpdateProfile12);

    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void fetchUserName(String email) {
        Query userQuery = db.collection("users").whereEqualTo("email", email);
        userQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String name = document.getString("username");
                    text.setText(name);
                }
            } else {
                // Handle failures related to fetching user data
            }
        });
    }


    protected void fetchTasks() {
        Query tasksQuery = db.collection("Tasks");
        tasksQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                LinkedList<Tasks> tasks = new LinkedList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String title = document.getString("Title");
                    String description = document.getString("Description");
                    String deadline = document.getString("Deadline");
                    String Image = document.getString("Image");



                    // Make sure deadline is not null before adding the task
                    if (deadline != null) {
                        tasks.add(new Tasks(title, description, deadline,Image));
                    } else {
                        // Handle the case where the deadline is null
                        Log.e("fetchTasks", "Deadline is null for document: " + document.getId());
                    }
                }
                setupRecyclerView(tasks);
            } else {
                // Handle failures related to fetching tasks
                Log.e("fetchTasks", "Error fetching tasks: ", task.getException());
                // Display an error message to the user, retry the operation, or take other appropriate actions
            }
        });
    }






    private void setupRecyclerView(LinkedList<Tasks> tasks) {
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerView.setAdapter(new MyAdapter(tasks,MainActivity.this));
        recyclerView.scrollTo(1,20);
    }

    private void setupButtonClick() {
        button.setOnClickListener(v -> {
            mAuth.signOut();
            goToLoginActivity();
        });
    }
    private void setupButtonClick1() {


        button1.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), AddTask.class);
            startActivity(intent);
            finish();

        });
    }
    private void setupButtonClick2(String Email) {
        button2.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), UpdateProfile.class);

            intent.putExtra("username",text.getText().toString());
            intent.putExtra("email",Email);
            startActivity(intent);
            finish();

        });
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }



}
