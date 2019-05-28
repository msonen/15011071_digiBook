package com.example.digiBook;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import static com.example.digiBook.DataManager.NOTE_BODY;
import static com.example.digiBook.DataManager.NOTE_TITLE;


public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(context, "Alarm Triggered", Toast.LENGTH_SHORT).show();
        Bundle bundle = intent.getExtras();

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_new)
                    //example for large icon
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_notify))
                    .setContentTitle(bundle.getString(NOTE_TITLE))
                    .setContentText(bundle.getString(NOTE_BODY))
                    .setOngoing(false)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            Intent i = new Intent(context, EditActivity.class);
            i.putExtras(bundle);
            PendingIntent pendingIntent =
                    PendingIntent.getActivity(
                            context,
                            0,
                            i,
                            PendingIntent.FLAG_ONE_SHOT
                    );
            // example for blinking LED
            builder.setLights(0xFFb71c1c, 1000, 2000);
            //builder.setSound(yourSoundUri);
            builder.setContentIntent(pendingIntent);
            manager.notify(12345, builder.build());



    }
}

