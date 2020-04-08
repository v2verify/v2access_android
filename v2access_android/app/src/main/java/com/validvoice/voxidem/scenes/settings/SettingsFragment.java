package com.validvoice.voxidem.scenes.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.dynamic.scene.listeners.OnItemWasSelectedListener;
import com.validvoice.dynamic.speech.SpeechApi;
import com.validvoice.dynamic.speech.service.AdaptiveSpeechServiceConnection;
import com.validvoice.dynamic.voice.VoiceRecorder;
import com.validvoice.voxidem.BuildConfig;
import com.validvoice.voxidem.MainActivity;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.cloud.ControlKeys;
import com.validvoice.voxidem.db.contracts.DevicesContract;
import com.validvoice.voxidem.db.contracts.HistoryContract;

public class SettingsFragment extends SceneFragment {
    private static final String TAG = "SettingsFragment";

    public static final String PREF_GENERAL_FEEDBACK = "pref_general_feedback";

    public static final String PREF_LOGOFF_SENSITIVITY = "pref_logoff_sensitivity";

    public static final String PREF_GENERAL_DARK_MODE = "pref_general_DARK_MODE";

    public static final String PREF_GENERAL_SENSITIVITY_OPTION = "pref_general_sensitivity_option";

    public static final String PREF_GENERAL_QUESTION_TONE = "pref_general_question_tone";

    public static final String PREF_GENERAL_SPEECH_TO_TEXT = "pref_general_speech_to_text";


    private TextView settings_user_account_name;
    private Button settings_user_logout;

    private CheckBox settings_general_animations;
    private CheckBox settings_general_feedback;
    private CheckBox settings_general_noise_cancellation;
    private TextView settings_logoff_sensitivity;
    private SeekBar settings_logoff_sensitivity_bar;
    private CheckBox settings_general_question_tone;
    private CheckBox settings_general_speech_to_text;
    private CheckBox settings_general_sensitivity_option;


    private TextView settings_application_version;

    private CheckBox settings_debugging_adaptive_microphone;
    private Button settings_debugging_re_enroll;
    private Button settings_debugging_calibrate;
    private Button settings_debugging_calibration_reset;
    private CheckBox settings_debugging_lead_in;
    private Spinner settings_debugging_lead_in_millis;
    private TextView settings_debugging_silence_threshold;
    private Button settings_debugging_clear_accounts;
    private Button settings_debugging_clear_devices;
    private Button settings_debugging_clear_history;
    private Button settings_debugging_card_history;
    private CheckBox settings_general_enable_darkmode;
    private Button settings_language_select;
    private Button settings_companyId_select;
    private LinearLayout mStartPage;
    private LinearLayout mCompanyInfo;
    private TextInputEditText mCompanyId;
    private TextInputLayout mCompanyIdLayout;


    private Button settings_application_privacy_policy;

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settings_user_account_name = view.findViewById(R.id.settings_user_account_name);
        settings_user_logout = view.findViewById(R.id.settings_user_logout);

        settings_general_animations = view.findViewById(R.id.settings_general_animations);
        settings_general_sensitivity_option = view.findViewById(R.id.settings_general_sensitivity_option);
        settings_general_noise_cancellation = view.findViewById(R.id.settings_general_noise_cancellation);

        settings_logoff_sensitivity = view.findViewById(R.id.settings_logoff_sensitivity_threshold);
        settings_logoff_sensitivity_bar = view.findViewById(R.id.settings_logoff_sensitivity_bar);
        settings_general_question_tone = view.findViewById(R.id.settings_general_question_tone);
        settings_general_speech_to_text = view.findViewById(R.id.settings_general_speech_to_text);
        settings_debugging_card_history = view.findViewById(R.id.settings_card);
        settings_language_select = view.findViewById(R.id.settings_language_select);


        mStartPage = view.findViewById(R.id.settings_account);
        mCompanyInfo = view.findViewById(R.id.comp_info);
        mCompanyId = view.findViewById(R.id.scene_user_company_id);
        mCompanyIdLayout = view.findViewById(R.id.scene_user_company_id_layout);
        settings_companyId_select =  view.findViewById(R.id.settings_companyId_select);

        ((MainActivity) getActivity()).spanTitleBar("Settings");




