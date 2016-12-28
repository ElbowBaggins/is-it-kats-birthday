package net.mythoclast.birthday.android;

import java.util.Locale;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class BirthdayWidgetReceiver extends BroadcastReceiver {

	/**
	 * This receiver runs only on Kat's birthday, at midnight.
	 *  
	 * It is meant to handle the case where a widget has already been added prior to Kat's birthday,
	 * and to make it so that the widget updates precisely at midnight for the avid watcher.
	 * 
	 * If a widget is added on Kat's birthday, the correct answer will be set in the 
	 * BirthdayWidgetProvider's onUpdate method. 
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		/* Essentially gambling here that the widgets can all be updated within the few seconds
		 * that a BroadcastReceiver has to work with. If someone has like 1000 widgets somehow, then they 
		 * might conceivably see an ANR, and maybe I should have made a Service. I'm okay with that.
		 *
		 * Of course you are. :)
		 */
		int answerID = Birthday.answer(Birthday.isIt(), Locale.getDefault());
		String answer = context.getResources().getString(answerID);
        
        RemoteViews views = BirthdayWidgetProvider.buildView(context, answer);
        
        ComponentName widget = new ComponentName(context, BirthdayWidgetProvider.class);
		AppWidgetManager manager = AppWidgetManager.getInstance(context);
		manager.updateAppWidget(widget, views);
	}
}
