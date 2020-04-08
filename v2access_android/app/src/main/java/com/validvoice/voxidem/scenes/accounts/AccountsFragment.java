package com.validvoice.voxidem.scenes.accounts;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.db.contracts.AccountsContract;
import com.validvoice.voxidem.db.models.AccountModel;

public class AccountsFragment extends SceneFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView mListView;
    private AccountsCursorAdapter mAccountsCursorAdapter;
    //private FloatingActionButton mFloatingActionButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);

        mListView = (ListView) view.findViewById(R.id.accountsListView);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor)mAccountsCursorAdapter.getItem(position);
                dispatchExpand(R.id.actor_details, R.id.action_item, AccountModel.createRecordFromCursor(c));
            }
        });

        //mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
        //    @Override
        //    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //        final Cursor c = (Cursor)mAccountsCursorAdapter.getItem(position);
        //        dispatchExpand(R.id.actor_options, R.id.action_item, AccountModel.createRecordFromCursor(c));
        //        mAccountsCursorAdapter.setSelectedPosition(position);
        //        return true;
        //    }
        //});

        //mFloatingActionButton = (FloatingActionButton) view.findViewById(R.id.action_capture_or_verify);
        //mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        // go to qr scanner for verify link qr code
        //    }
        //});

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAccountsCursorAdapter = new AccountsCursorAdapter(
                getSceneController(),
                0
        );
        mListView.setAdapter(mAccountsCursorAdapter);
        getLoaderManager().restartLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                AccountsContract.CONTENT_URI,
                AccountModel.ACCOUNT_PROJECTION,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAccountsCursorAdapter.updateCursor(cursor);
        mAccountsCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAccountsCursorAdapter.updateCursor(null);
        mAccountsCursorAdapter.notifyDataSetChanged();
    }
}
