package me.avankziar.wsop.bungee.cmdtree;

import java.io.IOException;

import me.avankziar.wsop.general.cmdtree.ArgumentConstructor;
import me.avankziar.wsop.general.cmdtree.BaseConstructor;
import net.md_5.bungee.api.CommandSender;

public abstract class ArgumentModule
{
	public ArgumentConstructor argumentConstructor;

    public ArgumentModule(ArgumentConstructor argumentConstructor)
    {
       this.argumentConstructor = argumentConstructor;
       BaseConstructor.getArgumentMapBungee().put(argumentConstructor.getPath(), this);
    }
    
    //This method will process the command.
    public abstract void run(CommandSender sender, String[] args) throws IOException;

}
