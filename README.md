WPGIS
=====
WorldPainter-GIS Related Hax!

In working with Minecraft to build the Lakecraft project (lakecraft.net) I needed a way to integrate GIS data layers of roads, landcover, hydrography, landuse, pollution, and more, into the Lakecraft world files. The project initially began by building files and importing them into WorldPainter. Since WorldPainter is FOSS, I decided to extend it a bit to make working with it for my purposes easier.

The hack allows using black and white image files as masks for applying block-level terrain and layer changes to the map. Imagine a black and white image of a road network - everywhere there is roadway is black, and everything else is white. This hack allows us to apply a chosen type of block onto the map only where the black roadways are shown on the black and white image. Really not brilliant, but useful.

Likewise, we can use a black and white image of evergreen tree cover areas, or deciduous tree cover areas, and put appropriate trees in only those places. The National Land Cover Dataset (NLCD) is perfect for this.

To use the hack:

1) Download and build so you can run. You will recieve an error at launch about not having a JIDE software license - this is non-fatal and is no big deal. You can ask them for a free license if you want, but I cannot distribute the free one they gave me that I never got to work anyway :^P

2) Take the black and white image you want and use it in the "configure view" menu of WorldPainter to draw it as an overlay image on the map. I think this function was originally meant to allow the image to act as a rough guide to where you "paint" ... so by default when the image is loaded in and shown, it has 50% transparency. So open your image, and then immediatley set the transparency to 0%. When the overlay image is enabled, you should only see black and white, not your map.

3) In the file menu, use the new "Overlay Resources" menu command to open the dialog box with the various overlay options.

3a) Raise/Lower - raise or lower the blocks based on the black areas of your image overlay by a certain amount. Does not change the type of block.

3b) Roads - select the roads option from the upper list box, and then in the lower listbox choose one of the road types: gravel, cobble, paved, primary, secondary. Currently these map in the code to using brown wool, mossy cobblestone, coal blocks, cyan clay, and gray wool, respectively.

3c) Landuse - really this is about land cover right now. Choose landuse in the type list box, and then one of the land use types in the lower listbox: pine forest, deciduous forest, swamp, or plains, and then the frozen variety of each of those.

3d) Rivers - this just lays down water wherever the black part of the mask is. Does not use the landuse type or road type options. It just uses water.

3e) Colors - this is going to be about using colored wool or clay to demonstrate different GIS data in the 3D world. Not ready yet.

4) Hit the "Process World" button and hope it works. Small worlds can be completed in a second or two. Lakecraft can take around 1-2 minutes, but it is a 10gb world file.

If you have ideas or thoughts or contributions, bring 'em on!

-Nick
