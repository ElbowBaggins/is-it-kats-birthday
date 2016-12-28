package net.mythoclast.birthday.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BirthdayBootReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED))
			BirthdayAlarm.setEnabledAlarms(context);
	}

}