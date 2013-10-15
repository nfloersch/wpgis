/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.pepsoft.worldpainter;

import org.pepsoft.worldpainter.vo.AttributeKeyVO;

/**
 *
 * @author pepijn
 */
public final class Constants {
    private Constants() {
        // Prevent instantiation
    }

    /**
     * Size in blocks of a tile. <b>Must be a power of two!</b>
     */
    public static final int TILE_SIZE = 128;
    /**
     * The number of bits to shift to the left to multiply with {@link #TILE_SIZE}.
     */
    public static final int TILE_SIZE_BITS = 7;
    public static final int TILE_SIZE_MASK = TILE_SIZE - 1;
    
    public static final float PETA_BLOBS     = 2097.169f;
    public static final float TERA_BLOBS     = 1048.583f;
    public static final float GIGANTIC_BLOBS =  524.309f;
    public static final float ENORMOUS_BLOBS =  262.147f;
    public static final float HUGE_BLOBS     =  131.101f;
    public static final float LARGE_BLOBS    =   65.537f;
    public static final float MEDIUM_BLOBS   =   32.771f;
    public static final float SMALL_BLOBS    =   16.411f;
    public static final float TINY_BLOBS     =    4.099f;
    
    public static final int VOID_COLOUR             = 0xC0FFFF;
    public static final int UNKNOWN_MATERIAL_COLOUR = 0xFF00FF;
    
    public static final int DIM_NORMAL = 0;
    public static final int DIM_NETHER = 1;
    public static final int DIM_END  = 2;
    
    public static final AttributeKeyVO<Integer> ATTRIBUTE_KEY_MAX_HEIGHT = new AttributeKeyVO<Integer>("maxHeight");
    public static final AttributeKeyVO<Integer> ATTRIBUTE_KEY_VERSION = new AttributeKeyVO<Integer>("version");
    public static final AttributeKeyVO<Boolean> ATTRIBUTE_KEY_MAP_FEATURES = new AttributeKeyVO<Boolean>("mapFeatures");
    public static final AttributeKeyVO<Integer> ATTRIBUTE_KEY_GAME_TYPE = new AttributeKeyVO<Integer>("gameType");
    public static final AttributeKeyVO<Boolean> ATTRIBUTE_KEY_ALLOW_CHEATS = new AttributeKeyVO<Boolean>("allowCheats");
    public static final AttributeKeyVO<String> ATTRIBUTE_KEY_GENERATOR = new AttributeKeyVO<String>("generator");
    public static final AttributeKeyVO<String> ATTRIBUTE_KEY_GENERATOR_OPTIONS = new AttributeKeyVO<String>("generatorOptions");
    public static final AttributeKeyVO<Integer> ATTRIBUTE_KEY_TILES = new AttributeKeyVO<Integer>("tiles");
    public static final AttributeKeyVO<Integer> ATTRIBUTE_KEY_NETHER_TILES = new AttributeKeyVO<Integer>("nether.tiles");
    public static final AttributeKeyVO<Integer> ATTRIBUTE_KEY_END_TILES = new AttributeKeyVO<Integer>("end.tiles");
    public static final AttributeKeyVO<Integer> ATTRIBUTE_KEY_EXPORTED_DIMENSION = new AttributeKeyVO<Integer>("exportedDimension");
    public static final AttributeKeyVO<Integer> ATTRIBUTE_KEY_EXPORTED_DIMENSION_TILES = new AttributeKeyVO<Integer>("exportedDimension.tiles");
    public static final AttributeKeyVO<Boolean> ATTRIBUTE_KEY_IMPORTED_WORLD = new AttributeKeyVO<Boolean>("importedWorld");
    public static final AttributeKeyVO<String> ATTRIBUTE_KEY_PLUGINS = new AttributeKeyVO<String>("plugins");
    
    public static final String EVENT_KEY_ACTION_NEW_WORLD         = "action.newWorld";
    public static final String EVENT_KEY_ACTION_EXPORT_WORLD      = "action.exportWorld";
    public static final String EVENT_KEY_ACTION_IMPORT_MAP        = "action.importMap";
    public static final String EVENT_KEY_ACTION_MERGE_WORLD       = "action.mergeWorld";
    public static final String EVENT_KEY_ACTION_OPEN_WORLD        = "action.openWorld";
    public static final String EVENT_KEY_ACTION_SAVE_WORLD        = "action.saveWorld";
    public static final String EVENT_KEY_ACTION_MIGRATE_WORLD     = "action.migrateWorld";
    public static final String EVENT_KEY_ACTION_MIGRATE_HEIGHT    = "action.migrateHeight";
    public static final String EVENT_KEY_ACTION_MIGRATE_ROTATION  = "action.migrateRotation";
    public static final String EVENT_KEY_DONATION_DONATE          = "donation.donate";
    public static final String EVENT_KEY_DONATION_ALREADY_DONATED = "donation.alreadyDonated";
    public static final String EVENT_KEY_DONATION_ASK_LATER       = "donation.askLater";
    public static final String EVENT_KEY_DONATION_NO_THANKS       = "donation.noThanks";
    public static final String EVENT_KEY_DONATION_CLOSED          = "donation.closed";
}