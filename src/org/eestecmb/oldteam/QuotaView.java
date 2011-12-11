package org.eestecmb.oldteam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import org.eestecmb.oldteam.R;

public class QuotaView extends LinearLayout
{

	TextView text;
	TextView status;
	SeekBar bar;
	Quota quota;
	Animation animation;

	public QuotaView(Context context)
	{
		super(context);

		this.setOrientation(VERTICAL);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.quota, this);

		text = (TextView) findViewById(R.id.QuotaText);
		status = (TextView) findViewById(R.id.QuotaStatus);
		bar = (SeekBar) findViewById(R.id.QuotaSeek);
		animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
		animation.setDuration(2000);
	}

	public void setQuota(Quota q)
	{
		quota = q;
		setQuotaText(q.description);
		setQuotaStatus(Math.round(q.used) + " / " + Math.round(q.bought()));
		setQuotaSeek(q.used, q.bought());
	}

	public void setQuotaText(String value)
	{
		text.setText(value);
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
		bar.setThumb(null);
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
				// TODO Auto-generated method stub

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
