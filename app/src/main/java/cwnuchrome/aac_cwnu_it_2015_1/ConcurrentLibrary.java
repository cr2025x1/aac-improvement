package cwnuchrome.aac_cwnu_it_2015_1;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Chrome on 8/3/15.
 *
 * 파일 접근과 같은 부하가 큰 작업을 할 때 로딩 팝업을 뛰우는 독립 메소드를 제공.
 */
public class ConcurrentLibrary {
    public static void run_off_ui_thread(@NonNull Activity activity, @NonNull final Runnable run, @Nullable final Runnable after) {
        // 참고: http://stackoverflow.com/questions/11411022/how-to-check-if-current-thread-is-not-main-thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            final ProgressDialog dialog = ProgressDialog.show(activity, "Please wait...", "Loading...", true);
            dialog.setCancelable(false);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            run.run();
                            dialog.dismiss();
                            if (after != null) after.run();

                        }
                    }
            ).start();
        }
        else {
            run.run();
            if (after != null) after.run();
        }
    }

    public static <T> void run_off_ui_thread_with_result(@NonNull Activity activity, @NonNull final RunnableWithResult<T> run, @Nullable final RunnableWithResult<T> after) {
        // 참고: http://stackoverflow.com/questions/11411022/how-to-check-if-current-thread-is-not-main-thread
        if (Looper.myLooper() == Looper.getMainLooper()) {
            final ProgressDialog dialog = ProgressDialog.show(activity, "Please wait...", "Loading...", true);
            dialog.setCancelable(false);
            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            run.run();
                            dialog.dismiss();
                            if (after != null) {
                                after.setParam(run.getResult());
                                after.run();
                            }
                        }
                    }
            ).start();
        }
        else {
            run.run();
            if (after != null) {
                after.setParam(run.getResult());
                after.run();
            }
        }
    }


}
