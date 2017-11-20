package fishlinghu.footprint;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

public class LocationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private CheckIn current_check_in;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storage_reference;

    private String author_email;

    long SIXTEEN_MEGABYTE = 1024 * 1024 * 16;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        current_check_in = (CheckIn) getIntent().getSerializableExtra("current_check_in");

        // set download button function
        Button button_download = findViewById(R.id.button_download);
        button_download.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Toast.makeText(LocationActivity.this,
                        "The photo is still loading",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        // set location name
        TextView textView_location_name = findViewById(R.id.textView_location_name);
        textView_location_name.setText(current_check_in.getLocationName());
        textView_location_name.setGravity(Gravity.CENTER_HORIZONTAL);

        // set location description
        TextView textView_location_description = findViewById(R.id.textView_location_description);
        textView_location_description.setText(current_check_in.getDescription());

        // set visiting time
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String visiting_time = sdf.format(current_check_in.getDateTime());
        TextView textView_visiting_time = findViewById(R.id.textView_visiting_time);
        textView_visiting_time.setText("Visited on " + visiting_time);
        textView_visiting_time.setGravity(Gravity.CENTER_HORIZONTAL);

        // make the filename for photo
        SimpleDateFormat sdf_2 = new SimpleDateFormat("yyyyMMdd-HHmm");
        final String filename = sdf_2.format(current_check_in.getDateTime()) + ".jpg";

        author_email = (String) getIntent().getSerializableExtra("author_email");
        storage_reference = storage.getReferenceFromUrl("gs://footprint-aff8d.appspot.com")
                .child("images")
                .child(author_email);

        StorageReference photo_ref = storage_reference.child(
                current_check_in.getPhotoUrl()
        );
        photo_ref.getBytes(SIXTEEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                final Bitmap temp_bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                ImageView temp_image_view = findViewById(R.id.imageView_location_photo);
                temp_image_view.setImageBitmap(temp_bitmap);

                // set button behavior as storing photo
                Button button_download = findViewById(R.id.button_download);
                button_download.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        storePhoto(temp_bitmap, filename, LocationActivity.this);
                    }
                });
            }
        });
    }

    public static void storePhoto(Bitmap bitmap, String filename, Context context) {
        File mediaFile = new File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename
                );
        try {
            FileOutputStream fos = new FileOutputStream(mediaFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();
            Toast.makeText(context,
                    "Photo stored at " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename,
                    Toast.LENGTH_LONG
            ).show();
        } catch (Exception e) {
            Log.d("DEBUG---", e.toString());
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(LocationActivity.this, MainActivity.class));
        } else if (id == R.id.nav_user_profile) {
            FirebaseUser google_user = FirebaseAuth.getInstance().getCurrentUser();
            String account_email = google_user.getEmail();
            Intent next_intent = new Intent(LocationActivity.this, ProfileActivity.class);
            next_intent.putExtra("account_email", account_email);
            startActivity(next_intent);
        } else if (id == R.id.nav_start_trip) {
            startActivity(new Intent(LocationActivity.this, RecordTripActivity.class));
        } else if (id == R.id.nav_follow) {
            startActivity(new Intent(LocationActivity.this, FollowActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
