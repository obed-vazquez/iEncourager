package org.whitedev.spigot.plugins.iencourager.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.whitedev.spigot.plugins.iencourager.IEncouragerConfigFile;
import org.whitedev.spigot.plugins.iencourager.util.Util;

public class VelocityChangesListener implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player)
            setExhaustionModifier((Player) event.getEntity());
    }
    
    @EventHandler 
    public void onPlayerMove(PlayerMoveEvent event){
        setExhaustionModifier(event.getPlayer());
    }
    
    public void setExhaustionModifier(Player player) {
        Location spawnLocation = Util.getSpawnLocation();
        Double distance = spawnLocation.distanceSquared(player.getLocation());
        //player.sendMessage("distance: " + distance );
	Integer limit=new Integer(IEncouragerConfigFile.getConfigValue("exhaustionLimit"));
        if (distance > limit) {
            Double modifier = ((distance - limit) / (limit*5));
	    Double maxModifier=new Double(IEncouragerConfigFile.getConfigValue("maxModifier"));
            player.setExhaustion(player.getExhaustion() * new Float(modifier > maxModifier ? maxModifier : modifier));
            //player.sendMessage("modifier: " + new Float(modifier>3?3:modifier));
        }
    }
}
