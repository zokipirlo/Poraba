package org.eestecmb.oldteam;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import si.mobitel.monitor.R;

public class PorabaView extends LinearLayout
{

	TextView text;
	TextView status;

	public PorabaView(Context context)
	{
		super(context);

		this.setOrientation(VERTICAL);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.poraba, this);

		text = (TextView) findViewById(R.id.PorabaTitle);
		status = (TextView) findViewById(R.id.PorabaStanje);
	}

	public void setTitle(String value)
	{
		text.setText(value);
	}

	public void setStanje(Double value)
	{
		String newValue = "0";
		if ((Math.floor(value) - value) == 0)
			newValue = String.valueOf(Math.round(value));
		else
			newValue = value.toString().replace(".", ",");

		status.setText(newValue + " €");
	}
}
