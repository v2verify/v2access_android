package com.validvoice.voxidem.scenes.history;

import com.validvoice.dynamic.scene.ISceneControllerFactory;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDirector;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.voxidem.R;

public class HistorySceneController extends SceneController {

    private HistorySceneController(SceneDirector sceneManager, SceneLayout sceneLayout) {
        super(R.id.scene_history, 300, sceneManager, sceneLayout);
        addActor(new HistorySceneActor());
    }

    private class HistorySceneActor extends SceneLayout.FragmentHomeActor<HistorySceneController, HistoryFragment> {
        HistorySceneActor() {
            super(HistorySceneController.this, R.id.actor_home, HistoryFragment.class);
        }
    }

    public static class Factory implements ISceneControllerFactory {

        @Override
        public SceneController CreateSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout) {
            return new HistorySceneController(sceneDirector, sceneLayout);
        }

    }

}
