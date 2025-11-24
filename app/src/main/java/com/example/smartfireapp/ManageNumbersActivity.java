package com.example.smartfireapp;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

public class ManageNumbersActivity extends AppCompatActivity {

    ListView listView;
    EditText inputNumber;
    ArrayList<String> contactList;       // list of numbers (always in +63 format)
    ArrayList<String> contactKeys;       // matching Firebase keys for each number
    ArrayAdapter<String> adapter;
    DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_numbers);

        listView = findViewById(R.id.listNumbers);
        inputNumber = findViewById(R.id.inputNumber);
        findViewById(R.id.btnAddNumber).setOnClickListener(v -> addContact());

        contactList = new ArrayList<>();
        contactKeys = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactList);
        listView.setAdapter(adapter);

        db = FirebaseDatabase.getInstance().getReference("contacts");

        loadContacts();

        // Tap → edit
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String oldNumber = contactList.get(position);
            String key = contactKeys.get(position);
            showEditDialog(oldNumber, key);
        });

        // Long press → delete
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            String number = contactList.get(position);
            String key = contactKeys.get(position);
            confirmAndDelete(number, key);
            return true;
        });
    }

    // Load contacts and keep contactList & contactKeys in sync
    private void loadContacts() {
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                contactList.clear();
                contactKeys.clear();

                for (DataSnapshot data : snapshot.getChildren()) {
                    String number = data.getValue(String.class);
                    if (number == null) continue;
                    contactKeys.add(data.getKey());
                    contactList.add(number);
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) { }
        });
    }

    // Add new contact
    private void addContact() {
        String rawInput = inputNumber.getText().toString().trim();
        if (rawInput.isEmpty()) {
            inputNumber.setError("Enter phone number");
            return;
        }

        String cleaned = autoClean(rawInput);
        String converted = convertToInternational(cleaned); // returns converted or same invalid

        // Validate & check duplicates (use in-memory contactList)
        String validateErr = validateNumber(converted);
        if (validateErr != null) {
            inputNumber.setError(validateErr);
            return;
        }

        // Duplicate check
        if (contactList.contains(converted)) {
            Toast.makeText(this, "Number already saved.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save to Firebase
        String id = db.push().getKey();
        if (id == null) {
            Toast.makeText(this, "Failed to generate id.", Toast.LENGTH_SHORT).show();
            return;
        }
        db.child(id).setValue(converted);

        inputNumber.setText("");
        Toast.makeText(this, "Contact added: " + converted, Toast.LENGTH_SHORT).show();
    }

    // Confirm and delete using key
    private void confirmAndDelete(String number, String key) {
        new AlertDialog.Builder(this)
                .setTitle("Delete contact")
                .setMessage("Delete " + number + " ?")
                .setPositiveButton("Delete", (d, w) -> {
                    if (key != null) {
                        db.child(key).removeValue();
                        Toast.makeText(this, "Deleted: " + number, Toast.LENGTH_SHORT).show();
                    } else {
                        // Fallback: scan and remove by value (rare)
                        db.get().addOnSuccessListener(snapshot -> {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                if (number.equals(data.getValue(String.class))) {
                                    data.getRef().removeValue();
                                    Toast.makeText(this, "Deleted: " + number, Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Edit dialog: oldNumber & key present
    private void showEditDialog(String oldNumber, String key) {
        EditText editInput = new EditText(this);
        editInput.setText(oldNumber);
        editInput.setInputType(InputType.TYPE_CLASS_PHONE);

        new AlertDialog.Builder(this)
                .setTitle("Edit Phone Number")
                .setView(editInput)
                .setPositiveButton("Save", (dialog, which) -> {
                    String raw = editInput.getText().toString().trim();
                    String cleaned = autoClean(raw);
                    String newNumber = convertToInternational(cleaned);

                    String validateErr = validateNumber(newNumber);
                    if (validateErr != null) {
                        Toast.makeText(this, validateErr, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // If unchanged
                    if (newNumber.equals(oldNumber)) {
                        Toast.makeText(this, "No changes made.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // If newNumber exists already (duplicate)
                    if (contactList.contains(newNumber)) {
                        Toast.makeText(this, "That number already exists.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Update Firebase by key (preferred)
                    if (key != null) {
                        db.child(key).setValue(newNumber);
                        Toast.makeText(this, "Updated: " + newNumber, Toast.LENGTH_SHORT).show();
                    } else {
                        // fallback: find by value and update
                        db.get().addOnSuccessListener(snapshot -> {
                            for (DataSnapshot data : snapshot.getChildren()) {
                                if (oldNumber.equals(data.getValue(String.class))) {
                                    data.getRef().setValue(newNumber);
                                    Toast.makeText(this, "Updated: " + newNumber, Toast.LENGTH_SHORT).show();
                                    break;
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // Auto-clean user input: remove spaces, dashes, parentheses, keep leading + if present
    private String autoClean(String input) {
        if (input == null) return "";
        input = input.trim();

        // If starts with '+', keep '+', else don't
        boolean hasPlus = input.startsWith("+");
        String digitsOnly = input.replaceAll("[^0-9]", ""); // remove non-digits

        return hasPlus ? "+" + digitsOnly : digitsOnly;
    }

    /**
     * Convert various user inputs to international +63 format if possible:
     * Accepts:
     *  - 09123456789 -> +639123456789
     *  - 9123456789  -> +639123456789
     *  - 639123456789 -> +639123456789
     *  - +639123456789 -> +639123456789
     *
     * If conversion not possible, returns the best-effort value (which will later fail validation).
     */
    private String convertToInternational(String number) {
        if (number == null) return "";

        // If already in +63 form
        if (number.startsWith("+63") && number.length() == 13) {
            return number;
        }

        // If starts with 09 and length 11 -> replace leading 0 with +63
        if (number.startsWith("09") && number.length() == 11) {
            return "+63" + number.substring(1);
        }

        // If starts with '9' and 10 digits -> eg 9123456789
        if (number.length() == 10 && number.startsWith("9")) {
            return "+63" + number;
        }

        // If starts with '63' and length 12 -> add '+'
        if (number.startsWith("63") && number.length() == 12) {
            return "+" + number;
        }

        // If starts with '+' but not +63 -> just return as-is (will fail validation)
        if (number.startsWith("+")) {
            return number;
        }

        // Fallback: return original (likely invalid)
        return number;
    }

    // Validate final number; return null when OK, else an error message string
    private String validateNumber(String number) {
        if (number == null || number.isEmpty()) return "Enter phone number";

        // Must start with +63 after conversion
        if (!number.startsWith("+63")) return "Number must be Philippine mobile (+63)";

        // Must be exactly 13 chars: +63 + 10 digits
        if (number.length() != 13) return "Number must be 13 characters (e.g. +639123456789)";

        // All chars after + must be digits
        String afterPlus = number.substring(1);
        if (!afterPlus.matches("\\d+")) return "Invalid characters in number";

        // Mobile prefix check: after +63 the first digit should be '9'
        if (number.charAt(3) != '9') return "Not a valid Philippine mobile number";

        // Optionally check next two digits for valid 90-99 ranges (we allow 90-99)
        char secondDigit = number.charAt(4); // e.g. for +6391..., this is '1'
        // No strict check beyond first '9' required; still pass.

        return null; // OK
    }
}
