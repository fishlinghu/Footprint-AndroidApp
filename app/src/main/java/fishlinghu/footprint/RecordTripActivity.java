/* Reference */
/* http://www.coderzheaven.com/2016/07/29/simple-example-on-using-camera-access-permission-in-android-marshmallow/ */
/* http://www.techotopia.com/index.php/Video_Recording_and_Image_Capture_on_Android_6_using_Camera_Intents */
package fishlinghu.footprint;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.Manifest;
import android.widget.Toast;

import java.io.File;

public class RecordTripActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 102;
    private static final int IMAGE_CAPTURE = 1;
    private Uri fileUri;
    private static final String ALLOW_KEY = "ALLOWED";
    private static final String CAMERA_PREF = "camera_pref";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_trip);

        Button button_check_in = findViewById(R.id.button_check_in);
        button_check_in.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(
                        RecordTripActivity.this,
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED) {
                    // permission is not yet granted
                    Toast.makeText(RecordTripActivity.this, "IF", Toast.LENGTH_LONG).show();

                    if (getFromPref(RecordTripActivity.this, ALLOW_KEY)) {
                        showSettingsAlert();
                    } else if (ContextCompat.checkSelfPermission(RecordTripActivity.this,
                            Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(RecordTripActivity.this,
                                Manifest.permission.CAMERA)) {
                            showAlert();
                        } else {
                            // No explanation needed, we can request the permission.
                            ActivityCompat.requestPermissions(RecordTripActivity.this,
                                    new String[]{Manifest.permission.CAMERA},
                                    MY_PERMISSIONS_REQUEST_CAMERA);
                        }
                    }
                } else {
                    Toast.makeText(RecordTripActivity.this, "ELSE", Toast.LENGTH_LONG).show();
                    openCamera();
                }
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

    private void openCamera() {
        File mediaFile = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/aabbcc.jpg"
        );
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        fileUri = Uri.fromFile(mediaFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        startActivityForResult(intent, IMAGE_CAPTURE);
    }

    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        if (requestCode == IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                        this,
                        "Video has been saved to:\n" + fileUri,
                        Toast.LENGTH_LONG
                ).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this,
                        "Video recording cancelled.",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(this,
                        "Failed to record video",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    public static void saveToPreferences(
            Context context,
            String key,
            Boolean allowed
    ) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, allowed);
        prefsEditor.commit();
    }

    public static Boolean getFromPref(Context context, String key) {
        SharedPreferences myPrefs = context.getSharedPreferences(CAMERA_PREF, Context.MODE_PRIVATE);
        return (myPrefs.getBoolean(key, false));
    }

    private void showAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        ActivityCompat.requestPermissions(
                                RecordTripActivity.this,
                                new String[]{Manifest.permission.CAMERA},
                                MY_PERMISSIONS_REQUEST_CAMERA
                        );
                    }
                });
        alertDialog.show();
    }

    // the callback function after the user granted / not granted the permission
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grant_results) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grant_results.length > 0
                        && grant_results[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                    openCamera();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    if (ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.CAMERA)) {

                        showAlert();

                    } else {
                        saveToPreferences(RecordTripActivity.this, ALLOW_KEY, true);
                    }
                }
            }
            case MY_PERMISSIONS_REQUEST_LOCATION: {

            }
        }
    }

    // the alert message for asking permission
    private void showSettingsAlert() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage("App needs to access the Camera.");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //finish();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "SETTINGS",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        startInstalledAppDetailsActivity(RecordTripActivity.this);

                    }
                });
        alertDialog.show();
    }

    public static void startInstalledAppDetailsActivity(final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

}
