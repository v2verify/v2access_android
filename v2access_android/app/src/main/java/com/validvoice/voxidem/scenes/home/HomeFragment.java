package com.validvoice.voxidem.scenes.home;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.validvoice.dynamic.cloud.CloudArray;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.cloud.ICloudObject;
import com.validvoice.dynamic.db.ContractController;
import com.validvoice.dynamic.scene.SceneDirector;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.dynamic.scene.SceneSwipeRefreshLayout;
import com.validvoice.dynamic.speech.service.AdaptiveSpeechServiceConnection;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.cloud.UserAccountDetails;
import com.validvoice.voxidem.cloud.UserDetails;
import com.validvoice.voxidem.cloud.UserDeviceDetails;
import com.validvoice.voxidem.cloud.UserIntent;
import com.validvoice.voxidem.db.contracts.AccountsContract;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.contracts.HistoryContract;
import com.validvoice.voxidem.db.models.AccountModel;
import com.validvoice.voxidem.db.models.DeviceModel;
import com.validvoice.voxidem.scenes.subs.capture.CaptureFragment;
import com.validvoice.voxidem.scenes.subs.sve.VerifyFragment;
import com.validvoice.voxidem.scenes.subs.sve.VerifyIntent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HomeFragment extends SceneFragment implements
        IntentArrayAdapter.IntentsSelected {

    private static final String TAG = "HomeFragment";

    private ListView mListView;
    private TextView mEmptyView;
    private TextView devicesInfo;
    private ImageView upwardArrow;
    private ImageView sidewaysArrow;
    private ImageView downArrow;
    private ImageView logoIcon;
    private SceneSwipeRefreshLayout mSwipeRefresh;
    private IntentArrayAdapter mIntentArrayAdapter;
   // private FloatingActionButton mFloatingActionButton;
    private Button mFloatingActionButton;
    private Button mMobileLogin;
    private boolean mIsCapture = true;
    private boolean mIsFetching = false;
    private long standardEnrollDate = 1576180573;

    // Resource caches
    private Drawable mQrCode;
    private Drawable mLogin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        final Resources resources = getResources();
        final Resources.Theme theme = inflater.getContext().getTheme();
        mQrCode = ResourcesCompat.getDrawable(resources, R.drawable.ic_qr_code, theme);
        mLogin = ResourcesCompat.getDrawable(resources, R.drawable.ic_login, theme);

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        upwardArrow = view.findViewById(R.id.upward_arrow);
        sidewaysArrow = view.findViewById(R.id.sideways_arrow);
        logoIcon = view.findViewById(R.id.logo_icon);
        downArrow = view.findViewById(R.id.down_arrow);

        mSwipeRefresh = view.findViewById(R.id.swiperefresh);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
                try {
                    fetchIntents();
                } catch(IOException ex) {
                    ex.printStackTrace();
                    mSwipeRefresh.setRefreshing(false);
                }
            }
        });

        mListView = view.findViewById(android.R.id.list);
        mEmptyView = view.findViewById(android.R.id.empty);
        devicesInfo = view.findViewById(R.id.devices_info);
        VoxidemPreferences.setPrefIsMobileLogin(false);


        ((MainActivity) getActivity()).spanTitleBar("Home");

        manageEmptyViews();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            devicesInfo.setText(Html.fromHtml(getString(R.string.scene_home_device_info), Html.FROM_HTML_MODE_COMPACT));
        } else {
            devicesInfo.setText(Html.fromHtml(getString(R.string.scene_home_device_info)));
        }

        mFloatingActionButton = view.findViewById(R.id.action_capture_or_verify);
        mMobileLogin = view.findViewById(R.id.action_mobile_login);
        Animation pulse = AnimationUtils.loadAnimation(getActivity(), R.anim.pulse);
