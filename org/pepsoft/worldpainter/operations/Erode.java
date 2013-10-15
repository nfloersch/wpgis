/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter.operations;

import java.util.Random;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.MapDragControl;
import org.pepsoft.worldpainter.RadiusControl;
import org.pepsoft.worldpainter.WorldPainter;

/**
 *
 * @author pepijn
 */
public class Erode extends RadiusOperation {
    public Erode(WorldPainter view, RadiusControl radiusControl, MapDragControl mapDragControl) {
        super("Erode", "Erode the terrain", view, radiusControl, mapDragControl, 100, true, "operation.erode");
    }

    @Override
    protected void tick(int centerX, int centerY, boolean undo, boolean first, float dynamicLevel) {
        Dimension dimension = getDimension();
        dimension.setEventsInhibited(true);
        try {
            int radius = getRadius();
            for (int i = 0; i < ROUNDS; i++) {
                for (int x = centerX - radius; x <= centerX + radius; x++) {
                    for (int y = centerY - radius; y <= centerY + radius; y++) {
                        float strength = getStrength(centerX, centerY, x, y);
                        if ((strength == 1.0f) || (random.nextFloat() < strength)) {
                            for (int dx = -1; dx <= 1; dx++) {
                                for (int dy = -1; dy <= 1; dy++) {
                                    heightBuffer[dx + 1][dy + 1] = dimension.getRawHeightAt(x + dx, y + dy);
                                }
                            }
                            int lowestDx = 0, lowestDy = 0, lowestRawHeight = Integer.MAX_VALUE;
                            boolean reverseX = random.nextBoolean(), reverseY = random.nextBoolean();
                            if (reverseX) {
                                for (int dx = 2; dx >= 0; dx--) {
                                    if (reverseY) {
                                        for (int dy = 2; dy >= 0; dy--) {
                                            if (heightBuffer[dx][dy] < lowestRawHeight) {
                                                lowestRawHeight = heightBuffer[dx][dy];
                                                lowestDx = dx;
                                                lowestDy = dy;
                                            }
                                        }
                                    } else {
                                        for (int dy = 0; dy < 3; dy++) {
                                            if (heightBuffer[dx][dy] < lowestRawHeight) {
                                                lowestRawHeight = heightBuffer[dx][dy];
                                                lowestDx = dx;
                                                lowestDy = dy;
                                            }
                                        }
                                    }
                                }
                            } else {
                                for (int dx = 0; dx < 3; dx++) {
                                    if (reverseY) {
                                        for (int dy = 2; dy >= 0; dy--) {
                                            if (heightBuffer[dx][dy] < lowestRawHeight) {
                                                lowestRawHeight = heightBuffer[dx][dy];
                                                lowestDx = dx;
                                                lowestDy = dy;
                                            }
                                        }
                                    } else {
                                        for (int dy = 0; dy < 3; dy++) {
                                            if (heightBuffer[dx][dy] < lowestRawHeight) {
                                                lowestRawHeight = heightBuffer[dx][dy];
                                                lowestDx = dx;
                                                lowestDy = dy;
                                            }
                                        }
                                    }
                                }
                            }
                            if ((lowestDx != 1) || (lowestDy != 1)) {
                                int difference = heightBuffer[1][1] - heightBuffer[lowestDx][lowestDy];
                                int amount = Math.min((int) (difference / 2 / (((lowestDx != 1) && (lowestDy != 1)) ? ROOT_OF_TWO : 1)), ERODE_AMOUNT);
                                amount = (int) ((amount / 64f) * (amount / 64f) * 64);
                                if (amount > 0) {
                                    dimension.setRawHeightAt(x, y, heightBuffer[1][1] - amount);
                                    dimension.setRawHeightAt(x + lowestDx - 1, y + lowestDy - 1, heightBuffer[lowestDx][lowestDy] + amount);
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            dimension.setEventsInhibited(false);
        }
    }

    private final int[][] heightBuffer = new int[3][3];
    private final Random random = new Random();
    
    private static final int ROUNDS = 1;
    private static final int ERODE_AMOUNT = 64;
    private static final float ROOT_OF_TWO = (float) Math.sqrt(2);
}