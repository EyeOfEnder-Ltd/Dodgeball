package com.eyeofender.dodgeball.util;

import java.io.Serializable;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class SerializableLocation implements Serializable {

    private static final long serialVersionUID = 1L;

    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private String worldName;

    public SerializableLocation(Location location, boolean center) {
        if (location == null) return;
        if (center) location = asBlockLocation(location);
        worldName = location.getWorld() == null ? "null" : location.getWorld().getName();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();
    }

    public static Location asBlockLocation(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());
    }

    public Location asBukkitLocation() {
        World world = Bukkit.getWorld(worldName);
        return world == null ? null : new Location(world, x, y, z, yaw, pitch);
    }

    public boolean equals(Object loc) {
        if (!(loc instanceof SerializableLocation)) return false;
        SerializableLocation compare = (SerializableLocation) loc;
        if (x == compare.x && y == compare.y && z == compare.z && yaw == compare.yaw && pitch == compare.pitch && worldName.equalsIgnoreCase(compare.worldName)) return true;
        return false;
    }
}
