package me.benjozork.explosionregen;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 Looks like you decompiled my code :) Don't worry, you have to right to do so.

 The MIT License (MIT)

 Copyright (c) 2016 Benjozork

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 **/

public class BlockRegenerator {
    private List<List<Location>> blockLocations = new ArrayList<>();
    private List<HashMap<Location, Material>> blocks = new ArrayList<>();
    private HashMap<List<Location>, Integer> explosionCounts = new HashMap<>();

    private int explosionCount = 0;
    private final int delay;

    private boolean isRegenerating = false;

    public BlockRegenerator(int delay) {
        this.delay = delay;
    }

    private void incrementID(int i) {
        HashMap<List<Location>, Integer> buffer_explosionIDs = explosionCounts;
        int buffer_int;

        buffer_int = explosionCounts.get(blockLocations.get(i));
        buffer_explosionIDs.remove(blockLocations.get(i));
        buffer_explosionIDs.put(blockLocations.get(i), buffer_int + 1);

        explosionCounts = buffer_explosionIDs;
    }

    public void registerExplosion(List<Location> explodedLocations, List<Material> explodedMaterials) {
        List<Location> buffer_loc = new ArrayList<>();
        HashMap<Location, Material> buffer_loc_mat = new HashMap<>();

        for (int i = 0; i < explodedLocations.size(); i++) {
            buffer_loc.add(explodedLocations.get(i));
            buffer_loc_mat.put(explodedLocations.get(i), explodedMaterials.get(i));
        }

        blockLocations.add(buffer_loc);
        explosionCounts.put(buffer_loc, 0);
        blocks.add(buffer_loc_mat);
    }

    public void regenerateNextBlocks() {

        if (blockLocations.isEmpty()) return;

        isRegenerating = true;

        if (explosionCount >= blockLocations.size()) {
            explosionCount = 0;
            for (int i = 0; i < blockLocations.size(); i++) {
                incrementID(i);
            }
        }

        if (explosionCounts.get(blockLocations.get(explosionCount)) >= blockLocations.get(explosionCount).size()) {
            blockLocations.remove(explosionCount);
            blocks.remove(explosionCount);
            if (explosionCount >= blockLocations.size()) {
                explosionCount = 0;
                for (int i = 0; i < blockLocations.size(); i++) {
                    incrementID(i);
                }
            }
        }

        if (blockLocations.isEmpty()) return;

        Location l;
        try {
            l = blockLocations.get (explosionCount).get(explosionCounts.get(blockLocations.get(explosionCount)));
        } catch (Exception ignored) {
            return;
        }
        Block b = l.getWorld().getBlockAt(l);

        b.setType(blocks.get(explosionCount).get(l));

        explosionCount++;
    }
}
