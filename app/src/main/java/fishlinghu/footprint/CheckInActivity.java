package fishlinghu.footprint;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class CheckInActivity extends AppCompatActivity {

    private Uri fileUri = Uri.parse("content://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/aabbcc.jpg");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/aabbcc.jpg";
        Bitmap bitmap = BitmapFactory.decodeFile(filepath);

        Matrix matrix = new Matrix();
        /*
        try {
            int rotated_degree;
            ExifInterface exif = new ExifInterface(filepath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotated_degree = 90;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotated_degree = 180;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotated_degree = 270;
                default:
                    rotated_degree = 0;
            }
            Log.d("Debug-----", Integer.toString(rotated_degree));
            matrix.postRotate(rotated_degree);
        } catch (java.io.IOException e) {

        }
        */
        String[] orientationColumn = { MediaStore.Images.ImageColumns.ORIENTATION };
        Cursor cur = this.getContentResolver().query(fileUri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur == null) {
            Log.d("Debug-----", "Cur is null");
        }
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(0);
            Log.d("Debug-----", Integer.toString(orientation));
        }
        matrix.postRotate(orientation);

        ImageView image = findViewById(R.id.imageView_check_in_photo);
        Bitmap rotated_bitmap = Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        image.setImageBitmap(rotated_bitmap);

        Button button_finish = findViewById(R.id.button_finish_check_in);
        button_finish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(CheckInActivity.this, RecordTripActivity.class));
                finish();
            }
        });
    }
}
