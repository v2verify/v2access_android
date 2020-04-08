package com.validvoice.voxidem.scenes.home;

import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Stopwatch;
import com.steelkiwi.library.ui.SeparateShapesView;
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
import com.validvoice.voxidem.scenes.subs.capture.CaptureFragment;
import com.validvoice.voxidem.scenes.devices.DevicesFragment;
import com.validvoice.voxidem.scenes.subs.sve.EnrollFragment;
import com.validvoice.voxidem.scenes.subs.sve.VerifyFragment;
import com.validvoice.voxidem.scenes.subs.sve.QuestionSet;
import com.validvoice.voxidem.scenes.subs.sve.QuestionSetList;
import com.validvoice.voxidem.scenes.user_setup.UserSetupSceneController;

import java.util.concurrent.TimeUnit;

public class HomeSceneController extends SceneController {

    private HomeSceneController(SceneDirector sceneManager, SceneLayout sceneLayout) {
        super(R.id.scene_home, 100, sceneManager, sceneLayout);
        addActor(new HomeSceneActor());
        addActor(new HomeSceneCapture());
        addActor(new HomeSceneVerify());
        addActor(new HomeSceneAddDevice());
        addActor(new HomeSceneEnroll());
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
                case R.id.actor_add: {
                    dispatchAction(getActiveActorId(), R.id.action_cancel, null);
                } break;
            }
        }
        if(getActiveActorId() != getHomeActorId()) {
            dispatchCollapse(getActiveActorId());
        }
        getActivity().invalidateOptionsMenu();
    }

    private class HomeSceneActor extends SceneLayout.FragmentHomeActor<HomeSceneController, HomeFragment> {
        HomeSceneActor() {
            super(HomeSceneController.this, R.id.actor_home, HomeFragment.class);
        }
    }

    private class HomeSceneCapture extends SceneLayout.FragmentSceneActor<HomeSceneController, CaptureFragment> {
        HomeSceneCapture() {
            super(HomeSceneController.this, R.id.actor_capture, CaptureFragment.class, SceneLayout.FragmentActor.TRANSITION_BOTTOM_UP);
        }
    }

    private class HomeSceneVerify extends SceneLayout.FragmentSceneActor<HomeSceneController, VerifyFragment> {

        private static final int MAX_SECURITY_NUMBERS = 4;
        private QuestionSetList mQuestionSets;

        HomeSceneVerify() {
            super(HomeSceneController.this, R.id.actor_verify, VerifyFragment.class, SceneLayout.FragmentActor.TRANSITION_BOTTOM_UP);

            mQuestionSets = new QuestionSetList();

            // Initial Questions
            mQuestionSets.addQuestionSet(new QuestionSet()
                    .addQuestion(new QuestionSet.SecurityNumberQuestionBuilder()
                            .setName(getResources().getString(R.string.scene_sve_challenge_name))
                            .setGrammar(getResources().getString(R.string.scene_sve_challenge_grammar_initial))
                            .setMax(MAX_SECURITY_NUMBERS)
                            .setMaxSpeechLength(4000)
                            .setSpeechTimeout(1000)
                            .build())
                    .addQuestion(new QuestionSet.SecurityNumberQuestionBuilder()
                            .setName(getResources().getString(R.string.scene_sve_challenge_name))
                            .setGrammar(getResources().getString(R.string.scene_sve_challenge_grammar_initial))
                            .setMax(MAX_SECURITY_NUMBERS)
                            .setMaxSpeechLength(4000)
                            .setSpeechTimeout(1000)
                            .build())
            );

            mQuestionSets.addQuestionSet(new QuestionSet()
                    .addQuestion(new QuestionSet.SecurityNumberQuestionBuilder()
                            .setName(getResources().getString(R.string.scene_sve_challenge_name))
                            .setGrammar(getResources().getString(R.string.scene_sve_challenge_grammar_initial))
                            .setMax(MAX_SECURITY_NUMBERS)
                            .setMaxSpeechLength(4000)
                            .setSpeechTimeout(1000)
                            .build())
            );

            mQuestionSets.addQuestionSet(new QuestionSet()
                    .addQuestion(new QuestionSet.SecurityNumberQuestionBuilder()
                            .setName(getResources().getString(R.string.scene_sve_challenge_name))
                            .setGrammar(getResources().getString(R.string.scene_sve_challenge_grammar_initial))
                            .setMax(MAX_SECURITY_NUMBERS)
                            .setMaxSpeechLength(4000)
                            .setSpeechTimeout(1000)
                            .build())
            );

            mQuestionSets.addQuestionSet(new QuestionSet()
                    .addQuestion(new QuestionSet.SecurityNumberQuestionBuilder()
                            .setName(getResources().getString(R.string.scene_sve_challenge_name))
                            .setGrammar(getResources().getString(R.string.scene_sve_challenge_grammar_initial))
                            .setMax(MAX_SECURITY_NUMBERS)
                            .setMaxSpeechLength(4000)
                            .setSpeechTimeout(1000)
                            .build())
            );

            mQuestionSets.addQuestionSet(new QuestionSet()
                    .addQuestion(new QuestionSet.SecurityNumberQuestionBuilder()
                            .setName(getResources().getString(R.string.scene_sve_challenge_name))
                            .setGrammar(getResources().getString(R.string.scene_sve_challenge_grammar))
                            .setMax(MAX_SECURITY_NUMBERS)
                            .setMaxSpeechLength(4000)
                            .setSpeechTimeout(1000)
                            .build())
            );
        }

        @Override
        public void onAttachingFragment(VerifyFragment fragment) {
            super.onAttachingFragment(fragment);
            fragment.setQuestions(mQuestionSets);
        }
    }


    private class HomeSceneEnroll extends SceneLayout.FragmentSceneActor<HomeSceneController, EnrollFragment> {

        private QuestionSet mEnrollQuestions;
        private QuestionSet mVerifyQuestions;
        private static final int MAX_SECURITY_NUMBERS = 4;

        HomeSceneEnroll() {
            super(HomeSceneController.this, R.id.actor_enroll, EnrollFragment.class, SceneLayout.FragmentActor.TRANSITION_BOTTOM_UP);

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
    private class HomeSceneAddDevice extends SceneLayout.SceneActorHeader {

        private TextView device_add_label;
        private TextInputLayout scene_home_device_add_nickname_layout;
        private TextInputEditText scene_home_device_add_nickname;
        private SeparateShapesView scene_home_device_add_button;

        HomeSceneAddDevice() {
            super(HomeSceneController.this, R.id.actor_add, SCRIM_ON_LOCKED);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup parent, boolean addToRoot) {
            View view = inflater.inflate(R.layout.scene_home_device_add, parent, addToRoot);
            device_add_label = view.findViewById(R.id.device_add_label);
            scene_home_device_add_nickname_layout = view.findViewById(R.id.scene_home_device_add_nickname_layout);
            scene_home_device_add_nickname = view.findViewById(R.id.scene_home_device_add_nickname);
            scene_home_device_add_button = view.findViewById(R.id.scene_home_device_add_button);
            scene_home_device_add_button.setOnButtonClickListener(new SeparateShapesViewClickListener());
            scene_home_device_add_button.restore();
            return view;
        }

        @Override
        public void onDestroyView(View view) {
            scene_home_device_add_button.restore();
            scene_home_device_add_nickname_layout = null;
            scene_home_device_add_nickname = null;
            scene_home_device_add_button = null;
        }

        @Override
        public void onActorOpening() {
            super.onActorOpening();
            final String device_info = (String) getSceneDirector().getData(DevicesFragment.DEVICE_INFO);
            device_add_label.setText(String.format(
                        getResources().getString(R.string.scene_verify_add_new_device),
                        device_info
                    ));
            scene_home_device_add_nickname.setText("");
            scene_home_device_add_button.restore();
        }

        @Override
        public void onActorOpened() {
            scene_home_device_add_nickname.requestFocus();
        }

        ///
        /// SeparateShapesViewClickListener Class
        ///

        private class SeparateShapesViewClickListener implements SeparateShapesView.OnButtonClickListener {

            @Override
            public boolean onLeftButtonClick() {
                scene_home_device_add_nickname.setText("");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onCloseScene(false);
                    }
                }, 950);
                return true;
            }

            @Override
            public boolean onRightButtonClick() {final String nickname = scene_home_device_add_nickname.getText().toString();

                if(nickname.isEmpty()) {
                    scene_home_device_add_nickname_layout.setError(getResources().getString(R.string.scene_home_device_add_nickname_error));
                    return true;
                } else if(getContractController().contains(DevicesContract.CONTENT_URI, DevicesContract.DEVICE_NICKNAME, nickname)) {
                    scene_home_device_add_nickname_layout.setError(getResources().getString(R.string.scene_home_device_add_nickname_exists_error));
                    return true;
                } else {
                    scene_home_device_add_nickname_layout.setErrorEnabled(false);
                }

                hideSoftKeyboardFromWindow();

                final Stopwatch stopWatch = Stopwatch.createStarted();
                final String deviceId = (String) getSceneDirector().getData(DevicesFragment.DEVICE_ID);
                CloudMessage message = CloudMessage.Create("v2access.Devices.{@v2w_user_name}");
                message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
                message.putString("v2w_device_id", deviceId);
                message.putString("v2w_nick_name", nickname);
                message.send(getActivity(), new CloudController.ResponseOnUiCallback() {
                    @Override
                    public void onResult(CloudResult result) {

                        long time = stopWatch.stop().elapsed(TimeUnit.MILLISECONDS);

                        final ContractController cc = getContractController();
                        final String deviceType = (String) getSceneDirector().getData(DevicesFragment.DEVICE_INFO);
                        final String deviceIp = (String) getSceneDirector().getData(DevicesFragment.DEVICE_IP);
                        long device_id = cc.insertModel(DevicesContract.CONTENT_URI,
                                DeviceModel.createRecord(deviceId, deviceType, nickname, deviceIp));
                        cc.insertModel(HistoryContract.CONTENT_URI,
                                HistoryModel.createDeviceRecord(
                                        device_id,
                                        VoxidemPreferences.getUserAccountName(),
                                        nickname
                                ));

                        Toast.makeText(getActivity(), "Device: " + nickname + ", saved!", Toast.LENGTH_SHORT).show();

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
                        }
                        Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Exception ex) {
                        ex.printStackTrace();
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
                        Toast.makeText(getActivity(), ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                return true;
            }

            @Override
            public boolean onMiddleButtonClick() {
                return false;
            }

        }

    }

    public static class Factory implements ISceneControllerFactory {

        @Override
        public SceneController CreateSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout) {
            return new HomeSceneController(sceneDirector, sceneLayout);
        }

    }

}
