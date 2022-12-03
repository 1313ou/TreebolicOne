package treebolic.provider;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

import androidx.annotation.NonNull;

/**
 * Image utilities
 *
 * @author <a href="mailto:1313ou@gmail.com">Bernard Bou</a>
 */
@SuppressWarnings("WeakerAccess")
public class Utils
{
	static private Bitmap bytesToImage(@NonNull final byte[] imageBytes)
	{
		return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
	}

	static public Bitmap roundCrop(@NonNull final Bitmap bitmap)
	{
		final int w = bitmap.getWidth();
		final int h = bitmap.getHeight();
		final Bitmap output = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		final Canvas canvas = new Canvas(output);
		canvas.drawARGB(0, 0, 0, 0);
		final Rect rect = new Rect(0, 0, w, h);

		final int color = 0xff808080;
		final Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(color);
		canvas.drawCircle(h / 2F, h / 2F, h / 2F, paint);    // canvas.drawRoundRect(rect, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	static public Bitmap scale(@NonNull final Bitmap bitmap, int w, int h)
	{
		return Bitmap.createScaledBitmap(bitmap, w, h, false);
	}

	@NonNull
	static String bitmapToUrl(@NonNull final Bitmap bitmap)
	{
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		final byte[] byteArray = byteArrayOutputStream.toByteArray();
		final String imageBase64 = Base64.encodeToString(byteArray, Base64.DEFAULT);
		return "data:image/png;base64," + imageBase64;

		// webview.loadUrl(dataURL); //pass the bitmap base64 dataUrl in URL parameter
	}
}
