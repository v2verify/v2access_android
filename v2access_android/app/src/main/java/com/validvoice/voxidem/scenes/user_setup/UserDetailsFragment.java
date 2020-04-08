package com.validvoice.voxidem.scenes.user_setup;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneFragment;
import com.validvoice.dynamic.speech.authorization.SveEnroller;
import com.validvoice.voxidem.R;
import com.validvoice.voxidem.VoxidemPreferences;
import com.validvoice.voxidem.cloud.UserError;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.regex.Pattern;

public class UserDetailsFragment extends SceneFragment {

    private static final String TAG = "UserDetailsFragment";

    private View mView;

    private TextInputEditText mFirstName;
    private TextInputLayout mFirstNameLayout;

    private TextInputEditText mLastName;
    private TextInputLayout mLastNameLayout;

    private TextInputEditText mUserName;
    private TextInputLayout mUserNameLayout;

    private TextInputEditText mPrimaryPhone;
    private TextInputLayout mPrimaryPhoneLayout;

    private TextInputEditText mEmailAddress;
    private TextInputLayout mEmailAddressLayout;

    private TextInputEditText mTrainerPin;
    private TextInputLayout mTrainerPinLayout;

    private MaterialBetterSpinner mLanguageSelector;
    private MaterialBetterSpinner mGenderSelector;

    private String[] mLanguages;
    private String[] mLanguageCodes;

    private String[] mGender;
    private String[] mGenderCodes;

    private LinearLayout mTokenPage;
    private LinearLayout mUserDetails;



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(mView != null) return mView;

        mView = inflater.inflate(R.layout.fragment_user_details, container, false);

        mFirstName = mView.findViewById(R.id.scene_user_new_first_name);
        mFirstNameLayout = mView.findViewById(R.id.scene_user_new_first_name_layout);

        mLastName = mView.findViewById(R.id.scene_user_new_last_name);
        mLastNameLayout = mView.findViewById(R.id.scene_user_new_last_name_layout);

        mUserName = mView.findViewById(R.id.scene_user_new_user_name);
        mUserNameLayout = mView.findViewById(R.id.scene_user_new_user_name_layout);

        mLanguageSelector = mView.findViewById(R.id.scene_user_new_language_selector);
        mGenderSelector = mView.findViewById(R.id.scene_user_new_gender_selector);

        mPrimaryPhone = mView.findViewById(R.id.scene_user_new_primary_phone);
        mPrimaryPhoneLayout = mView.findViewById(R.id.scene_user_new_primary_phone_layout);

        mEmailAddress = mView.findViewById(R.id.scene_user_new_email_address);
        mEmailAddressLayout = mView.findViewById(R.id.scene_user_new_email_address_layout);

        mTrainerPin = mView.findViewById(R.id.scene_user_new_trainer_pin);
        mTrainerPinLayout = mView.findViewById(R.id.scene_user_new_trainer_pin_layout);

        mTokenPage = mView.findViewById(R.id.token_page);
        mUserDetails = mView.findViewById(R.id.user_details);
//        user_token_title = mView.findViewById(R.id.user_token_title);

