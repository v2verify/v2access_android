package com.validvoice.voxidem.scenes.devices;

import android.app.Activity;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.db.ContractController;
import com.validvoice.dynamic.scene.ISceneControllerFactory;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDirector;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.contracts.HistoryContract;
import com.validvoice.voxidem.db.models.DeviceModel;
import com.validvoice.voxidem.db.models.HistoryModel;

public class DevicesSceneController extends SceneController {

    private static final String TAG = "DevicesSceneController";

    private DeviceModel mModel;

    private DevicesSceneController(SceneDirector sceneManager, SceneLayout sceneLayout) {
        super(R.id.scene_devices, 400, sceneManager, sceneLayout);
        //addActor(new DeviceSceneFooter());
        addActor(new DevicesSceneActor());
        addActor(new DeviceDetailsSceneActor());
        addActor(new DeviceOptionsSceneActor());
        addActor(new DeviceEditSceneActor());
        addActor(new DeviceDeleteSceneHeader());
    }

    @Override
    public void onOpen() {
        dispatchExpand(R.id.actor_footer, R.id.actor_home);
    }

    @Override
    public void onClose() {
        dispatchCollapse(R.id.actor_footer, R.id.actor_home);
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
        if(backPressed) {
            switch(getActiveActorId()) {
                case R.id.actor_options:
                case R.id.actor_edit:
                case R.id.actor_delete: {
                    dispatchAction(getActiveActorId(), R.id.action_cancel, null);
                } break;
            }
        }
        if(getActiveActorId() != getHomeActorId()) {
            dispatchCollapse(getActiveActorId());
        }
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.devices, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_save);
        if(item != null) {
            item.setVisible(getActiveActorId() == R.id.actor_edit);
        }
        item = menu.findItem(R.id.action_edit);
        if(item != null) {
            item.setVisible(getActiveActorId() == R.id.actor_options);
        }
        item = menu.findItem(R.id.action_delete);
        if(item != null) {
            item.setVisible(getActiveActorId() == R.id.actor_options);
        }
        item = menu.findItem(R.id.action_commit);
        if(item != null) {
            item.setVisible(getActiveActorId() == R.id.actor_delete);
        }
        item = menu.findItem(R.id.action_cancel);
        if(item != null) {
            item.setVisible(getActiveActorId() == R.id.actor_edit || getActiveActorId() == R.id.actor_delete);
        }
        return true;
    }

    private class DevicesSceneActor extends SceneLayout.FragmentHomeActor<DevicesSceneController, DevicesFragment> {
        DevicesSceneActor() {
            super(DevicesSceneController.this, R.id.actor_home, DevicesFragment.class);
        }
    }

    /*
    private class DeviceSceneFooter extends SceneLayout.SceneActorFooter {

        private Button mSignOffButton;

        public DeviceSceneFooter() {
            super(DevicesSceneController.this, R.id.actor_footer);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup parent, boolean addToRoot) {
            View view = inflater.inflate(R.layout.scene_devices_footer, parent, addToRoot);
            mSignOffButton = view.findViewById(R.id.scene_device_sign_out_all_devices);
            mSignOffButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // send log off message

                    CloudMessage message = CloudMessage.Delete("voxidem.LogOff.{@v2w_user_name}.Devices");
                    message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
                    message.send(getActivity(), new CloudController.ResponseOnUiCallback() {
                        @Override
                        public void onResult(CloudResult result) {
                        }

                        @Override
                        public void onError(CloudError error) {
                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            ex.printStackTrace();
                        }
                    });

                }
            });
            return view;
        }

        @Override
        public void onDestroyView(View view) {
            mSignOffButton = null;
        }
    }
    */

    private class DeviceDetailsSceneActor extends SceneLayout.FragmentSceneActor<DevicesSceneController, DeviceDetailsFragment> {

        private DeviceModel mDeviceModel;

        DeviceDetailsSceneActor() {
            super(DevicesSceneController.this, R.id.actor_details, DeviceDetailsFragment.class, TRANSITION_RIGHT_TO_LEFT);
            setBlendedToolbarId(R.id.toolbar_overlay);
        }

        @Override
        public void onAttachingFragment(DeviceDetailsFragment fragment) {
            super.onAttachingFragment(fragment);
            fragment.setDeviceModel(mDeviceModel);
        }

        @Override
        public boolean onActorAction(int action_id, Object data) {
            if(action_id == R.id.action_item && data instanceof DeviceModel) {
                mDeviceModel = (DeviceModel)data;
                return true;
            }
            return super.onActorAction(action_id, data);
        }
    }

    private class DeviceOptionsSceneActor extends SimpleOptionsSceneActorHeader {

        DeviceOptionsSceneActor() {
            super(DevicesSceneController.this, new SimpleOptionsItemListener() {
                @Override
                public boolean onItem(Object item) {
                    if(item instanceof DeviceModel) {
                        mModel = (DeviceModel)item;
                        return true;
                    }
                    return false;
                }
            }, true);
        }
    }

    private class DeviceEditSceneActor extends SceneLayout.SceneActorHeader {

        private TextInputLayout scene_devices_edit_nickname_layout;
        private TextInputEditText scene_devices_edit_nickname;

        DeviceEditSceneActor() {
            super(DevicesSceneController.this, R.id.actor_edit, SceneLayout.SceneActorView.SCRIM_ON);
            setBlendedToolbarId(R.id.toolbar_overlay);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup parent, boolean addToRoot) {
            Log.d(TAG, "onCreateView::::::::::::::::::::::parent"+parent);
            Log.d(TAG, "onCreateView::::::::::::::::::::::addToRoot"+addToRoot);
            View view = inflater.inflate(R.layout.scene_devices_edit_header, parent, addToRoot);
            scene_devices_edit_nickname_layout = view.findViewById(R.id.scene_devices_edit_nickname_layout);
            scene_devices_edit_nickname = view.findViewById(R.id.scene_devices_edit_nickname);
            Log.d(TAG, "onCreateView::::::::::::::sadasdasdasdasdassadasdasdadasdsadsadsadasdsads::::::::parent"+parent);
            return view;
        }

        @Override
        public void onActorOpening() {
            super.onActorOpening();
            if(mModel != null && isActorAvailable()) {
                scene_devices_edit_nickname.setText(mModel.getDeviceName());
            } else {
                scene_devices_edit_nickname.setText("");
            }
        }

        @Override
        public void onActorOpened() {
            scene_devices_edit_nickname.requestFocus();
        }

        @Override
        public void onDestroyView(View view) {
            scene_devices_edit_nickname_layout = null;
            scene_devices_edit_nickname = null;
        }

        @Override
        public boolean onActorAction(int action_id, Object data) {
            if(action_id == R.id.action_save) {
                final String nickname = scene_devices_edit_nickname.getText().toString();

                if(nickname.isEmpty()) {
                    scene_devices_edit_nickname_layout.setError(getResources().getString(R.string.scene_devices_edit_nickname_required_error));
                    return true;
                } else if(getContractController().contains(DevicesContract.CONTENT_URI, DevicesContract.DEVICE_NICKNAME, nickname)) {
                    scene_devices_edit_nickname_layout.setError(getResources().getString(R.string.scene_devices_edit_nickname_exists_error));
                    return true;
                } else {
                    scene_devices_edit_nickname_layout.setErrorEnabled(false);
                }

                hideSoftKeyboardFromWindow();
                onCloseScene(false);

                CloudMessage message = CloudMessage.Update("v2access.Devices.{@v2w_user_name}");
                message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
                message.putString("v2w_device_id", mModel.getDeviceId());
                message.putString("v2w_nick_name", nickname);
                message.send(getActivity(), new CloudController.ResponseOnUiCallback() {
                    @Override
                    public void onResult(CloudResult result) {
                        Activity activity = getActivity();
                        if(activity != null) {
                            final String oldName = mModel.getDeviceName();
                            mModel.updateDeviceName(nickname);
                            final ContractController cc = getContractController();
                            cc.updateModel(DevicesContract.CONTENT_URI, mModel);
                            cc.insertModel(HistoryContract.CONTENT_URI,
                                    HistoryModel.updateDeviceRecord(
                                            mModel.getId(),
                                            VoxidemPreferences.getUserAccountName(),
                                            oldName,
                                            nickname
                                    ));
                            Toast.makeText(activity, "Device: " + nickname + ", updated!", Toast.LENGTH_SHORT).show();
                            mModel = null;
                        }
                    }

                    @Override
                    public void onError(CloudError error) {
                        Activity activity = getActivity();
                        if(activity != null) {
                            Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
                            mModel = null;
                        }
                    }

                    @Override
                    public void onFailure(Exception ex) {
                        Activity activity = getActivity();
                        if(activity != null) {
                            ex.printStackTrace();
                            Toast.makeText(activity, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            mModel = null;
                        }
                    }
                });
                hideSoftKeyboardFromWindow();
                scene_devices_edit_nickname.setText("");
                onCloseScene(false);
                return true;
            } else if(action_id == R.id.action_cancel) {
                mModel = null;
                hideSoftKeyboardFromWindow();
                scene_devices_edit_nickname.setText("");
                onCloseScene(false);
                return true;
            }
            return false;
        }
    }

    private class DeviceDeleteSceneHeader extends SceneLayout.SceneActorHeader {

        private TextView mDeleteLabel;
        private TextView mDeleteMessage;

        DeviceDeleteSceneHeader() {
            super(DevicesSceneController.this, R.id.actor_delete, SceneLayout.SceneActorView.SCRIM_ON);
            setBlendedToolbarId(R.id.toolbar_overlay);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup parent, boolean addToRoot) {
            View view = inflater.inflate(R.layout.scene_delete_header, parent, addToRoot);
            mDeleteLabel = view.findViewById(R.id.delete_label);
            mDeleteLabel.setText(String.format(getResources().getString(R.string.action_delete_label), getResources().getString(R.string.scene_devices_label)));
            mDeleteMessage = view.findViewById(R.id.delete_message);
            return view;
        }

        @Override
        public void onDestroyView(View view) {
            mDeleteLabel = null;
            mDeleteMessage = null;
        }

        @Override
        public void onActorOpening() {
            super.onActorOpening();
            if(isActorAvailable()) {
                mDeleteMessage.setText(
                        String.format(
                                getResources().getString(R.string.action_delete_message),
                                mModel.getDeviceName()
                        )
                );
            }
        }

        @Override
        public void onActorClosed() {
            mDeleteMessage.setText("");
        }

        @Override
        public boolean onActorAction(int id, Object data) {
            if(id == R.id.action_commit) {
                CloudMessage message = CloudMessage.Delete("v2access.Devices.{v2w_user_name}.{v2w_device_id}");
                message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
                message.putString("v2w_device_id", mModel.getDeviceId());
                final ContractController cc = getContractController();
                cc.deleteModel(DevicesContract.CONTENT_URI, mModel);
                cc.insertModel(HistoryContract.CONTENT_URI,
                        HistoryModel.deleteDeviceRecord(
                                mModel.getId(),
                                VoxidemPreferences.getUserAccountName(),
                                mModel.getDeviceName()
                        ));

                message.send(getActivity(), new CloudController.ResponseOnUiCallback() {
                    @Override
                    public void onResult(CloudResult result) {
                        Activity activity = getActivity();
                        if(activity != null) {
                            Toast.makeText(activity, "Device: " + mModel.getDeviceName() + ", deleted!", Toast.LENGTH_SHORT).show();
                            mModel = null;
                        }
                    }

                    @Override
                    public void onError(CloudError error) {
                        Activity activity = getActivity();
                        if(activity != null) {
                            // Until a proper error system is determined, use codes
                            if (error.getCode() == 404) {
                                String msg = "Device: " + mModel.getDeviceName() + ", not found on server. Deleting locally";
                                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                            mModel = null;
                        }
                    }

                    @Override
                    public void onFailure(Exception ex) {
                        ex.printStackTrace();
                        Activity activity = getActivity();
                        if(activity != null) {
                            Toast.makeText(activity, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            mModel = null;
                        }
                    }
                });
                hideSoftKeyboardFromWindow();
                onCloseScene(false);
                return true;
            } else if(id == R.id.action_cancel) {
                hideSoftKeyboardFromWindow();
                onCloseScene(false);
                mModel = null;
                return true;
            }
            return false;
        }
    }

    public static class Factory implements ISceneControllerFactory {

        @Override
        public SceneController CreateSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout) {
            return new DevicesSceneController(sceneDirector, sceneLayout);
        }

    }
}
