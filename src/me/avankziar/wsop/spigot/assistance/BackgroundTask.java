package me.avankziar.wsop.spigot.assistance;

import me.avankziar.wsop.spigot.WSOP;

public class BackgroundTask
{
	private static WSOP plugin;
	
	public BackgroundTask(WSOP plugin)
	{
		BackgroundTask.plugin = plugin;
		initBackgroundTask();
	}
	
	public boolean initBackgroundTask()
	{
		
		return true;
	}
}
