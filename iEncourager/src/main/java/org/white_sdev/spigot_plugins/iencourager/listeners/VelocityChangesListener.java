package org.white_sdev.spigot_plugins.iencourager.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.white_sdev.spigot_plugins.iencourager.IEncouragerConfigFile;
import org.white_sdev.spigot_plugins.iencourager.util.Util;

public class VelocityChangesListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player)
            setExhaustionModifier((Player) event.getEntity());
    }
    
    @EventHandler 
    public void onPlayerMove(PlayerMoveEvent event) {
	setExhaustionModifier(event.getPlayer());
    }

    public void setExhaustionModifier(Player player) {
	Location spawnLocation = Util.getSpawnLocation();
	
	//if the player is in the real world
	if( !(player.getWorld().getName().endsWith("_nether") || player.getWorld().getName().endsWith("_end")) ) {
	    Double distance = spawnLocation.distanceSquared(player.getLocation());

	    Integer distanceLowerLimit = new Integer(IEncouragerConfigFile.getConfigValue("exhaustionModifierStartDistance"));
	    Integer distanceUpperLimit = new Integer(IEncouragerConfigFile.getConfigValue("exhaustionModifierEndDistance"));

	    Float initialExhaustion = player.getExhaustion();
	    Integer exhaustionSafeLimit = 8;

	    if (distance > distanceLowerLimit) {
		if (initialExhaustion < exhaustionSafeLimit) {

		    if (player.getFoodLevel() > 0) {

			Double maxModifier = new Double(IEncouragerConfigFile.getConfigValue("maxModifier"));

			Double distanceTraveledAfterLimit = distance - distanceLowerLimit;
			//Core Formula that reduces drastically the modifier and makes it start in 1 (to not reduce the exhaustion) to the limit in relation to the distance
			Double calculatedModifier = ((distanceTraveledAfterLimit / (distanceUpperLimit - distanceLowerLimit)) * (maxModifier - 1)) + 1;

			//checks that the modifier is not over the distanceLowerLimit
			Double modifier = calculatedModifier > maxModifier ? maxModifier : calculatedModifier;
			Double fixedMoodifier = 1 + (modifier / 80);

			Float modifiedExhaustion = initialExhaustion * new Float(fixedMoodifier);

			player.setExhaustion(modifiedExhaustion);

			//player.sendMessage("Modified - mod: " + round(modifier) + " -- initialEx: " + round(initialExhaustion) + " -- modifEx: " + round(modifiedExhaustion));
		    } else {
			//player.sendMessage("Not Modifing - Food level=0 -- initialEx: " + round(initialExhaustion));
		    }
		} else {
		    //This shoul'nt happen. lo hace injugable
		    player.sendMessage("ERROR: Exhaustion over safe limit:" + exhaustionSafeLimit + " Please Contact an administrator ");
		}
	    } else {
		//player.sendMessage("Not Modifing - distance under " + distanceLowerLimit + " -- distance: " + round(distance)+ " -- initialEx: " + round(initialExhaustion) );
	    }
	}
    }
    
    Double round(Double i){
	return ((double) Math.round(i * 100) / 100);
    }
    
    Double round(Float i){
	return ((double) Math.round(i * 100) / 100);
    }
}
