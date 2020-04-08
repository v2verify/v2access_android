package com.validvoice.voxidem.scenes.subs.sve;

import com.validvoice.dynamic.speech.authorization.SveVerifier;

class VerifyData {

    private String mSay;
    private SveVerifier.Result mResult;

    VerifyData(String say) {
        mSay = say;
        mResult = SveVerifier.Result.Unknown;
    }

    public String getSay() {
        return mSay;
    }

    public SveVerifier.Result getResult() {
        return mResult;
    }

    public void updateResult(SveVerifier.Result result) {
        mResult = result;
    }

}
