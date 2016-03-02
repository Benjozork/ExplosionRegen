package me.benjozork.explosionregen;

import me.benjozork.explosionregen.listeners.ExplosionListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

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
                regenerator.regenerateNextBlocks();
                System.out.println(speed);
            }

        }, 0, 1);
    }

    public void registerExplosion(List<Material> lm, List<Location> ll) {
        explosion_buffer.add(ll);
        explosion_buffer_time.put(ll, getConfig().getInt("delay") * 20);
        explosion_buffer_material.put(ll, lm);
        if (explosion_buffer.size() > 1 && speed > 6) {
            speed = speed - 10;
        }
    }
}
