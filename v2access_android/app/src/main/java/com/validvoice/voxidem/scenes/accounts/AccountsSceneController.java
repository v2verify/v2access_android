package com.validvoice.voxidem.scenes.accounts;

import com.validvoice.dynamic.scene.ISceneControllerFactory;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDirector;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.models.AccountModel;

public class AccountsSceneController extends SceneController {

    private AccountsSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout) {
        super(R.id.scene_accounts, 500, sceneDirector, sceneLayout);
        addActor(new AccountsSceneActor());
        addActor(new AccountDetailsSceneActor());
    }

    @Override
    public boolean onBackPressed() {
        if(getActiveActorId() != getHomeActorId()) {
            onCloseScene(true);
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onCloseScene(boolean backPressed) {
        if(getActiveActorId() != getHomeActorId()) {
            dispatchCollapse(getActiveActorId());
        }
        getActivity().invalidateOptionsMenu();
    }

    private class AccountsSceneActor extends SceneLayout.FragmentHomeActor<AccountsSceneController, AccountsFragment> {
        AccountsSceneActor() {
            super(AccountsSceneController.this, R.id.actor_home, AccountsFragment.class);
        }
    }

    private class AccountDetailsSceneActor extends SceneLayout.FragmentSceneActor<AccountsSceneController, AccountDetailsFragment> {

        private AccountModel mAccountModel;

        AccountDetailsSceneActor() {
            super(AccountsSceneController.this, R.id.actor_details, AccountDetailsFragment.class, TRANSITION_RIGHT_TO_LEFT);
            setBlendedToolbarId(R.id.toolbar_overlay);
        }

        @Override
        public void onAttachingFragment(AccountDetailsFragment fragment) {
            super.onAttachingFragment(fragment);
            fragment.setAccountModel(mAccountModel);
        }

        @Override
        public boolean onActorAction(int action_id, Object data) {
            if(action_id == R.id.action_item && data instanceof AccountModel) {
                mAccountModel = (AccountModel)data;
                return true;
            }
            return super.onActorAction(action_id, data);
        }
    }

    public static class Factory implements ISceneControllerFactory {

        @Override
        public SceneController CreateSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout) {
            return new AccountsSceneController(sceneDirector, sceneLayout);
        }

    }

}
