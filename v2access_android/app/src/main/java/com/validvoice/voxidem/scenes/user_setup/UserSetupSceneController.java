package com.validvoice.voxidem.scenes.user_setup;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.common.base.Stopwatch;
import com.steelkiwi.library.ui.SeparateShapesView;
import com.validvoice.dynamic.cloud.CloudArray;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.cloud.ICloudObject;
import com.validvoice.dynamic.db.ContractController;
import com.validvoice.dynamic.scene.ISceneControllerFactory;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDirector;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.voxidem.BuildConfig;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.cloud.UserAccountDetails;
import com.validvoice.voxidem.cloud.UserDetails;
import com.validvoice.voxidem.cloud.UserDeviceDetails;
import com.validvoice.voxidem.db.contracts.AccountsContract;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.models.AccountModel;
import com.validvoice.voxidem.db.models.DeviceModel;
import com.validvoice.voxidem.scenes.subs.capture.CaptureFragment;
import com.validvoice.voxidem.scenes.subs.sve.EnrollFragment;
import com.validvoice.voxidem.scenes.subs.sve.QuestionSet;

import java.util.concurrent.TimeUnit;

public class UserSetupSceneController extends SceneController {
    private static final String TAG = "UserSetupSceneControlle";

    private UserSetupSceneController(SceneDirector sceneManager, SceneLayout sceneLayout) {
        super(R.id.scene_user_setup, 200, sceneManager, sceneLayout);
        addActor(new UserSetupSceneActor());
        addActor(new UserSetupSceneAdd());
        addActor(new UserSetupSceneExisting());
        if (BuildConfig.FLAVOR.equals("dev") || BuildConfig.FLAVOR.equals("local")) {
            addActor(new UserSetupSceneCapture());
        }
        addActor(new UserSetupEnroll());
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
        switch(getActiveActorId()) {
            case R.id.actor_enroll:
                dispatchCollapse(R.id.actor_enroll, R.id.actor_add);
                break;
            case R.id.actor_add:
            case R.id.actor_existing:
                dispatchCollapse(getActiveActorId());
                break;
        }
        getActivity().invalidateOptionsMenu();
    }

    private class UserSetupSceneActor extends SceneLayout.FragmentHomeActor<UserSetupSceneController, UserSetupFragment> {

        UserSetupSceneActor() {
            super(UserSetupSceneController.this, R.id.actor_home, UserSetupFragment.class);
        }

        @Override
        public void onActorClosed() {
            hideSoftKeyboardFromWindow();
        }

    }

    private class UserSetupSceneAdd extends SceneLayout.FragmentSceneActor<UserSetupSceneController, UserDetailsFragment> {

        UserSetupSceneAdd() {
            super(UserSetupSceneController.this, R.id.actor_add, UserDetailsFragment.class, SceneLayout.FragmentActor.TRANSITION_BOTTOM_UP);
        }

        @Override
        public void onActorClosed() {
            hideSoftKeyboardFromWindow();
        }

    }

    private class UserSetupSceneExisting extends SceneLayout.SceneActorHeader {

        private TextInputLayout scene_user_existing_username_layout;
        private TextInputEditText scene_user_existing_username;
        private SeparateShapesView scene_user_existing_login_button;

        UserSetupSceneExisting() {
            super(UserSetupSceneController.this, R.id.actor_existing, SCRIM_ON_LOCKED);
            setBlendedToolbarId(R.id.toolbar_overlay);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup parent, boolean addToRoot) {
            View view = inflater.inflate(R.layout.scene_user_existing_header, parent, addToRoot);
            scene_user_existing_username_layout = view.findViewById(R.id.scene_user_existing_username_layout);
            scene_user_existing_username = view.findViewById(R.id.scene_user_existing_username);
            scene_user_existing_login_button = view.findViewById(R.id.scene_user_existing_login_button);
            scene_user_existing_login_button.setOnButtonClickListener(new SeparateShapesViewClickListener());
            scene_user_existing_login_button.restore();
            return view;
        }

