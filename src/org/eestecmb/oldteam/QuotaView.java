package org.eestecmb.oldteam;

import java.util.GregorianCalendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import si.mobitel.monitor.R;

public class QuotaView extends LinearLayout
{

	TextView text;
	TextView status;
	TextView progress;
	SeekBar bar;
	Quota quota;
	Animation animation;
	double remainingDays;

	public QuotaView(Context context)
	{
		super(context);

		this.setOrientation(VERTICAL);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.quota, this);

		text = (TextView) findViewById(R.id.QuotaText);
		progress = (TextView) findViewById(R.id.QuotaProgress);
		status = (TextView) findViewById(R.id.QuotaStatus);
		bar = (SeekBar) findViewById(R.id.QuotaSeek);
		animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
		animation.setDuration(2000);
		remainingDays = GregorianCalendar.getInstance().getActualMaximum(GregorianCalendar.DAY_OF_MONTH) - GregorianCalendar.getInstance().get(GregorianCalendar.DAY_OF_MONTH) + 1;
	}

	public void setQuota(Quota q)
	{
		quota = q;
		setQuotaText(q.description);
		String remaining = "";
		if (q.used <= q.bought())
		{
			long remain = Math.round((q.bought() - q.used) / remainingDays);
			remaining = "Porabite lahko \u2248" + remain + " " + q.unit + " na dan";
		}
		setQuotaProgress(Math.round(q.used) + " / " + Math.round(q.bought()));
		setQuotaStatus(remaining);
		setQuotaSeek(q.used, q.bought());
	}

	public void setQuotaText(String value)
	{
		text.setText(value);
	}

	public void setQuotaProgress(String value)
	{
		progress.setText(value);
	}
	
	public void setQuotaStatus(String value)
	{
		status.setText(value);
	}

	public void setQuotaSeek(double value, double max)
	{
		final int used = (int) (value);
		bar.setMax((int) max);
		bar.setProgress(used);
		bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener()
		{
			// @Override
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				seekBar.setProgress(used);

			}

			// @Override
			public void onStartTrackingTouch(SeekBar seekBar)
			{

			}

			// @Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
			{
				seekBar.setProgress(used);

			}
		});
		bar.startAnimation(animation);
	}
}
