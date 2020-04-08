package com.validvoice.dynamic.cloud;

import android.support.annotation.DrawableRes;
import android.widget.ImageView;

public class CloudImageLoader {

    private ICloudImageLoader mCloudImageLoader;

    public CloudImageLoader(String service) {
        mCloudImageLoader = CloudController.createCloudImageLoader();
        mCloudImageLoader.setVersion(CloudController.defaultVersion());
        mCloudImageLoader.setService(service);
    }

    public void setErrorImage(@DrawableRes int errorImageResId) {
        mCloudImageLoader.setErrorImage(errorImageResId);
    }

    public void setPlaceHolderImage(@DrawableRes int placeholderImageResId) {
        mCloudImageLoader.setPlaceHolderImage(placeholderImageResId);
    }

    public void loadImage(String url, ImageView imageView) {
        mCloudImageLoader.loadImage(url, imageView);
    }

}
