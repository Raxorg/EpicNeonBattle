package com.badlogic.gdx.math;

import java.util.Random;

public class RandomXS128 extends Random {
    private static final double NORM_DOUBLE = 1.1102230246251565E-16d;
    private static final double NORM_FLOAT = 5.960464477539063E-8d;
    private long seed0;
    private long seed1;

    public RandomXS128() {
        setSeed(new Random().nextLong());
    }

    public RandomXS128(long seed) {
        setSeed(seed);
    }

    public RandomXS128(long seed0, long seed1) {
        setState(seed0, seed1);
    }

    public long nextLong() {
        long s1 = this.seed0;
        long s0 = this.seed1;
        this.seed0 = s0;
        s1 ^= s1 << 23;
        long j = ((s1 ^ s0) ^ (s1 >>> 17)) ^ (s0 >>> 26);
        this.seed1 = j;
        return j + s0;
    }

    protected final int next(int bits) {
        return (int) (nextLong() & ((1 << bits) - 1));
    }

    public int nextInt() {
        return (int) nextLong();
    }

    public int nextInt(int n) {
        return (int) nextLong((long) n);
    }

    public long nextLong(long n) {
        if (n <= 0) {
            throw new IllegalArgumentException("n must be positive");
        }
        long value;
        long bits;
        do {
            bits = nextLong() >>> 1;
            value = bits % n;
        } while ((bits - value) + (n - 1) < 0);
        return value;
    }

    public double nextDouble() {
        return ((double) (nextLong() >>> 11)) * NORM_DOUBLE;
    }

    public float nextFloat() {
        return (float) (((double) (nextLong() >>> 40)) * NORM_FLOAT);
    }

    public boolean nextBoolean() {
        return (nextLong() & 1) != 0;
    }

    public void nextBytes(byte[] bytes) {
        int i = bytes.length;
        while (i != 0) {
            int n;
            if (i < 8) {
                n = i;
            } else {
                n = 8;
            }
            long bits = nextLong();
            int n2 = n;
            while (true) {
                n = n2 - 1;
                if (n2 != 0) {
                    i--;
                    bytes[i] = (byte) ((int) bits);
                    bits >>= 8;
                    n2 = n;
                }
            }
        }
    }

    public void setSeed(long seed) {
        if (seed == 0) {
            seed = Long.MIN_VALUE;
        }
        long seed0 = murmurHash3(seed);
        setState(seed0, murmurHash3(seed0));
    }

    public void setState(long seed0, long seed1) {
        this.seed0 = seed0;
        this.seed1 = seed1;
    }

    public long getState(int seed) {
        return seed == 0 ? this.seed0 : this.seed1;
    }

    private static final long murmurHash3(long x) {
        x = (x ^ (x >>> 33)) * -49064778989728563L;
        x = (x ^ (x >>> 33)) * -4265267296055464877L;
        return x ^ (x >>> 33);
    }
}
