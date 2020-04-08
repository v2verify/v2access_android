package com.validvoice.voxidem.scenes.subs.sve;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Stopwatch;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.scene.IScenePermissionListener;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.dynamic.speech.authorization.SpeechContent;
import com.validvoice.dynamic.speech.authorization.SveEnroller;
import com.validvoice.dynamic.speech.authorization.SveVerifier;
import com.validvoice.dynamic.audio.AudioBuffer;
import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContexts;
import com.validvoice.dynamic.speech.service.AdaptiveSpeechServiceConnection;
import com.validvoice.dynamic.speech.service.SpeechServiceConnection;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.db.contracts.HistoryContract;
import com.validvoice.voxidem.db.models.HistoryModel;
import com.validvoice.voxidem.scenes.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class EnrollFragment extends SceneFragment implements IScenePermissionListener,
        SpeechServiceConnection.SpeechServiceListener ,View.OnClickListener {

    private static final String TAG = "EnrollFragment";

    public static final String QR_INTENT_ID = "enroll_qr_intent_id";

    // Amount of times a verify is allowed to validate the enrollment
    private static final int MAX_VERIFY_ATTEMPTS = 3;

    // Audio Tools
    private AudioBuffer mAudioBuffer;
    private SpeechServiceConnection mSpeechConnection;

    // Enroll References
    private SveEnroller mEnroller;
    private String mEnrollSessionId;
    private Boolean mIsEnrollCanceled;
    private QuestionSet mEnrollQuestionSet;

    // Verify References
    private SveVerifier mVerifier;
    private String mVerifySessionId;
    private int mCurrentVerifyAttempt;
    private Boolean mIsVerifyCanceled;
    private QuestionSet mVerifyQuestionSet;
    private MediaPlayer mMediaPlayer;
    private String mChosenNumbers;
    private SpeechContexts mSpeechContexts;
    private SpeechContent mAllContent;



    //private String mQrIntentId;

    // UI References
    private Handler mUiHandler;

    // View References
    private TextView scene_enroll_listening;
    private TextView scene_enroll_training;
    private TextView scene_enroll_instructions;
    private TextView scene_enroll_text_label;
    private LinearLayout scene_enroll_group;
    private LinearLayout scene_enroll_button_insturctions;
    private LinearLayout scene_enroll_button_frame;
    private Button scene_enroll_button;
    private Button scene_enroll_button_ins_yes;
    private ImageView microphoneView;

    private ProgressBar scene_enroll_train_progress;
    private ProgressBar enrollment_question_progress;

    // View Adapters
    private EnrollViewGroupAdapter mEnrollViewGroupAdapter;
    private VerifyViewGroupAdapter mVerifyViewGroupAdapter;

    // Resource caches
    private int mColorListening;
    private int mColorNotListening;
    Drawable success;
    Drawable microphone;
    private BottomNavigationView mBottomNavigationView;

    //QuestionTone
    private boolean mHasQuestionTone = false;
    private boolean mSpeechToText = false;
    private boolean mAnimation = false;
    private boolean enrollmentInstructionQuestion = true;
    private TextToSpeech textToSpeech;
    private int animationDelayDuration = 0;

    private int trainingQuestionCount = 10;
    private int passTrainingCount = 0;

    // States
    private enum State {
        None,
        EnrollInstructions,
        EnrollQuestions,
        EnrollTraining,
        VerifyInstructions,
        VerifyQuestions,
        GatherDone,
        GatherFailure,
        PermissionError,
        TrainingVerifications
    }

    private State mState = State.None;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final Resources resources = getResources();
        final Activity activity = getActivity();
        if(activity != null) {
            final Resources.Theme theme = activity.getTheme();
            mColorListening = ResourcesCompat.getColor(resources, R.color.colorStatusListening, theme);
            mColorNotListening = ResourcesCompat.getColor(resources, R.color.colorStatusNotListening, theme);
            success = ResourcesCompat.getDrawable(resources, R.drawable.animated_check, theme);
            microphone = ResourcesCompat.getDrawable(resources, R.drawable.ic_microphone_green, theme);
        }

        mAudioBuffer = new AudioBuffer();
        mUiHandler = new Handler();
        mSpeechConnection = new SpeechServiceConnection(this);
        mCurrentVerifyAttempt = 0;

        View view = inflater.inflate(R.layout.fragment_enroll, container, false);

        ((MainActivity) getActivity()).spanTitleBar("Enrollment");


        scene_enroll_listening = view.findViewById(R.id.scene_enroll_listening);
        scene_enroll_training = view.findViewById(R.id.scene_enroll_training);
        scene_enroll_instructions = view.findViewById(R.id.scene_enroll_instructions);
        scene_enroll_group = view.findViewById(R.id.scene_enroll_group);
        scene_enroll_button_frame = view.findViewById(R.id.scene_enroll_button_frame);
        scene_enroll_button = view.findViewById(R.id.scene_enroll_button);
        scene_enroll_train_progress = view.findViewById(R.id.scene_enroll_train_progress);
        scene_enroll_text_label = view.findViewById(R.id.enroll_text_label);
        microphoneView =  view.findViewById(R.id.scene_enroll_microphone_image);
        mBottomNavigationView = getActivity().findViewById(R.id.botton_nav);
        scene_enroll_button_insturctions = view.findViewById(R.id.scene_enroll_button_insturctions);
        scene_enroll_button_ins_yes = view.findViewById(R.id.scene_enroll_button_ins_yes);
        scene_enroll_text_label.setText(R.string.scene_enroll_enroll_by_voice);
        scene_enroll_button_ins_yes.setOnClickListener(this);
        enrollment_question_progress = view.findViewById(R.id.enrollment_question_progress);


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mHasQuestionTone = sp.getBoolean(SettingsFragment.PREF_GENERAL_QUESTION_TONE, false);
        mSpeechToText = sp.getBoolean(SettingsFragment.PREF_GENERAL_SPEECH_TO_TEXT, false);
        mAnimation = sp.getBoolean(SceneLayout.PREF_ALLOW_THEME_ANIMATIONS, false);


        textToSpeech = new TextToSpeech(activity, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == textToSpeech.SUCCESS){
                    textToSpeech.setLanguage(Locale.US);
                    textToSpeech.setSpeechRate(1.0f);
                    textToSpeech.setPitch(1.0f);
                }
            }
        });

        enrollment_question_progress.setScaleY(10f);
        enrollment_question_progress.setScaleX(1.5f);

