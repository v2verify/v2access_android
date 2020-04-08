package com.validvoice.voxidem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.iid.InstanceID;
import com.validvoice.dynamic.speech.authorization.SveEnroller;
import com.validvoice.voxidem.cloud.UserDetails;

import java.io.IOException;
import java.util.Locale;

public class VoxidemPreferences {
    private static final String TAG = "VoxidemPreferences";

    private static final String PREF_USER_DEFINED = "pref_user_defined";

    private static final String PREF_USER_FIRST_NAME = "pref_user_first_name";

    private static final String PREF_USER_LAST_NAME = "pref_user_last_name";

    private static final String PREF_USER_ACCOUNT_NAME = "pref_user_account_name";

    private static final String PREF_USER_VOICE_PRINT_ID = "pref_user_voice_print_id";

    private static final String PREF_USER_GENDER = "pref_user_gender";

    private static final String PREF_USER_LANG = "pref_user_lang";



    private static final String PREF_USER_PHONE = "pref_user_phone";

    private static final String PREF_USER_EMAIL = "pref_user_email";

    private static final String PREF_TRAINER_PIN = "pref_trainer_pin";

    private static final String PREF_USER_IS_ENROLLED = "pref_user_is_enrolled";

    private static final String PREF_USER_ENROLLMENT_DATE = "pref_user_is_enrolled_date";

    private static final String PREF_IS_BACKGROUND_NOISE = "pref_is_background_noise";

    private static final String PREF_IS_RE_ENROLLMENT = "pref_is_re_enrollment";


    private static final String PREF_IS_MOBILE_LOGIN = "pref_is_mobile_login";

    private static final String PREF_IS_LANGUAGE_SELECTED = "pref_is_language_selected";

    private static final String PREF_IS_LOCALE_SET = "pref_is_locale_set";

    private static final String PREF_IS_APPLICATION_KEY = "pref_is_application_key";

    private static final String PREF_IS_DEVELOPER_KEY = "pref_is_developer_key";

    private static final String PREF_IS_COMPANY_INFO = "pref_is_company_info";


    private static SharedPreferences mSharedPreferences;

