package org.eestecmb.oldteam;

import java.io.CharArrayReader;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;


public class NamiznikAuthenticationReader extends DefaultHandler
{

	private boolean isAuthenticationResult = false;

	private boolean isFaultString = false;

	private int status = -1;

	public void startElement(String uri, String name, String qName, Attributes atts)
	{
		if (name.trim().equals("AuthenticateUserResult"))
		{
			isAuthenticationResult = true;
			return;
		}
		else if (name.trim().equalsIgnoreCase("faultstring"))
		{
			isFaultString = true;
		}
	}

	public void endElement(String uri, String name, String qName) throws SAXException
	{
		if (name.trim().equals("AuthenticateUserResult"))
		{
			isAuthenticationResult = true;
			return;
		}
		else if (name.trim().equalsIgnoreCase("faultstring"))
		{
			isFaultString = true;
		}
	}

	public void characters(char ch[], int start, int length)
	{
		String value = String.valueOf(ch).substring(start, (start + length));
		// Log.w("MONITOR", value);
		if (isFaultString)
		{
			if (value.contains("Authentication failed."))
			{
				status = -1;
			}
		}
		if (isAuthenticationResult)
		{
			status = Integer.parseInt(value);
		}
	}

	public int parseXML(String xml)
	{
		try
		{
			SAXParserFactory spf = SAXParserFactory.newInstance();
			SAXParser sp = spf.newSAXParser();
			XMLReader xr = sp.getXMLReader();
			xr.setContentHandler(this);
			CharArrayReader carr = new CharArrayReader(xml.toCharArray());
			xr.parse(new InputSource(carr));
			return status;
		}
		catch (IOException e)
		{
			Log.e("ShowDataXMLParser", e.toString());
			return -1;
		}
		catch (SAXException e)
		{
			Log.e("ShowDataXMLParser", e.toString());
			return -1;
		}
		catch (ParserConfigurationException e)
		{
			Log.e("ShowDataXMLParser", e.toString());
			return -1;
		}
	}

}
