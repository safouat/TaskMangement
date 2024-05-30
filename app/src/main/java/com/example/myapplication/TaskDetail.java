package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.graphics.vector.SolidFill;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.auth.FirebaseAuthException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;



public class TaskDetail extends AppCompatActivity {
    private static final String TAG = "TaskDetail";

    private TextView Title;
    private TextView Description;
    private TextView Deadline;
    private ImageView image;
    BottomNavigationView bottomNavigationView;

    private Button button;
    private FirebaseAuth mAuth;
    private Button button1;
    private AnyChartView anychart;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        initializeViews();
        displayTaskDetails();
        setupButtonsVisibility();
        setChartView();
        mAuth = FirebaseAuth.getInstance();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_home) {
                    // Handle click on home item
                    startActivity(new Intent(TaskDetail.this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.menu_add) {
                    db.collection("users").whereEqualTo("email", mAuth.getCurrentUser().getEmail()).whereEqualTo("role", 1).get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Assuming you have a setupButtonClick1() method to set up the button click
                            // Show the button
                            setupButtonClick1();
                        }
                    }).addOnFailureListener(e -> {
                        MenuItem menuItem = bottomNavigationView.getMenu().findItem(R.id.menu_add);
                        menuItem.setTitle("Notifications");
                        menuItem.setIcon(R.drawable.settings); // Assuming 'settings' is the name of your drawable resource



                        // Handle any errors here
                        Log.e(TAG, "Error fetching users: " + e.getMessage());
                    });
                    return true;

                } else if (itemId == R.id.menu_calendar) {
                    // Handle click on profile item
                    setupButtonClick3();
                    return true;
                }else if (itemId == R.id.menu_profile) {
                    // Handle click on profile item
                    setupButtonClick2(mAuth.getCurrentUser().getEmail().toString());
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void initializeViews() {
        Title = findViewById(R.id.TitleTask);
        Description = findViewById(R.id.DescriptionTask);
        Deadline = findViewById(R.id.DeadlineTask);
        image = findViewById(R.id.Image1234);
        button = findViewById(R.id.Delete);
        button1 = findViewById(R.id.Update);
        anychart=findViewById(R.id.ChartView);
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
    private void setChartView() {
        Pie pie = AnyChart.pie();


        // Define a custom palette with dark colors
        pie.palette(new String[]{
                "#808080", // Dark color for the first slice
                "#343434"  // Dark color for the second slice
                // Add more dark colors if you have more slices
        });

        List<DataEntry> list = new ArrayList<>();
        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String description = intent.getStringExtra("description");
        String deadline = intent.getStringExtra("deadline");
        String imageUrl = intent.getStringExtra("imageUrl");

        db.collection("Tasks").whereEqualTo("Deadline", deadline)
                .whereEqualTo("Description", description)
                .whereEqualTo("Image", imageUrl)
                .whereEqualTo("Title", title)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            List<String> doneUsers = (List<String>) document.get("DoneUsers");
                            if (doneUsers != null && !doneUsers.isEmpty()) { // Check if the list is not null or empty
                                HashSet<String> hash1 = new HashSet<>(doneUsers);
                                int doneCount = hash1.size();
                                int notDoneCount = 70 - doneCount;

                                list.add(new ValueDataEntry("Done Users", doneCount));
                                list.add(new ValueDataEntry("Not Done Users", notDoneCount));

                                pie.data(list);

                                AnyChartView anychart = findViewById(R.id.ChartView);
                                anychart.setChart(pie);

                                Log.d("MainActivity", "Number of done users: " + doneCount);
                                Toast.makeText(this, "Number of done users: " + doneCount, Toast.LENGTH_SHORT).show();
                            } else {
                                Log.d("MainActivity", "No done users found");
                                Toast.makeText(this, "No done users found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Log.e("MainActivity", "Error fetching task", task.getException());
                        Toast.makeText(this, "Error fetching task", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupButtonClick1() {



        Intent intent = new Intent(getApplicationContext(), AddTask.class);
        startActivity(intent);
        finish();

    }
    private void setupButtonClick3() {



        Intent intent = new Intent(getApplicationContext(), SaveTasksCalendarActivity.class);
        startActivity(intent);
        finish();

    }
    private void setupButtonClick2(String Email) {

        Intent intent = new Intent(getApplicationContext(), UpdateProfile.class);

        intent.putExtra("email",Email);
        startActivity(intent);
        finish();

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