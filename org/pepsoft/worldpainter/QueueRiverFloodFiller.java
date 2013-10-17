package org.pepsoft.worldpainter;

//Original algorithm by J. Dunlap http://www.codeproject.com/KB/GDI-plus/queuelinearfloodfill.aspx
//Java port by Owen Kaluza
// Adapted for WorldPainter by Pepijn Schmitz on 28-3-2011
import java.awt.Point;
import java.awt.Window;
import java.util.BitSet;
import java.util.Hashtable;
import java.util.Queue;
import org.pepsoft.worldpainter.layers.FloodWithLava;
import static org.pepsoft.worldpainter.Constants.*;
import javax.swing.JOptionPane;
import javax.vecmath.Point3d;

public class QueueRiverFloodFiller {
    // Dimension to flood

    private final Dimension dimension;
    // Level to flood to
    private final int waterLevel;
    // Whether to flood with lava instead of water
    private final boolean floodWithLava;
    // Whether to remove a layer of material instead of adding it
    private final boolean undo;
    //cached image properties
    protected int width = 0;
    protected int height = 0;
    protected int offsetX, offsetY;
    //internal, initialized per fill
    protected BitSet blocksChecked;
    //Queue of floodfill ranges
    protected Queue<FloodFillRange> ranges;

    private Hashtable boundaries = new Hashtable();
    private Hashtable visited = new Hashtable();
    private Point3d currentPoint = new Point3d(); // Like a cursor on current map location
    private WorldPainter appView = null;
    
    // Constructor
    public QueueRiverFloodFiller(Dimension dimension, int waterLevel, boolean floodWithLava, boolean undo, WorldPainter view) {
        this.dimension = dimension;
        this.waterLevel = waterLevel;
        this.floodWithLava = floodWithLava;
        this.undo = undo;
        width = dimension.getWidth() * TILE_SIZE;
        height = dimension.getHeight() * TILE_SIZE;
        offsetX = dimension.getLowestX() * TILE_SIZE;
        offsetY = dimension.getLowestY() * TILE_SIZE;
        appView = view;
        JOptionPane.showMessageDialog(null, "River Flood Fill Instantiated");
    }

    // Shorthand
    public Dimension getDimension() {
        return dimension;
    }

    // Maybe remove... old Flood Fill init stuff.
    protected void prepare() {
        //Called before starting flood-fill
        //blocksChecked = new BitSet(width * height);
        //ranges = new LinkedList<FloodFillRange>();
    }

    // Return a string value for a given coordinate double, translated to the
    // world coordinate system.
    private String coordShow(Point3d coords) {
        Point Point2d = appView.imageToWorldCoordinates((int)coords.x, (int)coords.y);
        String Xval = "X->[" + String.valueOf(Point2d.x) + "]";
        String Yval = "Y->[" + String.valueOf(Point2d.y) + "]";
        String Zval = "Z->[" + String.valueOf(coords.z) + "]";
        return Xval + ", " + Yval + ", " + Zval;
    }
    
    // The PRIMARY external method call. This method kicks off other action.
    public boolean floodFill(int x, int y, Window parent) {
        currentPoint.x = x;
        currentPoint.y = y;
        currentPoint.z = dimension.getIntHeightAt(offsetX + (int)currentPoint.x, offsetY + (int)currentPoint.y);
        edgeSearch();
        return true;
    }
    
    
    
    
    
    
    
    // Detect edges of entire river and stuff into hashtable with key 1 being X 
    // coordinate, and then under each X a hashtable of values keyed by Y 
    // coordinate. Maybe value can be Z ?
    
    void edgeSearch() {
        // an Edge is block that has a higher Z value than the current point
        // OR is an existing water block.
        MoveCurrentPointToNorthernMostEdge();
        FindAdjacentEdgeCCW(new Point3d(currentPoint));
        DetectIslands(boundaries); // If boundaries does not enclose start location, there is probably an island.
        // Or maybe just scan every location inside boundaries to find if Z value is higher than water level?
    }
    
