package fishlinghu.footprint;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static android.provider.UserDictionary.Words.APP_ID;
import static fishlinghu.footprint.R.id.textView;

public class CheckInActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private Uri fileUri = Uri.parse("content://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/aabbcc.jpg");

    private GoogleApiClient googleApiClient;
    private Location last_location;
    private Trip current_trip;

    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();

    private FirebaseUser google_user;
    private String account_email;
    private User user_data;

    protected String location_name = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        // get current user and email
        google_user = FirebaseAuth.getInstance().getCurrentUser();
        account_email = google_user.getEmail();

        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aabbcc.jpg";
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        Matrix matrix = new Matrix();

        try {
            int rotated_degree;
            ExifInterface exif = new ExifInterface(filepath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotated_degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotated_degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotated_degree = 270;
                    break;
                default:
                    rotated_degree = 0;
                    break;
            }
            matrix.postRotate(rotated_degree);
        } catch (java.io.IOException e) {

        }

        ImageView image = findViewById(R.id.imageView_check_in_photo);
        Bitmap rotated_bitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        image.setImageBitmap(rotated_bitmap);

        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();

        Button button_finish = findViewById(R.id.button_finish_check_in);
        button_finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // get user input
                EditText editText_description = findViewById(R.id.editText_description);
                String location_intro = editText_description.getText().toString();
                // get local time
                Calendar calendar = (Calendar) getIntent().getSerializableExtra("current_calendar");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
                String local_time = sdf.format(calendar.getTime());
                Toast.makeText(CheckInActivity.this, local_time, Toast.LENGTH_LONG).show();
                // generate photo url
                String photo_url = local_time + ".jpg";
                // get trip
                current_trip = (Trip) getIntent().getSerializableExtra("current_trip");
                current_trip.addCheckIn(last_location.getLatitude(), last_location.getLongitude(), photo_url, calendar.getTime(), location_intro, location_name);

                // update the unfinished trip
                db_reference = FirebaseDatabase.getInstance().getReference();
                Query query = db_reference.child("users").child( account_email.replace(".",",") );
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            db_reference.child("users").child(account_email.replace(".",",")).child("unfinishedTrip").setValue(current_trip);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                startActivity(new Intent(CheckInActivity.this, RecordTripActivity.class));
                finish();
            }
        });


    }

    private void showLocationName() {
        // get location name
        String request_url = getLocationNameUrl(last_location.getLatitude(), last_location.getLongitude());
        Log.d("DEBUG---URL:", request_url);
        FetchUrlForLocationName fetch_url = new FetchUrlForLocationName();
        fetch_url.execute(request_url);
    }

    private String getLocationNameUrl(Double latitude, Double longitude){

        // Building the parameters to the web service
        String parameters = latitude + "," + longitude;

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/place/search/json?location=" + parameters +
                "&rankby=distance&types=establishment&sensor=false&key=AIzaSyDQxVaohRnN3im7rkkxIDs9_kuyc062B7o&language=en&region=US";

        return url;
    }

    // Fetches data from url passed
    private class FetchUrlForLocationName extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuffer sb = new StringBuffer();

                String line = "";
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data.toString());
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }

        protected void setLocationName (String temp) {
            location_name = temp;
            TextView text_view_at = findViewById(R.id.textView_at);
            text_view_at.setText("- at " + location_name);
            Log.d("Location Name In", location_name);
        }

        private class ParserTask extends AsyncTask<String, Integer, String> {

            // Parsing the data in non-ui thread
            @Override
            protected String doInBackground(String... jsonData) {
                // parse the jsonData which contains location name information
                JSONObject jObject;
                String location_name = "";

                try {
                    jObject = new JSONObject(jsonData[0]);
                    location_name = jObject.getJSONArray("results").getJSONObject(0).getString("name");

                } catch (Exception e) {
                    Log.d("ParserTask",e.toString());
                    e.printStackTrace();
                }
                return location_name;
            }

            // Executes in UI thread, after the parsing process
            @Override
            protected void onPostExecute(String location_name) {
                // set location name
                setLocationName(location_name);
            }
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            last_location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            // last_location could be null here if the location service is not turned on
            Log.d("DEBUG--- Latitude", Double.toString(last_location.getLatitude()));
            Log.d("DEBUG--- Longitude", Double.toString(last_location.getLongitude()));
            if (Objects.equals("", location_name)) {
                showLocationName();
            }
            Log.d("Location Name Out", location_name);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("DEBUG---", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("DEBUG---", "Location services suspended. Please reconnect.");
    }

    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

}
