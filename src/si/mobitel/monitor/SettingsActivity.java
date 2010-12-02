package si.mobitel.monitor;

import java.util.Date;

import si.mobitel.monitor.db.SettingsPersistence;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsActivity extends Activity {
	EditText telefonska;
	EditText passEdit;
	SettingsPersistence settings;
	LinearLayout quotaslayout;
	RequestData rd;
	Boolean created = false;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = new SettingsPersistence(this);
        if (rd == null)
        	rd = new RequestData(this);
        
        setContentView(R.layout.settings);
        telefonska = (EditText) findViewById(R.id.telStSettings);
        passEdit = (EditText) findViewById(R.id.gesloSettings);
        quotaslayout = (LinearLayout) findViewById(R.id.QuotasSettingsLayout);
        telefonska.setText(settings.getPhoneNumber());
        passEdit.setText(settings.getSetting("password"));
        passEdit.requestFocus();
        Log.d("settings", "Geslo: "+ passEdit.getText());
        
        Button save = (Button) findViewById(R.id.shraniButtonSettings);
        save.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View v) {
				Boolean enako = true;
				if (!settings.getPhoneNumber().equalsIgnoreCase(telefonska.getText().toString())) {
					settings.setPhoneNumber(telefonska.getText().toString());
					enako = false;
					
				}
				if (!passEdit.getText().toString().equalsIgnoreCase(settings.getPassword())) {
					enako = false;
				}
				settings.setSetting("password", passEdit.getText().toString());
				if (!enako) {
					refreshQuotas();
				}
				settings.save();
				//
				//loadQuotas();
				finish();
			}
		});
        loadQuotas();
	}
	
	private void refreshQuotas() {
        Date now = new Date();
        MonitorResult result;
        MobitelMonitor application  = ((MobitelMonitor)getApplicationContext());
        long nowMilis= now.getTime();
        long lastMilis = 0;
        if (application.lastRefresh != null) {
        	lastMilis = application.lastRefresh.getTime();
        	lastMilis += 600000;
        }
        if (!telefonska.getText().toString().equalsIgnoreCase(application.lastPhoneNum) || application.monitorResult == null || lastMilis < nowMilis) {
        	result = rd.podatki(telefonska.getText().toString(), passEdit.getText().toString());
        } else {
        	result = application.monitorResult;
        }

	//	MonitorResult result = rd.createResult();
		if (result == null || result.quotas == null) return;
		application.monitorResult = result;
    	application.lastRefresh = new Date();
    	application.lastPhoneNum = telefonska.getText().toString();
		for (Quota qu : result.quotas) {
			if (settings.getSetting("qu:"+qu.description) == null || settings.getSetting(qu.description) == "") {
				settings.setSetting("qu:"+qu.description, "yes");
			}
			settings.save();
		}
	}
	
	private void loadQuotas() {
		quotaslayout.removeAllViews();
		for (String key : settings.settings.keySet()) {
			if (key.contains("qu:")) {
				String name = key.substring(3);
				Boolean selected = settings.getSetting(key).equalsIgnoreCase("yes");
				Log.w("settings", "KVOTA: " + key + " ; vredost: " + settings.getSetting(key));
				CheckBox cb = new CheckBox(this);
				cb.setText(name);
				cb.setTextColor(Color.parseColor("#4c1212"));
				cb.setChecked(selected);
				cb.setButtonDrawable(R.drawable.check);
				cb.setPadding(40, cb.getPaddingTop(), 20, cb.getPaddingBottom());
				cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						String sett = buttonView.getText().toString();
						sett = "qu:" + sett;
						settings.setSetting(sett, isChecked ? "yes" : "no");
						settings.save();
					}
				});
				//TODO daj toti checkbox nekam
				quotaslayout.addView(cb);
			}
		}
	
	}
}
