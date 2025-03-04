package me.avankziar.wsop.spigot;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import me.avankziar.ifh.spigot.administration.Administration;
import me.avankziar.wsop.general.assistance.Utility;
import me.avankziar.wsop.general.cmdtree.BaseConstructor;
import me.avankziar.wsop.general.cmdtree.CommandConstructor;
import me.avankziar.wsop.general.cmdtree.CommandSuggest;
import me.avankziar.wsop.general.database.ServerType;
import me.avankziar.wsop.general.database.YamlHandler;
import me.avankziar.wsop.general.database.YamlManager;
import me.avankziar.wsop.spigot.ModifierValueEntry.Bypass;
import me.avankziar.wsop.spigot.assistance.BackgroundTask;
import me.avankziar.wsop.spigot.cmd.TabCompletion;
import me.avankziar.wsop.spigot.cmd.TeamCommandExecutor;
import me.avankziar.wsop.spigot.cmdtree.ArgumentModule;
import me.avankziar.wsop.spigot.database.MysqlHandler;
import me.avankziar.wsop.spigot.database.MysqlSetup;
import me.avankziar.wsop.spigot.handler.TeamHandler;
import me.avankziar.wsop.spigot.listener.JoinLeaveListener;
import me.avankziar.wsop.spigot.metric.Metrics;

public class WSOP extends JavaPlugin
{
	public static Logger logger;
	private static WSOP plugin;
	public static String pluginname = "WorkServerOrPlay";
	private YamlHandler yamlHandler;
	private YamlManager yamlManager;
	private MysqlSetup mysqlSetup;
	private MysqlHandler mysqlHandler;
	private Utility utility;
	private BackgroundTask backgroundTask;
	
	private TeamHandler teamHandler;
	
	private Administration administrationConsumer;
	
	public void onEnable()
	{
		plugin = this;
		logger = getLogger();
		
		//https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=WSOP
		logger.info(" ██╗    ██╗███████╗ ██████╗ ██████╗  | API-Version: "+plugin.getDescription().getAPIVersion());
		logger.info(" ██║    ██║██╔════╝██╔═══██╗██╔══██╗ | Author: "+plugin.getDescription().getAuthors().toString());
		logger.info(" ██║ █╗ ██║███████╗██║   ██║██████╔╝ | Plugin Website: "+plugin.getDescription().getWebsite());
		logger.info(" ██║███╗██║╚════██║██║   ██║██╔═══╝  | Depend Plugins: "+plugin.getDescription().getDepend().toString());
		logger.info(" ╚███╔███╔╝███████║╚██████╔╝██║      | SoftDepend Plugins: "+plugin.getDescription().getSoftDepend().toString());
		logger.info("  ╚══╝╚══╝ ╚══════╝ ╚═════╝ ╚═╝      | LoadBefore: "+plugin.getDescription().getLoadBefore().toString());
		
		setupIFHAdministration();
		
		yamlHandler = new YamlHandler(YamlManager.Type.SPIGOT, pluginname, logger, plugin.getDataFolder().toPath(),
        		(plugin.getAdministration() == null ? null : plugin.getAdministration().getLanguage()));
        setYamlManager(yamlHandler.getYamlManager());
		
		String path = plugin.getYamlHandler().getConfig().getString("IFHAdministrationPath");
		boolean adm = plugin.getAdministration() != null 
				&& plugin.getYamlHandler().getConfig().getBoolean("useIFHAdministration")
				&& plugin.getAdministration().isMysqlPathActive(path);
		if(adm || yamlHandler.getConfig().getBoolean("Mysql.Status", false) == true)
		{
			mysqlSetup = new MysqlSetup(plugin, adm, path);
			mysqlHandler = new MysqlHandler(plugin);
		} else
		{
			logger.severe("MySQL is not set in the Plugin " + pluginname + "!");
			Bukkit.getPluginManager().getPlugin(pluginname).getPluginLoader().disablePlugin(this);
			return;
		}
		
		BaseConstructor.init(yamlHandler);
		utility = new Utility(mysqlHandler);
		backgroundTask = new BackgroundTask(this);
		
		setupBypassPerm();
		setupCommandTree();
		setupListeners();
		setupIFHConsumer();
		setupBstats();
	}
	
	public void onDisable()
	{
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		yamlHandler = null;
		yamlManager = null;
		mysqlSetup = null;
		mysqlHandler = null;
		if(getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	getServer().getServicesManager().unregisterAll(plugin);
	    }
		logger.info(pluginname + " is disabled!");
		logger = null;
	}

	public static WSOP getPlugin()
	{
		return plugin;
	}
	
	public static void shutdown()
	{
		WSOP.getPlugin().onDisable();
	}
	
	public YamlHandler getYamlHandler() 
	{
		return yamlHandler;
	}
	
	public YamlManager getYamlManager()
	{
		return yamlManager;
	}

	public void setYamlManager(YamlManager yamlManager)
	{
		this.yamlManager = yamlManager;
	}
	
	public MysqlSetup getMysqlSetup() 
	{
		return mysqlSetup;
	}
	
	public MysqlHandler getMysqlHandler()
	{
		return mysqlHandler;
	}
	
	public Utility getUtility()
	{
		return utility;
	}
	
	public BackgroundTask getBackgroundTask()
	{
		return backgroundTask;
	}
	
	public String getServername()
	{
		return getPlugin().getAdministration() != null ? getPlugin().getAdministration().getSpigotServerName() 
				: getPlugin().getYamlHandler().getConfig().getString("ServerName");
	}
	
