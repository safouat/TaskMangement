package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.LinkedList;

public class MyAdapter extends RecyclerView.Adapter<ViewHolder> {

    private LinkedList<Tasks> taches;
    private Context context;
    // Provide a suitable constructor (depends on the kind of dataset)
    public MyAdapter(LinkedList<Tasks> taches, Context context) {
        this.taches = new LinkedList<Tasks>() ;
        this.taches.addAll(taches);
        this.context=context;
    }




    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
// create a new view
        View itemLayoutView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_task_item_layout,
                        parent, false);
        ViewHolder vh = new ViewHolder(itemLayoutView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Bind data to the views of the ViewHolder here
        // For example:
        Tasks task = taches.get(position);
        holder.title.setText(task.getTitle());
        holder.description.setText(task.getDescription());
        holder.deadline.setText( task.getDeadline());
        holder.imageView.setTag(task.getImage());
        Glide.with(context).load(task.getImage()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {

        return taches.size();
    }
}