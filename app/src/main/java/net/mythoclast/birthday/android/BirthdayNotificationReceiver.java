package net.mythoclast.birthday.android;

import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

public class BirthdayNotificationReceiver extends BroadcastReceiver {
 
	@Override
	public void onReceive(Context context, Intent intent) {
		int answerID = Birthday.answer(Birthday.isIt(), Locale.getDefault());
		String answer = context.getResources().getString(answerID);
		String title = context.getResources().getString(R.string.app_name);
		Notification notification = getNotification(title, answer, context);
		
		NotificationManager notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notifyManager.notify(title.hashCode(), notification);
		
		Log.i(Birthday.TAG, "Notified of Kat's birthday... Answer: " + answer);
	}
	
	private Notification getNotification(String title, String answer, Context context) {
		int icon = R.drawable.notification;
		long when = System.currentTimeMillis();
		
		Notification notification = new Notification(icon, answer, when);
		Intent intent = new Intent(context, BirthdayActivity.class);
		
		PendingIntent contentIntent = PendingIntent
				.getActivity(context, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
		
		notification.contentIntent = contentIntent;
		notification.contentView = buildView(context, answer);
		
		// Attach notification sound if the user picked one (defaults to silent)
		String ringtone = PreferenceManager.getDefaultSharedPreferences(context).getString(BirthdayPreferences.RINGTONE_KEY, BirthdayPreferences.RINGTONE_DEFAULT);
		if (ringtone != null)
			notification.sound = Uri.parse(ringtone);
		
		// Vibrate unless user disabled it
		boolean vibration = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BirthdayPreferences.VIBRATE_KEY, BirthdayPreferences.VIBRATE_DEFAULT);
		if (vibration)
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		
		// always show the light
		notification.ledARGB = 0xffffffff;
		notification.ledOnMS = 2000;
		notification.flags |= Notification.FLAG_SHOW_LIGHTS;

		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		return notification;
	}
	
	public static RemoteViews buildView(Context context, String answer) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notification);
        views.setTextViewText(R.id.answer, answer);
        return views;
	}
}