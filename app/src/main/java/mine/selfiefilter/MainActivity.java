package mine.selfiefilter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;

public class MainActivity extends AppCompatActivity implements RecyclerViewPhotoAdapter.ItemClickListener
{
    private RecyclerViewPhotoAdapter mRecyclerAdapter;

    public static final int REQUEST_USE_CAMERA = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.take_selfie);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_USE_CAMERA);
                } else {
                    // Permission has already been granted
                    launchTakeSelfieActivity();
                }
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.pictures);
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        mRecyclerAdapter = new RecyclerViewPhotoAdapter(this, getFiles());
        mRecyclerAdapter.setClickListener(this);
        recyclerView.setAdapter(mRecyclerAdapter);

    }

    private void launchTakeSelfieActivity()
    {
        Intent i = new Intent(MainActivity.this, TakeSelfieActivity.class);
        startActivity(i);
    }

    private ArrayList<File> getFiles()
    {
        ArrayList<File> files = new ArrayList<>();
        File folder = new File(Environment.getExternalStorageDirectory() + "/PhotoAR/");
        File[] listOfFiles = folder.listFiles();

        if(listOfFiles != null) {
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) { // TODO check it's an image
                    files.add(listOfFiles[i]);
                    System.out.println("File " + listOfFiles[i].getName());
                } else if (listOfFiles[i].isDirectory()) {
                    System.out.println("Directory " + listOfFiles[i].getName());
                }
            }
        }
        return files;
    }

    @Override
    public void onItemClick(View view, int position) {
        Intent showPictureIntent = new Intent(MainActivity.this, PicturePreviewActivity.class);
        showPictureIntent.putExtra(PicturePreviewActivity.TEMP_FILE_PATH, mRecyclerAdapter.getItem(position).getAbsolutePath());
        startActivity(showPictureIntent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_USE_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    launchTakeSelfieActivity();

                } else {
                    // permission denied
                }
                return;
            }
        }
    }
}
