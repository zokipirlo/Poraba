package org.eestecmb.oldteam;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;

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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.eestecmb.oldteam.settings.SettingsHelper;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

public class RequestData extends AsyncTask<String, String, MonitorResult>
{
	Context c;

	public RequestData(Context context)
	{
		c = context;
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		if (c instanceof PorabaActivity)
		{
			((PorabaActivity) c).showLoader();
		}
		else if (c instanceof SettingsActivity)
		{
			((SettingsActivity) c).showLoader();
		}
	}

	@Override
	protected MonitorResult doInBackground(String... params)
	{
		String telefonska = params[0];

		MonitorResult monitorResult = null;

		ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();

		// 0 : nepovezan | 1 : wifi | 2 : mobile | -1 : neznano
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

		Log.d("MONITOR", "Status povezave: " + status);

		if (status > 0)
		{
			String geslo = "";
			if (status == 1)
			{
				geslo = SettingsHelper.getInstance(c).getPassword(telefonska);
				if (geslo.length() == 0)
				{
					publishProgress("geslo");
					return null;
				}
			}
			Log.d("MONITOR", "Poskus MVrata");
			monitorResult = tryMVrata(telefonska, geslo, status == 1);
			if (monitorResult == null)
			{
				if (status == 2)
				{
					geslo = SettingsHelper.getInstance(c).getPassword(telefonska);
					if (geslo.length() == 0)
					{
						publishProgress("mnamiznikgeslo");
						return null;
					}
					if (!SettingsHelper.getInstance(c).useMNamiznik(telefonska))
					{
						publishProgress("mnamiznikmobile");
						return null;
					}
				}
				Log.d("MONITOR", "Poskus MNamiznik");
				monitorResult = tryMNamiznik(telefonska, geslo);
			}
		}
		else
		{
			publishProgress("povezava");
		}

		return monitorResult;
	}
	
	//obve¹èa o napakah
	@Override
	protected void onProgressUpdate(String... values)
	{
		super.onProgressUpdate(values);
		if (c instanceof PorabaActivity)
		{
			((PorabaActivity) c).resultError(values[0]);
		}
		else if (c instanceof SettingsActivity)
		{
			((SettingsActivity) c).resultError(values[0]);
		}
	}

