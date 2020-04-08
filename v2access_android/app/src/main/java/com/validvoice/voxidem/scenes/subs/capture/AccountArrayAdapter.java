package com.validvoice.voxidem.scenes.subs.capture;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import com.validvoice.dynamic.cloud.CloudArrayAdapter;
import com.validvoice.voxidem.cloud.QrUserAccount;

class AccountArrayAdapter extends CloudArrayAdapter<QrUserAccount> {

    AccountArrayAdapter(@NonNull Context context) {
        super(context, android.R.layout.simple_list_item_1, QrUserAccount.class);
    }

    private class ViewHolder {
        TextView account_name;
    }

    @Override
    public Object newTag(View view) {
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.account_name = (TextView)view.findViewById(android.R.id.text1);
        return viewHolder;
    }

    @Override
    public void adjustTag(Object tag, Context context, QrUserAccount item) {
        ViewHolder viewHolder = (ViewHolder)tag;
        viewHolder.account_name.setText(item.getName());
    }
}
