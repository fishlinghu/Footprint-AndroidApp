package fishlinghu.footprint;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

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

import static android.provider.UserDictionary.Words.APP_ID;
import static fishlinghu.footprint.R.id.textView;

public class CheckInActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private Uri fileUri = Uri.parse("content://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/aabbcc.jpg");

    private GoogleApiClient googleApiClient;
    private Location last_location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

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
                startActivity(new Intent(CheckInActivity.this, RecordTripActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            last_location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            // last_location could be null here if the location service is not turned on
            Log.d("DEBUG--- Latitude", Double.toString(last_location.getLatitude()));
            Log.d("DEBUG--- Longitude", Double.toString(last_location.getLongitude()));
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
