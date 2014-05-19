/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.tools;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.pepsoft.worldpainter.Configuration;

/**
 *
 * @author SchmitzP
 */
public class ScriptingTool {
    public static void main(String[] args) throws IOException, ClassNotFoundException, ScriptException {
        // Initialise logging
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$s] %5$s%n");
        
        // Load script
        File scriptFile = new File(args[0]);
        if (! scriptFile.isFile()) {
            throw new IllegalArgumentException(args[0] + " does not exist or is not a file");
        }
        String scriptFileName = scriptFile.getName();
        int p = scriptFileName.lastIndexOf('.');
        if (p == -1) {
            throw new IllegalArgumentException("Script file name " + scriptFileName + " has no extension");
        }
        String extension = scriptFileName.substring(p + 1);
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension(extension);
        if (scriptEngine == null) {
            throw new IllegalArgumentException("Script file language " + extension + " not supported");
        }
        String[] scriptArgs = new String[args.length - 1];
        System.arraycopy(args, 1, scriptArgs, 0, scriptArgs.length);
        scriptEngine.put(ScriptEngine.ARGV, scriptArgs);
        scriptEngine.put(ScriptEngine.FILENAME, scriptFileName);
        
        // Initialise WorldPainter
        Configuration config = Configuration.load();
        if (config == null) {
            config = new Configuration();
        }
        Configuration.setInstance(config);
        
        // Initialise script context
        ScriptingContext context = new ScriptingContext();
        Bindings bindings = scriptEngine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("wp", context);
        
        // Execute script
        scriptEngine.eval(new FileReader(scriptFile));
    }
}