//        final SharedPreferences.Editor editor = sp.edit();
//        editor.putBoolean(AdaptiveSpeechServiceConnection.PREF_ADAPTIVE_MICROPHONE, false).apply();



        //getSpeechRequired
        scene_enroll_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Button:onClickListener: " + mState.name());
                switch(mState) {
                    case EnrollInstructions:
                        mEnrollQuestionSet.reset();
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scene_enroll_button_frame.setVisibility(View.GONE);
                                scene_enroll_instructions.setVisibility(View.GONE);
                            }
                        });
                        showListeningStatus(true);
                        showNextEnrollQuestion(false);
                        break;
                    case VerifyInstructions:
                        mVerifyQuestionSet.reset();
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scene_enroll_button_frame.setVisibility(View.GONE);
                                scene_enroll_instructions.setVisibility(View.GONE);
                            }
                        });
                        showListeningStatus(true);
                        showNextVerifyQuestion();
                        break;
                    case PermissionError:
                    case GatherDone:
                        final Activity activity = getActivity();
                        if(activity != null) {
                            startActivity(new Intent(activity, MainActivity.class));
                            activity.finish();
                        }
                        break;
                    case TrainingVerifications:
                        if (passTrainingCount == 3){
                            if(getActivity() != null) {
                                startActivity(new Intent(getActivity(), MainActivity.class));
                                getActivity().finish();
                            }
                    }else {
                            mVerifyQuestionSet.reset();
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    scene_enroll_button_frame.setVisibility(View.GONE);
                                    scene_enroll_instructions.setVisibility(View.GONE);
                                }
                            });
                            showListeningStatus(true);
                            showNextVerifyQuestion();
                        }
                        break;
                    case GatherFailure:
                        if(mCurrentVerifyAttempt >= 1 && mCurrentVerifyAttempt <= MAX_VERIFY_ATTEMPTS) {
                            callVerifySetupScene();
                        } else {
                            callSetupScene();
                            callEnrollStart(RECOVER_FROM_START);
                        }
                        break;
                }
            }
        });

        callSetupScene();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        assertNotNull(mEnrollQuestionSet);
        assertNotNull(mVerifyQuestionSet);

        mEnrollViewGroupAdapter = new EnrollViewGroupAdapter(
                getSceneController(),
                scene_enroll_group
        );
        mEnrollViewGroupAdapter.setLimitations(true);

        mVerifyViewGroupAdapter = new VerifyViewGroupAdapter(
                getSceneController(),
                scene_enroll_group
        );
        mVerifyViewGroupAdapter.setLimitations(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        View view = getView();
        mBottomNavigationView.setVisibility(View.VISIBLE);

        if(view != null) {
            ViewGroup viewGroup = (ViewGroup)view.getParent();
            if(viewGroup != null) {
                viewGroup.removeAllViews();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().findViewById(R.id.botton_nav).setVisibility(View.GONE);
        mBottomNavigationView.setVisibility(View.INVISIBLE);
        requestPermission(1, Manifest.permission.RECORD_AUDIO, this);

        dispatchBypassAction(R.id.actor_main, R.id.action_stop_calibrating, null);
        dispatchBypassAction(R.id.actor_main, R.id.action_screen_keep_on, null);
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        trainingQuestionCount = 10;
        passTrainingCount = 0;
    }

    @Override
    public void onStop() {
        showListeningStatus(false);

        Context context = getContext();
        assertNotNull(context);

        mSpeechConnection.unbind(context);
        if(mEnroller != null && mEnroller.isSessionOpen()) {
            callEnrollCancel("Stopping Enrollment");
        }
        if(mVerifier != null && mVerifier.isSessionOpen()) {
            callVerifyCancel("Stopping Verification");
        }
        if (mAllContent != null) {
            mAllContent.clear();
        }

        dispatchBypassAction(R.id.actor_main, R.id.action_screen_clear_on, null);
        dispatchBypassAction(R.id.actor_main, R.id.action_start_calibrating, null);
        super.onStop();
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.scene_enroll_button_ins_yes:
                 if (scene_enroll_instructions.getText().toString().equalsIgnoreCase(getResources().getString(R.string.scene_enrollment_instruction_ques1))){
                     scene_enroll_instructions.setText(R.string.scene_enrollment_instruction_ques2);
                 }else if (scene_enroll_instructions.getText().toString().equalsIgnoreCase(getResources().getString(R.string.scene_enrollment_instruction_ques2))){
                    scene_enroll_instructions.setText(R.string.scene_enrollment_instruction_ques3);
                 }else{
                     showEnrollInstructions();
                 }
                break;
            default:
                break;
        }
    }

    @Override
    public void onPermissionGranted(@NonNull String permission) {
        Log.d(TAG, "onPermissionGranted");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            String userAccount = VoxidemPreferences.getUserAccountName();
            String userFirstName = VoxidemPreferences.getUserFirstName();
            String userLastName = VoxidemPreferences.getUserLastName();
            String userPhoneNumber = VoxidemPreferences.getUserPhoneNumber();
            String userVoicePrintId = VoxidemPreferences.getUserVoicePrintId();
            SveEnroller.Gender gender = VoxidemPreferences.getUserGender();
            String trainerPin = VoxidemPreferences.getTrainerPin();


            Context context = getContext();
            if(context != null) {
                mSpeechConnection.bind(context);
            }

            String interactionId = userLastName + ", " +
                    userFirstName + " (" + userPhoneNumber + ")";

            mAllContent = new SpeechContent();

            mEnroller = new SveEnroller()
                    .setClientId(userVoicePrintId)
                    .setGender(gender)
                    .setInteractionId(interactionId)
                    .setInteractionTag(userAccount)
                    .setAuthToken(trainerPin);

            mVerifier = new SveVerifier()
                    .setClientId(userVoicePrintId)
                    .setInteractionId(interactionId)
                    .setInteractionTag(userAccount);
        }
    }

    @Override
    public void onPermissionRationale(@NonNull String permission) {
        Log.d(TAG, "onPermissionRationale");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            makeSnackbar(R.string.audio_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermission(100, Manifest.permission.RECORD_AUDIO,
                                    EnrollFragment.this);
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onPermissionDenied(@NonNull String permission) {
        Log.d(TAG, "onPermissionDenied");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            makeSnackbar(R.string.no_record_audio_permission, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Activity activity = getActivity();
                            if(activity != null) {
                                activity.finish();
                            }
                        }
                    })
                    .show();
        }
    }

    private void setState(State state) {
        Log.d(TAG, "setState: " + mState.name() + " - changing to - " + state.name());
        mState = state;
    }

    // UI Methods

    private void callSetupScene() {
        setState(State.None);

        mAudioBuffer.clear();
        mIsEnrollCanceled = false;
        mIsVerifyCanceled = false;

        scene_enroll_listening.setVisibility(View.GONE);
        scene_enroll_training.setVisibility(View.GONE);
        scene_enroll_instructions.setVisibility(View.GONE);
        scene_enroll_group.setVisibility(View.GONE);
        scene_enroll_button_frame.setVisibility(View.GONE);
        scene_enroll_train_progress.setVisibility(View.GONE);

        if(mEnrollViewGroupAdapter != null) {
            mEnrollViewGroupAdapter.clear();
        }
        if(mVerifyViewGroupAdapter != null) {
            mVerifyViewGroupAdapter.clear();
        }

        if (mEnrollQuestionSet != null) {
            mEnrollQuestionSet.reset();
        }
        if (mVerifyQuestionSet != null) {
            mVerifyQuestionSet.reset();
        }

        //showEnrollInstructions();
        showEnrollInstructionsQuestions();
    }

    private void showListeningStatus(final boolean isShowing) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_listening.setText("Listening");
                scene_enroll_listening.setVisibility(isShowing ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void changeListeningStatus(final boolean isListening) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(isListening){
                    screenAnimation();
                }
                scene_enroll_listening.setText("Listening");
                scene_enroll_listening.setTextColor(isListening ? mColorListening : mColorNotListening);
            }
        });
    }

    private void showButton(long delayMillis) {
        final State state = mState;
        final Runnable runner = new Runnable() {
            @Override
            public void run() {
                switch (state) {
                    case EnrollInstructions:
                        scene_enroll_button.setText(R.string.scene_enroll_begin);
                        break;
                    case VerifyInstructions:
                        scene_enroll_training.setText(R.string.scene_enroll_trained);
                        scene_enroll_train_progress.setVisibility(View.GONE);
                        scene_enroll_button.setText(R.string.scene_enroll_continue);
                        break;
                    case TrainingVerifications:
                        scene_enroll_training.setText(R.string.scene_enroll_trained);
                        scene_enroll_train_progress.setVisibility(View.GONE);
                        scene_enroll_button.setText(R.string.scene_enroll_continue);
                        break;
                    case PermissionError:
                        scene_enroll_listening.setVisibility(View.GONE);
                        scene_enroll_group.setVisibility(View.GONE);
                        scene_enroll_training.setVisibility(View.GONE);
                        scene_enroll_instructions.setText(R.string.audio_permission_rationale);
                        scene_enroll_instructions.setVisibility(View.VISIBLE);
                        scene_enroll_button.setText(R.string.scene_enroll_done);
                        break;
                    case GatherDone:
                        scene_enroll_listening.setVisibility(View.GONE);
                        scene_enroll_group.setVisibility(View.GONE);
                        scene_enroll_training.setText(R.string.scene_enroll_success);
                        scene_enroll_training.setVisibility(View.VISIBLE);
                        scene_enroll_instructions.setVisibility(View.GONE);
                        scene_enroll_button.setText(R.string.scene_enroll_done);
                        scene_enroll_text_label.setText(R.string.scene_enroll_enrolled_by_voice);
                        microphoneView.setImageDrawable(success);
                        ((Animatable) microphoneView.getDrawable()).start();
                        scene_enroll_text_label.setText(R.string.scene_verify_verified_by_voice);


                        break;
                    case GatherFailure:
                        scene_enroll_button.setText(R.string.scene_enroll_try_again);
                        break;
                    default: return;
                }
                scene_enroll_button_frame.setVisibility(View.VISIBLE);
            }
        };
        if(delayMillis <= 0) {
            mUiHandler.post(runner);
        } else {
            mUiHandler.postDelayed(runner, delayMillis);
        }
    }

    // Enroll UI

    private void showEnrollInstructions() {
        setState(State.EnrollInstructions);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_listening.setVisibility(View.GONE);
                scene_enroll_button.setVisibility(View.VISIBLE);
                scene_enroll_button_insturctions.setVisibility(View.GONE);
                scene_enroll_instructions.setText(R.string.scene_enroll_instructions_strict_form);
                scene_enroll_instructions.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showEnrollInstructionsQuestions() {
        setState(State.EnrollInstructions);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_button.setVisibility(View.GONE);
                scene_enroll_button_insturctions.setVisibility(View.VISIBLE);
                scene_enroll_listening.setVisibility(View.GONE);
                scene_enroll_instructions.setText(R.string.scene_enrollment_instruction_ques1);
                scene_enroll_instructions.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean showNextEnrollQuestion(boolean onlyRequiredQuestions) {
        mAllContent.clear();
        QuestionSet.Mode mode = onlyRequiredQuestions ?
                QuestionSet.Mode.RequiredOnly :
                QuestionSet.Mode.Question;
        if (mIsEnrollCanceled) {
            return true;
        } else if(mEnrollQuestionSet.nextQuestion(mode)) {
            final QuestionSet.IQuestion q = mEnrollQuestionSet.getQuestion();
            mSpeechConnection.setSpeechMaxLengthMillis(q.maxSpeechLengthMillis());
            mSpeechConnection.setSpeechTimeoutMillis(q.speechTimeoutMillis());
            setState(State.EnrollQuestions);
            final String question = q.getQuestion();
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEnrollViewGroupAdapter.clear();
                }
            });
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scene_enroll_group.setVisibility(View.VISIBLE);
                    mEnrollViewGroupAdapter.add(new EnrollData(question));
                }
            },700);

            if(mSpeechToText){
                mSpeechConnection.pause();
                animationDelayDuration = mHasQuestionTone?1700+1000:1900;
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String speechString = q.getQuestion().contains("from")? "Count "+question : "Say " + question;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak(speechString,TextToSpeech.QUEUE_FLUSH,null,null);
                        } else {
                            textToSpeech.speak(speechString, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });
            }
         //   handleQuestionTone();

            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSpeechConnection.resume();
                }
            },animationDelayDuration);
            return true;
        }
        return false;
    }

    private void showTraining(final int recoverOption) {
        mAllContent.clear();
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_listening.setText("Listening");
                scene_enroll_train_progress.setVisibility(View.VISIBLE);

            }
        });
        if(mState != State.EnrollTraining) {
            setState(State.EnrollTraining);
            mEnroller.end(new SveEnroller.EndCallback() {
                @Override
                public void onEndComplete(SveEnroller.Result result) {
                    logEnrollResult();
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            scene_enroll_train_progress.setVisibility(View.GONE);
                        }
                    });
                    if(result == SveEnroller.Result.Success) {
                        callVerifySetupScene();
                    } else {
                        mEnrollSessionId = "";
                        showGatherFailure(R.string.scene_enroll_instructions_server_failure, result.name());
                        showButton(0);
                    }
                }

                @Override
                public void onFailure(Exception ex) {
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            scene_enroll_train_progress.setVisibility(View.GONE);

                        }
                    });
                    if (RECOVER_FROM_END == recoverOption) {
                        ex.printStackTrace();
                        callEnrollStart(RECOVER_FROM_END);
                        return;
                    }
                    mEnrollSessionId = "";
                    showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
                    logEnrollResult();
                    showButton(0);
                }
            });

            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    scene_enroll_listening.setVisibility(View.GONE);
                    scene_enroll_group.setVisibility(View.GONE);
                    scene_enroll_training.setVisibility(View.VISIBLE);
                    scene_enroll_train_progress.setVisibility(View.VISIBLE);
                    mEnrollViewGroupAdapter.clear();
                }
            });
        }
    }

    // Verify UI

    private void callVerifySetupScene() {
        mAllContent.clear();

        mAudioBuffer.clear();
        mIsEnrollCanceled = false;
        mIsVerifyCanceled = false;

        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_listening.setVisibility(View.GONE);
                scene_enroll_training.setVisibility(View.GONE);
                scene_enroll_instructions.setVisibility(View.GONE);
                scene_enroll_group.setVisibility(View.GONE);
                scene_enroll_button_frame.setVisibility(View.GONE);
                scene_enroll_train_progress.setVisibility(View.GONE);
            }
        });

        showVerifyInstructions();
    }

    private void showVerifyInstructions() {
        mAllContent.clear();
        setState(State.VerifyInstructions);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_listening.setVisibility(View.GONE);
                scene_enroll_group.setVisibility(View.GONE);
                scene_enroll_training.setVisibility(View.GONE);
                scene_enroll_train_progress.setVisibility(View.GONE);

                scene_enroll_instructions.setText(R.string.scene_enroll_instructions_confirm_enroll);
                scene_enroll_instructions.setVisibility(View.VISIBLE);
            }
        });
        callVerifyStart(RECOVER_FROM_START);
    }


    private boolean showNextVerifyQuestion() {
        mAllContent.clear();
        if (mIsVerifyCanceled) {
            return true;
        } else if(mVerifyQuestionSet.nextQuestion()){
            final QuestionSet.IQuestion q = mVerifyQuestionSet.getQuestion();
            final String question = q.getQuestion();
            mSpeechConnection.setSpeechMaxLengthMillis(q.maxSpeechLengthMillis());
            mSpeechConnection.setSpeechTimeoutMillis(q.speechTimeoutMillis());
            setState(State.VerifyQuestions);
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVerifyViewGroupAdapter.clear();
                }
            });
            mUiHandler.postDelayed(new Runnable(){
                @Override
                public void run() {
                    scene_enroll_group.setVisibility(View.VISIBLE);
                    mVerifyViewGroupAdapter.add(new VerifyData(question));
                }
            },500);

            if(mSpeechToText){
                mSpeechConnection.pause();
                animationDelayDuration = mHasQuestionTone?1700+1000:1700;
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String speechString = "Say " + question;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak(speechString,TextToSpeech.QUEUE_FLUSH,null,null);
                        } else {
                            textToSpeech.speak(speechString, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });
            }
            handleQuestionTone();
            if(q.questionType() == QuestionSet.Type.Challenge){
                mChosenNumbers = question;
                mSpeechContexts = new SpeechContexts();
                List<String> phrases = new ArrayList<>();
                phrases.add(mChosenNumbers.trim());
                mSpeechContexts.add(
                        q.getName(), q.getGrammar(),
                        "en-US",
                        phrases
                );

            }
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mSpeechConnection.resume();
                }
            },animationDelayDuration);
            return true;
        }
        return false;
    }



    private void showTrainingInstructions() {
        setState(State.TrainingVerifications);
        trainingQuestionCount -= 1;
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_listening.setText("Listening");
                scene_enroll_listening.setVisibility(View.GONE);
                scene_enroll_group.setVisibility(View.GONE);
                scene_enroll_training.setVisibility(View.GONE);
                scene_enroll_train_progress.setVisibility(View.GONE);
                if (trainingQuestionCount != 10){
                    scene_enroll_instructions.setText(R.string.scene_enroll_instructions_comolete_training);
                }else{
                    scene_enroll_instructions.setText(R.string.scene_enroll_instructions_confirm_training);
                }
                scene_enroll_instructions.setVisibility(View.VISIBLE);
                scene_enroll_button.setVisibility(View.VISIBLE);
            }
        });
        callVerifyStart(RECOVER_FROM_START);
    }

    // Misc UI

    private void showGatherFailure(final int instruction_id, final String message) {
        setState(State.GatherFailure);
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_listening.setVisibility(View.GONE);
                scene_enroll_group.setVisibility(View.GONE);
                scene_enroll_listening.setText("Listening");
                scene_enroll_instructions.setText(String.format(
                        getResources().getString(instruction_id),
                        message
                ));
                scene_enroll_train_progress.setVisibility(View.GONE);
                scene_enroll_instructions.setVisibility(View.VISIBLE);
            }
        });
        showButton(1000);
        callEnrollCancel(message);
        callVerifyCancel(message);
    }

