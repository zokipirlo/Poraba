package org.eestecmb.oldteam;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TableLayout;
import android.widget.TableRow;
import org.eestecmb.oldteam.R;

public class PorabeView extends TableLayout
{

	Context _context;

	public PorabeView(Context context)
	{
		super(context);

		_context = context;

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.porabe, this);
	}

	public void setPorabe(boolean aditional, double poraba, double moneta, double porabaLocen, double monetaLocen)
	{
		PorabaView pv;
		TableRow tr;

		setColumnStretchable(0, true);
		setColumnStretchable(1, true);

		// osnovni
		tr = new TableRow(_context);

		pv = new PorabaView(_context);
		pv.setTitle("Poraba");
		pv.setStanje(poraba);
		tr.addView(pv);

		pv = new PorabaView(_context);
		pv.setTitle("Moneta");
		pv.setStanje(moneta);
		tr.addView(pv);

		this.addView(tr);

		// loceni racun
		if (aditional)
		{
			tr = new TableRow(_context);

			pv = new PorabaView(_context);
			pv.setTitle("Poraba Loèen");
			pv.setStanje(porabaLocen);
			tr.addView(pv);

			pv = new PorabaView(_context);
			pv.setTitle("Moneta Loèen");
			pv.setStanje(monetaLocen);
			tr.addView(pv);

			this.addView(tr);
		}
	}
}
