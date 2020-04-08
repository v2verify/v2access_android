package com.validvoice.voxidem.scenes.subs.sve;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
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
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
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

import com.steelkiwi.library.ui.SeparateShapesView;
import com.validvoice.dynamic.audio.AudioBuffer;
import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.scene.IScenePermissionListener;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDialogFragment;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.dynamic.scene.widgets.KeyboardEditText;
import com.validvoice.dynamic.scene.widgets.TextInputKeyboardEditText;
import com.validvoice.dynamic.speech.authorization.SpeechContent;
import com.validvoice.dynamic.speech.authorization.SveFeedback;
import com.validvoice.dynamic.speech.authorization.SveVerifier;
import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContexts;
import com.validvoice.dynamic.speech.service.AdaptiveSpeechServiceConnection;
import com.validvoice.dynamic.speech.service.SpeechServiceConnection;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.cloud.UserLogin;
import com.validvoice.voxidem.scenes.devices.DevicesFragment;
import com.validvoice.voxidem.scenes.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class VerifyFragment extends SceneFragment implements IScenePermissionListener,
        SpeechServiceConnection.SpeechServiceListener {

    private static final String TAG = "VerifyFragment";
    public static final String VERIFY_INTENT = "verify_intent";

    private enum ScreenState {
        None,
        Setup,
        AskQuestions,
        ResolveVoice,
        AccessDenied,
        AccessDenied_Recalibrate,
        AccessDenied_NotEnoughSpeech,
        AccessDenied_Complete,
        AccessGranted,
        AccessGranted_Reset,
        AccessDenied_AskFeedbackQuestions,
        AccessDenied_AskFeedbackQuestions_Complete,
        AccessGranted_AskFeedbackQuestions,
        AccessOverridable,
        Error,
        Error_AskFeedbackQuestions
    }

    private enum FeedbackState {
        None,
        BreakAttempt,
        Recording,
        BackgroundNoise,
        Comments,
        Error,
        Done
    }

    private enum VerifyEndState {
        ExitingFragment,
        NotEnoughSpeech,
        Done
    }

    private ScreenState mScreenState = ScreenState.None;
    private FeedbackState mFeedbackState = FeedbackState.None;

    // UI References
    private Handler mUiHandler;

    // View References
    private ImageView scene_verify_vv_logo;

    // Verify Elements (AskQuestions)
    private TextView scene_verify_listening;
    private LinearLayout scene_verify_group;

    // Progress Elements (ResolveVoice)
    private TextView scene_verify_resolver_description;
    private ProgressBar resolver_progress_bar;

    // Completion Elements (AskFeedbackQuestions, AccessDenied, AccessGranted, Error)
    private TextView scene_verify_authorization;
    private TextView scene_verify_error_description;
    private TextView scene_verify_feedback_question;
    private Button scene_go_home;
    private TextView verify_text_label;
    private KeyboardEditText scene_verify_feedback_comments;
    private TextInputLayout scene_verify_override_pin_layout;
    private TextInputKeyboardEditText scene_verify_override_pin;
    private LinearLayout scene_verify_button_wrapper;
    private LinearLayout scene_verify_segment;
    private LinearLayout scene_banking_landing;
    private SeparateShapesView scene_verify_button;
    private ImageView microphoneView;

    // Resource caches
    private int mColorListening;
    private int mColorNotListening;

    // Audio Tools
    private AudioBuffer mAudioBuffer;
    private SpeechServiceConnection mSpeechConnection;

    // Verify References
    private SveVerifier mVerifier;
    private SveFeedback mFeedback;
    private VerifyViewGroupAdapter mVerifyViewGroupAdapter;
    private QuestionSetList mQuestionsList;
    private QuestionSet.Mode mQuestionMode = QuestionSet.Mode.RequiredOnly;
    private boolean mIsVerifyCanceled = false;
    private boolean mIsVerifyComplete = false;
    private boolean mIsInteractionStopping = false;
    private boolean mAskFeedbackQuestions = false;
    private boolean appTheme;
    private boolean initialRecordings = true;
    private MediaPlayer mMediaPlayer;

    //QuestionTone
    private boolean mHasQuestionTone = false;
    private boolean mSpeechToText = false;
    private boolean mAnimation = false;
    private TextToSpeech textToSpeech;
    private int animationDelayDuration = 0;
    private int questionCount = 0;



    // Speech References
    private SpeechContexts mSpeechContexts;
    private String mChosenNumbers;
    private String mLivenessString = "";
    private String mresetPassword;

    //
    private VerifyIntent mVerifyIntent;
    private int mCurrentVerifyAttempt;
    private int mMaxVerifyAttempts;
    private boolean mLeftScreenDuringTransaction;
    private boolean mAdaptiveMicOn;
    private SpeechContent mContent;
    private SpeechContent mAllContent;

    //
    private AddActorSceneActionListener mAddActorSceneActionListener;

    //
    private VerifyCalibrateDialog mVerifyCalibrateDialog;

    //Drawables
    Drawable failure;
    Drawable success;
    Drawable microphone;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        final Resources resources = getResources();
        final Activity activity = getActivity();


        ((MainActivity) getActivity()).spanTitleBar("Verification");


        mUiHandler = new Handler();
        mAudioBuffer = new AudioBuffer();
        mSpeechConnection = new SpeechServiceConnection(this);

        View view = inflater.inflate(R.layout.fragment_new_verify, container, false);

        scene_verify_vv_logo =  view.findViewById(R.id.scene_verify_vv_logo);

        scene_verify_listening = view.findViewById(R.id.scene_verify_listening);
        scene_verify_group = view.findViewById(R.id.scene_verify_group);
        verify_text_label = view.findViewById(R.id.scene_verify_vv_header);

        microphoneView =  view.findViewById(R.id.scene_verify_vv_logo);
        scene_verify_resolver_description = view.findViewById(R.id.scene_verify_resolver_description);
        resolver_progress_bar = view.findViewById(R.id.resolver_progress_bar);

        scene_verify_authorization = view.findViewById(R.id.scene_verify_authorization);
        scene_verify_error_description = view.findViewById(R.id.scene_verify_error_description);
        scene_verify_feedback_question =  view.findViewById(R.id.scene_verify_feedback_question);
        scene_verify_feedback_comments =  view.findViewById(R.id.scene_verify_feedback_comments);
        scene_verify_override_pin =  view.findViewById(R.id.scene_verify_override_pin);
        scene_verify_override_pin_layout =  view.findViewById(R.id.scene_verify_override_pin_layout);
        scene_verify_button_wrapper = view.findViewById(R.id.scene_verify_button_wrapper);
        scene_verify_button = view.findViewById(R.id.scene_verify_button);

        scene_verify_segment = view.findViewById(R.id.scene_verify_segment);
        scene_banking_landing = view.findViewById(R.id.scene_banking_landing);
        scene_go_home = view.findViewById(R.id.scene_go_home);

        verify_text_label.setText(R.string.scene_verify_verify_by_voice);


        scene_verify_feedback_comments.setOnKeyboardListener(new FeedbackKeyboardListener());
        scene_verify_override_pin.setOnKeyboardListener(new OverrideKeyboardListener());
        scene_verify_button.setOnButtonClickListener(new SeparateShapesViewClickListener());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mAskFeedbackQuestions = sp.getBoolean(SettingsFragment.PREF_GENERAL_FEEDBACK, false);
        mAdaptiveMicOn = sp.getBoolean(AdaptiveSpeechServiceConnection.PREF_ADAPTIVE_MICROPHONE, false);
        appTheme = sp.getBoolean(SettingsFragment.PREF_GENERAL_DARK_MODE,false);
        mHasQuestionTone = sp.getBoolean(SettingsFragment.PREF_GENERAL_QUESTION_TONE, false);
        mSpeechToText = sp.getBoolean(SettingsFragment.PREF_GENERAL_SPEECH_TO_TEXT, false);
        mAnimation = sp.getBoolean(SceneLayout.PREF_ALLOW_THEME_ANIMATIONS, false);

        if(activity != null) {
            final Resources.Theme theme = activity.getTheme();
            mColorListening = ResourcesCompat.getColor(resources, R.color.colorStatusListening, theme);
            mColorNotListening = appTheme ? ResourcesCompat.getColor(resources, R.color.colorTextDark, theme):ResourcesCompat.getColor(resources, R.color.colorStatusNotListening, theme);
            failure = ResourcesCompat.getDrawable(resources, R.drawable.ic_warning, theme);
            success = ResourcesCompat.getDrawable(resources, R.drawable.animated_check, theme);
            microphone = ResourcesCompat.getDrawable(resources, R.drawable.ic_microphone_green, theme);
        }

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


        scene_go_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSceneController().dispatchCollapse(R.id.actor_verify);
                    }
                }, 150);
            }
        });
