package com.validvoice.dynamic.speech.authorization.lib.http.body;

import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpBody;

import java.io.IOException;
import java.io.OutputStream;

public class SveNullHttpBody extends SveHttpBody {

    public SveNullHttpBody() {
        super("text/plain", 0);
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {

    }
}
