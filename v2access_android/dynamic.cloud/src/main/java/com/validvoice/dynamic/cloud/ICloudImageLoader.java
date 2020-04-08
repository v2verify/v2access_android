package com.validvoice.dynamic.cloud;

import android.support.annotation.DrawableRes;
import android.widget.ImageView;

public interface ICloudImageLoader {

    void setVersion(CloudController.Version version);
    void setService(String service);

    void setErrorImage(@DrawableRes int errorImageResId);
    void setPlaceHolderImage(@DrawableRes int placeholderImageResId);

    void loadImage(String image, ImageView imageView);
}
