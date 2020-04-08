package com.validvoice.voxidem.scenes.subs.sve;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.validvoice.dynamic.scene.ISceneModelView;
import com.validvoice.dynamic.scene.IScenePermissionListener;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneDialogFragment;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.dynamic.scene.SceneModelViewFragment;
import com.validvoice.dynamic.scene.widgets.KeyboardEditText;
import com.validvoice.dynamic.speech.authorization.SveVerifier;
import com.validvoice.dynamic.speech.authorization.lib.contexts.SpeechContexts;
import com.validvoice.dynamic.speech.service.SpeechServiceConnection;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.cloud.UserLogin;
import com.validvoice.voxidem.scenes.devices.DevicesFragment;
import com.validvoice.voxidem.scenes.settings.SettingsFragment;

import java.util.ArrayList;
import java.util.List;

public class VerifyModelViewFragment extends SceneModelViewFragment implements IScenePermissionListener,
        SpeechServiceConnection.SpeechServiceListener {

    private static final String TAG = "VerifyModelViewFragment";
    public static final String VERIFY_INTENT = "verify_intent";

    private enum FeedbackState {
        None,
        BreakAttempt,
        Recording,
        BackgroundNoise,
        Comments,
        Error,
        Done
    }

    private FeedbackState mFeedbackState = FeedbackState.None;

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
    private KeyboardEditText scene_verify_feedback_comments;
    private LinearLayout scene_verify_button_wrapper;
    private SeparateShapesView scene_verify_button;

    // Resource caches
    private int mColorListening;
    private int mColorNotListening;

    // Audio Tools
    private AudioBuffer mAudioBuffer;
    private SpeechServiceConnection mSpeechConnection;

    // Verify References
    private SveVerifier mVerifier;
    private VerifyViewGroupAdapter mVerifyViewGroupAdapter;
    private QuestionSetList mQuestionsList;
    private QuestionSet.Mode mQuestionMode = QuestionSet.Mode.RequiredOnly;
    private boolean mIsVerifyCanceled = false;
    private boolean mIsVerifyComplete = false;
    private boolean mIsInteractionStopping = false;
    private boolean mAskFeedbackQuestions = false;
    private boolean mFeedbackBreakAttempt = false;
    private boolean mFeedbackRecording = false;
    private boolean mFeedbackBackgroundNoise = false;
    private String mFeedbackComments = "";

    // Speech References
    private SpeechContexts mSpeechContexts;
    private String mChosenNumbers;

    //
    private VerifyIntent mVerifyIntent;
    private int mCurrentVerifyAttempt;
    private int mMaxVerifyAttempts;
    private boolean mLeftScreenDuringTransaction;
    private boolean mNotEnoughSpeech;
    private boolean mHasRecalibrated;

    //
    private AddActorSceneActionListener mAddActorSceneActionListener;

    @Override
    public View onCreateModelView(@NonNull LayoutInflater inflater, ViewGroup container,
                                  Bundle savedInstanceState) throws Exception {

        Log.d(TAG, "onCreateModelView");
        final Resources resources = getResources();
        final Activity activity = getActivity();
        if(activity != null) {
            final Resources.Theme theme = activity.getTheme();
            mColorListening = ResourcesCompat.getColor(resources, R.color.colorStatusListening, theme);
            mColorNotListening = ResourcesCompat.getColor(resources, R.color.colorStatusNotListening, theme);
        }

        mAudioBuffer = new AudioBuffer();
        mSpeechConnection = new SpeechServiceConnection(this);
        mNotEnoughSpeech = false;
        mHasRecalibrated = false;

        View view = inflater.inflate(R.layout.fragment_new_verify, container, false);

        scene_verify_vv_logo =  view.findViewById(R.id.scene_verify_vv_logo);

        scene_verify_listening = view.findViewById(R.id.scene_verify_listening);
        scene_verify_group = view.findViewById(R.id.scene_verify_group);

        scene_verify_resolver_description = view.findViewById(R.id.scene_verify_resolver_description);
        resolver_progress_bar = view.findViewById(R.id.resolver_progress_bar);

        scene_verify_authorization = view.findViewById(R.id.scene_verify_authorization);
        scene_verify_error_description = view.findViewById(R.id.scene_verify_error_description);
        scene_verify_feedback_question =  view.findViewById(R.id.scene_verify_feedback_question);
        scene_verify_feedback_comments =  view.findViewById(R.id.scene_verify_feedback_comments);
        scene_verify_button_wrapper = view.findViewById(R.id.scene_verify_button_wrapper);
        scene_verify_button = view.findViewById(R.id.scene_verify_button);

        scene_verify_feedback_comments.setOnKeyboardListener(new KeyboardListener());
        scene_verify_button.setOnButtonClickListener(new SeparateShapesViewClickListener());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mAskFeedbackQuestions = sp.getBoolean(SettingsFragment.PREF_GENERAL_FEEDBACK, false);

        mAddActorSceneActionListener = new AddActorSceneActionListener();

        addInitialModelView(R.id.verify_screen_state_setup, setupModelView);
        addModelView(R.id.verify_screen_state_ask_questions, askQuestionsModelView);
        addModelView(R.id.verify_screen_state_resolve_voice, resolveVoiceModelView);
        addModelView(R.id.verify_screen_state_access_denied, accessDeniedModelView);
        addModelView(R.id.verify_screen_state_access_denied_complete, accessDeniedCompleteModelView);
        addModelView(R.id.verify_screen_state_access_granted, accessGrantedModelView);
        addModelView(R.id.verify_screen_state_access_denied_ask_feedback_questions, accessDeniedFeedbackQuestionsModelView);
        addModelView(R.id.verify_screen_state_access_denied_ask_feedback_questions_complete, accessDeniedFeedbackQuestionsCompleteModelView);
        addModelView(R.id.verify_screen_state_access_granted_ask_feedback_questions, accessGrantedFeedbackQuestionsModelView);
        addModelView(R.id.verify_screen_state_error_ask_feedback_questions, errorFeedbackQuestionsModelView);
        addModelView(R.id.verify_screen_state_error, errorModelView);

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

        // ensure the screen state is None or Setup,
        // if we come back in from the Used Apps button, we need to skip the
        // reset logic
        int modelViewId = getModelViewId();
        if(modelViewId == R.id.model_view_none || modelViewId == R.id.verify_screen_state_setup) {
            mLeftScreenDuringTransaction = false;

            getSceneController().getSceneLayout().addSceneActionListener(mAddActorSceneActionListener);

            requestPermission(1, Manifest.permission.RECORD_AUDIO, this);
        } else if(mLeftScreenDuringTransaction) {
            getModelViewHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    getSceneController().dispatchCollapse(R.id.actor_verify);
                }
            }, 950);
        }
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop");
        Context context = getContext();
        if(context != null) {
            mSpeechConnection.unbind(context);
        }
        callVerifyEnd(true);
     //   stopInteraction();
        getSceneController().getSceneLayout().removeSceneActionListener(mAddActorSceneActionListener);
        mAddActorSceneActionListener = null;
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

            mCurrentVerifyAttempt = 0;
            mHasRecalibrated = false;
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
                                    VerifyModelViewFragment.this);
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
        callVerifyStart();
    }

    @Override
    public void onSpeechServiceDisconnected() {

    }

    @Override
    public void onVoiceStart() {
        Log.d(TAG, "onVoiceStart");
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
        mVerifier.append(buffer, mSpeechContexts);
        mSpeechContexts = null;
        mAudioBuffer.clear();
        callUpdateQuestion();
        if(!callNextQuestion()) {
            callVerifyProcess();
        }
    }

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

    @Override
    public @IdRes int onOverrideModelView(@IdRes int fromViewId, @IdRes int toViewId) {

        if(toViewId == R.id.verify_screen_state_access_denied_not_enough_speech) {
            toViewId = R.id.verify_screen_state_access_denied;
            mNotEnoughSpeech = true;
        } else {
            mNotEnoughSpeech = false;
        }

        if(toViewId == R.id.verify_screen_state_access_denied) {
            if(++mCurrentVerifyAttempt >= mMaxVerifyAttempts) {
                toViewId = R.id.verify_screen_state_access_denied_complete;
            }
        }

        if(mAskFeedbackQuestions) {
            if(toViewId == R.id.verify_screen_state_access_denied) {
                toViewId = R.id.verify_screen_state_access_denied_ask_feedback_questions;
            } else if(toViewId == R.id.verify_screen_state_access_denied_complete) {
                toViewId = R.id.verify_screen_state_access_denied_ask_feedback_questions_complete;
            } else if(toViewId == R.id.verify_screen_state_access_granted) {
                toViewId = R.id.verify_screen_state_access_granted_ask_feedback_questions;
            } else if(toViewId == R.id.verify_screen_state_error) {
                toViewId = R.id.verify_screen_state_error_ask_feedback_questions;
            }
        }

        return toViewId;
    }

    @Override
    public void onChangingModelView(@IdRes int fromViewId, @IdRes int toViewId) {
        if(mVerifyViewGroupAdapter != null) {
            mVerifyViewGroupAdapter.clear();
        }
    }

    private boolean isAccessGranted() {
        @IdRes int modelViewId = getModelViewId();
        return modelViewId == R.id.verify_screen_state_access_granted ||
                modelViewId == R.id.verify_screen_state_access_granted_ask_feedback_questions;
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
                getModelViewHandler().postDelayed(new Runnable() {
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
                getModelViewHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_feedback_question.setText(R.string.scene_verify_feedback_question_background_noise);
                    }
                }, 1650);
                break;
            case Comments:
                @IdRes int modelViewId = getModelViewId();
                scene_verify_feedback_question.setText(null);
                if(modelViewId == R.id.verify_screen_state_access_denied_ask_feedback_questions) {
                    scene_verify_button.setSingleShape(false);
                    scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.scene_verify_again));
                    scene_verify_button.setRightShapeTitle(getResources().getString(R.string.scene_verify_cancel));
                    scene_verify_feedback_comments.setText(null);
                    scene_verify_feedback_comments.clearFocus();
                    getModelViewHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scene_verify_feedback_question.setText(R.string.scene_verify_feedback_question_comments);
                            scene_verify_feedback_comments.setVisibility(View.VISIBLE);
                            scene_verify_feedback_comments.clearFocus();
                        }
                    }, 1750);
                } else if(modelViewId == R.id.verify_screen_state_access_granted_ask_feedback_questions ||
                        modelViewId == R.id.verify_screen_state_access_denied_ask_feedback_questions_complete) {
                    scene_verify_button.setSingleShape(true);
                    scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
                    scene_verify_feedback_comments.setText(null);
                    scene_verify_feedback_comments.clearFocus();
                    getModelViewHandler().postDelayed(new Runnable() {
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
                getModelViewHandler().postDelayed(new Runnable() {
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
                break;
        }
    }

    ///
    /// UI Methods
    ///

    private void changeListeningStatus(final boolean isListening) {
        Log.d(TAG, "changeListeningStatus");
        getModelViewHandler().post(new Runnable() {
            @Override
            public void run() {
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
            Log.d(TAG, "callNextQuestion().nextQuestion");
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
                phrases.add(mChosenNumbers);
                mSpeechContexts.add(
                        q.getName(), q.getGrammar(),
                        mVerifyIntent.getLanguageCode(),
                        phrases
                );
                verifyData = new VerifyData(mChosenNumbers);
            } else {
                throw new IllegalArgumentException("FreeSpeech type not allowed");
            }
            mSpeechConnection.resume();
            getModelViewHandler().post(new Runnable() {
                @Override
                public void run() {
                    mVerifyViewGroupAdapter.add(verifyData);
                }
            });
            return true;
        }

        return false;
    }

    private void callUpdateQuestion() {
        Log.d(TAG, "callUpdateQuestion");
        if (!mIsVerifyCanceled) {
            if(!mQuestionsList.isLastQuestion(mQuestionMode)) {
                getModelViewHandler().post(new Runnable() {
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
        changeModelView(R.id.verify_screen_state_error, "setError() -> " + from);
    }

    ///
    /// Verify Methods
    ///

    private void callVerifyStart() {
        Log.d(TAG, "callVerifyStart");
        mVerifier.setMetaData("Silence-Threshold", mSpeechConnection.getSilenceAmplitudeThreshold());
        mVerifier.start(new SveVerifier.StartCallback() {
            @Override
            public void onStartComplete() {
                Log.d(TAG, "callVerifyStart().onStartComplete");
                getModelViewHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        mQuestionsList.reset();
                        mSpeechConnection.resume();
                        changeModelView(R.id.verify_screen_state_ask_questions, "callVerifyStart().onStartComplete");
                    }
                });
            }

            @Override
            public void onFailure(final Exception ex) {
                Log.d(TAG, "callVerifyStart().onFailure");
                ex.printStackTrace();
                getModelViewHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), ex.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        getSceneDirector().setHomeSceneActive();
                    }
                });
            }
        });
    }

    private void callVerifyProcess() {
        Log.d(TAG, "callVerifyProcess");
        if(mVerifier.isSessionOpen()) {
            changeModelView(R.id.verify_screen_state_resolve_voice, "callVerifyProcess()");
            mVerifier.post(new SveVerifier.PostCallback() {

                @Override
                public void onPostComplete(SveVerifier.Result result) {
                    if(result == SveVerifier.Result.PassIsAlive) {
                        callDbUpdate();
                        updateInteraction(SveVerifier.Result.Pass);
                    } else if(result == SveVerifier.Result.PassNotAlive) {
                        callDbUpdate();
                        changeModelView(R.id.verify_screen_state_access_denied, "callVerifyProcess().onPostComplete");
                        updateInteraction(SveVerifier.Result.Fail);
                    } else if(result == SveVerifier.Result.NeedMore ||
                            result == SveVerifier.Result.NeedMoreAlive ||
                            result == SveVerifier.Result.Ambiguous ||
                            result == SveVerifier.Result.AmbiguousIsAlive) {
                        mQuestionMode = QuestionSet.Mode.Question;
                        changeModelView(mQuestionsList.hasMoreSets()
                                        ? R.id.verify_screen_state_ask_questions
                                        : R.id.verify_screen_state_access_denied_not_enough_speech,
                                "callVerifyProcess().onPostComplete");
                    } else {
                        callDbUpdate();
                        changeModelView(R.id.verify_screen_state_access_denied, "callVerifyProcess().onPostComplete");
                        updateInteraction(SveVerifier.Result.Fail);
                    }
                }

                @Override
                public void onFailure(final Exception ex) {
                    Log.d(TAG, "callVerifyProcess().onFailure()");
                    ex.printStackTrace();
                    setError(ex.getLocalizedMessage(), "callVerifyProcess().onFailure()");
                }
            });
        }
    }

    private void callVerifyEnd(final boolean exitingFragment) {
        Log.d(TAG, "callVerifyEnd");
        if(mVerifier != null && mVerifier.isSessionOpen()) {
            if(exitingFragment) {
                mLeftScreenDuringTransaction = true;
            }
            mVerifier.end(new SveVerifier.EndCallback() {
                @Override
                public void onEndComplete(boolean isAuthorized) {
                    Log.d(TAG, "callVerifyEnd().onEndComplete()");
                }

                @Override
                public void onFailure(final Exception ex) {
                    Log.d(TAG, "callVerifyEnd().onEndComplete()");
                    ex.printStackTrace();
                    if(!exitingFragment) {
                        setError(ex.getLocalizedMessage(), "callVerifyEnd().onEndComplete()");
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
                        setError(ex.getLocalizedMessage(), "callVerifyCancel().onFailure");
                    }
                }
            });
            callDbUpdate();
        }
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

        if(mAskFeedbackQuestions) {
            mVerifyIntent.updateHistory(
                    getContractController(), result.name(), mVerifier.getSessionId(),
                    mChosenNumbers, "N/A", mFeedbackBreakAttempt, mFeedbackRecording,
                    mFeedbackBackgroundNoise, mFeedbackComments
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
            callVerifyCancel();
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

    private void updateInteraction(final SveVerifier.Result serverResult) {
        Log.d(TAG, "updateInteraction");
        CloudMessage message;
        switch(mVerifyIntent.getVerifyMode()) {
            case VerifyQrCode:
            case VerifyQrRequest:
            case VerifyGuest:
                VerifyIntent.VerifyInstance instance = mVerifyIntent.getInstances().get(0);
                message = CloudMessage.Update("v2access.Intent.{@v2w_interaction_id}");
                message.putString("v2w_interaction_id", instance.getQrIntent());
                message.putString("v2w_user_name", mVerifyIntent.getVoxidemUser());
                if(instance.getAccountId() != -1) {
                    message.putLong("v2w_account_id", instance.getAccountUserId());
                }
                message.putString("v2w_result", serverResult.name().toUpperCase().substring(0, 1));
                break;
            case VerifyDevice:
                message = CloudMessage.Update("v2access.Intents.{@v2w_user_name}");
                message.putString("v2w_user_name", mVerifyIntent.getVoxidemUser());
                message.putObject("v2w_interaction_ids", mVerifyIntent.getIntentIdList());
                message.putString("v2w_result", serverResult.name().toUpperCase().substring(0, 1));
                break;
            default:
                throw new IllegalArgumentException("Unknown verify mode");
        }

        message.send(new CloudController.ResponseCallback() {

            @Override
            public void onResult(final CloudResult result) {
                Log.d(TAG, "CallServerUpdate().onResult");
                if (result.hasData() && result.getData() instanceof UserLogin) {
                    UserLogin userLogin = (UserLogin) result.getData();
                    if (userLogin.isComplete() && userLogin.isSuccess()) {
                        mIsVerifyComplete = true;
                        changeModelView(R.id.verify_screen_state_access_granted, "CallServerUpdate().onResult");
                    }
                }
            }

            @Override
            public void onError(final CloudError error) {
                Log.d(TAG, "CallServerUpdate().onError");
                setError(error.getMessage(), "CallServerUpdate().onError");
            }

            @Override
            public void onFailure(final Exception ex) {
                Log.d(TAG, "CallServerUpdate().onFailure");
                ex.printStackTrace();
                setError(ex.getLocalizedMessage(), "CallServerUpdate().onFailure");
            }
        });
    }

    ///
    /// Model View Classes
    ///

    private ISceneModelView setupModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(@IdRes int fromViewId) {
            mAudioBuffer.clear();
            mIsVerifyCanceled = false;
            mIsVerifyComplete = false;
            mQuestionMode = QuestionSet.Mode.RequiredOnly;
            mFeedbackBreakAttempt = false;
            mFeedbackRecording = false;
            mFeedbackBackgroundNoise = false;
            mIsInteractionStopping = false;
            mNotEnoughSpeech = false;
            mFeedbackComments = "";

            if(mQuestionsList != null) {
                mQuestionsList.reset();
            }
        }

        @Override
        public void onOpenedModelView(@IdRes int fromViewId) {
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
            scene_verify_button.setVisibility(View.GONE);
            scene_verify_button_wrapper.setVisibility(View.GONE);

            if(mAskFeedbackQuestions) {
                scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.general_yes));
                scene_verify_button.setRightShapeTitle(getResources().getString(R.string.general_no));
            } else {
                scene_verify_button.setSingleShape(true);
                scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
            }
        }

        @Override
        public void onClosingModelView(@IdRes int toViewId) {

        }

        @Override
        public void onClosedModelView(@IdRes int toViewId) {

        }
    };

    private ISceneModelView askQuestionsModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(@IdRes int fromViewId) {
            mQuestionsList.nextSet();

            scene_verify_vv_logo.setVisibility(View.VISIBLE);
            scene_verify_resolver_description.setVisibility(View.GONE);
            resolver_progress_bar.setVisibility(View.GONE);
            scene_verify_authorization.setVisibility(View.GONE);
            scene_verify_error_description.setVisibility(View.GONE);
            scene_verify_feedback_question.setVisibility(View.GONE);
            scene_verify_feedback_comments.setVisibility(View.GONE);
            scene_verify_button.setVisibility(View.GONE);
            scene_verify_button_wrapper.setVisibility(View.GONE);
        }

        @Override
        public void onOpenedModelView(@IdRes int fromViewId) {
            if(fromViewId != R.id.verify_screen_state_resolve_voice) {
                scene_verify_listening.setVisibility(View.VISIBLE);
                scene_verify_group.setVisibility(View.VISIBLE);
                callNextQuestion();
            } else {
                getModelViewHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scene_verify_listening.setVisibility(View.VISIBLE);
                        scene_verify_group.setVisibility(View.VISIBLE);
                        callNextQuestion();
                    }
                }, 300);
            }
        }

        @Override
        public void onClosingModelView(@IdRes int toViewId) {

        }

        @Override
        public void onClosedModelView(@IdRes int toViewId) {

        }
    };

    private ISceneModelView resolveVoiceModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {
            scene_verify_vv_logo.setVisibility(View.VISIBLE);
            scene_verify_listening.setVisibility(View.GONE);
            scene_verify_group.setVisibility(View.GONE);
            scene_verify_resolver_description.setVisibility(View.VISIBLE);
            resolver_progress_bar.setVisibility(View.VISIBLE);
            scene_verify_authorization.setVisibility(View.GONE);
            scene_verify_error_description.setVisibility(View.GONE);
            scene_verify_feedback_question.setVisibility(View.GONE);
            scene_verify_feedback_comments.setVisibility(View.GONE);
            scene_verify_button.setVisibility(View.GONE);
            scene_verify_button_wrapper.setVisibility(View.GONE);
        }

        @Override
        public void onOpenedModelView(int fromViewId) {

        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    private ISceneModelView accessDeniedModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {
            if(!mHasRecalibrated) {
                getSceneController().dispatchBypassAction(R.id.actor_main, R.id.action_start_calibrating, null);
            } else {
                scene_verify_vv_logo.setVisibility(View.VISIBLE);
                scene_verify_listening.setVisibility(View.GONE);
                scene_verify_group.setVisibility(View.GONE);
                scene_verify_resolver_description.setVisibility(View.GONE);
                resolver_progress_bar.setVisibility(View.GONE);
                scene_verify_authorization.setText(!mNotEnoughSpeech ?
                        R.string.scene_verify_access_denied :
                        R.string.scene_verify_access_denied_not_enough_speech
                );
                scene_verify_authorization.setVisibility(View.VISIBLE);
                scene_verify_error_description.setVisibility(View.GONE);
                scene_verify_feedback_question.setVisibility(View.GONE);
                scene_verify_feedback_comments.setVisibility(View.GONE);
                scene_verify_button.setSingleShape(false);
                scene_verify_button.setLeftShapeTitle(getResources().getString(R.string.scene_verify_again));
                scene_verify_button.setRightShapeTitle(getResources().getString(R.string.scene_verify_cancel));

                if(!mLeftScreenDuringTransaction) {
                    VerifyInstructionsDialog dialog = VerifyInstructionsDialog.newInstance(getSceneController());
                    getSceneController().showDialogFragment(dialog, "VerifyInstructionsDialog");
                }
            }
        }

        @Override
        public void onOpenedModelView(int fromViewId) {
            scene_verify_button.setVisibility(View.VISIBLE);
            scene_verify_button_wrapper.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    private ISceneModelView accessDeniedCompleteModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {
            scene_verify_vv_logo.setVisibility(View.VISIBLE);
            scene_verify_listening.setVisibility(View.GONE);
            scene_verify_group.setVisibility(View.GONE);
            scene_verify_resolver_description.setVisibility(View.GONE);
            resolver_progress_bar.setVisibility(View.GONE);
            scene_verify_authorization.setText(!mNotEnoughSpeech ?
                    R.string.scene_verify_access_denied :
                    R.string.scene_verify_access_denied_not_enough_speech
            );
            scene_verify_authorization.setVisibility(View.VISIBLE);
            scene_verify_error_description.setVisibility(View.GONE);
            scene_verify_feedback_question.setVisibility(View.GONE);
            scene_verify_feedback_comments.setVisibility(View.GONE);
            scene_verify_button.setSingleShape(true);
            scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));

            if(!mLeftScreenDuringTransaction) {
                VerifyInstructionsDialog dialog = VerifyInstructionsDialog.newInstance(getSceneController());
                getSceneController().showDialogFragment(dialog, "VerifyInstructionsDialog");
            }
        }

        @Override
        public void onOpenedModelView(int fromViewId) {
            scene_verify_button.setVisibility(View.VISIBLE);
            scene_verify_button_wrapper.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    private ISceneModelView accessGrantedModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {

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
            scene_verify_button.setSingleShape(true);
            scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
        }

        @Override
        public void onOpenedModelView(int fromViewId) {
            if (mVerifyIntent.getVerifyMode() == VerifyIntent.VerifyMode.VerifyQrRequest) {
                getSceneDirector().setData(DevicesFragment.DEVICE_ID, mVerifyIntent.getDeviceQrId());
                getSceneDirector().setData(DevicesFragment.DEVICE_IP, mVerifyIntent.getDeviceQrIp());
                getSceneDirector().setData(DevicesFragment.DEVICE_INFO, mVerifyIntent.getDeviceQrInfo());
                getSceneController().dispatchExpand(R.id.actor_add);
                scene_verify_button.setVisibility(View.GONE);
                scene_verify_button_wrapper.setVisibility(View.GONE);
            } else {
                scene_verify_button.setVisibility(View.VISIBLE);
                scene_verify_button_wrapper.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    private ISceneModelView accessDeniedFeedbackQuestionsModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {

            scene_verify_vv_logo.setVisibility(View.VISIBLE);
            scene_verify_listening.setVisibility(View.GONE);
            scene_verify_group.setVisibility(View.GONE);
            scene_verify_resolver_description.setVisibility(View.GONE);
            resolver_progress_bar.setVisibility(View.GONE);
            scene_verify_authorization.setText(!mNotEnoughSpeech ?
                    R.string.scene_verify_access_denied :
                    R.string.scene_verify_access_denied_not_enough_speech
            );
            scene_verify_authorization.setVisibility(View.VISIBLE);
            scene_verify_error_description.setVisibility(View.GONE);

            if(!mLeftScreenDuringTransaction) {
                VerifyInstructionsDialog dialog = VerifyInstructionsDialog.newInstance(getSceneController());
                getSceneController().showDialogFragment(dialog, "VerifyInstructionsDialog");
            }

            setFeedbackState(FeedbackState.BreakAttempt, "accessDeniedFeedbackQuestionsModelView.onOpeningModelView()");
        }

        @Override
        public void onOpenedModelView(int fromViewId) {

        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    private ISceneModelView accessDeniedFeedbackQuestionsCompleteModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {

            scene_verify_vv_logo.setVisibility(View.VISIBLE);
            scene_verify_listening.setVisibility(View.GONE);
            scene_verify_group.setVisibility(View.GONE);
            scene_verify_resolver_description.setVisibility(View.GONE);
            resolver_progress_bar.setVisibility(View.GONE);
            scene_verify_authorization.setText(!mNotEnoughSpeech ?
                    R.string.scene_verify_access_denied :
                    R.string.scene_verify_access_denied_not_enough_speech
            );
            scene_verify_authorization.setVisibility(View.VISIBLE);
            scene_verify_error_description.setVisibility(View.GONE);

            if(!mLeftScreenDuringTransaction) {
                VerifyInstructionsDialog dialog = VerifyInstructionsDialog.newInstance(getSceneController());
                getSceneController().showDialogFragment(dialog, "VerifyInstructionsDialog");
            }

            setFeedbackState(FeedbackState.BreakAttempt, "accessDeniedFeedbackQuestionsModelView.onOpeningModelView()");
        }

        @Override
        public void onOpenedModelView(int fromViewId) {

        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    private ISceneModelView accessGrantedFeedbackQuestionsModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {
            scene_verify_vv_logo.setVisibility(View.VISIBLE);
            scene_verify_listening.setVisibility(View.GONE);
            scene_verify_group.setVisibility(View.GONE);
            scene_verify_resolver_description.setVisibility(View.GONE);
            resolver_progress_bar.setVisibility(View.GONE);
            scene_verify_authorization.setText(R.string.scene_verify_access_granted);
            scene_verify_authorization.setVisibility(View.VISIBLE);
            scene_verify_error_description.setVisibility(View.GONE);
        }

        @Override
        public void onOpenedModelView(int fromViewId) {
            if (mVerifyIntent.getVerifyMode() == VerifyIntent.VerifyMode.VerifyQrRequest) {
                    getSceneDirector().setData(DevicesFragment.DEVICE_ID, mVerifyIntent.getDeviceQrId());
                    getSceneDirector().setData(DevicesFragment.DEVICE_IP, mVerifyIntent.getDeviceQrIp());
                    getSceneDirector().setData(DevicesFragment.DEVICE_INFO, mVerifyIntent.getDeviceQrInfo());
                    getSceneController().dispatchExpand(R.id.actor_add);
                    scene_verify_button.setVisibility(View.GONE);
                    scene_verify_button_wrapper.setVisibility(View.GONE);
            } else {
                setFeedbackState(FeedbackState.BreakAttempt, "accessGrantedFeedbackQuestionsModelView.onOpenedModelView()");
            }
        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    private ISceneModelView errorFeedbackQuestionsModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {
            scene_verify_vv_logo.setVisibility(View.VISIBLE);
            scene_verify_listening.setVisibility(View.GONE);
            scene_verify_group.setVisibility(View.GONE);
            scene_verify_resolver_description.setVisibility(View.GONE);
            resolver_progress_bar.setVisibility(View.GONE);
            scene_verify_authorization.setText(R.string.scene_verify_access_denied_error);
            scene_verify_authorization.setVisibility(View.VISIBLE);
            scene_verify_error_description.setVisibility(View.GONE);
            setFeedbackState(FeedbackState.Error, "errorFeedbackQuestionsModelView.onOpeningModelView()");
        }

        @Override
        public void onOpenedModelView(int fromViewId) {

        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    private ISceneModelView errorModelView = new ISceneModelView() {

        @Override
        public void onOpeningModelView(int fromViewId) {
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
            scene_verify_button.setSingleShape(true);
            scene_verify_button.setCenterShapeTitle(getResources().getString(R.string.general_done));
        }

        @Override
        public void onOpenedModelView(int fromViewId) {
            scene_verify_button.setVisibility(View.VISIBLE);
            scene_verify_button_wrapper.setVisibility(View.VISIBLE);
        }

        @Override
        public void onClosingModelView(int toViewId) {

        }

        @Override
        public void onClosedModelView(int toViewId) {

        }
    };

    ///
    /// KeyboardListener Class
    ///

    private class KeyboardListener implements KeyboardEditText.KeyboardListener {

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
    /// SeparateShapesViewClickListener Class
    ///

    private class SeparateShapesViewClickListener implements SeparateShapesView.OnButtonClickListener {

        @Override
        public boolean onLeftButtonClick() {
            if(mAskFeedbackQuestions) {
                onLeftButtonClickFeedback();
            } else {
                callVerifyEnd(false);
                getModelViewHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changeModelView(R.id.verify_screen_state_setup, "onLeftButtonClick");
                        callVerifyStart();
                    }
                }, 950);
            }
            return true;
        }

        @Override
        public boolean onRightButtonClick() {
            if(mAskFeedbackQuestions) {
                onRightButtonClickFeedback();
            } else {
                callVerifyEnd(false);
                getModelViewHandler().postDelayed(new Runnable() {
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
            if(mAskFeedbackQuestions) {
                onMiddleButtonClickFeedback();
            } else {
                callVerifyEnd(true);
                setFeedbackState(FeedbackState.Done, "onMiddleButtonClick()");
                getModelViewHandler().postDelayed(new Runnable() {
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
                    mFeedbackBreakAttempt = true;
                    setFeedbackState(isAccessGranted() ? FeedbackState.Recording : FeedbackState.Comments, "onLeftButtonClickFeedback()");
                    break;
                case Recording:
                    mFeedbackRecording = true;
                    setFeedbackState(FeedbackState.BackgroundNoise, "onLeftButtonClickFeedback()");
                    break;
                case BackgroundNoise:
                    mFeedbackBackgroundNoise = true;
                    setFeedbackState(FeedbackState.Comments, "onLeftButtonClickFeedback()");
                    break;
                case Comments:
                    mFeedbackComments = scene_verify_feedback_comments.getText().toString();
                    mVerifier.setFeedback(
                            mFeedbackBreakAttempt,
                            mFeedbackRecording,
                            mFeedbackBackgroundNoise,
                            mFeedbackComments
                    );
                    callVerifyEnd(false);
                    setFeedbackState(FeedbackState.Done, "onLeftButtonClickFeedback()");
                    getModelViewHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            changeModelView(R.id.verify_screen_state_setup, "onLeftButtonClickFeedback");
                            callVerifyStart();
                        }
                    }, 950);
                    break;
            }
        }

        private void onRightButtonClickFeedback() {
            switch(mFeedbackState) {
                case BreakAttempt:
                    mFeedbackBreakAttempt = false;
                    setFeedbackState(isAccessGranted() ? FeedbackState.Comments : FeedbackState.BackgroundNoise, "onRightButtonClickFeedback()");
                    break;
                case Recording:
                    mFeedbackRecording = false;
                    setFeedbackState(FeedbackState.BackgroundNoise, "onRightButtonClickFeedback()");
                    break;
                case BackgroundNoise:
                    mFeedbackBackgroundNoise = false;
                    setFeedbackState(FeedbackState.Comments, "onRightButtonClickFeedback()");
                    break;
                case Comments:
                    mFeedbackComments = scene_verify_feedback_comments.getText().toString();
                    mVerifier.setFeedback(
                            mFeedbackBreakAttempt,
                            mFeedbackRecording,
                            mFeedbackBackgroundNoise,
                            mFeedbackComments
                    );
                    callVerifyEnd(true);
                    setFeedbackState(FeedbackState.Done, "onRightButtonClickFeedback()");
                    getModelViewHandler().postDelayed(new Runnable() {
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
                    mFeedbackComments = scene_verify_feedback_comments.getText().toString();
                    mVerifier.setFeedback(
                            mFeedbackBreakAttempt,
                            mFeedbackRecording,
                            mFeedbackBackgroundNoise,
                            mFeedbackComments
                    );
                    callVerifyEnd(true);
                    setFeedbackState(FeedbackState.Done, "onMiddleButtonClickFeedback()");
                    getModelViewHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getSceneController().dispatchCollapse(R.id.actor_verify);
                        }
                    }, 950);
                    break;
            }
        }

    }

    ///
    /// AddActorSceneActionListener Class
    ///

    private class AddActorSceneActionListener extends SceneLayout.SimpleSceneActionListener {
        @Override
        public void onActorClosed(int actorId) {
            if(actorId == R.id.actor_add) {
                getModelViewHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        @IdRes int modelViewId = getModelViewId();
                        if(modelViewId == R.id.verify_screen_state_access_granted) {
                            scene_verify_button.setVisibility(View.VISIBLE);
                            scene_verify_button_wrapper.setVisibility(View.VISIBLE);
                        } else if(modelViewId == R.id.verify_screen_state_access_granted_ask_feedback_questions) {
                            setFeedbackState(FeedbackState.BreakAttempt, "AddActorSceneActionListener.onActorClosed()");
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

    private void assertNotNull(Object object) {
        if(object == null) {
            throw new AssertionError();
        }
    }
}
