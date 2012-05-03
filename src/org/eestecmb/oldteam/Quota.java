package org.eestecmb.oldteam;


public class Quota
{
	public String shortDescription = "";
	public String description = "";
	public String unit = "";
	public double available = 0.0;
	public double used = 0.0;

	public Quota(String tmpShortDescription, String tmpDescription, String tmpUnit, double tmpAvailable, double tmpUsed)
	{
		shortDescription = tmpShortDescription;
		description = tmpDescription;
		unit = tmpUnit;
		available = tmpAvailable;
		used = tmpUsed;
	}

	public double bought()
	{
		return available + used;
	}

	@Override
	public String toString()
	{
		return "Quota [shortDescription=" + shortDescription + ", description=" + description + ", unit=" + unit + ", available=" + available + ", used=" + used + "]";
	}
}

/*
 * <Quota> <ShortDescription>Klici druga mob. </ShortDescription>
 * <Description>Klici v druga mobilna 100 min*</Description> <Unit>min</Unit>
 * <Available>54</Available> <Used>46</Used> </Quota>
 */