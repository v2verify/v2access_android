package com.validvoice.voxidem.scenes.faq;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;

public class FaqFragment extends SceneFragment {

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_faq, container, false);

        ((MainActivity) getActivity()).spanTitleBar("Best Practices");

//        TextView best_practices = view.findViewById(R.id.best_practices);
//        best_practices.setText(Html.fromHtml(getString(R.string.best_practices)));

        return  view;
    }
}
