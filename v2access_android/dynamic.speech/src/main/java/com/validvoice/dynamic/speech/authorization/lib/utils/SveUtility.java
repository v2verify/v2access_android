package com.validvoice.dynamic.speech.authorization.lib.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import com.validvoice.dynamic.audio.AudioCodec;

public class SveUtility {

    public static String GenerateInteractionId() {
        Random random = new Random();
        StringBuilder builder = new StringBuilder();
        builder.append(GetTime());
        for(int i = 0; i < 5; ++i) {
            builder.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }

        return builder.toString();
    }

    private static String GetTime() {
        return new SimpleDateFormat("yyMMddHHmmss").format(new Date());
    }

    public static AudioCodec GetCodec(String algo) {
        switch(algo.toLowerCase()) {
            case "alaw": return AudioCodec.PCM_ALAW;
            case "pcm_little_endian": return AudioCodec.PCM_LE;
        }
        return AudioCodec.Unknown;
    }

    private static final String CHARS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

}