//        mFloatingActionButton.startAnimation(pulse);
//        mMobileLogin.startAnimation(pulse);

        mMobileLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!VoxidemPreferences.getBackgroundNoiseFlag()) {
                    VoxidemPreferences.setPrefIsMobileLogin(true);
                    VerifyIntent verifyIntent = new VerifyIntent(
                            VerifyIntent.VerifyMode.verifyMobileLogin,
                            VoxidemPreferences.getUserAccountName(),
                            VoxidemPreferences.getUserVoicePrintId(),
                            VoxidemPreferences.getUserLanguage()
                    );
                    SceneDirector sd = getSceneDirector();
                    sd.setData(VerifyFragment.VERIFY_INTENT, verifyIntent);
                    verifyIntent.resolveIds(getContractController());
                    getSceneController().dispatchExpand(R.id.actor_verify);
                }else{

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle(R.string.scene_verify_instructions_speech_tip);
                    alertDialogBuilder.setMessage(R.string.scene_background_failure);
                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    return;
                                }
                            }
                    );

                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }
            }
        });


        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!VoxidemPreferences.getBackgroundNoiseFlag()){
                    final SceneDirector sd = getSceneDirector();
                if (mIsCapture) {
                    sd.setData(CaptureFragment.CAPTURE_MODE, CaptureFragment.CaptureMode.Verify);
                    getSceneController().dispatchExpand(R.id.actor_capture);
                } else if (mIntentArrayAdapter.getSelectedCount() > 0) {
                    final VerifyIntent verifyIntent = new VerifyIntent(
                            VerifyIntent.VerifyMode.VerifyDevice,
                            VoxidemPreferences.getUserAccountName(),
                            VoxidemPreferences.getUserVoicePrintId(),
                            VoxidemPreferences.getUserLanguage()
                    );
                    final List<String> intents = new ArrayList<>();
                    final List<Integer> accounts = new ArrayList<>();
                    for (int i = 0; i < mIntentArrayAdapter.getCount(); ++i) {
                        UserIntent io = mIntentArrayAdapter.getItemType(i);
                        if (io.GetIsSelected()) {
                            verifyIntent.addDeviceInstance(io.GetIntentId(), io.GetDeviceNickName(),
                                    io.GetCompanyUser(), io.GetCompany(), io.GetMaxAttempts());
                            intents.add(io.GetIntentId());
                            accounts.add(io.GetCompanyUserAccountId());
                        }
                    }

                    CloudMessage message = CloudMessage.Validate("v2access.Intents.{@v2w_user_name}");
                    message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
                    message.putObject("v2w_interaction_ids", intents);
                    message.putObject("v2w_account_ids", accounts);
                    message.send(getActivity(), new CloudController.ResponseOnUiCallback() {
                        @Override
                        public void onResult(CloudResult result) {
                            sd.setData(VerifyFragment.VERIFY_INTENT, verifyIntent);
                            verifyIntent.resolveIds(getContractController());
                            getSceneController().dispatchExpand(R.id.actor_verify);
                        }

                        @Override
                        public void onError(CloudError error) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setTitle("Network Error");
                            alertDialogBuilder.setMessage(R.string.scene_intent_failure);
                            alertDialogBuilder.setCancelable(true);
                            alertDialogBuilder.setPositiveButton(R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            return;
                                        }
                                    }
                            );

                            alertDialogBuilder.create();
                            alertDialogBuilder.show();
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            Toast.makeText(getActivity(), "Please refresh the browser and Try again", Toast.LENGTH_SHORT).show();

                            ex.printStackTrace();
                        }
                    });

                    mIsCapture = true;
                }
            }else{
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle(R.string.scene_verify_instructions_speech_tip);
                    alertDialogBuilder.setMessage(R.string.scene_background_failure);
                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setPositiveButton(R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                   return;
                                }
                            }
                    );

                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                }
            }
        });

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIntentArrayAdapter = new IntentArrayAdapter(view.getContext(), this);
        mIntentArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                if(mIntentArrayAdapter.getCount() == 0) {
                    manageEmptyViews();
                    if (getActivity() != null ) {
                        downArrow.setVisibility(View.VISIBLE);
                        Animation animate = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);
                        downArrow.setAnimation(animate);
                    }
                } else {
                    mEmptyView.setVisibility(View.GONE);
                    logoIcon.setVisibility(View.GONE);
                    downArrow.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                    devicesInfo.setVisibility(View.VISIBLE);
                    animateArrows();
                }
            }

            @Override
            public void onInvalidated() {
                onChanged();
            }
        });
        mListView.setAdapter(mIntentArrayAdapter);
        onRefresh();
    }

    @Override
    public void onRefresh() {
        if(!mIsFetching) {
            mSwipeRefresh.setRefreshing(true);
            try {
                fetchIntents();
            } catch(IOException ex) {
                ex.printStackTrace();
                mSwipeRefresh.setRefreshing(false);
            }
        }
    }

    @Override
    public boolean onActorAction(int id, Object data) {
        if(id == R.id.action_refresh) {
            onRefresh();
            return true;
        }
        return false;
    }

    private void fetchIntents() throws IOException {
        if(!mIsFetching) {
            mIsFetching = true;
            CloudMessage message = CloudMessage.List("v2access.Intents.{v2w_user_name}");
            message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());

            message.send(new CloudController.ResponseCallback() {
                @Override
                public void onResult(final CloudResult result) {
                    if(result.getData() instanceof CloudArray) {
                        HashMap<String, Long> resolvedIds = new HashMap<>();
                        ContractController cc = getContractController();
                        final CloudArray array = (CloudArray)result.getData();
                        for (ICloudObject io : array.getList()) {
                            UserIntent ui = (UserIntent) io;
                            if (!resolvedIds.containsKey(ui.GetDeviceNickName())) {
                                long id = cc.getId(DevicesContract.CONTENT_URI,
                                        DevicesContract.DEVICE_NICKNAME, ui.GetDeviceNickName());
                                resolvedIds.put(ui.GetDeviceNickName(), id);
                                cc.updateField(DevicesContract.CONTENT_URI, id,
                                        DevicesContract.DEVICE_LAST_KNOWN_IP_ADDRESS,
                                        ui.GetDeviceIp());
                            }
                        }
                        Activity activity = getActivity();
                        if(activity != null) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mIsFetching = false;
                                    mSwipeRefresh.setRefreshing(false);
                                    mIntentArrayAdapter.setAll(array);
                                }
                            });
                        }
                    }
                }

                @Override
                public void onError(final CloudError error) {
                    Activity activity = getActivity();
                    if(activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mIsFetching = false;
                                mSwipeRefresh.setRefreshing(false);
                                Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }

                @Override
                public void onFailure(final Exception ex) {
                    Activity activity = getActivity();
                    if(activity != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mIsFetching = false;
                                mSwipeRefresh.setRefreshing(false);
                                ex.printStackTrace();
                                Toast.makeText(getActivity(), ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onIntentsSelected() {
        mIsCapture = false;
        mFloatingActionButton.setText(R.string.intent_log_in);
       // animateFab(mLogin);
    }

    @Override
    public void onIntentsDeselected() {
        mIsCapture = true;
        mFloatingActionButton.setText(R.string.scan_qr_code);
      //  animateFab(mQrCode);
    }

    @Override
    public void onStart() {
        super.onStart();

//        if (VoxidemPreferences.getUserEnrollmentDate() == 0){
//            final Activity activity = getActivity();
//            if(activity != null) {
//                startActivity(new Intent(activity, MainActivity.class));
//                activity.finish();
//            }
//        }else {
//
//        }

        Log.d(TAG, "onStart::::::::::::VoxidemPreferences.getUserIsReEnrolled():::::::::::::::::::::::"+VoxidemPreferences.getUserIsReEnrolled());
        if (VoxidemPreferences.getUserIsReEnrolled()){
            final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            final SharedPreferences.Editor editor = sp.edit();
            getContractController().clear(DevicesContract.CONTENT_URI);
            getContractController().clear(HistoryContract.CONTENT_URI);
            VoxidemPreferences.clearUserDetails(getActivity());
            editor.remove(SceneLayout.PREF_ALLOW_THEME_ANIMATIONS).apply();
            final Activity activity = getActivity();
            if(activity != null) {
                startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            }
        }
        getActivity().findViewById(R.id.botton_nav).setVisibility(View.VISIBLE);

        checkIfReEnrollmentNeeded();


        ((MainActivity) getActivity()).spanTitleBar("Home");

    }

    private void animateFab(final Drawable drawable) {
        mFloatingActionButton.clearAnimation();
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
            //    mFloatingActionButton.setImageDrawable(drawable);

                // Scale up animation
                ScaleAnimation expand =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(100);     // animation duration in milliseconds
                expand.setInterpolator(new AccelerateInterpolator());
                mFloatingActionButton.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mFloatingActionButton.startAnimation(shrink);
    }


    public void checkIfReEnrollmentNeeded(){
        CloudMessage message = CloudMessage.Get("v2access.User.{@v2w_user_name}");
        message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
        final Activity activity = getActivity();
        if(activity != null) {
            message.send(activity, new CloudController.ResponseOnUiCallback() {
                @Override
                public void onResult(CloudResult result) {
                    if (result.getData() instanceof UserDetails) {
                        UserDetails details = (UserDetails) result.getData();
                        VoxidemPreferences.setUserDetails(activity, details);
                        if (VoxidemPreferences.getUserEnrollmentDate() < standardEnrollDate) {
                            VoxidemPreferences.setPrefIsReEnrollment(true);
                            VoxidemPreferences.setTrainerPin(VoxidemPreferences.getTrainerPin());
                            dispatchExpand(R.id.actor_enroll);
                        }
                    }
                }

                @Override
                public void onError(CloudError error) {

                }

                @Override
                public void onFailure(Exception ex) {

                }
            });
        }

    }
    public void animateArrows(){
        upwardArrow.setVisibility(View.VISIBLE);
        sidewaysArrow.setVisibility(View.VISIBLE);
        if (getActivity() != null ) {
            Animation animate = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);
            upwardArrow.setAnimation(animate);
            sidewaysArrow.setAnimation(animate);
        }
    }

    public void manageEmptyViews(){
        mEmptyView.setVisibility(View.VISIBLE);
        logoIcon.setVisibility(View.VISIBLE);
        mListView.setVisibility(View.GONE);
        devicesInfo.setVisibility(View.GONE);
        upwardArrow.setVisibility(View.GONE);
        sidewaysArrow.setVisibility(View.GONE);
    }

}
