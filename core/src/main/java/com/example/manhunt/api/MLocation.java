package com.example.manhunt.api;

public class MLocation {
    private final String worldName;
    private final double x, y, z;
    private final float yaw, pitch;

    public MLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public String getWorldName() { return worldName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public float getYaw() { return yaw; }
    public float getPitch() { return pitch; }

    public double distanceSquared(MLocation other) {
        if (!other.worldName.equals(this.worldName)) return Double.MAX_VALUE;
        return Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2) + Math.pow(z - other.z, 2);
    }

    public double distance(MLocation other) {
        return Math.sqrt(distanceSquared(other));
    }
}
