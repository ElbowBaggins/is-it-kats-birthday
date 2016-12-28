package net.mythoclast.birthday.android;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;

public class BirthdayAlarm {
	
	public static void setEnabledAlarms(Context context) {
		if (singleEnabled(context))
			setBirthdayAlarm(context);
		
		if (recurringEnabled(context))
			setRecurringAlarm(context);
	}
	
	public static void setBirthdayAlarm(Context context) {
		long time = Birthday.time();
		
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = singleAlarmIntent(context);
	    manager.cancel(alarmIntent);
	    
	    manager.set(AlarmManager.RTC_WAKEUP, time, alarmIntent);
	    
	    Log.d(Birthday.TAG, "Scheduled single alarm for Kat's birthday. (" + formatTime(time) + ")");
	}
	
	public static void cancelBirthdayAlarm(Context context) {
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = singleAlarmIntent(context);
	    manager.cancel(alarmIntent);
	    
	    Log.d(Birthday.TAG, "Canceled single alarm for Kat's birthday.");
	}
	
	public static void setRecurringAlarm(Context context) {
		setRecurringAlarm(context, getInterval(context));
	}
	
	public static void setRecurringAlarm(Context context, String intervalValue) {
		long time = firstRecurringTime(intervalValue);
		long interval = getIntervalMillis(intervalValue);
		
		if (time < 0 || interval < 0) {
			Log.i(Birthday.TAG, "Invalid value for recurring notification interval, no recurring notifications were scheduled.");
			return;
		}
		
		
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = recurringAlarmIntent(context);
		manager.cancel(alarmIntent);
		
		manager.setRepeating(AlarmManager.RTC_WAKEUP, time, interval, alarmIntent);
		
		Log.d(Birthday.TAG, "Scheduled recurring alarm for Kat's birthday. (" + formatTime(time) + ")");
	}
	
	public static void cancelRecurringAlarm(Context context) {
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		PendingIntent alarmIntent = recurringAlarmIntent(context);
		manager.cancel(alarmIntent);
		
		Log.d(Birthday.TAG, "Canceled recurring alarm for Kat's birthday.");
	}
	
	// helper methods
	
	private static PendingIntent singleAlarmIntent(Context context) {
		Intent intent = new Intent(context, BirthdayNotificationReceiver.class);
		intent.setData(Uri.parse("birthday://single"));
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}
	
	private static PendingIntent recurringAlarmIntent(Context context) {
		Intent intent = new Intent(context, BirthdayNotificationReceiver.class);
		intent.setData(Uri.parse("birthday://multiple"));
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}
	
	private static long firstRecurringTime(String interval) {
		if (interval.equals("daily"))
			return nextNoon();
		else if (interval.equals("fifteen"))
			return System.currentTimeMillis() + (15 * 60 * 1000);
		else
			return -1;
	}
	
	private static String getInterval(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getString(BirthdayPreferences.RECURRING_INTERVAL_KEY, BirthdayPreferences.RECURRING_INTERVAL_DEFAULT);
	}
	
	private static long getIntervalMillis(String interval) {
		if (interval.equals("daily"))
			return (24 * 60 * 60 * 1000);
		else if (interval.equals("fifteen"))
			return (15 * 60 * 1000);
		else
			return -1;
	}
	
	private static long nextNoon() {
		Time now = new Time();
		now.setToNow();
		
		Time noon = new Time();
		
		if (now.hour >= 12)
			noon.set(0, 0, 12, now.monthDay + 1, now.month, now.year);
		else
			noon.set(0, 0, 12, now.monthDay    , now.month, now.year);
		
		return noon.toMillis(false);
	}
	
	private static boolean singleEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BirthdayPreferences.SINGLE_ENABLED_KEY, BirthdayPreferences.SINGLE_ENABLED_DEFAULT);
	}
	
	private static boolean recurringEnabled(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(BirthdayPreferences.RECURRING_ENABLED_KEY, BirthdayPreferences.RECURRING_ENABLED_DEFAULT);
	}
	
	private static String formatTime(long time) {
		return time + " (" + DateFormat.format("MM-dd-yyyy hh:mm:ss", time) + ")";
	}
}