package mine.selfiefilter;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PicturePreviewActivity extends AppCompatActivity
{
    public static final String TEMP_FILE_PATH = "TEMP_FILE_PATH";

    private File mPictureFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_picture_preview);
        getSupportActionBar().hide();

        mPictureFile = getFileFromIntent();

        setImageFromBitmap(BitmapUtils.bitmapFromFile(mPictureFile));

        ImageView closeButton = findViewById(R.id.close_button);
        ImageView shareButton = findViewById(R.id.share_button);
        ImageView saveButton = findViewById(R.id.save_button);

        initialiseCloseButton(closeButton);
        initialiseShareButton(shareButton);
        initialiseSaveButton(saveButton);
    }

    private void setImageFromBitmap(Bitmap bitmap) {
        ImageView picturePreviewImageView = findViewById(R.id.picture_preview_image_view);
        Glide.with(PicturePreviewActivity.this).load(bitmap).into(picturePreviewImageView);
    }

    private File getFileFromIntent() {
        Intent intent = getIntent();
        String tempFilePath = intent.getStringExtra(TEMP_FILE_PATH);
        return new File(tempFilePath);
    }

    private void initialiseCloseButton(ImageView closeButton) {
        closeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initialiseShareButton(ImageView shareButton) {
        shareButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri uri = Uri.fromFile(mPictureFile);
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("image/jpeg");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(sharingIntent, "Share image using"));
            }
        });
    }

    private void initialiseSaveButton(ImageView saveButton) {
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    File storagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

                    File newImageFile = new File(storagePath,
                        Long.toString(System.currentTimeMillis()) + ".jpg");

                    FileOutputStream out = new FileOutputStream(newImageFile);
                    Bitmap bitmap = BitmapUtils.bitmapFromFile(mPictureFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);
                    out.flush();
                    out.close();

                    finish();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }


}
