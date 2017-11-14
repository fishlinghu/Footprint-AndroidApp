package fishlinghu.footprint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class TripCompleteActivity extends AppCompatActivity {

    private FirebaseUser google_user;
    private String account_email;
    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();
    private Trip current_trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_complete);

        // get current user and email
        google_user = FirebaseAuth.getInstance().getCurrentUser();
        account_email = google_user.getEmail();
        current_trip = (Trip) getIntent().getSerializableExtra("current_trip");
        current_trip.setAuthorEmail(account_email);

        Button button_publish = findViewById(R.id.button_publish);
        button_publish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // get trip name from user
                EditText editText_description = findViewById(R.id.editText_trip_name);
                String trip_name = editText_description.getText().toString();

                current_trip.setTripName(trip_name);

                db_reference.child("trips").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            String trip_key = db_reference.child("trips").push().getKey();
                            db_reference.child("trips").child(trip_key).setValue(current_trip);
                            DatabaseReference user_ref =
                                    db_reference.child("users").child(account_email.replace(".", ","));
                            user_ref.child("trips").child(trip_key).setValue("");
                            user_ref.child("unfinishedTrip").setValue(null);
                            user_ref.child("unfinishedTripFlag").setValue(false);
                        }
                        startActivity(new Intent(TripCompleteActivity.this, MainActivity.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }
}
