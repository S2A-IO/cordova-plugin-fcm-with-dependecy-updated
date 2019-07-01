package com.gae.scaffolder.plugin;



import android.app.NotificationManager;

import android.app.PendingIntent;

import android.content.Context;

import android.content.Intent;

import android.content.pm.PackageManager;

import android.content.pm.ResolveInfo;

import android.media.RingtoneManager;

import android.net.Uri;

import android.os.Bundle;

import android.support.v4.app.NotificationCompat;

import android.util.Log;



import java.util.List;

import java.util.Map;

import java.util.HashMap;



import com.google.firebase.messaging.FirebaseMessagingService;

import com.google.firebase.messaging.RemoteMessage;



import me.leolin.shortcutbadger.ShortcutBadger;



/**

 * Created by Felipe Echanique on 08/06/2016.

 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {



    private static final String TAG = "FCMPlugin";



    /**

     * Called when message is received.

     *

     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.

     */

    // [START receive_message]

    @Override

    public void onMessageReceived(RemoteMessage remoteMessage) {

        // TODO(developer): Handle FCM messages here.

        // If the application is in the foreground handle both data and notification messages here.

        // Also if you intend on generating your own notifications as a result of a received FCM

        // message, here is where that should be initiated. See sendNotification method below.

        Log.d(TAG, "==> MyFirebaseMessagingService onMessageReceived");



        if(remoteMessage.getNotification() != null){

            Log.d(TAG, "\tNotification Title: " + remoteMessage.getNotification().getTitle());

            Log.d(TAG, "\tNotification Message: " + remoteMessage.getNotification().getBody());

        }



        Map<String, Object> data = new HashMap<String, Object>();

        data.put("wasTapped", false);



        if(remoteMessage.getNotification() != null){

            data.put("title", remoteMessage.getNotification().getTitle());

            data.put("body", remoteMessage.getNotification().getBody());

        }



        for (String key : remoteMessage.getData().keySet()) {

                Object value = remoteMessage.getData().get(key);

                Log.d(TAG, "\tKey: " + key + " Value: " + value);

                data.put(key, value);

        }



        Log.d(TAG, "\tNotification Data: " + data.toString());

        FCMPlugin.sendPushPayload( data );

        //sendNotification(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody(), remoteMessage.getData());

    }

    // [END receive_message]



    /**

     * Create and show a simple notification containing the received FCM message.

     *

     * @param messageBody FCM message body received.

     */

    private void sendNotification(String title, String messageBody, Map<String, Object> data) {

        Intent intent = new Intent(this, FCMPluginActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        for (String key : data.keySet()) {

            intent.putExtra(key, data.get(key).toString());

        }

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,

                PendingIntent.FLAG_ONE_SHOT);



        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)

                .setSmallIcon(getApplicationInfo().icon)

                .setContentTitle(title)

                .setContentText(messageBody)

                .setAutoCancel(true)

                .setSound(defaultSoundUri)

                .setContentIntent(pendingIntent);



        NotificationManager notificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);



        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());

    }



    @Override

    public void handleIntent( Intent intent ) {

        Log.d(TAG, "==> MyFirebaseMessagingService handleIntent");

        Bundle bundle = intent.getExtras();

        if ( bundle != null ) {

            for (String key : bundle.keySet()) {

                Object value = bundle.get(key);

                Log.d(TAG, "\tKey: " + key + " Value: " + value);

                if ( key.equals("gcm.notification.badge") || key.equals( "badge" ) ) {

                    try {

                        setBadge(getApplicationContext(), Integer.parseInt( value.toString() ));

                        break;

                    } catch ( Error err ) {}

                }

            }

        }

        super.handleIntent(intent);

    }



    private void setBadge(Context context, int count) {

        String launcherClassName = getLauncherClassName(context);

        if (launcherClassName == null) {

            Log.e("classname","null");

            return;

        }

        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");

        intent.putExtra("badge_count", count);

        intent.putExtra("badge_count_package_name", context.getPackageName());

        intent.putExtra("badge_count_class_name", launcherClassName);

        context.sendBroadcast(intent);

        ShortcutBadger.applyCount(getApplicationContext(), count);

    }



    private String getLauncherClassName(Context context) {



        PackageManager pm = context.getPackageManager();



        Intent intent = new Intent(Intent.ACTION_MAIN);

        intent.addCategory(Intent.CATEGORY_LAUNCHER);



        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfos) {

            String pkgName = resolveInfo.activityInfo.applicationInfo.packageName;

            if (pkgName.equalsIgnoreCase(context.getPackageName())) {

                String className = resolveInfo.activityInfo.name;

                return className;

            }

        }

        return null;

    }

}
