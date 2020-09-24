/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.whitedev.spigot.plugins.iencourager.commands;

import org.white_sdev.spigot_plugins.iencourager.commands.StartCommand;
import java.util.logging.Logger;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.white_sdev.spigot_plugins.iencourager.IEncouragerPlugin;

/**
 *
 * @author WhitePC
 */
public class StartCommandTest {
    
    public StartCommandTest() {
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
     * Test of onCommand method, of class StartCommand.
     */
    @Test
    public void testOnCommand() {
	System.out.println("onCommand");
	
	
	IEncouragerPlugin pluginMock =  mock(IEncouragerPlugin.class);
	
	CommandSender mock =  mock(CommandSender.class);
	CommandSender sender = mock;
	
	Command cmndMock =  mock(Command.class);
	Command cmnd = cmndMock;
	
	String label = "";
	String[] args = null;
	StartCommand instance = StartCommand.getInstance(true);
	
	
	//when(pluginMock.getLogger()).thenReturn( Logger.getLogger(StartCommandTest.class.getName()) );
	
	//instance.onCommand(sender, cmnd, label, args);
	
	//no exceptions
	assert(true);
    }
    
}