    static void initialize(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean isUserDefined() {
        return mSharedPreferences.getBoolean(PREF_USER_DEFINED, false);
    }

    public static boolean isUserComplete() {
        return mSharedPreferences.getBoolean(PREF_USER_DEFINED, false) &&
                mSharedPreferences.getBoolean(PREF_USER_IS_ENROLLED, false);
    }

    public static boolean isUserEnrolled() {
        return mSharedPreferences.getBoolean(PREF_USER_IS_ENROLLED, false);
    }

    public static String getApplicationKey(){
        return mSharedPreferences.getString(PREF_IS_APPLICATION_KEY, "");
    }

    public static String getDeveloperKey(){
        return mSharedPreferences.getString(PREF_IS_DEVELOPER_KEY, "");
    }


    public static boolean setKeys(String applicationKey, String developmentKey){
        mSharedPreferences
                .edit()
                .putString(PREF_IS_APPLICATION_KEY, applicationKey)
                .putString(PREF_IS_DEVELOPER_KEY, developmentKey)
                .apply();

        return true;
    }


    public static boolean isCompanyInfoSet(){
        return mSharedPreferences.getBoolean(PREF_IS_COMPANY_INFO, false);
    }


    public static void setCompanyInfo(boolean isSet){
        mSharedPreferences
                .edit()
                .putBoolean(PREF_IS_COMPANY_INFO,isSet)
                .apply();
    }
    public static boolean setUserDetails(Context context, String firstName, String lastName, String userName,
                                         SveEnroller.Gender gender, String languageCode,
                                         String primaryPhone, String emailAddress, String trainerPin) {
        if(firstName != null && !firstName.isEmpty() &&
                lastName != null && !lastName.isEmpty() &&
                userName != null && !userName.isEmpty() &&
                gender != null && gender != SveEnroller.Gender.Unknown &&
                languageCode != null && !languageCode.isEmpty() &&
                primaryPhone != null && !primaryPhone.isEmpty() &&
                emailAddress != null && !emailAddress.isEmpty() &&
                trainerPin != null) {
            mSharedPreferences
                    .edit()
                    .putString(PREF_USER_FIRST_NAME, firstName)
                    .putString(PREF_USER_LAST_NAME, lastName)
                    .putString(PREF_USER_ACCOUNT_NAME, userName)
                    .putInt(PREF_USER_GENDER, gender.ordinal())
                    .putString(PREF_USER_LANG, languageCode)
                    .putString(PREF_USER_PHONE, primaryPhone)
                    .putString(PREF_USER_EMAIL, emailAddress)
                    .putString(PREF_TRAINER_PIN, trainerPin)
                    .putString(PREF_USER_VOICE_PRINT_ID, InstanceID.getInstance(context).getId())
                    .putBoolean(PREF_USER_DEFINED, true)
                    .apply();
            return true;
        }
        return false;
    }

    public static boolean setUserDetails(Context context, UserDetails details) {
        clearUserDetails(context);
        if(details.HasVoicePrintId()) {
            mSharedPreferences
                    .edit()
                    .putString(PREF_USER_FIRST_NAME, details.GetFirstName())
                    .putString(PREF_USER_LAST_NAME, details.GetLastName())
                    .putString(PREF_USER_ACCOUNT_NAME, details.GetUsername())
                    .putInt(PREF_USER_GENDER, details.GetGender().ordinal())
                    .putString(PREF_USER_LANG, details.GetLanguageCode())
                    .putString(PREF_USER_PHONE, details.GetPrimaryPhone())
                    .putString(PREF_USER_EMAIL, details.GetEmailAddress())
                    .putString(PREF_TRAINER_PIN, "")
                    .putString(PREF_USER_VOICE_PRINT_ID, details.GetVoicePrintId())
                    .putLong(PREF_USER_ENROLLMENT_DATE, details.GetmEnrollmentDate())
                    .putBoolean(PREF_USER_DEFINED, true)
                    .putBoolean(PREF_USER_IS_ENROLLED, true)
                    .apply();
            return true;
        }
        return setUserDetails(context, details.GetFirstName(), details.GetLastName(),
                details.GetUsername(), details.GetGender(), details.GetLanguageCode(),
                details.GetPrimaryPhone(), details.GetEmailAddress(), "");
    }


    public static void saveBackgroundNoiseFlag(Boolean isBackgroundNoise){
        mSharedPreferences
                .edit()
                .putBoolean(PREF_IS_BACKGROUND_NOISE,isBackgroundNoise)
                .apply();
    }

    public static boolean setUserEnrolled() {

        Log.d(TAG, "setUserEnrolled::::::::::::::::::::::::::::::::::::::::::::::"+System.currentTimeMillis()/1000);
        if(mSharedPreferences.getBoolean(PREF_USER_DEFINED, false)) {
            mSharedPreferences
                    .edit()
                    .putString(PREF_TRAINER_PIN, "")
                    .putBoolean(PREF_USER_IS_ENROLLED, true)
                    .putLong(PREF_USER_ENROLLMENT_DATE,System.currentTimeMillis()/1000)
                    .apply();
            return true;
        }
        return false;
    }

    public  static void setPrefIsReEnrollment(boolean flag){
        mSharedPreferences
                .edit()
                .putBoolean(PREF_IS_RE_ENROLLMENT, flag)
                .apply();
    }


    public static void setPrefIsLanguageSelected(String locale){
        mSharedPreferences
                .edit()
                .putString(PREF_IS_LANGUAGE_SELECTED, locale)
                .apply();
    }

    public static void setPrefIsLocaleSet(Boolean flag){
        mSharedPreferences
                .edit()
                .putBoolean(PREF_IS_LOCALE_SET, flag)
                .apply();
    }

    public static void clearUserDetails(Context context) {
        if(isUserDefined()) {
            mSharedPreferences
                .edit()
                    .remove(PREF_USER_FIRST_NAME)
                    .remove(PREF_USER_LAST_NAME)
                    .remove(PREF_USER_ACCOUNT_NAME)
                    .remove(PREF_USER_GENDER)
                    .remove(PREF_USER_LANG)
                    .remove(PREF_USER_PHONE)
                    .remove(PREF_USER_EMAIL)
                    .remove(PREF_TRAINER_PIN)
                    .remove(PREF_USER_VOICE_PRINT_ID)
                    .remove(PREF_USER_IS_ENROLLED)
                    .remove(PREF_USER_ENROLLMENT_DATE)
                    .putBoolean(PREF_USER_DEFINED, false)
                .apply();
            new DeleteInstanceId(context).execute();
        }
    }

    public static String getUserFirstName() {
        return mSharedPreferences.getString(PREF_USER_FIRST_NAME, "");
    }

    public static String getUserLastName() {
        return mSharedPreferences.getString(PREF_USER_LAST_NAME, "");
    }

    public static String getUserAccountName() {
        return mSharedPreferences.getString(PREF_USER_ACCOUNT_NAME, "");
    }

    public static SveEnroller.Gender getUserGender() {
        int gender = mSharedPreferences.getInt(PREF_USER_GENDER, 0);
        if (gender != 1 && gender != 2) return SveEnroller.Gender.Unknown;
        return SveEnroller.Gender.values()[gender];
    }

    public static Boolean getBackgroundNoiseFlag(){
        return mSharedPreferences.getBoolean(PREF_IS_BACKGROUND_NOISE, false);
    }


    public static Boolean getIsMobileLogin(){
        return mSharedPreferences.getBoolean(PREF_IS_MOBILE_LOGIN, false);
    }

    public static Boolean getUserIsReEnrolled(){
        return mSharedPreferences.getBoolean(PREF_IS_RE_ENROLLMENT, false);

    }

    public static String getUserLanguage() {
        return mSharedPreferences.getString(PREF_USER_LANG, "");
    }

    public  static Long getUserEnrollmentDate(){
        return mSharedPreferences.getLong(PREF_USER_ENROLLMENT_DATE,0);
    }

    public static void setTrainerPin(String trainerPin){
        mSharedPreferences
                .edit()
                .putString(PREF_TRAINER_PIN, trainerPin)
                .apply();
    }

    public static void setPrefIsMobileLogin(Boolean flag){
        mSharedPreferences
                .edit()
                .putBoolean(PREF_IS_MOBILE_LOGIN, flag)
                .apply();
    }

    public static String getUserPhoneNumber() {
        return mSharedPreferences.getString(PREF_USER_PHONE, "");
    }

    public static String getUserEmailAddress() {
        return mSharedPreferences.getString(PREF_USER_EMAIL, "");
    }

    public static String getTrainerPin() {
        return mSharedPreferences.getString(PREF_TRAINER_PIN, "");
    }

    public static String getUserVoicePrintId() {
        return mSharedPreferences.getString(PREF_USER_VOICE_PRINT_ID, "");
    }


    public static String getSelectedLanguage(){
        return mSharedPreferences.getString(PREF_IS_LANGUAGE_SELECTED, "");
    }

    public static Boolean getPrefIsLocaleSet(){
        return mSharedPreferences.getBoolean(PREF_IS_LOCALE_SET, false);
    }


    public static String getDefaultLanguageCode() {
        final Locale locale = Locale.getDefault();
        final StringBuilder language = new StringBuilder(locale.getLanguage());
        final String country = locale.getCountry();
        if (!TextUtils.isEmpty(country)) {
            language.append("-");
            language.append(country);
        }
        return language.toString();
    }

    private static class DeleteInstanceId extends AsyncTask<Void, Void, Void> {

        private InstanceID mId;

        DeleteInstanceId(@NonNull Context context) {
            mId = InstanceID.getInstance(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                mId.deleteInstanceID();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
