package com.validvoice.dynamic.audio;

import java.util.ArrayList;
import java.util.Arrays;

public class AudioBuffer {

    private class Segment {

        private static final int SEGMENT_SIZE = 8192;

        Segment next;   // used for free list

        byte[] data = new byte[SEGMENT_SIZE];

        int length = 0;

        boolean ContainsEnoughSpace(int sz) {
            return (length + sz) < SEGMENT_SIZE;
        }

    }

    private static Segment pool = null;
    private static final Object object = new Object();

    private int mBytesSize = 0;
    private ArrayList<Segment> mSegments = new ArrayList<>();

    public void write(byte[] data, int length) {
        Segment curr;
        if(mSegments.size() == 0 || !mSegments.get(mSegments.size()-1).ContainsEnoughSpace(length)) {
            curr = acquireSegment();
            mSegments.add(curr);
        } else {
            curr = mSegments.get(mSegments.size()-1);
        }

        System.arraycopy(data, 0, curr.data, curr.length, length);
        curr.length += length;
        mBytesSize += length;
    }

    public byte[] getFullBuffer() {
        int pos = 0;
        byte[] buffer = new byte[mBytesSize];
        for (Segment segment : mSegments) {
            System.arraycopy(segment.data, 0, buffer, pos, segment.length);
            pos += segment.length;
        }
        assertEquals(pos, mBytesSize);
        return buffer;
    }

    public byte[] getRepeatedBuffer(int repeat) {
        assertTrue(repeat >= 2);
        int pos = 0;
        byte[] buffer = new byte[mBytesSize * repeat];
        for(int i = 0; i < repeat; ++i) {
            for (Segment segment : mSegments) {
                System.arraycopy(segment.data, 0, buffer, pos, segment.length);
                pos += segment.length;
            }
        }
        assertEquals(pos, mBytesSize * repeat);
        return buffer;
    }

    public void clear() {
        for(Segment seg : mSegments) {
            releaseSegment(seg);
        }
        mSegments.clear();
        mBytesSize = 0;
    }

    public int size() {
        return mBytesSize;
    }

    private Segment acquireSegment() {
        Segment seg;
        if(pool == null) {
            seg = new Segment();
        } else {
            synchronized (object) {
                seg = pool;
                pool = seg.next;
                seg.next = null;
            }
        }
        seg.length = 0;
        Arrays.fill(seg.data, (byte)0);
        return seg;
    }

    private void releaseSegment(Segment seg) {
        seg.length = 0;
        Arrays.fill(seg.data, (byte)0);
        synchronized (object) {
            seg.next = pool;
            pool = seg;
        }
    }

    private void assertTrue(boolean condition) {
        if(!condition) {
            throw new AssertionError();
        }
    }

    private void assertEquals(int expected, int actual) {
        if(expected != actual) {
            throw new AssertionError();
        }
    }

}
