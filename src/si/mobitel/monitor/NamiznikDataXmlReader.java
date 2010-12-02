package si.mobitel.monitor;

import java.io.CharArrayReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class NamiznikDataXmlReader extends  DefaultHandler{

	private boolean isQuotas = false;
	
	private boolean isQuota = false;

	private boolean isShortDescription = false;
	private boolean isDescription = false;
	private boolean isUnit = false;
	private boolean isAvailable = false;
	private boolean isUsed = false;
	
	private boolean isStanje = false;
	private boolean isMonetaLocenRacun = false;
	private boolean isMoneta = false;
	private boolean isPorabaLocenRacun = false;
	private boolean isPoraba = false;
	private boolean isHasAdditionalAccount = false;
	
	private boolean isMonitorResult = false;
	private boolean isPPStatus = false;
	private boolean isAirTimeDate = false;
	private boolean isDiscDate = false;
	private boolean isUserType = false;
	
	private boolean isFaultString = false;
	
	private MonitorResult monitorResult;
	
	public void startElement(String uri, String name, String qName, Attributes atts) {
		if (name.trim().equals("MonitorResult")) {
			isMonitorResult = true;
			return;
		} else if (name.trim().equals("PPStatus")) {
			isPPStatus = true;
			return;
		} else if (name.trim().equals("AirTimeDate")) {
			isAirTimeDate = true;
			return;
		} else if (name.trim().equals("DiscDate")) {
			isDiscDate = true;
			return;
		} else if (name.trim().equals("userType")) {
			isUserType = true;
			return;
		} else if (name.trim().equals("Quotas")) {
			isQuotas = true;
			return;
		} else if (name.trim().equals("Quota")) {
			isQuota = true;
			return;
		} else if (name.trim().equals("ShortDescription")) {
			isShortDescription = true;
			return;
		} else if (name.trim().equals("Description")) {
			isDescription = true;
			return;
		} else if (name.trim().equals("Unit")) {
			isUnit = true;
			return;
		} else if (name.trim().equals("Available")) {
			isAvailable = true;
			return;
		} else if (name.trim().equals("Used")) {
			isUsed = true;
			return;
		} else if (name.trim().equals("Stanje")) {
			isStanje = true;
			return;
		} else if (name.trim().equals("MonetaLocenRacun")) {
			isMonetaLocenRacun = true;
			return;
		} else if (name.trim().equals("Moneta")) {
			isMoneta = true;
			return;
		} else if (name.trim().equals("PorabaLocenRacun")) {
			isPorabaLocenRacun = true;
			return;
		} else if (name.trim().equals("Poraba")) {
			isPoraba = true;
			return;	
		} else if (name.trim().equals("HasAdditionalAccount")) {
			isHasAdditionalAccount = true;
			return;
		} else if (name.trim().equalsIgnoreCase("faultstring")) {
			isFaultString = true;
		}
	}
	
	
	public void endElement(String uri, String name, String qName) throws SAXException {
		if (name.trim().equals("MonitorResult")) {
			isMonitorResult = true;
			return;
		} else if (name.trim().equals("PPStatus")) {
			isPPStatus = false;
			return;
		} else if (name.trim().equals("AirTimeDate")) {
			isAirTimeDate = false;
			return;
		} else if (name.trim().equals("DiscDate")) {
			isDiscDate = false;
			return;
		} else if (name.trim().equals("userType")) {
			isUserType = false;
			return;
		} else if (name.trim().equals("Quotas")) {
			isQuotas = false;
			return;
		} else if (name.trim().equals("Quota")) {
			isQuota = false;
			return;
		} else if (name.trim().equals("ShortDescription")) {
			isShortDescription = false;
			return;
		} else if (name.trim().equals("Description")) {
			isDescription = false;
			return;
		} else if (name.trim().equals("Unit")) {
			isUnit = false;
			return;
		} else if (name.trim().equals("Available")) {
			isAvailable = false;
			return;
		} else if (name.trim().equals("Used")) {
			isUsed = false;
			return;
		} else if (name.trim().equals("Stanje")) {
			isStanje = false;
			return;
		} else if (name.trim().equals("MonetaLocenRacun")) {
			isMonetaLocenRacun = false;
			return;
		} else if (name.trim().equals("Moneta")) {
			isMoneta = false;
			return;
		} else if (name.trim().equals("PorabaLocenRacun")) {
			isPorabaLocenRacun = false;
			return;
		} else if (name.trim().equals("Poraba")) {
			isPoraba = false;
			return;	
		} else if (name.trim().equals("HasAdditionalAccount")) {
			isHasAdditionalAccount = false;
			return;
		} else if (name.trim().equalsIgnoreCase("faultstring")) {
			isFaultString = true;
		}
	}
	
	private String tmpShortDescription = "";
	private String tmpDescription = "";
	private String tmpUnit = "";
	private double tmpAvailable = 0.0;
	private double tmpUsed = 0.0;
	
	private Boolean wrongUserPass = false;
	
	public void characters(char ch[], int start, int length) { 
		String value = String.valueOf(ch).substring(start, (start + length));
		//Log.w("MONITOR", value);
		if (isFaultString) {
			if (value.contains("Authentication failed.")) {
				wrongUserPass = true;
			}
		}
		if (isMonitorResult && isQuotas) {
			//quote
			if (isQuota) {
				//shrani lokalno
				if (isShortDescription) tmpShortDescription = value;
				else if (isDescription) tmpDescription = value;
				else if (isUnit) tmpUnit = value;
				else if (isAvailable) tmpAvailable = Double.parseDouble(value);
				else if (isUsed) {
					tmpUsed = Double.parseDouble(value);
					
					Quota tmpQuote = new Quota(tmpShortDescription, tmpDescription, tmpUnit, tmpAvailable, tmpUsed);
					if (monitorResult.quotas == null) {
						monitorResult.quotas = new ArrayList<Quota>();
					}
					monitorResult.quotas.add(tmpQuote);
					tmpAvailable = 0.0;
					tmpDescription = "";
					tmpShortDescription = "";
					tmpUnit = "";
					tmpUsed = 0.0;
				}
				
			} 
		} else if (isMonitorResult && isPPStatus){
			if (isAirTimeDate) {
				SimpleDateFormat tmp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				try {
					monitorResult.airTimeDate = tmp.parse(value);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (isDiscDate) {
				SimpleDateFormat tmp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
				try {
					monitorResult.discDate = tmp.parse(value);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else if (isMonitorResult && !isQuotas && !isPPStatus) {
			//stanje
			/*<Stanje>0</Stanje>
				<MonetaLocenRacun>0</MonetaLocenRacun>
				<Moneta>0.00</Moneta>
				<PorabaLocenRacun>0</PorabaLocenRacun>
				<Poraba>0.42</Poraba>
				<HasAdditionalAccount>false</HasAdditionalAccount>*/
			if (isUserType) {
				if (value.equalsIgnoreCase("postpaid")) monitorResult.prePaid = false;
				else monitorResult.prePaid = true;
			}
			else if (isStanje) monitorResult.stanje = Double.parseDouble(value);
			else if (isStanje) monitorResult.stanje = Double.parseDouble(value);
			else if (isStanje) monitorResult.stanje = Double.parseDouble(value);
			else if (isStanje) monitorResult.stanje = Double.parseDouble(value);
			else if (isStanje) monitorResult.stanje = Double.parseDouble(value);
			else if (isMonetaLocenRacun) monitorResult.monetaLocenRacun = Double.parseDouble(value);
			else if (isMoneta) monitorResult.moneta = Double.parseDouble(value);
			else if (isPorabaLocenRacun) monitorResult.porabaLocenRacun = Double.parseDouble(value);
			else if (isPoraba) monitorResult.poraba = Double.parseDouble(value);
			else if (isHasAdditionalAccount) monitorResult.hasAdditionalAccount = Boolean.parseBoolean(value);
		}
	}
	
	
	public MonitorResult parseXML(String xml) {
        try {
        	monitorResult = new MonitorResult();
        	SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            xr.setContentHandler(this);
            CharArrayReader carr = new CharArrayReader(xml.toCharArray());
            xr.parse(new InputSource(carr));  
            if (wrongUserPass) return null;
            return monitorResult;
        } catch (IOException e) {
                Log.e("ShowDataXMLParser", e.toString());
                return null;
        } catch (SAXException e) {
                Log.e("ShowDataXMLParser", e.toString());
                return null;
        } catch (ParserConfigurationException e) {
                Log.e("ShowDataXMLParser", e.toString());
                return null;
        }
	}

}
