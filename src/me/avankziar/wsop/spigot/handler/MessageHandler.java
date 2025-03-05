package me.avankziar.wsop.spigot.handler;

import org.bukkit.command.CommandSender;

import me.avankziar.wsop.general.assistance.ChatApiS;

public class MessageHandler 
{
	public static void sendMessage(CommandSender sender, String...msg)
	{
		for(String s : msg)
		{
			sender.spigot().sendMessage(ChatApiS.tl(s));
		}
	}
}