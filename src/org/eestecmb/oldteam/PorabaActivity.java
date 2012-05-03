package org.eestecmb.oldteam;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eestecmb.oldteam.settings.SettingsHelper;
import si.mobitel.monitor.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class PorabaActivity extends Activity
{
	private AdView adView = null;
	RequestData rd;
	MonitorResult result;
	Quota q;
	AboutDialog myDialog;
	MobitelMonitor application;
	LinearLayout paketLL;
	TextView naslov;
	ProgressDialog requestDialog;
	RelativeLayout holder;
	ScrollView scroll;
	FrameLayout adHolder;
	boolean isLoading = false;

	// Se poklice, ko se ustvari ta Activity (samo enkrat)
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		isLoading = true;
		myDialog = new AboutDialog(this);
		if (holder == null)
		{
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			holder = (RelativeLayout) inflater.inflate(R.layout.paket, null);
		}

		setContentView(holder);

		if (adHolder == null)
			adHolder = (FrameLayout) findViewById(R.id.AdHolder);
		if (scroll == null)
			scroll = (ScrollView) findViewById(R.id.PaketScroll);
		if (paketLL == null)
			paketLL = (LinearLayout) findViewById(R.id.PaketLayout);
		if (naslov == null)
			naslov = (TextView) findViewById(R.id.TitleText);
		naslov.setText("PORABA");
	}
	
	// Se poklice, ko se prikaze ta Activity
	@Override
	protected void onResume()
	{
		super.onResume();
		showAd();
		getData();
	}

	private MobitelMonitor getApp()
	{
		if (application == null)
			application = (MobitelMonitor)getApplicationContext();
		return application;
	}

	private void getData()
	{
		isLoading = true;
		naslov.setText("PRIDOBIVAM PODATKE...");
		paketLL.removeAllViews();
		paketLL.addView(naslov);

		// Pridobi telefonsko iz nastavitev. če se razlikuje od tiste v getApp() je uporabnik spremenil številko v nastavitvah
		// če je prazen String potem pridobi telfonsko iz telefona.
		String username = SettingsHelper.getInstance(this).getLastPhoneNumber();
		if (username.length() == 0)
			username = SettingsHelper.getInstance(this).getMyPhoneNumber();
		
		if (getApp().lastPhoneNum.length() == 0)
		{
			getApp().lastPhoneNum = username;
		}
		
		long nowMilis = new Date().getTime();
		long lastMilis = SettingsHelper.getInstance(this).getLastRefresh(username);
		lastMilis += 600000;
		
		MonitorResult lastResult = SettingsHelper.getInstance(this).getLastResult(username);
		if (!username.equalsIgnoreCase(getApp().lastPhoneNum) || lastMilis < nowMilis || lastResult == null)
		{
			if (rd != null)
				rd.cancel(true);
			rd = new RequestData(this);
			rd.execute(username);
			return;
		}
		else
		{
			getApp().monitorResult = lastResult;
			showResult(getApp().monitorResult);
		}
	}

	public void showResult(MonitorResult result)
	{
		isLoading = false;
		paketLL.removeAllViews();
		paketLL.addView(naslov);
		if (result == null)
		{
			naslov.setText("NAPAKA! POSKUSITE PONOVNO KASNEJE");
		}
		else
		{
			naslov.setText("PORABA");
			getApp().lastPhoneNum = SettingsHelper.getInstance(this).getLastPhoneNumber();
			getApp().monitorResult = result;
			getApp().lastRefresh = new Date().getTime();
			SettingsHelper.getInstance(this).setLastRefresh(getApp().lastPhoneNum, getApp().lastRefresh);

			if (result.prePaid)
			{
				StanjeView sv = new StanjeView(this);
				SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
				sv.setStanje(Double.toString(result.stanje));
				sv.setAirTime(sdf.format(result.airTimeDate));
				sv.setDiscDate(sdf.format(result.discDate));

				paketLL.addView(sv);
			}
			else
			{
				PorabeView pv = new PorabeView(this);
				pv.setPorabe(result.hasAdditionalAccount, result.poraba, result.moneta, result.porabaLocenRacun, result.monetaLocenRacun);

				paketLL.addView(pv);

				QuotasView qvs = new QuotasView(this, getApp().lastPhoneNum);
				qvs.setQuotas(result.quotas);

				paketLL.addView(qvs);
			}
		}
	}
	
	private void showAd()
	{
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		// 0 : nepovezan | 1 : wifi | 2 : mobile | -1 : neznano
		int status = 0;
		if (ni != null && ni.isConnected())
		{
			Log.d("MONITOR", "Connected ...");
			if (ni.getType() == ConnectivityManager.TYPE_WIFI)
			{
				Log.d("MONITOR", "Wifi");
				status = 1;
			}
			else if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
			{
				Log.d("MONITOR", "Mobile");
				status = 2;
			}
			else
			{
				Log.d("MONITOR", "Unknown");
				status = -1;
			}
		}
		
		//show ad only on wi-fi
		if (status == 1)
		{
			// Create the adView
			if (adView == null)
			{
				adView = new AdView(this, AdSize.BANNER, "a14f05753e8ea7f");
				// Initiate a generic request to load it with an ad
				adView.loadAd(new AdRequest());
			}

		    // Add the adView to it
			if (adHolder.indexOfChild(adView) == -1)
			{
			    adHolder.addView(adView);
			}
			adHolder.setVisibility(View.VISIBLE);
		}
		else
		{
			adView = null;
			adHolder.setVisibility(View.GONE);
		}
	}
	
	public void resultError(String description)
	{
		if (description.equals("geslo"))
		{
			AlertDialog.Builder dialog = new Builder(this);
			dialog.setTitle("Poraba");
			dialog.setMessage("Potrebno je geslo iz portala Planet. Ali ga želite vnesti sedaj?");
			dialog.setPositiveButton("Želim", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					showSettings();
				}
			});
			dialog.setNegativeButton("Ne želim", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Toast.makeText(PorabaActivity.this, "Za uporabo na brezžičnem omrežju je potrebno geslo. Vnesite ga v nastavitvah in poskusite ponovno.", Toast.LENGTH_LONG).show();
				}
			});
			dialog.show();
		}
		else if (description.equals("povezava"))
		{
			Toast.makeText(this, "Preverite internetno povezavo in poskusite ponovno", Toast.LENGTH_LONG).show();
		}
		else if (description.equals("mnamiznikgeslo"))
		{
			AlertDialog.Builder dialog = new Builder(this);
			dialog.setTitle("Poraba");
			dialog.setMessage("Za uporabo M:Namiznika je potrebno geslo iz portala Planet. Ali ga želite vnesti sedaj?");
			dialog.setPositiveButton("Želim", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					showSettings();
				}
			});
			dialog.setNegativeButton("Ne želim", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Toast.makeText(PorabaActivity.this, "Za uporabo M:Namiznika je potrebno geslo. Vnesite ga v nastavitvah in poskusite ponovno.", Toast.LENGTH_LONG).show();
				}
			});
			dialog.show();
		}
		else if (description.equals("mnamiznikmobile"))
		{
			AlertDialog.Builder dialog = new Builder(this);
			dialog.setTitle("Poraba");
			dialog.setMessage("M:Vrata se ne odzivajo. Uporabo M:Namiznika morate potrditi v nastavitvah.\nŽelite storiti to sedaj?");
			dialog.setPositiveButton("Želim", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					showSettings();
				}
			});
			dialog.setNegativeButton("Ne želim", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					Toast.makeText(PorabaActivity.this, "Za uporabo M:Namiznika je potrebna odobritev. Omogočite lahko v nastavitvah.", Toast.LENGTH_LONG).show();
				}
			});
			dialog.setNeutralButton("Več o tem", new OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					AlertDialog.Builder about = new Builder(PorabaActivity.this);
					about.setTitle("M:Namiznik");
					about.setMessage("Pri uporabi M:Vrat se podatki, ki se prenašajo ne štejejo kot prenešeni podatki, oz. povedano drugače, je brezplačno.\n" +
							"Ker je uporaba M:Vrat brezplačna se najprej probajo podatki pridobiti od tam, če gre karkoli narobe pa se poskusi preko M:Namiznika.\n" +
							"Pri uporabi M:Namiznika se pridobljeni podatki štejejo kot podatkovni promet. V primeru da nimate zakupljenih količin ali ste jih presegli se vam ti podatki zaračunajo.\n" +
							"Uporabo M:Namiznika na mobilnem omrežju lahko omogočite v nastavitvah. Na brezžičnem omrežju je v vsakem primeru omogočena.");
					about.setNeutralButton("OK", null);
					about.show();
				}
			});
			dialog.show();
		}
	}

	public void showLoader()
	{
		if (requestDialog == null || !requestDialog.isShowing())
			requestDialog = ProgressDialog.show(this, "", "Čakam na podatke. Prosimo počakajte...", true);
	}

	public void hideLoader()
	{
		if (requestDialog != null && requestDialog.isShowing())
			requestDialog.dismiss();
	}

	@Override
	protected void onDestroy()
	{
		if (adView != null)
			adView.destroy();
		hideLoader();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle item selection
		switch (item.getItemId())
		{
			case R.id.nastavitve:
				showSettings();
				return true;
			case R.id.exit:
				finish();
				return true;
			case R.id.refresh:
				getApp().lastRefresh = -1;
				SettingsHelper.getInstance(this).setLastRefresh(getApp().lastPhoneNum, -1);
				getData();
				return true;
			case R.id.avtorji:
				myDialog.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		if (!isLoading)
			showResult(getApp().monitorResult);
	}

	public void showSettings()
	{
		Intent settings = new Intent();
		settings.setClass(this, SettingsActivity.class);
		startActivity(settings);
	}
}
