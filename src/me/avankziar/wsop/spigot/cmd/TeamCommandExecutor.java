package me.avankziar.wsop.spigot.cmd;

import java.io.IOException;
import java.util.ArrayList;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.wsop.general.assistance.ChatApiS;
import me.avankziar.wsop.general.cmdtree.ArgumentConstructor;
import me.avankziar.wsop.general.cmdtree.CommandConstructor;
import me.avankziar.wsop.general.cmdtree.CommandSuggest;
import me.avankziar.wsop.general.objects.ChangingGroup;
import me.avankziar.wsop.spigot.WSOP;
import me.avankziar.wsop.spigot.cmdtree.ArgumentModule;
import me.avankziar.wsop.spigot.handler.MessageHandler;

public class TeamCommandExecutor  implements CommandExecutor
{
	private WSOP plugin;
	private static CommandConstructor cc;
	
	public TeamCommandExecutor(WSOP plugin, CommandConstructor cc)
	{
		this.plugin = plugin;
		TeamCommandExecutor.cc = cc;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) 
	{
		if(cc == null)
		{
			return false;
		}
		if(args.length == 0)
		{
			if (!(sender instanceof Player)) 
			{
				plugin.getLogger().info("Cmd is only for Player!");
				return false;
			}
			Player player = (Player) sender;
			if(!player.hasPermission(cc.getPermission()))
			{
				///Du hast dafür keine Rechte!
				MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("NoPermission"));
				return false;
			}
			baseCommands(player); //Base and Info Command
			return true;
		}
		int length = args.length-1;
		ArrayList<ArgumentConstructor> aclist = cc.subcommands;
		for(int i = 0; i <= length; i++)
		{
			for(ArgumentConstructor ac : aclist)
			{
				if(args[i].equalsIgnoreCase(ac.getName()))
				{
					if(length >= ac.minArgsConstructor && length <= ac.maxArgsConstructor)
					{
						if (sender instanceof Player)
						{
							Player player = (Player) sender;
							if(player.hasPermission(ac.getPermission()))
							{
								ArgumentModule am = plugin.getArgumentMap().get(ac.getPath());
								if(am != null)
								{
									try
									{
										am.run(sender, args);
									} catch (IOException e)
									{
										e.printStackTrace();
									}
								} else
								{
									plugin.getLogger().info("ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
											.replace("%ac%", ac.getName()));
									MessageHandler.sendMessage(sender, 
											"ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
											.replace("%ac%", ac.getName()));
									return false;
								}
								return false;
							} else
							{
								MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("NoPermission"));
								return false;
							}
						} else
						{
							ArgumentModule am = plugin.getArgumentMap().get(ac.getPath());
							if(am != null)
							{
								try
								{
									am.run(sender, args);
								} catch (IOException e)
								{
									e.printStackTrace();
								}
							} else
							{
								plugin.getLogger().info("ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
										.replace("%ac%", ac.getName()));
								MessageHandler.sendMessage(sender,
										"ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
										.replace("%ac%", ac.getName()));
								return false;
							}
							return false;
						}
					} else
					{
						aclist = ac.subargument;
						break;
					}
				}
			}
		}
		MessageHandler.sendMessage(sender, ChatApiS.click(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
				"RUN_COMMAND", CommandSuggest.getCmdString(CommandSuggest.Type.WSOP)));
		return false;
	}
	
	public void baseCommands(final Player player)
	{
		if(plugin.getMysqlHandler().exist(new ChangingGroup(), "`player_uuid` = ?", player.getUniqueId().toString()))
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("Team.Status.IsDeactive"));
		} else
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("Team.Status.IsActive"));
		}
	}
}