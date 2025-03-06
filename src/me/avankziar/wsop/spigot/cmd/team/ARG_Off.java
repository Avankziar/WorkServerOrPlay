package me.avankziar.wsop.spigot.cmd.team;

import java.io.IOException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.wsop.general.cmdtree.ArgumentConstructor;
import me.avankziar.wsop.general.objects.ChangingGroup;
import me.avankziar.wsop.spigot.WSOP;
import me.avankziar.wsop.spigot.cmdtree.ArgumentModule;
import me.avankziar.wsop.spigot.handler.MessageHandler;
import me.avankziar.wsop.spigot.handler.TeamHandler;

public class ARG_Off extends ArgumentModule
{
	private WSOP plugin;
	
	public ARG_Off(ArgumentConstructor argumentConstructor)
	{
		super(argumentConstructor);
		this.plugin = WSOP.getPlugin();
	}

	@Override
	public void run(CommandSender sender, String[] args) throws IOException
	{
		Player player = (Player) sender;
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				task(player, args);
			}
		}.runTaskAsynchronously(plugin);
	}
	
	private void task(Player player, String[] args)
	{
		if(plugin.getMysqlHandler().exist(new ChangingGroup(), "`player_uuid` = ?", player.getUniqueId().toString()))
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("Team.Status.IsDeactive"));
		}
		if(TeamHandler.changeToPlay(player.getUniqueId()))
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("Team.Off.WasDeactivated"));
		} else
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("Team.Off.HasNoPermissionGroupToSave"));
		}
	}
}