package net.mythoclast.birthday.android;

import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;


public class BirthdayWidgetProvider extends AppWidgetProvider {

	@Override
	public void onEnabled(Context context) {
		long herBirthday = Birthday.time();
		
		Intent receiver = new Intent(context, BirthdayWidgetReceiver.class);
		PendingIntent birthday = PendingIntent.getBroadcast(context, 0, receiver, 0);
		
		AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		manager.set(AlarmManager.RTC, herBirthday, birthday);
	}
	
	@Override
	public void onUpdate(Context context, AppWidgetManager manager, int[] appWidgetIDs) {		
		int answerID = Birthday.answer(Birthday.isIt(), Locale.getDefault());
        String answer = context.getResources().getString(answerID);
		
        // Perform this loop procedure for each widget that belongs to this provider
        final int length = appWidgetIDs.length;
        for (int i = 0; i < length; i++) {
            int appWidgetID = appWidgetIDs[i];
            RemoteViews views = buildView(context, answer);
            manager.updateAppWidget(appWidgetID, views);
        }
	}
	
	public static RemoteViews buildView(Context context, String answer) {
		RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setTextViewText(R.id.answer, answer);
        return views;
	}
}