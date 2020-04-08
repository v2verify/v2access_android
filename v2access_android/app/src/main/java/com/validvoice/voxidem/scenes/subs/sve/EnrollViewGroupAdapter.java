package com.validvoice.voxidem.scenes.subs.sve;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.TSceneViewGroupAdapter;
import com.validvoice.voxidem.R;

class EnrollViewGroupAdapter extends TSceneViewGroupAdapter<EnrollData> {

//    private Drawable mSpeechGood;
//    private Drawable mSpeechBad;

    EnrollViewGroupAdapter(@NonNull SceneController sceneController, @NonNull ViewGroup viewGroup) {
        super(sceneController, R.layout.scene_sve_list_row_item, viewGroup);
//        final Resources resources = sceneController.getContext().getResources();
//        final Resources.Theme theme = sceneController.getContext().getTheme();
//        mSpeechGood = ResourcesCompat.getDrawable(resources, R.drawable.ic_speech_good, theme);
//        mSpeechBad = ResourcesCompat.getDrawable(resources, R.drawable.ic_speech_bad, theme);
    }

    private class ViewHolder {
        //        private ProgressBar scene_sve_progress;
//        private ImageView scene_sve_result;
        private TextView scene_sve_text;
    }

    @Override
    public Object newTag(View view) {
        ViewHolder viewHolder = new ViewHolder();
//        viewHolder.scene_sve_progress =  view.findViewById(R.id.scene_sve_progress);
//        viewHolder.scene_sve_result =  view.findViewById(R.id.scene_sve_result);
        viewHolder.scene_sve_text =  view.findViewById(R.id.scene_sve_text);
        return viewHolder;
    }

    @Override
    public void adjustTag(Object tag, Context context, EnrollData item) {
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
