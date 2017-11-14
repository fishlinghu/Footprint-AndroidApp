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
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();
    private ArrayList<Trip> trip_list = new ArrayList<>();
    private ArrayList<String> trip_key_list = new ArrayList<>();
    private String keyword = "";

    private ArrayList<Integer> view_id_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
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

        final LinearLayout ll = findViewById(R.id.ll_search);

        Button button_search = findViewById(R.id.button_search);
        button_search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
                EditText editText_keyword = findViewById(R.id.editText_search_keyword);
                keyword = editText_keyword.getText().toString().toLowerCase();
                db_reference.child("trips").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // go through trips and record trips containing the keyword
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Trip temp_trip = snapshot.getValue(Trip.class);
                            if (temp_trip.getTripName().toLowerCase().contains(keyword)) {
                                trip_list.add(temp_trip);
                                trip_key_list.add(snapshot.getKey());
                            }
                        }
                        int i = trip_list.size() - 1;
                        while (i >= 0) {
                            final Trip temp_trip = trip_list.get(i);
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
                                    Intent next_intent = new Intent(SearchActivity.this, TripActivity.class);
                                    next_intent.putExtra("trip", temp_trip);
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
            }
        });
    }

    static public int genID(){
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
            startActivity(new Intent(SearchActivity.this, MainActivity.class));
        } else if (id == R.id.nav_user_profile) {
            startActivity(new Intent(SearchActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_start_trip) {
            startActivity(new Intent(SearchActivity.this, RecordTripActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