    private void FindAdjacentEdgeCCW(Point3d startPoint) {
        JOptionPane.showMessageDialog(null, "FindAdjacentEdgeCCW() called");
        // Although water cannot flow diagnoally and thus could not reach a block at
        // the 1:30, 4:30, 7:30, or 10:30 positions around a central block IF
        // there was also a block at the 12 & 3, 3 & 6, 6 & 9, or 9 & 12 positions
        // (Respectively), we do need to also consider the 1:30, 4:30, 7:30, and 10:30
        // positions as 'continuance' of edges found at 12, 3, 6, and 9 (respectively).
        // So we need to search all 8 block positions around each block, but the
        // corners are treated differently from the sides.
        Point3d lastFoundBoundaryBlock = null;
        do {
            // a 3x3 array of where boundaries are found based on currentPoint.
            boolean[][] BoundsForCurLoc = { {false,false,false},
                                            {false,false,false},
                                            {false,false,false}};
            
            FindAndAddNewBoundBlocksCCW(lastFoundBoundaryBlock, BoundsForCurLoc);
            FindAndTakeNextHop(lastFoundBoundaryBlock, BoundsForCurLoc);
            
       }while(currentPoint != startPoint);
        
        JOptionPane.showMessageDialog(null, "FindAdjacentEdgeCCW() done");
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void FindAndAddNewBoundBlocksCCW(Point3d lastFoundBoundaryBlock, boolean[][] outBnds ) {
        int curX = (int)currentPoint.x;
        int curY = (int)currentPoint.y;
        int curZ = (int)currentPoint.z;
        int curW = dimension.getWaterLevelAt(curX, curY);
        // Remember Y - 1 is actually referring to the north of current Y.
        // But X - 1 is still referring to west of current X.
        // This search is CCW starting at 12 o'clock.
        Point3d outBnd = null;
        // North
        outBnd = TestAndAddBoundary(curX,curY-1,curZ,curW);
        if (outBnd != null) {
            outBnds[0][1] = true;
            lastFoundBoundaryBlock = outBnd;
        }
        // North West
        // curX - 1, curY - 1
        outBnd = TestAndAddBoundary(curX-1,curY-1,curZ,curW);
        if (outBnd != null) {
            outBnds[0][0] = true;
            lastFoundBoundaryBlock = outBnd;
        }
        // West
        // curX - 1, curY
        outBnd = TestAndAddBoundary(curX-1,curY,curZ,curW);
        if (outBnd != null) {
            outBnds[1][0] = true;
            lastFoundBoundaryBlock = outBnd;
        }  
        // South West
        // curX - 1, curY + 1
        outBnd = TestAndAddBoundary(curX-1,curY+1,curZ,curW);
        if (outBnd != null) {
            outBnds[2][0] = true;
            lastFoundBoundaryBlock = outBnd;
        }
        // South
        // curX, curY + 1
        outBnd = TestAndAddBoundary(curX,curY+1,curZ,curW);
        if (outBnd != null) {
            outBnds[2][1] = true;
            lastFoundBoundaryBlock = outBnd;
        }
        // South East
        // curX + 1, curY + 1
        outBnd = TestAndAddBoundary(curX+1,curY+1,curZ,curW);
        if (outBnd != null) {
            outBnds[2][2] = true;
            lastFoundBoundaryBlock = outBnd;
        }
        // East
        // curX + 1, curY
        outBnd = TestAndAddBoundary(curX+1,curY,curZ,curW);
        if (outBnd != null) {
            outBnds[1][2] = true;
            lastFoundBoundaryBlock = outBnd;
        }
        // North East
        // curX + 1, curY - 1
        outBnd = TestAndAddBoundary(curX+1,curY-1,curZ,curW);
        if (outBnd != null) {
            outBnds[0][2] = true;
            lastFoundBoundaryBlock = outBnd;
        }
    }

    private void FindAndTakeNextHop(Point3d lastFoundBoundaryBlock, boolean[][] BoundsForCurLoc) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
    private Point3d TestAndAddBoundary(int testX, int testY, int curZ, int curW){
        if (!isCoordsInBoundaries(testX, testY)) {
            if ((dimension.getHeightAt(testX, testY - 1) > curZ) ||
                (dimension.getWaterLevelAt(testX, testY - 1) > curZ )) {
                Point3d newBound = new Point3d(testX, testY - 1, dimension.getHeightAt(testX, testY - 1));
                putCoordsToBoundaries(newBound);
                return newBound;
            }
        }
        return null;
    }
    
    // Drives the currentPoint coordinates to the northern-most (y-most) 'edge'
    // we can find.
    private void MoveCurrentPointToNorthernMostEdge() {
        boolean NorthEdgeFound = false;
        JOptionPane.showMessageDialog(null, "MoveCurrentPointToNorthernMostEdge() called");
        while (!NorthEdgeFound) {
            Point3d nextPointUp = new Point3d();
            nextPointUp.x = currentPoint.x;
            nextPointUp.y = currentPoint.y - 1; // Not + 1, for some reason Y coords are upside down...
            nextPointUp.z = dimension.getIntHeightAt(offsetX + (int)nextPointUp.x, offsetY + (int)nextPointUp.y);
            
            if (!(nextPointUp.z <= currentPoint.z)){
                Integer waterLevelAtNextPoint = dimension.getWaterLevelAt((int)nextPointUp.x, (int)nextPointUp.y);
                //JOptionPane.showMessageDialog(null, "Water Level At Point = [" + waterLevelAtNextPoint.toString() + "]");
                NorthEdgeFound = true;
            }
            else {
                currentPoint.y = nextPointUp.y;
            }
        }
        // This is first entry in boundaries.
        putCoordsToBoundaries(currentPoint);
        
        JOptionPane.showMessageDialog(null, "Northernmost Edge = " + coordShow(currentPoint));
        JOptionPane.showMessageDialog(null, "MoveCurrentPointToNorthernMostEdge() done");
    }

    private boolean putCoordsToBoundaries(Point3d coords) {
        if (!(boundaries.containsKey((int)coords.x))) {
            boundaries.put((int)coords.x, new Hashtable());
        }
        Hashtable YZBounds = (Hashtable)boundaries.get((int)coords.x);
        // Will not store duplicate Y/Z values... ok?
        if (!(YZBounds.containsKey((int)coords.y))) {
            YZBounds.put((int)coords.y, (int)coords.z);
        }
        return true;
    }
    
    private boolean isCoordsInBoundaries(int X, int Y) {
        if (boundaries.containsKey(X)) {
            Hashtable YZhash = (Hashtable)boundaries.get(X);
            if (YZhash.containsKey(Y)) {
                // Not checking Z right now, not relevant
                    return true;
                
            }
        }
        return false;
    }
        
    private boolean isCoordsInBoundaries(Point3d coords) {
        return isCoordsInBoundaries((int)coords.x,(int)coords.y);
    }
    
    private void DetectIslands(Hashtable boundaries) {
        JOptionPane.showMessageDialog(null, "DetectIslands() called");
        JOptionPane.showMessageDialog(null, "DetectIslands() done");
    }
    
    
    
    
    
    
    
    
    
    
    
    // Fills the specified point on the bitmap with the currently selected fill color.
    // int x, int y: The starting coords for the fill
//    public boolean floodFill(int x, int y, Window parent) {
//        JOptionPane.showMessageDialog(null, "River Flood Fill Started");
//        //Setup
//        prepare();
//
//        long start = System.currentTimeMillis();
//
//        //***Do first call to floodfill.
//        linearFill(x, y);
//
//        //***Call floodfill routine while floodfill ranges still exist on the queue
//        FloodFillRange range;
//        while (ranges.size() > 0) {
//            //**Get Next Range Off the Queue
//            range = ranges.remove();
//            processRange(range);
//
//            long lap = System.currentTimeMillis();
//            if ((lap - start) > 2000) {
//                // We're taking more than two seconds. Do the rest in the
//                // background and show a progress dialog so the user can cancel
//                // the operation
//                if (ProgressDialog.executeTask(parent, new ProgressTask<Dimension>() {
//                    @Override
//                    public String getName() {
//                        return undo ? "Draining" : "Flooding";
//                    }
//
//                    @Override
//                    public Dimension execute(ProgressReceiver progressReceiver) throws OperationCancelled {
//                        //***Call floodfill routine while floodfill ranges still exist on the queue
//                        FloodFillRange range;
//                        while (ranges.size() > 0) {
//                            //**Get Next Range Off the Queue
//                            range = ranges.remove();
//                            processRange(range);
//                            progressReceiver.checkForCancellation();
//                        }
//                        return dimension;
//                    }
//                }) == null) {
//                    // Operation cancelled
//                    return false;
//                }
//                return true;
//            }
//        }
//
//        return true;
//    }

    private void processRange(FloodFillRange range) {
        //**Check Above and Below Each block in the Floodfill Range
        int downPxIdx = (width * (range.Y + 1)) + range.startX;
        int upPxIdx = (width * (range.Y - 1)) + range.startX;
        int upY = range.Y - 1;//so we can pass the y coord by ref
        int downY = range.Y + 1;
        for (int i = range.startX; i <= range.endX; i++) {
            //*Start Fill Upwards
            //if we're not above the top of the bitmap and the block above this one is within the color tolerance
            if (range.Y > 0 && (!blocksChecked.get(upPxIdx)) && checkBlock(upPxIdx)) {
                linearFill(i, upY);
            }

            //*Start Fill Downwards
            //if we're not below the bottom of the bitmap and the block below this one is within the color tolerance
            if (range.Y < (height - 1) && (!blocksChecked.get(downPxIdx)) && checkBlock(downPxIdx)) {
                linearFill(i, downY);
            }
            downPxIdx++;
            upPxIdx++;
        }
    }

    // Finds the furthermost left and right boundaries of the fill area
    // on a given y coordinate, starting from a given x coordinate, filling as it goes.
    // Adds the resulting horizontal range to the queue of floodfill ranges,
    // to be processed in the main loop.
    //
    // int x, int y: The starting coords
    protected void linearFill(int x, int y) {
        //***Find Left Edge of Color Area
        int lFillLoc = x; //the location to check/fill on the left
        int pxIdx = (width * y) + x;
        int origPxIdx = pxIdx;
        while (true) {
            if (undo) {
                //**remove a layer of material
                dimension.setWaterLevelAt(offsetX + x + pxIdx - origPxIdx, offsetY + y, waterLevel - 1);
            } else {
                //**flood
                dimension.setWaterLevelAt(offsetX + x + pxIdx - origPxIdx, offsetY + y, waterLevel);
                dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, offsetX + x + pxIdx - origPxIdx, offsetY + y, floodWithLava);
            }
            //**indicate that this block has already been checked and filled
            blocksChecked.set(pxIdx);
            //**de-increment
            lFillLoc--;     //de-increment counter
            pxIdx--;        //de-increment block index
            //**exit loop if we're at edge of bitmap or color area
            if (lFillLoc < 0 || blocksChecked.get(pxIdx) || !checkBlock(pxIdx)) {
                break;
            }
        }
        lFillLoc++;

        //***Find Right Edge of Color Area
        int rFillLoc = x; //the location to check/fill on the left
        pxIdx = (width * y) + x;
        origPxIdx = pxIdx;
        while (true) {
            if (undo) {
                //**remove a layer of material
                dimension.setWaterLevelAt(offsetX + x + pxIdx - origPxIdx, offsetY + y, waterLevel - 1);
            } else {
                //**flood
                dimension.setWaterLevelAt(offsetX + x + pxIdx - origPxIdx, offsetY + y, waterLevel);
                dimension.setBitLayerValueAt(FloodWithLava.INSTANCE, offsetX + x + pxIdx - origPxIdx, offsetY + y, floodWithLava);
            }
            //**indicate that this block has already been checked and filled
            blocksChecked.set(pxIdx);
            //**increment
            rFillLoc++;     //increment counter
            pxIdx++;        //increment block index
            //**exit loop if we're at edge of bitmap or color area
            if (rFillLoc >= width || blocksChecked.get(pxIdx) || !checkBlock(pxIdx)) {
                break;
            }
        }
        rFillLoc--;

        //add range to queue
        FloodFillRange r = new FloodFillRange(lFillLoc, rFillLoc, y);
        ranges.offer(r);
    }

    //Sees if a block should be flooded (or unflooded) returning only true or false
    protected boolean checkBlock(int px) {
        int y = px / width;
        int x = px % width;
        // If block is VOID return
        if (dimension.getBitLayerValueAt(org.pepsoft.worldpainter.layers.Void.INSTANCE, offsetX + x, offsetY + y)) {
            return false;
        } else {
            int height = dimension.getIntHeightAt(offsetX + x, offsetY + y);
            if (undo) {
                return (height != -1);
                   /* && (dimension.getWaterLevelAt(offsetX + x, offsetY + y) >= waterLevel)
                    && (height < waterLevel); */
            } else {
                return (height != -1);
                    /* && (waterLevel > height)
                    && (waterLevel > dimension.getWaterLevelAt(offsetX + x, offsetY + y)); */
            }
        }
    }

    // Represents a linear range to be filled and branched from.
    protected class FloodFillRange {

        public int startX;
        public int endX;
        public int Y;

        public FloodFillRange(int startX, int endX, int y) {
            this.startX = startX;
            this.endX = endX;
            this.Y = y;
        }
    }
}
