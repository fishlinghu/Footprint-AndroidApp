package fishlinghu.footprint;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class CheckInActivity extends AppCompatActivity {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private Uri fileUri = Uri.parse("content://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/aabbcc.jpg");

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

        displayLocationSettingsRequest(this);

        Button button_finish = findViewById(R.id.button_finish_check_in);
        button_finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(CheckInActivity.this, RecordTripActivity.class));
                finish();
            }
        });
    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("DEBUG---", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("DEBUG---", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(CheckInActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("DEBUG---", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("DEBUG---", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }
}
