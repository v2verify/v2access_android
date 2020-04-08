package com.validvoice.voxidem;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.text.Html;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import com.validvoice.dynamic.cloud.CloudController;
import com.validvoice.dynamic.cloud.CloudError;
import com.validvoice.dynamic.cloud.CloudMessage;
import com.validvoice.dynamic.cloud.CloudResult;
import com.validvoice.dynamic.scene.IScenePermissionListener;
import com.validvoice.dynamic.scene.SceneActivity;
import com.validvoice.dynamic.scene.SceneController;
import com.validvoice.dynamic.scene.SceneLayout;
import com.validvoice.dynamic.speech.SpeechApi;
import com.validvoice.dynamic.speech.service.AdaptiveSpeechServiceConnection;
import com.validvoice.voxidem.scenes.devices.DevicesSceneController;
import com.validvoice.voxidem.scenes.faq.FaqSceneController;
import com.validvoice.voxidem.scenes.history.HistorySceneController;
import com.validvoice.voxidem.scenes.home.HomeSceneController;
import com.validvoice.voxidem.scenes.settings.SettingsFragment;
import com.validvoice.voxidem.scenes.settings.SettingsSceneController;
import com.validvoice.voxidem.scenes.user_setup.UserSetupSceneController;
import com.validvoice.voxidem.utils.SimplePedometer;

import java.util.Locale;

import static com.validvoice.voxidem.VoxidemPreferences.saveBackgroundNoiseFlag;
import static com.validvoice.voxidem.scenes.settings.SettingsFragment.PREF_GENERAL_DARK_MODE;

