package me.avankziar.wsop.velocity.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;

import me.avankziar.wsop.general.assistance.ChatApiV;
import me.avankziar.wsop.general.assistance.MatchApi;
import me.avankziar.wsop.general.cmdtree.ArgumentConstructor;
import me.avankziar.wsop.general.cmdtree.BaseConstructor;
import me.avankziar.wsop.general.cmdtree.CommandConstructor;
import me.avankziar.wsop.general.cmdtree.CommandSuggest;
import me.avankziar.wsop.velocity.WSOP;
import me.avankziar.wsop.velocity.cmdtree.ArgumentModule;

public class BaseCommandExecutor implements SimpleCommand
{
	private WSOP plugin;
	private static CommandConstructor cc;
	
	public BaseCommandExecutor(WSOP plugin, CommandConstructor cc)
	{
		this.plugin = plugin;
		BaseCommandExecutor.cc = cc;
	}
	
    public CompletableFuture<List<String>> suggestAsync(final Invocation invocation) 
    {
        return CompletableFuture.completedFuture(new TabCompletion().getTabs(invocation.source(), cc.getName(), invocation.arguments()));
    }
	
	public void execute(final Invocation invocation) 
	{
        CommandSource sender = invocation.source();
        String[] args = invocation.arguments();
        
        Player player = null;
        if(sender instanceof Player)
        {
        	player = (Player) sender;
        }
        if(cc == null)
		{
			return;
		}
		if (args.length == 1) 
		{
			if(cc.canConsoleAccess() && player == null)
			{
				if(MatchApi.isInteger(args[0]))
				{
					baseCommands(player, Integer.parseInt(args[0]));
					return;
				}
			} else
			{
				if(MatchApi.isInteger(args[0]))
				{
					if(!player.hasPermission(cc.getPermission()))
					{
						player.sendMessage(ChatApiV.tl(plugin.getYamlHandler().getLang().getString("NoPermission")));
						return;
					}
					baseCommands(player, Integer.parseInt(args[0]));
					return;
				}
			}
		} else if(args.length == 0)
		{
			if(cc.canConsoleAccess() && player == null)
			{
				baseCommands(player, 0);
			} else
			{
				if(!player.hasPermission(cc.getPermission()))
				{
					player.sendMessage(ChatApiV.tl(plugin.getYamlHandler().getLang().getString("NoPermission")));
					return;
				}
				baseCommands(player, 0);
				return;
			}
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
						ArgumentModule am = plugin.getArgumentMap().get(ac.getPath());
						if(ac.canConsoleAccess() && player == null)
						{
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
								WSOP.getPlugin().getLogger().info("ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
										.replace("%ac%", ac.getName()));
								return;
							}
						} else if(player != null)
						{
							if(player.hasPermission(ac.getPermission()))
							{
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
									WSOP.getPlugin().getLogger().info("ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
											.replace("%ac%", ac.getName()));
									player.sendMessage(ChatApiV.tl(
											"ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
											.replace("%ac%", ac.getName())));
									return;
								}
								return;
							} else
							{
								player.sendMessage(ChatApiV.tl(plugin.getYamlHandler().getLang().getString("NoPermission")));
								return;
							}
						} else
						{
							WSOP.getPlugin().getLogger().info("Cannot access ArgumentModule! Command is not for ConsoleAccess and Executer is Console "
									+ "or Executor is Player and a other Error set place!"
									.replace("%ac%", ac.getName()));
						}
					} else
					{
						aclist = ac.subargument;
						break;
					}
				}
			}
		}
		if(player == null)
		{
			WSOP.getPlugin().getLogger().warning(plugin.getYamlHandler().getLang().getString("InputIsWrong"));
		} else
		{
			player.sendMessage(ChatApiV.tl(ChatApiV.click(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
					"RUN_COMMAND", CommandSuggest.getCmdString(CommandSuggest.Type.WSOP))));
		}
		return;
	}
	
	public void baseCommands(final Player player, int page)
	{
		int count = 0;
		int start = page*10;
		int end = page*10+9;
		int last = 0;
		player.sendMessage(ChatApiV.tl(plugin.getYamlHandler().getLang().getString("BaseInfo.Headline")));
		for(BaseConstructor bc : plugin.getHelpList())
		{
			if(count >= start && count <= end)
			{
				if(player.hasPermission(bc.getPermission()))
				{
					sendInfo(player, bc);
				}
			}
			count++;
			last++;
		}
		pastNextPage(player, page, last, CommandSuggest.getCmdString(CommandSuggest.Type.WSOP));
	}
	
	private void sendInfo(Player player, BaseConstructor bc)
	{
		player.sendMessage(ChatApiV.tl(ChatApiV.clickHover(
				bc.getHelpInfo(),
				"SUGGEST_COMMAND", bc.getSuggestion(),
				"SHOW_TEXT", plugin.getYamlHandler().getLang().getString("GeneralHover"))));
	}
	
	public static void pastNextPage(Player player,
			int page, int last, String cmdstring, String...objects)
	{
		if(page == 0 && page >= last)
		{
			return;
		}
		int i = page+1;
		int j = page-1;
		StringBuilder sb = new StringBuilder();
		if(page != 0)
		{
			String msg2 = WSOP.getPlugin().getYamlHandler().getLang().getString("BaseInfo.Past");
			String cmd = cmdstring+" "+String.valueOf(j);
			for(String o : objects)
			{
				cmd += " "+o;
			}
			sb.append(ChatApiV.click(msg2,"RUN_COMMAND", cmd));
		}
		if(page >= last)
		{
			String msg1 = WSOP.getPlugin().getYamlHandler().getLang().getString("BaseInfo.Next");
			String cmd = cmdstring+" "+String.valueOf(i);
			for(String o : objects)
			{
				cmd += " "+o;
			}
			if(sb.length() > 0)
			{
				sb.append(" | ");
			}
			sb.append(ChatApiV.click(msg1, "RUN_COMMAND", cmd));
		}	
		player.sendMessage(ChatApiV.tl(sb.toString()));
	}
}