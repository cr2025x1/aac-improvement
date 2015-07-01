package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

/**
 * Created by Chrome on 7/1/15.
 *
 * 출처: http://stackoverflow.com/questions/7021578/resize-drawable-in-android
 */
public class DrawableResizer {
    // 원본
    public static Drawable resize(Drawable image, Context context, int width, int height) {
        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width, height, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }

    // 지정한 크기 내에서 비율을 유지하며 확대 또는 축소하여 그 크기 내에 맞춤
    public static Drawable fitToArea(Drawable image, Context context, int width, int height) {
        float originalImageWidth = image.getIntrinsicWidth();
        float originalImageHeight = image.getIntrinsicHeight();
        float widthScaleRatio = originalImageWidth / width;
        float heightScaleRadio = originalImageHeight / height;

        if (widthScaleRatio > heightScaleRadio) {
            height = (int)(originalImageHeight / widthScaleRatio);
        }
        else if (widthScaleRatio < heightScaleRadio) {
            width = (int)(originalImageWidth / heightScaleRadio);
        }

        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width, height, false);
        return new BitmapDrawable(context.getResources(), bitmapResized);
    }

    // fitToArea는 px 단위를 쓰므로, DP 단위를 입력받아 fitToArea에 넘겨주는 Wrapper
    public static Drawable fitToAreaByDP(Drawable image, Context context, int width, int height) {
        return fitToArea(image,
                context,
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, context.getResources().getDisplayMetrics()),
                (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, height, context.getResources().getDisplayMetrics())
        );
    }
}
