package com.validvoice.voxidem.scenes.settings;

import com.validvoice.dynamic.scene.ISceneControllerFactory;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDirector;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.scenes.subs.sve.EnrollFragment;
import com.validvoice.voxidem.scenes.subs.sve.QuestionSet;

public class SettingsSceneController extends SceneController {

    private SettingsSceneController(SceneDirector sceneManager, SceneLayout sceneLayout) {
        super(R.id.scene_settings, 1000, sceneManager, sceneLayout);
        addActor(new SettingsActor());
        addActor(new SettingsActorReEnroll());
        addActor(new SettingsActorCalibrate());
    }

    private class SettingsActor extends SceneLayout.FragmentHomeActor<SettingsSceneController, SettingsFragment> {

        SettingsActor() {
            super(SettingsSceneController.this, R.id.actor_home, SettingsFragment.class);
        }

        @Override
        public boolean onActorAction(int id, Object data) {
            return false;
        }
    }

    private class SettingsActorReEnroll extends SceneLayout.FragmentSceneActor<SettingsSceneController, EnrollFragment> {

        private QuestionSet mEnrollQuestions;
        private QuestionSet mVerifyQuestions;
        private static final int MAX_SECURITY_NUMBERS = 4;


        SettingsActorReEnroll() {
            super(SettingsSceneController.this, R.id.actor_enroll, EnrollFragment.class, SceneLayout.FragmentActor.TRANSITION_LEFT_TO_RIGHT);

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

    private class SettingsActorCalibrate extends SceneLayout.FragmentSceneActor<SettingsSceneController, SettingsCalibrateFragment> {

        SettingsActorCalibrate() {
            super(SettingsSceneController.this, R.id.actor_calibrate, SettingsCalibrateFragment.class, SceneLayout.FragmentActor.TRANSITION_LEFT_TO_RIGHT);
        }
    }

    public static class Factory implements ISceneControllerFactory {

        @Override
        public SceneController CreateSceneController(SceneDirector sceneDirector, SceneLayout sceneLayout) {
            return new SettingsSceneController(sceneDirector, sceneLayout);
        }

    }
}
