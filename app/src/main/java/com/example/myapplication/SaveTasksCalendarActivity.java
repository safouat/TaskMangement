package com.example.myapplication;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SaveTasksCalendarActivity extends AppCompatActivity {

    private Set<String> highlightedDates;
    private FirebaseFirestore db;
    private String username;
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_main);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();

        if (currentUser != null) {
            Query userQuery = db.collection("users").whereEqualTo("email", currentUser.getEmail());
            userQuery.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        username = document.getString("username");
                        if (username != null) {
                            fetchHighlightedDates();
                            bottomNavigationView = findViewById(R.id.bottom_navigation);

                            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                                @SuppressLint("RestrictedApi")
                                @Override
                                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                                    int itemId = item.getItemId();
                                    if (itemId == R.id.menu_home) {
                                        // Handle click on home item
                                        startActivity(new Intent(SaveTasksCalendarActivity.this, MainActivity.class));
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
                                        setupButtonClick2(currentUser.getEmail().toString(),username);
                                        return true;
                                    } else {
                                        return false;
                                    }
                                }
                            });

                        } else {
                            Log.e("SaveTasksCalendar", "Username is null");
                        }
                    }
                } else {
                    Log.e("SaveTasksCalendar", "Failed to retrieve user data");
                }
            });
        } else {
            Log.e("SaveTasksCalendar", "No current user");
        }
    }
    private void setupButtonClick2(String Email,String name) {

        Intent intent = new Intent(getApplicationContext(), UpdateProfile.class);
        intent.putExtra("username",name);
        intent.putExtra("email",Email);


        startActivity(intent);
        finish();

    }
    private void setupButtonClick3() {



        Intent intent = new Intent(getApplicationContext(), SaveTasksCalendarActivity.class);
        startActivity(intent);
        finish();

    }
    private void setupButtonClick1() {



        Intent intent = new Intent(getApplicationContext(), AddTask.class);
        startActivity(intent);
        finish();

    }
    private void fetchHighlightedDates() {
        db.collection("Tasks")
                .whereArrayContains("SaveUsers", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HashMap <String, String> dateDescriptionMap = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String deadline = document.getString("Deadline");
                            String description = document.getString("Title");
                            if (deadline != null && description != null) {
                                dateDescriptionMap.put(deadline, description);
                            }
                        }
                        displayHighlightedDates(dateDescriptionMap);
                    } else {
                        Log.e("SaveTasksCalendar", "Failed to fetch highlighted dates");
                    }
                });
    }

    private void displayHighlightedDates(HashMap<String, String> dateDescriptionMap) {
        CompactCalendarView compactCalendarView = findViewById(R.id.ok);
        compactCalendarView.setUseThreeLetterAbbreviation(true);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d", Locale.getDefault());

        for (Map.Entry<String, String> entry : dateDescriptionMap.entrySet()) {
            String date = entry.getKey();
            String description = entry.getValue();
            try {
                Event event = new Event(Color.BLACK, dateFormat.parse(date).getTime(), description);
                compactCalendarView.addEvent(event);
            } catch (ParseException e) {
                Log.e("SaveTasksCalendar", "Error parsing date: " + date, e);
            }
        }

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(java.util.Date dateClicked) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.M.d", Locale.getDefault());
                String dateString = dateFormat.format(dateClicked);
                showDateDialog(dateDescriptionMap.get(dateString));
            }

            @Override
            public void onMonthScroll(java.util.Date firstDayOfNewMonth) {
                // Optional: handle month scroll events if needed
            }
        });
    }


    private void showDateDialog(String title) {
        new AlertDialog.Builder(this)
                .setTitle("Saved Task")
                .setMessage("Task Title: " + title)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Action on OK click
                    }
                })
                .show();
    }
}
