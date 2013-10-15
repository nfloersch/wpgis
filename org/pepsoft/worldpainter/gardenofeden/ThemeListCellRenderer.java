/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.gardenofeden;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import org.pepsoft.worldpainter.WPContextProvider;
import org.pepsoft.worldpainter.util.MinecraftUtil;

/**
 *
 * @author pepijn
 */
public class ThemeListCellRenderer extends DefaultListCellRenderer {
    public ThemeListCellRenderer() {
        File file = MinecraftUtil.findMinecraftJar(WPContextProvider.getWPContext().getMinecraftJarProvider());
        if (file != null) {
            logger.info("Loading default Minecraft texture map from " + file);
            try {
                JarFile minecraftJar = new JarFile(file);
                JarEntry entry = minecraftJar.getJarEntry("terrain.png");
                if (entry != null) {
                    InputStream in = minecraftJar.getInputStream(entry);
                    try {
                        texturePack = ImageIO.read(in);
                    } finally {
                        in.close();
                    }
                } else {
                    texturePack = null;
                }
            } catch (IOException e) {
                throw new RuntimeException("I/O error getting default texture map from minecraft.jar", e);
            }
        } else {
            logger.warning("Minecraft installation not found; could not load default texture map!");
            texturePack = null;
        }
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if ((texturePack != null) && (value != null)) {
            setText(null);
            Icon preview = iconCache.get((Theme) value);
            if (preview == null) {
                preview = new ImageIcon(((Theme) value).getPreview(texturePack));
                iconCache.put((Theme) value, preview);
            }
            setIcon(preview);
        }
        return this;
    }
    
    private final BufferedImage texturePack;
    private final Map<Theme, Icon> iconCache = new HashMap<Theme, Icon>();
    
    private static final Logger logger = Logger.getLogger(ThemeListCellRenderer.class.getName());
    private static final long serialVersionUID = 1L;
}