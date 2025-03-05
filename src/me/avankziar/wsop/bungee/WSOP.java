package me.avankziar.wsop.bungee;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.logging.Logger;

import me.avankziar.ifh.bungee.plugin.ServicePriority;
import me.avankziar.wsop.bungee.cmd.BaseCommandExecutor;
import me.avankziar.wsop.bungee.cmdtree.ArgumentModule;
import me.avankziar.wsop.bungee.database.MysqlHandler;
import me.avankziar.wsop.bungee.database.MysqlSetup;
import me.avankziar.wsop.bungee.ifh.AdministrationProvider;
import me.avankziar.wsop.bungee.listener.PlayerJoinLeaveListener;
import me.avankziar.wsop.bungee.metric.Metrics;
import me.avankziar.wsop.general.cmdtree.BaseConstructor;
import me.avankziar.wsop.general.cmdtree.CommandConstructor;
import me.avankziar.wsop.general.cmdtree.CommandSuggest;
import me.avankziar.wsop.general.database.YamlHandler;
import me.avankziar.wsop.general.database.YamlManager;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class WSOP extends Plugin
{
	public static WSOP plugin;
	public static Logger logger;
	public static String pluginname = "WorkServerOrPlay";
	private static YamlHandler yamlHandler;
	private static YamlManager yamlManager;
	private static MysqlHandler mysqlHandler;
	private static MysqlSetup mysqlSetup;
	private AdministrationProvider administrationProvider;
	
	public void onEnable() 
	{
		plugin = this;
		logger = Logger.getLogger("WSOP");
		
		//https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=WSOP
		logger.info(" ██╗    ██╗███████╗ ██████╗ ██████╗  | Version: "+plugin.getDescription().getVersion());
		logger.info(" ██║    ██║██╔════╝██╔═══██╗██╔══██╗ | Author: "+plugin.getDescription().getAuthor());
		logger.info(" ██║ █╗ ██║███████╗██║   ██║██████╔╝ | Plugin Website: ");
		logger.info(" ██║███╗██║╚════██║██║   ██║██╔═══╝  | Depend Plugins: "+plugin.getDescription().getDepends().toString());
		logger.info(" ╚███╔███╔╝███████║╚██████╔╝██║      | SoftDepend Plugins: "+plugin.getDescription().getSoftDepends().toString());
		logger.info("  ╚══╝╚══╝ ╚══════╝ ╚═════╝ ╚═╝      | Have Fun^^");
		
		yamlHandler = new YamlHandler(YamlManager.Type.BUNGEE, pluginname, logger, plugin.getDataFolder().toPath(),
        		(plugin.getAdministration() == null ? null : plugin.getAdministration().getLanguage()));
        setYamlManager(yamlHandler.getYamlManager());
		
		String path = plugin.getYamlHandler().getConfig().getString("IFHAdministrationPath");
		boolean adm = plugin.getAdministration() != null 
				&& plugin.getAdministration().isMysqlPathActive(path);
		if(adm || yamlHandler.getConfig().getBoolean("Mysql.Status", false) == true)
		{
			mysqlSetup = new MysqlSetup(plugin, adm, path);
			mysqlHandler = new MysqlHandler(plugin);
		}
		
		BaseConstructor.init(yamlHandler);
		setupCommandTree();
		ListenerSetup();
		setupIFHProvider();
		setupBstats();
	}
	
	public void onDisable()
	{
		getProxy().getScheduler().cancel(plugin);	
		logger = null;
		yamlHandler = null;
		yamlManager = null;
		mysqlSetup = null;
		mysqlHandler = null;
		getProxy().getPluginManager().unregisterListeners(plugin);
		getProxy().getPluginManager().unregisterCommands(plugin);
		Plugin ifhp = getProxy().getPluginManager().getPlugin("InterfaceHub");
        if(ifhp != null) 
        {
        	 me.avankziar.ifh.bungee.IFH ifh = (me.avankziar.ifh.bungee.IFH) ifhp;
        	 ifh.getServicesManager().unregisterAll(plugin);
        }
		logger.info(pluginname + " is disabled!");
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
	
	public MysqlHandler getMysqlHandler()
	{
		return mysqlHandler;
	}
	
	public MysqlSetup getMysqlSetup()
	{
		return mysqlSetup;
	}
	
	public void setYamlManager(YamlManager yamlManager)
	{
		WSOP.yamlManager = yamlManager;
	}
	
	private void setupCommandTree()
	{
		PluginManager pm = getProxy().getPluginManager();
		CommandConstructor base = new CommandConstructor(CommandSuggest.Type.WSOP, "base", true, false);
		pm.registerCommand(plugin, new BaseCommandExecutor(plugin, base));
	}
	
	public ArrayList<BaseConstructor> getHelpList()
	{
		return BaseConstructor.getHelpList();
	}
    
    public ArrayList<CommandConstructor> getCommandTree()
	{
		return BaseConstructor.getCommandTree();
	}
    
    public LinkedHashMap<String, ArgumentModule> getArgumentMap()
	{
		return BaseConstructor.getArgumentMapBungee();
	}
	
	public void ListenerSetup()
	{
		PluginManager pm = getProxy().getPluginManager();
		pm.registerListener(plugin, new PlayerJoinLeaveListener(plugin));
	}
	
	private void setupIFHProvider()
	{
		Plugin ifhp = getProxy().getPluginManager().getPlugin("InterfaceHub");
        if (ifhp == null) 
        {
            return;
        }
        me.avankziar.ifh.bungee.IFH ifh = (me.avankziar.ifh.bungee.IFH) ifhp;
        try
        {
        	administrationProvider = new AdministrationProvider(plugin);
            ifh.getServicesManager().register(
             		me.avankziar.ifh.bungee.administration.Administration.class,
             		administrationProvider, plugin, ServicePriority.Normal);
            logger.info(pluginname + " detected InterfaceHub >>> Administration.class is provided!");
    		
        } catch(NoClassDefFoundError e){}
	}
	
	public AdministrationProvider getAdministration()
	{
		return administrationProvider;
	}
	
	public void setupBstats()
	{
		int pluginId = 24955;
        new Metrics(this, pluginId);
	}
}