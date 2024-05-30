package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class ViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    public Button Done;
    public Button Done1;
    public Button share;

    TextView title, description, deadline;
    Context context;
    FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        mAuth = FirebaseAuth.getInstance();
        title = itemView.findViewById(R.id.title);
        description = itemView.findViewById(R.id.description);
        deadline = itemView.findViewById(R.id.deadline);
        imageView = itemView.findViewById(R.id.image124);
        Done = itemView.findViewById(R.id.Done);
        Done1 = itemView.findViewById(R.id.Done1);
        share = itemView.findViewById(R.id.Share);
        context = itemView.getContext();
        db = FirebaseFirestore.getInstance();

        itemView.setOnClickListener(v -> {
            String clickedTitle = title.getText().toString();
            String clickedDescription = description.getText().toString();
            String clickedDeadline = deadline.getText().toString();
            String imageUrl = (String) imageView.getTag();

            Intent intent = new Intent(context, TaskDetail.class);
            intent.putExtra("title", clickedTitle);
            intent.putExtra("description", clickedDescription);
            intent.putExtra("deadline", clickedDeadline);
            intent.putExtra("imageUrl", imageUrl);
            context.startActivity(intent);
        });

        Done.setOnClickListener(v -> handleTaskAction("DoneUsers", "Task is Done"));
        Done1.setOnClickListener(v -> handleTaskAction("SaveUsers", "Task is Saved"));
        share.setOnClickListener(v -> handleShareAction());
    }

    private void handleTaskAction(String userField, String text) {
        String doneTitle = title.getText().toString();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            Query userQuery = db.collection("users").whereEqualTo("email", currentUser.getEmail());

            db.collection("Tasks")
                    .whereEqualTo("Title", doneTitle)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ArrayList<String> users = (ArrayList<String>) document.get(userField);
                                if (users == null) {
                                    users = new ArrayList<>();
                                }

                                ArrayList<String> finalUsers = users;
                                userQuery.get().addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        for (QueryDocumentSnapshot userDoc : task1.getResult()) {
                                            String name = userDoc.getString("username");
                                            if (!finalUsers.contains(name)) {
                                                finalUsers.add(name);
                                            }
                                        }
                                        document.getReference().update(userField, finalUsers)
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
                                                    Log.d("ok", "Updated " + userField + ": " + finalUsers);
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("error", "Error updating document", e);
                                                });
                                    } else {
                                        Log.e("error", "Error getting user documents: ", task1.getException());
                                    }
                                });
                            }
                        } else {
                            Log.e("error", "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private void handleShareAction() {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, title.getText().toString()+ ":" +description.getText().toString());
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }
}
