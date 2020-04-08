package com.validvoice.voxidem.scenes.user_setup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.common.base.Stopwatch;
import com.validvoice.dynamic.cloud.CloudArray;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.cloud.ICloudObject;
import com.validvoice.dynamic.db.ContractController;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.voxidem.BuildConfig;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.cloud.ControlKeys;
import com.validvoice.voxidem.cloud.UserAccountDetails;
import com.validvoice.voxidem.cloud.UserDetails;
import com.validvoice.voxidem.db.contracts.AccountsContract;
import com.validvoice.voxidem.db.models.AccountModel;
import com.validvoice.voxidem.db.models.DeviceModel;
import com.validvoice.voxidem.scenes.subs.capture.CaptureFragment;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UserSetupFragment extends SceneFragment {
    private static final String TAG = "UserSetupFragment";


    private LinearLayout mStartPage;
    private LinearLayout mCompanyInfo;
    private TextInputEditText mCompanyId;
    private TextInputLayout mCompanyIdLayout;
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_setup, container, false);



        mStartPage = view.findViewById(R.id.start_page);
        mCompanyInfo = view.findViewById(R.id.company_info);
        mCompanyId = view.findViewById(R.id.scene_user_company_id);
        mCompanyIdLayout = view.findViewById(R.id.scene_user_company_id_layout);


        if(VoxidemPreferences.isUserDefined()) {
            //view.findViewById(R.id.user_setup_continue_user_bar).setVisibility(View.VISIBLE);
            View user_continue = view.findViewById(R.id.user_setup_continue_user_button);
            user_continue.setVisibility(View.VISIBLE);
            user_continue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSceneController().dispatchExpand(R.id.actor_enroll);
                }
            });
        }

        view.findViewById(R.id.user_setup_new_user_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoxidemPreferences.clearUserDetails(getActivity());
                getSceneController().dispatchExpand(R.id.actor_add);
            }
        });

            if (VoxidemPreferences.isCompanyInfoSet()){
                mStartPage.setVisibility(View.VISIBLE);
                mCompanyInfo.setVisibility(View.GONE);
            }else{
                mStartPage.setVisibility(View.GONE);
                mCompanyInfo.setVisibility(View.VISIBLE);
            }

        view.findViewById(R.id.company_info_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((mCompanyId.getText().toString().isEmpty())){
                    return;
                }
                CloudMessage message = CloudMessage.Get("access.{@company_id}");
                message.putString("company_id", mCompanyId.getText().toString());

                final Activity activity = getActivity();
                if(activity != null) {
                    message.send(activity, new CloudController.ResponseOnUiCallback() {
                        @Override
                        public void onResult(CloudResult result) {

                            Log.d(TAG, "onResult:::instanceof:::::::::::::::::"+(result.getData() instanceof ControlKeys));
                            if (result.getData() instanceof ControlKeys) {
                                ControlKeys companyInfo = (ControlKeys) result.getData();
                                Log.d(TAG, "ApplicationKey: " + companyInfo.getApplicationKey() + ", DevelopmentKey:"+ companyInfo.getDevelopmentKey());
                                VoxidemPreferences.setCompanyInfo(true);
                                VoxidemPreferences.setKeys(companyInfo.getApplicationKey(),companyInfo.getDevelopmentKey());
                                Intent refresh = new Intent(getActivity(), MainActivity.class);
                                getActivity().finish();
                                startActivity(refresh);
                            }
                        }

                        @Override
                        public void onError(CloudError error) {
                            Toast.makeText(activity, getResources().getString(R.string.scene_user_new_error), Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void onFailure(Exception ex) {
                            Toast.makeText(activity, getResources().getString(R.string.scene_user_new_error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        if (VoxidemPreferences.getSelectedLanguage().isEmpty()){
            changeAppLanguage();
        }

        view.findViewById(R.id.settings_language_select).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAppLanguage();
            }
        });


        view.findViewById(R.id.scene_user_setup_existing_user_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VoxidemPreferences.clearUserDetails(getActivity());
                getSceneController().dispatchExpand(R.id.actor_existing);
            }
        });

        if (BuildConfig.FLAVOR.equals("dev") || BuildConfig.FLAVOR.equals("local")) {

            view.findViewById(R.id.user_setup_capture_code_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getSceneDirector().setData(CaptureFragment.CAPTURE_MODE, CaptureFragment.CaptureMode.Enroll);
                    getSceneController().dispatchExpand(R.id.actor_capture);
                }
            });

        }
        getActivity().findViewById(R.id.botton_nav).setVisibility(View.GONE);


        return view;
    }

   public void changeAppLanguage(){
       final String[] languages = {"English", "Vietnamese"};
       AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
       builder.setTitle("Select Language");
       builder.setItems(languages, new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) {
               switch (which) {
                   case 0:
                       Toast.makeText(getActivity(), "English-en", Toast.LENGTH_LONG).show();
                       if(((MainActivity)getActivity()).setLocale("en")){
                           Intent refresh = new Intent(getActivity(), MainActivity.class);
                           getActivity().finish();
                           startActivity(refresh);
                       }
                       break;
                   case 1:
                       Toast.makeText(getActivity(), "Vietnamese-vi", Toast.LENGTH_LONG).show();
                       if(((MainActivity)getActivity()).setLocale("vi")){
                           Intent refresh = new Intent(getActivity(), MainActivity.class);
                           getActivity().finish();
                           startActivity(refresh);
                       }
                       break;
                   default:
                       break;
               }
           }
       });
       builder.show();
   }

}
