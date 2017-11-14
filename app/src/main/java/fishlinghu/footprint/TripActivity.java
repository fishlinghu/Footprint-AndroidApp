package fishlinghu.footprint;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Semaphore;

import static fishlinghu.footprint.RecordTripActivity.getResizedBitmap;
import static fishlinghu.footprint.RecordTripActivity.plotMap;
import static fishlinghu.footprint.SearchActivity.genID;

public class TripActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private Trip current_trip;
    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storage_reference;

    private FirebaseUser google_user;
    private String account_email;
    private User user_data;

    private GoogleMap google_map;
    private MapView map_view;

    long SIXTEEN_MEGABYTE = 1024 * 1024 * 16;

    private Task<Void> all_task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        map_view = findViewById(R.id.mapView_trip);
        map_view.onCreate(savedInstanceState);
        map_view.getMapAsync(this);

        // get current user and email
        google_user = FirebaseAuth.getInstance().getCurrentUser();
        account_email = google_user.getEmail();

        storage_reference = storage.getReferenceFromUrl("gs://footprint-aff8d.appspot.com")
                .child("images")
                .child(account_email);

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

        current_trip = (Trip) getIntent().getSerializableExtra("trip");
        Log.d("DEBUG---", current_trip.getTripName());

        TextView textView_trip_name = findViewById(R.id.textView_trip_name);
        textView_trip_name.setText(current_trip.getTripName());

        // render photo buttons below the map
        final ArrayList<ImageView> image_view_list = new ArrayList<>();
        LinearLayout ll = findViewById(R.id.ll_trip_in);
        int i = 0;
        while (i < current_trip.getCheckInList().size()) {
            final int j = i;
            ImageView temp_image_view = new ImageView(getApplicationContext());
            temp_image_view.setId(genID());
            ll.addView(temp_image_view);
            temp_image_view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent next_intent = new Intent(TripActivity.this, LocationActivity.class);
                    next_intent.putExtra("current_check_in", current_trip.getCheckInList().get(j));
                    startActivity(next_intent);
                    finish();
                }
            });
            image_view_list.add(temp_image_view);
            i = i + 1;
        }

        // download all photos
        final ArrayList<Bitmap> photo_list = new ArrayList<>();
        i = 0;
        for (CheckIn temp_check_in : current_trip.getCheckInList()) {
            photo_list.add(null);
            final int j = i;
            StorageReference photo_ref = storage_reference.child(
                    temp_check_in.getPhotoUrl()
            );
            photo_ref.getBytes(SIXTEEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap temp_bitmap = getResizedBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    photo_list.set(j, temp_bitmap);
                    ImageView temp_image_view = image_view_list.get(j);
                    temp_image_view.setImageBitmap(temp_bitmap);
                    temp_image_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }
            });
            i = i + 1;
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        google_map = map;
        plotMap(current_trip, google_map, TripActivity.this);
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
            startActivity(new Intent(TripActivity.this, MainActivity.class));
        } else if (id == R.id.nav_user_profile) {
            startActivity(new Intent(TripActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_start_trip) {
            startActivity(new Intent(TripActivity.this, RecordTripActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
