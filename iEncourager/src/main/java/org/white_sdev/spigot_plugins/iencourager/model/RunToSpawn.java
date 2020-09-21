package org.white_sdev.spigot_plugins.iencourager.model;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import static org.bukkit.Bukkit.getServer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.white_sdev.spigot_plugins.iencourager.IEncouragerConfigFile;
import org.white_sdev.spigot_plugins.iencourager.exceptions.IEncouragerException;
import org.white_sdev.spigot_plugins.iencourager.util.Util;
import static org.white_sdev.white_validations.parameters.ParameterValidator.notNullValidation;

public class RunToSpawn {

    Logger logger=Logger.getLogger(RunToSpawn.class.getName());
    private JavaPlugin plugin;
    
    //<editor-fold defaultstate="collapsed" desc="SINGLETON">

    private static RunToSpawn singleton = null;

    public static RunToSpawn getInstance() {
	if (singleton == null) {
	    singleton = new RunToSpawn();
	}
	return singleton;
    }

    public static RunToSpawn getInstance(JavaPlugin plugin) {
	if (singleton == null) {
	    singleton = new RunToSpawn();
	}
	singleton.plugin = plugin;
	return singleton;
    }
    //</editor-fold>

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
    
    public void forcedLaunch(){
	try{
	    logger.info("::forcedLaunch() - Start: Launching event");
	    ForcedRunToSpawnThread forcedRunToSpawnThread = new ForcedRunToSpawnThread();
	    forcedRunToSpawnThread.plugin = this.plugin;
	    forcedRunToSpawnThread.start();
	    logger.info("::forcedLaunch() - Finished: Event launched in another thread");
	}catch(Exception ex){
	    throw new IEncouragerException("Impossible to launch the event due to an unexpected error.", ex);
	}
	
    }
    
    private RunToSpawnThread thread = null;
    private ForcedRunToSpawnThread forcedThread=null;

    private class RunToSpawnThread extends Thread {

	CommandSender sender;
	JavaPlugin plugin;

	RunToSpawnThread(CommandSender sender) {
	    this.sender = sender;
	}

