package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class task_item_layout extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    String title,description;
    Context context;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_item_layout);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        title= String.valueOf(findViewById(R.id.title));
        description= String.valueOf(findViewById(R.id.description));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem done = menu.findItem(R.id.menu_Done);
        MenuItem save = menu.findItem(R.id.menu_Save);
        // You can now work with the menu items here
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.menu_Done) {
            Intent intent = getIntent();
            handleDoneAction(intent);
            return true;
        } else if (itemId == R.id.menu_Save) {
            handleShareAction();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
    private void handleShareAction() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title.toString()+ ":" +description.toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    private void handleDoneAction(Intent intent) {
        String doneTitle = intent.getStringExtra("title");

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();

            Query userQuery = db.collection("users").whereEqualTo("email", userEmail);

            // Query for the specific document with the given title
            db.collection("Tasks")
                    .whereEqualTo("Title", doneTitle)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Update the DoneUsers array locally
                                ArrayList<String> doneUsers = (ArrayList<String>) document.get("DoneUsers");
                                if (doneUsers == null) {
                                    doneUsers = new ArrayList<>();
                                }

                                ArrayList<String> finalDoneUsers = doneUsers;
                                userQuery.get().addOnCompleteListener(userTask -> {
                                    if (userTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot userDoc : userTask.getResult()) {
                                            String name = userDoc.getString("username");
                                            finalDoneUsers.add(name);
                                        }
                                        // Update Firestore with the modified array
                                        document.getReference().update("DoneUsers", finalDoneUsers)
                                                .addOnSuccessListener(aVoid -> {
                                                    // Successfully updated the DoneUsers array
                                                    Log.d("ok", "Updated DoneUsers: " + finalDoneUsers);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("error", "Error updating document", e);
                                                });
                                    } else {
                                        // Handle failures related to fetching user data
                                        Log.e("error", "Error getting user documents: ", userTask.getException());
                                    }
                                });
                            }
                        } else {
                            Log.e("error", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }
}
