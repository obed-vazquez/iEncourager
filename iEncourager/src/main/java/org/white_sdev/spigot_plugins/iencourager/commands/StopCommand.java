package org.white_sdev.spigot_plugins.iencourager.commands;

import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.white_sdev.spigot_plugins.iencourager.exceptions.IEncouragerException;
import org.white_sdev.spigot_plugins.iencourager.model.ExhaustionModifier;
import org.white_sdev.spigot_plugins.iencourager.model.RunToSpawn;
import static org.white_sdev.white_validations.parameters.ParameterValidator.notNullValidation;

public class StopCommand implements CommandExecutor {

    public static Logger logger;
    private JavaPlugin plugin;

    //<editor-fold defaultstate="collapsed" desc="Provisional for testing">
    
    public static StopCommand getInstance(boolean testing) {
	if (testing == true) SINGLETON = new StopCommand(testing);
	return SINGLETON;
    }
    
    
    private StopCommand(boolean testing){
	if(testing==false) throw new UnsupportedOperationException("Provisional testing-only operation.");
	plugin=null;
    }
    
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="SINGLETON">
    private static StopCommand SINGLETON = null;

    /**
     * Class Constructor. Private constructor making a singleton with this.
     *
     * @author <a href="mailto:obed.vazquez@gmail.com">Obed Vazquez</a>
     * @since Sep 20, 2020
     * @param parameter The parameter to create the object.
     * @throws IllegalArgumentException - if the argument provided is null.
     */
    private StopCommand(JavaPlugin plugin){
	notNullValidation(plugin,"You must provide a plugin reference to instanciate the event");
	if(plugin.getLogger()==null){
	    logger = Logger.getLogger(RunToSpawn.class.getName());
	    logger.warning("Class initialized without plugin link. Loggers will have no parent");
	}else{
	    logger=plugin.getLogger();
	}
	this.plugin=plugin;
    }
    
    public static StopCommand getInstance() {
	if (SINGLETON == null) 
	    throw new IEncouragerException("You can only retrieve a singleton with no plugin once it has been instanciated. "
		    + "You must provide a plugin reference to instanciate the event for the first time");
	return SINGLETON;
    }

    public static StopCommand getInstance(JavaPlugin plugin) {
	if (SINGLETON == null) {
	    SINGLETON = new StopCommand(plugin);
	}
	return SINGLETON;
    }
    //</editor-fold>

    public void startExecutorPlugin(JavaPlugin plugin) {
	this.plugin = plugin;
	plugin.getCommand("stopIE").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
	try {
	    RunToSpawn.getInstance().stop(sender);
	    ExhaustionModifier.getInstance().stop(sender);
	    return true;
	} catch (Exception e) {
	    return false;
	}
    }

}
