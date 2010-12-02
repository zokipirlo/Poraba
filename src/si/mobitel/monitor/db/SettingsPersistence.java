package si.mobitel.monitor.db;

import java.util.HashMap;

import android.content.Context;
import android.telephony.TelephonyManager;

public class SettingsPersistence {
	
	public HashMap<String, String> settings = null;
	private String phoneNumber = defaultUser;
	private SQLHelper sqlHelp;
	
	public static String defaultUser = "system";
	
	
	
	public Boolean isQuotaVisible(String description) {
		if (settings.containsKey("qu:"+ description)) {
			if (settings.get("qu:"+ description).equalsIgnoreCase("yes")) {
				return true;
			}
			return false;
		}
		return true;
	}
	
	public String getPassword () {
		if (settings.containsKey("password")) {
			return settings.get("password");
		}
		return null;
	}
	
	
	public void setPhoneNumber(String phoneNum) {
		phoneNumber = defaultUser;
		settings.put("currentAccount", phoneNum);
		save();
		phoneNumber = phoneNum;
		load();
	}
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	
	
	public SettingsPersistence(Context context) {
		sqlHelp = new SQLHelper(context);
		init(context);
	}
	
	public String getSetting(String key) {
		if (key == null || key == "") return null;
		if (settings == null) {
			load();
		}
		return settings.get(key);
	}
	
	public void setSetting(String key, String value) {
		if (key == null || key == "") return;
		if (settings == null) {
			load();
		}
		settings.put(key, value);
	}
	
	
	public void save() {
		sqlHelp.insertAll(settings, phoneNumber);
	}
	
	private void load() {
		settings = sqlHelp.getAllSettingsForPhoneNum(phoneNumber);
	}
	
	private void init(Context context) {
		load();
		if (settings.containsKey("currentAccount")) {
			phoneNumber = settings.get("currentAccount");
		} else {
			TelephonyManager tMgr =(TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			String pNum = tMgr.getLine1Number();
			if (pNum == null) pNum = "";
			if (pNum.contains("+386")) {
				pNum = pNum.substring(4);
				pNum = "0" + pNum;
			}
			settings.put("currentAccount", pNum);
			save();
			phoneNumber = pNum;
		}
		load();
		
	}

}