	@Override
	public void run() {
	    try {
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
		launchAlertThreads(firstSleepMilli, minRewards, maxRewards, minDistance, maxDistance);

		try {
		    Calendar calendarToSleep = Calendar.getInstance();
		    firstSleepMilli += 10000;
		    try{
			calendarToSleep.add(Calendar.MILLISECOND,firstSleepMilli.intValue());
		    }catch(Exception e){
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
	 * Main method of the class which determines what will the rewards for the players be depending on their
	 * distance off the spawn. The players that are not full price winners and are not losers will be rewarded with
	 * a respective amount of gold of the distance they achieved.
	 *
	 * @param minRewards
	 * @param maxRewards
	 * @param minDistance how far can the player be from the spawn to be considered a full price winer
	 * @param maxDistance how close can the player be from the spawn and still be considered a loser
	 */
	public synchronized void calculateAndShowEventResults(Integer minRewards, Integer maxRewards, Long minDistance, Long maxDistance) {
	    Location spawnLocation = Util.getSpawnLocation();
	    logger.info("Delivering rewards of the event.");
	    Integer winnersCounter = 0;
	    for (Player player : Bukkit.getOnlinePlayers()) {
		//player.sendMessage("IEncourager message: Your position is being monitored");

		if (spawnLocation.distanceSquared(player.getLocation()) <= minDistance) {

		    player.sendMessage("You are one of the winners! You won: $$" + maxRewards);
		    try{
			Sounds.FIREWORK_LAUNCH.play(player);
			Sounds.FIREWORK_BLAST2.play(player);
			Sounds.FIREWORK_LARGE_BLAST.play(player);
			Sounds.FIREWORK_TWINKLE.play(player);

//			launchSound(player,Sound.ENTITY_FIREWORK_ROCKET_LAUNCH);
//			launchSound(player,Sound.ENTITY_FIREWORK_ROCKET_TWINKLE);
//			launchSound(player,Sound.ENTITY_PLAYER_LEVELUP);
		    }catch(Exception ex){
			logger.log(Level.WARNING, "Impossible to reproduce ANY sound due to an exception:{0}", ex);
		    }
		    winnersCounter++;
		    
		    //CORE
		    String commandToExecute = IEncouragerConfigFile.getConfigValue(player.getInventory().firstEmpty() == -1? //is Player's inventory full?
			    "serverCommandToDeposit":"serverCommandToGiveMoney") 
			    + player.getName() + " " + maxRewards;
		    
		    
		    try{
			getServer().dispatchCommand(getServer().getConsoleSender(),commandToExecute);
		    }catch(Exception ex){
			throw new IEncouragerException("En exception has ocurred while executing rewards command - have you implemented the economics plugin? Have you configured the command at \\plugins\\iEncourager\\config.yml?", ex);
		    }
		} else {
		    if (spawnLocation.distanceSquared(player.getLocation()) > maxDistance) {
			player.sendMessage("Sorry, you were too far from the spawn :( you are not getting the reward.  "
				+ "Remember that you get hungry faster if you are too far from the spawn.");
			    Sounds.FIREWORK_LAUNCH.play(player);
			    Sounds.FIREWORK_BLAST2.play(player);
//			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 2);
		    } else {
			Double distanceFromMin = spawnLocation.distanceSquared(player.getLocation()) - minDistance;
			Double walkedDistance = maxDistance - distanceFromMin;
			Integer deltaRewards = maxRewards - minRewards;
			Long deltaDistance = maxDistance - minDistance;
			//This is the main formula of the class
			Long partialReward = Math.round((walkedDistance * deltaRewards / deltaDistance));

			player.sendMessage("Sory you couldn't make it on time, but you were so close! "
				+ "Here is your reward: " + partialReward + " gold");
			player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_DEATH, 1, 2);

			winnersCounter++;
			String serverCommandToGiveMoneyParameter = IEncouragerConfigFile.
				getConfigValue("serverCommandToGiveMoney");
			getServer().dispatchCommand(getServer().getConsoleSender(),
				serverCommandToGiveMoneyParameter + player.getName() + " " + partialReward);
		    }

		}
	    }
	    logger.info("Delivering rewards of the event. " + winnersCounter + " Winner" + (winnersCounter == 1 ? "" : "s"));
	}
	
	public synchronized void launchSound(Player player, Sound sound){
	    notNullValidation(new Object[]{player,sound},"You need to provide a Player and a Sand to reproduce to him/her");
	    try{
		player.getWorld().playSound(player.getLocation(), sound, 1, 2);
	    }catch(Exception ex){
		logger.log(Level.WARNING, "Impossible to reproduce sound due to an exception:{0}", ex);
	    }
	}

	/**
	 * Launches 2 alerts. The first 1 minute before the event and the second 15 seconds with a countdown.
	 *
	 * @param firstSleepMilli How much time left for the event
	 * @param maxRewards	How much can the players get at this event
	 */
	public synchronized void launchAlertThreads(Long firstSleepMilli, Integer minRewards, Integer maxRewards, Long minDistance, Long maxDistance) {
	    new OneMinAlert(firstSleepMilli, maxRewards).start();
	    new FifteenSecAlert(firstSleepMilli, minRewards, maxRewards, minDistance, maxDistance).start();
	}

	/**
	 * Calculates how much for an odd hour of the day.
	 *
	 * @return Milliseconds left for the next odd hour of the day.
	 */
	private Long calculateNextSleep() {
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
		Bukkit.getServer().broadcastMessage(ChatColor.YELLOW +IEncouragerConfigFile.getConfigValue(
			"oneMinuteRemainingMessage") + ".. You can get up to: " + maxRewards + " gold.");
		Bukkit.getServer().broadcastMessage(ChatColor.BLUE +IEncouragerConfigFile.getConfigValue(
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

	Long firstSleepMilli;
	Integer minRewards;
	Integer maxRewards;
	Long minDistance;
	Long maxDistance;

	public FifteenSecAlert(Long firstSleepMilli, Integer minRewards, Integer maxRewards, Long minDistance, Long maxDistance) {
	    this.firstSleepMilli = firstSleepMilli;
	    this.minRewards = minRewards;
	    this.maxRewards = maxRewards;
	    this.minDistance = minDistance;
	    this.maxDistance = maxDistance;
	}

	@Override
	public void run() {
	    try {
		Calendar calendarToSleep = Calendar.getInstance();
		long millisToSleep=firstSleepMilli - (15 * 1000);
		if(millisToSleep<0)millisToSleep=0;
		
		calendarToSleep.setTimeInMillis(millisToSleep);
		logger.log(Level.INFO, "Sleeping the '15sec-alert' for: {0}mins {1}secs or {2}millisecs", new Object[]{calendarToSleep.get(Calendar.MINUTE), calendarToSleep.get(Calendar.SECOND),millisToSleep});
		
		if(millisToSleep>0){
		    Thread.sleep(millisToSleep);
		}else{
		    logger.warning("::FifteenSecAlert.run():: Starting inmmediatly! ");
		}

		logger.info("15sec Alert Broadcasting:");
		Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + IEncouragerConfigFile.
			getConfigValue("tenSecsRemainingMessage"));
		Bukkit.getServer().broadcastMessage(ChatColor.BLUE + IEncouragerConfigFile.
			getConfigValue("diezSegundosRestantes"));
		Thread.sleep((5 * 1000));
		for (int i = 10; i > 0; i--) {
		    Bukkit.getServer().broadcastMessage("Ends in: " + i + " second" + (i > 1 ? "s." : "."));
		    Thread.sleep(1000);
		}
		thread.calculateAndShowEventResults(minRewards, maxRewards, minDistance, maxDistance);

	    } catch (InterruptedException ex) {
		logger.log(Level.SEVERE, null, ex);
	    }
	}
    }
    
    
    private class ForcedRunToSpawnThread extends Thread{
	JavaPlugin plugin;

	@Override
	public void run(){
	    String eventTime = "hour";
	    Integer minRewards = new Integer(IEncouragerConfigFile.getConfigValue(eventTime + "EventMinRewards"));
	    Integer maxRewards = new Integer(IEncouragerConfigFile.getConfigValue(eventTime + "EventMaxRewards"));
	    Long minDistance = new Long(IEncouragerConfigFile.getConfigValue("winEventDistance"));
	    Long maxDistance = new Long(IEncouragerConfigFile.getConfigValue("outOfEventDistance"));
	    
	    logger.info( "::ForcedRunToSpawnThread.run(): Data read. Calling for alert to let players know and calculate the result");
	    
	    //sleeping for 15 +1 seconds
	    new FifteenSecAlert(16000l, minRewards, maxRewards, minDistance, maxDistance).start();
	}
    }

    
}
