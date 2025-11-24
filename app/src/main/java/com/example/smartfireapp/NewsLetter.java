package com.example.smartfireapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewsLetter extends AppCompatActivity {

    private EditText et_email;
    private Button btn_signup;
    private DatabaseReference db; // Firebase Database Reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // the layout file for the screen
        setContentView(R.layout.news_letter);


        // This will create a root node called "newsletter_subscribers"
        db = FirebaseDatabase.getInstance().getReference("newsletter_subscribers");


        et_email = findViewById(R.id.et_email);
        btn_signup = findViewById(R.id.btn_signup);


        btn_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEmailToFirebase();
            }
        });
    }

    private void saveEmailToFirebase() {
        String email = et_email.getText().toString().trim();

        // validation
        if (email.isEmpty()) {
            et_email.setError("Email is required!");
            et_email.requestFocus();
            return;
        }

        // Create the subscriber object
        NewsletterSubscriber subscriber = new NewsletterSubscriber(email);


        // Pushing generates a unique key for each subscriber.
        db.push().setValue(subscriber)
                .addOnSuccessListener(aVoid -> {
                    // Success
                    Toast.makeText(this, "Subscribed successfully!", Toast.LENGTH_SHORT).show();
                    et_email.setText(""); // Clear the input field
                })
                .addOnFailureListener(e -> {
                    // Failure
                    Toast.makeText(this, "Failed to subscribe: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}