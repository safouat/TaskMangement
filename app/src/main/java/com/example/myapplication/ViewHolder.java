package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    public ImageView imageView;
    TextView title, description, deadline;
    Context context;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        description = itemView.findViewById(R.id.description);
        deadline = itemView.findViewById(R.id.deadline);
        imageView = itemView.findViewById(R.id.image124);
        context = itemView.getContext();
        // Assuming imageUrl is the URL of the image


        // Setting OnClickListener for the item view
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Extract the text from TextView objects
                String clickedTitle = title.getText().toString();
                String clickedDescription = description.getText().toString();
                String clickedDeadline = deadline.getText().toString();

                // Extract the image URL from the ImageView
                String imageUrl = (String) imageView.getTag();

                // Create an Intent to start the TaskDetail activity
                Intent intent = new Intent(context, TaskDetail.class);
                // Pass the extracted data as extras to the Intent
                intent.putExtra("title", clickedTitle);
                intent.putExtra("description", clickedDescription);
                intent.putExtra("deadline", clickedDeadline);
                intent.putExtra("imageUrl", imageUrl); // Pass the image URL

                // Start the TaskDetail activity
                context.startActivity(intent);
            }
        });
    }
}
