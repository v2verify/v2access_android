package com.validvoice.voxidem.scenes.subs.capture;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDialogFragment;
import com.validvoice.voxidem.cloud.QrIntent;
import com.validvoice.voxidem.cloud.QrUserAccount;

public class AccountSelectionDialogFragment extends SceneDialogFragment {

    private QrIntent mQrIntent;
    private AccountSelectionListener mListener;

    public interface AccountSelectionListener {
        void onAccountSelected(int id, String name);
        void onAccountCancelled();
    }

    public AccountSelectionDialogFragment() {

    }

    public void setQrIntent(QrIntent qrIntent) {
        mQrIntent = qrIntent;
    }

    public void setAccountSelectionListener(AccountSelectionListener listener) {
        mListener = listener;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        AccountArrayAdapter adapter = new AccountArrayAdapter(getActivity());
        adapter.setAll(mQrIntent.getAccounts());

        alertDialogBuilder.setTitle("Which " + mQrIntent.getCompany() + " account do you want to login to?");
        alertDialogBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mListener != null) {
                    QrUserAccount acct = (QrUserAccount)mQrIntent.getAccounts().getList().get(which);
                    mListener.onAccountSelected(acct.getId(), acct.getName());
                }
            }
        });
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setNeutralButton("cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(mListener != null) {
                            mListener.onAccountCancelled();
                        }
                        dialog.cancel();
                    }
                }
        );

        return alertDialogBuilder.create();
    }

    public static AccountSelectionDialogFragment newInstance(SceneController controller, QrIntent qrIntent, AccountSelectionListener listener) {
        AccountSelectionDialogFragment fragment = new AccountSelectionDialogFragment();
        fragment.setSceneController(controller);
        fragment.setQrIntent(qrIntent);
        fragment.setAccountSelectionListener(listener);
        return fragment;
    }

}
