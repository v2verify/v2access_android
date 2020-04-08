package com.validvoice.voxidem.scenes.home;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.validvoice.dynamic.cloud.CloudArrayAdapter;
import com.validvoice.dynamic.cloud.CloudImageLoader;
import com.validvoice.dynamic.scene.listeners.OnWasCheckedChangeListener;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.cloud.UserIntent;

import java.util.ArrayList;
import java.util.List;

class IntentArrayAdapter extends CloudArrayAdapter<UserIntent> {

    private static final String TAG = "IntentArrayAdapter";

    interface IntentsSelected {
        void onIntentsSelected();
        void onIntentsDeselected();
    }

    private IntentsSelected mIntentsSelected;
    private int mSelectedCount;
    private Handler mHandler;
    private CloudImageLoader mImageLoader;

    private Drawable mPlaceholder;
    private Drawable mWebMail;
    private Drawable mSecuredByVoice;
    private Drawable mV2Factor;
    private Drawable mV2OnDemand;

    IntentArrayAdapter(@NonNull Context context, @NonNull IntentsSelected intentsSelected) {
        super(context, R.layout.scene_home_list_row_intent, UserIntent.class);
        mIntentsSelected = intentsSelected;
        mSelectedCount = 0;
        mImageLoader = new CloudImageLoader("voxidem.icon");
        mImageLoader.setErrorImage(R.drawable.logo_icon_smallest);
        mImageLoader.setPlaceHolderImage(R.drawable.logo_icon_smallest);
        mHandler = new Handler();
        mHandler.post(new Runnable() {

            private List<UserIntent> mRemoveList = new ArrayList<>();

            @Override
            public void run() {
                if(getCount() > 0) {
                    for (int i = 0; i < getCount(); ++i) {
                        UserIntent io = getItemType(i);
                        if (!io.IsActive()) {
                            if(io.GetIsSelected()) {
                                io.SetIsSelected(false);
                                if (--mSelectedCount == 0) {
                                    mIntentsSelected.onIntentsDeselected();
                                }
                            }
                            mRemoveList.add(io);
                        }
                    }
                    removeAll(mRemoveList);
                    mRemoveList.clear();
                }
                mHandler.postDelayed(this, 1000);
            }
        });
        final Resources resources = context.getResources();
        final Resources.Theme theme = context.getTheme();
        mPlaceholder = ResourcesCompat.getDrawable(resources, R.drawable.logo_icon_smallest, theme);
        mWebMail = ResourcesCompat.getDrawable(resources, R.drawable.ic_webmail, theme);
        mSecuredByVoice = ResourcesCompat.getDrawable(resources, R.drawable.ic_securedbyvoice, theme);
        mV2Factor = ResourcesCompat.getDrawable(resources, R.drawable.ic_v2factor, theme);
        mV2OnDemand = ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, theme);
    }

    int getSelectedCount() {
        return mSelectedCount;
    }

    private class WasCheckedChangeListener extends OnWasCheckedChangeListener {

        private UserIntent mUserIntent;

        WasCheckedChangeListener(CompoundButton button) {
            super(button);
            mUserIntent = null;
        }

        void setIntentObject(UserIntent userIntent) {
            mUserIntent = userIntent;
        }

        @Override
        public void onWasCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(mUserIntent != null) {
                mUserIntent.SetIsSelected(isChecked);
                int c = 0;
                for(int i = 0; i < getCount(); ++i) {
                    UserIntent io = getItemType(i);
                    if(!io.equals(mUserIntent) &&
                            io.GetIntentId().equals(mUserIntent.GetIntentId())) {
                        ++c;
                        io.SetIsDisabled(isChecked);
                    }
                }
                if(c != 0) {
                    notifyDataSetChanged();
                }
            }

            if (isChecked) {
                if (++mSelectedCount == 1) {
                    mIntentsSelected.onIntentsSelected();
                }
            } else {
                if (--mSelectedCount == 0) {
                    mIntentsSelected.onIntentsDeselected();
                }
            }
        }
    }

    private class ViewHolder {
        ImageView company_icon;
        TextView device_nickname;
        TextView company_username;
      //  TextView expiration_time;
        CheckBox include_intent;
        WasCheckedChangeListener wasCheckedChangeListener;
    }

    @Override
    public Object newTag(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.company_icon = view.findViewById(R.id.company_icon);
        viewHolder.device_nickname = view.findViewById(R.id.device_nickname);
        viewHolder.company_username = view.findViewById(R.id.company_username);
     //   viewHolder.expiration_time = view.findViewById(R.id.expiration_time);
        viewHolder.include_intent = view.findViewById(R.id.include_intent);
        viewHolder.wasCheckedChangeListener = new WasCheckedChangeListener(viewHolder.include_intent);
        return viewHolder;
    }

    @Override
    public void adjustTag(Object tag, Context context, UserIntent item) {
        ViewHolder viewHolder = (ViewHolder)tag;
        switch(item.GetCompanyLogo().toLowerCase()) {
            case "webmail.png":
                viewHolder.company_icon.setImageDrawable(mWebMail);
                break;
            case "securedbyvoice.png":
                viewHolder.company_icon.setImageDrawable(mSecuredByVoice);
                break;
            case "v2factor.png":
                viewHolder.company_icon.setImageDrawable(mV2Factor);
                break;
            case "v2ondemand.png":
                viewHolder.company_icon.setImageDrawable(mV2OnDemand);
                break;
            default:
                //mImageLoader.loadImage(item.GetCompanyLogo().toLowerCase(), viewHolder.company_icon);
                viewHolder.company_icon.setImageDrawable(mPlaceholder);
                break;
        }
        viewHolder.device_nickname.setText(item.GetDeviceDisplay());
        viewHolder.company_username.setText(item.GetCompanyUser());
        viewHolder.include_intent.setEnabled(!item.GetIsDisabled());
      //  viewHolder.expiration_time.setText(item.GetTimeLeft());
        viewHolder.wasCheckedChangeListener.setIntentObject(item);
    }

}
