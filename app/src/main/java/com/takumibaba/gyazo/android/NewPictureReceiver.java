package com.takumibaba.gyazo.android;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Created by takumi on 2014/05/11.
 */
public class NewPictureReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(!intent.getAction().equals(android.hardware.Camera.ACTION_NEW_PICTURE))
            return;
        Intent serviceIntent  = new Intent(context, GyazoService.class);
        serviceIntent.putExtra("data", intent.getData());
        context.startService(serviceIntent);
    }
}