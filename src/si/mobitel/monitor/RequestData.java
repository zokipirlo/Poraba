package si.mobitel.monitor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.ParseException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class RequestData {
	Context c;
	
	public RequestData(Context context)
	{
		c = context;
	}
	
	public MonitorResult podatki(String telefonska, String geslo) {
    	MonitorResult monitorResult = null;
    	
    	ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		
		//0 : nepovezan | 1 : wifi | 2 : mobile | -1 : neznano
    	int status = 0;
		if (ni != null && ni.isConnected())
		{
			Log.d("MONITOR", "Connected ...");
			if (ni.getType() == ConnectivityManager.TYPE_WIFI)
			{
				Log.d("MONITOR", "Wifi");
				status = 1;
			}
			else if (ni.getType() == ConnectivityManager.TYPE_MOBILE)
			{
				Log.d("MONITOR", "Mobile");
				status = 2;
			}
			else
			{
				Log.d("MONITOR", "Unknown");
				status = -1;
			}
		}
		
		Log.d("MONITOR", "Povezava status: " + status);
		
		if (status > 0)
		{
			Log.d("MONITOR", "Poskus MVrata");
			monitorResult = tryMVrata(telefonska, geslo, status == 1);
			if (monitorResult == null)
			{
				Log.d("MONITOR", "Poskus MNamiznik");
				monitorResult = tryMNamiznik(telefonska, geslo, status == 1);
			}
		}
		return monitorResult;
        
    }
	
	private MonitorResult tryMVrata(String telefonska, String geslo, boolean wireless) {
		MonitorResult result = null;
		
		DefaultHttpClient httpclient = new DefaultHttpClient(); 
        HttpGet httpget = new HttpGet("https://m-vrata.mobitel.si/android/monitorinfo/infoxml.ashx"); 
        
        if (wireless) {
        	httpclient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(telefonska, geslo));
        	httpclient.addRequestInterceptor(preemptiveAuth, 0);
        }
        
        BasicHttpResponse httpResponse = null;
        try {
        	Log.d("MONITOR", "Requesting data ...");
			httpResponse = (BasicHttpResponse) httpclient.execute(httpget);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			Log.d("MONITOR", " Request ClientProtocolException!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MONITOR", "Request IOException!");
			e.printStackTrace();
			return null;
		}
		
		try {
			Log.d("MONITOR", "Response!");
			String xml = EntityUtils.toString(httpResponse.getEntity());
			Log.d("MONITOR", xml);
			MonitorDataXmlReader xmlReader = new MonitorDataXmlReader();
			result = xmlReader.parseXML(xml);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			Log.d("MONITOR", "Parse error!");
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MONITOR", "IOException!");
			e.printStackTrace();
			return null;
		}
		
		return result;
	}
	
	private MonitorResult tryMNamiznik(String telefonska, String geslo, boolean wireless) {
    	MonitorResult result = null;
    	
    	HttpClient httpclient = new DefaultHttpClient(); 
        HttpPost httppost = new HttpPost("https://moj.mobitel.si/mobidesktop-v2/service"); 

        StringEntity se = null;
        BasicHttpResponse httpResponse = null;
        
        // -1 : nepravilni podatki | 0 : OK
        int status = -1;

    	//authentication
    	try {
			se = new StringEntity("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:mob=\"http://mobitel.si/MobiDesktop\">" +
					"<soapenv:Header/><soapenv:Body><mob:AuthenticateUser><mob:Username>" + telefonska + "</mob:Username><mob:Password>" + geslo + "</mob:Password>" + 
					"</mob:AuthenticateUser></soapenv:Body></soapenv:Envelope>", HTTP.UTF_8);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}

        se.setContentType("text/xml");
        httppost.setHeader("Content-Type","text/xml; charset=utf-8");
        httppost.setHeader("SOAPAction", "http://mobitel.si/MobiDesktop/AuthenticateUser");
        httppost.setEntity(se);
        
        httpResponse = null;
        try {
			httpResponse = (BasicHttpResponse) httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		try {
			String xml = EntityUtils.toString(httpResponse.getEntity());
			Log.w("MONITOR", xml);
			NamiznikAuthenticationReader xmlReader = new NamiznikAuthenticationReader();
			status = xmlReader.parseXML(xml);
			//TODO
			//check: <AuthenticateUserResult>0</AuthenticateUserResult>
			/*
			 <?xml version="1.0" encoding="utf-8"?>
			<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema">
				<soap:Body>
					<AuthenticateUserResponse xmlns="http://mobitel.si/MobiDesktop">
						<AuthenticateUserResult>0</AuthenticateUserResult>
					</AuthenticateUserResponse>
				</soap:Body>
			</soap:Envelope>
			 */
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
    
        
        if (status != 0)
        	return null;
        
        //MonitorResult

        try {
			se = new StringEntity(
					"<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:mob=\"http://mobitel.si/MobiDesktop\">" +
					"<soapenv:Header/><soapenv:Body><mob:Monitor><mob:Username>" + telefonska + "</mob:Username><mob:Password>" + geslo + "</mob:Password>" + 
					"</mob:Monitor></soapenv:Body></soapenv:Envelope>", HTTP.UTF_8);
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}

        se.setContentType("text/xml");
        httppost.setHeader("Content-Type","text/xml; charset=utf-8");
        httppost.setHeader("SOAPAction", "http://mobitel.si/MobiDesktop/Monitor");
        httppost.setEntity(se);
        
        httpResponse = null;
        try {
			httpResponse = (BasicHttpResponse) httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
		try {
			Log.d("MONITOR", "Response!");
			String xml = EntityUtils.toString(httpResponse.getEntity());
			Log.d("MONITOR", xml);
			NamiznikDataXmlReader xmlReader = new NamiznikDataXmlReader();
			result = xmlReader.parseXML(xml);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
       
		return result;
	}
	
	HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor() {
	    public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException {
	        AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
	        CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
	                ClientContext.CREDS_PROVIDER);
	        HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
	        
	        if (authState.getAuthScheme() == null) {
	            AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
	            Credentials creds = credsProvider.getCredentials(authScope);
	            if (creds != null) {
	                authState.setAuthScheme(new BasicScheme());
	                authState.setCredentials(creds);
	            }
	        }
	    }    
	};
	
	public MonitorResult createResult() {
		MonitorResult mr = new MonitorResult();
		
		ArrayList<Quota> al = new ArrayList<Quota>();
		al.add(new Quota("mobitelOmrezje", "Mobitel Omre¾je", "min", 600, 400));
		al.add(new Quota("ostaloOmrezje", "Ostalo Omre¾je", "min", 100, 100));
		al.add(new Quota("smsOmrezje", "SMSi", "number", 900, 100));
		al.add(new Quota("podatkiOmrezje", "Podatki", "number", 0, 100));
		al.add(new Quota("ostaloOmrezje", "Ostalo Omre¾je", "min", 100, 100));
		al.add(new Quota("smsOmrezje", "SMSi", "number", 900, 100));
		al.add(new Quota("podatkiOmrezje", "Podatki", "number", 0, 100));
		
		mr.poraba = 5.0;
		mr.moneta = 2.0;
		mr.porabaLocenRacun = 10;
		mr.monetaLocenRacun = 15;
		mr.prePaid = false;
		mr.quotas = al;
		//mr.hasAdditionalAccount = false;
		mr.hasAdditionalAccount = true;
		mr.porabaLocenRacun = 2.25;
		mr.monetaLocenRacun = 125;
		
		
		/*mr.prePaid = true;
		mr.stanje = 20;
		
		String datum = "2010-05-02T01:02:03";
		SimpleDateFormat tmp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		try {
			mr.airTimeDate = tmp.parse(datum);
			datum = "2010-08-22T16:39:57";
			mr.discDate = tmp.parse(datum);
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
				
		return mr;
	}
}
