package com.eyeofender.dodgeball.util;

import java.io.Serializable;

import org.bukkit.Location;
import org.bukkit.World;

public class Region implements Serializable {

    private static final long serialVersionUID = 1L;

    private double x1, z1, x2, z2;
    private String worldName;

    public Region(Location loc1, Location loc2) {
        double swap;
        x1 = loc1.getX();
        x2 = loc2.getX();
        if (x1 > x2) {
            swap = x1;
            x1 = x2;
            x2 = swap;
        }
        z1 = loc1.getZ();
        z2 = loc2.getZ();
        if (z1 > z2) {
            swap = z1;
            z1 = z2;
            z2 = swap;
        }
        if (loc1.getWorld() != null)
            worldName = loc1.getWorld().getName();
        else
            worldName = loc2.getWorld() == null ? "" : loc2.getWorld().getName();
    }

    public Region(World world, double x1, double z1, double x2, double z2) {
        double swap;
        if (x1 > x2) {
            swap = x1;
            x1 = x2;
            x2 = swap;
        }
        this.x1 = x1;
        this.x2 = x2;
        if (z1 > z2) {
            swap = z1;
            z1 = z2;
            z2 = swap;
        }
        this.z1 = z1;
        this.z2 = z2;
        worldName = world == null ? "" : world.getName();
    }

    public boolean contains(Location loc) {
        return loc.getX() >= x1 && loc.getX() <= x2 && loc.getZ() >= z1 && loc.getZ() <= z2;
    }

    public double getX1() {
        return x1;
    }

    public double getZ1() {
        return z1;
    }

    public double getX2() {
        return x2;
    }

    public double getZ2() {
        return z2;
    }

    public String getWorldName() {
        return worldName;
    }
}
