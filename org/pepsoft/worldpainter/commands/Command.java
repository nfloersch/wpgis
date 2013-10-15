/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.commands;

import org.apache.commons.cli.Option;
import org.pepsoft.util.ProgressReceiver;

/**
 *
 * @author pepijn
 */
public interface Command {
    /**
     * Get the Apache Commons CLI option to use to invoke this command from the
     * command line.
     * 
     * @return The Apache Commons CLI option to use to invoke this command from
     * the command line.
     */
    Option getOption();
    
    /**
     * Execute the command.
     * 
     * @param context The context in which to execute the command.
     * @param progressReceiver The progress receiver to which to report progress
     * of long running commands.
     */
    void execute(CommandContext context, ProgressReceiver progressReceiver);
}