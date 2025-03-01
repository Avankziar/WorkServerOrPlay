package me.avankziar.wsop.velocity.database;

import me.avankziar.wsop.general.database.MysqlBaseHandler;
import me.avankziar.wsop.velocity.WSOP;

public class MysqlHandler extends MysqlBaseHandler
{	
	public MysqlHandler(WSOP plugin)
	{
		super(plugin.getLogger(), plugin.getMysqlSetup());
	}
}
