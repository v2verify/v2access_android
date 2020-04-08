package com.validvoice.dynamic.scene;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.validvoice.dynamic.db.ContractController;

import java.util.HashMap;
import java.util.Stack;

public class SceneController {

    private int mId;
    private int mIdOffset;
    private SceneDirector mSceneDirector;
    private SceneLayout mSceneLayout;
    private Context mContext;
    private Stack<ISceneFragment> mSceneFragmentStack;
    private HashMap<String, Object> mSceneData;
    private SharedPreferences mSharedPreferences;
    private boolean mIsPortraitMode;
    private boolean mIsControllerClosed = true;
    private int mHomeActorId = -1;
    private int mActiveActorId = 1;

    public SceneController(int sceneControllerId, int sceneIdOffset, SceneDirector sceneDirector, SceneLayout sceneLayout) {
        this.mId = sceneControllerId;
        this.mIdOffset = sceneIdOffset;
        this.mSceneDirector = sceneDirector;
        this.mSceneLayout = sceneLayout;
        this.mSceneLayout.setSceneController(this);
        this.mContext = sceneLayout.getContext();
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        this.mIsPortraitMode = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        this.mSceneFragmentStack = new Stack<>();
        this.mSceneData = new HashMap<>();
    }

    public final Context getContext() {
        return mContext;
    }

    public final SceneDirector getSceneDirector() {
        return mSceneDirector;
    }

    public final ContractController getContractController() {
        return mSceneDirector.getContractController();
    }

    public final MenuInflater getMenuInflater() {
        return this.getActivity().getMenuInflater();
    }

    public final Resources getResources() {
        return  this.mContext.getResources();
    }

    public final SceneLayout getSceneLayout() {
        return mSceneLayout;
    }

    public final SharedPreferences getSharedPreferences() {
        return this.mSharedPreferences;
    }

    public final int getId() {
        return mId;
    }

    private int getIdOffset() {
        return mIdOffset;
    }

    public final AppCompatActivity getActivity() {
        return (AppCompatActivity)this.mContext;
    }

    public final SceneActivity getSceneActivity() {
        return (SceneActivity)this.mContext;
    }

    private FragmentManager getFragmentManager() {
        return this.getActivity().getSupportFragmentManager();
    }

    public final boolean getIsPortraitMode() {
        return mIsPortraitMode;
    }

    protected final void addActor(SceneLayout.SceneActor sceneActor) {
        if(sceneActor.type == SceneLayout.SceneActor.FRAGMENT_ACTOR) {
            SceneLayout.FragmentActor fa = (SceneLayout.FragmentActor) sceneActor;
            if(fa.isHomeFragment()) {
                if(mHomeActorId == -1) {
                    mHomeActorId = resolveInId(fa.id);
                } else {
                    throw new IllegalStateException("Can not have more than one home fragment per scene controller.");
                }
            }
        }
        sceneActor.id = resolveInId(sceneActor.id);
        mSceneLayout.addActor(sceneActor);
    }

    public final boolean hasActor(int actorId) {
        return mSceneLayout.hasActor(resolveInId(actorId));
    }

    public final void removeActor(int actorId) {
        mSceneLayout.removeActor(resolveInId(actorId));
    }

    public final void removeAllActors() {
        mSceneLayout.removeAllActors();
    }

    public void setData(String name, Object data) {
        mSceneData.put(name, data);
    }

    public Object getData(String name) {
        if(!mSceneData.containsKey(name)) {
            return getSceneDirector().getData(name);
        }
        return mSceneData.get(name);
    }

    public boolean hasData(String name) {
        return mSceneData.containsKey(name) || getSceneDirector().hasData(name);
    }

    public void removeData(String name) {
        if(!mSceneData.containsKey(name)) {
            getSceneDirector().removeData(name);
        }
        mSceneData.remove(name);
    }

    public final void open() {
        if(mIsControllerClosed) {
            mIsControllerClosed = false;
            if(mHomeActorId != -1) {
                dispatchExpand(getHomeActorId());
            }
            if(requireSceneReset()) {
                mSceneLayout.resetScene();
            }
            onOpen();
        }
    }

