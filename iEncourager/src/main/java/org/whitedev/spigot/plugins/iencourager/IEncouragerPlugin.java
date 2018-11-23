package org.whitedev.spigot.plugins.iencourager;

import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.whitedev.spigot.plugins.iencourager.commands.StartCommand;
import org.whitedev.spigot.plugins.iencourager.commands.StopCommand;
import org.whitedev.spigot.plugins.iencourager.listeners.VelocityChangesListener;
import org.whitedev.spigot.plugins.iencourager.model.RunToSpawn;

public class IEncouragerPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
	try {
	    super.onEnable();
	    IEncouragerConfigFile.getInstance().loadOrCreateConfigFile(this);
	    StartCommand.getInstance().startExecutorPlugin(this);
	    StopCommand.getInstance().startExecutorPlugin(this);
	    getServer().getPluginManager().registerEvents(new VelocityChangesListener(), this);
	    RunToSpawn.getSingleton(this).start(getServer().getConsoleSender());
	} catch (Exception e) {
	    this.getLogger().log(Level.SEVERE, e.toString());
	}
    }

}
