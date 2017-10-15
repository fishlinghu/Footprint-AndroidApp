package fishlinghu.footprint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class RecordTripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_trip);

        Button button_check_in = findViewById(R.id.button_check_in);
        button_check_in.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(RecordTripActivity.this, MainActivity.class));
                finish();
            }
        });

        Button button_finish_trip = findViewById(R.id.button_finish_trip);
        button_finish_trip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(RecordTripActivity.this, TripCompleteActivity.class));
                finish();
            }
        });
    }
}
