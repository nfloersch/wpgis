/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pepsoft.worldpainter.layers.exporters;

import java.awt.Rectangle;
import java.util.List;
import java.util.Random;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.exporting.AbstractLayerExporter;
import org.pepsoft.worldpainter.exporting.Fixup;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.exporting.SecondPassLayerExporter;
import org.pepsoft.worldpainter.layers.Tunnels;

/**
 *
 * @author pepijn
 */
public class TunnelsExporter extends AbstractLayerExporter<Tunnels> implements SecondPassLayerExporter<Tunnels> {
    public TunnelsExporter() {
        super(Tunnels.INSTANCE, new TunnelsSettings());
    }

    @Override
    public List<Fixup> render(final Dimension dimension, final Rectangle area, final Rectangle exportedArea, final MinecraftWorld minecraftWorld) {
        // Go chunk by chunk in order to have consistency and predictability
        final int chunkX1 = exportedArea.x >> 4;
        final int chunkY1 = exportedArea.y >> 4;
        final int chunkX2 = (exportedArea.x + exportedArea.width - 1) >> 4;
        final int chunkY2 = (exportedArea.y + exportedArea.height - 1) >> 4;
        for (int chunkX = chunkX1; chunkX <= chunkX2; chunkX++) {
            final int chunkWorldX = chunkX << 4;
            for (int chunkY = chunkY1; chunkY <= chunkY2; chunkY++) {
                final int chunkWorldY = chunkY << 4;
                final long seed = dimension.getSeed() + chunkX * 65537 + chunkY;
                final Random random = new Random(seed);
                final int noOfTunnels = Math.max(random.nextInt(6) - 2, 0);
                for (int i = 0; i < noOfTunnels; i++) {
                    final int tunnelX = random.nextInt(16);
                    final int tunnelY = random.nextInt(16);
                    final int tunnelZ = random.nextInt(dimension.getIntHeightAt(chunkWorldX + tunnelX, chunkWorldY + tunnelY));
                    renderTunnel(dimension, minecraftWorld, tunnelX, tunnelY, tunnelZ, random);
                }
            }
        }
        return null;
    }

    private void renderTunnel(final Dimension dimension, final MinecraftWorld minecraftWorld, final int x, final int y, final int z, final Random random) {
        final int length = random.nextInt(LENGTH_VARIATION) + LENGTH_BIAS;
        float headX = x, headY = y, headZ = z;
//        carveTunnel(dimension, minecraftWorld, (int) (headX + 0.5f), (int) (headX + 0.5f), (int) (headX + 0.5f));
    }
    
    private static final int LENGTH_VARIATION = 20;
    private static final int LENGTH_BIAS = 10;

    public static class TunnelsSettings implements ExporterSettings<Tunnels> {
        @Override
        public boolean isApplyEverywhere() {
            return applyEverywhere;
        }

        @Override
        public Tunnels getLayer() {
            return Tunnels.INSTANCE;
        }

        @Override
        @SuppressWarnings("unchecked") // Guaranteed by Java
        public ExporterSettings<Tunnels> clone() {
            try {
                return (ExporterSettings<Tunnels>) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        
        private boolean applyEverywhere;

        private static final long serialVersionUID = 1L;
    }
}