package com.validvoice.voxidem.scenes.devices;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.models.DeviceModel;

public class DevicesFragment extends SceneFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_INFO = "device_info";
    public static final String DEVICE_IP = "device_ip";

    private ListView mListView;
    private DevicesCursorAdapter mDevicesCursorAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        mListView = view.findViewById(R.id.devicesListView);

        ((MainActivity) getActivity()).spanTitleBar("Prior Paired Devices");



        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor)mDevicesCursorAdapter.getItem(position);
                dispatchExpand(R.id.actor_details, R.id.action_item, DeviceModel.createRecordFromCursor(c));
            }
        });

        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Cursor c = (Cursor)mDevicesCursorAdapter.getItem(position);
                dispatchExpand(R.id.actor_options, R.id.action_item, DeviceModel.createRecordFromCursor(c));
                mDevicesCursorAdapter.setSelectedPosition(position);
                return true;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mDevicesCursorAdapter = new DevicesCursorAdapter(
                getSceneController(),
                0
        );
        mListView.setAdapter(mDevicesCursorAdapter);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                DevicesContract.CONTENT_URI,
                DeviceModel.DEVICE_PROJECTION,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mDevicesCursorAdapter.updateCursor(cursor);
        mDevicesCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDevicesCursorAdapter.updateCursor(null);
        mDevicesCursorAdapter.notifyDataSetChanged();
    }
}
