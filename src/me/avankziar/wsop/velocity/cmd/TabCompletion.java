package me.avankziar.wsop.velocity.cmd;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

import me.avankziar.wsop.general.cmdtree.ArgumentConstructor;
import me.avankziar.wsop.general.cmdtree.CommandConstructor;

public class TabCompletion
{	
	public List<String> getTabs(CommandSource source, String cmd, String[] args)
	{
		if(!(source instanceof Player))
		{
			return List.of();
		}
		Player player = (Player) source;
		
		if(args.length < 0)
		{
			return List.of();
		}
		CommandConstructor cc = CommandConstructor.getCommandFromPath(cmd);
		if(cc == null)
		{
			cc = CommandConstructor.getCommandFromPath(cmd);
		}
		if(cc == null)
		{
			return List.of();
		}
		
		ArrayList<String> reList = new ArrayList<>();
		
		int length = args.length-1;
		ArrayList<ArgumentConstructor> aclist = cc.subcommands;
		ArrayList<String> OneArgumentBeforeList = new ArrayList<>();
		ArgumentConstructor lastAc = null;
		for(ArgumentConstructor ac : aclist)
		{
			OneArgumentBeforeList.add(ac.getName());
		}
		boolean isBreak = false;
		for(int i = 0; i <= length; i++)
		{				
			isBreak = false;
			for(int j = 0; j <= aclist.size()-1; j++)
			{
				ArgumentConstructor ac = aclist.get(j);				
				/*
				 * Wenn das aktuelle Argument NICHT leer ist, so loop durch die aclist und checke ob das Argument mit "xx" anfängt.
				 */
				ArrayList<ArgumentConstructor> c = countHowMuchAreStartsWithIgnoreCase(aclist, args[i]);
				if(c.size() > 1)
				{
					/*
					 * Wenn mehr als 1 Argument mit dem Chateintrag startet, so liefere eine Liste mit allen diesen zurück.
					 */
					ArrayList<String> notPassed = new ArrayList<>();
					for(String s : listIfArgumentIsnotEmpty(c, args[i], player))
					{
						if(s.equalsIgnoreCase(args[i]))
						{
							if(i+1 <= length)
							{
								/*
								 * Das Argument startet mit dem Argumentenname. aclist mit den Subargumenten vom Argument setzten.
								 * Sowie den innern Loop brechen.
								 */
								if(ac.subargument.size() == 0)
								{
									/*
									 * Keine Subargumente sind vorhanden, returne die tablist
									 */
									return getReturnTabList(ac.tabList.get(length), args[length]);
								}
								aclist = ac.subargument;
								isBreak = true;
								lastAc = ac;
								break;
							} else
							{
								/*
								 * Das Argument passt perfekt mit dem angegeben zusammen, nun nehme nur dieses!
								 */
								return new ArrayList<String>(Arrays.asList(args[i]));
							}
						} else
						{
							notPassed.add(s);
						}
					}
					if(notPassed.size() > 0)
					{
						return notPassed;
					}
				}
				if(ac.getName().toLowerCase().startsWith(args[i].toLowerCase()))
				{
					if(ac.getName().length() > args[i].length())
					{
						/*
						 * Wenn das Argument noch nicht vollständig ausgeschrieben ist, so return das.
						 */
						ArrayList<String> list = new ArrayList<>();
						list.add(ac.getName());
						return list;
					}
					/*
					 * Das Argument startet mit dem Argumentenname. aclist mit den Subargumenten vom Argument setzten.
					 * Sowie den innern Loop brechen.
					 */
					aclist = ac.subargument;
					isBreak = true;
					lastAc = ac;
					break;
				}
				if(j == aclist.size()-1)
				{
					/*
					 * Wenn keins der Argumente an der spezifischen Position gepasst hat, abbrechen. Und leere aclist setzten.
					 */
					aclist = new ArrayList<>();
				}
			}
			if(!isBreak)
			{
				if(lastAc != null)
				{
					return getReturnTabList(lastAc.tabList.get(length), args[length]);
					//Return leer, wenn die Tabliste nicht existiert! Aka ein halbes break;
				}
				if(i == length || aclist.isEmpty()) //Wenn das ende erreicht ist oder die aclist vorher leer gesetzt worden ist
				{
					break;
				}
			}
		}
		return reList;
	}
	
	private ArrayList<String> getReturnTabList(ArrayList<String> tabList, String argsi)
	{
		ArrayList<String> list = new ArrayList<>();
		if(tabList != null && argsi != null)
		{
			for(String s : tabList)
			{
				if(s.startsWith(argsi))
				{
					list.add(s);
				}
			}
		}
		Collections.sort(list);
		return list;
	}
	
	private ArrayList<String> listIfArgumentIsnotEmpty(ArrayList<ArgumentConstructor> subarg, String arg, Player player)
	{
		ArrayList<String> returnlist = new ArrayList<String>();
		for(ArgumentConstructor ac : subarg)
		{
			if(ac != null)
			{
				String acn = ac.getName();
				if(acn.toLowerCase().startsWith(arg.toLowerCase()))
				{
					if(player.hasPermission(ac.getPermission()))
					{
						if(!returnlist.contains(acn))
						{
							returnlist.add(ac.getName());
						}
					}
				}
			}
		}
		return returnlist;
	}
	
	private ArrayList<ArgumentConstructor> countHowMuchAreStartsWithIgnoreCase(ArrayList<ArgumentConstructor> subarg, String arg)
	{
		ArrayList<ArgumentConstructor> l = new ArrayList<>();
		for(ArgumentConstructor ac : subarg)
		{
			if(ac.getName().toLowerCase().startsWith(arg.toLowerCase()))
			{
				l.add(ac);
			}
		}
		return l;
	}
	
	public String[] AddToStringArray(String[] oldArray, String newString)
	{
	    String[] newArray = Arrays.copyOf(oldArray, oldArray.length+1);
	    newArray[oldArray.length] = newString;
	    return newArray;
	}
}