package com.validvoice.dynamic.scene;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class TSceneViewGroupAdapter<T> extends BaseAdapter {

    private SceneController mSceneController;
    private List<T> mObjects;
    private int mSelectedIndex = -1;
    private int mLayoutResourceId;
    private ViewGroup mViewGroup;
    private boolean mNotifyOnChange = true;
    private final Object mLock = new Object();
    private LayoutInflater mLayoutInflater;
    private int mMaxStore = -1;
    private int mItemHeight = -1;
    private DataSetObserver mObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            synchronized (mLock) {
                final int afterCount = getCount();
                for (int i = 0; i < afterCount; ++i) {
                    getView(i, mViewGroup.getChildAt(i), mViewGroup);
                }
            }
        }

        @Override
        public void onInvalidated() {
            synchronized (mLock) {
                mViewGroup.removeAllViews();
                final int afterCount = getCount();
                for (int i = 0; i < afterCount; ++i) {
                    View view = getView(i, null, mViewGroup);
                    mViewGroup.addView(view, view.getLayoutParams());
                }
            }
        }
    };

    public TSceneViewGroupAdapter(@NonNull SceneController sceneController, @LayoutRes int layoutId, @NonNull ViewGroup viewGroup) {
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        mViewGroup = viewGroup;
        mViewGroup.removeAllViews();
        mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = new ArrayList<>();
        registerDataSetObserver(mObserver);
        mViewGroup.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(mMaxStore != -1) {
                    int ph = v.getMeasuredHeight() - (v.getPaddingTop() + v.getPaddingBottom());
                    if (ph > 0 && mItemHeight > 0) {
                        mMaxStore = ph / mItemHeight;
                    }
                    if(enforceLimitation() && mNotifyOnChange) {
                        notifyDataSetChanged();
                    }
                }
            }
        });
    }

    public TSceneViewGroupAdapter(@NonNull SceneController sceneController, @LayoutRes int layoutId, @NonNull ViewGroup viewGroup, @NonNull List<T> items) {
        mSceneController = sceneController;
        mLayoutResourceId = layoutId;
        mViewGroup = viewGroup;
        mViewGroup.removeAllViews();
        mLayoutInflater = (LayoutInflater) sceneController.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mObjects = new ArrayList<>();
        addAll(items);
        registerDataSetObserver(mObserver);
        mViewGroup.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(mMaxStore != -1) {
                    int ph = v.getMeasuredHeight() - (v.getPaddingTop() + v.getPaddingBottom());
                    if (ph > 0 && mItemHeight > 0) {
                        mMaxStore = ph / mItemHeight;
                    }
                    if(enforceLimitation() && mNotifyOnChange) {
                        notifyDataSetChanged();
                    }
                }
            }
        });
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

    final public void setLimitations(boolean enable) {
        mMaxStore = enable ? 0 : -1;
    }

    final public boolean hasLimitations() {
        return mMaxStore != -1;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        mSelectedIndex = selectedIndex;
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (position >= mObjects.size()) {
            throw new IllegalStateException("couldn't get item at position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(getContext(), parent);
        } else {
            v = convertView;
        }
        bindView(position, v, getContext(), mObjects.get(position));
        if(mMaxStore != -1) {
            int ph = parent.getMeasuredHeight() - (parent.getPaddingTop() + parent.getPaddingBottom());
            mItemHeight = v.getMeasuredHeight();
            if (ph > 0 && mItemHeight > 0) {
                mMaxStore = ph / mItemHeight;
            }
        }
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

    public void add(T object) {
        synchronized (mLock) {
            mObjects.add(object);
            View view = getView(mObjects.size() - 1, null, mViewGroup);
            mViewGroup.addView(view, view.getLayoutParams());
            enforceLimitation();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void addAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            for(T object : collection) {
                mObjects.add(object);
                View view = getView(mObjects.size() - 1, null, mViewGroup);
                mViewGroup.addView(view, view.getLayoutParams());
            }
            enforceLimitation();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    public void setAll(Collection<? extends T> collection) {
        synchronized (mLock) {
            mViewGroup.removeAllViews();
            mObjects.clear();
            for(T object : collection) {
                mObjects.add(object);
                View view = getView(mObjects.size() - 1, null, mViewGroup);
                mViewGroup.addView(view, view.getLayoutParams());
            }
            enforceLimitation();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void update(int idx, T item) {
        if(idx < 0) return;
        synchronized (mLock) {
            if(idx >= mObjects.size()) return;
            mObjects.set(idx, item);
            View view = mViewGroup.getChildAt(idx);
            getView(idx, view, mViewGroup);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void remove(T item) {
        synchronized (mLock) {
            int i = mObjects.indexOf(item);
            if(i == -1) return;
            mViewGroup.removeViewAt(i);
            mObjects.remove(i);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void removeAll(Collection<? extends T> items) {
        synchronized (mLock) {
            mViewGroup.removeAllViews();
            mObjects.removeAll(items);
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void clear() {
        synchronized (mLock) {
            mViewGroup.removeAllViews();
            mObjects.clear();
        }
        if (mNotifyOnChange) notifyDataSetChanged();
    }

    final public void sort(Comparator<? super T> comparator) {
        synchronized (mLock) {
            mViewGroup.removeAllViews();
            Collections.sort(mObjects, comparator);
            final int afterCount = getCount();
            for (int i = 0; i < afterCount; ++i) {
                View view = getView(i, null, mViewGroup);
                mViewGroup.addView(view, view.getLayoutParams());
            }
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

    private boolean enforceLimitation() {
        if(mMaxStore != -1 && mMaxStore > 0 && mObjects.size() > mMaxStore) {
            int drop = Math.abs(mObjects.size() - mMaxStore);
            for(int i = 0; i < drop; ++i) {
                mObjects.remove(0);
                mViewGroup.removeViewAt(0);
            }
            return drop != 0;
        }
        return false;
    }

}
