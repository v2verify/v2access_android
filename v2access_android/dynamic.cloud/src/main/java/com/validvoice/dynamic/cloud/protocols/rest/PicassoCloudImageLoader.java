package com.validvoice.dynamic.cloud.protocols.rest;

import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.ICloudImageLoader;
import com.validvoice.dynamic.speech.SpeechApi;

import okhttp3.HttpUrl;

public class PicassoCloudImageLoader implements ICloudImageLoader {

    private CloudController.Version mVersion = CloudController.Version.Unknown;
    private String mService = "";
    private String mCachedQuery = "";
    private int mErrorImageResId = -1;
    private int mPlaceHolderImageResId = -1;

    @Override
    public void setVersion(CloudController.Version version) {
        mVersion = version;
    }

    @Override
    public void setService(String service) {
        mService = service;
    }

    @Override
    public void setErrorImage(@DrawableRes int errorImageResId) {
        mErrorImageResId = errorImageResId;
    }

    @Override
    public void setPlaceHolderImage(@DrawableRes int placeholderImageResId) {
        mPlaceHolderImageResId = placeholderImageResId;
    }

    @Override
    public void loadImage(String name, ImageView imageView) {
        String server = "";

        try {
            server = SpeechApi.getServer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        HttpUrl url = HttpUrl.parse(server)
                .newBuilder()
                .addPathSegments(getQuery(name))
                .build();

        RequestCreator creator = Picasso.get().load(url.toString());

        if(mErrorImageResId != -1) {
            creator.error(mErrorImageResId);
        }

        if(mPlaceHolderImageResId != -1) {
            creator.placeholder(mPlaceHolderImageResId);
        }

        creator.into(imageView);
    }

    private String getQuery(String name) {
        if(mCachedQuery.isEmpty()) {
            String service = mService.replace('.', '/');
            if (mVersion == CloudController.Version.Unknown) {
                mCachedQuery = "cloud/" + service + "/";
            } else {
                mCachedQuery = mVersion.ordinal() + "/cloud/" + service + "/";
            }
        }
        return mCachedQuery + name;
    }
}
