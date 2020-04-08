package com.validvoice.voxidem.scenes.accounts;

import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.contracts.HistoryContract;
import com.validvoice.voxidem.db.models.AccountModel;
import com.validvoice.voxidem.db.models.HistoryModel;
import com.validvoice.voxidem.scenes.history.HistoryCursorAdapter;

import java.text.DateFormat;
import java.util.Date;

public class AccountDetailsFragment extends SceneFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = AccountDetailsFragment.class.getSimpleName();

    private SlidingUpPanelLayout mSlidingLayout;
    private ImageView mCompanyIcon;
    private TextView mCompanyName;
    private TextView mAccountName;
    private TextView mAccountCompany;
    private TextView mAccountCompanyUser;
    private TextView mAccountDateLinked;
    private TextView mAccountDateLastSignedIn;
    private TextView mAccountLastSignedInFrom;
    private ListView mListView;
    private HistoryCursorAdapter mHistoryCursorAdapter;

    private AccountModel mAccountModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_details, container, false);
        mSlidingLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        mCompanyIcon = (ImageView)view.findViewById(R.id.account_company_icon);
        mCompanyName = (TextView)view.findViewById(R.id.account_company_name);
        mAccountName = (TextView)view.findViewById(R.id.account_name);
        mAccountCompany = (TextView)view.findViewById(R.id.account_company);
        mAccountCompanyUser = (TextView)view.findViewById(R.id.account_company_user);
        mAccountDateLinked = (TextView)view.findViewById(R.id.account_date_linked);
        mAccountDateLastSignedIn = (TextView)view.findViewById(R.id.account_date_last_signed_in);
        mAccountLastSignedInFrom = (TextView)view.findViewById(R.id.account_last_signed_in_from);
        mListView = (ListView) view.findViewById(R.id.historyListView);
        mSlidingLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
                Log.i(TAG, "onPanelSlide, offset " + slideOffset);
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                Log.i(TAG, "onPanelStateChanged " + newState);
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHistoryCursorAdapter = new HistoryCursorAdapter(
                getSceneController(),
                0
        );
        mListView.setAdapter(mHistoryCursorAdapter);
        applyModel();
    }

    @Override
    public boolean onBackPressed() {
        if(mSlidingLayout != null &&
                (mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED ||
                        mSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
        return false;
    }

    private void applyModel() {
        if (mAccountModel != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
            if(mCompanyIcon != null) {
                final Resources resources = getContext().getResources();
                final Resources.Theme theme = getContext().getTheme();
                Drawable drawable = null;
                switch(mAccountModel.getCompanyLogo().toLowerCase()) {
                    case "webmail.png":
                        drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_webmail, theme);
                        break;
                    case "securedbyvoice.png":
                        drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_securedbyvoice, theme);
                        break;
                    case "v2factor.png":
                        drawable = ResourcesCompat.getDrawable(resources, R.drawable.ic_v2factor, theme);
                        break;
                    case "v2ondemand.png":
                        drawable = ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher, theme);
                        break;
                    default:
                        mCompanyIcon.setImageDrawable(null);
                        break;
                }
                mCompanyIcon.setImageDrawable(drawable);
            }
            if (mCompanyName != null) {
                mCompanyName.setText(mAccountModel.getCompanyName());
                mCompanyName.invalidate();
            }
            if (mAccountName != null) {
                mAccountName.setText(mAccountModel.getAccountUsername());
                mAccountName.invalidate();
            }
            if (mAccountCompany != null) {
                mAccountCompany.setText(mAccountModel.getCompanyName());
                mAccountCompany.invalidate();
            }
            if (mAccountCompanyUser != null) {
                mAccountCompanyUser.setText(mAccountModel.getAccountUsername());
                mAccountCompanyUser.invalidate();
            }
            if (mAccountDateLinked != null) {
                Date date = mAccountModel.getDateLinked().getTime();
                mAccountDateLinked.setText(dateFormat.format(date) + " " + timeFormat.format(date));
                mAccountDateLinked.invalidate();
            }
            if (mAccountDateLastSignedIn != null) {
                Date date = mAccountModel.getDateLinked().getTime();
                mAccountDateLastSignedIn.setText(dateFormat.format(date) + " " + timeFormat.format(date));
                mAccountDateLastSignedIn.invalidate();
            }
            if (mAccountLastSignedInFrom != null) {
                mAccountLastSignedInFrom.setText(mAccountModel.getLastSignedInFrom());
                mAccountLastSignedInFrom.invalidate();
            }
            getLoaderManager().restartLoader(1, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mAccountModel != null) {
            return new CursorLoader(getActivity(),
                    HistoryContract.CONTENT_URI,
                    HistoryModel.HISTORY_PROJECTION,
                    HistoryContract.HISTORY_ACCOUNT_ID + " = ? ",
                    new String[]{ Long.toString(mAccountModel.getId()) },
                    HistoryContract.HISTORY_DATETIME + " DESC"
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mHistoryCursorAdapter.updateCursor(cursor);
        mHistoryCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHistoryCursorAdapter.updateCursor(null);
        mHistoryCursorAdapter.notifyDataSetChanged();
    }

    public void setAccountModel(AccountModel model) {
        mAccountModel = model;
    }
}
