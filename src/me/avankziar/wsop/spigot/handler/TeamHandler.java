package me.avankziar.wsop.spigot.handler;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
	
	public static boolean changeToPlay(UUID uuid)
	{
		User user = lp.getUserManager().getUser(uuid);
		String primary = user.getPrimaryGroup();
		int i = 0;
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
			ChangingGroup cg = new ChangingGroup(0, uuid, "n", in.getGroupName(),
					primary.equals(in.getGroupName()), context.toArray(new String[context.size()]));
			plugin.getMysqlHandler().create(cg);
			user.data().remove(in);
			i++;
		}
		if(i == 0)
		{
			return false;
		}
		user.setPrimaryGroup(defaultGroup);
		InheritanceNode.Builder b = InheritanceNode.builder().group(defaultGroup);
		user.data().add(b.build());
		CompletableFuture.runAsync(() -> lp.getUserManager().saveUser(user));
		return true;
	}
	
	public static void changeToWork(UUID uuid)
	{
		ArrayList<ChangingGroup> cgs = plugin.getMysqlHandler().getFullList(
				new ChangingGroup(), "`id` ASC", "`player_uuid` = ?", uuid.toString());
		User user = lp.getUserManager().getUser(uuid);
		for(ChangingGroup cg : cgs)
		{
			if(cg.isPrimaryGroup())
			{
				user.setPrimaryGroup(cg.getChangedGroup());
			}
			InheritanceNode.Builder b = InheritanceNode.builder()
					.group(cg.getChangedGroup());
			for(Entry<String, String> e : cg.getContextMap().entrySet())
			{
				b.withContext(e.getKey(), e.getValue());
			}
			user.data().add(b.build());
		}
		user.data().remove(InheritanceNode.builder().group(defaultGroup).build());
		CompletableFuture.runAsync(() -> lp.getUserManager().saveUser(user));
		plugin.getMysqlHandler().deleteData(new ChangingGroup(), "`player_uuid` = ?", uuid.toString());
	}
}