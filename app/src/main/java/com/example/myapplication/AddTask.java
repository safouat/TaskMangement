package com.example.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddTask extends AppCompatActivity {
    private TextInputEditText title;
    private TextInputEditText description;
    private TextInputEditText deadline;
    private Button button;
    private Button button2;
    private TextView textView12;
    private MaterialButton imageSelect;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private ImageView imageView;
    private StorageReference storageReference;
    private Uri image;
    private AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        title = findViewById(R.id.title1);
        description = findViewById(R.id.description1);
        imageSelect = findViewById(R.id.selectImage);
        imageView = findViewById(R.id.ImageView);
        button2 = findViewById(R.id.Deadline12);
        textView12 = findViewById(R.id.text);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(AddTask.this,
                    android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(AddTask.this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        10);
            }
        }

        button = findViewById(R.id.Add);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (image != null) {
                    uploadImage(image);
                } else {
                    Toast.makeText(AddTask.this, "No image selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        imageSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                activityResultLauncher.launch(intent);
            }
        });

        button2.setOnClickListener(v -> openDatePicker());
    }

    private void openDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Log.d("DatePicker", "onDateSet called");
                textView12.setText(String.valueOf(year) + "." + String.valueOf(month + 1) + "." + String.valueOf(dayOfMonth));
                Log.d("DatePicker", "Date set to: " + textView12.getText().toString());
            }
        }, 2023, 1, 20);

        datePickerDialog.setOnShowListener(dialog -> Log.d("DatePicker", "DatePickerDialog shown"));
        datePickerDialog.setOnDismissListener(dialog -> Log.d("DatePicker", "DatePickerDialog dismissed"));

        datePickerDialog.show();
    }

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                image = result.getData().getData();
                Glide.with(AddTask.this).load(image).into(imageView);
            } else {
                Toast.makeText(AddTask.this, "Please select an image", Toast.LENGTH_SHORT).show();
            }
        }
    });

    private void uploadImage(Uri file) {
        StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
        ref.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri downloadUri) {
                                saveImageToFirestore(downloadUri);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddTask.this, "Failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void showProgressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.progress_dialog);
        builder.setCancelable(false); // Prevent closing the dialog by pressing back button
        progressDialog = builder.create();
        progressDialog.show();
    }

    private void hideProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
    private void saveImageToFirestore(Uri downloadUri) {
        showProgressDialog();
        String titleValue = title.getText().toString();
        String descriptionValue = description.getText().toString();
        String deadlineValue = textView12.getText().toString();
        ArrayList<String> DoneUsers = new ArrayList<>();

        Map<String, Object> data = new HashMap<>();
        data.put("Title", titleValue);
        data.put("Description", descriptionValue);
        data.put("Deadline", deadlineValue);
        data.put("DoneUsers", DoneUsers);
        data.put("Image", downloadUri.toString());

        db.collection("Tasks")
                .document(UUID.randomUUID().toString())
                .set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(AddTask.this, "Task added successfully", Toast.LENGTH_SHORT).show();
                            makeNotification();
                            Intent intent = new Intent(AddTask.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(AddTask.this, "Task not added successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void makeNotification() {
        String channelID = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setSmallIcon(R.drawable.settings)
                .setContentTitle("New Task")
                .setContentText("Check it Now ")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data", "Some value to be passed here");

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelID,
                    "Channel Name", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Some description");
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        int notificationId = (int) System.currentTimeMillis();
        notificationManager.notify(notificationId, builder.build());
        sendNotificationToAllUsers("New Task", "Some text for notification");
    }

    private void sendNotificationToAllUsers(String title, String body) {
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();

        Map<String, String> dataPayload = new HashMap<>();
        dataPayload.put("title", title);
        dataPayload.put("body", body);

        RemoteMessage.Builder messageBuilder = new RemoteMessage.Builder("1094745701081" + "@fcm.googleapis.com")
                .setData(dataPayload);

        firebaseMessaging.send(messageBuilder.build());
    }
}
