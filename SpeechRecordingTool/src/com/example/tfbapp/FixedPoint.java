package com.example.tfbapp;

public class FixedPoint {

    long FIXED_ZERO;
    long FIXED_1;
    long FIXED_2;
    long FIXED_4;
    long FIXED_MINUS1;
    long FIXED_HALF;
    long piFx;
    long FIX13_2PI;
    long FIX13_R2PI;
    long FIXED_POINT54;
    long FIXED_POINT46;

    FixedPoint() {
        FIXED_ZERO = ConvertInt2S13(0);
        FIXED_1 = ConvertInt2S13(1);
        FIXED_2 = ConvertInt2S13(2);
        FIXED_4 = ConvertInt2S13(4);
        FIXED_MINUS1 = ConvertInt2S13(-1);
        FIXED_HALF = ConvertFloat2S13(0.5f);
        piFx = ConvertFloat2S13((float) Math.PI);
        FIX13_2PI = ConvertFloat2S13((float) (2 * Math.PI));
        FIX13_R2PI = ConvertFloat2S13(1.0f / (float) (2 * Math.PI));
        FIXED_POINT54 = ConvertFloat2S13(0.54f);
        FIXED_POINT46 = ConvertFloat2S13(0.46f);
    }

    public long ConvertFloat2S13(float a) {
        return (long) (0x2000 * a);
    }

    public long ConvertFloat2S16(float a) {
        return (long) (0x10000 * a);
    }

    public long MpyS13ByS13RoundS13(long a, long b) {
        return (long) (((a * b) + (1 << 12)) >> 13);
    }

    public long ConvertInt2S13(int a) {
        return ((long) (0x2000 * a));
    }

    public int ConvertS132Int(long a) {
        return ((int) a / 0x2000);
    }

    public long ConvertS132S16(long a) {
        return ((long) (0x8 * a));
    }

    public float ConvertS132Float(long a) {
        return ((float) (long) (a) / 0x2000);
    }

    public long MpyS16ByS16RoundS16(long a, long b) {
        return ((long) ((((long) (long) (a) * (long) (long) (b)) + (1 << 15)) >> 16));
    }

    public long DvdS13ByS13RoundS13(long a, long b) {
        return (long) (0x2000 * a / b);
    }
}
