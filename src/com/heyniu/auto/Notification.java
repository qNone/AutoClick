package com.heyniu.auto;

import android.app.Instrumentation;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

class Notification {

    final static String FAST_MESSAGE = "快速模式已启动。";
    final static String NORMAL_MESSAGE = "迭代模式已启动。";
    final static String REPTILE_MESSAGE = "爬虫模式已启动。";
    final static String RECORD_MESSAGE = "录制模式已启动，请随意操作App。";

    static void showToast(Instrumentation instrumentation, String message) {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(instrumentation.getTargetContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    static void sendNotification(Instrumentation instrumentation, Class cls, String message) {
        Context context = instrumentation.getTargetContext();
        Bitmap bitmap = drawableToBitmap(getTargetAppIcon(instrumentation));
        if (bitmap == null) return;
        android.content.Intent intent = new android.content.Intent(context, cls);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("AutoClick");
        builder.setContentText(message);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(bitmap);
        builder.setSmallIcon(android.R.drawable.stat_sys_download);
        builder.setAutoCancel(true);
        NotificationManager manager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        android.app.Notification notify = builder.build();
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                manager.notify(10000, notify);
            }
        });
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) return null;
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private static Drawable getTargetAppIcon(Instrumentation instrumentation) {
        Context context = instrumentation.getTargetContext();
        PackageManager packageManager = context.getPackageManager();
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(context.getPackageName(), 0);
            return info.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
