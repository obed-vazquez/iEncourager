/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.whitedev.spigot.plugins.iencourager.model;

import org.white_sdev.spigot_plugins.iencourager.model.RunToSpawn;
import org.bukkit.configuration.file.FileConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.*;
import org.white_sdev.spigot_plugins.iencourager.IEncouragerConfigFile;
import org.white_sdev.spigot_plugins.iencourager.IEncouragerPlugin;
import org.white_sdev.spigot_plugins.iencourager.util.ConfigFile;


/**
 *
 * @author WhitePC
 */
public class RunToSpawnThreadTest {
    
    public RunToSpawnThreadTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getSingleton method, of class RunToSpawn.
     */
    @Test
    public void testRunToSpawnThread() {
	System.out.println("start");
	FileConfiguration mock =  mock(FileConfiguration.class);
	ConfigFile.iEnforcerFileConfig=mock;
	
	when(mock.get("worldName")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("worldName"));
	when(mock.get("serverCommandToGiveMoney")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("serverCommandToGiveMoney"));
	when(mock.get("exhaustionLimit")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("exhaustionLimit"));
	when(mock.get("maxModifier")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("maxModifier"));
	when(mock.get("oneMinuteRemainingMessage")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("oneMinuteRemainingMessage"));
	when(mock.get("tenSecsRemainingMessage")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("tenSecsRemainingMessage"));
	when(mock.get("winEventDistance")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("winEventDistance"));
	when(mock.get("outOfEventDistance")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("outOfEventDistance"));
	when(mock.get("weekEventMinRewards")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("weekEventMinRewards"));
	when(mock.get("weekEventMaxRewards")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("weekEventMaxRewards"));
	when(mock.get("dailyEventMinRewards")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("dailyEventMinRewards"));
	when(mock.get("dailyEventMaxRewards")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("dailyEventMaxRewards"));
	when(mock.get("hourEventMinRewards")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("hourEventMinRewards"));
	when(mock.get("hourEventMaxRewards")).thenReturn(IEncouragerConfigFile.getInstance(true).getConfig().get("hourEventMaxRewards"));
	
	
	
	RunToSpawn instance= RunToSpawn.getInstance(true);
	instance.start(null);
	
	
	assert(true);
    }

    
}