//    private void showGatherDone() {
//        setState(State.GatherDone);
//        VoxidemPreferences.setUserEnrolled();
//        showButton(0);
//    }

    // Enroll Methods

    private static int RECOVER_FROM_NONE = 0;
    private static int RECOVER_FROM_START = 1;
    private static int RECOVER_FROM_PROCESS = 2;
    private static int RECOVER_FROM_END = 3;

    private void callEnrollStart(final int recoverOption) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_train_progress.setVisibility(View.VISIBLE);

            }
        });
        screenAnimation();
        final Stopwatch stopWatch = Stopwatch.createStarted();
        mEnroller.setMetaData("Silence-Threshold", mSpeechConnection.getSilenceAmplitudeThreshold());
        mEnroller.start(new SveEnroller.StartCallback() {
            @Override
            public void onStartComplete() {

                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_enroll_train_progress.setVisibility(View.GONE);

                    }
                });
                mEnrollSessionId = mEnroller.getSessionId();
                enrollment_question_progress.setMax((int) mEnroller.getSpeechRequired());

                if (RECOVER_FROM_PROCESS == recoverOption) {
                    callEnrollProcess(null, RECOVER_FROM_NONE);
                } else if (RECOVER_FROM_END == recoverOption) {
                    callEnrollProcess(null, RECOVER_FROM_END);
                } else {
                    if(stopWatch.stop().elapsed(TimeUnit.SECONDS) < 2) {
                        showButton(1000);
                    } else {
                        showButton(0);
                    }
                }

            }

            @Override
            public void onFailure(final Exception ex) {

                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_enroll_train_progress.setVisibility(View.GONE);

                    }
                });
                if (RECOVER_FROM_START == recoverOption) {
                    ex.printStackTrace();
                    callEnrollStart(RECOVER_FROM_NONE);
                    return;
                }
                mEnrollSessionId = "";
                showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                      //  Toast.makeText(getActivity(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        enrollment_question_progress.setVisibility(View.GONE);
                        getSceneDirector().setHomeSceneActive();
                    }
                });
            }
        });
    }

    private void callEnrollCancel(String message) {
        if(!mIsEnrollCanceled && mEnroller.isSessionOpen()) {
            mIsEnrollCanceled = true;
            mEnroller.cancel(message, new SveEnroller.CancelCallback() {
                @Override
                public void onCancelComplete() {
                    mEnrollSessionId = "";
                }

                @Override
                public void onFailure(Exception ex) {
                    mEnrollSessionId = "";
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            enrollment_question_progress.setVisibility(View.GONE);
                        }
                    });                    showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
                }
            });
            logEnrollResult();
        }
    }

    private void callEnrollUpdate(final SveEnroller.Result result) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                int index = mEnrollViewGroupAdapter.getCount() - 1;
                EnrollData data = mEnrollViewGroupAdapter.getItemType(index);
                data.updateResult(result);
                mEnrollViewGroupAdapter.update(index, data);
            }
        });
    }

    // Verify Methods

    private void callVerifyStart(final int recoverOption) {
        final Stopwatch stopWatch = Stopwatch.createStarted();
        mVerifier.setMetaData("Silence-Threshold", mSpeechConnection.getSilenceAmplitudeThreshold());
        mVerifier.start(new SveVerifier.StartCallback() {
            @Override
            public void onStartComplete() {
                mVerifySessionId = mVerifier.getSessionId();
                if (RECOVER_FROM_PROCESS == recoverOption) {
                    callVerifyProcess(null, RECOVER_FROM_NONE);
                } else if (RECOVER_FROM_END == recoverOption) {
                    callVerifyProcess(null, RECOVER_FROM_END);
                }else {
                    ++mCurrentVerifyAttempt;
                    if (stopWatch.stop().elapsed(TimeUnit.SECONDS) < 2) {
                        showButton(1000);
                    } else {
                        showButton(0);
                    }
                }
            }

            @Override
            public void onFailure(Exception ex) {
                if (RECOVER_FROM_START == recoverOption) {
                    ex.printStackTrace();
                    callVerifyStart(RECOVER_FROM_NONE);
                    return;
                }
                mVerifySessionId = "";
                showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
            }
        });
    }

    private void callVerifyEnd(final int recoverOption) {
//        mUiHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                getFragment().getView().setBackground(((MainActivity)getActivity()).getSceneBackground());
//            }
//        });

        if(mState != State.None) {
            setState(State.None);
            mVerifier.end(new SveVerifier.EndCallback() {
                @Override
                public void onEndComplete(boolean isAuthorized) {

                }

                @Override
                public void onFailure(Exception ex) {
                    mVerifySessionId = "";
                    showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
                }
            });
            logVerifyResult();
        }
    }

    private void callVerifyCancel(String message) {
        if(!mIsVerifyCanceled && mVerifier.isSessionOpen()) {
            mIsVerifyCanceled = true;
            mVerifier.cancel(message, new SveVerifier.CancelCallback() {
                @Override
                public void onCancelComplete() {
                    mVerifySessionId = "";
                }

                @Override
                public void onFailure(Exception ex) {
                    mVerifySessionId = "";
                    showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
                }
            });
            logVerifyResult();
        }
    }

    private void callVerifyUpdate(final SveVerifier.Result result) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                int index = mVerifyViewGroupAdapter.getCount() - 1;
                VerifyData data = mVerifyViewGroupAdapter.getItemType(index);
                data.updateResult(result);
                mVerifyViewGroupAdapter.update(index, data);
            }
        });
    }

    // Log Methods

    private void logEnrollResult() {
        HistoryModel model = HistoryModel.createEnrollmentRecord(VoxidemPreferences.getUserAccountName());
        model.setResult(mEnroller.getEnrollResult().name());
        getContractController().insertModel(HistoryContract.CONTENT_URI, model);
    }

    private void logVerifyResult() {
        HistoryModel model = HistoryModel.createEnrollmentVerificationRecord(VoxidemPreferences.getUserAccountName());
        model.setResult(mEnroller.getEnrollResult().name() + " - " + mVerifier.getVerifyResult().name());
        getContractController().insertModel(HistoryContract.CONTENT_URI, model);
    }

    // Question Setter Methods

    public void setQuestions(QuestionSet enrollQuestions, QuestionSet verifyQuestions) {
        mEnrollQuestionSet = enrollQuestions;
        mVerifyQuestionSet = verifyQuestions;
    }

    // Callback Classes

    @Override
    public void onSpeechServiceConnected() {
        callEnrollStart(RECOVER_FROM_START);
    }

    @Override
    public void onSpeechServiceDisconnected() {

    }

    @Override
    public void onVoiceStart() {
        Log.d(TAG, "onVoiceStart: " + mState.name());
        changeListeningStatus(true);
    }

    @Override
    public void onVoice(byte[] data, int size) {
        Log.d(TAG, "onVoice: " + mState.name());
        switch(mState) {
            case EnrollQuestions:
            case VerifyQuestions:
            case TrainingVerifications:
                mAudioBuffer.write(data, size);
                break;
        }
    }

    public void manageProgressBar(){
        int progress = (int) mEnroller.getSpeechExtracted();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            enrollment_question_progress.setProgress(progress,true);
        }
        enrollment_question_progress.getProgressDrawable().setColorFilter(mColorListening, PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onVoiceEnd() {
        mSpeechConnection.pause();
        changeListeningStatus(false);
        if (trainingQuestionCount != 10){
            setState(State.TrainingVerifications);
        }
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                scene_enroll_listening.setText("Processing...Please Wait");
            }
        });
        final byte[] buffer = mAudioBuffer.getFullBuffer();
        mAllContent.add(buffer,mSpeechContexts);
        switch(mState) {
            case EnrollQuestions:
            callEnrollProcess(buffer,RECOVER_FROM_PROCESS);
                break;
            case VerifyQuestions:
                callVerifyProcess(buffer,RECOVER_FROM_PROCESS);
                break;
            case TrainingVerifications:
               callTrainingProcess(buffer,RECOVER_FROM_PROCESS);
                break;
        }
        mAudioBuffer.clear();
    }


    private void callEnrollProcess(byte[] buffer, final int recoverOption) {
        Log.i(TAG, "callEnrollProcess");
//        if (buffer != null) {
////            mEnroller.append(buffer);
////        } else {
////            mEnroller.append(mAllContent);
////        }
        mEnroller.post(buffer, new SveEnroller.PostCallback() {
            @Override
            public void onPostComplete(SveEnroller.Result result) {
                Log.d(TAG, "Enroll Result(EnrollQuestions) onPostComplete: " + result.toString());
                mAllContent.clear();

                if (RECOVER_FROM_END == recoverOption) {
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            enrollment_question_progress.setVisibility(View.GONE);
                        }
                    });
                    showTraining(RECOVER_FROM_NONE);
                    return;
                }
                callEnrollUpdate(result);
                if(result != SveEnroller.Result.Success && result != SveEnroller.Result.NeedMore) {
                    showGatherFailure(R.string.scene_enroll_instructions_server_failure, result.name());
                    return;
                }
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_enroll_listening.setText("Listening");
                        enrollment_question_progress.setVisibility(View.VISIBLE);
                    }
                });
                // if we have enough speech, then we switch to only required speech phrases
                boolean hasEnoughSpeech = mEnroller.hasEnoughSpeech();
                if (hasEnoughSpeech) {
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            enrollment_question_progress.setVisibility(View.GONE);
                        }
                    });
                    showTraining(RECOVER_FROM_NONE);
                } else {
                    showNextEnrollQuestion(hasEnoughSpeech);
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            scene_enroll_listening.setText("Listening");
                            manageProgressBar();
                        }
                    });


                }
            }

            @Override
            public void onFailure(Exception ex) {
                Log.d(TAG, "Enroll Result(EnrollQuestions) onFailure: " + ex.getLocalizedMessage());
                if (RECOVER_FROM_PROCESS == recoverOption) {
                    ex.printStackTrace();
                    callEnrollStart(RECOVER_FROM_PROCESS);
                    return;
                }
                ex.printStackTrace();
                showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
            }
        });
    }

    private void callVerifyProcess(byte[] buffer, final int recoverOption) {

        Log.i(TAG, "callVerifyProcess");

        if (buffer != null) {
            mVerifier.append(buffer);
        } else {
            mVerifier.append(mAllContent);
        }
//
//        mVerifier.append(buffer, mSpeechContexts);
//        mSpeechContexts = null;
        mVerifier.post(new SveVerifier.PostCallback()  {
            @Override
            public void onPostComplete(SveVerifier.Result result) {
                Log.d(TAG, "Verify Result(VerifyQuestions) onPostComplete: " + result.toString());
                if (RECOVER_FROM_END == recoverOption) {
                    callVerifyEnd(RECOVER_FROM_NONE);
                    return;
                }
                callVerifyUpdate(result);
                VoxidemPreferences.setPrefIsReEnrollment(false);
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_enroll_listening.setText("Listening");
                    }
                });
                if (!showNextVerifyQuestion()) {
                    boolean isVerified = mVerifier.isVerified();
                    callVerifyEnd(RECOVER_FROM_END);
                    if (!isVerified) {
                        showGatherFailure(R.string.scene_enroll_instructions_speech_failure, result.name());
                    } else {
                        showTrainingInstructions();
                        // showGatherDone();
                    }
                }
            }

            @Override
            public void onFailure(Exception ex) {
                Log.d(TAG, "Verify Result(VerifyQuestions) onFailure: " + ex.getLocalizedMessage());
                if (RECOVER_FROM_PROCESS == recoverOption) {
                    ex.printStackTrace();
                    callVerifyStart(RECOVER_FROM_PROCESS);
                    return;
                }
                ex.printStackTrace();
                showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
            }
        });
    }

    private void callTrainingProcess(byte[] buffer, final int recoverOption) {
        Log.i(TAG, "callVerifyProcess");

        if (buffer != null) {
            mVerifier.append(buffer);
        } else {
            mVerifier.append(mAllContent);
        }

//        mVerifier.append(buffer, mSpeechContexts);
//        mSpeechContexts = null;
        mVerifier.post(new SveVerifier.PostCallback()  {
            @Override
            public void onPostComplete(final SveVerifier.Result result) {
                scene_enroll_train_progress.setVisibility(View.GONE);
                Log.d(TAG, "Verify Result(TrainingQuestions) onPostComplete: " + result.toString());
                if (RECOVER_FROM_END == recoverOption) {
                    callVerifyEnd(RECOVER_FROM_NONE);
                    return;
                }

                callVerifyUpdate(result);
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_enroll_listening.setText("Listening");
                    }
                });
                if (!showNextVerifyQuestion()) {
                    boolean isVerified = mVerifier.isVerified();
                    callVerifyEnd(RECOVER_FROM_END);
                    if (trainingQuestionCount == 0 && passTrainingCount < 3) {
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scene_enroll_listening.setText("Listening");
                                makeSnackbar(R.string.scene_enrollment_training_cancelled, Snackbar.LENGTH_INDEFINITE)
                                        .setAction(R.string.ok, new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                trainingQuestionCount = 10;
                                                passTrainingCount = 0;
                                                showGatherFailure(R.string.scene_enroll_instructions_speech_failure, result.name());
                                            }
                                        })
                                        .show();
                            }
                        });
                    } else {
                        if (!isVerified || (mVerifier.getVerifyScore() < 11.5)) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    scene_enroll_listening.setText("Listening");
                                    makeSnackbar(R.string.scene_enrollment_training_fail, Snackbar.LENGTH_INDEFINITE)
                                            .setAction(R.string.ok, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    showTrainingInstructions();
                                                }
                                            })
                                            .show();
                                }
                            });
                        } else {
                            passTrainingCount += 1;
                            if (passTrainingCount == 3) {
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scene_enroll_listening.setText("Listening");
                                        makeSnackbar(R.string.scene_enrollment_training_complete, Snackbar.LENGTH_INDEFINITE)
                                                .setAction(R.string.ok, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        trainingQuestionCount = 10;
                                                        passTrainingCount = 0;
                                                        setState(State.GatherDone);
                                                        VoxidemPreferences.setUserEnrolled();
                                                        showButton(0);
                                                    }
                                                })
                                                .show();
                                    }
                                });

                            } else {
                                mUiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        scene_enroll_listening.setText("Listening");
                                        makeSnackbar(R.string.scene_enrollment_training_pass, Snackbar.LENGTH_INDEFINITE)
                                                .setAction(R.string.ok, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        showTrainingInstructions();
                                                    }
                                                })
                                                .show();
                                    }
                                });

                            }
                        }
                    }
                }

            }

            @Override
            public void onFailure(Exception ex) {
                Log.d(TAG, "Verify Result(TrainingQuestions) onFailure: " + ex.getLocalizedMessage());
                if (RECOVER_FROM_PROCESS == recoverOption) {
                    ex.printStackTrace();
                    callVerifyStart(RECOVER_FROM_PROCESS);
                    return;
                }
                ex.printStackTrace();
                showGatherFailure(R.string.scene_enroll_instructions_server_failure, ex.getLocalizedMessage());
            }
        });
    }

        private void assertNotNull(Object object) {
        if(object == null) {
            throw new AssertionError();
        }
    }


    public void screenAnimation(){
        if(mAnimation) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    getFragment().getView().setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.analyze_gradient));
                    AnimationDrawable animationDrawable = (AnimationDrawable) getFragment().getView().getBackground();
                    animationDrawable.setEnterFadeDuration(1000);
                    animationDrawable.setExitFadeDuration(1000);
                    animationDrawable.start();
                }
            });
        }
    }

    public void getTone(){

        mMediaPlayer = MediaPlayer.create(getContext(), R.raw.short_chime);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setLooping(false);
        mMediaPlayer.start();
//        ToneGenerator audioToken = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
//        audioToken.startTone(ToneGenerator.TONE_CDMA_PIP,50);
    }

    public void handleQuestionTone(){
        if(mHasQuestionTone && mSpeechToText) {
            mSpeechConnection.pause();
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getTone();
                }
            },1300);
        } else if (mHasQuestionTone) {
            animationDelayDuration = 1000;
            mSpeechConnection.pause();
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    getTone();
                }
            });
        }else{
            return;
        }
    }

}
