package si.mobitel.monitor;

import java.util.ArrayList;
import java.util.Date;

import android.util.Log;

public class MonitorResult {
	public boolean prePaid = false;
	
	public Date airTimeDate;
	public Date discDate;
	
	public double stanje = 0.0;
	public double monetaLocenRacun = 0.0;
	public double moneta = 0.0;
	public double porabaLocenRacun = 0.0;
	public double poraba = 0.0;
	public boolean hasAdditionalAccount = false;
	
	public ArrayList<Quota> quotas;
	
	public MonitorResult() {
		Log.w("MONITOR", "Monitor Result created!");
	}
	
	
}

/* 
<MonitorInfo>
	<PPStatus>
		<AirTimeDate>0001-01-01T00:00:00</AirTimeDate>
		<DiscDate>0001-01-01T00:00:00</DiscDate>
	</PPStatus>
	<userType>Postpaid</userType>
	<Quotas>
		<Quota>
			<ShortDescription>Klici druga mob. </ShortDescription>
			<Description>Klici v druga mobilna 100 min*</Description>
			<Unit>min</Unit>
			<Available>54</Available>
			<Used>46</Used>
		</Quota>
		<Quota>
			<ShortDescription>Klici druga mob. </ShortDescription>
			<Daescription>Klici v druga mobilna 100 min</Description>
			<Unit>min</Unit>
			<Available>70</Available>
			<Used>30</Used>
		</Quota>
	</Quotas>
	<Stanje>-1</Stanje>
	<MonetaLocenRacun>11.61</MonetaLocenRacun>
	<Moneta>0.00</Moneta>
	<PorabaLocenRacun>1.66</PorabaLocenRacun>
	<Poraba>10.03</Poraba>
	<HasAdditionalAccount>true</HasAdditionalAccount>
</MonitorInfo>

<MonitorInfo>
	<PPStatus>
		<AirTimeDate>0001-01-01T00:00:00</AirTimeDate>
		<DiscDate>0001-01-01T00:00:00</DiscDate>
	</PPStatus>
	<userType>Prepaid</userType>
	<Stanje>2.12</Stanje>
</MonitorInfo>
*/