package org.eestecmb.oldteam;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import si.mobitel.monitor.R;

public class AboutDialog extends Dialog
{
	public AboutDialog(Context context, int theme)
	{
		super(context, theme);
	}

	public AboutDialog(Context context)
	{
		super(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.avtorji);
		setTitle("avtorji");
		Button buttonOK = (Button) findViewById(R.id.Button01);
		buttonOK.setOnClickListener(new OKListener());
	}

	private class OKListener implements android.view.View.OnClickListener
	{
		@Override
		public void onClick(View arg0)
		{
			// TODO Auto-generated method stub
			AboutDialog.this.dismiss();
		}
	}
}
