
package org.whitedev.spigot.plugins.iencourager.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.whitedev.spigot.plugins.iencourager.model.RunToSpawn;

public class StartCommand implements CommandExecutor{

    private JavaPlugin plugin;
    
    
    //<editor-fold defaultstate="collapsed" desc="SINGLETON">
    private StartCommand() {}
    private static class StartCommandHolder {
	private static final StartCommand INSTANCE = new StartCommand();
    }
    public static StartCommand getInstance() {
	return StartCommandHolder.INSTANCE;
    }
    //</editor-fold>
    
    public void startExecutorPlugin(JavaPlugin plugin){
        this.plugin=plugin;
        plugin.getCommand("scheduleIE").setExecutor(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
        try {
	    RunToSpawn.getSingleton().start(sender);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }
    
}
