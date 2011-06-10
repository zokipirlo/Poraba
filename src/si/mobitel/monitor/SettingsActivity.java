package si.mobitel.monitor;

import java.util.Date;

import si.mobitel.monitor.db.SettingsPersistence;
import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;

public class SettingsActivity extends Activity {
	EditText telefonska;
	EditText passEdit;
	SettingsPersistence settings;
	LinearLayout quotaslayout;
	RequestData rd;
	Boolean created = false;
	MobitelMonitor application;
	ProgressDialog requestDialog;
	Button save;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        settings = new SettingsPersistence(this);
        
        setContentView(R.layout.settings);
        
        if (telefonska == null)
        	telefonska = (EditText) findViewById(R.id.telStSettings);
        if (passEdit == null)
        	passEdit = (EditText) findViewById(R.id.gesloSettings);
        if (quotaslayout == null)
        	quotaslayout = (LinearLayout) findViewById(R.id.QuotasSettingsLayout);
        
        telefonska.setText(settings.getPhoneNumber());
        passEdit.setText(settings.getSetting("password"));
        
        if (passEdit.getText() == null || passEdit.getText().length() == 0)
        	passEdit.requestFocus();
        
        Log.d("settings", "Geslo: "+ passEdit.getText());
        
        if (save == null)
        {
	        save = (Button) findViewById(R.id.shraniButtonSettings);
	        save.setOnClickListener(mSaveClick);
        }

        loadQuotas();
	}
	
	OnClickListener mSaveClick = new OnClickListener()
	{
		@Override
		public void onClick(View v)
		{
			Boolean enako = true;
			if (!settings.getPhoneNumber().equalsIgnoreCase(telefonska.getText().toString())) {
				settings.setPhoneNumber(telefonska.getText().toString());
				enako = false;
			}
			if (!passEdit.getText().toString().equalsIgnoreCase(settings.getPassword())) {
				enako = false;
			}
			settings.setSetting("password", passEdit.getText().toString());
			settings.save();
			if (!enako) {
				refreshQuotas();
			}
			else
			{
				//loadQuotas();
				finish();
			}
		}
	};
	
	private void refreshQuotas() {
        /*Date now = new Date();
        MobitelMonitor application  = ((MobitelMonitor)getApplicationContext());
        long nowMilis= now.getTime();
        long lastMilis = 0;
        if (application.lastRefresh != null) {
        	lastMilis = application.lastRefresh.getTime();
        	lastMilis += 600000;	//600.000ms = 10min cache
        }
        if (!telefonska.getText().toString().equalsIgnoreCase(application.lastPhoneNum) || application.monitorResult == null || lastMilis < nowMilis) {
        	//result = rd.podatki(telefonska.getText().toString(), passEdit.getText().toString());
        	rd = new RequestData(this);
        	rd.execute(telefonska.getText().toString(), passEdit.getText().toString());
        } else {
        	//result = application.monitorResult;
        	showResult(application.monitorResult);
        }*/
		rd = new RequestData(this);
    	rd.execute(telefonska.getText().toString(), passEdit.getText().toString());
	}
	
	public void showResult(MonitorResult result)
	{
		//MonitorResult result = rd.createResult();
		if (result == null || result.quotas == null) return;
		if (application == null)
			application  = ((MobitelMonitor)getApplicationContext());
		application.monitorResult = result;
    	application.lastRefresh = new Date();
    	application.lastPhoneNum = telefonska.getText().toString();
		for (Quota qu : result.quotas) {
			if (settings.getSetting("qu:"+qu.description) == null || settings.getSetting(qu.description) == "") {
				settings.setSetting("qu:"+qu.description, "yes");
			}
		}
		
		settings.save();
		finish();
	}
	
	public void showLoader()
	{
		requestDialog = ProgressDialog.show(this, "", "Èakam na podatke. Prosimo poèakajte...", true);
	}
	
	public void hideLoader()
	{
		if (requestDialog != null && requestDialog.isShowing())
			requestDialog.dismiss();
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
	
	@Override
	protected void onDestroy()
	{
		hideLoader();
		super.onDestroy();
	}
}