        mView.findViewById(R.id.user_new_submit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSceneController().hideSoftKeyboardFromWindow();
                validateUserDetails();
            }
        });


        mView.findViewById(R.id.user_new_begin_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSceneController().hideSoftKeyboardFromWindow();
                validateTokenAndSend();
            }
        });

        mLanguages = getResources().getStringArray(R.array.scene_user_new_languages);
        mLanguageCodes = getResources().getStringArray(R.array.scene_user_new_language_codes);

        mGender = getResources().getStringArray(R.array.scene_user_gender);
        mGenderCodes = getResources().getStringArray(R.array.scene_user_gender_code);
        getActivity().findViewById(R.id.botton_nav).setVisibility(View.GONE);

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SceneController controller = getSceneController();
        if(controller == null) return;

        Context context = controller.getContext();
        if(context == null) return;

        ArrayAdapter<CharSequence> languageAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.scene_user_new_languages,
                android.R.layout.simple_spinner_dropdown_item
        );

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(
                context,
                R.array.scene_user_gender,
                android.R.layout.simple_spinner_dropdown_item
        );
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mLanguageSelector.setAdapter(languageAdapter);
        mGenderSelector.setAdapter(genderAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume::::::::::::: ");
        getActivity().findViewById(R.id.botton_nav).setVisibility(View.GONE);
        mTokenPage.setVisibility(View.GONE);
        mUserDetails.setVisibility(View.VISIBLE);
        if(VoxidemPreferences.isUserDefined()) {
            mFirstName.setText(VoxidemPreferences.getUserFirstName());
            mLastName.setText(VoxidemPreferences.getUserLastName());
            mUserName.setText(VoxidemPreferences.getUserAccountName());
            mUserName.setEnabled(false);
            mPrimaryPhone.setText(VoxidemPreferences.getUserPhoneNumber());
            mEmailAddress.setText(VoxidemPreferences.getUserEmailAddress());
            mTrainerPin.setText(VoxidemPreferences.getTrainerPin());
            int idx = findIndex(mLanguageCodes, VoxidemPreferences.getUserLanguage());
            if(idx < 0) {
                idx = findIndex(mLanguageCodes, VoxidemPreferences.getDefaultLanguageCode());
                if(idx < 0) return;
            }

            int genderIndex = findIndex(mGenderCodes, VoxidemPreferences.getUserGender().toString());
            if(genderIndex < 0) {
                genderIndex = findIndex(mGenderCodes, VoxidemPreferences.getUserGender().toString());
                if(genderIndex < 0) return;
            }
            mLanguageSelector.setText(mLanguages[idx]);
            mGenderSelector.setText(mGender[genderIndex]);
        } else {
            mFirstName.setText("");
            mFirstName.invalidate();
            mLastName.setText("");
            mLastName.invalidate();
//            mUserName.setText("");
//            mUserName.invalidate();
            mPrimaryPhone.setText("");
            mPrimaryPhone.invalidate();
            mEmailAddress.setText("");
            mEmailAddress.invalidate();
//            mTrainerPin.setText("");
//            mTrainerPin.invalidate();
            int idx = findIndex(mLanguageCodes, VoxidemPreferences.getDefaultLanguageCode());
            if(idx < 0) return;
            mLanguageSelector.setText(mLanguages[idx]);
            mLanguageSelector.invalidate();

            int genderIdx = findIndex(mGenderCodes, VoxidemPreferences.getUserGender().toString());
            if(genderIdx < 0) return;
            mLanguageSelector.setText(mGender[genderIdx]);
            mLanguageSelector.invalidate();
           // mUserName.setEnabled(true);
        }
    }


    private  void validateUserDetails(){


        final String firstName = mFirstName.getText().toString();
        final String lastName = mLastName.getText().toString();
        final String userName = mPrimaryPhone.getText().toString().trim().replace("+", "");
        final String phoneNumber = mPrimaryPhone.getText().toString().trim().replace("+", "");
        final String emailAddress = mEmailAddress.getText().toString();
      //  final String trainerPin = mTrainerPin.getText().toString();
        String language = mLanguageSelector.getText().toString();
        String gender = mGenderSelector.getText().toString();


        boolean errors = false;
        if(firstName.isEmpty()) {
            errors = true;
            mFirstNameLayout.setError(getString(R.string.scene_user_new_first_name_error));
        } else {
            mFirstNameLayout.setErrorEnabled(false);
        }

        if(lastName.isEmpty()) {
            errors = true;
            mLastNameLayout.setError(getString(R.string.scene_user_new_last_name_error));
        } else {
            mLastNameLayout.setErrorEnabled(false);
        }

        if(userName.isEmpty()) {
            errors = true;
            mUserNameLayout.setError(getString(R.string.scene_user_new_user_name_error));
        } else if(userName.length() < 5 || userName.length() > 16) {
            errors = true;
            mUserNameLayout.setError(getString(R.string.scene_user_new_user_name_limit_error));
        } else if(!validateUsername(userName)) {
            errors = true;
            mUserNameLayout.setError(getString(R.string.scene_user_new_user_name_invalid_error));
        } else {
            mUserNameLayout.setErrorEnabled(false);
        }

        if(language.isEmpty()) {
            errors = true;
            mLanguageSelector.setError(getString(R.string.scene_user_new_vocal_language_error));
        } else {
            int idx = findIndex(mLanguages, language);
            if(idx < 0) {
                errors = true;
                mLanguageSelector.setError(getString(R.string.scene_user_new_vocal_language_unsupported_error));
            } else {
                mLanguageSelector.setError(null);
                language = mLanguageCodes[idx];
            }
        }


        if(gender.isEmpty()) {
            errors = true;
            mGenderSelector.setError(getString(R.string.scene_user_new_vocal_gender_error));
        } else {
            int genderIdx = findIndex(mGender, gender);
            if(genderIdx < 0) {
                errors = true;
                mGenderSelector.setError(getString(R.string.scene_user_new_vocal_gender_error));
            } else {
                mGenderSelector.setError(null);
                gender = mGenderCodes[genderIdx];
            }
        }

        if(phoneNumber.isEmpty()) {
            errors = true;
            mPrimaryPhoneLayout.setError(getString(R.string.scene_user_new_primary_phone_error));
        } else if(!validatePhone(phoneNumber)) {
            errors = true;
            mPrimaryPhoneLayout.setError(getString(R.string.scene_user_new_primary_phone_invalid_error));
        } else {
            mPrimaryPhoneLayout.setErrorEnabled(false);
        }

        if(emailAddress.isEmpty()) {
            errors = true;
            mEmailAddressLayout.setError(getString(R.string.scene_user_new_email_address_error));
        } else if(!validateEmail(emailAddress)) {
            errors = true;
            mEmailAddressLayout.setError(getString(R.string.scene_user_new_email_address_invalid_error));
        } else {
            mEmailAddressLayout.setErrorEnabled(false);
        }

        if (!errors){
            mUserDetails.setVisibility(View.GONE);
            mTokenPage.setVisibility(View.VISIBLE);
        }

    }
    private void validateTokenAndSend() {


        if(VoxidemPreferences.isUserDefined()) {
          if(mPrimaryPhone.getText().toString().equalsIgnoreCase(VoxidemPreferences.getUserPhoneNumber())) {
              Log.d(TAG, "validateTokenAndSend::::::::::::inside userrrrrrrrrr::::::::::");
              dispatchExpand(R.id.actor_enroll);
          }
        }

        if(VoxidemPreferences.isUserDefined()) {
            mFirstName.setText(VoxidemPreferences.getUserFirstName());
            mLastName.setText(VoxidemPreferences.getUserLastName());
            mUserName.setText(VoxidemPreferences.getUserAccountName());
            mUserName.setEnabled(false);
            mPrimaryPhone.setText(VoxidemPreferences.getUserPhoneNumber());
            mEmailAddress.setText(VoxidemPreferences.getUserEmailAddress());
            mTrainerPin.setText(VoxidemPreferences.getTrainerPin());
            int idx = findIndex(mLanguageCodes, VoxidemPreferences.getUserLanguage());
            if(idx < 0) {
                idx = findIndex(mLanguageCodes, VoxidemPreferences.getDefaultLanguageCode());
                if(idx < 0) return;
            }
            mLanguageSelector.setText(mLanguages[idx]);

            int genderIndex = findIndex(mGenderCodes, VoxidemPreferences.getUserGender().toString());
            if(genderIndex < 0) {
                genderIndex = findIndex(mGenderCodes, VoxidemPreferences.getUserGender().toString());
                if(genderIndex < 0) return;
            }
            mGenderSelector.setText(mGender[genderIndex]);

        }

            // validate user data
            final String firstName = mFirstName.getText().toString();
            final String lastName = mLastName.getText().toString();
            final String userName = mPrimaryPhone.getText().toString().trim().replace("+", "");
            final String phoneNumber = mPrimaryPhone.getText().toString().trim().replace("+", "");
            final String emailAddress = mEmailAddress.getText().toString();
            final String trainerPin = mTrainerPin.getText().toString();
            String language = mLanguageSelector.getText().toString();
            String gender = mGenderSelector.getText().toString();

            boolean errors = false;

            if(trainerPin.isEmpty()) {
                errors = true;
                mTrainerPinLayout.setError(getString(R.string.scene_user_new_trainer_pin_error));
            } else if(!validateTrainerPin(trainerPin)) {
                errors = true;
                mTrainerPinLayout.setError(getString(R.string.scene_user_new_trainer_pin_invalid_error));
            } else {
                mTrainerPinLayout.setErrorEnabled(false);
            }

            if(language.isEmpty()) {
                errors = true;
                mLanguageSelector.setError(getString(R.string.scene_user_new_vocal_language_error));
            } else {
                int idx = findIndex(mLanguages, language);
                if(idx < 0) {
                    errors = true;
                    mLanguageSelector.setError(getString(R.string.scene_user_new_vocal_language_unsupported_error));
                } else {
                    mLanguageSelector.setError(null);
                    language = mLanguageCodes[idx];
                }
            }

        if(gender.isEmpty()) {
            errors = true;
            mGenderSelector.setError(getString(R.string.scene_user_new_vocal_gender_error));
        } else {
            int genderIdx = findIndex(mGender, gender);
            if(genderIdx < 0) {
                errors = true;
                mGenderSelector.setError(getString(R.string.scene_user_new_vocal_gender_error));
            } else {
                mGenderSelector.setError(null);
                gender = mGenderCodes[genderIdx];
            }
        }

            if(!errors) {
                Log.d(TAG, "validateTokenAndSend::::::::::::creating userrrrrrrrrr::::::::::"+language);
                Log.d(TAG, "validateTokenAndSend::::::::::::creating userrrrrrrrrr:::::asdklhaslkdhaskljdnas:::::"+gender);

                // commit the details
                CloudMessage message = CloudMessage.Create("v2access.User");
                message.putString("v2w_first_name", firstName);
                message.putString("v2w_last_name", lastName);
                message.putString("v2w_user_name", userName);
                message.putString("v2w_gender", gender);
                message.putString("v2w_language_code", language);
                message.putString("v2w_phone_number", phoneNumber);
                message.putString("v2w_email_address", emailAddress);

                final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Validating User Details ...");
                progressDialog.show();

                final String language_code = language;
                final String user_gender = gender;
                message.send(getActivity(), new CloudController.ResponseOnUiCallback() {

                    @Override
                    public void onResult(CloudResult result) {

                        progressDialog.dismiss();
                        VoxidemPreferences.setUserDetails(
                                getActivity(),
                                firstName,
                                lastName,
                                phoneNumber,
                                SveEnroller.getGender(user_gender),
                                language_code,
                                phoneNumber,
                                emailAddress,
                                trainerPin
                        );
                        dispatchExpand(R.id.actor_enroll);
                    }

                    @Override
                    public void onError(CloudError error) {
                        progressDialog.dismiss();
                        mTokenPage.setVisibility(View.GONE);
                        mUserDetails.setVisibility(View.VISIBLE);
                        if(error.hasData() && error.getData() instanceof UserError) {
                            UserError userError = (UserError)error.getData();
                            if(userError.isEmailAddressError()) {
                                mEmailAddressLayout.setError(getString(R.string.scene_user_new_email_address_found_error));
                            }
                            if(userError.isPhoneError()) {
                                mPrimaryPhoneLayout.setError(getString(R.string.scene_user_new_primary_phone_found_error));
                            }
                        } else {
                            Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Exception ex) {
                        ex.printStackTrace();
                        progressDialog.dismiss();
                        // do something here, print a message to the screen
                    }
                });
            }

    }

    private boolean validateTrainerPin(String trainerPin) {
        if (trainerPin.length() != 6) {
            return false;
        }

        char ch;
        boolean b;
        for (int j = 0; j < trainerPin.length(); j++) {
            ch = trainerPin.charAt(j);
            b = '0' <= ch && ch <= '9';
            if (!b) {
                return false;
            }
        }

        return true;
    }

    private boolean validateEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validatePhone(String phone) {
        return Patterns.PHONE.matcher(phone).matches();
    }

    private static final String USER_NAME_PATTERN = "^[A-Za-z0-9]+(?:[._-][A-Za-z0-9]+)*$";
    private Pattern username_pattern = Pattern.compile(USER_NAME_PATTERN);

    private boolean validateUsername(String username) {
        return username_pattern.matcher(username).matches();
    }

    private int findIndex(String[] haystack, String needle) {
        for(int i = 0; i < haystack.length; ++i) {
            if(haystack[i].equals(needle)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean onBackPressed() {
        //this is only needed if you have specific things
        //that you want to do when the user presses the back button.
        /* your specific things...*/
       return true;
    }

}
