package me.avankziar.wsop.velocity.cmdtree;

import java.io.IOException;

import com.velocitypowered.api.command.CommandSource;

import me.avankziar.wsop.general.cmdtree.ArgumentConstructor;
import me.avankziar.wsop.general.cmdtree.BaseConstructor;

public abstract class ArgumentModule
{
	public ArgumentConstructor argumentConstructor;

    public ArgumentModule(ArgumentConstructor argumentConstructor)
    {
       this.argumentConstructor = argumentConstructor;
       BaseConstructor.getArgumentMapVelo().put(argumentConstructor.getPath(), this);
    }
    
    //This method will process the command.
    public abstract void run(CommandSource sender, String[] args) throws IOException;

}
