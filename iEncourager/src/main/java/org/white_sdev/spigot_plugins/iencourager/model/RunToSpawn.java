package org.white_sdev.spigot_plugins.iencourager.model;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.white_sdev.spigot_plugins.iencourager.IEncouragerConfigFile;
import org.white_sdev.spigot_plugins.iencourager.IEncouragerPlugin;
import org.white_sdev.spigot_plugins.iencourager.exceptions.IEncouragerException;
import org.white_sdev.spigot_plugins.iencourager.util.Util;
import static org.white_sdev.white_validations.parameters.ParameterValidator.notNullValidation;

public class RunToSpawn {

    public static Logger logger;
    private final JavaPlugin plugin;

    //<editor-fold defaultstate="collapsed" desc="Provisional for testing">
    
    public static RunToSpawn getInstance(boolean testing) {
	if (testing == true) SINGLETON = new RunToSpawn(testing);
	return SINGLETON;
    }
    
    
    private RunToSpawn(boolean testing){
	if(testing==false) throw new UnsupportedOperationException("Provisional testing-only operation.");
	plugin=null;
    }
    
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="SINGLETON">
    private static RunToSpawn SINGLETON = null;

    private RunToSpawn(JavaPlugin plugin){
	notNullValidation(plugin,"You must provide a plugin reference to instanciate the event");
	if(plugin.getLogger()==null){
	    logger = Logger.getLogger(RunToSpawn.class.getName());
	    logger.warning("Class initialized without plugin link. Loggers will have no parent");
	}else{
	    logger=plugin.getLogger();
	}
	this.plugin=plugin;
    }
    
    public static RunToSpawn getInstance() {
	if (SINGLETON == null) 
	    throw new IEncouragerException("You can only retrieve a singleton with no plugin once it has been instanciated. "
		    + "You must provide a plugin reference to instanciate the event for the first time");
	return SINGLETON;
    }

    public static RunToSpawn getInstance(JavaPlugin plugin) {
	if (SINGLETON == null) {
	    SINGLETON = new RunToSpawn(plugin);
	}
	return SINGLETON;
    }
    //</editor-fold>
    
    private RunToSpawnThread thread = null;
    private final ForcedRunToSpawnThread forcedThread = null;
    
    
    //<editor-fold defaultstate="collapsed" desc="Methods">
    
    //<editor-fold defaultstate="collapsed" desc="Commands">

    /**
     * Launches the whole Event process.
     *
     * @param sender who is launching the process. So the plug-in can answer to it/h(er)im
     */
    public void start(CommandSender sender) {
	if (!(sender instanceof Player)) {
	    if (sender != null) {
		sender.sendMessage("Activating Event by Server");
	    }
	    thread = new RunToSpawnThread(sender);
	    thread.plugin = this.plugin;
	    thread.start();
	    if (sender != null) {
		sender.sendMessage("Activated");
	    }
	} else {
	    Player player = (Player) sender;
	    player.sendMessage("Activating by Player");
	    thread = new RunToSpawnThread(sender);
	    thread.plugin = this.plugin;
	    thread.start();
	    player.sendMessage("Activated");
	}
    }

    /**
     * Interrupts the event process.
     *
     * @param sender who is stopping the process. So the plug-in can answer to it/h(er)im
     */
    public void stop(CommandSender sender) {
	if (!(sender instanceof Player)) {
	    sender.sendMessage("Deactivating Event by Server");
	    thread.interrupt();
	    thread = null;
	    sender.sendMessage("Deactivated");
	} else {
	    Player player = (Player) sender;
	    player.sendMessage("Deactivating by Player");
	    thread.interrupt();
	    thread = null;
	    player.sendMessage("Deactivated");
	}
    }

    public void forcedLaunch() {
	try {
	    logger.info("::forcedLaunch() - Start: Launching event");
	    ForcedRunToSpawnThread forcedRunToSpawnThread = new ForcedRunToSpawnThread();
	    forcedRunToSpawnThread.plugin = this.plugin;
	    forcedRunToSpawnThread.start();
	    logger.info("::forcedLaunch() - Finished: Event launched in another thread");
	} catch (Exception ex) {
	    throw new IEncouragerException("Impossible to launch the event due to an unexpected error.", ex);
	}

    }

    //</editor-fold>
        
