package org.eestecmb.oldteam;

import android.app.Application;

public class MobitelMonitor extends Application
{
	public MonitorResult monitorResult = null;
	public String lastPhoneNum = "";
	public long lastRefresh = -1;

}