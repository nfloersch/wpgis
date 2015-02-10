/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.tools.scripts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPOutputStream;
import org.pepsoft.worldpainter.App;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.World2;

/**
 *
 * @author SchmitzP
 */
public class SaveWorldOp extends AbstractOperation<Void> {
    public SaveWorldOp(World2 world) throws ScriptException {
        if (world == null) {
            throw new ScriptException("world may not be null");
        }
        this.world = world;
    }
    
    public SaveWorldOp toFile(String fileName) {
        this.fileName = fileName;
        return this;
    }
    
    @Override
    public Void go() throws ScriptException {
        File file = new File(fileName);
        File dir = file.getParentFile();
        if (! dir.isDirectory()) {
            throw new ScriptException("Destination directory " + dir + " does not exist or is not a directory");
        }
        if (! dir.canWrite()) {
            throw new ScriptException("Access denied to destination directory " + dir);
        }
        
        try {
            Configuration config = Configuration.getInstance();
            if ((config.getWorldFileBackups() > 0) && file.isFile()) {
                for (int i = config.getWorldFileBackups(); i > 0; i--) {
                    File nextBackupFile = (i > 1) ? App.getBackupFile(file, i - 1) : file;
                    if (nextBackupFile.isFile()) {
                        File backupFile = App.getBackupFile(file, i);
                        if (backupFile.isFile()) {
                            if (! backupFile.delete()) {
                                throw new ScriptException("Could not delete old backup file " + backupFile);
                            }
                        }
                        if (! nextBackupFile.renameTo(backupFile)) {
                            throw new ScriptException("Could not move " + nextBackupFile + " to " + backupFile);
                        }
                    }
                }
            }

            ObjectOutputStream out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(file)));
            try {
                out.writeObject(world);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            throw new ScriptException("I/O error saving file (message: " + e.getMessage() + ")", e);
        }
        
        return null;
    }
    
    private final World2 world;
    private String fileName;
}