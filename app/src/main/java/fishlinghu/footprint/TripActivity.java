package fishlinghu.footprint;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static fishlinghu.footprint.RecordTripActivity.getResizedBitmap;
import static fishlinghu.footprint.RecordTripActivity.plotMap;

public class TripActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private Trip current_trip;
    private String current_trip_key;

    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storage_reference;

    private String author_email;
    private User user_data;
    private String current_user_email;

    private GoogleMap google_map;
    private MapView map_view;

    long SIXTEEN_MEGABYTE = 1024 * 1024 * 16;

    private Boolean is_voted;
    private int vote_count;

    private Task<Void> all_task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        current_user_email = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        map_view = findViewById(R.id.mapView_trip);
        map_view.onCreate(savedInstanceState);
        map_view.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        current_trip = (Trip) getIntent().getSerializableExtra("trip");
        current_trip_key = (String) getIntent().getSerializableExtra("trip_key");
        author_email = current_trip.getAuthorEmail();
        is_voted = current_trip.checkVoter(current_user_email);
        vote_count = current_trip.getVoteCount();

        storage_reference = storage.getReferenceFromUrl("gs://footprint-aff8d.appspot.com")
                .child("images")
                .child(author_email);

        // get author information
        db_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_data = dataSnapshot.child("users").child(author_email.replace(".", ",")).getValue(User.class);

                TextView textView_author_name = findViewById(R.id.textView_author_name);
                textView_author_name.setText(user_data.getName());
                textView_author_name.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent next_intent = new Intent(TripActivity.this, ProfileActivity.class);
                        next_intent.putExtra("account_email", author_email);
                        startActivity(next_intent);
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // show trip name in the layout
        TextView textView_trip_name = findViewById(R.id.textView_trip_name);
        textView_trip_name.setText(current_trip.getTripName());

        // render photo buttons below the map
        final ArrayList<ImageView> image_view_list = new ArrayList<>();
        LinearLayout ll = findViewById(R.id.ll_trip_in);
        int i = 0;
        while (i < current_trip.getCheckInList().size()) {
            final int j = i;
            final CheckIn temp_check_in = current_trip.getCheckInList().get(i);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            TextView temp_text_view = new TextView(getApplicationContext());
            temp_text_view.setText((i+1) + ". " + temp_check_in.getLocationName());
            temp_text_view.setTextColor(Color.BLACK);
            temp_text_view.setLayoutParams(params);
            ll.addView(temp_text_view);

            ImageView temp_image_view = new ImageView(getApplicationContext());
            ll.addView(temp_image_view);
            temp_image_view.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent next_intent = new Intent(TripActivity.this, LocationActivity.class);
                    next_intent.putExtra("current_check_in", temp_check_in);
                    next_intent.putExtra("check_in_idx", j);
                    next_intent.putExtra("trip_key", current_trip_key);
                    next_intent.putExtra("author_email", author_email);
                    startActivity(next_intent);
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
            final Matrix matrix = new Matrix();
            matrix.postRotate(temp_check_in.getPhotoRotatedDegree());
            photo_ref.getBytes(SIXTEEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap temp_bitmap = getResizedBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    Bitmap rotated_bitmap = Bitmap.createBitmap(temp_bitmap , 0, 0, temp_bitmap.getWidth(), temp_bitmap.getHeight(), matrix, true);
                    photo_list.set(j, rotated_bitmap);
                    ImageView temp_image_view = image_view_list.get(j);
                    temp_image_view.setImageBitmap(rotated_bitmap);
                    temp_image_view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                }
            });
            i = i + 1;
        }

        // get vote count information
        final TextView textView_number_of_votes = findViewById(R.id.textView_number_of_votes);
        textView_number_of_votes.setText(vote_count + " votes");

        // implement the vote button
        final Button button_vote = findViewById(R.id.button_vote);
        if (is_voted) {
            button_vote.setText("voted");
        } else {
            button_vote.setText("vote");
        }
        button_vote.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (is_voted) {
                    // state changes from voted to not voted
                    --vote_count;
                    textView_number_of_votes.setText(vote_count + " votes");
                    is_voted = false;
                    button_vote.setText("vote");
                    db_reference.child("trips")
                            .child(current_trip_key)
                            .child("voterMap")
                            .child(current_user_email)
                            .removeValue();
                } else {
                    // state changes from not voted to voted
                    ++vote_count;
                    textView_number_of_votes.setText(vote_count + " votes");
                    is_voted = true;
                    button_vote.setText("voted");
                    db_reference.child("trips")
                            .child(current_trip_key)
                            .child("voterMap")
                            .child(current_user_email)
                            .setValue("");
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        google_map = map;
        try {
            google_map.setMyLocationEnabled(true);
        } catch (SecurityException e) {

        }
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
            FirebaseUser google_user = FirebaseAuth.getInstance().getCurrentUser();
            String account_email = google_user.getEmail();
            Intent next_intent = new Intent(TripActivity.this, ProfileActivity.class);
            next_intent.putExtra("account_email", account_email);
            startActivity(next_intent);
        } else if (id == R.id.nav_start_trip) {
            startActivity(new Intent(TripActivity.this, RecordTripActivity.class));
        } else if (id == R.id.nav_follow) {
            startActivity(new Intent(TripActivity.this, FollowActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