	@Override
	protected void onPostExecute(MonitorResult result)
	{
		if (c instanceof PorabaActivity)
		{
			((PorabaActivity) c).hideLoader();
			((PorabaActivity) c).showResult(result);
		}
		else if (c instanceof SettingsActivity)
		{
			((SettingsActivity) c).hideLoader();
			((SettingsActivity) c).showResult(result);
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled()
	{
		/*if (c instanceof PorabaActivity)
		{
			((PorabaActivity) c).hideLoader();
			((PorabaActivity) c).showResult(null);
		}
		else if (c instanceof SettingsActivity)
		{
			((SettingsActivity) c).hideLoader();
			((SettingsActivity) c).showResult(null);
		}*/
		super.onCancelled();
	}

	private MonitorResult tryMVrata(String telefonska, String geslo, boolean wireless)
	{
		MonitorResult result = null;

		HttpGet httpGet = new HttpGet("https://m-vrata.mobitel.si/android/monitorinfo/infoxml.ashx");
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		HttpConnectionParams.setSoTimeout(httpParameters, 5000);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		
		if (wireless)
		{
			httpClient.getCredentialsProvider().setCredentials(new AuthScope(null, -1), new UsernamePasswordCredentials(telefonska, geslo));
			httpClient.addRequestInterceptor(preemptiveAuth, 0);
		}

		BasicHttpResponse httpResponse = null;
		try
		{
			Log.d("MONITOR", "Requesting data ...");
			httpResponse = (BasicHttpResponse) httpClient.execute(httpGet);
		}
		catch (SocketTimeoutException e)
		{
			Log.d("MONITOR", "Timeout exception!");
			e.printStackTrace();
			return null;
		}
		catch (ClientProtocolException e)
		{
			Log.d("MONITOR", "Request ClientProtocolException!");
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			Log.d("MONITOR", "Request IOException!");
			e.printStackTrace();
			return null;
		}

		try
		{
			Log.d("MONITOR", "Response!");
			String xml = EntityUtils.toString(httpResponse.getEntity());
			Log.d("MONITOR", xml);
			MonitorDataXmlReader xmlReader = new MonitorDataXmlReader();
			result = xmlReader.parseXML(xml);
			if (result != null);
				SettingsHelper.getInstance(c).setLastResult(telefonska, xml);
		}
		catch (ParseException e)
		{
			Log.d("MONITOR", "Parse error!");
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			Log.d("MONITOR", "IOException!");
			e.printStackTrace();
			return null;
		}

		return result;
	}

	private MonitorResult tryMNamiznik(String telefonska, String geslo)
	{
		MonitorResult result = null;

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		HttpConnectionParams.setSoTimeout(httpParameters, 5000);
		DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
		HttpPost httppost = new HttpPost("https://moj.mobitel.si/mobidesktop-v2/service");

		StringEntity se = null;
		BasicHttpResponse httpResponse = null;

		// -1 : nepravilni podatki | 0 : OK
		int status = -1;

		// authentication
		try
		{
			se = new StringEntity("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:mob=\"http://mobitel.si/MobiDesktop\">" + "<soapenv:Header/><soapenv:Body><mob:AuthenticateUser><mob:Username>" + telefonska + "</mob:Username><mob:Password>" + geslo + "</mob:Password>" + "</mob:AuthenticateUser></soapenv:Body></soapenv:Envelope>", HTTP.UTF_8);
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
			return null;
		}

		se.setContentType("text/xml");
		httppost.setHeader("Content-Type", "text/xml; charset=utf-8");
		httppost.setHeader("SOAPAction", "http://mobitel.si/MobiDesktop/AuthenticateUser");
		httppost.setEntity(se);

		httpResponse = null;
		try
		{
			httpResponse = (BasicHttpResponse) httpClient.execute(httppost);
		}
		catch (SocketTimeoutException e)
		{
			Log.d("MONITOR", "Timeout exception!");
			e.printStackTrace();
			return null;
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

		try
		{
			String xml = EntityUtils.toString(httpResponse.getEntity());
			Log.w("MONITOR", xml);
			NamiznikAuthenticationReader xmlReader = new NamiznikAuthenticationReader();
			status = xmlReader.parseXML(xml);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

		if (status != 0)
			return null;

		// MonitorResult
		try
		{
			se = new StringEntity("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:mob=\"http://mobitel.si/MobiDesktop\">" + "<soapenv:Header/><soapenv:Body><mob:Monitor><mob:Username>" + telefonska + "</mob:Username><mob:Password>" + geslo + "</mob:Password>" + "</mob:Monitor></soapenv:Body></soapenv:Envelope>", HTTP.UTF_8);
		}
		catch (UnsupportedEncodingException e1)
		{
			e1.printStackTrace();
			return null;
		}

		se.setContentType("text/xml");
		httppost.setHeader("Content-Type", "text/xml; charset=utf-8");
		httppost.setHeader("SOAPAction", "http://mobitel.si/MobiDesktop/Monitor");
		httppost.setEntity(se);

		httpResponse = null;
		try
		{
			httpResponse = (BasicHttpResponse) httpClient.execute(httppost);
		}
		catch (SocketTimeoutException e)
		{
			Log.d("MONITOR", "Timeout exception!");
			e.printStackTrace();
			return null;
		}
		catch (ClientProtocolException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

		try
		{
			Log.d("MONITOR", "Response!");
			String xml = EntityUtils.toString(httpResponse.getEntity());
			Log.d("MONITOR", xml);
			NamiznikDataXmlReader xmlReader = new NamiznikDataXmlReader();
			result = xmlReader.parseXML(xml);
			if (result != null);
				SettingsHelper.getInstance(c).setLastResult(telefonska, xml);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			return null;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return null;
		}

		return result;
	}

	HttpRequestInterceptor preemptiveAuth = new HttpRequestInterceptor()
	{
		public void process(final HttpRequest request, final HttpContext context) throws HttpException, IOException
		{
			AuthState authState = (AuthState) context.getAttribute(ClientContext.TARGET_AUTH_STATE);
			CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(ClientContext.CREDS_PROVIDER);
			HttpHost targetHost = (HttpHost) context.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

			if (authState.getAuthScheme() == null)
			{
				AuthScope authScope = new AuthScope(targetHost.getHostName(), targetHost.getPort());
				Credentials creds = credsProvider.getCredentials(authScope);
				if (creds != null)
				{
					authState.setAuthScheme(new BasicScheme());
					authState.setCredentials(creds);
				}
			}
		}
	};
}
