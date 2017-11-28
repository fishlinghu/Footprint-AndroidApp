/* Reference */
/* http://www.coderzheaven.com/2016/07/29/simple-example-on-using-camera-access-permission-in-android-marshmallow/ */
/* http://www.techotopia.com/index.php/Video_Recording_and_Image_Capture_on_Android_6_using_Camera_Intents */
package fishlinghu.footprint;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class RecordTripActivity extends AppCompatActivity implements OnMapReadyCallback {

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    private static final int MY_PERMISSIONS_REQUEST_CAMERA = 101;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 102;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 103;
    private static final int IMAGE_CAPTURE = 1;
    private Uri fileUri = Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/aabbcc.jpg");
    private static final String ALLOW_KEY = "ALLOWED";
    private static final String CAMERA_PREF = "camera_pref";

    final long SIXTEEN_MEGABYTE = 1024 * 1024 * 16;

    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storage_reference;

    private FirebaseUser google_user;
    private String account_email;
    private User user_data;

    private Boolean trip_flag;
    private Trip current_trip;

    private Calendar calendar;

    private GoogleMap google_map;
    private MapView map_view;

    private void checkAndRequirePermission() {
        if (ContextCompat.checkSelfPermission(
                RecordTripActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED) {
            // permission is not yet granted
            Toast.makeText(RecordTripActivity.this, "Try to get storage permission", Toast.LENGTH_LONG).show();

            if (getFromPref(RecordTripActivity.this, ALLOW_KEY)) {
                showSettingsAlert();
            } else if (ContextCompat.checkSelfPermission(RecordTripActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(RecordTripActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showAlert(MY_PERMISSIONS_REQUEST_STORAGE);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(RecordTripActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            MY_PERMISSIONS_REQUEST_STORAGE);
                }
            }
        }
        if (ContextCompat.checkSelfPermission(
                RecordTripActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            // permission is not yet granted
            Toast.makeText(RecordTripActivity.this, "Try to get location permission", Toast.LENGTH_LONG).show();

            if (getFromPref(RecordTripActivity.this, ALLOW_KEY)) {
                showSettingsAlert();
            } else if (ContextCompat.checkSelfPermission(RecordTripActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(RecordTripActivity.this,
                        Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showAlert(MY_PERMISSIONS_REQUEST_LOCATION);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(RecordTripActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);
                }
            }
        }
        displayLocationSettingsRequest(this);
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
                    showAlert(MY_PERMISSIONS_REQUEST_CAMERA);
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(RecordTripActivity.this,
                            new String[]{Manifest.permission.CAMERA},
                            MY_PERMISSIONS_REQUEST_CAMERA);
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_trip);

        checkAndRequirePermission();

        map_view = findViewById(R.id.mapView_record_trip);
        map_view.onCreate(savedInstanceState);
        map_view.getMapAsync(this);

        // get current user and email
        google_user = FirebaseAuth.getInstance().getCurrentUser();
        account_email = google_user.getEmail();

        storage_reference = storage.getReferenceFromUrl("gs://footprint-aff8d.appspot.com")
                 .child("images")
                 .child(account_email);

        // check if there is unfinished trip
        db_reference = FirebaseDatabase.getInstance().getReference();

        Query query = db_reference.child("users").child( account_email.replace(".",",") );
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    user_data = dataSnapshot.getValue(User.class);
                    trip_flag = user_data.getUnfinishedTripFlag();
                    if (trip_flag == false) {
                        // start a new trip
                        db_reference.child("users").child(account_email.replace(".",",")).child("unfinishedTrip").setValue(new Trip());
                        db_reference.child("users").child(account_email.replace(".",",")).child("unfinishedTripFlag").setValue(true);
                    }
                    current_trip = dataSnapshot.child("unfinishedTrip").getValue(Trip.class);
                    if (current_trip != null) {
                        plotMap(current_trip, google_map, RecordTripActivity.this);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Button button_check_in = findViewById(R.id.button_check_in);
        button_check_in.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                    Toast.makeText(RecordTripActivity.this, "ELSE", Toast.LENGTH_LONG).show();
                    openCamera();
            }
        });

        Button button_finish_trip = findViewById(R.id.button_finish_trip);
        button_finish_trip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Query query = db_reference.child("users").child( account_email.replace(".",",") );
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            user_data = dataSnapshot.getValue(User.class);
                            trip_flag = user_data.getUnfinishedTripFlag();
                            if (trip_flag == false) {
                                // start a new trip
                                db_reference.child("users").child(account_email.replace(".",",")).child("unfinishedTrip").setValue(new Trip());
                                db_reference.child("users").child(account_email.replace(".",",")).child("unfinishedTripFlag").setValue(true);
                            }
                            current_trip = dataSnapshot.child("unfinishedTrip").getValue(Trip.class);
                            if ( current_trip.getCheckInList().isEmpty() ) {
                                Toast.makeText(
                                        RecordTripActivity.this,
                                        "The trip is still empty!",
                                        Toast.LENGTH_LONG
                                ).show();
                            } else {
                                Intent next_intent = new Intent(RecordTripActivity.this, TripCompleteActivity.class);
                                next_intent.putExtra("current_trip", current_trip);
                                startActivity(next_intent);
                                finish();
                            }
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        google_map = map;
    }

    static public void plotMap(Trip current_trip, GoogleMap google_map, Context context) {
        final ArrayList<Marker> marker_list = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        // add markers to the map
        int i = 1;
        for (CheckIn temp_check_in : current_trip.getCheckInList()) {
            LatLng temp_lat_lng = new LatLng( temp_check_in.getLatitude(), temp_check_in.getLongitude());
            Marker temp_marker = google_map.addMarker(
                    new MarkerOptions()
                        .position(temp_lat_lng).title(temp_check_in.getLocationName()).snippet(Integer.toString(i))
            );
            temp_marker.showInfoWindow();
            marker_list.add(temp_marker);
            builder.include(temp_marker.getPosition());
            ++i;
        }

        // replace the icon as photo
        /*
        int i = 0;

        while (i < marker_list.size()) {

            final int j = i;
            StorageReference photo_ref = storage_reference.child(
                    current_trip.getCheckInList().get(i).getPhotoUrl()
            );
            photo_ref.getBytes(SIXTEEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    marker_list.get(j).setIcon(BitmapDescriptorFactory.fromBitmap(
                            getResizedBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length))
                            )
                    );
                }
            });
        }
        */

        // plot the route between markers
        i = 0;
        Marker first_marker, second_marker;
        while (i < marker_list.size() - 1) {
            first_marker = marker_list.get(i);
            second_marker = marker_list.get(i + 1);
            String url = getDirectionsUrl(first_marker.getPosition(), second_marker.getPosition());
            FetchUrl fetch_url = new FetchUrl(google_map);
            fetch_url.execute(url);
            i = i + 1;
        }

        // center the map to view all marker
        if (current_trip.getCheckInList().size() > 0) {
            LatLngBounds bounds = builder.build();
            int padding = 50; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            google_map.moveCamera(cu);
        } else {
            Toast.makeText(
                    context,
                    "No Marker",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    static public Bitmap getResizedBitmap(Bitmap bm) {
        // "RECREATE" THE NEW BITMAP
        Bitmap resized_bitmap = Bitmap.createScaledBitmap(bm, bm.getWidth()/15, bm.getHeight()/15, false);
        bm.recycle();
        return resized_bitmap;
    }


    static public String getDirectionsUrl(LatLng origin, LatLng dest){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;

        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+sensor;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;

        return url;
    }

    private void openCamera() {
        // set the filepath
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
        // open camera to capture picture
        startActivityForResult(intent, IMAGE_CAPTURE);
    }

    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {
        if (requestCode == IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                // get current user and email
                google_user = FirebaseAuth.getInstance().getCurrentUser();
                account_email = google_user.getEmail();

                // check if there is unfinished trip
                db_reference = FirebaseDatabase.getInstance().getReference();
                Query query = db_reference.child("users").child( account_email.replace(".",",") );
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            user_data = dataSnapshot.getValue(User.class);
                            trip_flag = user_data.getUnfinishedTripFlag();
                            if (trip_flag == false) {
                                // start a new trip
                                db_reference.child("users").child(account_email.replace(".",",")).child("unfinishedTrip").setValue(new Trip());
                                db_reference.child("users").child(account_email.replace(".",",")).child("unfinishedTripFlag").setValue(true);
                            }
                            current_trip = dataSnapshot.child("unfinishedTrip").getValue(Trip.class);

                            Toast.makeText(
                                    RecordTripActivity.this,
                                    "Photo has been saved to:\n" + fileUri,
                                    Toast.LENGTH_LONG
                            ).show();
                            // get time
                            calendar = Calendar.getInstance();
                            uploadPhoto();
                            Intent next_intent = new Intent(RecordTripActivity.this, CheckInActivity.class);
                            next_intent.putExtra("current_trip", current_trip);
                            next_intent.putExtra("current_calendar", calendar);
                            startActivity(next_intent);
                            finish();
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this,
                        "Photo capturing cancelled.",
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(this,
                        "Failed to capture photo",
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

    private void showAlert(int permission_request_code) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "DONT ALLOW",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                });

        switch (permission_request_code) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                alertDialog.setMessage("App needs to access the Camera.");
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
            }
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                alertDialog.setMessage("App needs to access the Location.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(
                                        RecordTripActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION
                                );
                            }
                        });
            }
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                alertDialog.setMessage("App needs to access the Storage.");
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ALLOW",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                ActivityCompat.requestPermissions(
                                        RecordTripActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        MY_PERMISSIONS_REQUEST_STORAGE
                                );
                            }
                        });
            }
        }

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

                        showAlert(MY_PERMISSIONS_REQUEST_CAMERA);

                    } else {
                        saveToPreferences(RecordTripActivity.this, ALLOW_KEY, true);
                    }
                }
            }
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grant_results.length > 0
                        && grant_results[0] == PackageManager.PERMISSION_GRANTED) {}
                else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )) {

                        showAlert(MY_PERMISSIONS_REQUEST_STORAGE);

                    } else {
                        saveToPreferences(RecordTripActivity.this, ALLOW_KEY, true);
                    }
                }
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

    private void startInstalledAppDetailsActivity(final Activity context) {
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

    private void uploadPhoto() {
        // get local time
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String local_time = sdf.format(calendar.getTime());
        Toast.makeText(this, local_time, Toast.LENGTH_LONG).show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();

        StorageReference riversRef = storageRef.child("images/" + account_email + "/" + local_time + ".jpg");
        UploadTask uploadTask = riversRef.putFile(fileUri);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                @SuppressWarnings("VisibleForTests") Uri downloadUrl = taskSnapshot.getDownloadUrl();
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
                        Log.d("DEBUG---", "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.d("DEBUG---", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            // why 999 works here?
                            status.startResolutionForResult(RecordTripActivity.this, 999);
                        } catch (IntentSender.SendIntentException e) {
                            Log.d("DEBUG---", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.d("DEBUG---", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        map_view.onResume();
        super.onResume();
    }

    @Override
    public final void onDestroy() {
        map_view.onDestroy();
        super.onDestroy();
    }

    @Override
    public final void onLowMemory() {
        map_view.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public final void onPause() {
        map_view.onPause();
        super.onPause();
    }

}
