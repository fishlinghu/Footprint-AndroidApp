package fishlinghu.footprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProfileActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();

    private FirebaseUser google_user;
    private String account_email;
    private User user_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // get current user and email
        google_user = FirebaseAuth.getInstance().getCurrentUser();
        account_email = google_user.getEmail();

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

        db_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_data = dataSnapshot.child("users").child(account_email.replace(".", ",")).getValue(User.class);

                // populate user information in the profile page
                TextView temp_textView;

                temp_textView = (TextView) findViewById(R.id.textView_profile_name);
                temp_textView.setText( "Name:" + user_data.getName() );

                temp_textView = (TextView) findViewById(R.id.textView_profile_intro);
                temp_textView.setText( user_data.getSelfIntro() );

                ImageView temp_ImageView = (ImageView) findViewById(R.id.imageView_profile);
                Glide.with(ProfileActivity.this).load(user_data.getPhotoUrl()).into( temp_ImageView );

                // find all finished trips of an user
                ArrayList<Trip> trip_list = new ArrayList<>();
                Map<String, String> trip_key =
                        (HashMap<String, String>) dataSnapshot
                                .child("users")
                                .child(account_email.replace(".", ","))
                                .child("trips").getValue();
                for (String key : trip_key.keySet()) {
                    trip_list.add(
                            dataSnapshot.child("trips").child(key).getValue(Trip.class)
                    );
                }

                // show the found trips on user's profile
                LinearLayout ll = findViewById(R.id.ll_profile_finished_trip);
                for (final Trip temp_trip : trip_list) {
                    Button temp_button = new Button(getApplicationContext());
                    temp_button.setId( genID() );
                    temp_button.setText(temp_trip.getTripName());
                    temp_button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    ll.addView(temp_button);
                    temp_button.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent next_intent = new Intent(ProfileActivity.this, TripActivity.class);
                            next_intent.putExtra("trip", temp_trip);
                            startActivity(next_intent);
                            finish();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private int genID(){
        AtomicInteger sNextGeneratedId = new AtomicInteger(1);
        for (;;) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF)
                newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
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
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
        } else if (id == R.id.nav_user_profile) {

        } else if (id == R.id.nav_start_trip) {
            startActivity(new Intent(ProfileActivity.this, RecordTripActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
