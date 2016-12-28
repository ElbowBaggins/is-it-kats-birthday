package net.mythoclast.birthday.android;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.TextView;

public class BirthdayActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        isItHerBirthday(); // set the answer now
        setLocalAlarm(); // so it updates this screen while the user is watching on Kat's birthday
        
        // will cancel and re-schedule all alarms when the user opens the app
        BirthdayAlarm.setEnabledAlarms(this);
    }
    
    public void isItHerBirthday() {
    	((TextView) findViewById(R.id.answer)).setText(Birthday.answer(Birthday.isIt(), Locale.getDefault()));
    }
    
    final Handler handler = new Handler();
    final Runnable updater = new Runnable() {
    	public void run() {
    		isItHerBirthday();
    	}
    };
    
    public void setLocalAlarm() {
    	Thread alarm = new Thread() {
    		public void run() {
    			long untilHerBirthday = Birthday.time() - System.currentTimeMillis();
    			
    			try {
    				sleep(untilHerBirthday);
    			} catch(InterruptedException e) {
    				// well, I never
                    //
                    // MAYBE YOU SHOULD
    			}
    			handler.post(updater);
    		};
    	};
    	alarm.start();
    }
    
    @Override 
	public boolean onCreateOptionsMenu(Menu menu) { 
		super.onCreateOptionsMenu(menu); 
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) { 
		case R.id.preferences:
			startActivity(new Intent(this, BirthdayPreferences.class));
			break;
		}
		return true;
	}
}