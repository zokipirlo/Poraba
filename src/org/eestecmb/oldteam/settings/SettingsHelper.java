package org.eestecmb.oldteam.settings;

import org.eestecmb.oldteam.MonitorDataXmlReader;
import org.eestecmb.oldteam.MonitorResult;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.telephony.TelephonyManager;

public class SettingsHelper
{
	private final String APP_SETTINGS = "AppSettings";
	private final String USER_SETTINGS = "UserSettings";
	private final String PASSWORDS = "Passwords";
	
	private static SettingsHelper mInstance = null;
	private static Context mContext;
	
	private SharedPreferences mAppSettings = null;
	private SharedPreferences mUserSettings = null;
	private SharedPreferences mPasswords = null;
	
	public static SettingsHelper getInstance(Context context)
	{
		mContext = context;
		if (mInstance == null)
			mInstance = new SettingsHelper();
		
		return mInstance;
	}
	
	private SharedPreferences getAppSettings()
	{
		if (mAppSettings == null)
			mAppSettings = mContext.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE);
		
		return mAppSettings;
	}
	private SharedPreferences getUserSettings()
	{
		if (mUserSettings == null)
			mUserSettings = mContext.getSharedPreferences(USER_SETTINGS, Context.MODE_PRIVATE);
		
		return mUserSettings;
	}
	private SharedPreferences getPasswords()
	{
		if (mPasswords == null)
			mPasswords = mContext.getSharedPreferences(PASSWORDS, Context.MODE_PRIVATE);
		
		return mPasswords;
	}
	
	//Application settings
	public String getLastPhoneNumber()
	{
		return getAppSettings().getString("last_phone_number", "");
	}
	public void setLastNumber(String number)
	{
		Editor edit = getAppSettings().edit();
		edit.putString("last_phone_number", number);
		edit.commit();
	}
	
	public String getMyPhoneNumber()
	{
		TelephonyManager tMgr = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
		String pNum = tMgr.getLine1Number();
		if (pNum == null)
			pNum = "";
		if (pNum.contains("+386"))
		{
			pNum = pNum.substring(4);
			pNum = "0" + pNum;
		}
		setLastNumber(pNum);
		return pNum;
	}
	
	//Password settings
	public String getPassword(String phoneNumber)
	{
		String password = getPasswords().getString(phoneNumber, "");
		
		if (password.length() == 0)
			return "";
		
		String[] passParts = password.split(",");
		byte[] pass = new byte[passParts.length];
		
		for (int i = 0; i < passParts.length; i++)
			pass[i] = Byte.parseByte(passParts[i]);
		
		return new String(new RC4(EncryptionKey.KEY).rc4(pass));
	}
	public void setPassword(String phoneNumber, String password)
	{
		if (password.length() == 0)
			return;
		
		Editor edit = getPasswords().edit();
		byte[] pass = new RC4(EncryptionKey.KEY).rc4(password);
		
		StringBuilder sb = new StringBuilder(pass.length);
		sb.append(pass[0]);
		for (int i = 1; i < pass.length; i++)
			sb.append("," + pass[i]);
		
		edit.putString(phoneNumber, sb.toString());
		edit.commit();
	}
	
	//User settings
	public long getLastRefresh(String phoneNumber)
	{
		try
		{
			JSONObject json = new JSONObject(getUserSettings().getString(phoneNumber, "{}"));
			return json.optLong("lastRefresh", -1);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return -1;
	}
	public void setLastRefresh(String phoneNumber, long milis)
	{
		try
		{
			JSONObject json = new JSONObject(getUserSettings().getString(phoneNumber, "{}"));
			json.put("lastRefresh", milis);
			Editor edit = getUserSettings().edit();
			edit.putString(phoneNumber, json.toString());
			edit.commit();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public MonitorResult getLastResult(String phoneNumber)
	{
		try
		{
			JSONObject json = new JSONObject(getUserSettings().getString(phoneNumber, "{}"));
			String xml = json.optString("lastResult", "");
			if (xml.length() > 0)
			{
				MonitorDataXmlReader xmlReader = new MonitorDataXmlReader();
				return xmlReader.parseXML(xml);
			}
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	public void setLastResult(String phoneNumber, String xml)
	{
		try
		{
			JSONObject json = new JSONObject(getUserSettings().getString(phoneNumber, "{}"));
			json.put("lastResult", xml);
			Editor edit = getUserSettings().edit();
			edit.putString(phoneNumber, json.toString());
			edit.commit();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public JSONObject getQuotas(String phoneNumber)
	{
		try
		{
			JSONObject json = new JSONObject(getUserSettings().getString(phoneNumber, "{}"));
			return json.optJSONObject("quotas");
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	public void setQuotas(String phoneNumber, JSONObject quotas)
	{
		try
		{
			JSONObject json = new JSONObject(getUserSettings().getString(phoneNumber, "{}"));
			json.put("quotas", quotas);
			Editor edit = getUserSettings().edit();
			edit.putString(phoneNumber, json.toString());
			edit.commit();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean useMNamiznik(String phoneNumber)
	{
		try
		{
			JSONObject json = new JSONObject(getUserSettings().getString(phoneNumber, "{}"));
			return json.optBoolean("useMNamiznik", false);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	public void setMNamiznik(String phoneNumber, boolean value)
	{
		try
		{
			JSONObject json = new JSONObject(getUserSettings().getString(phoneNumber, "{}"));
			json.put("useMNamiznik", value);
			Editor edit = getUserSettings().edit();
			edit.putString(phoneNumber, json.toString());
			edit.commit();
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}
	}
}
