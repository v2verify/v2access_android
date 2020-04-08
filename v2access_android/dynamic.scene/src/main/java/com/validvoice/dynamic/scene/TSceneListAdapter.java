package com.validvoice.dynamic.scene;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class TSceneListAdapter<T> extends BaseAdapter {

    private SceneController mSceneController;
    private List<T> mObjects;
    private int mSelectedIndex = -1;
    private int mLayoutResourceId;
    private ViewGroup mViewGroup;
    private boolean mNotifyOnChange = true;
    private final Object mLock = new Object();
    private LayoutInflater mLayoutInflater;

    public TSceneListAdapter(SceneController sceneController, int layoutId) {
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        if( layoutId != -1 ) {
            mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        mObjects = new ArrayList<>();
    }

    public TSceneListAdapter(SceneController sceneController, int layoutId, List<T> items) {
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        if( layoutId != -1 ) {
            mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        mObjects = items;
    }

    public TSceneListAdapter(SceneController sceneController, int layoutId, ViewGroup viewGroup) {
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        mViewGroup = viewGroup;
        if( mViewGroup != null ) {
            mViewGroup.removeAllViews();
        }
        if( layoutId != -1 ) {
            mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        mObjects = new ArrayList<>();
    }

    public TSceneListAdapter(SceneController sceneController, int layoutId, ViewGroup viewGroup, List<T> items) {
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        mViewGroup = viewGroup;
        if( mViewGroup != null ) {
            mViewGroup.removeAllViews();
        }
        if( layoutId != -1 ) {
            mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        mObjects = items;
    }

    final public SceneController getSceneController() {
        return mSceneController;
    }

    final public Context getContext() {
        return mSceneController.getContext();
    }

    final public LayoutInflater getLayoutInflater() {
        return mLayoutInflater;
    }

    final protected ViewGroup getViewGroup() {
        return mViewGroup;
    }

    final public int getSelectedIndex() {
        return mSelectedIndex;
    }

    final public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= mObjects.size()) {
            throw new IllegalStateException("couldn't get item at position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(mSceneController.getContext(), parent);
        } else {
            v = convertView;
        }
        bindView(position, v, mSceneController.getContext(), mObjects.get(position));
        return v;
    }

    private View newView(Context context, ViewGroup parent) {
        View view = null;
        if( mLayoutResourceId != -1 ) {
            view = getLayoutInflater().inflate(mLayoutResourceId, parent, false);
            view.setTag(newTag(view));
        }
        return view;
    }

    private void bindView(int position, View view, Context context, T item) {
        if( mLayoutResourceId != -1 ) {
            adjustTag(view.getTag(), context, item);
            if(position == getSelectedIndex()) {
                view.setSelected(true);
            } else {
                view.setSelected(false);
            }
        }
    }

    public abstract Object newTag(View view);

    public abstract void adjustTag(Object tag, Context context, T item);

    final public void add(T item) {
        synchronized (mLock) {
            mObjects.add(item);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public final void addAll(Collection<? extends T> items) {
        synchronized (mLock) {
            mObjects.addAll(items);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void clear() {
        synchronized (mLock) {
            if( mObjects != null ) {
                mObjects.clear();
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            if( mObjects != null ) {
                Collections.sort(mObjects, comparator);
            }
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void setAll(Collection<? extends T> items) {
        synchronized (mLock) {
            mObjects.clear();
            mObjects.addAll(items);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void remove(T item) {
        synchronized (mLock) {
            mObjects.remove(item);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void removeAll(Collection<? extends T> items) {
        synchronized (mLock) {
            mObjects.removeAll(items);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    @Override
    final public int getCount() {
        return mObjects.size();
    }

    @Override
    final public Object getItem(int position) {
        return mObjects.get(position);
    }

    final public T getItemType(int position) {
        return mObjects.get(position);
    }

    @Override
    final public long getItemId(int position) {
        return position;
    }

    final public void setNotifyOnChange(boolean notifyOnChange) {
        mNotifyOnChange = notifyOnChange;
    }

    final public boolean getNotifyOnChange() {
        return mNotifyOnChange;
    }

}
