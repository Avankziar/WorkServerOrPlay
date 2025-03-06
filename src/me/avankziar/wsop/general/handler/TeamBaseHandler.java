package me.avankziar.wsop.general.handler;

import java.util.List;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.luckperms.api.LuckPerms;

public class TeamBaseHandler
{
	public static LuckPerms lp;
	public static String defaultGroup = null;
	public static List<String> toConservatingGroup = null;
	
	public TeamBaseHandler(YamlDocument config, LuckPerms lp)
	{
		TeamBaseHandler.lp = lp;
		defaultGroup = config.getString("DefaultGroup", "default");
		toConservatingGroup = config.getStringList("ChangingGroups");
	}
}