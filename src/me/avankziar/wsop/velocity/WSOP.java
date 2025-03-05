package me.avankziar.wsop.velocity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import me.avankziar.ifh.velocity.IFH;
import me.avankziar.ifh.velocity.administration.Administration;
import me.avankziar.ifh.velocity.plugin.RegisteredServiceProvider;
import me.avankziar.wsop.general.assistance.Utility;
import me.avankziar.wsop.general.cmdtree.BaseConstructor;
import me.avankziar.wsop.general.cmdtree.CommandConstructor;
import me.avankziar.wsop.general.cmdtree.CommandSuggest;
import me.avankziar.wsop.general.database.YamlHandler;
import me.avankziar.wsop.general.database.YamlManager;
import me.avankziar.wsop.velocity.assistance.BackgroundTask;
import me.avankziar.wsop.velocity.cmd.BaseCommandExecutor;
import me.avankziar.wsop.velocity.cmdtree.ArgumentModule;
import me.avankziar.wsop.velocity.database.MysqlHandler;
import me.avankziar.wsop.velocity.database.MysqlSetup;
import me.avankziar.wsop.velocity.listener.JoinLeaveListener;
import me.avankziar.wsop.velocity.metric.Metrics;

@Plugin(
	id = "workserverorplay",
	name = "WorkServerOrPlay",
	version = "1-0-0",
	url = "tba",
	dependencies = {
			@Dependency(id = "interfacehub")
	},
	description = "A mc plugin, where server staff can deactivate there rolls to play normally.",
	authors = {"Avankziar"}
)
public class WSOP
{
	private static WSOP plugin;
    private final ProxyServer server;
    private Logger logger = null;
    private Path dataDirectory;
    public String pluginname = "WorkServerOrPlay";
    private final Metrics.Factory metricsFactory;
    private YamlHandler yamlHandler;
    private YamlManager yamlManager;
    private MysqlSetup mysqlSetup;
    private MysqlHandler mysqlHandler;
    private Utility utility;
    private ArrayList<CommandMeta> registeredCmds = new ArrayList<>();
    
	private static Administration administrationConsumer;
    
    @Inject
    public WSOP(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) 
    {
    	WSOP.plugin = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }
    
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) 
    {
    	PluginDescription pd = server.getPluginManager().getPlugin(pluginname.toLowerCase()).get().getDescription();
        List<String> dependencies = new ArrayList<>();
        pd.getDependencies().stream().allMatch(x -> dependencies.add(x.getId()));
        //https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=WSOP
		logger.info(" ██╗    ██╗███████╗ ██████╗ ██████╗  | Id: "+pd.getId());
		logger.info(" ██║    ██║██╔════╝██╔═══██╗██╔══██╗ | Version: "+pd.getVersion().get());
		logger.info(" ██║ █╗ ██║███████╗██║   ██║██████╔╝ | Author: ["+String.join(", ", pd.getAuthors())+"]");
		logger.info(" ██║███╗██║╚════██║██║   ██║██╔═══╝  | Description: "+(pd.getDescription().isPresent() ? pd.getDescription().get() : "/"));
		logger.info(" ╚███╔███╔╝███████║╚██████╔╝██║      | Plugin Website:"+pd.getUrl().toString());
		logger.info("  ╚══╝╚══╝ ╚══════╝ ╚═════╝ ╚═╝      | Dependencies Plugins: ["+String.join(", ", dependencies)+"]");
        
		setupIFHAdministration();
		
		yamlHandler = new YamlHandler(YamlManager.Type.VELO, pluginname, logger, dataDirectory,
        		(plugin.getAdministration() == null ? null : plugin.getAdministration().getLanguage()));
        setYamlManager(yamlHandler.getYamlManager());
        utility = new Utility(mysqlHandler);
        
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
        setListeners();
        setupBstats();
        new BackgroundTask(plugin);
    }
    
    public void onDisable(ProxyShutdownEvent event)
	{
    	getServer().getScheduler().tasksByPlugin(plugin).forEach(x -> x.cancel());
    	logger = null;
    	yamlHandler = null;
    	yamlManager = null;
    	mysqlSetup = null;
    	mysqlHandler = null;
    	registeredCmds.forEach(x -> getServer().getCommandManager().unregister(x));
    	getServer().getEventManager().unregisterListeners(plugin);
    	Optional<PluginContainer> ifhp = plugin.getServer().getPluginManager().getPlugin("interfacehub");
        if(!ifhp.isEmpty()) 
        {
        	Optional<PluginContainer> plugins = plugin.getServer().getPluginManager().getPlugin(pluginname.toLowerCase());
        	me.avankziar.ifh.velocity.IFH ifh = IFH.getPlugin();
        	plugins.ifPresent(x -> ifh.getServicesManager().unregister(x));
        }
	}
    
    public static WSOP getPlugin()
    {
    	return WSOP.plugin;
    }
    
    public static void shutdown()
    {
    	WSOP.getPlugin().onDisable(null);
    }
    
    public ProxyServer getServer()
    {
    	return server;
    }
    
    public Logger getLogger()
    {
    	return logger;
    }
    
    public Path getDataDirectory()
    {
    	return dataDirectory;
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
    
    private void setupCommandTree()
	{
    	CommandManager cm = getServer().getCommandManager();
    	CommandConstructor base = new CommandConstructor(CommandSuggest.Type.WSOP, "base", true, false);
		CommandMeta basemeta = cm.metaBuilder(base.getName()).plugin(plugin).build();
		
		cm.register(basemeta, new BaseCommandExecutor(plugin, base));
		registeredCmds.add(basemeta);
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
		return BaseConstructor.getArgumentMapVelo();
	}
    
    private void setListeners()
    {
    	EventManager em = server.getEventManager();
    	em.register(this, new JoinLeaveListener(plugin));
    }
    
    private void setupIFHAdministration()
	{ 
		Optional<PluginContainer> ifhp = plugin.getServer().getPluginManager().getPlugin("interfacehub");
        if (ifhp.isEmpty()) 
        {
        	logger.info(pluginname + " dont find InterfaceHub!");
            return;
        }
        me.avankziar.ifh.velocity.IFH ifh = IFH.getPlugin();
        RegisteredServiceProvider<Administration> rsp = ifh
        		.getServicesManager()
        		.getRegistration(Administration.class);
        if (rsp == null) 
        {
            return;
        }
        administrationConsumer = rsp.getProvider();
        if(administrationConsumer != null)
        {
    		logger.info(pluginname + " detected InterfaceHub >>> Administration.class is consumed!");
        }
        return;
	}
	
	public Administration getAdministration()
	{
		return administrationConsumer;
	}
    
    public void setupBstats()
	{
    	int pluginId = 24957;
        metricsFactory.make(this, pluginId);
	}
}