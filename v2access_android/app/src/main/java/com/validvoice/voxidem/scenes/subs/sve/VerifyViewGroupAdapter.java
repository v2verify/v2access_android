package com.validvoice.voxidem.scenes.subs.sve;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.TSceneViewGroupAdapter;
import com.validvoice.voxidem.R;

class VerifyViewGroupAdapter extends TSceneViewGroupAdapter<VerifyData> {

    private static final String TAG = "VerifyViewGroupAdapter";

    VerifyViewGroupAdapter(@NonNull SceneController sceneController, @NonNull ViewGroup viewGroup) {
        super(sceneController, R.layout.scene_sve_list_row_item, viewGroup);
    }

    private class ViewHolder {
        private TextView scene_sve_text;
    }

    @Override
    public Object newTag(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.scene_sve_text =  view.findViewById(R.id.scene_sve_text);
        return viewHolder;
    }

    @Override
    public void adjustTag(Object tag, Context context, VerifyData item) {
        ViewHolder viewHolder = (ViewHolder)tag;
        String question;
        if (item.getSay().contains("from")){
            question = "Count "+item.getSay();
        }else{
            question = "Say "+item.getSay();
        }
        viewHolder.scene_sve_text.setText(question);
    }

}