    /**
     * Main method of the class which determines what will the rewards for the players be depending on their distance off the spawn. 
     * The players that are not full price winners and are not losers (0 price) will be rewarded with a respective amount of gold of the distance they achieved.
     *
     * @param minRewards
     * @param maxRewards
     * @param minDistance how far can the player be from the spawn to be considered a full price winer
     * @param maxDistance how close can the player be from the spawn and still be considered a loser
     */
    public static synchronized void calculateAndShowEventResults(IEncouragerPlugin plugin, Integer minRewards, Integer maxRewards, Long minDistance, Long maxDistance) {
	logger.info(":: calculateAndShowEventResults(minRewards,maxRewards,minDistance,maxDistance):: - Start.");
	if( minRewards==null) minRewards=0;
	Location spawnLocation = Util.getSpawnLocation();
	logger.info(":: calculateAndShowEventResults(minRewards,maxRewards,minDistance,maxDistance):: Delivering rewards of the event.");
	try {
	    Integer winnersCounter = 0;
	    for (Player player : Bukkit.getOnlinePlayers()) {
		//player.sendMessage("IEncourager message: Your position is being monitored");

		if (spawnLocation.distanceSquared(player.getLocation()) <= minDistance) {

		    player.sendMessage("You are one of the winners! You won: $$" + maxRewards);
		    player.sendMessage("Eres uno de los ganadores! Ganaste: $$" + maxRewards);

		    winnersCounter++;

		    playSuccessSounds(player);
		    tryToPay(plugin, player, maxRewards);

		} else {
		    if (spawnLocation.distanceSquared(player.getLocation()) > maxDistance) {
			player.sendMessage("Sorry, you were too far from the spawn :( you are not getting the reward.  "
				+ "Remember that you get hungry faster if you are too far from the spawn.");
			player.sendMessage("&eLo siento, estas demaciado lejos del spawn para obtener el premio.  "
				+ "Recuerda que entre más lejos te encuentres del spawn provocará que tengas hambre más rápido.");
			playPatrialSuccessSounds(player);

		    } else {
			Double distanceFromMin = spawnLocation.distanceSquared(player.getLocation()) - minDistance;
			Double walkedDistance = maxDistance - distanceFromMin;
			Integer deltaRewards = maxRewards - minRewards;
			Long deltaDistance = maxDistance - minDistance;
			//This is the main formula of the class
			Long partialReward = Math.round((walkedDistance * deltaRewards / deltaDistance));

			winnersCounter++;

			player.sendMessage("Sory you couldn't make it on time, but you were so close! "
				+ "Here is your reward:");
			player.sendMessage("&eLo siento, no llegaste a tiempo, pero estubiste muy cerca! "
				+ "Aquí esta tu premio: \n$$" + partialReward);
			playFailureSounds(player);
			tryToPay(plugin, player, partialReward);
		    }

		}
	    }

	    logger.log(Level.INFO, ":: calculateAndShowEventResults(minRewards,maxRewards,minDistance,maxDistance):: Rewards Delivered. {0} Winner{1}", new Object[]{winnersCounter, winnersCounter == 1 ? "" : "s"});

	} catch (Exception ex) {
	    throw new IEncouragerException("Impossible to calculate rewards or show event results due to an unexpected exception", ex);
	}
    }
    
    //<editor-fold defaultstate="collapsed" desc="Sounds Methods">
    
    public static synchronized void playSound(Player player, Sounds sound) {
	logger.finer("::launchSound(player) - Start: ");
	notNullValidation(new Object[]{player, sound}, "The player and sound to reproduce must be provided to reproduce a sound.");
	try {

	    sound.play(player);
	    logger.finer("::launchSound(player) - Finish: ");

	} catch (Exception e) {
	    throw new IEncouragerException("Impossible to reproduce the sound due to an unknown internal exception.", e);
	}
    }

    public static synchronized void playSuccessSounds(Player player) {
	logger.finer("::playSuccessSounds(player) - Start: ");
	notNullValidation(player, "the player to reproduce the sound to, must provided.");
	try {

	    playSound(player, Sounds.FIREWORK_LAUNCH);
	    playSound(player, Sounds.FIREWORK_BLAST2);
	    playSound(player, Sounds.FIREWORK_LARGE_BLAST);
	    playSound(player, Sounds.FIREWORK_TWINKLE);

	    logger.finer("::playSuccessSounds(player) - Finish: ");
	} catch (Exception ex) {
	    logger.log(Level.WARNING, "Impossible to reproduce ANY sound due to an exception:{0}", ex);
	}
    }

