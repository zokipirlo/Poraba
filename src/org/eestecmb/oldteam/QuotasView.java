package org.eestecmb.oldteam;

import java.util.ArrayList;

import org.eestecmb.oldteam.settings.SettingsHelper;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;

public class QuotasView extends TableLayout
{
	Context _context;
	JSONObject savedQuotas;

	public QuotasView(Context context, String phoneNumber)
	{
		super(context);

		_context = context;

		savedQuotas = SettingsHelper.getInstance(_context).getQuotas(phoneNumber);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.quotas, this);

		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		setLayoutParams(params);
	}

	public void setQuotas(ArrayList<Quota> quotas)
	{
		if (quotas == null)
			return;

		int quotasNum = quotas.size();
		Quota q;

		Configuration c = getResources().getConfiguration();

		if (c.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			for (int i = 0; i < quotasNum; i++)
			{
				q = quotas.get(i);
				if (savedQuotas != null && savedQuotas.optBoolean(q.description, true) == false)
					continue;
				QuotaView qv = new QuotaView(_context);
				qv.setQuota(q);

				this.addView(qv);
			}
		}
		else
		{
			ArrayList<Quota> tempList = new ArrayList<Quota>();
			for (int k = 0; k < quotasNum; k++)
			{
				q = quotas.get(k);
				if (savedQuotas == null || savedQuotas.optBoolean(q.description, true) == true)
					tempList.add(quotas.get(k));
			}
			for (int i = 0; i < tempList.size(); i += 2)
			{
				TableRow tr = new TableRow(_context);
				setColumnStretchable(0, true);
				setColumnStretchable(1, true);
				for (int j = i; (j < tempList.size()) && (j < i + 2); j++)
				{
					q = tempList.get(j);
					QuotaView qv = new QuotaView(_context);
					qv.setQuota(q);
					tr.addView(qv);
				}
				this.addView(tr);
			}
		}
	}
}
