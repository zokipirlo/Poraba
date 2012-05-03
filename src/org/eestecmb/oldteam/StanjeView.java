package org.eestecmb.oldteam;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import si.mobitel.monitor.R;

public class StanjeView extends LinearLayout
{

	TextView stanje;
	TextView airtime;
	TextView disc;

	public StanjeView(Context context)
	{
		super(context);

		this.setOrientation(VERTICAL);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.stanje, this);

		stanje = (TextView) findViewById(R.id.StanjeVrednost);
		airtime = (TextView) findViewById(R.id.AirTimeVrednost);
		disc = (TextView) findViewById(R.id.DiscVrednost);
	}

	public void setStanje(String value)
	{
		stanje.setText(value);
	}

	public void setAirTime(String value)
	{
		airtime.setText(value);
	}

	public void setDiscDate(String value)
	{
		disc.setText(value);
	}
}