        @Override
        public void onDestroyView(View view) {
            scene_user_existing_username_layout = null;
            scene_user_existing_username = null;
        }

        @Override
        public void onActorOpening() {
            super.onActorOpening();
            scene_user_existing_username.setText("");
        }

        @Override
        public void onActorOpened() {
            scene_user_existing_username.requestFocus();
        }

        private class SeparateShapesViewClickListener implements SeparateShapesView.OnButtonClickListener {

            @Override
            public boolean onLeftButtonClick() {
                final String username = scene_user_existing_username.getText().toString();

                if(username.isEmpty()) {
                    scene_user_existing_username_layout.setError(getResources().getString(R.string.scene_user_existing_username_empty_error));
                    return true;
                } else {
                    scene_user_existing_username_layout.setErrorEnabled(false);
                }

                hideSoftKeyboardFromWindow();

                final Stopwatch stopWatch = Stopwatch.createStarted();

                CloudMessage message = CloudMessage.Get("v2access.User.{@v2w_user_name}");
                message.putString("v2w_user_name", username);

                final Activity activity = getActivity();
                if(activity != null) {
                    message.send(activity, new CloudController.ResponseOnUiCallback() {
                        @Override
                        public void onResult(CloudResult result) {

                            long time = stopWatch.stop().elapsed(TimeUnit.MILLISECONDS);
                            if (result.getData() instanceof UserDetails) {


                                UserDetails details = (UserDetails) result.getData();
                                Log.d(TAG, "onResult:::::::::::::::::::userDetails:::::::::::::::GetmEnrollmentDateGetmEnrollmentDateGetmEnrollmentDate::::::::::::::::::"+details.GetmEnrollmentDate());

                                VoxidemPreferences.setUserDetails(activity, details);
                                ContractController cc = getContractController();
                                if (details.HasAccounts()) {
                                    CloudArray array = details.GetAccounts();
                                    for (ICloudObject io : array.getList()) {
                                        AccountModel model = ((UserAccountDetails) io).getAccountModel();
                                        cc.insertModel(AccountsContract.CONTENT_URI, model);
                                    }
                                }

                                if (details.HasDevices()) {
                                    CloudArray array = details.GetDevices();
                                    for (ICloudObject io : array.getList()) {
                                        DeviceModel model = ((UserDeviceDetails) io).getDeviceModel();
                                        cc.insertModel(DevicesContract.CONTENT_URI, model);
                                    }
                                }

                                if(time < 950) {
                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            activity.startActivity(new Intent(activity, MainActivity.class));
                                            activity.finish();
                                        }
                                    }, 950 - time);
                                } else {
                                    activity.startActivity(new Intent(activity, MainActivity.class));
                                    activity.finish();
                                }

                                Toast.makeText(getActivity(), "User: " + username + ", Session Started!", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onError(CloudError error) {
                            long time = stopWatch.stop().elapsed(TimeUnit.MILLISECONDS);
                            if(time < 950) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        onCloseScene(false);
                                    }
                                }, 950 - time);
                            } else {
                                onCloseScene(false);
                            }
                            Toast.makeText(activity, error.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            ex.printStackTrace();
                            long time = stopWatch.stop().elapsed(TimeUnit.MILLISECONDS);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    onCloseScene(false);
                                }
                            }, 950 - time);
                            Toast.makeText(activity, ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                return true;
            }

