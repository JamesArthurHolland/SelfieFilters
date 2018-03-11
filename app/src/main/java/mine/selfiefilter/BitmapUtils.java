package mine.selfiefilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * Created by jamie on 11/03/18.
 */

public class BitmapUtils
{
    public static byte[] bitmapToByteArray(Bitmap bitmap) {
        //Convert to byte array
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public static Bitmap bitmapFromByteArray(byte[] bytes) {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static Bitmap bitmapFromFile(File file) {
        return BitmapFactory.decodeFile(file.getPath());
    }
}
