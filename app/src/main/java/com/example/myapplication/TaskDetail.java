package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TaskDetail extends AppCompatActivity {
    private TextView Title;
    private TextView Description;
    private TextView Deadline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        Intent intent=getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String description = intent.getStringExtra("description");
            String deadline = intent.getStringExtra("deadline");
            Title=findViewById(R.id.TitleTask);
            Description=findViewById(R.id.DescriptionTask);
            Deadline=findViewById(R.id.DeadlineTask);
            Title.setText(title);
            Description.setText(description);
            Deadline.setText(deadline);




            // Now you can use the received data as needed
            // For example, set text to a TextView
        }
    }
}