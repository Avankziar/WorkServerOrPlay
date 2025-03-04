package me.avankziar.wsop.spigot.handler;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import me.avankziar.wsop.general.handler.TeamBaseHandler;
import me.avankziar.wsop.general.objects.ChangingGroup;
import me.avankziar.wsop.spigot.WSOP;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;

public class TeamHandler extends TeamBaseHandler
{
	public static WSOP plugin;
	
	public TeamHandler(WSOP plugin, LuckPerms lp)
	{
		super(plugin.getYamlHandler().getConfig(), lp);
		TeamHandler.plugin = plugin;
	}
	
	public static boolean isGroupToChange(String groupname)
	{
		for(String group : toConservatingGroup)
		{
			if(group.equalsIgnoreCase(groupname))
			{
				return true;
			}
		}
		return false;
	}
	
	public static void changeToPlay(Player player, UUID uuid)
	{
		User user = lp.getUserManager().getUser(uuid);
		String primary = user.getPrimaryGroup();
		for(InheritanceNode in : user.resolveInheritedNodes(QueryOptions.nonContextual()).stream()
				.filter(NodeType.INHERITANCE::matches)
				.filter(x -> !x.hasExpiry())
				.map(NodeType.INHERITANCE::cast)
				.collect(Collectors.toList()))
		{
			if(!isGroupToChange(in.getGroupName()))
			{
				continue;
			}
			ArrayList<String> context = new ArrayList<>();
			in.getContexts().forEach(x -> context.add(x.getKey()+"="+x.getValue()));
			ChangingGroup cg = new ChangingGroup(0, uuid, "", in.getGroupName(),
					primary.equals(in.getGroupName()), context.toArray(new String[context.size()]));
			plugin.getMysqlHandler().create(cg);
			user.data().remove(in);
		}
		InheritanceNode.Builder b = InheritanceNode.builder().group(defaultGroup);
		user.data().add(b.build());
		CompletableFuture.runAsync(() -> lp.getUserManager().saveUser(user));
	}
	
	public static void changeToWork(Player player)
	{
		
	}
}