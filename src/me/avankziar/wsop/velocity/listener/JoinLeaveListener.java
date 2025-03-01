package me.avankziar.wsop.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;

import me.avankziar.wsop.velocity.WSOP;

public class JoinLeaveListener
{
	private WSOP plugin;
	
	public JoinLeaveListener(WSOP plugin)
	{
		this.plugin = plugin;
	}
	
	@Subscribe
	public void onPlayerJoin(PlayerChooseInitialServerEvent event)
	{
		
	}
	
	@Subscribe
	public void onPlayerQuit(DisconnectEvent event)
	{
		
	}
}