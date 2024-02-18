package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    TextView title, description, deadline;
    Context context;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        description = itemView.findViewById(R.id.description);
        deadline = itemView.findViewById(R.id.deadline);
        context = itemView.getContext();

        // Setting OnClickListener for the title TextView
        title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extract the text from TextView objects
                String clickedTitle = title.getText().toString();
                String clickedDescription = description.getText().toString();
                String clickedDeadline = deadline.getText().toString();

                // Create an Intent to start the TaskDetail activity
                Intent intent = new Intent(context, TaskDetail.class);
                // Pass the extracted text data as extras to the Intent
                intent.putExtra("title", clickedTitle);
                intent.putExtra("description", clickedDescription);
                intent.putExtra("deadline", clickedDeadline);

                // Start the TaskDetail activity
                context.startActivity(intent);
            }
        });
    }
}
