package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import java.util.LinkedList;



// Import statements

public class MainActivity extends AppCompatActivity   {

    private Button button;
    private TextView text;
    private FirebaseAuth mAuth;
    private String name;
    private SearchView search;

    private static FirebaseFirestore db;
    private TextInputEditText input;
    private Button button1;
    private Button button2;

    private Context context;
    BottomNavigationView bottomNavigationView;
    private static final int MENU_HOME = R.id.menu_home;
    private static final int MENU_ADD = R.id.menu_add;
    private static final int MENU_PROFILE = R.id.menu_profile;


    @SuppressLint("RestrictedApi")
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        initializeFirebase();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        10);
            }
        }


        // Check if the user is authenticated
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            fetchUserName(currentUser.getEmail());
            fetchTasks();
        } else {
            goToLoginActivity();
        }

        setupButtonClick();
        setupButtonSearch();
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.menu_home) {
                    // Handle click on home item
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                    return true;
                } else if (itemId == R.id.menu_add) {
                    db.collection("users").whereEqualTo("email", currentUser.getEmail()).whereEqualTo("role", 1).get().addOnSuccessListener(queryDocumentSnapshots -> {
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
                    setupButtonClick2(currentUser.getEmail().toString());
                    return true;
                } else {
                    return false;
                }
            }
        });

    }


    @SuppressLint("RestrictedApi")
    private void initializeViews() {
        button = findViewById(R.id.logout);
        text = findViewById(R.id.home);
        button1 = findViewById(R.id.floatingActionButton); // Add this line
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        search=findViewById(R.id.searchView);


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
                    name = document.getString("username");
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
    private void setupButtonSearch(){
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.isEmpty()){
                    fetchTasks();
                }
                else{
                    filterText(newText);
                }


                return false;
            }
        });
    }
    private void filterText(String text){
        Query tasksQuery = db.collection("Tasks").whereEqualTo("Title",text);
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

    private void setupButtonClick() {
        button.setOnClickListener(v -> {
            mAuth.signOut();

            goToLoginActivity();
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

            intent.putExtra("username",text.getText().toString());
            intent.putExtra("email",Email);
            startActivity(intent);
            finish();

    }

    private void goToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), Login.class);
        startActivity(intent);
        finish();
    }



}
