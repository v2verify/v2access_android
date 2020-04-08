package com.validvoice.voxidem.utils;

public class Coordinator implements Runnable {

    public interface Callback {
        boolean onUpdate();
    }

    private final Object mLock = new Object();
    private final Callback mCallback;

    private Thread mThread;
    private boolean mIsUpdated = false;

    public Coordinator(Callback c) {
        mCallback = c;
    }

    @Override
    public void run() {

        while(true) {
            synchronized(mLock) {
                if(mThread.isInterrupted()) {
                    break;
                }
            }
            if(waitForUpdate()) {
                if(mCallback.onUpdate()) {
                    return;
                }
            }
        }

    }

    private boolean waitForUpdate() {
        synchronized(mLock) {
            try {
                mLock.wait(1000);
                boolean isUpdated = mIsUpdated;
                mIsUpdated = false;
                return isUpdated;
            } catch(InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    public void start() {
        if(mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }
        mThread = new Thread(this);
        mThread.start();
    }

    public void stop() {
        if(mThread != null && mThread.isAlive()) {
            mThread.interrupt();
        }
    }

    public void update() {
        synchronized(mLock) {
            mIsUpdated = true;
            mLock.notify();
        }
    }

}