    public final void close() {
        if( !mIsControllerClosed ) {
            onClose();
            mSceneLayout.removeAllActors(this);
            mIsControllerClosed = true;
            this.getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
    }

    public final void refresh() {
        if(!dispatchAction(R.id.action_refresh, null)) {
            onRefresh();
        }
    }

    public void onOpen() {
    }

    public void onClose() {
    }

    public void onCloseScene(boolean isBackPressed) {
    }

    public boolean requireSceneReset() {
        return false;
    }

    public final boolean isClosed() {
        return mIsControllerClosed;
    }

    public boolean onBackPressed() {
        if(mSceneFragmentStack.empty() || !mSceneFragmentStack.peek().onBackPressed()) {
            FragmentManager fm = getFragmentManager();
            if( fm.getBackStackEntryCount() > 0 ) {
                mSceneFragmentStack.pop();
                return fm.popBackStackImmediate();
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private int resolveInId(int id) {
        return id + getIdOffset();
    }

    int resolveOutId(int id) {
        return id - getIdOffset();
    }

    private void setActiveActorId(int actorId) {
        this.mActiveActorId = resolveInId(actorId);
    }

    public int getActiveActorId() {
        return resolveOutId(this.mActiveActorId);
    }

    public int getHomeActorId() {
        return resolveOutId(this.mHomeActorId);
    }

    public SceneLayout.SceneActor findActorById(int actorId) {
        return mSceneLayout.findActorById(resolveInId(actorId));
    }

    public boolean dispatchAction(int actionId, Object data) {
        return mSceneLayout.performAction(this.mActiveActorId, actionId, data);
    }

    public boolean dispatchAction(int actorId, int actionId, Object data) {
        return mSceneLayout.performAction(resolveInId(actorId), actionId, data);
    }

    public boolean dispatchBypassAction(int actorId, int actionId, Object data) {
        return mSceneLayout.performAction(actorId, actionId, data);
    }

    public void dispatchExpand(int actorId) {
        dispatchExpand(actorId, actorId);
    }

    public void dispatchExpand(int expandingActorId, int becomingActorId) {
        setActiveActorId(becomingActorId);
        mSceneLayout.performExpand(resolveInId(expandingActorId));
    }

    public void dispatchExpand(int actorId, int actionId, Object data) {
        dispatchAction(actorId, actionId, data);
        dispatchExpand(actorId);
    }

    public void dispatchCollapse(int actorId) {
        dispatchCollapse(actorId, getHomeActorId());
    }

    public void dispatchCollapse(int collapsingActorId, int becomingActorId) {
        mSceneLayout.performCollapse(resolveInId(collapsingActorId));
        setActiveActorId(becomingActorId);
    }

    public void dispatchCollapse(int actorId, int actionId, Object data) {
        dispatchAction(actorId, actionId, data);
        dispatchCollapse(actorId);
    }

    public void dispatchCancel(int actorId) {
        mSceneLayout.performCancel(resolveInId(actorId));
    }

    public void dispatchSwap(int fromActorId, int toActorId) {
        mSceneLayout.performSwap(resolveInId(fromActorId), resolveInId(toActorId));
        setActiveActorId(toActorId);
    }

    public void dispatchSwap(int fromActorId, int toActorId, int actorId) {
        mSceneLayout.performSwap(resolveInId(fromActorId), resolveInId(toActorId));
        setActiveActorId(actorId);
    }

    public void dispatchSwap(int fromActorId, int toActorId, int toActionId, Object data) {
        mSceneLayout.performSwap(resolveInId(fromActorId), resolveInId(toActorId));
        dispatchAction(toActorId, toActionId, data);
    }

    public void dispatchHideHeader() {
        mSceneLayout.performHeaderHide();
    }

    public void dispatchShowHeader() {
        mSceneLayout.performHeaderShow();
    }

    public void dispatchHideFooter() {
        mSceneLayout.performFooterHide();
    }

    public void dispatchShowFooter() {
        mSceneLayout.performFooterShow();
    }

    private FragmentTransaction createFragmentTransaction(FragmentManager manager, int transition) {
        FragmentTransaction ft = manager.beginTransaction();
        switch(transition) {
            case SceneLayout.FragmentActor.TRANSITION_LEFT_TO_RIGHT:
                ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left);
                break;
            case SceneLayout.FragmentActor.TRANSITION_RIGHT_TO_LEFT:
                ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case SceneLayout.FragmentActor.TRANSITION_TOP_DOWN:
                ft.setCustomAnimations(R.anim.slide_in_top, R.anim.slide_out_bottom, R.anim.slide_in_bottom, R.anim.slide_out_top);
                break;
            case SceneLayout.FragmentActor.TRANSITION_BOTTOM_UP:
                ft.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom);
                break;
        }
        return ft;
    }

    void setHomeFragment(ISceneFragment fragment, int transition) {
        FragmentManager fm = this.getFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        mSceneFragmentStack.clear();
        createFragmentTransaction(fm, transition)
                .replace(this.mSceneLayout.getContentViewId(), fragment.getFragment())
                .commit();
        mSceneFragmentStack.push(fragment);
    }

    void replaceFragment(ISceneFragment fragment, String tag, int transition) {
        createFragmentTransaction(this.getFragmentManager(), transition)
                .replace(this.mSceneLayout.getContentViewId(),fragment.getFragment())
                .addToBackStack(tag)
                .commit();
        mSceneFragmentStack.push(fragment);
    }

    boolean replaceFragment(ISceneFragment fromFragment, String fromTag, ISceneFragment toFragment, String toTag, int transition) {
        if (mSceneFragmentStack.peek() == fromFragment) {
            mSceneFragmentStack.pop();
            FragmentManager fm = getFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStackImmediate(fromTag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            createFragmentTransaction(this.getFragmentManager(), transition)
                    .replace(this.mSceneLayout.getContentViewId(), toFragment.getFragment())
                    .addToBackStack(toTag)
                    .commit();
            mSceneFragmentStack.push(toFragment);
            return true;
        }
        return false;
    }

    public void showDialogFragment(DialogFragment dialogFragment, String tag) {
        this.getFragmentManager().beginTransaction()
                .add(dialogFragment, tag)
                .commit();
    }

    public void addBackgroundActor(IRetainedSceneFragment fragment, String tag) {
        this.getFragmentManager().beginTransaction()
                .add(fragment.getFragment(), tag)
                .commit();
    }

    public boolean popFragment(ISceneFragment fragment) {
        if (mSceneFragmentStack.peek() == fragment) {
            mSceneFragmentStack.pop();
            FragmentManager fm = getFragmentManager();
            if (fm.getBackStackEntryCount() > 0) {
                return fm.popBackStackImmediate();
            }
        }
        return false;
    }

    public void hideSoftKeyboardFromWindow() {
        final Activity activity = getActivity();
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager != null) {
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            onCloseScene(false);
            return false;
        }
        return dispatchAction(getActiveActorId(), item.getItemId(), item);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    class PermissionAction {

        final int RequestCode;
        final String[] Permissions;
        final int[] GrantResults;

        PermissionAction(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            RequestCode = requestCode;
            Permissions = permissions;
            GrantResults = grantResults;
        }
    }

    public void onRequestPermissionsResult(int requestCode,
                                    @NonNull String[] permissions,
                                    @NonNull int[] grantResults) {
        dispatchAction(getActiveActorId(), R.id.action_permission,
                new PermissionAction(requestCode, permissions, grantResults));
    }

    public boolean onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        return false;
    }

    public void onResume() { }

    public void onPause() { }

    @CallSuper
    public void onRefresh() {
        if(mSceneFragmentStack.isEmpty()) return;
        ISceneFragment fragment = mSceneFragmentStack.peek();
        if(fragment.getSceneId() == getActiveActorId()) {
            fragment.onRefresh();
        }
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) { }

    public void onSaveInstanceState(Bundle outState) { }

    public class SimpleDetailActor <T extends SceneController, U extends SceneFragment> extends SceneLayout.FragmentSceneActor<T, U> {
        public SimpleDetailActor(T sceneController, Class<U> clazz, int transition) {
            super(sceneController, R.id.actor_details, clazz, transition);
        }
    }

    public interface SimpleOptionsItemListener {
        boolean onItem(Object item);
    }

    public class SimpleOptionsSceneActorHeader extends SceneLayout.FreeScrimSceneActorHeader {

        private boolean mSwap = false;
        private boolean mOverlayView = false;
        private SimpleOptionsItemListener mSimpleOptionsItemListener;

        public SimpleOptionsSceneActorHeader(SceneController sceneController) {
            super(sceneController, R.id.actor_options);
            mSwap = false;
        }

        public SimpleOptionsSceneActorHeader(SceneController sceneController, SimpleOptionsItemListener listener) {
            super(sceneController, R.id.actor_options);
            mSimpleOptionsItemListener = listener;
            mSwap = false;
        }

        public SimpleOptionsSceneActorHeader(SceneController sceneController, boolean swap) {
            super(sceneController, R.id.actor_options);
            mSimpleOptionsItemListener = null;
            mSwap = swap;
        }

        public SimpleOptionsSceneActorHeader(SceneController sceneController, SimpleOptionsItemListener listener, boolean swap) {
            super(sceneController, R.id.actor_options);
            mSimpleOptionsItemListener = listener;
            mSwap = swap;
        }

        public SimpleOptionsSceneActorHeader(SceneController sceneController, boolean swap, boolean overlayView) {
            super(sceneController, R.id.actor_options);
            mSimpleOptionsItemListener = null;
            mSwap = swap;
            mOverlayView = overlayView;
        }

        public SimpleOptionsSceneActorHeader(SceneController sceneController, SimpleOptionsItemListener listener, boolean swap, boolean overlayView) {
            super(sceneController, R.id.actor_options);
            mSimpleOptionsItemListener = listener;
            mSwap = swap;
            mOverlayView = overlayView;
        }

        @Override
        public boolean allowOverlay(int id) {
            return id == R.id.actor_edit || id == R.id.actor_delete;
        }

        @Override
        public int overlayType() {
            return mOverlayView ? OVERLAY_VIEW : OVERLAY_NONE;
        }

        @Override
        public void onActorOpening() {
            super.onActorOpening();
            getActivity().invalidateOptionsMenu();
        }

        @Override
        public boolean onActorAction(int id, Object data) {
            if(id == R.id.action_edit) {
                if(mSwap) {
                    dispatchSwap(getActiveActorId(), R.id.actor_edit);
                } else {
                    dispatchExpand(R.id.actor_edit);
                }
                getActivity().invalidateOptionsMenu();
                return true;
            } else if(id == R.id.action_delete) {
                if(mSwap) {
                    dispatchSwap(getActiveActorId(), R.id.actor_delete);
                } else {
                    dispatchExpand(R.id.actor_delete);
                }
                getActivity().invalidateOptionsMenu();
                return true;
            } else if(id == R.id.action_item && mSimpleOptionsItemListener != null) {
                return mSimpleOptionsItemListener.onItem(data);
            }
            return false;
        }
    }

}
