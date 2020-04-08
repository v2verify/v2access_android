package com.validvoice.voxidem.scenes.devices;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneCursorAdapter;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.models.DeviceModel;

class DevicesCursorAdapter extends SceneCursorAdapter {

    DevicesCursorAdapter(SceneController sceneController, int flags) {
        super(sceneController, R.layout.scene_devices_list_row_item, flags);
    }

    private class ViewHolder {
        TextView device_name;
        TextView device_type;
    }

    @Override
    public Object newTag(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.device_name = view.findViewById(R.id.device_name);
        viewHolder.device_type = view.findViewById(R.id.device_type);
        return viewHolder;
    }

    @Override
    public void adjustTag(Object tag, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)tag;
        DeviceModel model = DeviceModel.createRecordFromCursor(cursor);
        viewHolder.device_name.setText(model.getDeviceName());
        viewHolder.device_type.setText(model.getDeviceType());
    }

}
