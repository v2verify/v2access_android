package com.validvoice.voxidem.scenes.accounts;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneCursorAdapter;
import com.validvoice.dynamic.scene.listeners.OnWasClickedListener;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.models.AccountModel;

public class AccountsCursorAdapter extends SceneCursorAdapter {

    private Drawable mWebMail;
    private Drawable mSecuredByVoice;
    private Drawable mV2Factor;
    private Drawable mV2OnDemand;

    AccountsCursorAdapter(SceneController sceneController, int flags) {
        super(sceneController, R.layout.scene_accounts_list_row_item, flags);

        final Resources resources = getContext().getResources();
        final Resources.Theme theme = getContext().getTheme();
        mWebMail = ResourcesCompat.getDrawable(resources, R.drawable.ic_webmail, theme);
        mSecuredByVoice = ResourcesCompat.getDrawable(resources, R.drawable.ic_securedbyvoice, theme);
        mV2Factor = ResourcesCompat.getDrawable(resources, R.drawable.ic_v2factor, theme);
        mV2OnDemand = ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, theme);
    }

    private class WasClickedListener extends OnWasClickedListener {

        private AccountModel mAccountModel;

        void setModel(AccountModel model) {
            mAccountModel = model;
        }

        public WasClickedListener(View view) {
            super(view);
            mAccountModel = null;
        }

        @Override
        public void onWasClicked(View view) {
            if(mAccountModel != null) {
                // AsyncTask do logout animation
            }
        }
    }

    private class ViewHolder {
        ImageView accounts_company_icon;
        TextView accounts_company_name;
        TextView accounts_company_username;
        FrameLayout accounts_company_logout_frame;
        Button accounts_company_logout;
        WasClickedListener wasClickedListener;
    }

    @Override
    public Object newTag(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.accounts_company_icon = (ImageView) view.findViewById(R.id.accounts_company_icon);
        viewHolder.accounts_company_name = (TextView) view.findViewById(R.id.accounts_company_name);
        viewHolder.accounts_company_username = (TextView) view.findViewById(R.id.accounts_company_username);
        viewHolder.accounts_company_logout_frame = (FrameLayout) view.findViewById(R.id.accounts_company_logout_frame);
        viewHolder.accounts_company_logout = (Button) view.findViewById(R.id.accounts_company_logout);
        viewHolder.wasClickedListener = new WasClickedListener(viewHolder.accounts_company_logout);
        return viewHolder;
    }

    @Override
    public void adjustTag(Object tag, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)tag;
        AccountModel model = AccountModel.createRecordFromCursor(cursor);
        switch(model.getCompanyLogo().toLowerCase()) {
            case "webmail.png":
                viewHolder.accounts_company_icon.setImageDrawable(mWebMail);
                break;
            case "securedbyvoice.png":
                viewHolder.accounts_company_icon.setImageDrawable(mSecuredByVoice);
                break;
            case "v2factor.png":
                viewHolder.accounts_company_icon.setImageDrawable(mV2Factor);
                break;
            case "v2ondemand.png":
                viewHolder.accounts_company_icon.setImageDrawable(mV2OnDemand);
                break;
            default:
                viewHolder.accounts_company_icon.setImageDrawable(null);
                break;
        }
        viewHolder.accounts_company_name.setText(model.getCompanyName());
        viewHolder.accounts_company_username.setText(model.getAccountUsername());
        viewHolder.accounts_company_logout_frame.setVisibility(View.INVISIBLE);
        viewHolder.accounts_company_logout.setVisibility(View.INVISIBLE);
        viewHolder.wasClickedListener.setModel(null);
    }

}