public class MainActivity extends SceneActivity
        implements NavigationView.OnNavigationItemSelectedListener,
         BottomNavigationView.OnNavigationItemSelectedListener,
        IScenePermissionListener,
        AdaptiveSpeechServiceConnection.OnAdaptiveSpeechListener,
        SimplePedometer.PedometerListener {

    private static final String TAG = "MainActivity";

    /**
     *
     */
    private DrawerLayout mDrawer;

    /**
     *
     */
    private ActionBarDrawerToggle mToggle;

    /**
     *
     */
    private Toolbar mToolbar;

    /**
     *
     */
    private NavigationView mNavigationView;
    /**
     *
     */
    private BottomNavigationView mBottomNavigationView;


    /**
     *
     */
    public AdaptiveSpeechServiceConnection mAdaptiveConnection;

    /**
     *
     */
    private SimplePedometer mSimplePedometer;
    private int mStepsAllowed;
    private boolean mMessageInFlight = false;
    private boolean generalDarkMode;


    /**
     *
     */
    private Snackbar mBackgroundTooNoisySnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppTheme();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");

        setContentView(R.layout.activity_main);
        setSceneLayout(R.id.sceneLayout,getSceneBackground());

        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitle("");
        mToolbar.setSubtitle("");
        setSupportActionBar(mToolbar);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mDrawer = findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(
                this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
      //  mDrawer.addDrawerListener(mToggle);
       // mToggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mBottomNavigationView = findViewById(R.id.botton_nav);
        mBottomNavigationView.setOnNavigationItemSelectedListener(this);

        mDrawer.addDrawerListener(new DrawerListener());

        mNavigationView.setVisibility(View.GONE);
       // mDrawer.setVisibility(View.GONE);
        addSceneActionActor(new MainActionActor());
        registerSceneControllerFactory(R.id.scene_home, new HomeSceneController.Factory());
        registerSceneControllerFactory(R.id.scene_user_setup, new UserSetupSceneController.Factory());
        //registerSceneControllerFactory(R.id.scene_accounts, new AccountsSceneController.Factory());
        registerSceneControllerFactory(R.id.scene_devices, new DevicesSceneController.Factory());
        registerSceneControllerFactory(R.id.scene_history, new HistorySceneController.Factory());
        registerSceneControllerFactory(R.id.scene_settings, new SettingsSceneController.Factory());
        registerSceneControllerFactory(R.id.scene_faq, new FaqSceneController.Factory());

        mAdaptiveConnection = new AdaptiveSpeechServiceConnection(1500, this);
        mSimplePedometer = new SimplePedometer(this, this);

     //   checkUpdates();
        TextView powered = findViewById(R.id.powered_by);
        powered.setText(Html.fromHtml(getString(R.string.powered_by_validvoice)));


        if(!VoxidemPreferences.getApplicationKey().equalsIgnoreCase(getString(R.string.sve_app_key)) && VoxidemPreferences.isCompanyInfoSet()) {
            SpeechApi.Builder builder = new SpeechApi.Builder(this);
            builder.setDeveloperKey(VoxidemPreferences.getDeveloperKey())
                    .setApplicationKey(VoxidemPreferences.getApplicationKey())
                    .setApplicationVersion(BuildConfig.VERSION_NAME)
                    .setServer(getString(R.string.sve_server))
                    .setTransportProtocol(SpeechApi.TransportProtocol.REST);
            SpeechApi.initialize(builder.build());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart");
        Log.d(TAG, "onStart: ::::::::::::::::::::::::::::::::::::::::"+VoxidemPreferences.isUserEnrolled());
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        final SharedPreferences.Editor editor = sp.edit();
//        editor.putBoolean(AdaptiveSpeechServiceConnection.PREF_ADAPTIVE_MICROPHONE, true).apply();

        saveBackgroundNoiseFlag(false);

        spanTitleBar("");

        if (VoxidemPreferences.isUserComplete()) {
            setNavigationDrawerState(false);
            setHomeScene(R.id.scene_home);
        } else {
            setNavigationDrawerState(false);
            setHomeScene(R.id.scene_user_setup);
        }
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed");
        if (mDrawer != null && mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else if (currentSceneController() == null) {
            super.onBackPressed();
        } else if (!sceneBackPressed()) {
            if (!isHomeSceneActive()) {
                Menu menu = mNavigationView.getMenu();
                MenuItem item = menu.findItem(R.id.scene_home);
                item.setChecked(true);
                setHomeSceneActive();
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");

        if (!VoxidemPreferences.getSelectedLanguage().isEmpty()){
           setLocale(VoxidemPreferences.getSelectedLanguage());
        }
        requestPermission(100, Manifest.permission.RECORD_AUDIO, this);
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        changeScene(item.getItemId());
        if (mDrawer != null && mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        }
        return true;
    }

    private void setNavigationDrawerState(boolean isEnabled) {
        Log.i(TAG, "setNavigationDrawerState");
       // if(isEnabled) {
        //    mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//            mToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_UNLOCKED);
//            mToggle.setDrawerIndicatorEnabled(true);
//            mToggle.syncState();
      //  } else {
            mDrawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//            mToggle.onDrawerStateChanged(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//            mToggle.setDrawerIndicatorEnabled(false);
//            mToggle.syncState();
       // }
    }

    ///
    /// IScenePermissionListener
    ///

    @Override
    public void onPermissionGranted(@NonNull String permission) {
        Log.d(TAG, "onPermissionGranted");
        if(permission.equals(Manifest.permission.RECORD_AUDIO)) {
            mAdaptiveConnection.bind(this);
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
                                MainActivity.this);
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
                        finish();
                    }
                })
                .show();
        }
    }

    ///
    /// AdaptiveSpeechServiceConnection.OnAdaptiveListener
    ///

    @Override
    public void onBackgroundOk() {
        Log.i(TAG, "onBackgroundOk");
        if(mBackgroundTooNoisySnackbar != null) {
            mBackgroundTooNoisySnackbar.dismiss();
            mBackgroundTooNoisySnackbar = null;
            saveBackgroundNoiseFlag(false);

        }
    }

    @Override
    public void onBackgroundTooNoisy() {
        Log.i(TAG, "onBackgroundTooNoisy");
        if(mBackgroundTooNoisySnackbar == null) {
            mBackgroundTooNoisySnackbar = makeSnackbar(
                    "Background Noise Detected.",
                    Snackbar.LENGTH_SHORT
            );
        }
        mBackgroundTooNoisySnackbar.show();
        saveBackgroundNoiseFlag(true);
    }

    @Override
    public void onBackgroundCalibrated(int level) {
        Log.i(TAG, "onBackgroundCalibrated");
        //makeSnackbar("Microphone Calibrated: " + level, Snackbar.LENGTH_LONG).show();
    }

    ///
    /// SimplePedometer.PedometerListener
    ///

    @Override
    public void onStep(long timeNs, int steps) {
        if(steps >= mStepsAllowed && !mMessageInFlight) {
            mMessageInFlight = true;
            CloudMessage message = CloudMessage.Post("v2access.accounts:logoff.{@v2w_user_name}");
            message.putString("v2w_user_name", VoxidemPreferences.getUserAccountName());
            message.send(this, new CloudController.ResponseOnUiCallback() {
                @Override
                public void onResult(CloudResult result) {
                    mMessageInFlight = false;
                    getSceneDirector().currentController()
                            .dispatchBypassAction(
                                    R.id.actor_main,
                                    R.id.action_step_tracker_off,
                                    null
                            );
                }

                @Override
                public void onError(CloudError error) {
                    mMessageInFlight = false;
                }

                @Override
                public void onFailure(final Exception ex) {
                    ex.printStackTrace();
                    mMessageInFlight = false;
                }
            });

        }
    }

    ///
    /// Span Title Bar
    ///

    public void spanTitleBar( String toolbarTitle)  {

        Log.i(TAG, "spanTitleBar");

        SpannableStringBuilder spanBuilder = new SpannableStringBuilder();

        SpannableString v2MobileString = new SpannableString("V2access");
        int titleColor = generalDarkMode ? getResources().getColor(R.color.colorNavTextDark) : getResources().getColor(R.color.colorNavText);
        v2MobileString.setSpan(new ForegroundColorSpan(titleColor), 0, 8, 0);
        spanBuilder.append(v2MobileString);

        if(BuildConfig.FLAVOR.equals("local")) {
            SpannableString devString = new SpannableString(" (local)");
            devString.setSpan(new ForegroundColorSpan(titleColor), 0, 8, 0);
            spanBuilder.append(devString);
        } else if(BuildConfig.FLAVOR.equals("dev")) {
            SpannableString devString = new SpannableString(" (dev)");
            devString.setSpan(new ForegroundColorSpan(titleColor), 0, 6, 0);
            spanBuilder.append(devString);
        } else if(BuildConfig.FLAVOR.equals("demo")) {
            SpannableString devString = new SpannableString(" (demo)");
            devString.setSpan(new ForegroundColorSpan(titleColor), 0, 7, 0);
            spanBuilder.append(devString);
        } else if(BuildConfig.FLAVOR.equals("demo test")) {
            SpannableString devString = new SpannableString(" (demo test)");
            devString.setSpan(new ForegroundColorSpan(titleColor), 0, 12, 0);
            spanBuilder.append(devString);
        }

        mToolbar.setTitle(toolbarTitle);
        mToolbar.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mToolbar.setTitleTextColor(titleColor);
//        spanBuilder.clear();
//        spanBuilder.clearSpans();
//
//        spanBuilder.append("   Life Beyond Passwords TM");
//        spanBuilder.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), 3, 24, 0);
//        spanBuilder.setSpan(new ForegroundColorSpan(titleColor), 3, 24, 0);
//        spanBuilder.setSpan(new RelativeSizeSpan(0.60f), 3, 24, 0);
//        spanBuilder.setSpan(new RelativeSizeSpan(0.30f), 24, 27, 0);
//        spanBuilder.setSpan(new SuperscriptSpan(), 0, 27, 0);
//
//        mToolbar.setSubtitle(spanBuilder);

    }

    ///
    ///
    ///

    private class DrawerListener extends DrawerLayout.SimpleDrawerListener {
        @Override
        public void onDrawerSlide(View drawerView, float slideOffset) {
            Log.i(TAG, "DrawerListener.onDrawerSlide");
            super.onDrawerSlide(drawerView, slideOffset);
            SceneController controller = currentSceneController();
            if (slideOffset < 0.25 && controller != null) {
                controller.onCloseScene(false);
            }
        }
    }

    ///
    ///
    ///

    private class MainActionActor extends SceneLayout.SceneActionActor {

        MainActionActor() {
            super(null, R.id.actor_main);
        }

        @Override
        public boolean onActorAction(int id, Object data) {
            Log.i(TAG, "MainActionActor.onActorAction");
            if(id == R.id.action_start_calibrating) {
                if(mAdaptiveConnection != null) {
                    AdaptiveSpeechServiceConnection.OnAdaptiveSpeechListener listener = null;
                    if(data instanceof AdaptiveSpeechServiceConnection.OnAdaptiveSpeechListener) {
                        listener = (AdaptiveSpeechServiceConnection.OnAdaptiveSpeechListener) data;
                    }
                    mAdaptiveConnection.startCalibrating(listener);
                }
                return true;
            } else if(id == R.id.action_stop_calibrating) {
                if(mAdaptiveConnection != null) {
                    AdaptiveSpeechServiceConnection.OnAdaptiveSpeechListener listener = null;
                    if(data instanceof AdaptiveSpeechServiceConnection.OnAdaptiveSpeechListener) {
                        listener = (AdaptiveSpeechServiceConnection.OnAdaptiveSpeechListener)data;
                    }
                    mAdaptiveConnection.stopCalibrating(listener);
                }
                return true;
            } else if(id == R.id.action_screen_keep_on) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                return true;
            } else if(id == R.id.action_screen_clear_on) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                return true;
            } else if(id == R.id.action_step_tracker_on) {
                int sensitivity = PreferenceManager.getDefaultSharedPreferences(MainActivity.this)
                        .getInt(SettingsFragment.PREF_LOGOFF_SENSITIVITY, 1);
                switch(sensitivity) {
                    case 0: mStepsAllowed = 15; break;
                    case 1: mStepsAllowed = 10; break;
                    case 2: mStepsAllowed = 5; break;
                }
                mSimplePedometer.start();
                return true;
            } else if(id == R.id.action_step_tracker_off) {
                mStepsAllowed = 0;
                mSimplePedometer.stop();
                return true;
            }
            return false;
        }
    }

    public GradientDrawable getSceneBackground() {
        GradientDrawable gradientDrawable = null;
            gradientDrawable = new GradientDrawable(
                    GradientDrawable.Orientation.TOP_BOTTOM,
                    new int[]{ContextCompat.getColor(this, generalDarkMode ? R.color.colorSecondaryDark : R.color.colorSecondary),
                            ContextCompat.getColor(this, generalDarkMode ? R.color.colorSecondaryDark : R.color.colorSecondary),
                            ContextCompat.getColor(this, R.color.colorPrimary)});
            gradientDrawable.setGradientRadius((float) (Math.sqrt(2) * 500));
        return gradientDrawable;

    }

    public void setAppTheme() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        generalDarkMode = sp.getBoolean(PREF_GENERAL_DARK_MODE, false);
        if (generalDarkMode) {
            setTheme(R.style.darktheme);
        } else {
            setTheme(R.style.AppTheme);
        }
    }

    public Boolean setLocale(String locale) {

            VoxidemPreferences.setPrefIsLanguageSelected(locale);
            VoxidemPreferences.setPrefIsLocaleSet(true);
            Locale myLocale = new Locale(locale);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
        return  true;
    }

    private void clearAppData() {
        try {
            // clearing app data
            if (Build.VERSION_CODES.KITKAT <= Build.VERSION.SDK_INT) {
                ((ActivityManager)getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData(); // note: it has a return value!
            } else {
                String packageName = getApplicationContext().getPackageName();
                Runtime runtime = Runtime.getRuntime();
                runtime.exec("pm clear "+packageName);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
