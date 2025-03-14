package me.avankziar.wsop.bungee.ifh;

import java.util.LinkedHashMap;

import me.avankziar.ifh.bungee.administration.Administration;
import me.avankziar.wsop.bungee.WSOP;

public class AdministrationProvider implements Administration
{
	private WSOP plugin;
	private LinkedHashMap<String, String> forwarding = new LinkedHashMap<>();
	
	public AdministrationProvider(WSOP plugin)
	{
		this.plugin = plugin;
		for(String entry : plugin.getYamlHandler().getConfig().getStringList("PluginForwarding"))
		{
			String[] s = entry.split(">>>");
			if(s.length != 2)
			{
				continue;
			}
			String k = s[0];
			String v = s[1];
			forwarding.put(k, v);
		}
	}
	
	@Override
	public String getLanguage()
	{
		return plugin.getYamlHandler().getConfig().getString("Language", "ENG");
	}
	
	private String getPath(String pathName)
	{
		String s = forwarding.get(pathName);
		return s != null ? s : pathName;
	}
	
	@Override
	public boolean isMysqlPathActive(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getBoolean("Mysql."+getPath(pathName)+".IsActive", false);
	}
	
	@Override
	public String getHost(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getString("Mysql."+getPath(pathName)+".Host", null);
	}
	
	@Override
	public int getPort(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getInt("Mysql."+getPath(pathName)+".Port", 3306);
	}
	
	@Override
	public String getDatabase(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getString("Mysql."+getPath(pathName)+".DatabaseName", null);
	}
	
	@Override
	public String getUsername(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getString("Mysql."+getPath(pathName)+".User", null);
	}
	
	@Override
	public String getPassword(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getString("Mysql."+getPath(pathName)+".Password", null);
	}
	
	@Override
	public boolean isAutoReconnect(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getBoolean("Mysql."+getPath(pathName)+".AutoReconnect", false);
	}
	
	@Override
	public boolean isVerifyServerCertificate(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getBoolean("Mysql."+getPath(pathName)+".VerifyServerCertificate", false);
	}
	
	@Override
	public boolean useSSL(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getBoolean("Mysql."+getPath(pathName)+".SSLEnabled", false);
	}
	
	@Override
	public boolean isRequireSSL(String pathName)
	{
		return plugin.getYamlHandler().getConfig().getBoolean("Mysql."+getPath(pathName)+".SSLEnabled", false);
	}
}