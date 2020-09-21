/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.white_sdev.spigot_plugins.iencourager.commands;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.white_sdev.spigot_plugins.iencourager.exceptions.IEncouragerException;
import org.white_sdev.spigot_plugins.iencourager.model.ExhaustionModifier;
import org.white_sdev.spigot_plugins.iencourager.model.RunToSpawn;
import org.white_sdev.white_validations.parameters.ParameterValidator;
import static org.white_sdev.white_validations.parameters.ParameterValidator.notNullValidation;

/**
 * Will launch an event
 *
 * @author <a href="mailto:obed.vazquez@gmail.com">Obed Vazquez</a>
 * @since Sep 20, 2020
 */
public class LaunchTestCommand implements CommandExecutor{

    Logger logger= Logger.getLogger(LaunchTestCommand.class.getName());
    public JavaPlugin plugin;
    
    //<editor-fold defaultstate="collapsed" desc="Singleton">
    /**
     * Class Constructor. Private constructor making a singleton with this.
     *
     * @author <a href="mailto:obed.vazquez@gmail.com">Obed Vazquez</a>
     * @since Sep 20, 2020
     * @param parameter The parameter to create the object.
     * @throws IllegalArgumentException - if the argument provided is null.
     */
    private LaunchTestCommand() {
    }

    private static class LaunchTestCommandHolder {

	private static final LaunchTestCommand INSTANCE = new LaunchTestCommand();
    }

    public static LaunchTestCommand getInstance() {
	return LaunchTestCommandHolder.INSTANCE;
    }
    //</editor-fold>

    public void startExecutorPlugin(JavaPlugin plugin) {
	Logger.getLogger(RunToSpawn.class.getName()).log(Level.FINEST, "::startExecutorPlugin(plugin) - Start: ");
	notNullValidation(plugin, "The parameter can't be null.");
	try {

	    this.plugin = plugin;
	    plugin.getCommand("launchTestIE").setExecutor(this);

	    Logger.getLogger(RunToSpawn.class.getName()).log(Level.FINEST, "::startExecutorPlugin(plugin) - Finish: ");
	} catch (Exception e) {
	    throw new IEncouragerException("Impossible to start Executor Plugin due to an unknown internal error.", e);
	}
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
	logger.log(Level.INFO, "::startExecutorPlugin(plugin) - Start: ");
        try {
	    
	    Boolean consoleIsCalling=isTheActivatorAConsole(sender);
	    logger.log(Level.INFO, "::startExecutorPlugin(plugin) : consoleIsCalling {0}", consoleIsCalling);
	    if(consoleIsCalling) RunToSpawn.getInstance().forcedLaunch();
	    
	    
	    logger.log(Level.FINEST, "::startExecutorPlugin(plugin) - Finish: ");
	    return true;
	    
	} catch (Exception e) {
	    throw new IEncouragerException("Error at starting the scheduling of the proccesses of the plugin due to an unexpected exception:", e);
	}
    }
    
    public Boolean isTheActivatorAConsole(CommandSender sender){
	try{
	    if (!(sender instanceof Player)) {
		if (sender != null) sender.sendMessage("Activating Event by Server");
		if (sender != null) sender.sendMessage("Activated");
		return true;
	    } else  return false;
	}catch(Exception ex){
	    throw new IEncouragerException("Impossible to validate if the caller is console or player due to an error: ", ex);
	}
	
    }

}
