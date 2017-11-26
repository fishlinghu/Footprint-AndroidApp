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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import static fishlinghu.footprint.SearchActivity.genID;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();
    private ArrayList<Trip> trip_list = new ArrayList<>();
    private ArrayList<String> trip_key_list = new ArrayList<>();
    private String keyword = "";

    private ArrayList<Integer> view_id_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        final LinearLayout ll = findViewById(R.id.ll_main);

        SearchView searchView = findViewById(R.id.search_view_1);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                // remove previous search result
                int i = view_id_list.size() - 1;
                while (i >= 0) {
                    ll.removeView(findViewById(view_id_list.get(i)));
                    i = i - 1;
                }
                trip_list.clear();
                trip_key_list.clear();
                view_id_list.clear();
                // get the user input keyword
                keyword = query.toString().toLowerCase();
                db_reference.child("trips").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // go through trips and record trips containing the keyword
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Trip temp_trip = snapshot.getValue(Trip.class);
                            Log.d("DEBUG---", snapshot.getKey());
                            if (temp_trip.getTripName().toLowerCase().contains(keyword)) {
                                trip_list.add(temp_trip);
                                trip_key_list.add(snapshot.getKey());
                            }
                        }
                        int i = trip_list.size() - 1;
                        while (i >= 0) {
                            final Trip temp_trip = trip_list.get(i);
                            final String temp_trip_key = trip_key_list.get(i);
                            Button temp_button = new Button(getApplicationContext());
                            int view_id = genID();
                            view_id_list.add(view_id);
                            temp_button.setId( view_id );
                            temp_button.setText(temp_trip.getTripName());
                            temp_button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            ll.addView(temp_button);
                            temp_button.setOnClickListener(new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    Intent next_intent = new Intent(MainActivity.this, TripActivity.class);
                                    next_intent.putExtra("trip", temp_trip);
                                    next_intent.putExtra("trip_key", temp_trip_key);
                                    startActivity(next_intent);
                                    finish();
                                }
                            });
                            i = i - 1;
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                return true;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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

        } else if (id == R.id.nav_user_profile) {
            FirebaseUser google_user = FirebaseAuth.getInstance().getCurrentUser();
            String account_email = google_user.getEmail();
            Intent next_intent = new Intent(MainActivity.this, ProfileActivity.class);
            next_intent.putExtra("account_email", account_email);
            startActivity(next_intent);
        } else if (id == R.id.nav_start_trip) {
            startActivity(new Intent(MainActivity.this, RecordTripActivity.class));
        } else if (id == R.id.nav_follow) {
            startActivity(new Intent(MainActivity.this, FollowActivity.class));
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