//        final SharedPreferences.Editor editor = sp.edit();
//        editor.putBoolean(AdaptiveSpeechServiceConnection.PREF_ADAPTIVE_MICROPHONE, false).apply();

        mAddActorSceneActionListener = new AddActorSceneActionListener();

        setScreenState(ScreenState.Setup, "onViewCreated");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        mVerifyIntent = null;
        if(getSceneDirector().hasData(VERIFY_INTENT)) {
            Object verifyIntent = getSceneDirector().getData(VERIFY_INTENT, true);
            if (verifyIntent != null && verifyIntent instanceof VerifyIntent) {
                mVerifyIntent = (VerifyIntent) verifyIntent;
            }
        }

        if(mVerifyIntent == null || mVerifyIntent.getVerifyMode() == VerifyIntent.VerifyMode.VerifyUnknown) {
            Log.d(TAG, "onViewCreated: ::::::::::::");
            throw new IllegalArgumentException("Unknown Verify Mode");
        }

        mVerifyViewGroupAdapter = new VerifyViewGroupAdapter(
                getSceneController(),
                scene_verify_group
        );

        mVerifyViewGroupAdapter.setLimitations(true);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart");
        super.onStart();
        if (mContent != null) {
            mContent.clear();
        }
        getActivity().findViewById(R.id.botton_nav).setVisibility(View.GONE);

        dispatchBypassAction(R.id.actor_main, R.id.action_screen_keep_on, null);

        // ensure the screen state is None or Setup,
        // if we come back in from the Used Apps button, we need to skip the
        // reset logic
        if(mScreenState == ScreenState.None || mScreenState == ScreenState.Setup) {
            mLeftScreenDuringTransaction = false;
            dispatchBypassAction(R.id.actor_main, R.id.action_stop_calibrating, null);
            dispatchBypassAction(R.id.actor_main, R.id.action_step_tracker_off, null);

            getSceneController().getSceneLayout().addSceneActionListener(mAddActorSceneActionListener);

            requestPermission(1, Manifest.permission.RECORD_AUDIO, this);
        } else if(mLeftScreenDuringTransaction) {
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Activity activity = getActivity();
                    if(activity != null) {
                        startActivity(new Intent(activity, MainActivity.class));
                        activity.finish();
                    }
                }
            }, 450);
        }
    }


    @Override
    public void onPause() {
        Log.d(TAG, "onPause");

        if (textToSpeech != null){
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        super.onPause();

        mLeftScreenDuringTransaction = false;
    }


    @Override
    public void onStop() {
        Log.d(TAG, "onStop");

        Context context = getContext();
        if(context != null) {
            mSpeechConnection.unbind(context);
        }

        callVerifyEnd(VerifyEndState.ExitingFragment,RECOVER_FROM_NONE);

        if (!VoxidemPreferences.getIsMobileLogin()) {
            stopInteraction();
        }
        if (mAllContent != null) {
            mAllContent.clear();
        }

        if (mContent != null) {
            mContent.clear();
        }
        getSceneController().getSceneLayout().removeSceneActionListener(mAddActorSceneActionListener);
        mAddActorSceneActionListener = null;
        dispatchBypassAction(R.id.actor_main, R.id.action_screen_clear_on, null);
        dispatchBypassAction(R.id.actor_main, R.id.action_start_calibrating, null);
        super.onStop();
    }

    @Override
    public void onPermissionGranted(@NonNull String permission) {
        Log.d(TAG, "onPermissionGranted");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            assertNotNull(mQuestionsList);
            assertNotNull(mVerifyIntent);

            Context context = getContext();
            if(context != null) {
                mSpeechConnection.bind(context);
            }

            mContent = new SpeechContent();
            mAllContent = new SpeechContent();

            mCurrentVerifyAttempt = 0;
            mMaxVerifyAttempts = mVerifyIntent.getMaxAttempts();

            mVerifier = new SveVerifier()
                    .setClientId(mVerifyIntent.getVoxidemId())
                    .setInteractionId(mVerifyIntent.getIntentIds())
                    .setInteractionTag(mVerifyIntent.getVoxidemUser());
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
                                    VerifyFragment.this);
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

    @Override
    public void onSpeechServiceConnected() {
        callVerifyStart(RECOVER_FROM_START);
    }

    @Override
    public void onSpeechServiceDisconnected() {

    }

    @Override
    public void onVoiceStart() {

        changeListeningStatus(true);
    }

    @Override
    public void onVoice(byte[] data, int size) {
        Log.d(TAG, "onVoice: Size: " + size);
        mAudioBuffer.write(data, size);
    }

    @Override
    public void onVoiceEnd() {
        mSpeechConnection.pause();
        final byte[] buffer = mAudioBuffer.getFullBuffer();
        Log.d(TAG, "onVoiceEnd: Buffer-Length: " + buffer.length);
        changeListeningStatus(false);

        if (questionCount <= 2){
            setScreenState(ScreenState.AskQuestions, "callVerifyProcess().onPostComplete");
        }else {
            if (initialRecordings && questionCount == 3 ){
                mContent.add(buffer, mSpeechContexts);
                mAllContent.add(buffer, mSpeechContexts);
                mAudioBuffer.clear();
                initialRecordings = false;
                callVerifyProcess(RECOVER_FROM_PROCESS);
            }else {
                mSpeechContexts = null;
                mContent.add(buffer, mSpeechContexts);
                mAllContent.add(buffer, mSpeechContexts);
                callUpdateQuestion();
                mAudioBuffer.clear();
                if (!callNextQuestion()) {
                    callVerifyProcess(RECOVER_FROM_PROCESS);
                }
            }
        }
    }

    ///
    ///
    ///

//    @Override
//    public void onBackgroundOk() {
//
//    }
//
//    @Override
//    public void onBackgroundTooNoisy() {
//
//    }
//
//    @Override
//    public void onBackgroundCalibrated(int level) {
//        if(mVerifyCalibrateDialog != null) {
//            mVerifyCalibrateDialog.dismiss();
//            mVerifyCalibrateDialog = null;
//            dispatchBypassAction(R.id.actor_main, R.id.action_stop_calibrating, VerifyFragment.this);
//            callVerifyRestart();
//        }
//    }

    ///
    /// Question Methods
    ///

    public void setQuestions(QuestionSetList list) {
        Log.d(TAG, "setQuestions");
        mQuestionsList = list;
    }

    ///
    /// State Methods
    ///

    private void setScreenState(ScreenState state, String from) {

        if(mLeftScreenDuringTransaction) {
            Log.d(TAG, "setScreenState(" + from + "): Left Screen During Transaction");
            return;
        }

        final boolean notEnoughSpeech;
        if(state == ScreenState.AccessDenied_NotEnoughSpeech) {
            state = ScreenState.AccessDenied;
            notEnoughSpeech = true;
        } else {
            notEnoughSpeech = false;
        }

        if(state == ScreenState.AccessDenied) {
            ++mCurrentVerifyAttempt;
            if(mAdaptiveMicOn && mCurrentVerifyAttempt == 1) {
                state = ScreenState.AccessDenied_Recalibrate;
            } else if(mCurrentVerifyAttempt >= mMaxVerifyAttempts) {
                state = ScreenState.AccessDenied_Complete;
            }
        }

        if(mAskFeedbackQuestions) {
            if(state == ScreenState.AccessDenied) {
                state = ScreenState.AccessDenied_AskFeedbackQuestions;
            } else if(state == ScreenState.AccessDenied_Complete) {
                state = ScreenState.AccessDenied_AskFeedbackQuestions_Complete;
            } else if(state == ScreenState.AccessGranted) {
                state = ScreenState.AccessGranted_AskFeedbackQuestions;
            } else if(state == ScreenState.Error) {
                state = ScreenState.Error_AskFeedbackQuestions;
            }
        }

        Log.d(TAG, "setScreenState(" + from + "): " + mScreenState.name() + " - changing to - " + state.name());

        final ScreenState previousState = mScreenState;

        mScreenState = state;

        if(mVerifyViewGroupAdapter != null) {
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    mVerifyViewGroupAdapter.clear();
                }
            });
        }

        switch(mScreenState) {
            case Setup:

                mAudioBuffer.clear();
                mIsVerifyCanceled = false;
                mIsVerifyComplete = false;
                mQuestionMode = QuestionSet.Mode.RequiredOnly;
                mIsInteractionStopping = false;

                if(mQuestionsList != null) {
                    mQuestionsList.reset();
                }

                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_listening.setTextColor(mColorNotListening);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setVisibility(View.GONE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_question.setText(null);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setText(null);
                        scene_verify_override_pin.setText("");
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setVisibility(View.GONE);
                        scene_verify_button_wrapper.setVisibility(View.GONE);
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);

                        if(mAskFeedbackQuestions) {
                            scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.general_yes));
                            scene_verify_button.setRightShapeTitle(getResources().getString(R.string.general_no));
                        } else {
                            scene_verify_button.setSingleShape(true);
                            scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
                        }
                    }
                });

                break;
            case AskQuestions:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        mQuestionsList.nextSet();

                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setVisibility(View.GONE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_override_pin.setText("");
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setVisibility(View.GONE);
                        scene_verify_button_wrapper.setVisibility(View.GONE);

                        if(previousState != ScreenState.ResolveVoice) {
                            scene_verify_listening.setVisibility(View.VISIBLE);
                            scene_verify_group.setVisibility(View.VISIBLE);
                            callNextQuestion();
                        } else {
                            mUiHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    scene_verify_listening.setVisibility(View.VISIBLE);
                                    scene_verify_group.setVisibility(View.VISIBLE);
                                    callNextQuestion();
                                }
                            }, 300);
                        }
                    }
                });
                break;
            case ResolveVoice:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.VISIBLE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setVisibility(View.GONE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_override_pin.setText("");
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setVisibility(View.GONE);
                        scene_verify_button_wrapper.setVisibility(View.GONE);
                    }
                });
                break;
            case AccessDenied:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(!notEnoughSpeech ?
                                R.string.scene_verify_access_denied :
                                R.string.scene_verify_access_denied_not_enough_speech
                        );
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_override_pin.setText("");
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setSingleShape(false);
                        scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.scene_verify_again));
                        scene_verify_button.setRightShapeTitle(getResources().getString(R.string.scene_verify_cancel));

                        if(!mLeftScreenDuringTransaction) {
                            VerifyInstructionsDialog dialog = VerifyInstructionsDialog.newInstance(getSceneController());
                            getSceneController().showDialogFragment(dialog, "VerifyInstructionsDialog");
                        }

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scene_verify_button.setVisibility(View.VISIBLE);
                                scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                break;
            case AccessDenied_Recalibrate:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(!notEnoughSpeech ?
                                R.string.scene_verify_access_denied :
                                R.string.scene_verify_access_denied_not_enough_speech
                        );
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_override_pin.setText("");
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setSingleShape(false);
                        scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.scene_verify_again));
                        scene_verify_button.setRightShapeTitle(getResources().getString(R.string.scene_verify_cancel));

                        if(!mLeftScreenDuringTransaction) {
                            mVerifyCalibrateDialog = VerifyCalibrateDialog.newInstance(getSceneController());
                            getSceneController().showDialogFragment(mVerifyCalibrateDialog, "VerifyCalibrateDialog");
                        }

                        dispatchBypassAction(R.id.actor_main, R.id.action_start_calibrating, VerifyFragment.this);
                    }
                });
                break;
            case AccessDenied_Complete:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(!notEnoughSpeech ?
                                R.string.scene_verify_access_denied :
                                R.string.scene_verify_access_denied_not_enough_speech
                        );
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_override_pin.setText("");
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setSingleShape(true);
                        scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));

                        if(!mLeftScreenDuringTransaction) {
                            VerifyInstructionsDialog dialog = VerifyInstructionsDialog.newInstance(getSceneController());
                            getSceneController().showDialogFragment(dialog, "VerifyInstructionsDialog");
                        }

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scene_verify_button.setVisibility(View.VISIBLE);
                                scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                break;
            case AccessGranted:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(R.string.scene_verify_access_granted);
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_override_pin.setText("");
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setSingleShape(true);
                        scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
                        microphoneView.setImageDrawable(success);
                        ((Animatable) microphoneView.getDrawable()).start();
                        verify_text_label.setText(R.string.scene_verify_verified_by_voice);

                        if (mVerifyIntent.getVerifyMode() == VerifyIntent.VerifyMode.VerifyQrRequest) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getSceneDirector().setData(DevicesFragment.DEVICE_ID, mVerifyIntent.getDeviceQrId());
                                    getSceneDirector().setData(DevicesFragment.DEVICE_IP, mVerifyIntent.getDeviceQrIp());
                                    getSceneDirector().setData(DevicesFragment.DEVICE_INFO, mVerifyIntent.getDeviceQrInfo());
                                    getSceneController().dispatchExpand(R.id.actor_add);
                                    scene_verify_button.setVisibility(View.GONE);
                                    scene_verify_button_wrapper.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    scene_verify_button.setVisibility(View.VISIBLE);
                                    scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });
                break;
                case AccessGranted_Reset:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(R.string.scene_verify_access_granted_pwd_reset);
                        scene_verify_authorization.setTextSize(15);
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_override_pin.setText(mresetPassword);
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setSingleShape(true);
                        scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
                        microphoneView.setImageDrawable(success);
                        ((Animatable) microphoneView.getDrawable()).start();
                        verify_text_label.setTextSize(30);
                        verify_text_label.setText(mresetPassword);
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scene_verify_button.setVisibility(View.VISIBLE);
                                scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                break;
            case AccessDenied_AskFeedbackQuestions:
            case AccessDenied_AskFeedbackQuestions_Complete:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(!notEnoughSpeech ?
                                R.string.scene_verify_access_denied :
                                R.string.scene_verify_access_denied_not_enough_speech
                        );
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_override_pin.setText(null);
                        scene_verify_override_pin.clearFocus();
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_error_description.setVisibility(View.GONE);

                        if(!mLeftScreenDuringTransaction) {
                            VerifyInstructionsDialog dialog = VerifyInstructionsDialog.newInstance(getSceneController());
                            getSceneController().showDialogFragment(dialog, "VerifyInstructionsDialog");
                        }

                        setFeedbackState(FeedbackState.BreakAttempt, "setScreenState()");
                    }
                });
                break;
            case AccessGranted_AskFeedbackQuestions:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(R.string.scene_verify_access_granted);
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_override_pin.setText(null);
                        scene_verify_override_pin.clearFocus();
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_error_description.setVisibility(View.GONE);

                        if (mVerifyIntent.getVerifyMode() == VerifyIntent.VerifyMode.VerifyQrRequest) {
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    getSceneDirector().setData(DevicesFragment.DEVICE_ID, mVerifyIntent.getDeviceQrId());
                                    getSceneDirector().setData(DevicesFragment.DEVICE_IP, mVerifyIntent.getDeviceQrIp());
                                    getSceneDirector().setData(DevicesFragment.DEVICE_INFO, mVerifyIntent.getDeviceQrInfo());
                                    getSceneController().dispatchExpand(R.id.actor_add);
                                    scene_verify_button.setVisibility(View.GONE);
                                    scene_verify_button_wrapper.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            setFeedbackState(FeedbackState.BreakAttempt, "setScreenState()");
                        }
                    }
                });
                break;
            case AccessOverridable:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(R.string.scene_verify_access_override);
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        scene_verify_override_pin.setText(null);
                        scene_verify_override_pin.clearFocus();
                        scene_verify_button.setSingleShape(true);
                        scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_override));

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scene_verify_button.setVisibility(View.VISIBLE);
                                scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                                scene_verify_override_pin_layout.setVisibility(View.VISIBLE);
                                scene_verify_override_pin.setVisibility(View.VISIBLE);
                                scene_verify_override_pin.clearFocus();
                            }
                        });
                    }
                });
                break;
            case Error_AskFeedbackQuestions:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(R.string.scene_verify_access_denied_error);
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_error_description.setVisibility(View.GONE);
                        setFeedbackState(FeedbackState.Error, "setScreenState()");
                    }
                });
                break;
            case Error:
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_vv_logo.setVisibility(View.VISIBLE);
                        scene_verify_listening.setVisibility(View.GONE);
                        scene_verify_group.setVisibility(View.GONE);
                        scene_verify_resolver_description.setVisibility(View.GONE);
                        resolver_progress_bar.setVisibility(View.GONE);
                        scene_verify_authorization.setText(R.string.scene_verify_access_denied_error);
                        scene_verify_authorization.setVisibility(View.VISIBLE);
                        scene_verify_error_description.setVisibility(View.VISIBLE);
                        scene_verify_feedback_question.setVisibility(View.GONE);
                        scene_verify_feedback_comments.setVisibility(View.GONE);
                        scene_verify_override_pin.setText("");
                        scene_verify_override_pin.setVisibility(View.GONE);
                        scene_verify_override_pin_layout.setVisibility(View.GONE);
                        scene_verify_button.setSingleShape(true);
                        scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));

                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                scene_verify_button.setVisibility(View.VISIBLE);
                                scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                });
                break;
        }
    }

    private boolean isAccessGranted() {
        return mScreenState == ScreenState.AccessGranted ||
                mScreenState == ScreenState.AccessGranted_AskFeedbackQuestions;
    }

    private void setFeedbackState(FeedbackState state, String from) {
        Log.d(TAG, "setFeedbackState(" + from + "): " + mFeedbackState.name() + " - changing to - " + state.name());
        mFeedbackState = state;
        switch(mFeedbackState) {
            case BreakAttempt:
                scene_verify_feedback_question.setVisibility(View.VISIBLE);
                scene_verify_feedback_question.setText(R.string.scene_verify_feedback_question_break_attempt);
                scene_verify_button.setSingleShape(false);
                scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.general_yes));
                scene_verify_button.setRightShapeTitle(getResources().getString(R.string.general_no));
                scene_verify_button.setVisibility(View.VISIBLE);
                scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                break;
            case Recording:
                scene_verify_feedback_question.setText(null);
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_feedback_question.setText(R.string.scene_verify_feedback_question_recording);
                    }
                }, 1450);
                break;
            case BackgroundNoise:
                scene_verify_feedback_question.setText(null);
                scene_verify_button.setSingleShape(false);
                scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.general_yes));
                scene_verify_button.setRightShapeTitle(getResources().getString(R.string.general_no));
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_feedback_question.setText(R.string.scene_verify_feedback_question_background_noise);
                    }
                }, 1650);
                break;
            case Comments:
                scene_verify_feedback_question.setText(null);
                if(mScreenState == ScreenState.AccessDenied_AskFeedbackQuestions) {
                    scene_verify_button.setSingleShape(false);
                    scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.scene_verify_again));
                    scene_verify_button.setRightShapeTitle(getResources().getString(R.string.scene_verify_cancel));
                    scene_verify_feedback_comments.setText(null);
                    scene_verify_feedback_comments.clearFocus();
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scene_verify_feedback_question.setText(R.string.scene_verify_feedback_question_comments);
                            scene_verify_feedback_comments.setVisibility(View.VISIBLE);
                            scene_verify_feedback_comments.clearFocus();
                        }
                    }, 1750);
                } else if(mScreenState == ScreenState.AccessGranted_AskFeedbackQuestions ||
                        mScreenState == ScreenState.AccessDenied_AskFeedbackQuestions_Complete) {
                    scene_verify_button.setSingleShape(true);
                    scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
                    scene_verify_feedback_comments.setText(null);
                    scene_verify_feedback_comments.clearFocus();
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scene_verify_feedback_question.setText(R.string.scene_verify_feedback_question_comments);
                            scene_verify_feedback_comments.setVisibility(View.VISIBLE);
                            scene_verify_feedback_comments.clearFocus();
                        }
                    }, 1750);
                }
                break;
            case Error:
                scene_verify_feedback_question.setText(null);
                scene_verify_button.setSingleShape(true);
                scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
                scene_verify_feedback_comments.setText(null);
                scene_verify_feedback_comments.clearFocus();
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_feedback_question.setText(R.string.scene_verify_feedback_question_error);
                        scene_verify_feedback_comments.setVisibility(View.VISIBLE);
                        scene_verify_feedback_comments.clearFocus();
                    }
                }, 1750);
                break;
            case Done:
                scene_verify_feedback_question.setVisibility(View.GONE);
                scene_verify_feedback_question.setText(null);
                scene_verify_feedback_comments.setVisibility(View.GONE);
                scene_verify_feedback_comments.setText(null);
                scene_verify_override_pin.setText("");
                scene_verify_override_pin.setVisibility(View.GONE);
                scene_verify_override_pin_layout.setVisibility(View.GONE);
                break;
        }
    }

    ///
    /// UI Methods
    ///

    private void changeListeningStatus(final boolean isListening) {
        Log.d(TAG, "changeListeningStatus");
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                if(isListening){
                    screenAnimation();
                }
                scene_verify_listening.setTextColor(isListening ? mColorListening : mColorNotListening);
            }
        });
    }

    private boolean callNextQuestion() {
        Log.d(TAG, "callNextQuestion");
        if (mIsVerifyCanceled) {
            Log.d(TAG, "callNextQuestion().Cancelled");
            return true;
        }

        if(mQuestionsList.nextQuestion(mQuestionMode)) {
            questionCount += 1;
            final QuestionSet.IQuestion q = mQuestionsList.getQuestion();
            mSpeechConnection.setSpeechMaxLengthMillis(q.maxSpeechLengthMillis());
            mSpeechConnection.setSpeechTimeoutMillis(q.speechTimeoutMillis());
            final VerifyData verifyData;


            if(q.questionType() == QuestionSet.Type.Question) {
                verifyData = new VerifyData(q.getQuestion());
            } else if(q.questionType() == QuestionSet.Type.Challenge) {
                mChosenNumbers = q.getQuestion();
                mSpeechContexts = new SpeechContexts();
                List<String> phrases = new ArrayList<>();
                if (questionCount <= 3) {
                    mLivenessString += " "+mChosenNumbers;
                    if (questionCount == 3){
                        phrases.add(mLivenessString.trim());
                        if (mVerifyIntent.getLanguageCode().contains("en")) {
                            mSpeechContexts.add(
                                    q.getName(), q.getGrammar(),
                                    mVerifyIntent.getLanguageCode(),
                                    phrases
                            );
                        } else {
                            mSpeechContexts.add(
                                    q.getName(),
                                    mVerifyIntent.getLanguageCode(),
                                    phrases
                            );
                        }
                    }
                }else{
                    phrases.add(mChosenNumbers);
                if (mVerifyIntent.getLanguageCode().contains("en")) {
                    mSpeechContexts.add(
                            q.getName(), q.getGrammar(),
                            mVerifyIntent.getLanguageCode(),
                            phrases
                    );
                } else {
                    mSpeechContexts.add(
                            q.getName(),
                            mVerifyIntent.getLanguageCode(),
                            phrases
                    );
                }
            }
                verifyData = new VerifyData(mChosenNumbers);
            } else {
                throw new IllegalArgumentException("FreeSpeech type not allowed");
            }
            mVerifyViewGroupAdapter.clear();



            if(mSpeechToText){
                mSpeechConnection.pause();
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mVerifyViewGroupAdapter.add(verifyData);
                    }
                }, 1000);
                animationDelayDuration = mHasQuestionTone?1100+1000:1900;
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String speechString = "Say " + verifyData.getSay();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            textToSpeech.speak(speechString,TextToSpeech.QUEUE_FLUSH,null,null);
                        } else {
                            textToSpeech.speak(speechString, TextToSpeech.QUEUE_FLUSH, null);
                        }
                    }
                });
            }
            handleQuestionTone();
            if (!mSpeechToText){
                if (mHasQuestionTone){
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mVerifyViewGroupAdapter.add(verifyData);
                        }
                    }, 1000);

                }else{
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mVerifyViewGroupAdapter.add(verifyData);
                        }
                    }, 700);

                }

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

    private void callUpdateQuestion() {
        Log.d(TAG, "callUpdateQuestion");
        if (!mIsVerifyCanceled) {
            if(!mQuestionsList.isLastQuestion(mQuestionMode)) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        int index = mVerifyViewGroupAdapter.getCount() - 1;
                        VerifyData data = mVerifyViewGroupAdapter.getItemType(index);
                        data.updateResult(SveVerifier.Result.Ambiguous);
                        mVerifyViewGroupAdapter.update(index, data);
                    }
                });
            }
        }
    }

    private void setError(String message, String from) {
        scene_verify_error_description.setText(message);
        setScreenState(ScreenState.Error, "setError() -> " + from);
    }

    ///
    /// Verify Methods
    ///


    private static int RECOVER_FROM_NONE = 0;
    private static int RECOVER_FROM_START = 1;
    private static int RECOVER_FROM_PROCESS = 2;
    private static int RECOVER_FROM_END = 3;

    private void callVerifyStart(final int recoverOption) {
        screenAnimation();
        questionCount = 0;
        initialRecordings = true;
        mLivenessString = "";
        Log.d(TAG, "callVerifyStart");
        mVerifier.setMetaData("Silence-Threshold", mSpeechConnection.getSilenceAmplitudeThreshold());
        mVerifier.start(new SveVerifier.StartCallback() {
            @Override
            public void onStartComplete() {
                Log.d(TAG, "callVerifyStart().onStartComplete");
                if (mAskFeedbackQuestions) {
                    mFeedback = new SveFeedback(mVerifier);
                }
                if (RECOVER_FROM_PROCESS == recoverOption) {
                    callVerifyProcess(RECOVER_FROM_NONE);
                } else if (RECOVER_FROM_END == recoverOption) {
                    callVerifyProcess(RECOVER_FROM_END);
                } else {
                    mUiHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mQuestionsList.reset();
                            mSpeechConnection.resume();
                            setScreenState(ScreenState.AskQuestions, "callVerifyStart().onStartComplete");
                        }
                    });
                }
            }

            @Override
            public void onFailure(final Exception ex) {
                Log.d(TAG, "callVerifyStart().onFailure"+ex.getLocalizedMessage());

                if (ex.getLocalizedMessage().contains("LockedOut")){
                    ex.printStackTrace();
                    lockedOutMessage();
                }else {
                    if (RECOVER_FROM_START == recoverOption) {
                        ex.printStackTrace();
                        callVerifyStart(RECOVER_FROM_NONE);
                        return;
                    } else {
                        ex.printStackTrace();
                        mUiHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getActivity(), "Please refresh the browser and Try again", Toast.LENGTH_LONG).show();
                                getSceneDirector().setHomeSceneActive();
                            }
                        });
                    }
                }
            }
        });
    }


    public void lockedOutMessage(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Account Locked Out");
        alertDialogBuilder.setMessage(R.string.scene_lockedout_failure);
        alertDialogBuilder.setCancelable(true);
        alertDialogBuilder.setPositiveButton(R.string.ok,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getSceneDirector().setHomeSceneActive();
                        return;
                    }
                }
        );

        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }

    private void callVerifyProcess(final int recoverOption) {
//        questionCount = 0;
        if(mVerifier.isSessionOpen()) {

            if (RECOVER_FROM_PROCESS == recoverOption) {
                mVerifier.append(mContent);
            } else {
                mVerifier.append(mAllContent);
            }
            setScreenState(ScreenState.ResolveVoice, "callVerifyProcess()");
            mVerifier.post(new SveVerifier.PostCallback() {

                @Override
                public void onPostComplete(SveVerifier.Result result) {
                    Log.d(TAG, "Verify Result(VerifyQuestions) onPostComplete: " + result.toString());
                    if (RECOVER_FROM_END == recoverOption) {
                        Log.d(TAG, "onPostComplete: recover from end::::::::::::::::::::::::::::::");
                        callVerifyEnd(VerifyEndState.Done,RECOVER_FROM_NONE);
                        return;
                    }
                    mContent.clear();
                    if(result == SveVerifier.Result.PassIsAlive) {
                        callDbUpdate();
                        callVerifyEnd(VerifyEndState.Done,RECOVER_FROM_END);
                    } else if(result == SveVerifier.Result.PassNotAlive) {
                        callDbUpdate();
                        callVerifyEnd(VerifyEndState.Done,RECOVER_FROM_END);
                    } else if(result == SveVerifier.Result.NeedMore ||
                            result == SveVerifier.Result.NeedMoreAlive ||
                            result == SveVerifier.Result.Ambiguous ||
                            result == SveVerifier.Result.AmbiguousIsAlive) {
                        if(mQuestionsList.isResultOverridableQuestion() && mVerifier.isOverridable()) {
                            setScreenState(ScreenState.AccessOverridable, "callVerifyProcess().onPostComplete");
                        } else {
                            mQuestionMode = QuestionSet.Mode.Question;
                            if(mQuestionsList.hasMoreSets()) {
                                setScreenState(ScreenState.AskQuestions, "callVerifyProcess().onPostComplete");
                            } else {
                                callVerifyEnd(VerifyEndState.NotEnoughSpeech,RECOVER_FROM_END);
                            }
                        }
                    } else {
                        callDbUpdate();
                        if(mVerifier.isOverridable()) {
                            setScreenState(ScreenState.AccessOverridable, "callVerifyProcess().onPostComplete");
                        } else {
                            callVerifyEnd(VerifyEndState.Done,RECOVER_FROM_END);
                        }
                    }
                }

                @Override
                public void onFailure(final Exception ex) {
                    Log.d(TAG, "callVerifyProcess().onFailure()");
                    mContent.clear();
                    if (RECOVER_FROM_PROCESS == recoverOption) {
                        ex.printStackTrace();
                        callVerifyStart(RECOVER_FROM_PROCESS);
                        return;
                    }
                    ex.printStackTrace();
                    setError(getResources().getString(R.string.scene_user_new_error), "callVerifyProcess().onFailure()");
                }
            });
        }
    }

    private void callVerifyEnd(final VerifyEndState endState,final int recoverOption) {
        Log.d(TAG, "callVerifyEnd");
        if(mVerifier != null && mVerifier.isSessionOpen()) {
            mContent.clear();
            if(VerifyEndState.ExitingFragment == endState) {
                mLeftScreenDuringTransaction = false;
            }

            mVerifier.end(new SveVerifier.EndCallback() {
                @Override
                public void onEndComplete(boolean isAuthorized) {
                    Log.d(TAG, "callVerifyEnd().onEndComplete()"+isAuthorized);

                    if(isAuthorized) {

                        boolean complete = false, success = false;
                        if(mVerifier.getExtra().containsKey("complete")) {
                            complete = (boolean)mVerifier.getExtra().get("complete");
                        }
                        
                        if(mVerifier.getExtra().containsKey("success")) {
                            success = (boolean)mVerifier.getExtra().get("success");
                        }
                        
                        if(complete && success) {
                            mIsVerifyComplete = true;

                            if(mVerifier.getExtra().containsKey("message")) {
                                mresetPassword = (String)mVerifier.getExtra().get("message");
                            }

                            if(mresetPassword != null && !mresetPassword.isEmpty()) {
                                setScreenState(ScreenState.AccessGranted_Reset, "callVerifyEnd().onEndComplete");
                            } else {
                                setScreenState(ScreenState.AccessGranted, "callVerifyEnd().onEndComplete");
                            }
                            
                            return;
                        }

                    }

                    if(VerifyEndState.NotEnoughSpeech == endState) {
                        setScreenState(ScreenState.AccessDenied_NotEnoughSpeech, "callVerifyEnd().onEndComplete()");
                    } else {
                        if (isAuthorized && VoxidemPreferences.getIsMobileLogin()){
                            mUiHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ((MainActivity) getActivity()).spanTitleBar("");
                                    scene_verify_segment.setVisibility(View.GONE);
                                    scene_banking_landing.setVisibility(View.VISIBLE);
                                }
                            });

                        }else {
                            setScreenState(ScreenState.AccessDenied, "callVerifyEnd().onEndComplete()");
                        }
                    }
                }

                @Override
                public void onFailure(final Exception ex) {
                    Log.d(TAG, "callVerifyEnd().onFailure()");


                    ex.printStackTrace();
                    if(VerifyEndState.ExitingFragment != endState) {
                        if (RECOVER_FROM_END == recoverOption) {
                            ex.printStackTrace();
                            callVerifyStart(RECOVER_FROM_END);
                            return;
                        }
                        setError(getResources().getString(R.string.scene_user_new_error), "callVerifyEnd().onFailure()");
                    }
                }
            });
        }
    }

    private void callVerifyCancel() {
        Log.d(TAG, "callVerifyCancel");
        if (!mIsVerifyCanceled && mVerifier.isSessionOpen()) {
            mIsVerifyCanceled = true;
            mVerifier.cancel("Stopping Verification", new SveVerifier.CancelCallback() {
                @Override
                public void onCancelComplete() {
                    Log.d(TAG, "callVerifyCancel().onCancelComplete");
                }

                @Override
                public void onFailure(final Exception ex) {
                    Log.d(TAG, "callVerifyCancel().onFailure");
                    if(isDetached()) return;
                    ex.printStackTrace();
                    if(!mIsInteractionStopping) {
                        setError(getResources().getString(R.string.scene_user_new_error), "callVerifyCancel().onFailure");
                    }
                }
            });
            callDbUpdate();
        }
    }

    private void callVerifyRestart() {
        questionCount = 0;
        initialRecordings = true;
        mLivenessString = "";
        mContent.clear();
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                setScreenState(ScreenState.Setup, "callVerifyEnd().onEndComplete()");
                callVerifyStart(RECOVER_FROM_START);
            }
        });
    }

    ///
    /// Database Methods
    ///

    private void callDbUpdate() {
        Log.d(TAG, "callDbUpdate");

        SveVerifier.Result result = SveVerifier.Result.Error;
        if (mVerifier.hasResult()) {
            result = mVerifier.getVerifyResult();
        }

        if(mAskFeedbackQuestions && mFeedback != null) {
            mVerifyIntent.updateHistory(
                    getContractController(), result.name(), mVerifier.getSessionId(),
                    mChosenNumbers, "N/A", mFeedback.getBreakAttempt(),
                    mFeedback.getRecording(), mFeedback.getBackgroundNoise(),
                    mFeedback.getComments()
            );
        } else {
            mVerifyIntent.updateHistory(
                    getContractController(), result.name(), mVerifier.getSessionId(),
                    mChosenNumbers, "N/A"
            );
        }
    }

    ///
    /// Voxidem Cloud Methods
    ///

    private void stopInteraction() {
        Log.d(TAG, "stopInteraction");
        if(!mIsVerifyComplete) {
            mIsInteractionStopping = true;
            CloudMessage message;
            switch (mVerifyIntent.getVerifyMode()) {
                case VerifyQrCode:
                case VerifyQrRequest:
                case VerifyGuest:
                    VerifyIntent.VerifyInstance instance = mVerifyIntent.getInstances().get(0);
                    message = CloudMessage.Delete("v2access.Intent.{v2w_interaction_id}");
                    message.putString("v2w_interaction_id", instance.getQrIntent());
                    break;
                case VerifyDevice:
                    message = CloudMessage.Delete("v2access.Intents.{v2w_user_name}.{v2w_interaction_ids}");
                    message.putString("v2w_user_name", mVerifyIntent.getVoxidemUser());
                    message.putObject("v2w_interaction_ids", mVerifyIntent.getIntentIds());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown verify mode");
            }
            message.send(new CloudController.ResponseCallback() {
                @Override
                public void onResult(CloudResult result) {

                }

                @Override
                public void onError(CloudError error) {

                }

                @Override
                public void onFailure(Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }

    ///
    /// FeedbackKeyboardListener Class
    ///

    private class FeedbackKeyboardListener implements KeyboardEditText.KeyboardListener {

        @Override
        public void onStateChanged(KeyboardEditText keyboardEditText, boolean showing) {
            if(mFeedbackState == FeedbackState.Comments ||
                    mFeedbackState == FeedbackState.Error) {
                if (showing) {
                    scene_verify_vv_logo.setVisibility(View.GONE);
                } else {
                    scene_verify_vv_logo.setVisibility(View.VISIBLE);
                }
            } else if(scene_verify_vv_logo.getVisibility() != View.VISIBLE) {
                scene_verify_vv_logo.setVisibility(View.VISIBLE);
            }
        }

    }

    ///
    /// OverrideKeyboardListener Class
    ///

    private class OverrideKeyboardListener implements TextInputKeyboardEditText.KeyboardListener {

        @Override
        public void onStateChanged(TextInputKeyboardEditText keyboardEditText, boolean showing) {
            if(mScreenState == ScreenState.AccessOverridable) {
                if (showing) {
                    scene_verify_vv_logo.setVisibility(View.GONE);
                } else {
                    scene_verify_vv_logo.setVisibility(View.VISIBLE);
                }
            } else if(scene_verify_vv_logo.getVisibility() != View.VISIBLE) {
                scene_verify_vv_logo.setVisibility(View.VISIBLE);
            }
        }

    }

    ///
    /// SeparateShapesViewClickListener Class
    ///

    private class SeparateShapesViewClickListener implements SeparateShapesView.OnButtonClickListener {

        @Override
        public boolean onLeftButtonClick() {
            getActivity().findViewById(R.id.powered_by).setBackgroundColor(Color.TRANSPARENT);
            if(mAskFeedbackQuestions && mFeedback != null) {
                onLeftButtonClickFeedback();
            } else {
                callVerifyRestart();
            }
            return true;
        }

        @Override
        public boolean onRightButtonClick() {
            getActivity().findViewById(R.id.powered_by).setBackgroundColor(Color.TRANSPARENT);
            if(mAskFeedbackQuestions && mFeedback != null) {
                onRightButtonClickFeedback();
            } else {
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSceneController().dispatchCollapse(R.id.actor_verify);
                    }
                }, 950);
            }
            return true;
        }

        @Override
        public boolean onMiddleButtonClick() {
            if(mScreenState == ScreenState.AccessOverridable) {
                String overridePin = scene_verify_override_pin.getText().toString();
                if(overridePin.isEmpty()) {
                    scene_verify_override_pin_layout.setError(getString(R.string.scene_verify_override_pin_error));
                    return false;
                } else if(!validateOverridePin(overridePin)) {
                    scene_verify_override_pin_layout.setError(getString(R.string.scene_verify_override_pin_invalid_error));
                    return false;
                } else {
                    scene_verify_override_pin_layout.setErrorEnabled(false);
                }
                mVerifier.setAuthToken(overridePin);
                callVerifyEnd(VerifyEndState.Done,RECOVER_FROM_END);
            } else if(mAskFeedbackQuestions && mFeedback != null) {
                onMiddleButtonClickFeedback();
            } else {
                setFeedbackState(FeedbackState.Done, "onMiddleButtonClick()");
                if(mScreenState == ScreenState.AccessGranted || mScreenState == ScreenState.AccessGranted_AskFeedbackQuestions) {
                    getSceneController().dispatchBypassAction(R.id.actor_main, R.id.action_step_tracker_on, null);
                }
                mUiHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        getSceneController().dispatchCollapse(R.id.actor_verify);
                    }
                }, 950);
            }
            return true;
        }

        private void onLeftButtonClickFeedback() {
            switch(mFeedbackState) {
                case BreakAttempt:
                    mFeedback.setBreakAttempt(true);
                    setFeedbackState(isAccessGranted() ? FeedbackState.Recording : FeedbackState.Comments, "onLeftButtonClickFeedback()");
                    break;
                case Recording:
                    mFeedback.setRecording(true);
                    setFeedbackState(FeedbackState.BackgroundNoise, "onLeftButtonClickFeedback()");
                    break;
                case BackgroundNoise:
                    mFeedback.setBackgroundNoise(true);
                    setFeedbackState(FeedbackState.Comments, "onLeftButtonClickFeedback()");
                    break;
                case Comments:
                    mFeedback.setComments(scene_verify_feedback_comments.getText().toString());
                    mFeedback.post(new SveFeedback.PostCallback() {
                        @Override
                        public void onPostComplete(boolean isOk) {

                        }

                        @Override
                        public void onFailure(Exception ex) {
                            Log.d(TAG, "onRightButtonClickFeedback().onFailure()");
                            ex.printStackTrace();
                        }
                    });
                    callVerifyRestart();
                    setFeedbackState(FeedbackState.Done, "onLeftButtonClickFeedback()");
                    break;
            }
        }

        private void onRightButtonClickFeedback() {
            switch(mFeedbackState) {
                case BreakAttempt:
                    mFeedback.setBreakAttempt(false);
                    setFeedbackState(isAccessGranted() ? FeedbackState.Comments : FeedbackState.BackgroundNoise, "onRightButtonClickFeedback()");
                    break;
                case Recording:
                    mFeedback.setRecording(false);
                    setFeedbackState(FeedbackState.BackgroundNoise, "onRightButtonClickFeedback()");
                    break;
                case BackgroundNoise:
                    mFeedback.setBackgroundNoise(false);
                    setFeedbackState(FeedbackState.Comments, "onRightButtonClickFeedback()");
                    break;
                case Comments:
                    mFeedback.setComments(scene_verify_feedback_comments.getText().toString());
                    mFeedback.post(new SveFeedback.PostCallback() {
                        @Override
                        public void onPostComplete(boolean isOk) {

                        }

                        @Override
                        public void onFailure(Exception ex) {
                            Log.d(TAG, "onRightButtonClickFeedback().onFailure()");
                            ex.printStackTrace();
                        }
                    });
                    setFeedbackState(FeedbackState.Done, "onRightButtonClickFeedback()");
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getSceneController().dispatchCollapse(R.id.actor_verify);
                        }
                    }, 950);
                    break;
            }
        }

        private void onMiddleButtonClickFeedback() {
            switch(mFeedbackState) {
                case Comments:
                case Error:
                    mFeedback.setComments(scene_verify_feedback_comments.getText().toString());
                    mFeedback.post(new SveFeedback.PostCallback() {
                        @Override
                        public void onPostComplete(boolean isOk) {

                        }

                        @Override
                        public void onFailure(Exception ex) {
                            Log.d(TAG, "onMiddleButtonClickFeedback().onFailure()");
                            ex.printStackTrace();
                        }
                    });
                    setFeedbackState(FeedbackState.Done, "onMiddleButtonClickFeedback()");
                    mUiHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getSceneController().dispatchCollapse(R.id.actor_verify);
                        }
                    }, 950);
                    break;
            }
        }

        private boolean validateOverridePin(String overridePin) {
            if (overridePin.length() != 6) {
                return false;
            }

            char ch;
            boolean b;
            for (int j = 0; j < overridePin.length(); j++) {
                ch = overridePin.charAt(j);
                b = '0' <= ch && ch <= '9';
                if (!b) {
                    return false;
                }
            }

            return true;
        }

    }

    ///
    /// AddActorSceneActionListener Class
    ///

    private class AddActorSceneActionListener extends SceneLayout.SimpleSceneActionListener {
        @Override
        public void onActorClosed(int actorId) {
            if(actorId == R.id.actor_add) {
                mUiHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mScreenState == ScreenState.AccessGranted) {
                            scene_verify_button.setVisibility(View.VISIBLE);
                            scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                        } else if(mScreenState == ScreenState.AccessGranted_AskFeedbackQuestions) {
                            setFeedbackState(FeedbackState.BreakAttempt, "setScreenState()");
                        }
                    }
                });
            }
        }
    }

    ///
    /// VerifyInstructionsDialog Class
    ///

    public static class VerifyInstructionsDialog extends SceneDialogFragment {

        @Override
        @NonNull
        public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

            alertDialogBuilder.setTitle(R.string.scene_verify_instructions_speech_tip);
            alertDialogBuilder.setMessage(R.string.scene_verify_instructions_speech_failure);
            alertDialogBuilder.setCancelable(true);
            alertDialogBuilder.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            dispatchBypassAction(R.id.actor_main, R.id.action_start_calibrating, null);
                        }
                    }
            );

            return alertDialogBuilder.create();
        }

        public static VerifyInstructionsDialog newInstance(SceneController controller) {
            VerifyInstructionsDialog fragment = new VerifyInstructionsDialog();
            fragment.setSceneController(controller);
            return fragment;
        }
    }

    ///
    /// VerifyCalibrateDialog Class
    ///

    public static class VerifyCalibrateDialog extends SceneDialogFragment {

        @Override
        @NonNull
        public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());

            progressDialog.setMessage(getString(R.string.scene_verify_calibrate_speech_failure));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            return progressDialog;
        }

        public static VerifyCalibrateDialog newInstance(SceneController controller) {
            VerifyCalibrateDialog fragment = new VerifyCalibrateDialog();
            fragment.setSceneController(controller);
            return fragment;
        }
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
    }

    public void handleQuestionTone(){
        if(mHasQuestionTone && mSpeechToText) {
            mSpeechConnection.pause();
            mUiHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    getTone();
                }
            },1350);
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

    @Override
    public void onResume() {
        super.onResume();
        animationDelayDuration = 0;
        questionCount = 0;
    }
}
