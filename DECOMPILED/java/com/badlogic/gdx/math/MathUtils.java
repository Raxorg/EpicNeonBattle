package com.badlogic.gdx.math;

import java.util.Random;

public final class MathUtils {
    private static final double BIG_ENOUGH_CEIL = 16384.999999999996d;
    private static final double BIG_ENOUGH_FLOOR = 16384.0d;
    private static final int BIG_ENOUGH_INT = 16384;
    private static final double BIG_ENOUGH_ROUND = 16384.5d;
    private static final double CEIL = 0.9999999d;
    public static final float E = 2.7182817f;
    public static final float FLOAT_ROUNDING_ERROR = 1.0E-6f;
    public static final float PI = 3.1415927f;
    public static final float PI2 = 6.2831855f;
    private static final int SIN_BITS = 14;
    private static final int SIN_COUNT = 16384;
    private static final int SIN_MASK = 16383;
    private static final float degFull = 360.0f;
    public static final float degRad = 0.017453292f;
    private static final float degToIndex = 45.511112f;
    public static final float degreesToRadians = 0.017453292f;
    public static final float nanoToSec = 1.0E-9f;
    public static final float radDeg = 57.295776f;
    private static final float radFull = 6.2831855f;
    private static final float radToIndex = 2607.5945f;
    public static final float radiansToDegrees = 57.295776f;
    public static Random random = new RandomXS128();

    private static class Sin {
        static final float[] table = new float[MathUtils.SIN_COUNT];

        private Sin() {
        }

        static {
            int i;
            for (i = 0; i < MathUtils.SIN_COUNT; i++) {
                table[i] = (float) Math.sin((double) (((((float) i) + 0.5f) / 16384.0f) * MathUtils.radFull));
            }
            for (i = 0; i < 360; i += 90) {
                table[((int) (((float) i) * MathUtils.degToIndex)) & MathUtils.SIN_MASK] = (float) Math.sin((double) (((float) i) * MathUtils.degreesToRadians));
            }
        }
    }

    public static float sin(float radians) {
        return Sin.table[((int) (radToIndex * radians)) & SIN_MASK];
    }

    public static float cos(float radians) {
        return Sin.table[((int) ((1.5707964f + radians) * radToIndex)) & SIN_MASK];
    }

    public static float sinDeg(float degrees) {
        return Sin.table[((int) (degToIndex * degrees)) & SIN_MASK];
    }

    public static float cosDeg(float degrees) {
        return Sin.table[((int) ((90.0f + degrees) * degToIndex)) & SIN_MASK];
    }

    public static float atan2(float y, float x) {
        if (x != 0.0f) {
            float z = y / x;
            float atan;
            if (Math.abs(z) < 1.0f) {
                atan = z / (((0.28f * z) * z) + 1.0f);
                if (x >= 0.0f) {
                    return atan;
                }
                return y < 0.0f ? atan - PI : atan + PI;
            }
            atan = 1.5707964f - (z / ((z * z) + 0.28f));
            return y < 0.0f ? atan - PI : atan;
        } else if (y > 0.0f) {
            return 1.5707964f;
        } else {
            if (y == 0.0f) {
                return 0.0f;
            }
            return -1.5707964f;
        }
    }

    public static int random(int range) {
        return random.nextInt(range + 1);
    }

    public static int random(int start, int end) {
        return random.nextInt((end - start) + 1) + start;
    }

    public static long random(long range) {
        return (long) (random.nextDouble() * ((double) range));
    }

    public static long random(long start, long end) {
        return ((long) (random.nextDouble() * ((double) (end - start)))) + start;
    }

    public static boolean randomBoolean() {
        return random.nextBoolean();
    }

    public static boolean randomBoolean(float chance) {
        return random() < chance;
    }

    public static float random() {
        return random.nextFloat();
    }

    public static float random(float range) {
        return random.nextFloat() * range;
    }

    public static float random(float start, float end) {
        return (random.nextFloat() * (end - start)) + start;
    }

    public static int randomSign() {
        return (random.nextInt() >> 31) | 1;
    }

    public static float randomTriangular() {
        return random.nextFloat() - random.nextFloat();
    }

    public static float randomTriangular(float max) {
        return (random.nextFloat() - random.nextFloat()) * max;
    }

    public static float randomTriangular(float min, float max) {
        return randomTriangular(min, max, ((max - min) * 0.5f) + min);
    }

    public static float randomTriangular(float min, float max, float mode) {
        float u = random.nextFloat();
        float d = max - min;
        if (u <= (mode - min) / d) {
            return ((float) Math.sqrt((double) ((u * d) * (mode - min)))) + min;
        }
        return max - ((float) Math.sqrt((double) (((1.0f - u) * d) * (max - mode))));
    }

    public static int nextPowerOfTwo(int value) {
        if (value == 0) {
            return 1;
        }
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        return (value | (value >> 16)) + 1;
    }

    public static boolean isPowerOfTwo(int value) {
        return value != 0 && ((value - 1) & value) == 0;
    }

    public static short clamp(short value, short min, short max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    public static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    public static long clamp(long value, long min, long max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    public static float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    public static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        return value > max ? max : value;
    }

    public static float lerp(float fromValue, float toValue, float progress) {
        return ((toValue - fromValue) * progress) + fromValue;
    }

    public static float lerpAngle(float fromRadians, float toRadians, float progress) {
        return ((((((((toRadians - fromRadians) + radFull) + PI) % radFull) - PI) * progress) + fromRadians) + radFull) % radFull;
    }

    public static float lerpAngleDeg(float fromDegrees, float toDegrees, float progress) {
        return ((((((((toDegrees - fromDegrees) + degFull) + 180.0f) % degFull) - 180.0f) * progress) + fromDegrees) + degFull) % degFull;
    }

    public static int floor(float value) {
        return ((int) (((double) value) + BIG_ENOUGH_FLOOR)) - 16384;
    }

    public static int floorPositive(float value) {
        return (int) value;
    }

    public static int ceil(float value) {
        return ((int) (((double) value) + BIG_ENOUGH_CEIL)) - 16384;
    }

    public static int ceilPositive(float value) {
        return (int) (((double) value) + CEIL);
    }

    public static int round(float value) {
        return ((int) (((double) value) + BIG_ENOUGH_ROUND)) - 16384;
    }

    public static int roundPositive(float value) {
        return (int) (0.5f + value);
    }

    public static boolean isZero(float value) {
        return Math.abs(value) <= FLOAT_ROUNDING_ERROR;
    }

    public static boolean isZero(float value, float tolerance) {
        return Math.abs(value) <= tolerance;
    }

    public static boolean isEqual(float a, float b) {
        return Math.abs(a - b) <= FLOAT_ROUNDING_ERROR;
    }

    public static boolean isEqual(float a, float b, float tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    public static float log(float a, float value) {
        return (float) (Math.log((double) value) / Math.log((double) a));
    }

    public static float log2(float value) {
        return log(2.0f, value);
    }
}
