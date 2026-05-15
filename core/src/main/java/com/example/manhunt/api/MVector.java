package com.example.manhunt.api;

public class MVector {
    private final double x, y, z;

    public MVector(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }

    public MVector multiply(double factor) {
        return new MVector(x * factor, y * factor, z * factor);
    }

    public MVector setY(double y) {
        return new MVector(this.x, y, this.z);
    }
}
