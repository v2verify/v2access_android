package com.validvoice.dynamic.scene;

import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class SceneCursorAdapter extends CursorAdapter {

    private SceneController mSceneController;
    private LayoutInflater mLayoutInflater;
    private int mSelectedPosition = -1;
    private boolean mNotifyOnChange = true;
    private int mLayoutResourceId;
    private ViewGroup mViewGroup;

    public SceneCursorAdapter(SceneController sceneController, int layoutId, int flags) {
        super(sceneController.getContext(), null, flags);
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        if( layoutId != -1 ) {
            mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    public SceneCursorAdapter(SceneController sceneController, Cursor cursor, int layoutId, int flags) {
        super(sceneController.getContext(), cursor, flags);
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        if( layoutId != -1 ) {
            mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    public SceneCursorAdapter(SceneController sceneController, int layoutId, ViewGroup viewGroup, int flags) {
        super(sceneController.getContext(), null, flags);
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        mViewGroup = viewGroup;
        if( mViewGroup != null ) {
            mViewGroup.removeAllViews();
        }
        if( layoutId != -1 ) {
            mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    public SceneCursorAdapter(SceneController sceneController, Cursor cursor, int layoutId, ViewGroup viewGroup, int flags) {
        super(sceneController.getContext(), cursor, flags);
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        mViewGroup = viewGroup;
        if( mViewGroup != null ) {
            mViewGroup.removeAllViews();
        }
        if( layoutId != -1 ) {
            mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
    }

    final protected Context getContext() {
        return mSceneController.getContext();
    }

    final protected LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    final protected SceneController getSceneController() {
        return mSceneController;
    }

    final protected LoaderManager getLoaderManager() {
        return mSceneController.getActivity().getLoaderManager();
    }

    final protected ViewGroup getViewGroup() {
        return mViewGroup;
    }

    final public boolean dispatchSceneAction(int sceneId, int actionId, Object data) {
        return mSceneController.dispatchAction(sceneId, actionId, data);
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setSelectedPosition(int selectedIndex) {
        mSelectedPosition = selectedIndex;
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public int getPositionById(long id) {
        Cursor cursor = getCursor();
        if(cursor != null && cursor.moveToFirst()) {
            do {
                if(cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)) == id) {
                    return cursor.getPosition();
                }
            } while(cursor.moveToNext());
        }
        return -1;
    }

    public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    public boolean getNotifyOnChange() {
        return mNotifyOnChange;
    }

    public Object newTag(View view) {
        return null;
    }

    public void adjustTag(Object tag, Context context, Cursor cursor) {

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = null;
        if( mLayoutResourceId != -1 ) {
            view = getLayoutInflater().inflate(mLayoutResourceId, parent, false);
            view.setTag(newTag(view));
        }
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if( mLayoutResourceId != -1 ) {
            adjustTag(view.getTag(), context, cursor);
            if(cursor.getPosition() == getSelectedPosition()) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
        }
    }

    public void updateCursor(Cursor cursor) {
        if( mViewGroup != null ) {
            mViewGroup.removeAllViews();
            changeCursor(cursor);
            final int afterCount = getCount();
            for (int i = 0; i < afterCount; ++i) {
                View view = getView(i, null, mViewGroup);
                mViewGroup.addView(view, view.getLayoutParams());
            }
        } else {
            changeCursor(cursor);
        }
    }
}
