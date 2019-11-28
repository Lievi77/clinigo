package com.uottawa.clinigo.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.provider.Telephony;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ThrowOnExtraProperties;
import com.google.firebase.database.ValueEventListener;
import com.uottawa.clinigo.R;
import com.uottawa.clinigo.adapters.PatientBookingsAdapter;
import com.uottawa.clinigo.model.Booking;

import java.util.ArrayList;

public class PatientBookingActivity extends AppCompatActivity {

    String patientId;
    private FirebaseDatabase mDatabase;
    private DatabaseReference patientReference;
    private ArrayList<Booking> patientArrayOfBookings;
    private TextView patientHasNoBookings;
    private String[] patientBookingDelete;
    ListView bookingsListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_booking);
        patientId = getIntent().getStringExtra("patientId");
        initVariables();
    }

    public void initVariables(){
        mDatabase = FirebaseDatabase.getInstance();
        patientReference = mDatabase.getReference().child("users").child(patientId);
        patientHasNoBookings = findViewById(R.id.textView_patient_noBookings);
        bookingsListView = findViewById(R.id.listView_patient_bookings);

        patientReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data: dataSnapshot.getChildren()){
                    if(data.getKey().equals("bookings")){
                        ArrayList<Booking> patientsTempBookings = new ArrayList<>();
                        for(DataSnapshot patientBookings: data.getChildren() ){
                            Booking patientApp = patientBookings.getValue(Booking.class);
                            patientsTempBookings.add(patientApp);
                        }
                        patientArrayOfBookings = patientsTempBookings;
                        displayResults(patientArrayOfBookings);
                    }
                }
                if(patientArrayOfBookings == null){
                    patientHasNoBookings.setText("You currently have no Bookings up coming");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    public void displayResults(ArrayList<Booking> results){
        final PatientBookingsAdapter patientBookingsAdapter = new PatientBookingsAdapter(PatientBookingActivity.this, results);
        bookingsListView.setAdapter(patientBookingsAdapter);
        bookingsListView.setClickable(true);
        bookingsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                patientBookingDelete[0] = patientBookingsAdapter.getItem(position).getClinicName();
                patientBookingDelete[1] = patientBookingsAdapter.getItem(position).getDate();
            }
        });
    }

    public void delete_booking(View view){

        final int position = bookingsListView.getPositionForView(view);
        android.app.AlertDialog.Builder dialogBuilder = new android.app.AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.delete_booking_dialog, null);
        dialogBuilder.setView(dialogView);

        final Button deleteButton = dialogView.findViewById(R.id.button_delete_booking);
        final Button cancelButton = dialogView.findViewById(R.id.button_cancel_noDelete);

        dialogBuilder.setTitle("Delete Booking");
        final AlertDialog b = dialogBuilder.create();
        b.show();

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.dismiss();
                Toast.makeText(getApplicationContext(), "removed : "+ position, Toast.LENGTH_LONG).show();
                patientArrayOfBookings.remove(position);
                displayResults(patientArrayOfBookings);
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                b.dismiss();
            }
        });

    }
}