    public static synchronized void playPatrialSuccessSounds(Player player) {
	logger.finer("::playPatrialSuccessSounds(player) - Start: ");
	notNullValidation(player, "the player to reproduce the sound to, must provided.");
	try {

	    playSound(player, Sounds.FIREWORK_LAUNCH);
	    playSound(player, Sounds.FIREWORK_BLAST2);

	    logger.finer("::playPatrialSuccessSounds(player) - Finish: ");
	} catch (Exception ex) {
	    logger.log(Level.WARNING, "Impossible to reproduce ANY sound due to an exception:{0}", ex);
	}
    }

    public static synchronized void playFailureSounds(Player player) {
	logger.finer("::playFailureSounds(player) - Start: ");
	notNullValidation(player, "the player to reproduce the sound to, must provided.");
	try {

	    //ENTITY_PLAYER_DEATH
	    playSound(player, Sounds.HURT_FLESH);

	    logger.finer("::playFailureSounds(player) - Finish: ");
	} catch (Exception ex) {
	    logger.log(Level.WARNING, "Impossible to reproduce ANY sound due to an exception:{0}", ex);
	}
    }

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Rewards Methods">

    public static synchronized void payPrice(IEncouragerPlugin plugin, Player player, Long rewards) {
	logger.finer("::payPrice(player,rewards) - Start: ");
	if (rewards == null) {
	    logger.warning("::payPrice(parameter): if you want to pay money to a player the ammount must be specified (Ignoring the payout)");
	    return;
	}
	try {
	    //should I use plugin instance instead of Class reference? if not, the plugin parameter is not required in a lot of methods :S
	    Economy economy=IEncouragerPlugin.getEconomy();
	    if(economy!=null){
		Double balance=economy.getBalance(player);
		player.sendMessage(ChatColor.YELLOW +"Previous Balance - "+ChatColor.BLUE +"Nuevo Balance: "+ChatColor.WHITE +"$$"+ChatColor.GREEN+balance.floatValue());
		
		economy.depositPlayer(player, rewards);
		balance=economy.getBalance(player);
		
		player.sendMessage(ChatColor.YELLOW +"New Balance - "+ChatColor.BLUE +"Nuevo Balance: "+ChatColor.WHITE +"$$"+ChatColor.GREEN+balance.floatValue());
		
	    }else{
		logger.info("::payPrice(player,rewards): Not Economy plugin detected, trying dangerous custom command.");
		
		String commandToExecute = IEncouragerConfigFile.getConfigValue(player.getInventory().firstEmpty() == -1
			? //is Player's inventory full?
			"serverCommandToDeposit" : "serverCommandToGiveMoney")
			+ player.getName() + " " + rewards;
		
		int taskID = Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
		    logger.finer("::run(parameter) - Start: ");
		    try {
			getServer().dispatchCommand(getServer().getConsoleSender(), commandToExecute);
			logger.finer("::run(parameter) - Finish: ");
		    } catch (Exception e) {
			throw new RuntimeException("Impossible to complete the operation due to an unknown internal error.", e);
		    }
		}, 20);//1 sec=20ticks
	    }

