package mine.selfiefilter;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class TakeSelfieActivity extends AppCompatActivity implements SurfaceHolder.Callback
{
    private Camera mCamera = null;
    private SurfaceView mCameraSurfaceView = null;
    private CustomPagerAdapter mCustomPagerAdapter = null;
    private SurfaceHolder mCameraSurfaceHolder = null;
    private ViewPager mViewPager = null;
    private boolean mPreviewing = false;
    private int mCurrentCameraId = 0;

    private ImageView mCaptureButton = null;
    private ImageView mFlashButton = null;
    private ImageView mReverseCameraButton = null;

    private int mScreenHeight = 1280;
    private int mScreenWidth = 960;

    private int[] mResources = { // TODO change names
        R.drawable.filter1,
        R.drawable.filter2,
        R.drawable.filter3
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        mScreenHeight = displayMetrics.heightPixels;
        mScreenWidth = displayMetrics.widthPixels;

        getSupportActionBar().hide();

        setContentView(R.layout.activity_take_selfie);

        mCustomPagerAdapter = new CustomPagerAdapter(this, mScreenWidth);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mCustomPagerAdapter);

        mCameraSurfaceView = findViewById(R.id.surfaceView1);
        mCameraSurfaceHolder = mCameraSurfaceView.getHolder();
        mCameraSurfaceHolder.addCallback(this);

        try {
            if(getFrontFacingCameraId() != null) {
                mCurrentCameraId = Integer.parseInt(getFrontFacingCameraId());
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


        mFlashButton = findViewById(R.id.flash_button);
        mFlashButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Camera.Parameters p = mCamera.getParameters();

                if(p.getSupportedFlashModes() == null) {
                    return;
                }

                if(p.getFlashMode().equals(android.hardware.Camera.Parameters.FLASH_MODE_OFF)) {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mFlashButton.setImageResource(R.drawable.ic_flash_on_black_24dp);
                    mCamera.setParameters(p);
                    mCamera.startPreview();
                    Log.d("Torch","MODE ON");
                } else if(p.getFlashMode().equals(android.hardware.Camera.Parameters.FLASH_MODE_ON)){
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                    mFlashButton.setImageResource(R.drawable.ic_flash_auto_black_24dp);
                    mCamera.setParameters(p);
                    mCamera.startPreview();
                    Log.d("Torch","MODE AUTO");
                }else if(p.getFlashMode().equals(android.hardware.Camera.Parameters.FLASH_MODE_AUTO)){
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    mFlashButton.setImageResource(R.drawable.ic_flash_off_black_24dp);
                    mCamera.setParameters(p);
                    mCamera.startPreview();
                    Log.d("Torch","MODE OFF");
                }else {
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                    mCamera.setParameters(p);
                    mCamera.startPreview();
                    Log.d("Torch","MODE ON " + p.getFlashMode());
                }
            }
        });

        mCaptureButton = findViewById(R.id.capture_button);
        mCaptureButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mCamera.takePicture(cameraShutterCallback,
                    cameraPictureCallbackRaw,
                    cameraPictureCallbackJpeg);
            }
        });

        mReverseCameraButton = findViewById(R.id.reverse_camera);
        mReverseCameraButton.setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v)
            {
                if(mPreviewing) {
                    mCamera.stopPreview();
                    mPreviewing = false;
                }
                mCamera.release();
                try {
                    if(mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                        mCurrentCameraId = Integer.parseInt(getFrontFacingCameraId());
                    }
                    else if(mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                        mCurrentCameraId = Integer.parseInt(getBackwardsFacingCameraId());
                    }
                    mCamera = getCameraInstance(mCurrentCameraId);
                    mCamera.setPreviewDisplay(mCameraSurfaceHolder);
                    mCamera.startPreview();
                    mPreviewing = true;
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    Camera.ShutterCallback cameraShutterCallback = new Camera.ShutterCallback()
    {
        @Override
        public void onShutter()
        {
            // TODO Auto-generated method stub
        }
    };

    Camera.PictureCallback cameraPictureCallbackRaw = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            // TODO Auto-generated method stub
        }
    };

    Camera.PictureCallback cameraPictureCallbackJpeg = new Camera.PictureCallback()
    {
        @Override
        public void onPictureTaken(byte[] data, Camera camera)
        {
            // TODO Auto-generated method stub
            Bitmap cameraBitmap = BitmapFactory.decodeByteArray
                (data, 0, data.length);

            Matrix matrix = new Matrix();

            int rotationDegrees = 0;
            if(mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                rotationDegrees = 90;
            }
            else if(mCurrentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                rotationDegrees = 270;
            }

            matrix.postRotate(rotationDegrees);
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(cameraBitmap, cameraBitmap.getWidth(), cameraBitmap.getHeight(),true); // TODO get height and weight from phone dimens
            Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);

            int wid = rotatedBitmap.getWidth();
            int hgt = rotatedBitmap.getHeight();

            //  Toast.makeText(getApplicationContext(), wid+""+hgt, Toast.LENGTH_SHORT).show();
            Bitmap newImage = Bitmap.createBitmap(wid, hgt, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(newImage);

            canvas.drawBitmap(rotatedBitmap, 0f, 0f, null);

            int virtualCurrentItemIndex = mViewPager.getCurrentItem();
            int currentItemIndex = virtualCurrentItemIndex % mResources.length;
            Drawable drawable = getResources().getDrawable(mResources[currentItemIndex]);
            drawable.setBounds(0, 0, rotatedBitmap.getWidth(), rotatedBitmap.getHeight());
            drawable.draw(canvas);
            canvas.rotate(270);


            File storagePath = new File(Environment.
                getExternalStorageDirectory() + "/PhotoAR/");
            storagePath.mkdirs();

            File myImage = new File(storagePath,
                Long.toString(System.currentTimeMillis()) + ".jpg");

            try
            {
                FileOutputStream out = new FileOutputStream(myImage);
                newImage.compress(Bitmap.CompressFormat.JPEG, 80, out);
                out.flush();
                out.close();
            }
            catch(FileNotFoundException e)
            {
                Log.d("In Saving File", e + "");
            }
            catch(IOException e)
            {
                Log.d("In Saving File", e + "");
            }

            camera.startPreview();

            newImage.recycle();

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            intent.setDataAndType(Uri.parse("file://" + myImage.getAbsolutePath()), "image/*");
            startActivity(intent);

        }
    };

    public static Camera getCameraInstance(int cameraId){
        Camera camera = null;
        try {
            camera = Camera.open(cameraId); // attempt to get a Camera instance
            camera.setDisplayOrientation(90);
        }
        catch (Exception e){
            Log.e("TakeSelfieActivity", e.getMessage());
        }
        return camera; // returns null if camera is unavailable
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height)
    {
        // TODO Auto-generated method stub

        if(mPreviewing)
        {
            mCamera.stopPreview();
            mPreviewing = false;
        }
        try
        {
            final Camera.Parameters parameters = mCamera.getParameters();
            Size size = getOptimalPreviewSize(parameters.getSupportedPictureSizes(), mScreenWidth, mScreenHeight);
            parameters.setPreviewSize(size.width, size.height); // TODO
            parameters.setPictureSize(size.width, size.height);
//            parameters.setPictureSize(1280, 960);
            if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                mCamera.setDisplayOrientation(90);
            }

            // parameters.setRotation(90);
            mCamera.setParameters(parameters);

            mCamera.setPreviewDisplay(mCameraSurfaceHolder);
            mCamera.startPreview();

            List<String> pList = mCamera.getParameters().getSupportedFlashModes();


            mPreviewing = true;
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.05;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    String getFrontFacingCameraId() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for(final String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        }
        return null;
    }

    String getBackwardsFacingCameraId() throws CameraAccessException {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for(final String cameraId : cameraManager.getCameraIdList()){
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if(cOrientation == CameraCharacteristics.LENS_FACING_BACK) {
                    return cameraId;
                }
            }
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        try
        {
            if(getFrontFacingCameraId() != null){
                mCamera = getCameraInstance(mCurrentCameraId);
            }
        }
        catch (CameraAccessException e){
            Log.e("TakeSelfieActivity", e.toString());
        }
        catch(RuntimeException e)
        {
            Toast.makeText(getApplicationContext(), "Device camera  is not working properly, please try after sometime.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        // TODO Auto-generated method stub
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
        mPreviewing = false;
    }

    class CustomPagerAdapter extends PagerAdapter {

        Context mContext;
        LayoutInflater mLayoutInflater;
        private int mScreenWidth = 960;

        public CustomPagerAdapter(Context context, int screenWidth) {
            mContext = context;
            mScreenWidth = screenWidth;
            mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);

            ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);

            position = position % mResources.length; // use modulo for infinite cycling
            imageView.setImageResource(mResources[position]);

            container.addView(itemView);

            return itemView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }
}
