package com.validvoice.voxidem.scenes.faq;
import com.validvoice.dynamic.scene.ISceneControllerFactory;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDirector;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.voxidem.R;

public class FaqSceneController extends SceneController {

    private FaqSceneController(SceneDirector sceneManager, SceneLayout sceneLayout) {
        super(R.id.scene_faq, 1100, sceneManager, sceneLayout);
        addActor(new FaqSceneActor());
    }

    private class FaqSceneActor extends SceneLayout.FragmentHomeActor<FaqSceneController, FaqFragment> {
        FaqSceneActor() {
            super(FaqSceneController.this, R.id.actor_home, FaqFragment.class);
        }
    }

    public static class Factory implements ISceneControllerFactory {

        @Override
        public SceneController CreateSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout) {
            return new FaqSceneController(sceneDirector, sceneLayout);
        }

    }

}