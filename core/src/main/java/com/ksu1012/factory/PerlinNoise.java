package com.ksu1012.factory;

import java.util.Random;

public class PerlinNoise {
    private final int[] p;
    private final int[] permutation;

    public PerlinNoise(long seed) {
        Random r = new Random(seed);
        permutation = new int[256];
        p = new int[512];
        for (int i = 0; i < 256; i++) permutation[i] = i;
        for (int i = 0; i < 256; i++) {
            int index = r.nextInt(256 - i) + i;
            int temp = permutation[i];
            permutation[i] = permutation[index];
            permutation[index] = temp;
        }
        for (int i = 0; i < 512; i++) p[i] = permutation[i % 256];
    }

    public double noise(double x, double y, double frequency) {
        x *= frequency;
        y *= frequency;
        int X = (int) Math.floor(x) & 255;
        int Y = (int) Math.floor(y) & 255;
        x -= Math.floor(x);
        y -= Math.floor(y);
        double u = fade(x);
        double v = fade(y);
        int A = p[X] + Y, AA = p[A], AB = p[A + 1], B = p[X + 1] + Y, BA = p[B], BB = p[B + 1];
        return lerp(v, lerp(u, grad(p[AA], x, y), grad(p[BA], x - 1, y)),
            lerp(u, grad(p[AB], x, y - 1), grad(p[BB], x - 1, y - 1)));
    }

    public double getFractalNoise(double x, double y, double frequency, int octaves, double persistence) {
        double total = 0;
        double amplitude = 1;
        double maxValue = 0;  // Used for normalizing result to 0.0 - 1.0

        for(int i=0; i<octaves; i++) {
            total += noise(x, y, frequency) * amplitude;
            maxValue += amplitude;

            amplitude *= persistence;
            frequency *= 2;
        }

        return total / maxValue;
    }

    private double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private double lerp(double t, double a, double b) { return a + t * (b - a); }
    private double grad(int hash, double x, double y) {
        int h = hash & 15;
        double u = h < 8 ? x : y, v = h < 4 ? y : h == 12 || h == 14 ? x : 0;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }
}
