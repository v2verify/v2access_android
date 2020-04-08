/*
package com.validvoice.dynamic.speech.sve.lib.tcmp;

import com.validvoice.dynamic.tcmp.TCMPProtocol;
import com.validvoice.dynamic.tcmp.types.DataType;

public class SveProtocol extends TCMPProtocol {

    public static final String SVE_VERSION = "sve.version";
    public static final String SVE_METHOD = "sve.method";
    public static final String SVE_DEVELOPER_KEY = "sve.developer.key";
    public static final String SVE_APPLICATION_KEY = "sve.application.key";
    public static final String SVE_INTERACTION_ID = "sve.interaction.id";
    public static final String SVE_CLIENT_ID = "sve.client.id";
    public static final String SVE_SESSION_ID = "sve.session.id";
    public static final String SVE_SUB_POP = "sve.subpop";
    public static final String SVE_ERROR = "sve.error";
    public static final String SVE_RESULT_TYPE = "sve.result.type";
    public static final String SVE_RESULT = "sve.result";
    public static final String SVE_REASON = "sve.reason";
    public static final String SVE_VOICE = "sve.voice";
    public static final String SVE_EXPECTED_TEXT = "sve.expected.text";
    public static final String SVE_CAPTURED = "sve.captured.text";
    public static final String SVE_IS_ALIVE = "sve.is.alive";

    public SveProtocol() {
        super();
        AddField(SVE_VERSION, DataType.Code.VariableString);
        AddField(SVE_METHOD, DataType.Code.VariableString);
        AddField(SVE_DEVELOPER_KEY, DataType.Code.VariableString);
        AddField(SVE_APPLICATION_KEY, DataType.Code.VariableString);
        AddField(SVE_INTERACTION_ID, DataType.Code.VariableString);
        AddField(SVE_CLIENT_ID, DataType.Code.VariableString);
        AddField(SVE_SESSION_ID, DataType.Code.VariableString);
        AddField(SVE_SUB_POP, DataType.Code.Signed8);
        AddField(SVE_ERROR, DataType.Code.Variable32);
        AddField(SVE_RESULT_TYPE, DataType.Code.VariableString);
        AddField(SVE_RESULT, DataType.Code.VariableString);
        AddField(SVE_REASON, DataType.Code.VariableString);
        AddField(SVE_VOICE, DataType.Code.VariableMessage);
        AddField(SVE_EXPECTED_TEXT, DataType.Code.MiniString);
        AddField(SVE_CAPTURED, DataType.Code.MiniString);
        AddField(SVE_IS_ALIVE, DataType.Code.Bool);

        // For right now
        DisableChangeControl();
    }

}
*/