            @Override
            public boolean onRightButtonClick() {
                scene_user_existing_username.setText("");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onCloseScene(false);
                    }
                }, 950);
                return true;
            }

            @Override
            public boolean onMiddleButtonClick() {
                return false;
            }

        }
    }

    private class UserSetupSceneCapture extends SceneLayout.FragmentSceneActor<UserSetupSceneController, CaptureFragment> {

        UserSetupSceneCapture() {
            super(UserSetupSceneController.this, R.id.actor_capture, CaptureFragment.class, SceneLayout.FragmentActor.TRANSITION_BOTTOM_UP);
        }

    }

    private class UserSetupEnroll extends SceneLayout.FragmentSceneActor<UserSetupSceneController, EnrollFragment> {

        private QuestionSet mEnrollQuestions;
        private QuestionSet mVerifyQuestions;
        private static final int MAX_SECURITY_NUMBERS = 4;

        UserSetupEnroll() {
            super(UserSetupSceneController.this, R.id.actor_enroll, EnrollFragment.class, SceneLayout.FragmentActor.TRANSITION_BOTTOM_UP);

            QuestionSet.IQuestion requiredFullName = new QuestionSet.QuestionBuilder()
                    .setQuestion(getResources().getString(R.string.scene_sve_full_name))
                    .setMaxSpeechLength(8000)
                    .setSpeechTimeout(1500)
                    .setRequired(true)
                    .build();

            QuestionSet.IQuestion requiredRandomNumber = new QuestionSet.SecurityNumberQuestionBuilder()
                    .setName(getResources().getString(R.string.scene_sve_challenge_name))
                    .setGrammar(getResources().getString(R.string.scene_sve_challenge_grammar))
                    .setMax(MAX_SECURITY_NUMBERS)
                    .setMaxSpeechLength(4000)
                    .setSpeechTimeout(1500)
                    .build();

            QuestionSet.IQuestion requiredNumbers0Through9 = new QuestionSet.QuestionBuilder()
                    .setQuestion(getResources().getString(R.string.scene_sve_numbers))
                    .setMaxSpeechLength(10000)
                    .setSpeechTimeout(1500)
                    .setRequired(true)
                    .build();

            QuestionSet.IQuestion optionalPhoneNumber = new QuestionSet.QuestionBuilder()
                    .setQuestion(getResources().getString(R.string.scene_sve_phone_number))
                    .setMaxSpeechLength(8000)
                    .setSpeechTimeout(1500)
                    .build();

            QuestionSet.IQuestion optionalFullAddress = new QuestionSet.QuestionBuilder()
                    .setQuestion(getResources().getString(R.string.scene_sve_full_address))
                    .setMaxSpeechLength(10000)
                    .setSpeechTimeout(1500)
                    .build();

            QuestionSet.IQuestion optionalNumbers0Through9 = new QuestionSet.QuestionBuilder()
                    .setQuestion(getResources().getString(R.string.scene_sve_numbers))
                    .setMaxSpeechLength(10000)
                    .setSpeechTimeout(1500)
                    .build();

            QuestionSet.IQuestion optionalRandomNumber = new QuestionSet.SecurityNumberQuestionBuilder()
                    .setName(getResources().getString(R.string.scene_sve_challenge_name))
                    .setGrammar(getResources().getString(R.string.scene_sve_challenge_grammar))
                    .setMaxSpeechLength(4000)
                    .setMax(MAX_SECURITY_NUMBERS)
                    .setSpeechTimeout(1500)
                    .build();

            mEnrollQuestions = new QuestionSet();
            mEnrollQuestions.addQuestion(requiredFullName);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);

            mEnrollQuestions.addQuestion(requiredNumbers0Through9);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredFullName);

            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredNumbers0Through9);

            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredFullName);
            mEnrollQuestions.addQuestion(requiredNumbers0Through9);

            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredNumbers0Through9);

            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);

            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);

            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);

            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);

            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);
            mEnrollQuestions.addQuestion(requiredRandomNumber);


            mVerifyQuestions = new QuestionSet();
            mVerifyQuestions.addQuestion(requiredRandomNumber);
            mVerifyQuestions.addQuestion(requiredRandomNumber);
            mVerifyQuestions.addQuestion(requiredRandomNumber);

        }

        @Override
        public void onAttachingFragment(EnrollFragment fragment) {
            super.onAttachingFragment(fragment);
            fragment.setQuestions(mEnrollQuestions, mVerifyQuestions);
        }

    }

    public static class Factory implements ISceneControllerFactory {

        @Override
        public SceneController CreateSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout) {
            return new UserSetupSceneController(sceneDirector, sceneLayout);
        }

    }

}
