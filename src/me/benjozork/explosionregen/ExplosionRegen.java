package me.benjozork.explosionregen;

import me.benjozork.explosionregen.listeners.ExplosionListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
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

public class ExplosionRegen extends JavaPlugin {

    private BlockRegenerator regenerator = new BlockRegenerator(getConfig().getInt("delay"));

    private List<List<Location>> explosion_buffer = new ArrayList<>();
    private HashMap<List<Location>, Integer> explosion_buffer_time = new HashMap<>();
    private HashMap<List<Location>, List<Material>> explosion_buffer_material = new HashMap<>();

    private int speed;
    private int tickCount;

    @Override
    public void onEnable() {
        List<Material> blacklist = new ArrayList<>();
        speed = getConfig().getInt("speed");
        for (String s : getConfig().getStringList("blacklisted_blocks")) {
            try {
                blacklist.add(Material.getMaterial(s));
                System.out.println("[ExplosionRegen] Blacklist data added: \"" + Material.getMaterial(s).toString() + "\" !");
            } catch (Exception e) {
                System.out.println("[ExplosionRegen] Invalid blacklist data \"" + s + "\" !");
            }
        }

        Bukkit.getPluginManager().registerEvents(new ExplosionListener(this, blacklist), this);
        this.saveDefaultConfig();


        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {

            @Override
            public void run() {
                if (explosion_buffer.isEmpty()) return;
                for (int i = 0; i < explosion_buffer.size(); i++) {

                    HashMap<List<Location>, Integer> buffer_increment = explosion_buffer_time;
                    int buffer_int;

                    buffer_int = explosion_buffer_time.get(explosion_buffer.get(i));
                    buffer_increment.remove(explosion_buffer.get(i));
                    buffer_increment.put(explosion_buffer.get(i), buffer_int - 1);

                    explosion_buffer_time = buffer_increment;

                    if (explosion_buffer_time.get(explosion_buffer.get(i)) == 0) {
                        regenerator.registerExplosion(explosion_buffer.get(i), explosion_buffer_material.get(explosion_buffer.get(i)));
                        explosion_buffer_time.remove(explosion_buffer.get(i));
                        explosion_buffer_material.remove(explosion_buffer.get(i));
                        explosion_buffer.remove(i);
                    }
                }
            }

        }, 0, 20);

        Bukkit.getScheduler().runTaskTimer(this, new Runnable() {

            @Override
            public void run() {
                List<Integer> divisorTable10 = Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20);
                List<Integer> divisorTable5 = Arrays.asList(4, 8, 12, 16, 20);
                List<Integer> divisorTable4 = Arrays.asList(5, 10, 15, 20);
                List<Integer> divisorTable2 = Arrays.asList(10, 20);

                tickCount++;
                if (tickCount > 20) tickCount = 0;
                if (speed != getSpeed(new int[]{1,2,4,5,10,20}, regenerator.getRemainingExplosions())) speed = getSpeed(new int[]{1,2,4,5,10,20}, regenerator.getRemainingExplosions());
                if (speed == 1) {
                    if (tickCount == 1) regenerator.regenerateNextBlocks();
                } else if (speed == 2) {
                    if  (divisorTable2.contains(tickCount)) regenerator.regenerateNextBlocks();
                } else if (speed == 4) {
                    if (divisorTable4.contains(tickCount)) regenerator.regenerateNextBlocks();
                } else if (speed == 5) {
                    if (divisorTable5.contains(tickCount)) regenerator.regenerateNextBlocks();
                } else if (speed == 10) {
                    if (divisorTable10.contains(tickCount)) regenerator.regenerateNextBlocks();
                } else if (speed == 20) {
                    regenerator.regenerateNextBlocks();
                }
            }

        }, 0, 1);
    }

    public void registerExplosion(List<Material> lm, List<Location> ll) {
        explosion_buffer.add(ll);
        explosion_buffer_time.put(ll, getConfig().getInt("delay"));
        explosion_buffer_material.put(ll, lm);
    }

    public int getSpeed(int[] array, int input) {
        int lowestDiff = Integer.MAX_VALUE;
        int result = 0;
        for (int i : array) {
            int diff = Math.abs(input - i);
            if (diff < lowestDiff) {
                lowestDiff = diff;
                result = i;
            }
        }
        return result;
    }
}
