package si.mobitel.monitor;

import java.text.SimpleDateFormat;
import java.util.Date;

import si.mobitel.monitor.db.SettingsPersistence;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class PorabaActivity extends Activity {
	RequestData rd;
	MonitorResult result;
	Quota q;
	SettingsPersistence settingsp;
	AboutDialog myDialog;
	MobitelMonitor application;
	LinearLayout paketLL;
	TextView naslov;
	ProgressDialog requestDialog;
	ScrollView scroll;
	
	// Se poklice ko se zaprejo settingsi
	@Override
	protected void onResume() {	
		getData();
		super.onResume();
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myDialog = new AboutDialog(this);
        //getData();	will call in onResume
	}
	
	private void getData()
	{
        // Show settings window
        /*Intent settings = new Intent();
        settings.setClass(this, SettingsActivity.class);
		
		startActivity(settings);*/
        /*
        SettingsPersistence settingsp = new SettingsPersistence(this);
        settingsp.getPhoneNumber();
        //Ce ni vneseno geslo vrne null
        settingsp.getPassword();
        settingsp.isQuotaVisible(description);*/
        
		if (application == null)
			application  = ((MobitelMonitor)getApplicationContext());
		if (scroll == null)
		{
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        scroll = (ScrollView) inflater.inflate(R.layout.paket, null);
		}
        
		setContentView(scroll);
		
		if (paketLL == null)
			paketLL = (LinearLayout) findViewById(R.id.PaketLayout);
		if (naslov == null)
			naslov = (TextView) findViewById(R.id.TitleText);
		
        paketLL.removeAllViews();
        paketLL.addView(naslov);
        
        settingsp = new SettingsPersistence(this);
        
        String username = settingsp.getPhoneNumber();
        String pass = settingsp.getPassword();
        
        if (pass == null) {
        	showSettings();
        	return;
        }
        
        Date now = new Date();
        long nowMilis= now.getTime();
        long lastMilis = 0;
        if (application.lastRefresh != null) {
        	lastMilis = application.lastRefresh.getTime();
        	lastMilis += 600000;
        }
        if (!username.equalsIgnoreCase(application.lastPhoneNum) || application.monitorResult == null || lastMilis < nowMilis) {
        	//result = rd.podatki(username, pass);
        	rd = new RequestData(this);
        	rd.execute(username, pass);
        } else {
        	showResult(application.monitorResult);
        }
	}
	
	public void showResult(MonitorResult result)
	{
		//result = rd.createResult();
	    if (result == null) {
	    	//Toast.makeText(this, "Cannot login", Toast.LENGTH_SHORT);
	    	naslov.setText("NAPAKA, POSKUSITE PONOVNO KASNEJE!");
	    } else {
	    	if (application == null)
				application  = ((MobitelMonitor)getApplicationContext());
	    	application.monitorResult = result;
	    	application.lastRefresh = new Date();
	    	application.lastPhoneNum = settingsp.getPhoneNumber();
	    	
	    	if (result.prePaid)
	    	{
	    		StanjeView sv = new StanjeView(this);
	    		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	    		sv.setStanje(Double.toString(result.stanje));
	    		sv.setAirTime(sdf.format(result.airTimeDate));
	    		sv.setDiscDate(sdf.format(result.discDate));
	    		
	    		paketLL.addView(sv);
	    	}
	    	else {
	        	PorabeView pv = new PorabeView(this);
	        	pv.setPorabe(result.hasAdditionalAccount, result.poraba, result.moneta, result.porabaLocenRacun, result.monetaLocenRacun);
	
	        	paketLL.addView(pv);
	        	
	        	QuotasView qvs = new QuotasView(this, settingsp);
	        	qvs.setQuotas(result.quotas);
	        	
	        	paketLL.addView(qvs);
	    	}
	    }
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
	
	@Override
	protected void onDestroy()
	{
		hideLoader();
		super.onDestroy();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.nastavitve:
	        showSettings();
	        return true;
	    case R.id.exit:
	        finish();
	        return true;
	    case R.id.refresh:
	    	if (application == null)
				application = ((MobitelMonitor)getApplicationContext());
	    	application.lastRefresh = null;
	    	getData();
	    	return true;
	    case R.id.avtorji:
	    	myDialog.show();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	public void showSettings()
	{
		Intent settings = new Intent();
        settings.setClass(this, SettingsActivity.class);
		startActivity(settings);
	}
}
