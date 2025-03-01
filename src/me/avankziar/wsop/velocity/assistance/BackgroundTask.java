package me.avankziar.wsop.velocity.assistance;

import java.util.concurrent.TimeUnit;

import me.avankziar.wsop.velocity.WSOP;

public class BackgroundTask 
{
	private WSOP plugin;
	
	public BackgroundTask(WSOP plugin)
	{
		this.plugin = plugin;
		runTask();
	}
	
	private void runTask()
	{
		plugin.getServer().getScheduler().buildTask(plugin, (task) ->
		{
			//Do something
		}).delay(1L, TimeUnit.MILLISECONDS).repeat(15L, TimeUnit.MILLISECONDS).schedule();
	}
	
	
}
