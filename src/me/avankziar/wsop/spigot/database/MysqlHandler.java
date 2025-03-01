package me.avankziar.wsop.spigot.database;

import me.avankziar.wsop.general.database.MysqlBaseHandler;
import me.avankziar.wsop.spigot.WSOP;

public class MysqlHandler extends MysqlBaseHandler
{	
	public MysqlHandler(WSOP plugin)
	{
		super(plugin.getLogger(), plugin.getMysqlSetup());
	}
}
