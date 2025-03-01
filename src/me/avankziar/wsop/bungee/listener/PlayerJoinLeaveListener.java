package me.avankziar.wsop.bungee.listener;

import me.avankziar.wsop.bungee.WSOP;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class PlayerJoinLeaveListener implements Listener
{
	private WSOP plugin;
	
	public PlayerJoinLeaveListener(WSOP plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerDisconnect(PlayerDisconnectEvent event)
	{
		
	}
}