	    logger.finer("::payPrice(player,rewards) - Finish: ");

	} catch (Exception e) {
	    throw new IEncouragerException("En exception has ocurred while executing rewards command - have you implemented the economics plugin? "
		    + " you configured the command at \\plugins\\iEncourager\\config.yml?.", e);
	}
    }

    /**
     * Manages logistics when calling {@link #payPrice(org.bukkit.entity.Player, java.lang.Long) }.
     * @param player
     * @param ammount 
     */
    public static synchronized void tryToPay(IEncouragerPlugin plugin, Player player, Number ammount) {
	logger.fine("::tryToPay(player) - Start: ");
	notNullValidation(player, "The parameter can't be null.");
	try {
	    try {
		payPrice(plugin, player, ammount.longValue());
	    } catch (IEncouragerException ex) {
		Integer code = new Random().nextInt((9999 - 100) + 1) + 10;
		logger.log(Level.SEVERE, "An exception has ocurred when paying $${0} player: {1}.Pay manmually with code: {2}. Will try to ignore and keep delivering payouts!, Exception: {3} \n {4}",
			new Object[]{ammount, player, code,ex,ex.getCauses()});
		player.sendMessage("iEncourager couldn't pay you due to an exception please report it to an admin. To claim your manual reward provide this code: " + code);
		player.sendMessage("iEncourager no pudo pagarte debido a un error, favor de reportarlo a un admin. Para reclamar tu premio entrega este codigo: " + code);
	    }
	    logger.fine("::tryToPay(player) - Finish: ");

	} catch (Exception e) {
	    throw new IEncouragerException("Impossible to complete the payout due to an unknown internal error.", e);
	}
    }

    
    //</editor-fold>
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Threads">

    private class RunToSpawnThread extends Thread {

	CommandSender sender;
	JavaPlugin plugin;

	RunToSpawnThread(CommandSender sender) {
	    this.sender = sender;
	}

	@Override
	public synchronized void run() {
	    try { // this should be done with this instead https://www.spigotmc.org/threads/wait-30-seconds-to-run-code-for-individual-players.62317/
		//Waiting for the server to load (for logging purposes, you can eliminate the wait)...
		Thread.sleep(20 * 1000);
	    } catch (InterruptedException ex) {
		logger.log(Level.SEVERE, null, ex);
	    }
	    String eventTime;

	    do {

		logger.info("::RunToSpawnThread.run() : Starting time of event process");
		Calendar nextHour = Calendar.getInstance();
		nextHour.setTimeInMillis(Calendar.getInstance().getTimeInMillis() + (60000 * 60));
		if (nextHour.get(Calendar.HOUR_OF_DAY) == 19 || nextHour.get(Calendar.HOUR_OF_DAY) == 18) {
		    if (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) {
			eventTime = "week";
		    } else {
			eventTime = "daily";
		    }
		} else {
		    eventTime = "hour";
		}

		Integer minRewards = new Integer(IEncouragerConfigFile.getConfigValue(eventTime + "EventMinRewards"));
		Integer maxRewards = new Integer(IEncouragerConfigFile.getConfigValue(eventTime + "EventMaxRewards"));
		Long minDistance = new Long(IEncouragerConfigFile.getConfigValue("winEventDistance"));
		Long maxDistance = new Long(IEncouragerConfigFile.getConfigValue("outOfEventDistance"));

		Long firstSleepMilli = calculateNextSleep();
		//CORE Call
		launchAlertThreads(firstSleepMilli, minRewards, maxRewards, minDistance, maxDistance);

		try {
		    Calendar calendarToSleep = Calendar.getInstance();
		    firstSleepMilli += 10000;
		    try {
			calendarToSleep.add(Calendar.MILLISECOND, firstSleepMilli.intValue());
		    } catch (Exception e) {
			logger.warning("::RunToSpawnThread.run() : Problem formating log data, the next log is corrupted");
		    }
		    logger.log(Level.INFO, "Sleeping main event process will awake on: {0}mins {1}secs. For recalculations", new Object[]{calendarToSleep.get(Calendar.MINUTE), calendarToSleep.get(Calendar.SECOND)});
		    Thread.sleep(firstSleepMilli);

		} catch (InterruptedException ex) {
		    throw new IEncouragerException("Problem sleeping the event, the alerts are in a separate thread", ex);
		}
	    } while (true);
	}

	/**
	 * Launches 2 alerts. The first 1 minute before the event and the second 15 seconds with a countdown.
	 *
	 * @param firstSleepMilli How much time left for the event
	 * @param maxRewards	How much can the players get at this event
	 */
	public synchronized void launchAlertThreads(Long firstSleepMilli, Integer minRewards, Integer maxRewards, Long minDistance, Long maxDistance) {
	    new OneMinAlert(firstSleepMilli, maxRewards).start();
	    new FifteenSecAlert((IEncouragerPlugin) plugin, firstSleepMilli, minRewards, maxRewards, minDistance, maxDistance).start();
	}

	/**
	 * Calculates how much for an odd hour of the day.
	 *
	 * @return Milliseconds left for the next odd hour of the day.
	 */
	private synchronized Long calculateNextSleep() {
	    GregorianCalendar gregorian = (GregorianCalendar) Calendar.getInstance();
	    gregorian.set(Calendar.getInstance().get(Calendar.YEAR),
		    Calendar.getInstance().get(Calendar.MONTH),
		    Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
		    Calendar.getInstance().get(Calendar.HOUR_OF_DAY), 0, 0);
	    Long gregorianTimeInMillis = gregorian.getTimeInMillis();

	    Long millisecondsAfterHour = Calendar.getInstance().getTimeInMillis() - gregorianTimeInMillis;
	    Long millisecondsToSleep = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) % 2 == 0 ? (60000 * 60) - millisecondsAfterHour : (120000 * 60) - millisecondsAfterHour;

	    return millisecondsToSleep;
	}

    }

    private class OneMinAlert extends Thread {

	Long firstSleepMilli;
	Integer maxRewards;

	public OneMinAlert(Long firstSleepMilli, Integer maxRewards) {
	    this.firstSleepMilli = firstSleepMilli;
	    this.maxRewards = maxRewards;
	}

	@Override
	public void run() {
	    try {
		Calendar calendarToSleep = Calendar.getInstance();
		calendarToSleep.setTimeInMillis(firstSleepMilli - (60 * 1000));
		logger.info("Sleeping 1min alert for: " + calendarToSleep.get(Calendar.MINUTE) + "mins " + calendarToSleep.get(Calendar.SECOND) + "secs");
		Thread.sleep(firstSleepMilli - (60 * 1000));
		logger.info("Awake sending 1 minute left Alert to server!");
		Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + IEncouragerConfigFile.getConfigValue(
			"oneMinuteRemainingMessage") + ".. You can get up to: " + maxRewards + " gold.");
		Bukkit.getServer().broadcastMessage(ChatColor.BLUE + IEncouragerConfigFile.getConfigValue(
			"unMinutoRestante") + ".. Puedes obtener hasta: " + maxRewards + " de oro.");
		Bukkit.getOnlinePlayers().forEach((player) -> {
		    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
		});

	    } catch (InterruptedException ex) {
		logger.log(Level.SEVERE, null, ex);
	    }
	}
    }

    private class FifteenSecAlert extends Thread {

	IEncouragerPlugin plugin;
	Long firstSleepMilli;
	Integer minRewards;
	Integer maxRewards;
	Long minDistance;
	Long maxDistance;

	public FifteenSecAlert(IEncouragerPlugin plugin, Long firstSleepMilli, Integer minRewards, Integer maxRewards, Long minDistance, Long maxDistance) {
	    this.firstSleepMilli = firstSleepMilli;
	    this.minRewards = minRewards;
	    this.maxRewards = maxRewards;
	    this.minDistance = minDistance;
	    this.maxDistance = maxDistance;
	    this.plugin=plugin;
	}

	@Override
	public void run() {
	    try {
		Calendar calendarToSleep = Calendar.getInstance();
		long millisToSleep = firstSleepMilli - (15 * 1000);
		if (millisToSleep < 0) millisToSleep = 0;

		calendarToSleep.setTimeInMillis(millisToSleep);
		logger.log(Level.INFO, ":: FifteenSecAlert#run():: Sleeping the *15sec-alert* for: {0}mins {1}secs or {2}millisecs", new Object[]{calendarToSleep.get(Calendar.MINUTE), calendarToSleep.get(Calendar.SECOND), millisToSleep});

		if (millisToSleep > 0) {
		    Thread.sleep(millisToSleep);
		} else {
		    logger.warning(":: FifteenSecAlert#run():: Starting inmmediatly! ");
		}

		logger.info(":: FifteenSecAlert#run():: 15sec Alert Broadcasting:");
		Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + IEncouragerConfigFile.
			getConfigValue("tenSecsRemainingMessage"));
		Bukkit.getServer().broadcastMessage(ChatColor.BLUE + IEncouragerConfigFile.
			getConfigValue("diezSegundosRestantes"));
		Thread.sleep((5 * 1000));
		for (int i = 10; i > 0; i--) {
		    Bukkit.getServer().broadcastMessage("Ends in: " + i + " second" + (i > 1 ? "s." : "."));
		    Thread.sleep(1000);
		}
		calculateAndShowEventResults(plugin, minRewards, maxRewards, minDistance, maxDistance);

	    } catch (InterruptedException ex) {
		logger.log(Level.SEVERE, null, ex);
	    }
	}
    }

    private class ForcedRunToSpawnThread extends Thread {

	JavaPlugin plugin;

	@Override
	public void run() {
	    String eventTime = "hour";
	    Integer minRewards = new Integer(IEncouragerConfigFile.getConfigValue(eventTime + "EventMinRewards"));
	    Integer maxRewards = new Integer(IEncouragerConfigFile.getConfigValue(eventTime + "EventMaxRewards"));
	    Long minDistance = new Long(IEncouragerConfigFile.getConfigValue("winEventDistance"));
	    Long maxDistance = new Long(IEncouragerConfigFile.getConfigValue("outOfEventDistance"));

	    logger.info("::ForcedRunToSpawnThread.run(): Data read. Calling for alert to let players know and calculate the result");

	    //sleeping for 15 +1 seconds
	    new FifteenSecAlert((IEncouragerPlugin) plugin, 16000l, minRewards, maxRewards, minDistance, maxDistance).start();
	}
    }

    //</editor-fold>
}