        settings_companyId_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStartPage.setVisibility(View.GONE);
                mCompanyInfo.setVisibility(View.VISIBLE);
            }
        });


        view.findViewById(R.id.company_info_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ((mCompanyId.getText().toString().isEmpty())){
                    return;
                }
                SpeechApi.Builder builder = new SpeechApi.Builder(getActivity());
                builder.setDeveloperKey("89D8663D-E93B-H876-71B2-E85B39744EAF")
                        .setApplicationKey("2B9C4343-90BB-W2F0-1F41-EF7B39EF9C10")
                        .setApplicationVersion(BuildConfig.VERSION_NAME)
                        .setServer(getString(R.string.sve_server))
                        .setTransportProtocol(SpeechApi.TransportProtocol.REST);
                SpeechApi.initialize(builder.build());

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
                                mStartPage.setVisibility(View.VISIBLE);
                                mCompanyId.setVisibility(View.GONE);
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

        if(BuildConfig.FLAVOR.contains("local")) {
            settings_general_feedback = view.findViewById(R.id.settings_general_feedback);
            settings_general_feedback.setVisibility(View.VISIBLE);
            LinearLayout settings_debug_frame = view.findViewById(R.id.settings_debug_frame);
        //      settings_debug_frame.setVisibility(View.VISIBLE);
            if (VoxidemPreferences.isUserComplete()) {
                TextView settings_debug_voice_print_id = view.findViewById(R.id.settings_debug_voice_print_id);
                settings_debug_voice_print_id.setText(String.format(
                        getString(R.string.scene_settings_debugging_voice_print_id),
                        VoxidemPreferences.getUserVoicePrintId()));
            }
            settings_debugging_adaptive_microphone = view.findViewById(R.id.settings_adaptive_microphone);
            settings_general_enable_darkmode = view.findViewById(R.id.settings_general_dark_mode);
            settings_debugging_re_enroll = view.findViewById(R.id.settings_re_enroll);
            settings_debugging_lead_in = view.findViewById(R.id.settings_capture_lead_in_speech);
            settings_debugging_lead_in_millis = view.findViewById(R.id.settings_capture_lead_in_speech_millis);
            settings_debugging_clear_accounts = view.findViewById(R.id.settings_clear_accounts);
            settings_debugging_clear_devices = view.findViewById(R.id.settings_clear_devices);
            settings_debugging_clear_history = view.findViewById(R.id.settings_clear_history);
        } else if(BuildConfig.FLAVOR.contains("dev") ||
                BuildConfig.FLAVOR.contains("demo") ||
                BuildConfig.FLAVOR.contains("demo_test")) {
            settings_general_feedback = view.findViewById(R.id.settings_general_feedback);
         //   settings_general_feedback.setVisibility(View.VISIBLE);
            settings_general_enable_darkmode = view.findViewById(R.id.settings_general_dark_mode);

            LinearLayout settings_debug_frame = view.findViewById(R.id.settings_debug_frame);
        //    settings_debug_frame.setVisibility(View.VISIBLE);
            settings_debugging_re_enroll = view.findViewById(R.id.settings_re_enroll);
        }

        settings_debugging_calibrate = view.findViewById(R.id.settings_calibrate);
        settings_debugging_calibration_reset = view.findViewById(R.id.settings_calibration_reset);
        settings_debugging_silence_threshold = view.findViewById(R.id.settings_silence_threshold);

        if(BuildConfig.FLAVOR.contains("dev")) {
            settings_debugging_adaptive_microphone = view.findViewById(R.id.settings_adaptive_microphone);
            settings_debugging_lead_in = view.findViewById(R.id.settings_capture_lead_in_speech);
            settings_debugging_lead_in_millis = view.findViewById(R.id.settings_capture_lead_in_speech_millis);
        }


        settings_application_version = view.findViewById(R.id.settings_application_version);

        settings_application_privacy_policy = view.findViewById(R.id.settings_application_privacy_policy);

        if(settings_debugging_lead_in_millis != null) {
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                    container.getContext(),
                    R.array.scene_settings_debugging_capture_lead_in_speech_millis_array,
                    android.R.layout.simple_spinner_item
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            settings_debugging_lead_in_millis.setAdapter(adapter);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = sp.edit();

        settings_user_account_name.setText(VoxidemPreferences.getUserAccountName());

        settings_user_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getContractController().clear(AccountsContract.CONTENT_URI);
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
        });


        settings_debugging_card_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.card_info_layout);
                dialog.setTitle("Card Information");

                Button dialogButton =  dialog.findViewById(R.id.save_card_info);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
            }
        });

        if(BuildConfig.FLAVOR.contains("local") ||
                BuildConfig.FLAVOR.contains("dev")) {

            final boolean debugAdaptiveMic = sp.getBoolean(AdaptiveSpeechServiceConnection.PREF_ADAPTIVE_MICROPHONE, false);
            settings_debugging_adaptive_microphone.setChecked(debugAdaptiveMic);
            if (debugAdaptiveMic) {
                settings_debugging_adaptive_microphone.setText(String.format(getString(R.string.scene_settings_debugging_adaptive_microphone), getString(R.string.scene_settings_general_enabled)));
            } else {
                settings_debugging_adaptive_microphone.setText(String.format(getString(R.string.scene_settings_debugging_adaptive_microphone), getString(R.string.scene_settings_general_disabled)));
            }

            settings_debugging_adaptive_microphone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        settings_debugging_adaptive_microphone.setText(String.format(getString(R.string.scene_settings_debugging_adaptive_microphone), getString(R.string.scene_settings_general_enabled)));
                    } else {
                        settings_debugging_adaptive_microphone.setText(String.format(getString(R.string.scene_settings_debugging_adaptive_microphone), getString(R.string.scene_settings_general_disabled)));
                    }
                    editor.putBoolean(AdaptiveSpeechServiceConnection.PREF_ADAPTIVE_MICROPHONE, isChecked).apply();
                }
            });

            final int debugLeadInSpeech = sp.getInt(VoiceRecorder.PREF_LEAD_IN_MILLIS, 0);
            settings_debugging_lead_in.setChecked(debugLeadInSpeech > 0);

            settings_debugging_lead_in_millis.setEnabled(debugLeadInSpeech != 0);
            settings_debugging_lead_in_millis.setSelection(leadInMillisToOption(debugLeadInSpeech));

            if (debugLeadInSpeech > 0) {
                settings_debugging_lead_in.setText(String.format(getString(R.string.scene_settings_debugging_capture_lead_in_speech), getString(R.string.scene_settings_general_enabled)));
            } else {
                settings_debugging_lead_in.setText(String.format(getString(R.string.scene_settings_debugging_capture_lead_in_speech), getString(R.string.scene_settings_general_disabled)));
            }

            settings_debugging_lead_in.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        settings_debugging_lead_in.setText(String.format(getString(R.string.scene_settings_debugging_capture_lead_in_speech), getString(R.string.scene_settings_general_enabled)));
                        editor.putInt(VoiceRecorder.PREF_LEAD_IN_MILLIS, VoiceRecorder.DEFAULT_SPEECH_LEAD_IN_MILLIS).apply();
                    } else {
                        settings_debugging_lead_in.setText(String.format(getString(R.string.scene_settings_debugging_capture_lead_in_speech), getString(R.string.scene_settings_general_disabled)));
                        editor.remove(VoiceRecorder.PREF_LEAD_IN_MILLIS).apply();
                    }
                    settings_debugging_lead_in_millis.setEnabled(isChecked);
                    settings_debugging_lead_in_millis.setSelection(0);
                }
            });

            settings_debugging_lead_in_millis.setOnItemSelectedListener(new OnItemWasSelectedListener(settings_debugging_lead_in_millis) {
                @Override
                public void onItemWasSelected(AdapterView<?> parent, View view, int position, long id) {
                    editor.putInt(VoiceRecorder.PREF_LEAD_IN_MILLIS, leadInOptionToMillis(position)).apply();
                }
            });
        }

        if(BuildConfig.FLAVOR.contains("local") ||
                BuildConfig.FLAVOR.contains("dev") ||
                BuildConfig.FLAVOR.contains("demo_test")) {

            final boolean generalFeedback = sp.getBoolean(PREF_GENERAL_FEEDBACK, false);
            settings_general_feedback.setChecked(generalFeedback);
            if (generalFeedback) {
                settings_general_feedback.setText(String.format(getString(R.string.scene_settings_general_feedback), getString(R.string.scene_settings_general_enabled)));
            } else {
                settings_general_feedback.setText(String.format(getString(R.string.scene_settings_general_feedback), getString(R.string.scene_settings_general_disabled)));
            }

            settings_general_feedback.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        settings_general_feedback.setText(String.format(getString(R.string.scene_settings_general_feedback), getString(R.string.scene_settings_general_enabled)));
                    } else {
                        settings_general_feedback.setText(String.format(getString(R.string.scene_settings_general_feedback), getString(R.string.scene_settings_general_disabled)));
                    }
                    editor.putBoolean(PREF_GENERAL_FEEDBACK, isChecked).apply();
                }
            });
        }


        final boolean generalDarkMode = sp.getBoolean(PREF_GENERAL_DARK_MODE, false);
        settings_general_enable_darkmode.setChecked(generalDarkMode);
        if (generalDarkMode) {
            settings_general_enable_darkmode.setText(String.format(getString(R.string.scene_settings_general_dark_mode), getString(R.string.scene_settings_general_enabled)));
        } else {
            settings_general_enable_darkmode.setText(String.format(getString(R.string.scene_settings_general_dark_mode), getString(R.string.scene_settings_general_disabled)));
        }

        settings_general_enable_darkmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    settings_general_enable_darkmode.setText(String.format(getString(R.string.scene_settings_general_dark_mode), getString(R.string.scene_settings_general_enabled)));
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    settings_general_enable_darkmode.setText(String.format(getString(R.string.scene_settings_general_dark_mode), getString(R.string.scene_settings_general_disabled)));
                }
                editor.putBoolean(PREF_GENERAL_DARK_MODE, isChecked).apply();
                restartApplication();

            }
        });


        final boolean generalSensitivityOption = sp.getBoolean(PREF_GENERAL_SENSITIVITY_OPTION, false);
        settings_general_sensitivity_option.setChecked(generalSensitivityOption);
        if (generalSensitivityOption) {
            settings_logoff_sensitivity.setVisibility(View.VISIBLE);
            settings_logoff_sensitivity_bar.setVisibility(View.VISIBLE);
            settings_general_sensitivity_option.setText(String.format(getString(R.string.scene_settings_general_sensitivity_option), getString(R.string.scene_settings_general_enabled)));
        } else {
            settings_logoff_sensitivity.setVisibility(View.GONE);
            settings_logoff_sensitivity_bar.setVisibility(View.GONE);
            settings_general_sensitivity_option.setText(String.format(getString(R.string.scene_settings_general_sensitivity_option), getString(R.string.scene_settings_general_disabled)));
        }

        settings_general_sensitivity_option.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings_logoff_sensitivity.setVisibility(View.VISIBLE);
                    settings_logoff_sensitivity_bar.setVisibility(View.VISIBLE);
                    settings_general_sensitivity_option.setText(String.format(getString(R.string.scene_settings_general_sensitivity_option), getString(R.string.scene_settings_general_enabled)));
                } else {
                    settings_logoff_sensitivity.setVisibility(View.GONE);
                    settings_logoff_sensitivity_bar.setVisibility(View.GONE);
                    settings_general_sensitivity_option.setText(String.format(getString(R.string.scene_settings_general_sensitivity_option), getString(R.string.scene_settings_general_disabled)));
                }
                editor.putBoolean(PREF_GENERAL_SENSITIVITY_OPTION, isChecked).apply();
            }
        });


        final boolean generalQuestionTone = sp.getBoolean(PREF_GENERAL_QUESTION_TONE, false);
        settings_general_question_tone.setChecked(generalQuestionTone);
        if (generalQuestionTone) {
            settings_general_question_tone.setText(String.format(getString(R.string.scene_settings_general_question_tone), getString(R.string.scene_settings_general_enabled)));
        } else {
            settings_general_question_tone.setText(String.format(getString(R.string.scene_settings_general_question_tone), getString(R.string.scene_settings_general_disabled)));
        }

        settings_general_question_tone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    settings_general_question_tone.setText(String.format(getString(R.string.scene_settings_general_question_tone), getString(R.string.scene_settings_general_enabled)));
                } else {
                    settings_general_question_tone.setText(String.format(getString(R.string.scene_settings_general_question_tone), getString(R.string.scene_settings_general_disabled)));
                }
                editor.putBoolean(PREF_GENERAL_QUESTION_TONE, isChecked).apply();
            }
        });


        settings_debugging_re_enroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Dialog dialog = new Dialog(getContext());
                dialog.setContentView(R.layout.training_token_layout);
                Button dialogButton =  dialog.findViewById(R.id.start_enroll_process);
                final TextInputEditText tokenField = dialog.findViewById(R.id.scene_user_enroll_token);
                dialogButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String trainingToken = tokenField.getText().toString();
                        VoxidemPreferences.setTrainerPin(trainingToken);
                        dispatchExpand(R.id.actor_enroll);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        settings_language_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });


        final boolean generalSpeechToText = sp.getBoolean(PREF_GENERAL_SPEECH_TO_TEXT, false);
        settings_general_speech_to_text.setChecked(generalSpeechToText);
        if (generalSpeechToText) {
            settings_general_speech_to_text.setText(String.format(getString(R.string.scene_settings_general_speech_to_text), getString(R.string.scene_settings_general_enabled)));
        } else {
            settings_general_speech_to_text.setText(String.format(getString(R.string.scene_settings_general_speech_to_text), getString(R.string.scene_settings_general_disabled)));
        }

        settings_general_speech_to_text.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle(R.string.scene_verify_instructions_speech_tip);
                    alertDialogBuilder.setMessage(R.string.scene_setting_disability_feature);
                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setPositiveButton(R.string.understand,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    settings_general_speech_to_text.setText(String.format(getString(R.string.scene_settings_general_speech_to_text), getString(R.string.scene_settings_general_enabled)));
                                }
                            }
                    );

                    alertDialogBuilder.create();
                    alertDialogBuilder.show();
                } else {
                    settings_general_speech_to_text.setText(String.format(getString(R.string.scene_settings_general_speech_to_text), getString(R.string.scene_settings_general_disabled)));
                }
                editor.putBoolean(PREF_GENERAL_SPEECH_TO_TEXT, isChecked).apply();
            }
        });




        final boolean themeAnimations = sp.getBoolean(SceneLayout.PREF_ALLOW_THEME_ANIMATIONS, false);
        settings_general_animations.setChecked(themeAnimations);
        if(themeAnimations) {
            settings_general_animations.setText(String.format(getString(R.string.scene_settings_general_animations), getString(R.string.scene_settings_general_enabled)));
        } else {
            settings_general_animations.setText(String.format(getString(R.string.scene_settings_general_animations), getString(R.string.scene_settings_general_disabled)));
        }

        settings_general_animations.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    settings_general_animations.setText(String.format(getString(R.string.scene_settings_general_animations), getString(R.string.scene_settings_general_enabled)));
                } else {
                    settings_general_animations.setText(String.format(getString(R.string.scene_settings_general_animations), getString(R.string.scene_settings_general_disabled)));
                }
                editor.putBoolean(SceneLayout.PREF_ALLOW_THEME_ANIMATIONS, isChecked).apply();
            }
        });

        final boolean general_noise_cancellation = sp.getBoolean(VoiceRecorder.PREF_NOISE_CANCELLATION, true);
        settings_general_noise_cancellation.setChecked(general_noise_cancellation);
        if (general_noise_cancellation) {
            settings_general_noise_cancellation.setText(String.format(getString(R.string.scene_settings_general_noise_cancellation), getString(R.string.scene_settings_general_enabled)));
        } else {
            settings_general_noise_cancellation.setText(String.format(getString(R.string.scene_settings_general_noise_cancellation), getString(R.string.scene_settings_general_disabled)));
        }

        settings_general_noise_cancellation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int silenceThreshold;
                if (isChecked) {
                    silenceThreshold = sp.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD);
                    settings_general_noise_cancellation.setText(String.format(getString(R.string.scene_settings_general_noise_cancellation), getString(R.string.scene_settings_general_enabled)));
                } else {
                    silenceThreshold = sp.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_SILENCE_THRESHOLD);
                    settings_general_noise_cancellation.setText(String.format(getString(R.string.scene_settings_general_noise_cancellation), getString(R.string.scene_settings_general_disabled)));
                }
                settings_debugging_silence_threshold.setText(
                        String.format(getString(R.string.scene_settings_debugging_silence_threshold), "" + silenceThreshold)
                );
                editor.putBoolean(VoiceRecorder.PREF_NOISE_CANCELLATION, isChecked).apply();
            }
        });

        int logoffSensitivity = sp.getInt(PREF_LOGOFF_SENSITIVITY, 0);
        switch(logoffSensitivity) {
            case 0:
                settings_logoff_sensitivity.setText(String.format(getString(R.string.scene_settings_logoff_sensitivity), getString(R.string.scene_settings_logoff_sensitivity_low)));
                break;
            case 1:
                settings_logoff_sensitivity.setText(String.format(getString(R.string.scene_settings_logoff_sensitivity), getString(R.string.scene_settings_logoff_sensitivity_medium)));
                break;
            case 2:
                settings_logoff_sensitivity.setText(String.format(getString(R.string.scene_settings_logoff_sensitivity), getString(R.string.scene_settings_logoff_sensitivity_high)));
                break;
        }
        settings_logoff_sensitivity_bar.setProgress(logoffSensitivity);

        settings_logoff_sensitivity_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
               sp.edit().putInt(PREF_LOGOFF_SENSITIVITY, progress).apply();
                switch(progress) {
                    case 0:
                        settings_logoff_sensitivity.setText(String.format(getString(R.string.scene_settings_logoff_sensitivity), getString(R.string.scene_settings_logoff_sensitivity_low)));
                        break;
                    case 1:
                        settings_logoff_sensitivity.setText(String.format(getString(R.string.scene_settings_logoff_sensitivity), getString(R.string.scene_settings_logoff_sensitivity_medium)));
                        break;
                    case 2:
                        settings_logoff_sensitivity.setText(String.format(getString(R.string.scene_settings_logoff_sensitivity), getString(R.string.scene_settings_logoff_sensitivity_high)));
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        settings_debugging_calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchExpand(R.id.actor_calibrate);
            }
        });

        if(sp.contains(VoiceRecorder.PREF_SILENCE_THRESHOLD)) {
            settings_debugging_calibration_reset.setVisibility(View.VISIBLE);
        }

        settings_debugging_calibration_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.remove(VoiceRecorder.PREF_SILENCE_THRESHOLD).apply();
                settings_debugging_calibration_reset.setVisibility(View.GONE);
            }
        });

        final int silenceThreshold;
        if(!general_noise_cancellation) {
            silenceThreshold = sp.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_SILENCE_THRESHOLD);
        } else {
            silenceThreshold = sp.getInt(VoiceRecorder.PREF_SILENCE_THRESHOLD, VoiceRecorder.DEFAULT_NOISE_CANCELLATION_SILENCE_THRESHOLD);
        }
        settings_debugging_silence_threshold.setText(
                String.format(getString(R.string.scene_settings_debugging_silence_threshold), "" + silenceThreshold)
        );

        if(BuildConfig.FLAVOR.contains("local")) {

            settings_debugging_clear_accounts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //getContractController().clear(AccountsContract.CONTENT_URI);
                }
            });

            settings_debugging_clear_devices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContractController().clear(DevicesContract.CONTENT_URI);
                }
            });

            settings_debugging_clear_history.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getContractController().clear(HistoryContract.CONTENT_URI);
                }
            });
        }

        settings_application_version.setText(BuildConfig.VERSION_NAME);

        settings_application_privacy_policy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.scene_settings_privacy_policy_url))));
            }
        });

    }

    private int leadInMillisToOption(int millis) {
        switch(millis) {
            case 0: return 0;
            case 100: return 0;
            case 150: return 1;
            case 200: return 2;
            case 250: return 3;
            case 300: return 4;
            case 350: return 5;
            case 400: return 6;
            case 450: return 7;
            case 500: return 8;
        }
        return 0;
    }

    private int leadInOptionToMillis(int option) {
        switch(option) {
            case 0: return 100;
            case 1: return 150;
            case 2: return 200;
            case 3: return 250;
            case 4: return 300;
            case 5: return 350;
            case 6: return 400;
            case 7: return 450;
            case 8: return 500;
        }
        return 100;
    }

    public void restartApplication(){
        new Handler().post(new Runnable() {

            @Override
            public void run()
            {
                Intent intent = getActivity().getIntent();
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                getActivity().overridePendingTransition(0, 0);
                getActivity().finish();
                getActivity().overridePendingTransition(0, 0);
                startActivity(intent);
            }
        });
    }

}
