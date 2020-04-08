package com.validvoice.voxidem.scenes.history;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.contracts.HistoryContract;
import com.validvoice.voxidem.db.models.HistoryModel;

public class HistoryFragment extends SceneFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView mListView;
    HistoryCursorAdapter mHistoryCursorAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        mListView = (ListView) view.findViewById(R.id.historyListView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mHistoryCursorAdapter = new HistoryCursorAdapter(
                getSceneController(),
                0
        );
        ((MainActivity) getActivity()).spanTitleBar("History");
    mListView.setAdapter(mHistoryCursorAdapter);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onRefresh() {
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                HistoryContract.CONTENT_URI,
                HistoryModel.HISTORY_PROJECTION,
                null,
                null,
                HistoryContract.HISTORY_DATETIME + " DESC"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mHistoryCursorAdapter.updateCursor(cursor);
        mHistoryCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHistoryCursorAdapter.updateCursor(null);
        mHistoryCursorAdapter.notifyDataSetChanged();
    }

}
