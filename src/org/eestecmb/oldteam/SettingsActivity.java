package org.eestecmb.oldteam;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.eestecmb.oldteam.settings.SettingsHelper;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SettingsActivity extends Activity
{
	EditText telefonska;
	EditText passEdit;
	CheckBox mnamiznik;
	LinearLayout quotaslayout;
	RequestData rd;
	Boolean created = false;
	MobitelMonitor application;
	ProgressDialog requestDialog;
	Button save;
	String username;

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.settings);

		if (telefonska == null)
			telefonska = (EditText) findViewById(R.id.telStSettings);
		if (passEdit == null)
			passEdit = (EditText) findViewById(R.id.gesloSettings);
		if (quotaslayout == null)
			quotaslayout = (LinearLayout) findViewById(R.id.QuotasSettingsLayout);
		if (mnamiznik == null)
		{
			mnamiznik = (CheckBox) findViewById(R.id.mnamiznikMobile);
			mnamiznik.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					SettingsHelper.getInstance(SettingsActivity.this).setMNamiznik(username, isChecked);
				}
			});
		}

		username = SettingsHelper.getInstance(this).getLastPhoneNumber();
		if (username.length() == 0)
			username = SettingsHelper.getInstance(this).getMyPhoneNumber();
		telefonska.setText(username);
		passEdit.setText(SettingsHelper.getInstance(this).getPassword(username));
		
		mnamiznik.setChecked(SettingsHelper.getInstance(this).useMNamiznik(username));
		
		if (passEdit.getText() == null || passEdit.getText().length() == 0)
			passEdit.requestFocus();

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
			String enteredNumber = telefonska.getText().toString();
			if (!username.equalsIgnoreCase(enteredNumber))
			{
				SettingsHelper.getInstance(SettingsActivity.this).setLastNumber(enteredNumber);
				enako = false;
			}
			if (!passEdit.getText().toString().equalsIgnoreCase(SettingsHelper.getInstance(SettingsActivity.this).getPassword(enteredNumber)))
			{
				enako = false;
			}
			SettingsHelper.getInstance(SettingsActivity.this).setPassword(enteredNumber, passEdit.getText().toString());
			if (!enako)
			{
				refreshQuotas();
			}
			else
			{
				finish();
			}
		}
	};

	private void refreshQuotas()
	{
		if (rd != null)
			rd.cancel(true);
		rd = new RequestData(this);
		username = telefonska.getText().toString();
		rd.execute(username);
	}

	public void showResult(MonitorResult result)
	{
		if (result == null || result.quotas == null)
		{
			Toast.makeText(this, "Napaka, pri pridobivanju podatkov!", Toast.LENGTH_LONG).show();
			return;
		}

		if (application == null)
			application = ((MobitelMonitor) getApplicationContext());

		application.lastPhoneNum = username;
		application.monitorResult = result;
		application.lastRefresh = new Date().getTime();
		SettingsHelper.getInstance(this).setLastRefresh(application.lastPhoneNum, application.lastRefresh);

		// nalozi obstojece kvote v Map
		JSONObject json = SettingsHelper.getInstance(this).getQuotas(username);
		if (json == null)
			json = new JSONObject();
		HashMap<String, Boolean> map = new HashMap<String, Boolean>();
		@SuppressWarnings("unchecked")
		Iterator<String> iter = json.keys();
		while (iter.hasNext())
		{
			String key = iter.next();
			Boolean value = json.optBoolean(key, true);
			map.put(key, value);
		}

		// preveri ce je kaka nova in dodaj v seznam kvot
		for (Quota qu : result.quotas)
		{
			if (!map.containsKey(qu.description))
			{
				try
				{
					json.put(qu.description, true);
				}
				catch (JSONException e)
				{
					e.printStackTrace();
				}
			}
		}

		SettingsHelper.getInstance(this).setQuotas(username, json);
		loadQuotas();
		//finish();
	}

	public void resultError(String description)
	{
		if (description.equals("geslo"))
		{
			Toast.makeText(this, "Za uporabo na brez¾iènem omre¾ju potrebuje geslo", Toast.LENGTH_LONG).show();
		}
		else if (description.equals("povezava"))
		{
			Toast.makeText(this, "Preverite internetno povezavo in poskusite ponovno", Toast.LENGTH_LONG).show();
		}
		else if (description.equals("mnamiznikgeslo"))
		{
			Toast.makeText(this, "Za uporabo M:Namiznika na mobilnem omre¾ju je potrebno geslo.", Toast.LENGTH_LONG).show();
		}
		else if (description.equals("mnamiznikmobile"))
		{
			Toast.makeText(this, "Za uporabo M:Namiznika je potrebna odobritev. Levo od gumba 'Shrani' ga lahko omogoèite.\nPritisnite tipko 'Menu' za veè informacij.", Toast.LENGTH_LONG).show();
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

	private void loadQuotas()
	{
		quotaslayout.removeAllViews();
		final JSONObject json = SettingsHelper.getInstance(this).getQuotas(username);
		if (json == null)
			return;
		@SuppressWarnings("unchecked")
		Iterator<String> iter = json.keys();
		while (iter.hasNext())
		{
			String key = iter.next();
			CheckBox cb = new CheckBox(this);
			cb.setText(key);
			cb.setTextColor(Color.parseColor("#4c1212"));
			cb.setChecked(json.optBoolean(key, true));
			cb.setButtonDrawable(R.drawable.check);
			cb.setPadding(40, cb.getPaddingTop(), 20, cb.getPaddingBottom());
			cb.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
				{
					String quote = buttonView.getText().toString();
					try
					{
						json.put(quote, isChecked);
						SettingsHelper.getInstance(SettingsActivity.this).setQuotas(username, json);
					}
					catch (JSONException e)
					{
						e.printStackTrace();
					}
				}
			});
			quotaslayout.addView(cb);
		}
	}

	@Override
	protected void onDestroy()
	{
		hideLoader();
		super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.settingsmenu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case R.id.aboutMNamiznik:
				AlertDialog.Builder about = new Builder(this);
				about.setTitle("M:Namiznik");
				about.setMessage("Pri uporabi M:Vrat se podatki, ki se prena¹ajo ne ¹tejejo kot prene¹eni podatki, oz. povedano drugaèe je brezplaèno.\n" +
						"Ker je uporaba M:Vrat brezplaèna se najprej probajo podatki pridobiti od tam, èe gre karkoli narobe pa se poskusi preko M:Namiznika.\n" +
						"Pri uporabi M:Namiznika se pridobljeni podatki ¹tejejo kot podatkovni promet. V primeru da nimate zakupljenih kolièin ali ste jih presegli se vam ti podatki zaraèunajo.\n" +
						"Uporabo M:Namiznika na mobilnem omre¾ju lahko omogoèite v nastavitvah. Na brez¾iènem omre¾ju je v vsakem primeru omogoèena.");
				about.setNeutralButton("OK", null);
				about.show();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}
