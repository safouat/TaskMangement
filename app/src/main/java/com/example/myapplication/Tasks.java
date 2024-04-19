package com.example.myapplication;

import java.util.Date;

public class Tasks {
    private String Title;
    private String Description;
    private String deadline;
    private String Image;
    public Tasks(String title,String Description,String deadline,String Image){
        this.Title=title;
        this.Description=Description;
        this.deadline=deadline;
        this.Image=Image;
    }


    // Getter and setter for Title
    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    // Getter and setter for Description
    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    // Getter and setter for deadline
    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getImage() {
        return Image;
    }
    public void setImage(String Image){
        this.Image=Image;
    }

    // Getter and setter for category

}
