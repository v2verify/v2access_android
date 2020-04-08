package com.validvoice.voxidem.cloud;

import com.google.gson.JsonObject;
import com.validvoice.dynamic.cloud.ICloudObject;

public class QrUserAccount implements ICloudObject {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String TYPE = "type";

    public enum Type {
        Account,
        Hardware,
        Link
    }

    private final int mId;
    private final String mName;
    private final Type mType;

    private QrUserAccount(int id, String name, Type type) {
        mId = id;
        mName = name;
        mType = type;
    }

    public final int getId() {
        return mId;
    }

    public final String getName() {
        return mName;
    }

    public final Type getType() {
        return mType;
    }

    public static class Factory implements ICloudObject.IFactory {

        @Override
        public String getObjectId() {
            return "QrUserAccount";
        }

        @Override
        public Class<?> getClassType() {
            return QrUserAccount.class;
        }

        @Override
        public ICloudObject createObject(JsonObject object, ICloudObject.IFactoryParser parser) {
            int id = object.get(ID).getAsJsonPrimitive().getAsInt();
            String name = object.get(NAME).getAsJsonPrimitive().getAsString();
            char type = object.get(TYPE).getAsJsonPrimitive().getAsCharacter();
            Type acct_type = Type.Account;
            switch(type) {
                case 'H':
                case 'h':
                    acct_type = Type.Hardware;
                    break;
                case 'L':
                case 'l':
                    acct_type = Type.Link;
                    break;
            }
            return new QrUserAccount(id, name, acct_type);
        }
    }

}
