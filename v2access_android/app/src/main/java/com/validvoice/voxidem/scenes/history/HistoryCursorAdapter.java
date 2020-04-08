package com.validvoice.voxidem.scenes.history;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.TextView;

import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneCursorAdapter;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.models.HistoryModel;

public class HistoryCursorAdapter extends SceneCursorAdapter {

    public HistoryCursorAdapter(SceneController sceneController, int flags) {
        super(sceneController, R.layout.scene_history_list_row_item, flags);
    }

    private class ViewHolder {
        TextView history_datetime_text;
        TextView history_type;
        TextView history_device;
        TextView history_user;
        TextView history_site;
        TextView history_result;
    }

    @Override
    public Object newTag(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.history_datetime_text = view.findViewById(R.id.history_datetime_text);
       viewHolder.history_type = view.findViewById(R.id.history_type);
        viewHolder.history_device = view.findViewById(R.id.history_device);
        viewHolder.history_site = view.findViewById(R.id.history_site);
        viewHolder.history_user = view.findViewById(R.id.history_user);
        viewHolder.history_result = view.findViewById(R.id.history_result);
        return viewHolder;
    }

    @Override
    public void adjustTag(Object tag, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder)tag;
        HistoryModel model = HistoryModel.createRecordFromCursor(cursor);
        viewHolder.history_datetime_text.setText(model.getDateTimeAsString());
      //  viewHolder.history_data.setText(model.getDataAsString(context));
      //  viewHolder.history_site.setText(model.getHistoryType());
        viewHolder.history_user.setText(model.getUser());
        viewHolder.history_result.setText(model.getResult());
        viewHolder.history_site.setText(model.getmCompanyName());
        viewHolder.history_device.setText(model.getmDeviceName());
        viewHolder.history_type.setText(model.getmType().toString());
    }

}
