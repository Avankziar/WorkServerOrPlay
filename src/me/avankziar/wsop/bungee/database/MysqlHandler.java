package me.avankziar.wsop.bungee.database;

import me.avankziar.wsop.bungee.WSOP;
import me.avankziar.wsop.general.database.MysqlBaseHandler;

public class MysqlHandler extends MysqlBaseHandler
{	
	public MysqlHandler(WSOP plugin)
	{
		super(plugin.getLogger(), plugin.getMysqlSetup());
	}
}