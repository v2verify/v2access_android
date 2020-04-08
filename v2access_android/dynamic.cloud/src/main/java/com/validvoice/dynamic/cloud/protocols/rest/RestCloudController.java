package com.validvoice.dynamic.cloud.protocols.rest;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.validvoice.dynamic.cloud.BaseCloudController;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.cloud.ICloudImageLoader;
import com.validvoice.dynamic.cloud.ICloudObject;
import com.validvoice.dynamic.speech.authorization.lib.SveWebAgent;
import com.validvoice.dynamic.speech.authorization.lib.http.SveHttpResponse;
import com.validvoice.dynamic.cloud.ICloudMessage;

public class RestCloudController extends BaseCloudController {

    private SveWebAgent mWebAgent = new SveWebAgent();
    private static final String TAG = "RestCloudController";

    @Override
    public boolean sendMessage(ICloudMessage message, final CloudController.ResponseCallback callback) {
        if(message instanceof RestCloudMessage) {
            RestCloudMessage restMessage = (RestCloudMessage) message;
            switch(restMessage.getMethod()) {
                case GET:
                case LIST:
                    mWebAgent.get(restMessage.getRequestUrl(), new SveWebAgent.ResponseCallback() {
                        @Override
                        public void onResponse(SveHttpResponse response) {

                          //  Log.d("onResponse", "PrettyPrint"+new GsonBuilder().setPrettyPrinting().create().toJson(response));
                           ICloudObject io = TranslateResponse(response);
                            if(io instanceof CloudResult) {
                                callback.onResult((CloudResult)io);
                            } else if(io instanceof CloudError) {
                                callback.onError((CloudError)io);
                            } else {
                                callback.onFailure(new Exception("Unexpected ICloudObject type"));
                            }
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            callback.onFailure(ex);
                        }
                    });
                    return true;
                case POST:
                case CREATE:
                case VALIDATE:
                    mWebAgent.post(restMessage.getRequestUrl(), restMessage.getRequestBody(), new SveWebAgent.ResponseCallback() {
                        @Override
                        public void onResponse(SveHttpResponse response) {
                            ICloudObject io = TranslateResponse(response);
                            if(io instanceof CloudResult) {
                                callback.onResult((CloudResult)io);
                            } else if(io instanceof CloudError) {
                                callback.onError((CloudError)io);
                            } else {
                                callback.onFailure(new Exception("Unexpected ICloudObject type"));
                            }
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            callback.onFailure(ex);
                        }
                    });
                    return true;
                case PUT:
                case UPDATE:
                    mWebAgent.put(restMessage.getRequestUrl(), restMessage.getRequestBody(), new SveWebAgent.ResponseCallback() {
                        @Override
                        public void onResponse(SveHttpResponse response) {
                            ICloudObject io = TranslateResponse(response);
                            if(io instanceof CloudResult) {
                                callback.onResult((CloudResult)io);
                            } else if(io instanceof CloudError) {
                                callback.onError((CloudError)io);
                            } else {
                                callback.onFailure(new Exception("Unexpected ICloudObject type"));
                            }
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            callback.onFailure(ex);
                        }
                    });
                    return true;
                case DELETE:
                    mWebAgent.delete(restMessage.getRequestUrl(), new SveWebAgent.ResponseCallback() {
                        @Override
                        public void onResponse(SveHttpResponse response) {
                            ICloudObject io = TranslateResponse(response);
                            if(io instanceof CloudResult) {
                                callback.onResult((CloudResult)io);
                            } else if(io instanceof CloudError) {
                                callback.onError((CloudError)io);
                            } else {
                                callback.onFailure(new Exception("Unexpected ICloudObject type"));
                            }
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            callback.onFailure(ex);
                        }
                    });
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    @Override
    public ICloudMessage createCloudMessage() {
        return new RestCloudMessage();
    }

    @Override
    public ICloudImageLoader createCloudImageLoader() {
        return new PicassoCloudImageLoader();
    }

    private ICloudObject TranslateResponse(SveHttpResponse response) {
        if(response.getContentType() != null && response.getContentType().equals("application/json")) {
            return parseCloudObject(SveWebAgent.responseContentToJsonObject(response));
            //JsonObject o = SveWebAgent.responseContentToJsonObject(response);
            //if(o.has("__type")) {
            //    ICloudObject co = parseCloudObject(o);
            //    if (co != null) {
            //        return co;
            //    }
            //}
            //RestCloudMessage restMessage = new RestCloudMessage();
            //restMessage.setStatus(response.getStatusCode());
            //restMessage.setStatusDescription(response.getReasonPhrase());
            //for(Map.Entry<String, JsonElement> item : o.entrySet()) {
            //    JsonElement element = item.getValue();
            //    if(element.isJsonObject()) {
            //        JsonObject io = element.getAsJsonObject();
            //        if(io.has("__type")) {
            //            ICloudObject co = parseCloudObject(io);
            //            if (co != null) {
            //                restMessage.putObject(item.getKey(), co);
            //                continue;
            //            }
            //        }
            //        restMessage.putObject(item.getKey(), ParseJsonObject(io));
            //    } else if(element.isJsonArray()) {
            //        restMessage.putObject(item.getKey(), ParseJsonArray(element.getAsJsonArray()));
            //    } else if(element.isJsonPrimitive()) {
            //        JsonPrimitive primitive = element.getAsJsonPrimitive();
            //        if(primitive.isBoolean()) {
            //            restMessage.putBoolean(item.getKey(), primitive.getAsBoolean());
            //        } else if(primitive.isNumber()) {
            //            Number number = primitive.getAsNumber();
            //            if(number instanceof Byte) {
            //                restMessage.putInt(item.getKey(), number.byteValue());
            //            } else if(number instanceof Short) {
            //                restMessage.putInt(item.getKey(), number.shortValue());
            //            } else if(number instanceof Integer) {
            //                restMessage.putInt(item.getKey(), number.intValue());
            //            } else if(number instanceof Long) {
            //                restMessage.putLong(item.getKey(), number.longValue());
            //            } else if(number instanceof Float) {
            //                restMessage.putFloat(item.getKey(), number.floatValue());
            //            } else if(number instanceof Double) {
            //                restMessage.putDouble(item.getKey(), number.doubleValue());
            //            } else {
            //                // if nothing else, make this a double
            //                restMessage.putDouble(item.getKey(), number.doubleValue());
            //            }
            //        } else if(primitive.isString()) {
            //            restMessage.putString(item.getKey(), primitive.getAsString());
            //        }
            //    }
            //}
            //return restMessage;
        }
        return null;
    }

    //private HashMap<String, Object> ParseJsonObject(JsonObject o) {
    //    HashMap<String, Object> map = new HashMap<>();
    //    for(Map.Entry<String, JsonElement> item : o.entrySet()) {
    //        JsonElement element = item.getValue();
    //        if(element.isJsonObject()) {
    //            JsonObject io = element.getAsJsonObject();
    //            if(io.has("__type")) {
    //                ICloudObject co = parseCloudObject(io);
    //                if (co != null) {
    //                    map.put(item.getKey(), co);
    //                    continue;
    //                }
    //            }
    //            map.put(item.getKey(), ParseJsonObject(io));
    //        } else if(element.isJsonArray()) {
    //            map.put(item.getKey(), ParseJsonArray(element.getAsJsonArray()));
    //        } else if(element.isJsonPrimitive()) {
    //            JsonPrimitive primitive = element.getAsJsonPrimitive();
    //            if(primitive.isBoolean()) {
    //                map.put(item.getKey(), primitive.getAsBoolean());
    //            } else if(primitive.isNumber()) {
    //                Number number = primitive.getAsNumber();
    //                if(number instanceof Byte) {
    //                    map.put(item.getKey(), number.byteValue());
    //                } else if(number instanceof Short) {
    //                    map.put(item.getKey(), number.shortValue());
    //                } else if(number instanceof Integer) {
    //                    map.put(item.getKey(), number.intValue());
    //                } else if(number instanceof Long) {
    //                    map.put(item.getKey(), number.longValue());
    //                } else if(number instanceof Float) {
    //                    map.put(item.getKey(), number.floatValue());
    //                } else if(number instanceof Double) {
    //                    map.put(item.getKey(), number.doubleValue());
    //                } else {
    //                    // if nothing else, make this a double
    //                    map.put(item.getKey(), number.doubleValue());
    //                }
    //            } else if(primitive.isString()) {
    //                map.put(item.getKey(), primitive.getAsString());
    //            }
    //        }
    //    }
    //    return map;
    //}

    //private List<Object> ParseJsonArray(JsonArray a) {
    //    List<Object> list = new ArrayList<>();
    //    for(int i = 0; i < a.size(); ++i) {
    //        JsonElement element = a.get(i);
    //        if(element.isJsonObject()) {
    //            JsonObject io = element.getAsJsonObject();
    //            if(io.has("__type")) {
    //                ICloudObject co = parseCloudObject(io);
    //                if (co != null) {
    //                    list.add(co);
    //                    continue;
    //                }
    //            }
    //            list.add(ParseJsonObject(io));
    //        } else if(element.isJsonArray()) {
    //            list.add(ParseJsonArray(element.getAsJsonArray()));
    //        } else if(element.isJsonPrimitive()) {
    //            JsonPrimitive primitive = element.getAsJsonPrimitive();
    //            if(primitive.isBoolean()) {
    //                list.add(primitive.getAsBoolean());
    //            } else if(primitive.isNumber()) {
    //                Number number = primitive.getAsNumber();
    //                if(number instanceof Byte) {
    //                    list.add(number.byteValue());
    //                } else if(number instanceof Short) {
    //                    list.add(number.shortValue());
    //                } else if(number instanceof Integer) {
    //                    list.add(number.intValue());
    //                } else if(number instanceof Long) {
    //                    list.add(number.longValue());
    //                } else if(number instanceof Float) {
    //                    list.add(number.floatValue());
    //                } else if(number instanceof Double) {
    //                    list.add(number.doubleValue());
    //                } else {
    //                    // if nothing else, make this a double
    //                    list.add(number.doubleValue());
    //                }
    //            } else if(primitive.isString()) {
    //                list.add(primitive.getAsString());
    //            }
    //        }
    //    }
    //    return list;
    //}
}
