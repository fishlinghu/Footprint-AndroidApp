package fishlinghu.footprint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        final FirebaseUser google_user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference db_reference = FirebaseDatabase.getInstance().getReference();

        final EditText name = findViewById(R.id.editText_name);
        final EditText self_intro = findViewById(R.id.editText_self_intro);

        final String account_email = google_user.getEmail();

        Button button_confirm = findViewById(R.id.button_create_account);
        button_confirm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                User new_user_data = new User(
                        name.getText().toString(),
                        self_intro.getText().toString(),
                        google_user.getPhotoUrl().toString()
                );

                db_reference.child("users").child(account_email.replace(".",",")).setValue(new_user_data);

                startActivity(new Intent(CreateAccountActivity.this, MainActivity.class));
                finish();
            }
        });
    }
}