	private void setupCommandTree()
	{		
		TabCompletion tab = new TabCompletion();
		
		CommandConstructor base = new CommandConstructor(CommandSuggest.Type.BASE, "base", false, false);
		registerCommand(base, new TeamCommandExecutor(plugin, base), tab);
		
		//ArgumentConstructor add = new ArgumentConstructor(CommandSuggest.Type.FRIEND_ADD, "friend_add", 0, 1, 1, false, playerMapI);
		//CommandConstructor friend = new CommandConstructor(CommandSuggest.Type.FRIEND, "friend", false, add, remove);
		//registerCommand(friend, new FriendCommandExecutor(plugin, friend), tab);
		//new ARGAdd(plugin, add);
	}
	
	public void setupBypassPerm()
	{
		String path = "Count.";
		for(Bypass.Counter bypass : new ArrayList<Bypass.Counter>(EnumSet.allOf(Bypass.Counter.class)))
		{
			if(!bypass.forPermission())
			{
				continue;
			}
			Bypass.set(bypass, yamlHandler.getCommands().getString(path+bypass.toString().replace("_", ".")));
		}
		path = "Bypass.";
		for(Bypass.Permission bypass : new ArrayList<Bypass.Permission>(EnumSet.allOf(Bypass.Permission.class)))
		{
			Bypass.set(bypass, yamlHandler.getCommands().getString(path+bypass.toString().replace("_", ".")));
		}
	}
	
	public ArrayList<BaseConstructor> getHelpList()
	{
		return BaseConstructor.getHelpList();
	}
	
	public ArrayList<CommandConstructor> getCommandTree()
	{
		return BaseConstructor.getCommandTree();
	}
	
	public void registerCommand(CommandConstructor cc, CommandExecutor ce, TabCompletion tab)
	{
		registerCommand(cc.getPath(), cc.getName());
		getCommand(cc.getName()).setExecutor(ce);
		getCommand(cc.getName()).setTabCompleter(tab);
	}
	
	public void registerCommand(String... aliases) 
	{
		PluginCommand command = getCommand(aliases[0], plugin);
	 
		command.setAliases(Arrays.asList(aliases));
		getCommandMap().register(plugin.getDescription().getName(), command);
	}
	 
	private static PluginCommand getCommand(String name, WSOP plugin) 
	{
		PluginCommand command = null;
		try 
		{
			Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
			c.setAccessible(true);
	 
			command = c.newInstance(name, plugin);
		} catch (SecurityException e) 
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		} catch (InstantiationException e) 
		{
			e.printStackTrace();
		} catch (InvocationTargetException e) 
		{
			e.printStackTrace();
		} catch (NoSuchMethodException e) 
		{
			e.printStackTrace();
		}
	 
		return command;
	}
	 
	private static CommandMap getCommandMap() 
	{
		CommandMap commandMap = null;
	 
		try {
			if (Bukkit.getPluginManager() instanceof SimplePluginManager) 
			{
				Field f = SimplePluginManager.class.getDeclaredField("commandMap");
				f.setAccessible(true);
	 
				commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
			}
		} catch (NoSuchFieldException e) 
		{
			e.printStackTrace();
		} catch (SecurityException e) 
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e) 
		{
			e.printStackTrace();
		} catch (IllegalAccessException e) 
		{
			e.printStackTrace();
		}
	 
		return commandMap;
	}
	
	public LinkedHashMap<String, ArgumentModule> getArgumentMap()
	{
		return BaseConstructor.getArgumentMapSpigot();
	}
	
	public void setupListeners()
	{
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new JoinLeaveListener(), plugin);
	}
	
	public boolean reload() throws IOException
	{
		if(!yamlHandler.loadYamlHandler(YamlManager.Type.SPIGOT))
		{
			return false;
		}
		if(yamlHandler.getConfig().getBoolean("Mysql.Status", false))
		{
			if(!mysqlSetup.loadMysqlSetup(ServerType.SPIGOT))
			{
				return false;
			}
		} else
		{
			return false;
		}
		return true;
	}
	
	public boolean existHook(String externPluginName)
	{
		if(plugin.getServer().getPluginManager().getPlugin(externPluginName) == null)
		{
			return false;
		}
		logger.info(pluginname+" hook with "+externPluginName);
		return true;
	}
	
	private void setupIFHAdministration()
	{ 
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	return;
	    }
		RegisteredServiceProvider<me.avankziar.ifh.spigot.administration.Administration> rsp = 
                getServer().getServicesManager().getRegistration(Administration.class);
		if (rsp == null) 
		{
		   return;
		}
		administrationConsumer = rsp.getProvider();
		logger.info(pluginname + " detected InterfaceHub >>> Administration.class is consumed!");
	}
	
	public Administration getAdministration()
	{
		return administrationConsumer;
	}
	
	public void setupIFHConsumer()
	{
		
	}
	
	private void setupPermissionApis()
    {
		Plugin lp = getServer().getPluginManager().getPlugin("LuckPerms");
        if(lp != null && lp.isEnabled()) 
        {
        	teamHandler = new TeamHandler(plugin, net.luckperms.api.LuckPermsProvider.get());
        }
        if(teamHandler == null)
        {
        	shutdown();
        }
    }
	
	public TeamHandler getTeamHandler()
    {
    	return teamHandler;
    }
	
	public void setupBstats()
	{
		int pluginId = 24956;
        new Metrics(this, pluginId);
	}
}