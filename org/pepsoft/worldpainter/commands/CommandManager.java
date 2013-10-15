/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.commands;

/**
 *
 * @author pepijn
 */
public class CommandManager {
    public Command[] getCommands() {
        return COMMANDS.clone();
    }
    
    public static CommandManager getInstance() {
        return INSTANCE;
    }
    
    private static final CommandManager INSTANCE = new CommandManager();
    private static final Command[] COMMANDS = {new ImportWorld()};
}