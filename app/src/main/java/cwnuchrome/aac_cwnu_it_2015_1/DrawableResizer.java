package cwnuchrome.aac_cwnu_it_2015_1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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

    // 지정한 크기 내에서 비율을 유지하며 딱 맞도록 변형
    public static Drawable fitToArea(Drawable image, Context context, int width, int height) {
        float originalImageWidth = image.getIntrinsicWidth();
        float originalImageHeight = image.getIntrinsicHeight();
        float widthScaleRatio = originalImageWidth / width;
        float heightScaleRadio = originalImageHeight / height;
        int widthPadding = 0;
        int heightPadding = 0;

        if (widthScaleRatio > heightScaleRadio) {
            heightPadding = height;
            height = (int)(originalImageHeight / widthScaleRatio);
            heightPadding = (heightPadding - height) / 2;
        }
        else if (widthScaleRatio < heightScaleRadio) {
            widthPadding = width;
            width = (int)(originalImageWidth / heightScaleRadio);
            widthPadding = (widthPadding - width) / 2 ;
        }

        Bitmap b = ((BitmapDrawable)image).getBitmap();
        Bitmap bitmapResized = Bitmap.createScaledBitmap(b, width, height, false);
        BitmapDrawable bd = new BitmapDrawable(context.getResources(), bitmapResized);
//        bd.setBounds(widthPadding, heightPadding, widthPadding, heightPadding);
        return bd;
    }
}
