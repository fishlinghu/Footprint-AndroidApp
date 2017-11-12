package fishlinghu.footprint;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private CheckIn current_check_in;

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

        TextView textView_location_name = findViewById(R.id.textView_location_name);
        textView_location_name.setText(current_check_in.getLocationName());
        textView_location_name.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView textView_location_description = findViewById(R.id.textView_location_description);
        textView_location_description.setText(current_check_in.getDescription());

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        String visiting_time = sdf.format(current_check_in.getDateTime());
        TextView textView_visiting_time = findViewById(R.id.textView_visiting_time);
        textView_visiting_time.setText("Visited on " + visiting_time);
        textView_visiting_time.setGravity(Gravity.CENTER_HORIZONTAL);
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
            startActivity(new Intent(LocationActivity.this, ProfileActivity.class));
        } else if (id == R.id.nav_search) {
            startActivity(new Intent(LocationActivity.this, SearchActivity.class));
        } else if (id == R.id.nav_start_trip) {
            startActivity(new Intent(LocationActivity.this, RecordTripActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
