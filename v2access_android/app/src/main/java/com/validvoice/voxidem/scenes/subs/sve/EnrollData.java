package com.validvoice.voxidem.scenes.subs.sve;

import com.validvoice.dynamic.speech.authorization.SveEnroller;

class EnrollData {
    private String mSay;
    private SveEnroller.Result mResult;

    EnrollData(String say) {
        mSay = say;
        mResult = SveEnroller.Result.Unknown;
    }

    public String getSay() {
        return mSay;
    }

    public SveEnroller.Result getResult() {
        return mResult;
    }

    public void updateResult(SveEnroller.Result result) {
        mResult = result;
    }

}
