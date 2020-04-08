package com.validvoice.voxidem.scenes.devices;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.contracts.HistoryContract;
import com.validvoice.voxidem.db.models.DeviceModel;
import com.validvoice.voxidem.db.models.HistoryModel;
import com.validvoice.voxidem.scenes.history.HistoryCursorAdapter;

import java.text.DateFormat;
import java.util.Date;

public class DeviceDetailsFragment extends SceneFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = DeviceDetailsFragment.class.getSimpleName();

    private SlidingUpPanelLayout mSlidingLayout;
    private TextView mDeviceName;
    private TextView mDateAdded;
    private TextView mDateLastUsed;
    private TextView mDeviceIpAddress;
    private TextView mDeviceType;
    private TextView mDeviceId;
    private ListView mListView;
    private HistoryCursorAdapter mHistoryCursorAdapter;

    private DeviceModel mDeviceModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device_details, container, false);
        mSlidingLayout =  view.findViewById(R.id.sliding_layout);
        mDeviceName = view.findViewById(R.id.device_name);
        mDateAdded = view.findViewById(R.id.device_date_added);
        mDateLastUsed = view.findViewById(R.id.device_date_last_used);
        mDeviceIpAddress = view.findViewById(R.id.device_ip_address);
        mDeviceType = view.findViewById(R.id.device_type);
        mDeviceId = view.findViewById(R.id.device_id);
        mListView =  view.findViewById(R.id.historyListView);
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
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //dispatchHideFooter();
        mHistoryCursorAdapter = new HistoryCursorAdapter(
                getSceneController(),
                0
        );
        mListView.setAdapter(mHistoryCursorAdapter);
        applyModel();
    }

    @Override
    public void onDetach() {
        //dispatchShowFooter();
        super.onDetach();
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
        if (mDeviceModel != null) {
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(getActivity());
            if (mDeviceName != null) {
                mDeviceName.setText(mDeviceModel.getDeviceName());
                mDeviceName.invalidate();
            }
            if (mDateAdded != null) {
                Date date = mDeviceModel.getDateAdded().getTime();
                mDateAdded.setText(dateFormat.format(date) + " " + timeFormat.format(date));
                mDateAdded.invalidate();
            }
            if (mDateLastUsed != null) {
                Date date = mDeviceModel.getDateLastUsed().getTime();
                mDateLastUsed.setText(dateFormat.format(date) + " " + timeFormat.format(date));
            }
            if (mDeviceType != null) {
                mDeviceType.setText(mDeviceModel.getDeviceType());
            }
            if (mDeviceId != null) {
                mDeviceId.setText(mDeviceModel.getDeviceId());
            }
            if (mDeviceIpAddress != null) {
                mDeviceIpAddress.setText(mDeviceModel.getLastKnownIPAddress());
            }
            getLoaderManager().restartLoader(1, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Activity activity = getActivity();
        if(activity != null && mDeviceModel != null) {
            return new CursorLoader(activity,
                    HistoryContract.CONTENT_URI,
                    HistoryModel.HISTORY_PROJECTION,
                    HistoryContract.HISTORY_DEVICE_ID + " = ? ",
                    new String[]{ Long.toString(mDeviceModel.getId()) },
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

    public void setDeviceModel(DeviceModel model) {
        mDeviceModel = model;
    }

}
