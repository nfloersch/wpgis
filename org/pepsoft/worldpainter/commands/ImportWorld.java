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
public class ImportWorld implements Command {
    @Override
    public Option getOption() {
        return new Option("i", "import", false, "Import the landscape from an existing Minecraft map");
    }

    @Override
    public void execute(CommandContext context, ProgressReceiver progressReceiver) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}