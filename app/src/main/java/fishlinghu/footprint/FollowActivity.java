package fishlinghu.footprint;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;
import java.util.Map;

public class FollowActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();

    private FirebaseUser google_user;
    private String account_email;
    private User user_data; // user data shown by profile page

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
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

        // implement clear all button
        Button button_clear = findViewById(R.id.button_clear_new_trip);
        button_clear.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // remove data in the database
                db_reference.child("users")
                        .child(account_email.replace(".", ","))
                        .child("newTripMap")
                        .removeValue();
                // remove buttons
                LinearLayout ll = findViewById(R.id.ll_new_trip);
                ll.removeAllViews();
            }
        });

        // get current user and email
        google_user = FirebaseAuth.getInstance().getCurrentUser();
        account_email = google_user.getEmail();

        db_reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user_data = dataSnapshot.child("users").child(account_email.replace(".", ",")).getValue(User.class);
                addButtonsForFollowingPeople();
                addButtonsForNewTrips();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void addButtonsForFollowingPeople() {
        LinearLayout ll = findViewById(R.id.ll_followed_by_you);
        Iterator<Map.Entry<String, String>> it = user_data.getFollowingMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            final String following_account_email = pair.getKey();
            String following_name = pair.getValue();

            Button temp_button = new Button(getApplicationContext());
            temp_button.setText(following_name);
            temp_button.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Intent next_intent = new Intent(FollowActivity.this, ProfileActivity.class);
                    next_intent.putExtra("account_email", following_account_email);
                    startActivity(next_intent);
                }
            });
            ll.addView(temp_button);
        }
    }

    private void addButtonsForNewTrips() {
        final LinearLayout ll = findViewById(R.id.ll_new_trip);
        Iterator<Map.Entry<String, String>> it = user_data.getNewTripMap().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> pair = it.next();
            final String trip_key = pair.getKey();
            final String author_name = pair.getValue();

            // get the trip data
            db_reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Trip current_trip = dataSnapshot.child("trips").child(trip_key).getValue(Trip.class);;

                    Button temp_button = new Button(getApplicationContext());
                    temp_button.setText(current_trip.getTripName() + " by " + author_name);
                    temp_button.setOnClickListener(new View.OnClickListener(){
                        @Override
                        public void onClick(View v) {
                            Intent next_intent = new Intent(FollowActivity.this, TripActivity.class);
                            next_intent.putExtra("trip", current_trip);
                            next_intent.putExtra("trip_key", trip_key);
                            startActivity(next_intent);
                        }
                    });
                    ll.addView(temp_button);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
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
            startActivity(new Intent(FollowActivity.this, MainActivity.class));
        } else if (id == R.id.nav_user_profile) {
            FirebaseUser google_user = FirebaseAuth.getInstance().getCurrentUser();
            String account_email = google_user.getEmail();
            Intent next_intent = new Intent(FollowActivity.this, ProfileActivity.class);
            next_intent.putExtra("account_email", account_email);
            startActivity(next_intent);
        } else if (id == R.id.nav_start_trip) {
            startActivity(new Intent(FollowActivity.this, RecordTripActivity.class));
        } else if (id == R.id.nav_follow) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
