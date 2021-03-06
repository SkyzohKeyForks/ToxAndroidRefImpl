/**
 * [TRIfA], Java part of Tox Reference Implementation for Android
 * Copyright (C) 2017 Zoff <zoff@zoff.cc>
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA  02110-1301, USA.
 */

package com.zoffcc.applications.trifa;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicYuvToRGB;
import android.support.v8.renderscript.Type;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.gfx.android.orma.AccessThreadConstraint;
import com.github.gfx.android.orma.encryption.EncryptedDatabase;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;

import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Collector;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Detector;

import java.io.BufferedInputStream;
import java.io.File;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import info.guardianproject.iocipher.VirtualFileSystem;
import info.guardianproject.netcipher.proxy.OrbotHelper;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.zoffcc.applications.trifa.AudioReceiver.channels_;
import static com.zoffcc.applications.trifa.AudioReceiver.sampling_rate_;
import static com.zoffcc.applications.trifa.CallingActivity.audio_receiver_thread;
import static com.zoffcc.applications.trifa.CallingActivity.audio_thread;
import static com.zoffcc.applications.trifa.CallingActivity.close_calling_activity;
import static com.zoffcc.applications.trifa.MessageListActivity.ml_friend_typing;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_MIN_AUDIO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.GLOBAL_MIN_VIDEO_BITRATE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ORBOT_PROXY_HOST;
import static com.zoffcc.applications.trifa.TRIFAGlobals.ORBOT_PROXY_PORT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_INCOMING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_FT_DIRECTION.TRIFA_FT_DIRECTION_OUTGOING;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_FILE;
import static com.zoffcc.applications.trifa.TRIFAGlobals.TRIFA_MSG_TYPE.TRIFA_MSG_TYPE_TEXT;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_PREFIX;
import static com.zoffcc.applications.trifa.TRIFAGlobals.VFS_TMP_FILE_DIR;
import static com.zoffcc.applications.trifa.TRIFAGlobals.bootstrapping;
import static com.zoffcc.applications.trifa.TRIFAGlobals.cache_ft_fos;
import static com.zoffcc.applications.trifa.TRIFAGlobals.cache_ft_fos_normal;
import static com.zoffcc.applications.trifa.ToxVars.TOX_CONNECTION.TOX_CONNECTION_NONE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_CANCEL;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_PAUSE;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_CONTROL.TOX_FILE_CONTROL_RESUME;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_AVATAR;
import static com.zoffcc.applications.trifa.ToxVars.TOX_FILE_KIND.TOX_FILE_KIND_DATA;
import static com.zoffcc.applications.trifa.ToxVars.TOX_HASH_LENGTH;
import static com.zoffcc.applications.trifa.ToxVars.TOX_PUBLIC_KEY_SIZE;
import static com.zoffcc.applications.trifa.TrifaToxService.TOX_SERVICE_STARTED;
import static com.zoffcc.applications.trifa.TrifaToxService.is_tox_started;
import static com.zoffcc.applications.trifa.TrifaToxService.orma;
import static com.zoffcc.applications.trifa.TrifaToxService.vfs;

@RuntimePermissions
public class MainActivity extends AppCompatActivity
{
    private static final String TAG = "trifa.MainActivity";
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------
    final static boolean CTOXCORE_NATIVE_LOGGING = false;
    final static boolean ORMA_TRACE = false; // set "false" for release builds
    final static boolean DB_ENCRYPT = true;
    final static boolean VFS_ENCRYPT = true;
    // --------- global config ---------
    // --------- global config ---------
    // --------- global config ---------

    static TextView mt = null;
    static boolean native_lib_loaded = false;
    static String app_files_directory = "";
    // static boolean stop_me = false;
    // static Thread ToxServiceThread = null;
    Handler main_handler = null;
    static Handler main_handler_s = null;
    static Context context_s = null;
    static MainActivity main_activity_s = null;
    static AudioManager audio_manager_s = null;
    static Resources resources = null;
    static DisplayMetrics metrics = null;
    static int AudioMode_old;
    static int RingerMode_old;
    static boolean isSpeakerPhoneOn_old;
    static boolean isWiredHeadsetOn_old;
    static boolean isBluetoothScoOn_old;
    static Notification notification = null;
    static NotificationManager nmn3 = null;
    static int NOTIFICATION_ID = 293821038;
    static RemoteViews notification_view = null;
    static long[] friends = null;
    static FriendListFragment friend_list_fragment = null;
    static MessageListFragment message_list_fragment = null;
    static MessageListActivity message_list_activity = null;
    final static String MAIN_DB_NAME = "main.db";
    final static String MAIN_VFS_NAME = "files.db";
    static String SD_CARD_TMP_DIR = "";
    static String SD_CARD_STATIC_DIR = "";
    static String SD_CARD_TMP_DUMMYFILE = null;
    final static int AddFriendActivity_ID = 10001;
    final static int CallingActivity_ID = 10002;
    final static int ProfileActivity_ID = 10003;
    final static int SettingsActivity_ID = 10004;
    final static int AboutpageActivity_ID = 10005;
    final static int Notification_new_message_ID = 10023;
    static long Notification_new_message_last_shown_timestamp = -1;
    final static long Notification_new_message_every_millis = 2000; // ~2 seconds between notifications
    final static long UPDATE_MESSAGES_WHILE_FT_ACTIVE_MILLIS = 30000; // ~30 seconds
    final static long UPDATE_MESSAGES_NORMAL_MILLIS = 500; // ~0.5 seconds
    static String temp_string_a = "";
    static ByteBuffer video_buffer_1 = null;
    static ByteBuffer video_buffer_2 = null;
    final static int audio_in_buffer_max_count = 2; // how many out play buffers?
    final static int audio_out_buffer_mult = 3;
    static int audio_in_buffer_element_count = 0;
    static ByteBuffer[] audio_buffer_2 = new ByteBuffer[audio_in_buffer_max_count];
    static ByteBuffer audio_buffer_play = null;
    static int audio_buffer_play_length = 0;
    static int[] audio_buffer_2_read_length = new int[audio_in_buffer_max_count];
    static TrifaToxService tox_service_fg = null;
    static long update_all_messages_global_timestamp = -1;
    final static SimpleDateFormat df_date_time_long = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    //
    static boolean PREF__UV_reversed = true; // TODO: on older phones this needs to be "false"
    static boolean PREF__notification_sound = true;
    static boolean PREF__notification_vibrate = false;
    static boolean PREF__notification = true;
    static final int MIN_AUDIO_SAMPLINGRATE_OUT = 48000;
    static int PREF__min_audio_samplingrate_out = MIN_AUDIO_SAMPLINGRATE_OUT;
    static String PREF__DB_secrect_key = "98rj93ßjw3j8j4vj9w8p9eüiü9aci092";
    private static final String ALLOWED_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!§$%&()=?,.;:-_+";
    static boolean PREF__software_echo_cancel = false;
    static int PREF__udp_enabled = 0; // 0 -> Tox TCP mode, 1 -> Tox UDP mode
    static int PREF__audiosource = 2; // 1 -> VOICE_COMMUNICATION, 2 -> VOICE_RECOGNITION
    static boolean PREF__orbot_enabled = false;
    static boolean PREF__audiorec_asynctask = true;
    static boolean PREF__cam_recording_hint = true;
    static String versionName = "";
    static int versionCode = -1;
    static PackageInfo packageInfo_s = null;
    IntentFilter receiverFilter1 = null;
    IntentFilter receiverFilter2 = null;
    HeadsetStateReceiver receiver1 = null;
    HeadsetStateReceiver receiver2 = null;
    //
    // YUV conversion -------
    static ScriptIntrinsicYuvToRGB yuvToRgb = null;
    static Allocation alloc_in = null;
    static Allocation alloc_out = null;
    static Bitmap video_frame_image = null;
    static int buffer_size_in_bytes = 0;
    // YUV conversion -------

    // ---- lookup cache ----
    static Map<String, Long> cache_pubkey_fnum = new HashMap<String, Long>();
    static Map<Long, String> cache_fnum_pubkey = new HashMap<Long, String>();
    // ---- lookup cache ----

    // main drawer ----------
    Drawer main_drawer = null;
    AccountHeader main_drawer_header = null;
    ProfileDrawerItem profile_d_item = null;
    // main drawer ----------

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreate");

        EmojiManager.install(new IosEmojiProvider());
        // EmojiManager.install(new EmojiOneProvider());

        super.onCreate(savedInstanceState);

        main_handler = new Handler(getMainLooper());
        main_handler_s = main_handler;
        context_s = this.getBaseContext();
        main_activity_s = this;

        setContentView(R.layout.activity_main);

        //        try
        //        {
        //            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
        //        }
        //        catch (Exception e)
        //        {
        //            e.printStackTrace();
        //            Log.i(TAG, "onCreate:setThreadPriority:EE:" + e.getMessage());
        //        }

        getVersionInfo();

        try
        {
            packageInfo_s = getPackageManager().getPackageInfo(getPackageName(), 0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //        if (canceller == null)
        //        {
        //            canceller = new EchoCanceller();
        //        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bootstrapping = false;

        resources = this.getResources();
        metrics = resources.getDisplayMetrics();


        SD_CARD_TMP_DIR = getExternalFilesDir(null).getAbsolutePath() + "/tmpdir/";
        SD_CARD_STATIC_DIR = getExternalFilesDir(null).getAbsolutePath() + "/_staticdir/";
        SD_CARD_TMP_DUMMYFILE = make_some_static_dummy_file(this.getBaseContext());

        audio_manager_s = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        Log.i(TAG, "java.library.path:" + System.getProperty("java.library.path"));

        // prefs ----------
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        PREF__UV_reversed = settings.getBoolean("video_uv_reversed", true);
        PREF__notification_sound = settings.getBoolean("notifications_new_message_sound", true);
        PREF__notification_vibrate = settings.getBoolean("notifications_new_message_vibrate", false);
        PREF__notification = settings.getBoolean("notifications_new_message", true);
        PREF__software_echo_cancel = settings.getBoolean("software_echo_cancel", false);
        boolean tmp1 = settings.getBoolean("udp_enabled", false);
        if (tmp1)
        {
            PREF__udp_enabled = 1;
        }
        else
        {
            PREF__udp_enabled = 0;
        }

        PREF__orbot_enabled = false;
        boolean PREF__orbot_enabled__temp = settings.getBoolean("orbot_enabled", false);
        if (PREF__orbot_enabled__temp)
        {
            boolean orbot_installed = OrbotHelper.isOrbotInstalled(this);
            if (orbot_installed)
            {
                boolean orbot_running = OrbotHelper.isOrbotRunning(this);
                if (orbot_running)
                {
                    PREF__orbot_enabled = true;
                }
                else
                {
                    if (OrbotHelper.requestStartTor(this))
                    {
                        PREF__orbot_enabled = true;
                    }
                }
            }
            else
            {
                Intent orbot_get = OrbotHelper.getOrbotInstallIntent(this);
                try
                {
                    startActivity(orbot_get);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        Log.i(TAG, "PREF__UV_reversed:2=" + PREF__UV_reversed);
        Log.i(TAG, "PREF__notification_sound:2=" + PREF__notification_sound);
        Log.i(TAG, "PREF__notification_vibrate:2=" + PREF__notification_vibrate);
        try
        {
            if (settings.getString("min_audio_samplingrate_out", "8000").compareTo("Auto") == 0)
            {
                PREF__min_audio_samplingrate_out = 8000;
            }
            else
            {
                PREF__min_audio_samplingrate_out = Integer.parseInt(settings.getString("min_audio_samplingrate_out", "" + MIN_AUDIO_SAMPLINGRATE_OUT));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__min_audio_samplingrate_out = MIN_AUDIO_SAMPLINGRATE_OUT;
        }
        // prefs ----------

        PREF__DB_secrect_key = settings.getString("DB_secrect_key", "");
        if (PREF__DB_secrect_key.isEmpty())
        {
            // TODO: bad, make better
            // create new key -------------
            PREF__DB_secrect_key = getRandomString(20);
            settings.edit().putString("DB_secrect_key", PREF__DB_secrect_key).commit();
            // create new key -------------
        }

        // TODO: don't print this!!
        // ------ don't print this ------
        // ------ don't print this ------
        // ------ don't print this ------
        // ** // Log.i(TAG, "PREF__DB_secrect_key=" + PREF__DB_secrect_key);
        // ------ don't print this ------
        // ------ don't print this ------
        // ------ don't print this ------

        mt = (TextView) this.findViewById(R.id.main_maintext);
        mt.setText("...");

        nmn3 = (NotificationManager) context_s.getSystemService(NOTIFICATION_SERVICE);

        // get permission ----------
        MainActivityPermissionsDispatcher.dummyForPermissions001WithCheck(this);
        // get permission ----------

        // -------- drawer ------------
        // -------- drawer ------------
        // -------- drawer ------------
        PrimaryDrawerItem item1 = new PrimaryDrawerItem().withIdentifier(1).withName("Profile").withIcon(GoogleMaterial.Icon.gmd_face);
        PrimaryDrawerItem item2 = new PrimaryDrawerItem().withIdentifier(2).withName("Settings").withIcon(GoogleMaterial.Icon.gmd_settings);
        PrimaryDrawerItem item3 = new PrimaryDrawerItem().withIdentifier(3).withName("Logout/Login").withIcon(GoogleMaterial.Icon.gmd_refresh);
        PrimaryDrawerItem item4 = new PrimaryDrawerItem().withIdentifier(4).withName("Clear Cache").withIcon(GoogleMaterial.Icon.gmd_delete_sweep);
        PrimaryDrawerItem item5 = new PrimaryDrawerItem().withIdentifier(5).withName("About").withIcon(GoogleMaterial.Icon.gmd_info);
        PrimaryDrawerItem item6 = new PrimaryDrawerItem().withIdentifier(6).withName("Exit").withIcon(GoogleMaterial.Icon.gmd_exit_to_app);

        final Drawable d1 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_lock).
                color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);

        profile_d_item = new ProfileDrawerItem().
                withName("me").
                withIcon(d1);

        // Create the AccountHeader
        main_drawer_header = new AccountHeaderBuilder().
                withSelectionListEnabledForSingleProfile(false).
                withActivity(this).
                addProfiles(profile_d_item).
                withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener()
                {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile)
                    {
                        return false;
                    }
                }).build();

        // create the drawer and remember the `Drawer` result object
        main_drawer = new DrawerBuilder().
                withActivity(this).
                addDrawerItems(item1, new DividerDrawerItem(), item2, item3, item4, item5, new DividerDrawerItem(), item6).
                withTranslucentStatusBar(true).withAccountHeader(main_drawer_header).
                withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener()
                {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem)
                    {
                        Log.i(TAG, "drawer:item=" + position);
                        if (position == 1)
                        {
                            // profile
                            try
                            {
                                if (Callstate.state == 0)
                                {
                                    Log.i(TAG, "start profile activity");
                                    Intent intent = new Intent(context_s, ProfileActivity.class);
                                    startActivityForResult(intent, ProfileActivity_ID);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 3)
                        {
                            // settings
                            try
                            {
                                if (Callstate.state == 0)
                                {
                                    Log.i(TAG, "start settings activity");
                                    Intent intent = new Intent(context_s, SettingsActivity.class);
                                    startActivityForResult(intent, SettingsActivity_ID);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 4)
                        {
                            // logout/login
                            try
                            {
                                if (is_tox_started)
                                {
                                    tox_service_fg.stop_tox_fg();
                                }
                                else
                                {
                                    int PREF__orbot_enabled_to_int = 0;
                                    if (PREF__orbot_enabled)
                                    {
                                        PREF__orbot_enabled_to_int = 1;
                                    }
                                    init(app_files_directory, PREF__udp_enabled, PREF__orbot_enabled_to_int, ORBOT_PROXY_HOST, ORBOT_PROXY_PORT);
                                    tox_service_fg.tox_thread_start_fg();
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 6)
                        {
                            // About
                            try
                            {
                                Log.i(TAG, "start aboutpage activity");
                                Intent intent = new Intent(context_s, Aboutpage.class);
                                startActivityForResult(intent, AboutpageActivity_ID);
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if (position == 5)
                        {
                            // -- clear Glide cache --
                            // -- clear Glide cache --
                            clearCache();
                            // -- clear Glide cache --
                            // -- clear Glide cache --

                        }
                        else if (position == 8)
                        {
                            // Exit
                            try
                            {
                                if (is_tox_started)
                                {
                                    tox_service_fg.stop_tox_fg();
                                    tox_service_fg.stop_me(true);
                                }
                                else
                                {
                                    // just exit
                                    tox_service_fg.stop_me(true);
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                        return true;
                    }
                }).build();

        // -------- drawer ------------
        // -------- drawer ------------
        // -------- drawer ------------

        // reset calling state
        Callstate.state = 0;
        Callstate.tox_call_state = ToxVars.TOXAV_FRIEND_CALL_STATE.TOXAV_FRIEND_CALL_STATE_NONE.value;
        Callstate.call_first_video_frame_received = -1;
        Callstate.call_first_audio_frame_received = -1;
        Callstate.friend_pubkey = "-1";
        Callstate.audio_speaker = true;
        Callstate.other_audio_enabled = 1;
        Callstate.other_video_enabled = 1;
        Callstate.my_audio_enabled = 1;
        Callstate.my_video_enabled = 1;

        if (native_lib_loaded)
        {
            mt.setText("successfully loaded native library");
        }
        else
        {
            mt.setText("loadLibrary jni-c-toxcore failed!");
        }

        String native_api = getNativeLibAPI();
        mt.setText(mt.getText() + "\n" + native_api);
        mt.setText(mt.getText() + "\n" + "c-toxcore:v" + tox_version_major() + "." + tox_version_minor() + "." + tox_version_patch());
        mt.setText(mt.getText() + "\n" + "jni-c-toxcore:v" + jnictoxcore_version());

        // --- forground service ---
        // --- forground service ---
        // --- forground service ---
        Intent i = new Intent(this, TrifaToxService.class);
        if (!TOX_SERVICE_STARTED)
        {
            startService(i);
        }
        // --- forground service ---
        // --- forground service ---
        // --- forground service ---

        if ((!TOX_SERVICE_STARTED) || (orma == null))
        {
            try
            {
                String dbs_path = getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME;
                Log.i(TAG, "db:path=" + dbs_path);

                File database_dir = new File(new File(dbs_path).getParent());
                database_dir.mkdirs();

                OrmaDatabase.Builder builder = OrmaDatabase.builder(this);
                if (DB_ENCRYPT)
                {
                    builder = builder.provider(new EncryptedDatabase.Provider(PREF__DB_secrect_key));
                }
                orma = builder.name(dbs_path).
                        readOnMainThread(AccessThreadConstraint.NONE).
                        writeOnMainThread(AccessThreadConstraint.NONE).
                        trace(ORMA_TRACE).
                        build();
                Log.i(TAG, "db:open=OK:path=" + dbs_path);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "db:EE1:" + e.getMessage());

                String dbs_path = getDir("dbs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_DB_NAME;

                try
                {
                    Log.i(TAG, "db:deleting database:" + dbs_path);
                    new File(dbs_path).delete();
                }
                catch (Exception e3)
                {
                    e3.printStackTrace();
                    Log.i(TAG, "db:EE3:" + e3.getMessage());
                }

                Log.i(TAG, "db:path(2)=" + dbs_path);
                OrmaDatabase.Builder builder = OrmaDatabase.builder(this);
                if (DB_ENCRYPT)
                {
                    builder = builder.provider(new EncryptedDatabase.Provider(PREF__DB_secrect_key));
                }
                orma = builder.name(dbs_path).
                        readOnMainThread(AccessThreadConstraint.WARNING).
                        writeOnMainThread(AccessThreadConstraint.WARNING).
                        trace(ORMA_TRACE).
                        build();
                Log.i(TAG, "db:open(2)=OK:path=" + dbs_path);
            }

            // ----- Clear all messages from DB -----
            // ----- Clear all messages from DB -----
            // ----- Clear all messages from DB -----
            // ** // ** // orma.deleteFromMessage().execute();
            // ----- Clear all messages from DB -----
            // ----- Clear all messages from DB -----
            // ----- Clear all messages from DB -----

        }

        if ((!TOX_SERVICE_STARTED) || (vfs == null))
        {
            if (VFS_ENCRYPT)
            {
                try
                {
                    String dbFile = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME;

                    File database_dir = new File(new File(dbFile).getParent());
                    database_dir.mkdirs();

                    Log.i(TAG, "vfs:path=" + dbFile);
                    vfs = VirtualFileSystem.get();

                    try
                    {
                        if (!vfs.isMounted())
                        {
                            vfs.mount(dbFile, PREF__DB_secrect_key);
                        }
                    }
                    catch (Exception ee)
                    {
                        Log.i(TAG, "vfs:EE1:" + ee.getMessage());
                        ee.printStackTrace();
                        vfs.mount(dbFile, PREF__DB_secrect_key);
                    }
                    Log.i(TAG, "vfs:open(1)=OK:path=" + dbFile);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "vfs:EE2:" + e.getMessage());

                    String dbFile = getDir("vfs", MODE_PRIVATE).getAbsolutePath() + "/" + MAIN_VFS_NAME;

                    try
                    {
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**--------:" + dbFile);
                        new File(dbFile).delete();
                        Log.i(TAG, "vfs:**deleting database**--------:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                        Log.i(TAG, "vfs:**deleting database**:" + dbFile);
                    }
                    catch (Exception e3)
                    {
                        e3.printStackTrace();
                        Log.i(TAG, "vfs:EE3:" + e3.getMessage());
                    }

                    try
                    {
                        Log.i(TAG, "vfs:path=" + dbFile);
                        vfs = VirtualFileSystem.get();
                        vfs.createNewContainer(dbFile, PREF__DB_secrect_key);
                        vfs.mount(PREF__DB_secrect_key);
                        Log.i(TAG, "vfs:open(2)=OK:path=" + dbFile);
                    }
                    catch (Exception e2)
                    {
                        e2.printStackTrace();
                        Log.i(TAG, "vfs:EE4:" + e.getMessage());
                    }
                }

                Log.i(TAG, "vfs:encrypted:(1)prefix=" + VFS_PREFIX);

            }
            else
            {
                // VFS not encrypted -------------
                VFS_PREFIX = getExternalFilesDir(null).getAbsolutePath() + "/vfs/";
                Log.i(TAG, "vfs:not_encrypted:(2)prefix=" + VFS_PREFIX);
                // VFS not encrypted -------------

            }
        }

        // cleanup temp dirs --------
        if (!TOX_SERVICE_STARTED)
        {
            cleanup_temp_dirs();
        }
        // cleanup temp dirs --------

        // ---------- DEBUG, just a test ----------
        // ---------- DEBUG, just a test ----------
        // ---------- DEBUG, just a test ----------
        //        if (VFS_ENCRYPT)
        //        {
        //            if (vfs.isMounted())
        //            {
        //                vfs_listFilesAndFilesSubDirectories("/", 0, "");
        //            }
        //        }
        //        // ---------- DEBUG, just a test ----------
        //        // ---------- DEBUG, just a test ----------
        //        // ---------- DEBUG, just a test ----------

        app_files_directory = getFilesDir().getAbsolutePath();

        if (!TOX_SERVICE_STARTED)
        {
            tox_thread_start();
        }

        receiverFilter1 = new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        receiver1 = new HeadsetStateReceiver();
        registerReceiver(receiver1, receiverFilter1);

        receiverFilter2 = new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED);
        receiver2 = new HeadsetStateReceiver();
        registerReceiver(receiver2, receiverFilter2);
    }

    public void clearCache()
    {
        Log.i(TAG, "clearCache");

        try
        {
            Glide.get(MainActivity.this).clearMemory();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "clearCache:EE2:" + e.getMessage());
        }

        // ------clear Glide image cache------
        final Thread t_glide_clean_cache = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    Log.i(TAG, "clearCache:bg:start");

                    File cacheDir = Glide.getPhotoCacheDir(MainActivity.this);
                    if (cacheDir.isDirectory())
                    {
                        for (File child : cacheDir.listFiles())
                        {
                            if (!child.delete())
                            {
                            }
                            else
                            {
                                Log.i(TAG, "clearCache:" + child.getAbsolutePath());
                            }
                        }
                    }

                    Glide.get(MainActivity.this).clearDiskCache();
                    Log.i(TAG, "clearCache:bg:end");
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "clearCache:EE1:" + e.getMessage());
                }
            }
        };
        t_glide_clean_cache.start();
        // ------clear Glide image cache------

    }

    public static void cleanup_temp_dirs()
    {

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                Log.i(TAG, "cleanup_temp_dirs:---START---");

                try
                {
                    Thread.sleep(400);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    if (VFS_ENCRYPT)
                    {
                        Log.i(TAG, "cleanup_temp_dirs:001");
                        vfs_deleteFilesAndFilesSubDirectories_vfs(VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
                        Log.i(TAG, "cleanup_temp_dirs:002");
                    }
                    else
                    {
                        Log.i(TAG, "cleanup_temp_dirs:003");
                        vfs_deleteFilesAndFilesSubDirectories_real(VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
                        Log.i(TAG, "cleanup_temp_dirs:004");
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    vfs_deleteFilesAndFilesSubDirectories_real(SD_CARD_TMP_DIR + "/");
                }
                catch (Exception e)
                {
                    e.getMessage();
                }
                Log.i(TAG, "cleanup_temp_dirs:---READY---");
            }
        };
        t.start();
    }

    public static void vfs_deleteFilesAndFilesSubDirectories_real(String directoryName)
    {
        java.io.File directory1 = new java.io.File(directoryName);
        java.io.File[] fList1 = directory1.listFiles();

        for (java.io.File file : fList1)
        {
            if (file.isFile())
            {
                Log.i(TAG, "VFS:REAL:rm:" + file);
                file.delete();
            }
            else if (file.isDirectory())
            {
                Log.i(TAG, "VFS:REAL:rm:D:" + file);
                vfs_deleteFilesAndFilesSubDirectories_real(file.getAbsolutePath());
                file.delete();
            }
        }
    }

    public static void vfs_deleteFilesAndFilesSubDirectories_vfs(String directoryName)
    {
        if (VFS_ENCRYPT)
        {
            Log.i(TAG, "cleanup_temp_dirs:00a");
            info.guardianproject.iocipher.File directory1 = new info.guardianproject.iocipher.File(directoryName);
            info.guardianproject.iocipher.File[] fList1 = directory1.listFiles();

            for (info.guardianproject.iocipher.File file : fList1)
            {
                if (file.isFile())
                {
                    Log.i(TAG, "VFS:VFS:rm:" + file);
                    file.delete();
                }
                else if (file.isDirectory())
                {
                    Log.i(TAG, "VFS:VFS:rm:D:" + file);
                    vfs_deleteFilesAndFilesSubDirectories_vfs(file.getAbsolutePath());
                    file.delete();
                }
            }
            Log.i(TAG, "cleanup_temp_dirs:00b");
        }
    }

    public void vfs_listFilesAndFilesSubDirectories(String directoryName, int depth, String parent)
    {
        if (VFS_ENCRYPT)
        {
            info.guardianproject.iocipher.File directory1 = new info.guardianproject.iocipher.File(directoryName);
            info.guardianproject.iocipher.File[] fList1 = directory1.listFiles();

            for (info.guardianproject.iocipher.File file : fList1)
            {
                if (file.isFile())
                {
                    // final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // final String human_datetime = df.format(new Date(file.lastModified()));
                    Log.i(TAG, "VFS:f:" + parent + "/" + file.getName() + " bytes=" + file.length());
                }
                else if (file.isDirectory())
                {
                    Log.i(TAG, "VFS:d:" + parent + "/" + file.getName() + "/");
                    vfs_listFilesAndFilesSubDirectories(file.getAbsolutePath(), depth + 1, parent + "/" + file.getName());
                }
            }

        }
        else
        {
            java.io.File directory1 = new java.io.File(directoryName);
            java.io.File[] fList1 = directory1.listFiles();

            for (File file : fList1)
            {
                if (file.isFile())
                {
                    // final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // final String human_datetime = df.format(new Date(file.lastModified()));
                    Log.i(TAG, "VFS:f:" + parent + "/" + file.getName() + " bytes=" + file.length());
                }
                else if (file.isDirectory())
                {
                    Log.i(TAG, "VFS:d:" + parent + "/" + file.getName() + "/");
                    vfs_listFilesAndFilesSubDirectories(file.getAbsolutePath(), depth + 1, parent + "/" + file.getName());
                }
            }

        }

    }


    // ------- for runtime permissions -------
    // ------- for runtime permissions -------
    // ------- for runtime permissions -------
    @NeedsPermission({Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA})
    void dummyForPermissions001()
    {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
    // ------- for runtime permissions -------
    // ------- for runtime permissions -------
    // ------- for runtime permissions -------


    private static String getRandomString(final int sizeOfRandomString)
    {
        final Random random = new Random();
        final StringBuilder sb = new StringBuilder(sizeOfRandomString);
        for (int i = 0; i < sizeOfRandomString; ++i)
        {
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        }
        return sb.toString();
    }

    void tox_thread_start()
    {
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    long counter = 0;
                    while (tox_service_fg == null)
                    {
                        counter++;
                        if (counter > 100)
                        {
                            break;
                        }

                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (Exception e)
                        {
                            // e.printStackTrace();
                        }
                    }

                    try
                    {
                        // [TODO: move this also to Service.]
                        // HINT: seems to work pretty ok now.
                        if (!is_tox_started)
                        {
                            int PREF__orbot_enabled_to_int = 0;
                            if (PREF__orbot_enabled)
                            {
                                PREF__orbot_enabled_to_int = 1;

                                // need to wait for Orbot to be active ...
                                // max 20 seconds!
                                int max_sleep_iterations = 40;
                                int sleep_iteration = 0;
                                while (!OrbotHelper.isOrbotRunning(context_s))
                                {
                                    // sleep 0.5 seconds
                                    sleep_iteration++;
                                    try
                                    {
                                        Thread.sleep(500);
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                    if (sleep_iteration > max_sleep_iterations)
                                    {
                                        // giving up
                                        break;
                                    }
                                }
                            }
                            init(app_files_directory, PREF__udp_enabled, PREF__orbot_enabled_to_int, ORBOT_PROXY_HOST, ORBOT_PROXY_PORT);
                        }

                        tox_service_fg.tox_thread_start_fg();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "tox_thread_start:EE:" + e.getMessage());
        }
    }

    //    static void stop_tox()
    //    {
    //        try
    //        {
    //            Thread t = new Thread()
    //            {
    //                @Override
    //                public void run()
    //                {
    //                    long counter = 0;
    //                    while (tox_service_fg == null)
    //                    {
    //                        counter++;
    //                        if (counter > 100)
    //                        {
    //                            break;
    //                        }
    //
    //                        try
    //                        {
    //                            Thread.sleep(100);
    //                        }
    //                        catch (Exception e)
    //                        {
    //                            e.printStackTrace();
    //                        }
    //                    }
    //
    //                    try
    //                    {
    //
    //                        tox_service_fg.stop_tox_fg();
    //                    }
    //                    catch (Exception e)
    //                    {
    //                        e.printStackTrace();
    //                    }
    //                }
    //            };
    //            t.start();
    //        }
    //        catch (Exception e)
    //        {
    //            e.printStackTrace();
    //            Log.i(TAG, "stop_tox:EE:" + e.getMessage());
    //        }
    //    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // just in case, update own activity pointer!
        main_activity_s = this;
    }

    @Override
    protected void onPause()
    {
        Log.i(TAG, "onPause");
        super.onPause();

        MainActivity.friend_list_fragment = null;
    }

    @Override
    protected void onResume()
    {
        Log.i(TAG, "onResume");
        super.onResume();

        // prefs ----------
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        PREF__UV_reversed = settings.getBoolean("video_uv_reversed", true);
        PREF__notification_sound = settings.getBoolean("notifications_new_message_sound", true);
        PREF__notification_vibrate = settings.getBoolean("notifications_new_message_vibrate", true);
        PREF__notification = settings.getBoolean("notifications_new_message", true);
        PREF__software_echo_cancel = settings.getBoolean("software_echo_cancel", false);
        boolean tmp1 = settings.getBoolean("udp_enabled", false);
        if (tmp1)
        {
            PREF__udp_enabled = 1;
        }
        else
        {
            PREF__udp_enabled = 0;
        }

        try
        {
            if (settings.getString("min_audio_samplingrate_out", "8000").compareTo("Auto") == 0)
            {
                PREF__min_audio_samplingrate_out = 8000;
            }
            else
            {
                PREF__min_audio_samplingrate_out = Integer.parseInt(settings.getString("min_audio_samplingrate_out", "" + MIN_AUDIO_SAMPLINGRATE_OUT));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            PREF__min_audio_samplingrate_out = MIN_AUDIO_SAMPLINGRATE_OUT;
        }
        Log.i(TAG, "PREF__UV_reversed:2=" + PREF__UV_reversed);
        Log.i(TAG, "PREF__min_audio_samplingrate_out:2=" + PREF__min_audio_samplingrate_out);
        // prefs ----------

        try
        {
            profile_d_item.withIcon(get_drawable_from_vfs_image(get_vfs_image_filename_own_avatar()));
            main_drawer_header.updateProfile(profile_d_item);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "onResume:EE1:" + e.getMessage());
            try
            {
                final Drawable d1 = new IconicsDrawable(this).icon(FontAwesome.Icon.faw_lock).color(getResources().getColor(R.color.colorPrimaryDark)).sizeDp(24);
                profile_d_item.withIcon(d1);
                main_drawer_header.updateProfile(profile_d_item);
            }
            catch (Exception e2)
            {
                Log.i(TAG, "onResume:EE2:" + e2.getMessage());
                e2.printStackTrace();
            }
        }

        // just in case, update own activity pointer!
        main_activity_s = this;
    }

    @Override
    protected void onNewIntent(Intent i)
    {
        Log.i(TAG, "onNewIntent:i=" + i);
        super.onNewIntent(i);
    }

    static FriendList main_get_friend(long friendnum)
    {

        String pubkey_temp = tox_friend_get_public_key__wrapper(friendnum);
        Log.i(TAG, "main_get_friend:pubkey=" + pubkey_temp + " fnum=" + friendnum);

        FriendList f;
        List<FriendList> fl = orma.selectFromFriendList().
                tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                toList();

        Log.i(TAG, "main_get_friend:fl=" + fl + " size=" + fl.size());

        if (fl.size() > 0)
        {
            f = fl.get(0);
            Log.i(TAG, "main_get_friend:f=" + f);
        }
        else
        {
            f = null;
        }

        return f;
    }

    static int is_friend_online(long friendnum)
    {
        try
        {
            return (orma.selectFromFriendList().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                    toList().get(0).TOX_CONNECTION);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return 0;
        }

    }

    synchronized static void set_all_friends_offline()
    {

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    orma.updateFriendList().
                            TOX_CONNECTION(0).
                            execute();
                }
                catch (Exception e)
                {
                }

                try
                {
                    friend_list_fragment.set_all_friends_to_offline();
                }
                catch (Exception e)
                {
                }
            }
        };
        t.start();
    }

    synchronized static void update_friend_in_db(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_string(f.tox_public_key_string).
                name(f.name).
                status_message(f.status_message).
                TOX_CONNECTION(f.TOX_CONNECTION).
                TOX_USER_STATUS(f.TOX_USER_STATUS).
                execute();
    }

    synchronized static void update_friend_in_db_status_message(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                status_message(f.status_message).
                execute();
    }

    synchronized static void update_friend_in_db_status(FriendList f)
    {
        Log.i(TAG, "update_friend_in_db_status:f=" + f);

        int numrows = orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                TOX_USER_STATUS(f.TOX_USER_STATUS).
                execute();

        Log.i(TAG, "update_friend_in_db_status:numrows=" + numrows);

    }

    synchronized static void update_friend_in_db_connection_status(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                TOX_CONNECTION(f.TOX_CONNECTION).
                execute();
    }

    synchronized static void update_friend_in_db_name(FriendList f)
    {
        orma.updateFriendList().
                tox_public_key_stringEq(f.tox_public_key_string).
                name(f.name).
                execute();
    }

    synchronized static void update_message_in_db(final Message m)
    {
        final Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    orma.updateMessage().
                            idEq(m.id).
                            read(m.read).
                            text(m.text).
                            sent_timestamp(m.sent_timestamp).
                            rcvd_timestamp(m.rcvd_timestamp).
                            filename_fullpath(m.filename_fullpath).
                            execute();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    static void update_message_in_db_filename_fullpath_friendnum_and_filenum(long friend_number, long file_number, String filename_fullpath)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).orderByIdDesc().get(0).id;

            update_message_in_db_filename_fullpath_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    get(0).id, filename_fullpath);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath_from_id(long msg_id, String filename_fullpath)
    {
        try
        {
            orma.updateMessage().idEq(msg_id).filename_fullpath(filename_fullpath).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_filename_fullpath(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    filename_fullpath(m.filename_fullpath).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_message_in_db_read_rcvd_timestamp(final Message m)
    {
        try
        {
            orma.updateMessage().
                    idEq(m.id).
                    read(m.read).
                    rcvd_timestamp(m.rcvd_timestamp).
                    execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void change_notification(int a_TOXCONNECTION)
    {
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash_app_java(1);
        // crash_app_C();
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash -----------------
        // crash -----------------

        Log.i(TAG, "change_notification");
        final int a_TOXCONNECTION_f = a_TOXCONNECTION;
        try
        {
            Thread t = new Thread()
            {
                @Override
                public void run()
                {
                    long counter = 0;
                    while (tox_service_fg == null)
                    {
                        counter++;
                        if (counter > 10)
                        {
                            break;
                        }
                        // Log.i(TAG, "change_notification:sleep");

                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (Exception e)
                        {
                            // e.printStackTrace();
                        }
                    }
                    Log.i(TAG, "change_notification:change");
                    try
                    {
                        tox_service_fg.change_notification_fg(a_TOXCONNECTION_f);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed()
    {
        if (main_drawer.isDrawerOpen())
        {
            main_drawer.closeDrawer();
        }
        else
        {
            super.onBackPressed();
        }
    }


    // -- this is for incoming video --
    // -- this is for incoming video --
    static void allocate_video_buffer_1(int frame_width_px1, int frame_height_px1, long ystride, long ustride, long vstride)
    {
        if (video_buffer_1 != null)
        {
            // video_buffer_1.clear();
            video_buffer_1 = null;
        }

        if (video_frame_image != null)
        {
            video_frame_image.recycle();
            video_frame_image = null;
        }

        /*
        * YUV420 frame with width * height
        *
        * @param y Luminosity plane. Size = MAX(width, abs(ystride)) * height.
        * @param u U chroma plane. Size = MAX(width/2, abs(ustride)) * (height/2).
        * @param v V chroma plane. Size = MAX(width/2, abs(vstride)) * (height/2).
        */
        int y_layer_size = (int) Math.max(frame_width_px1, Math.abs(ystride)) * frame_height_px1;
        int u_layer_size = (int) Math.max((frame_width_px1 / 2), Math.abs(ustride)) * (frame_height_px1 / 2);
        int v_layer_size = (int) Math.max((frame_width_px1 / 2), Math.abs(vstride)) * (frame_height_px1 / 2);

        int frame_width_px = (int) Math.max(frame_width_px1, Math.abs(ystride));
        int frame_height_px = (int) frame_height_px1;

        buffer_size_in_bytes = y_layer_size + v_layer_size + u_layer_size;

        Log.i(TAG, "YUV420 frame w1=" + frame_width_px1 + " h1=" + frame_height_px1 + " bytes=" + buffer_size_in_bytes);
        Log.i(TAG, "YUV420 frame w=" + frame_width_px + " h=" + frame_height_px + " bytes=" + buffer_size_in_bytes);
        Log.i(TAG, "YUV420 frame ystride=" + ystride + " ustride=" + ustride + " vstride=" + vstride);
        video_buffer_1 = ByteBuffer.allocateDirect(buffer_size_in_bytes);
        set_JNI_video_buffer(video_buffer_1, frame_width_px, frame_height_px);

        RenderScript rs = RenderScript.create(context_s);
        yuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));

        // --------- works !!!!! ---------
        // --------- works !!!!! ---------
        // --------- works !!!!! ---------
        Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(frame_width_px).setY(frame_height_px);
        yuvType.setYuvFormat(ImageFormat.YV12);
        alloc_in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
        Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(frame_width_px).setY(frame_height_px);
        alloc_out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        // --------- works !!!!! ---------
        // --------- works !!!!! ---------
        // --------- works !!!!! ---------

        video_frame_image = Bitmap.createBitmap(frame_width_px, frame_height_px, Bitmap.Config.ARGB_8888);
    }
    // -- this is for incoming video --
    // -- this is for incoming video --


    static
    {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------
    public native void init(@NonNull String data_dir, int udp_enabled, int orbot_enabled, String orbot_host, long orbot_port);

    public native String getNativeLibAPI();

    public static native void update_savedata_file();

    public static native String get_my_toxid();

    public static native void bootstrap();

    public static native int add_tcp_relay_single(String ip, String key_hex, long port);

    public static native int bootstrap_single(String ip, String key_hex, long port);

    public static native void init_tox_callbacks();

    public static native long tox_iteration_interval();

    public static native long tox_iterate();

    public static native long tox_kill();

    public static native void exit();

    public static native long tox_friend_send_message(long friendnum, int a_TOX_MESSAGE_TYPE, @NonNull String message);

    public static native long tox_version_major();

    public static native long tox_version_minor();

    public static native long tox_version_patch();

    public static native String jnictoxcore_version();

    public static native long tox_max_filename_length();

    public static native long tox_file_id_length();

    public static native long tox_max_message_length();

    public static native long tox_friend_add(@NonNull String toxid_str, @NonNull String message);

    public static native long tox_friend_add_norequest(@NonNull String public_key_str);

    public static native long tox_self_get_friend_list_size();

    public static native long tox_friend_by_public_key(@NonNull String friend_public_key_string);

    public static native String tox_friend_get_public_key(long friend_number);

    public static native long[] tox_self_get_friend_list();

    public static native int tox_self_set_name(@NonNull String name);

    public static native int tox_self_set_status_message(@NonNull String status_message);

    public static native void tox_self_set_status(int a_TOX_USER_STATUS);

    public static native int tox_self_set_typing(long friend_number, int typing);

    public static native int tox_friend_get_connection_status(long friend_number);

    public static native int tox_friend_delete(long friend_number);

    public static native String tox_self_get_name();

    public static native long tox_self_get_name_size();

    public static native long tox_self_get_status_message_size();

    public static native String tox_self_get_status_message();

    public static native int tox_file_control(long friend_number, long file_number, int a_TOX_FILE_CONTROL);

    public static native int tox_hash(ByteBuffer hash_buffer, ByteBuffer data_buffer, long data_length);

    public static native int tox_file_seek(long friend_number, long file_number, long position);

    public static native int tox_file_get_file_id(long friend_number, long file_number, ByteBuffer file_id_buffer);

    public static native long tox_file_send(long friend_number, long kind, long file_size, ByteBuffer file_id_buffer, String file_name, long filename_length);

    public static native int tox_file_send_chunk(long friend_number, long file_number, long position, ByteBuffer data_buffer, long data_length);

    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------
    public static native int toxav_answer(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native long toxav_iteration_interval();

    public static native int toxav_call(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native int toxav_bit_rate_set(long friendnum, long audio_bit_rate, long video_bit_rate);

    public static native int toxav_call_control(long friendnum, int a_TOXAV_CALL_CONTROL);

    public static native int toxav_video_send_frame_uv_reversed(long friendnum, int frame_width_px, int frame_height_px);

    public static native int toxav_video_send_frame(long friendnum, int frame_width_px, int frame_height_px);

    public static native long set_JNI_video_buffer(ByteBuffer buffer, int frame_width_px, int frame_height_px);

    public static native void set_JNI_video_buffer2(ByteBuffer buffer2, int frame_width_px, int frame_height_px);

    public static native void set_JNI_audio_buffer(ByteBuffer audio_buffer);

    // buffer2 is for incoming audio
    public static native void set_JNI_audio_buffer2(ByteBuffer audio_buffer2);

    /**
     * Send an audio frame to a friend.
     * <p>
     * The expected format of the PCM data is: [s1c1][s1c2][...][s2c1][s2c2][...]...
     * Meaning: sample 1 for channel 1, sample 1 for channel 2, ...
     * For mono audio, this has no meaning, every sample is subsequent. For stereo,
     * this means the expected format is LRLRLR... with samples for left and right
     * alternating.
     *
     * @param friend_number The friend number of the friend to which to send an
     *                      audio frame.
     * @param sample_count  Number of samples in this frame. Valid numbers here are
     *                      ((sample rate) * (audio length) / 1000), where audio length can be
     *                      2.5, 5, 10, 20, 40 or 60 millseconds.
     * @param channels      Number of audio channels. Supported values are 1 and 2.
     * @param sampling_rate Audio sampling rate used in this frame. Valid sampling
     *                      rates are 8000, 12000, 16000, 24000, or 48000.
     */
    public static native int toxav_audio_send_frame(long friend_number, long sample_count, int channels, long sampling_rate);
    // --------------- AV -------------
    // --------------- AV -------------
    // --------------- AV -------------

    // -------- native methods --------
    // -------- native methods --------
    // -------- native methods --------

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------

    static void android_toxav_callback_call_cb_method(long friend_number, int audio_enabled, int video_enabled)
    {
        if (Callstate.state != 0)
        {
            // don't accept a new call if we already are in a call
            return;
        }


        Log.i(TAG, "toxav_call:from=" + friend_number + " audio=" + audio_enabled + " video=" + video_enabled);
        final long fn = friend_number;
        final int f_audio_enabled = audio_enabled;
        final int f_video_enabled = video_enabled;

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (Callstate.state == 0)
                    {
                        Log.i(TAG, "CALL:start:show activity");
                        Callstate.state = 1;
                        Callstate.accepted_call = 0;
                        Callstate.call_first_video_frame_received = -1;
                        Callstate.call_first_audio_frame_received = -1;
                        Callstate.call_start_timestamp = -1;
                        Callstate.audio_speaker = true;
                        Callstate.other_audio_enabled = 1;
                        Callstate.other_video_enabled = 1;
                        Callstate.my_audio_enabled = 1;
                        Callstate.my_video_enabled = 1;
                        Intent intent = new Intent(context_s, CallingActivity.class);
                        Callstate.friend_pubkey = tox_friend_get_public_key__wrapper(fn);
                        try
                        {
                            Callstate.friend_name = orma.selectFromFriendList().
                                    tox_public_key_stringEq(Callstate.friend_pubkey).
                                    toList().get(0).name;
                        }
                        catch (Exception e)
                        {
                            Callstate.friend_name = "Unknown";
                            e.printStackTrace();
                        }
                        Callstate.other_audio_enabled = f_audio_enabled;
                        Callstate.other_video_enabled = f_video_enabled;
                        Callstate.call_init_timestamp = System.currentTimeMillis();
                        main_activity_s.startActivityForResult(intent, CallingActivity_ID);
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "CALL:start:EE:" + e.getMessage());
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    static void android_toxav_callback_video_receive_frame_cb_method(long friend_number, long frame_width_px, long frame_height_px, long ystride, long ustride, long vstride)
    {
        if (Callstate.other_video_enabled == 0)
        {
            return;
        }

        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        // Log.i(TAG, "toxav_video_receive_frame:from=" + friend_number + " video width=" + frame_width_px + " video height=" + frame_height_px);
        if (Callstate.call_first_video_frame_received == -1)
        {
            Callstate.call_first_video_frame_received = System.currentTimeMillis();

            // allocate new video buffer on 1 frame
            allocate_video_buffer_1((int) frame_width_px, (int) frame_height_px, ystride, ustride, vstride);

            temp_string_a = "" + (int) ((Callstate.call_first_video_frame_received - Callstate.call_start_timestamp) / 1000) + "s";
            CallingActivity.update_top_text_line(temp_string_a, 3);
        }

        try
        {
            alloc_in.copyFrom(video_buffer_1.array());
            yuvToRgb.setInput(alloc_in);
            yuvToRgb.forEach(alloc_out);
            alloc_out.copyTo(video_frame_image);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    CallingActivity.mContentView.setImageBitmap(video_frame_image);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    static void android_toxav_callback_call_state_cb_method(long friend_number, int a_TOXAV_FRIEND_CALL_STATE)
    {
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        Log.i(TAG, "toxav_call_state:from=" + friend_number + " state=" + a_TOXAV_FRIEND_CALL_STATE);
        Log.i(TAG, "Callstate.tox_call_state=" + a_TOXAV_FRIEND_CALL_STATE + " old=" + Callstate.tox_call_state);

        if (Callstate.state == 1)
        {
            int old_value = Callstate.tox_call_state;
            Callstate.tox_call_state = a_TOXAV_FRIEND_CALL_STATE;

            if ((a_TOXAV_FRIEND_CALL_STATE & (4 + 8 + 16 + 32)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call starting");
                Callstate.call_start_timestamp = System.currentTimeMillis();

                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            CallingActivity.accept_button.setVisibility(View.GONE);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };
                CallingActivity.callactivity_handler_s.post(myRunnable);
            }
            else if ((a_TOXAV_FRIEND_CALL_STATE & (2)) > 0)
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(1)");
                close_calling_activity();
            }
            else if ((old_value > 0) && (a_TOXAV_FRIEND_CALL_STATE == 0))
            {
                Log.i(TAG, "toxav_call_state:from=" + friend_number + " call ending(2)");
                close_calling_activity();
            }

        }
    }

    static void android_toxav_callback_bit_rate_status_cb_method(long friend_number, long audio_bit_rate, long video_bit_rate)
    {
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        Log.i(TAG, "toxav_bit_rate_status:from=" + friend_number + " audio_bit_rate=" + audio_bit_rate + " video_bit_rate=" + video_bit_rate);

        // TODO: for now ignore suggested bitrates!!!! ---------------
        if (Callstate.state == 1)
        {
            final long friend_number_ = friend_number;

            long audio_bit_rate2 = audio_bit_rate;
            long video_bit_rate2 = video_bit_rate;

            if (audio_bit_rate2 < GLOBAL_MIN_AUDIO_BITRATE)
            {
                audio_bit_rate2 = GLOBAL_MIN_AUDIO_BITRATE;
            }

            if (video_bit_rate2 < GLOBAL_MIN_VIDEO_BITRATE)
            {
                video_bit_rate2 = GLOBAL_MIN_VIDEO_BITRATE;
            }

            final long audio_bit_rate_ = audio_bit_rate2;
            final long video_bit_rate_ = video_bit_rate2;

            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // set audio and video bitrate according to suggestion from c-toxcore
                        toxav_bit_rate_set(friend_number_, audio_bit_rate_, video_bit_rate_);

                        Callstate.audio_bitrate = audio_bit_rate_;
                        Callstate.video_bitrate = video_bit_rate_;
                        update_bitrates();

                        Log.i(TAG, "toxav_bit_rate_status:CALL:toxav_bit_rate_set");
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i(TAG, "toxav_bit_rate_status:CALL:EE:" + e.getMessage());
                    }
                }
            };

            if (main_handler_s != null)
            {
                main_handler_s.post(myRunnable);
            }
        }
        // TODO: for now ignore suggested bitrates!!!! ---------------
    }

    static void android_toxav_callback_audio_receive_frame_cb_method(long friend_number, long sample_count, int channels, long sampling_rate)
    {
        if (tox_friend_by_public_key__wrapper(Callstate.friend_pubkey) != friend_number)
        {
            // not the friend we are in call with now
            return;
        }

        if (Callstate.other_audio_enabled == 0)
        {
            if (Callstate.call_first_audio_frame_received == -1)
            {
                sampling_rate_ = sampling_rate;
                Log.i(TAG, "audio_play:read:incoming sampling_rate[0]=" + sampling_rate + " kHz");
                channels_ = channels;
            }

            return;
        }

        if (Callstate.call_first_audio_frame_received == -1)
        {
            Callstate.call_first_audio_frame_received = System.currentTimeMillis();

            sampling_rate_ = sampling_rate;
            Log.i(TAG, "audio_play:read:incoming sampling_rate[1]=" + sampling_rate + " Hz");
            channels_ = channels;

            Log.i(TAG, "audio_play:read:init sample_count=" + sample_count + " channels=" + channels + " sampling_rate=" + sampling_rate);


            temp_string_a = "" + (int) ((Callstate.call_first_audio_frame_received - Callstate.call_start_timestamp) / 1000) + "s";
            CallingActivity.update_top_text_line(temp_string_a, 4);

            // HINT: PCM_16 needs 2 bytes per sample per channel
            AudioReceiver.buffer_size = ((int) ((sample_count * channels) * 2)) * audio_out_buffer_mult; // TODO: this is really bad
            AudioReceiver.sleep_millis = (int) (((float) sample_count / (float) sampling_rate) * 1000.0f * 0.9f); // TODO: this is bad also
            Log.i(TAG, "audio_play:read:init buffer_size=" + AudioReceiver.buffer_size);
            Log.i(TAG, "audio_play:read:init sleep_millis=" + AudioReceiver.sleep_millis);

            // reset audio in buffers
            int i = 0;
            for (i = 0; i < audio_in_buffer_max_count; i++)
            {
                try
                {
                    if (audio_buffer_2 != null)
                    {
                        if (audio_buffer_2[i] != null)
                        {
                            audio_buffer_2[i].clear();
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                try
                {
                    audio_buffer_2[i] = null;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                audio_buffer_2[i] = ByteBuffer.allocateDirect(AudioReceiver.buffer_size);
                audio_buffer_2_read_length[i] = 0;
                Log.i(TAG, "audio_play:audio_buffer_2[" + i + "] size=" + AudioReceiver.buffer_size);
            }

            audio_in_buffer_element_count = 0;
            audio_buffer_play = ByteBuffer.allocateDirect(AudioReceiver.buffer_size);

            // always write to buffer[0] in the pipeline !! -----------
            set_JNI_audio_buffer2(audio_buffer_2[0]);
            // always write to buffer[0] in the pipeline !! -----------

            Log.i(TAG, "audio_play:audio_buffer_play size=" + AudioReceiver.buffer_size);
        }

        // TODO: dirty hack, "make good"
        try
        {
            // audio_buffer_read_write(sample_count, channels, sampling_rate, true);
            if (audio_receiver_thread != null)
            {
                if (!audio_receiver_thread.stopped)
                {
                    audio_receiver_thread.track.write(audio_buffer_2[0].array(), 0, (int) ((sample_count * channels) * 2));
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "audio_play:EE3:" + e.getMessage());
        }
    }

    // -------- called by AV native methods --------
    // -------- called by AV native methods --------
    // -------- called by AV native methods --------


    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    static void android_tox_callback_self_connection_status_cb_method(int a_TOX_CONNECTION)
    {
        Log.i(TAG, "self_connection_status:" + a_TOX_CONNECTION);

        if (bootstrapping)
        {
            Log.i(TAG, "self_connection_status:bootstrapping=true");
            // we just went online
            if (a_TOX_CONNECTION != 0)
            {
                Log.i(TAG, "self_connection_status:bootstrapping set to false");
                bootstrapping = false;
            }
        }

        // -- notification ------------------
        // -- notification ------------------
        change_notification(a_TOX_CONNECTION);
        // -- notification ------------------
        // -- notification ------------------
    }

    static void android_tox_callback_friend_name_cb_method(long friend_number, String friend_name, long length)
    {
        Log.i(TAG, "friend_name:friend:" + friend_number + " name:" + friend_name);

        FriendList f = main_get_friend(friend_number);
        Log.i(TAG, "friend_name:002:" + f);
        if (f != null)
        {
            f.name = friend_name;
            update_friend_in_db_name(f);
            if (friend_list_fragment != null)
            {
                Log.i(TAG, "friend_name:003");
                friend_list_fragment.modify_friend(f, friend_number);
            }
        }

    }

    static void android_tox_callback_friend_status_message_cb_method(long friend_number, String status_message, long length)
    {
        Log.i(TAG, "friend_status_message:friend:" + friend_number + " status message:" + status_message);

        FriendList f = main_get_friend(friend_number);
        if (f != null)
        {
            f.status_message = status_message;
            update_friend_in_db_status_message(f);
            if (friend_list_fragment != null)
            {
                friend_list_fragment.modify_friend(f, friend_number);
            }
        }
    }

    static void android_tox_callback_friend_status_cb_method(long friend_number, int a_TOX_USER_STATUS)
    {
        Log.i(TAG, "friend_status:friend:" + friend_number + " status:" + a_TOX_USER_STATUS);

        FriendList f = main_get_friend(friend_number);
        Log.i(TAG, "friend_status:f=" + f);
        Log.i(TAG, "friend_status:1:f.TOX_USER_STATUS=" + f.TOX_USER_STATUS);

        if (f != null)
        {
            f.TOX_USER_STATUS = a_TOX_USER_STATUS;
            Log.i(TAG, "friend_status:2:f.TOX_USER_STATUS=" + f.TOX_USER_STATUS);
            update_friend_in_db_status(f);

            try
            {
                Log.i(TAG, "friend_status:002");
                message_list_activity.set_friend_status_icon();
                Log.i(TAG, "friend_status:003");
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                Log.i(TAG, "friend_status:EE1:" + e.getMessage());
            }

            try
            {
                Log.i(TAG, "friend_status:004");
                friend_list_fragment.modify_friend(f, friend_number);
                Log.i(TAG, "friend_status:004");
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                Log.i(TAG, "friend_status:EE2:" + e.getMessage());
            }
        }
    }

    static void android_tox_callback_friend_connection_status_cb_method(long friend_number, int a_TOX_CONNECTION)
    {
        Log.i(TAG, "friend_connection_status:friend:" + friend_number + " connection status:" + a_TOX_CONNECTION);
        FriendList f = main_get_friend(friend_number);
        if (f != null)
        {
            if (f.TOX_CONNECTION != a_TOX_CONNECTION)
            {
                if (f.TOX_CONNECTION == TOX_CONNECTION_NONE.value)
                {

                    final long friend_number_ = friend_number;
                    final Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                // friend just came online
                                if (VFS_ENCRYPT)
                                {
                                    String fname = get_vfs_image_filename_own_avatar();
                                    if (fname != null)
                                    {
                                        ByteBuffer avatar_bytes = file_to_bytebuffer(fname, true);
                                        if (avatar_bytes != null)
                                        {
                                            // Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:avatar_bytes=" + bytes_to_hex(avatar_bytes));

                                            ByteBuffer hash_bytes = ByteBuffer.allocateDirect(TOX_HASH_LENGTH);
                                            int res = tox_hash(hash_bytes, avatar_bytes, avatar_bytes.capacity());
                                            if (res == 0)
                                            {
                                                Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:hash(1)=" + bytes_to_hex(hash_bytes));


                                                // send avatar to friend -------
                                                long filenum = tox_file_send(friend_number_, TOX_FILE_KIND_AVATAR.value, avatar_bytes.capacity(), hash_bytes, "avatar.png", "avatar.png".length());
                                                Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:filenum=" + filenum);

                                                // save FT to db ---------------
                                                Filetransfer ft_avatar_outgoing = new Filetransfer();
                                                ft_avatar_outgoing.tox_public_key_string = tox_friend_get_public_key__wrapper(friend_number_);
                                                ft_avatar_outgoing.direction = TRIFA_FT_DIRECTION_OUTGOING.value;
                                                ft_avatar_outgoing.file_number = filenum;
                                                ft_avatar_outgoing.kind = TOX_FILE_KIND_AVATAR.value;
                                                ft_avatar_outgoing.filesize = avatar_bytes.capacity();
                                                long rowid = insert_into_filetransfer_db(ft_avatar_outgoing);
                                            }
                                            else
                                            {
                                                Log.i(TAG, "android_tox_callback_friend_connection_status_cb_method:tox_hash res=" + res);
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    // TODO: write code
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    };
                    main_handler_s.post(myRunnable);
                }
            }

            f.TOX_CONNECTION = a_TOX_CONNECTION;
            update_friend_in_db_connection_status(f);

            try
            {
                if (message_list_activity != null)
                {
                    message_list_activity.set_friend_connection_status_icon();
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            try
            {
                if (friend_list_fragment != null)
                {
                    friend_list_fragment.modify_friend(f, friend_number);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    static void android_tox_callback_friend_typing_cb_method(long friend_number, final int typing)
    {
        Log.i(TAG, "friend_typing_cb:fn=" + friend_number + " typing=" + typing);
        Runnable myRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (message_list_activity != null)
                    {
                        if (ml_friend_typing != null)
                        {
                            if (typing == 1)
                            {
                                ml_friend_typing.setText("friend is typing ...");
                            }
                            else
                            {
                                ml_friend_typing.setText("");
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    // e.printStackTrace();
                    Log.i(TAG, "friend_typing_cb:EE:" + e.getMessage());
                }
            }
        };

        if (main_handler_s != null)
        {
            main_handler_s.post(myRunnable);
        }
    }

    static void android_tox_callback_friend_read_receipt_cb_method(long friend_number, long message_id)
    {
        Log.i(TAG, "friend_read_receipt:friend:" + friend_number + " message_id:" + message_id);

        try
        {
            // there can be older messages with same message_id for this friend! so always take the latest one! -------
            final Message m = orma.selectFromMessage().
                    message_idEq(message_id).
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    directionEq(1).
                    orderByIdDesc().
                    toList().get(0);
            // there can be older messages with same message_id for this friend! so always take the latest one! -------

            // Log.i(TAG, "friend_read_receipt:m=" + m);
            Log.i(TAG, "friend_read_receipt:m:message_id=" + m.message_id + " text=" + m.text + " friendpubkey=" + m.tox_friendpubkey + " read=" + m.read + " direction=" + m.direction);

            if (m != null)
            {
                Runnable myRunnable = new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            m.rcvd_timestamp = System.currentTimeMillis();
                            m.read = true;
                            update_message_in_db_read_rcvd_timestamp(m);

                            // TODO this updates all messages. should be done nicer and faster!
                            // update_message_view();
                            update_single_message(m, true);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                };

                if (main_handler_s != null)
                {
                    main_handler_s.post(myRunnable);
                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "friend_read_receipt:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static void android_tox_callback_friend_request_cb_method(String friend_public_key, String friend_request_message, long length)
    {
        Log.i(TAG, "friend_request:friend:" + friend_public_key + " friend request message:" + friend_request_message);
        Log.i(TAG, "friend_request:friend:" + friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2) + " friend request message:" + friend_request_message);

        final String friend_public_key__final = friend_public_key.substring(0, TOX_PUBLIC_KEY_SIZE * 2);

        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    // toxcore needs this!!
                    Thread.sleep(120);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----
                long friendnum = tox_friend_add_norequest(friend_public_key__final); // add friend

                try
                {
                    Thread.sleep(20);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                update_savedata_file(); // save toxcore datafile (new friend added)

                final FriendList f = new FriendList();
                f.tox_public_key_string = friend_public_key__final;
                f.TOX_USER_STATUS = 0;
                f.TOX_CONNECTION = 0;
                // set name as the last 5 char of the publickey (until we get a proper name)
                f.name = friend_public_key__final.substring(friend_public_key__final.length() - 5, friend_public_key__final.length());
                f.avatar_pathname = null;
                f.avatar_filename = null;

                try
                {
                    Log.i(TAG, "friend_request:insert:001:f=" + f);
                    long res = orma.insertIntoFriendList(f);
                    Log.i(TAG, "friend_request:insert:002:res=" + res);
                }
                catch (android.database.sqlite.SQLiteConstraintException e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "friend_request:insert:EE1:" + e.getMessage());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i(TAG, "friend_request:insert:EE2:" + e.getMessage());
                }

                if (friend_list_fragment != null)
                {
                    Log.i(TAG, "friend_request:003");
                    friend_list_fragment.modify_friend(f, friendnum);
                }

                // ---- auto add all friends ----
                // ---- auto add all friends ----
                // ---- auto add all friends ----
            }
        };
        t.start();
    }

    static void android_tox_callback_friend_message_cb_method(long friend_number, int message_type, String friend_message, long length)
    {
        Log.i(TAG, "friend_message:friend:" + friend_number + " message:" + friend_message);

        // if message list for this friend is open, then don't do notification and "new" badge
        boolean do_notification = true;
        boolean do_badge_update = true;
        Log.i(TAG, "noti_and_badge:001:" + message_list_activity);
        if (message_list_activity != null)
        {
            Log.i(TAG, "noti_and_badge:002:" + message_list_activity.get_current_friendnum() + ":" + friend_number);
            if (message_list_activity.get_current_friendnum() == friend_number)
            {
                Log.i(TAG, "noti_and_badge:003:");
                // no notifcation and no badge update
                do_notification = false;
                do_badge_update = false;
            }
        }

        Message m = new Message();

        if (!do_badge_update)
        {
            Log.i(TAG, "noti_and_badge:004a:");
            m.is_new = false;
        }
        else
        {
            Log.i(TAG, "noti_and_badge:004b:");
            m.is_new = true;
        }

        // m.tox_friendnum = friend_number;
        m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friend_number);
        m.direction = 0; // msg received
        m.TOX_MESSAGE_TYPE = 0;
        m.read = false;
        m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_TYPE_TEXT.value;
        m.rcvd_timestamp = System.currentTimeMillis();
        m.text = friend_message;

        insert_into_message_db(m, true);

        try
        {
            // update "new" status on friendlist fragment
            FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
            if (friend_list_fragment != null)
            {
                if (f != null)
                {
                    friend_list_fragment.modify_friend(f, friend_number);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
        }

        if (do_notification)
        {
            Log.i(TAG, "noti_and_badge:005:");

            // start "new" notification
            Runnable myRunnable = new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        // allow notification every n seconds
                        if ((Notification_new_message_last_shown_timestamp + Notification_new_message_every_millis) < System.currentTimeMillis())
                        {

                            if (PREF__notification)
                            {
                                Notification_new_message_last_shown_timestamp = System.currentTimeMillis();

                                Intent notificationIntent = new Intent(context_s, MainActivity.class);
                                notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                PendingIntent pendingIntent = PendingIntent.getActivity(context_s, 0, notificationIntent, 0);

                                // -- notification ------------------
                                // -- notification ------------------

                                NotificationCompat.Builder b = new NotificationCompat.Builder(context_s);
                                b.setContentIntent(pendingIntent);
                                b.setSmallIcon(R.drawable.circle_orange);
                                b.setLights(Color.parseColor("#ffce00"), 500, 500);
                                Uri default_notification_sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                if (PREF__notification_sound)
                                {
                                    b.setSound(default_notification_sound);
                                }

                                if (PREF__notification_vibrate)
                                {
                                    long[] vibrate_pattern = {100, 300};
                                    b.setVibrate(vibrate_pattern);
                                }

                                b.setContentTitle("TRIfA");
                                b.setAutoCancel(true);
                                b.setContentText("new Message");

                                Notification notification3 = b.build();
                                nmn3.notify(Notification_new_message_ID, notification3);
                                // -- notification ------------------
                                // -- notification ------------------
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            };

            try
            {
                if (main_handler_s != null)
                {
                    main_handler_s.post(myRunnable);
                }
            }
            catch (Exception e)
            {
            }
        }
    }

    static void android_tox_callback_file_recv_control_cb_method(long friend_number, long file_number, int a_TOX_FILE_CONTROL)
    {
        Log.i(TAG, "file_recv_control:" + friend_number + ":fn==" + file_number + ":" + a_TOX_FILE_CONTROL);

        if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_CANCEL.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_CANCEL");
            cancel_filetransfer(friend_number, file_number);
        }
        else if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_RESUME.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME");

            long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:ft_id=" + ft_id);
            long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_RESUME:msg_id=" + msg_id);
            set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_RESUME.value);
            set_message_state_from_id(msg_id, TOX_FILE_CONTROL_RESUME.value);

            // update_all_messages_global(true);
            try
            {
                if (ft_id != -1)
                {
                    update_single_message_from_messge_id(msg_id, true);
                }
            }
            catch (Exception e)
            {
            }
        }
        else if (a_TOX_FILE_CONTROL == TOX_FILE_CONTROL_PAUSE.value)
        {
            Log.i(TAG, "file_recv_control:TOX_FILE_CONTROL_PAUSE");

            long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
            long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
            set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_PAUSE.value);
            set_message_state_from_id(msg_id, TOX_FILE_CONTROL_PAUSE.value);

            // update_all_messages_global(true);
            try
            {
                if (ft_id != -1)
                {
                    update_single_message_from_messge_id(msg_id, true);
                }
            }
            catch (Exception e)
            {
            }

        }
    }

    static void android_tox_callback_file_chunk_request_cb_method(long friend_number, long file_number, long position, long length)
    {
        Log.i(TAG, "file_chunk_request:" + friend_number + ":" + file_number + ":" + position + ":" + length);

        // TODO: we must send a chunck of that file ...

        try
        {
            Filetransfer ft = orma.selectFromFiletransfer().
                    directionEq(TRIFA_FT_DIRECTION_OUTGOING.value).
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    file_numberEq(file_number).
                    stateNotEq(TOX_FILE_CONTROL_CANCEL.value).
                    orderByIdDesc().
                    toList().get(0);

            if (ft == null)
            {
                Log.i(TAG, "file_chunk_request:ft=NULL");
                return;
            }

            if (ft.kind == TOX_FILE_KIND_AVATAR.value)
            {
                if (length == 0)
                {
                    // avatar transfer finished -----------
                    orma.deleteFromFiletransfer().idEq(ft.id);
                    // avatar transfer finished -----------

                    ByteBuffer avatar_chunk = ByteBuffer.allocateDirect(1);
                    int res = tox_file_send_chunk(friend_number, file_number, position, avatar_chunk, 0);
                    Log.i(TAG, "file_chunk_request:res(2)=" + res);
                }
                else
                {
                    // TODO: this is really aweful and slow. FIX ME -------------
                    if (VFS_ENCRYPT)
                    {
                        final String fname = get_vfs_image_filename_own_avatar();
                        if (fname != null)
                        {
                            final ByteBuffer avatar_bytes = file_to_bytebuffer(fname, true);
                            if (avatar_bytes != null)
                            {
                                long avatar_chunk_length = length;
                                byte[] bytes_chunck = new byte[(int) avatar_chunk_length];
                                avatar_bytes.position((int) position);
                                avatar_bytes.get(bytes_chunck, 0, (int) avatar_chunk_length);
                                ByteBuffer avatar_chunk = ByteBuffer.allocateDirect((int) avatar_chunk_length);
                                avatar_chunk.put(bytes_chunck);
                                int res = tox_file_send_chunk(friend_number, file_number, position, avatar_chunk, avatar_chunk_length);
                                Log.i(TAG, "file_chunk_request:res(1)=" + res);
                                // int res = tox_hash(hash_bytes, avatar_bytes, avatar_bytes.capacity());
                            }
                        }
                    }
                }
            }
            // TODO: this is really aweful and slow. FIX ME -------------
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "file_chunk_request:EE1:" + e.getMessage());
        }
    }

    static void android_tox_callback_file_recv_cb_method(long friend_number, long file_number, int a_TOX_FILE_KIND, long file_size, String filename, long filename_length)
    {
        Log.i(TAG, "file_recv:" + friend_number + ":fn==" + file_number + ":" + a_TOX_FILE_KIND + ":" + file_size + ":" + filename + ":" + filename_length);

        if (a_TOX_FILE_KIND == TOX_FILE_KIND_AVATAR.value)
        {
            Log.i(TAG, "file_recv:incoming avatar");

            String file_name_avatar = "_____xyz____avatar.png";

            Filetransfer f = new Filetransfer();
            f.tox_public_key_string = tox_friend_get_public_key__wrapper(friend_number);
            f.direction = TRIFA_FT_DIRECTION_INCOMING.value;
            f.file_number = file_number;
            f.kind = a_TOX_FILE_KIND;
            f.message_id = -1;
            f.state = TOX_FILE_CONTROL_RESUME.value;
            f.path_name = VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + f.tox_public_key_string + "/";
            f.file_name = file_name_avatar;
            f.filesize = file_size;
            f.current_position = 0;

            insert_into_filetransfer_db(f);

            // TODO: we just accept incoming avatar, maybe make some checks first?
            tox_file_control(friend_number, file_number, TOX_FILE_CONTROL_RESUME.value);
        }
        else // DATA file ft
        {
            Log.i(TAG, "file_recv:incoming regular file");

            Filetransfer f = new Filetransfer();
            f.tox_public_key_string = tox_friend_get_public_key__wrapper(friend_number);
            f.direction = TRIFA_FT_DIRECTION_INCOMING.value;
            f.file_number = file_number;
            f.kind = a_TOX_FILE_KIND;
            f.state = TOX_FILE_CONTROL_PAUSE.value;
            f.path_name = VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + f.tox_public_key_string + "/";
            f.file_name = filename;
            f.filesize = file_size;
            f.ft_accepted = false;
            f.ft_outgoing_started = false; // dummy for incoming FTs, but still set it here
            f.current_position = 0;

            long ft_id = insert_into_filetransfer_db(f);

            // add FT message to UI
            Message m = new Message();

            m.tox_friendpubkey = tox_friend_get_public_key__wrapper(friend_number);
            m.direction = 0; // msg received
            m.TOX_MESSAGE_TYPE = 0;
            m.TRIFA_MESSAGE_TYPE = TRIFA_MSG_FILE.value;
            m.filetransfer_id = ft_id;
            m.filedb_id = -1;
            m.state = TOX_FILE_CONTROL_PAUSE.value;
            m.ft_accepted = false;
            m.ft_outgoing_started = false; // dummy for incoming FTs, but still set it here
            m.rcvd_timestamp = System.currentTimeMillis();
            m.text = filename + "\n" + file_size + " bytes";

            long new_msg_id = insert_into_message_db(m, true);

            f.message_id = new_msg_id;
            update_filetransfer_db_full(f);

            try
            {
                // update "new" status on friendlist fragment
                FriendList f2 = orma.selectFromFriendList().tox_public_key_stringEq(m.tox_friendpubkey).toList().get(0);
                friend_list_fragment.modify_friend(f2, friend_number);
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "update *new* status:EE1:" + e.getMessage());
            }
        }
    }

    static void android_tox_callback_file_recv_chunk_cb_method(long friend_number, long file_number, long position, byte[] data, long length)
    {
        // Log.i(TAG, "file_recv_chunk:" + friend_number + ":fn==" + file_number + ":position=" + position + ":length=" + length + ":data len=" + data.length + ":data=" + data);
        // Log.i(TAG, "file_recv_chunk:--START--");

        // Log.i(TAG, "file_recv_chunk:" + friend_number + ":" + file_number + ":" + position + ":" + length);

        Filetransfer f = null;
        try
        {
            f = orma.selectFromFiletransfer().
                    file_numberEq(file_number).
                    and().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().
                    toList().get(0);

            // Log.i(TAG, "file_recv_chunk:filesize==" + f.filesize);

            if (position == 0)
            {
                Log.i(TAG, "file_recv_chunk:START-O-F:filesize==" + f.filesize);

                // file start. just to be sure, make directories
                if (VFS_ENCRYPT)
                {
                    info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(f.path_name + "/" + f.file_name);
                    info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(f1.getParent());
                    // Log.i(TAG, "file_recv_chunk:f1=" + f1.getAbsolutePath());
                    // Log.i(TAG, "file_recv_chunk:f2=" + f2.getAbsolutePath());
                    f2.mkdirs();
                }
                else
                {
                    java.io.File f1 = new java.io.File(f.path_name + "/" + f.file_name);
                    java.io.File f2 = new java.io.File(f1.getParent());
                    // Log.i(TAG, "file_recv_chunk:f1=" + f1.getAbsolutePath());
                    // Log.i(TAG, "file_recv_chunk:f2=" + f2.getAbsolutePath());
                    f2.mkdirs();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (length == 0)
        {
            Log.i(TAG, "file_recv_chunk:END-O-F:filesize==" + f.filesize);

            try
            {
                Log.i(TAG, "file_recv_chunk:file fully received");

                if (VFS_ENCRYPT)
                {
                    info.guardianproject.iocipher.FileOutputStream fos = null;
                    fos = cache_ft_fos.get(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);

                    if (f.fos_open)
                    {
                        try
                        {
                            fos.close();
                        }
                        catch (Exception e3)
                        {
                            Log.i(TAG, "file_recv_chunk:EE3:" + e3.getMessage());
                        }
                    }
                    f.fos_open = false;
                }
                else
                {
                    java.io.FileOutputStream fos = null;
                    fos = cache_ft_fos_normal.get(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);

                    if (f.fos_open)
                    {
                        try
                        {
                            fos.close();
                        }
                        catch (Exception e3)
                        {
                            Log.i(TAG, "file_recv_chunk:EE3:" + e3.getMessage());
                        }
                    }
                    f.fos_open = false;
                }

                update_filetransfer_db_fos_open(f);

                move_tmp_file_to_real_file(f.path_name, f.file_name, VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/", f.file_name);

                long filedb_id = -1;
                if (f.kind != TOX_FILE_KIND_AVATAR.value)
                {
                    // put into "FileDB" table
                    FileDB file_ = new FileDB();
                    file_.kind = f.kind;
                    file_.direction = f.direction;
                    file_.tox_public_key_string = f.tox_public_key_string;
                    file_.path_name = VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/";
                    file_.file_name = f.file_name;
                    file_.filesize = f.filesize;
                    long row_id = orma.insertIntoFileDB(file_);
                    Log.i(TAG, "file_recv_chunk:FileDB:row_id=" + row_id);
                    filedb_id = orma.selectFromFileDB().tox_public_key_stringEq(f.tox_public_key_string).and().file_nameEq(f.file_name).orderByIdDesc().get(0).id;
                    Log.i(TAG, "file_recv_chunk:FileDB:filedb_id=" + filedb_id);
                }

                Log.i(TAG, "file_recv_chunk:kind=" + f.kind);
                if (f.kind == TOX_FILE_KIND_AVATAR.value)
                {
                    set_friend_avatar(tox_friend_get_public_key__wrapper(friend_number), VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/", f.file_name);
                }
                else
                {
                    Log.i(TAG, "file_recv_chunk:file_READY:001:f.id=" + f.id);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(f.id, friend_number);
                    Log.i(TAG, "file_recv_chunk:file_READY:001a:msg_id=" + msg_id);

                    update_message_in_db_filename_fullpath_friendnum_and_filenum(friend_number, file_number, VFS_PREFIX + VFS_FILE_DIR + "/" + f.tox_public_key_string + "/" + f.file_name);
                    set_message_state_from_friendnum_and_filenum(friend_number, file_number, TOX_FILE_CONTROL_CANCEL.value);
                    set_message_filedb_from_friendnum_and_filenum(friend_number, file_number, filedb_id);
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);

                    try
                    {
                        Log.i(TAG, "file_recv_chunk:file_READY:002");
                        if (f.id != -1)
                        {
                            Log.i(TAG, "file_recv_chunk:file_READY:003:f.id=" + f.id + " msg_id=" + msg_id);
                            update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                        Log.i(TAG, "file_recv_chunk:file_READY:EE:" + e.getMessage());
                    }

                }

                // remove FT from DB
                delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);

            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "file_recv_chunk:EE2:" + e2.getMessage());
            }
        }
        else // normal chunck recevied ---------- (NOT start, and NOT end)
        {
            try
            {
                if (VFS_ENCRYPT)
                {
                    info.guardianproject.iocipher.FileOutputStream fos = null;
                    if (!f.fos_open)
                    {
                        fos = new info.guardianproject.iocipher.FileOutputStream(f.path_name + "/" + f.file_name);
                        Log.i(TAG, "file_recv_chunk:new fos[1]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                        cache_ft_fos.put(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number, fos);
                        f.fos_open = true;
                        update_filetransfer_db_fos_open(f);
                    }
                    else
                    {
                        fos = cache_ft_fos.get(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);
                        if (fos == null)
                        {
                            fos = new info.guardianproject.iocipher.FileOutputStream(f.path_name + "/" + f.file_name);
                            Log.i(TAG, "file_recv_chunk:new fos[2]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                            cache_ft_fos.put(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number, fos);
                            f.fos_open = true;
                            update_filetransfer_db_fos_open(f);
                        }
                        // Log.i(TAG, "file_recv_chunk:fos=" + fos + " file=" + f.path_name + "/" + f.file_name);
                    }

                    // Log.i(TAG, "file_recv_chunk:fos:" + fos);
                    fos.write(data);
                }
                else
                {
                    java.io.FileOutputStream fos = null;
                    if (!f.fos_open)
                    {
                        fos = new java.io.FileOutputStream(f.path_name + "/" + f.file_name);
                        Log.i(TAG, "file_recv_chunk:new fos[3]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                        cache_ft_fos_normal.put(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number, fos);
                        f.fos_open = true;
                        update_filetransfer_db_fos_open(f);
                    }
                    else
                    {
                        fos = cache_ft_fos_normal.get(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number);
                        if (fos == null)
                        {
                            fos = new java.io.FileOutputStream(f.path_name + "/" + f.file_name);
                            Log.i(TAG, "file_recv_chunk:new fos[4]=" + fos + " file=" + f.path_name + "/" + f.file_name);
                            cache_ft_fos_normal.put(tox_friend_get_public_key__wrapper(friend_number) + ":" + file_number, fos);
                            f.fos_open = true;
                            update_filetransfer_db_fos_open(f);
                        }
                        // Log.i(TAG, "file_recv_chunk:fos=" + fos + " file=" + f.path_name + "/" + f.file_name);
                    }

                    // Log.i(TAG, "file_recv_chunk:fos:" + fos);
                    fos.write(data);
                }

                f.current_position = position;
                // Log.i(TAG, "file_recv_chunk:filesize==:2:" + f.filesize);
                update_filetransfer_db_current_position(f);

                if (f.kind != TOX_FILE_KIND_AVATAR.value)
                {
                    // update_all_messages_global(false);
                    try
                    {
                        if (f.id != -1)
                        {
                            update_single_message_from_ftid(f.id, false);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                Log.i(TAG, "file_recv_chunk:EE1:" + e.getMessage());
            }
        }

        // Log.i(TAG, "file_recv_chunk:--END--");
    }

    // void test(int i)
    // {
    //    Log.i(TAG, "test:" + i);
    // }

    static void android_tox_log_cb_method(int a_TOX_LOG_LEVEL, String file, long line, String function, String message)
    {
        if (CTOXCORE_NATIVE_LOGGING)
        {
            Log.i(TAG, "C-TOXCORE:" + ToxVars.TOX_LOG_LEVEL.value_str(a_TOX_LOG_LEVEL) + ":file=" + file + ":linenum=" + line + ":func=" + function + ":msg=" + message);
        }
    }

    static void logger_XX(int level, String text)
    {
        Log.i(TAG, text);
    }
    // -------- called by native methods --------
    // -------- called by native methods --------
    // -------- called by native methods --------

    /*
     * this is used to load the native library on
	 * application startup. The library has already been unpacked at
	 * installation time by the package manager.
	 */
    static
    {
        try
        {
            System.loadLibrary("jni-c-toxcore");
            native_lib_loaded = true;
            Log.i(TAG, "successfully loaded native library");
        }
        catch (java.lang.UnsatisfiedLinkError e)
        {
            native_lib_loaded = false;
            Log.i(TAG, "loadLibrary jni-c-toxcore failed!");
            e.printStackTrace();
        }
    }

    public static void add_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            if (message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (message_id != -1)
                        {
                            try
                            {
                                Message m = orma.selectFromMessage().idEq(message_id).orderByIdDesc().get(0);
                                if (m.id != -1)
                                {
                                    if ((force) || (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS < System.currentTimeMillis()))
                                    {
                                        update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.message_list_fragment.add_message(m);
                                    }
                                }
                            }
                            catch (Exception e2)
                            {
                            }
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }

    }

    public static void update_single_message_from_messge_id(final long message_id, final boolean force)
    {
        try
        {
            if (message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        if (message_id != -1)
                        {
                            try
                            {
                                Message m = orma.selectFromMessage().idEq(message_id).orderByIdDesc().get(0);
                                if (m.id != -1)
                                {
                                    if ((force) || (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS < System.currentTimeMillis()))
                                    {
                                        update_all_messages_global_timestamp = System.currentTimeMillis();
                                        MainActivity.message_list_fragment.modify_message(m);
                                    }
                                }
                            }
                            catch (Exception e2)
                            {
                            }
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
        }
    }

    public static void update_single_message_from_ftid(final long filetransfer_id, final boolean force)
    {
        try
        {
            if (message_list_fragment != null)
            {
                Thread t = new Thread()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            Message m = orma.selectFromMessage().filetransfer_idEq(filetransfer_id).orderByIdDesc().get(0);
                            if (m.id != -1)
                            {
                                if ((force) || (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS < System.currentTimeMillis()))
                                {
                                    update_all_messages_global_timestamp = System.currentTimeMillis();
                                    MainActivity.message_list_fragment.modify_message(m);
                                }
                            }
                        }
                        catch (Exception e2)
                        {
                            e2.printStackTrace();
                        }
                    }
                };
                t.start();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    public static void update_single_message(Message m, boolean force)
    {
        try
        {
            if (message_list_fragment != null)
            {
                if ((force) || (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS < System.currentTimeMillis()))
                {
                    update_all_messages_global_timestamp = System.currentTimeMillis();
                    message_list_fragment.modify_message(m);
                }
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    //    public static void update_all_messages_global(boolean force)
    //    {
    //        if ((force) || (update_all_messages_global_timestamp + UPDATE_MESSAGES_NORMAL_MILLIS < System.currentTimeMillis()))
    //        {
    //            update_all_messages_global_timestamp = System.currentTimeMillis();
    //            update_message_view();
    //        }
    //    }

    public static long tox_friend_by_public_key__wrapper(@NonNull String friend_public_key_string)
    {
        if (cache_pubkey_fnum.containsKey(friend_public_key_string))
        {
            // Log.i(TAG, "cache hit:1");
            return cache_pubkey_fnum.get(friend_public_key_string);
        }
        else
        {
            if (cache_pubkey_fnum.size() >= 20)
            {
                // TODO: bad!
                cache_pubkey_fnum.clear();
            }
            long result = tox_friend_by_public_key(friend_public_key_string);
            cache_pubkey_fnum.put(friend_public_key_string, result);
            return result;
        }
    }

    public static long get_message_id_from_filetransfer_id_and_friendnum(long filetransfer_id, long friend_number)
    {
        try
        {
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:=====================================");
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:" + orma.selectFromMessage().toList().size());
            //
            //            int i = 0;
            //            for (i = 0; i < orma.selectFromMessage().toList().size(); i++)
            //            {
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:****");
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:#" + i + ":" + orma.selectFromMessage().toList().get(i));
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:****");
            //            }
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:=====================================");
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2=====================================");
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2" + orma.selectFromMessage().filetransfer_idEq(filetransfer_id).toList().size());
            //
            //            for (i = 0; i < orma.selectFromMessage().filetransfer_idEq(filetransfer_id).toList().size(); i++)
            //            {
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2****");
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2#" + i + ":" + orma.selectFromMessage().toList().get(i));
            //                Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2****");
            //            }
            //
            //            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:2=====================================");
            //

            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:messages:filetransfer_id=" + filetransfer_id + " friend_number=" + friend_number);

            return orma.selectFromMessage().
                    filetransfer_idEq(filetransfer_id).and().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().get(0).id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_message_id_from_filetransfer_id_and_friendnum:EE:" + e.getMessage());
            return -1;
        }
    }

    public static long get_filetransfer_id_from_friendnum_and_filenum(long friend_number, long file_number)
    {
        try
        {
            Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:friend_number=" + friend_number + " file_number=" + file_number);
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().
                    file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id;
            Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:ft_id=" + ft_id);
            return ft_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_filetransfer_id_from_friendnum_and_filenum:EE:" + e.getMessage());
            return -1;
        }
    }

    public static void delete_filetransfer_tmpfile(long friend_number, long file_number)
    {
        try
        {
            delete_filetransfer_tmpfile(orma.selectFromFiletransfer().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).and().file_numberEq(file_number).get(0).id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfer_tmpfile(long filetransfer_id)
    {
        try
        {
            Filetransfer ft = orma.selectFromFiletransfer().idEq(filetransfer_id).get(0);
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + ft.tox_public_key_string + "/" + ft.file_name);
                f1.delete();
            }
            else
            {
                java.io.File f1 = new java.io.File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + ft.tox_public_key_string + "/" + ft.file_name);
                f1.delete();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_state_from_friendnum_and_filenum(long friend_number, long file_number, int state)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).orderByIdDesc().get(0).id;

            Log.i(TAG, "set_message_state_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number + " file_number=" + file_number);

            set_message_state_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    get(0).id, state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_state_from_id(long message_id, int state)
    {
        try
        {
            orma.updateMessage().idEq(message_id).state(state).execute();
            Log.i(TAG, "set_message_state_from_id:message_id=" + message_id + " state=" + state);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_state_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_start_sending_from_id(long message_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_outgoing_started(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_start_sending_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_friendnum_and_filenum(long friend_number, long file_number, long filedb_id)
    {
        try
        {
            long ft_id = orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id;

            Log.i(TAG, "set_message_filedb_from_friendnum_and_filenum:ft_id=" + ft_id + " friend_number=" + friend_number + " file_number=" + file_number);


            set_message_filedb_from_id(orma.selectFromMessage().
                    filetransfer_idEq(ft_id).and().
                    tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().
                    get(0).id, filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_friendnum_and_filenum:EE:" + e.getMessage());
        }
    }

    public static void set_message_filedb_from_id(long message_id, long filedb_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).filedb_id(filedb_id).execute();
            Log.i(TAG, "set_message_filedb_from_id:message_id=" + message_id + " filedb_id=" + filedb_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_message_filedb_from_id:EE:" + e.getMessage());
        }
    }

    public static void set_filetransfer_state_from_id(long filetransfer_id, int state)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).state(state).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_start_sending_from_id(long filetransfer_id)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).ft_outgoing_started(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_message_accepted_from_id(long message_id)
    {
        try
        {
            orma.updateMessage().idEq(message_id).ft_accepted(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_accepted_from_id(long filetransfer_id)
    {
        try
        {
            orma.updateFiletransfer().idEq(filetransfer_id).ft_accepted(true).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static long get_filetransfer_filenum_from_id(long filetransfer_id)
    {
        try
        {
            if (orma.selectFromFiletransfer().idEq(filetransfer_id).count() == 1)
            {
                return orma.selectFromFiletransfer().idEq(filetransfer_id).get(0).file_number;
            }
            else
            {
                return -1;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
    }

    public static void set_filetransfer_for_message_from_friendnum_and_filenum(long friend_number, long file_number, long ft_id)
    {
        try
        {
            set_filetransfer_for_message_from_filetransfer_id(orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().
                    file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id, ft_id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void set_filetransfer_for_message_from_filetransfer_id(long filetransfer_id, long ft_id)
    {
        try
        {
            orma.updateMessage().filetransfer_idEq(filetransfer_id).filetransfer_id(ft_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfers_from_friendnum_and_filenum(long friend_number, long file_number)
    {
        try
        {
            delete_filetransfers_from_id(orma.selectFromFiletransfer().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    and().
                    file_numberEq(file_number).
                    orderByIdDesc().
                    get(0).id);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static void delete_filetransfers_from_id(long filetransfer_id)
    {
        try
        {
            orma.deleteFromFiletransfer().idEq(filetransfer_id).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String tox_friend_get_public_key__wrapper(long friend_number)
    {
        if (cache_fnum_pubkey.containsKey(friend_number))
        {
            // Log.i(TAG, "cache hit:2");
            return cache_fnum_pubkey.get(friend_number);
        }
        else
        {
            if (cache_fnum_pubkey.size() >= 20)
            {
                // TODO: bad!
                cache_fnum_pubkey.clear();
            }
            String result = tox_friend_get_public_key(friend_number);
            cache_fnum_pubkey.put(friend_number, result);
            return result;
        }
    }

    public void show_add_friend(View view)
    {
        Intent intent = new Intent(this, AddFriendActivity.class);
        // intent.putExtra("key", value);
        startActivityForResult(intent, AddFriendActivity_ID);
    }

    static void cancel_filetransfer(long friend_number, long file_number)
    {
        Log.i(TAG, "FTFTFT:cancel_filetransfer");

        Filetransfer f = null;
        try
        {
            f = orma.selectFromFiletransfer().
                    file_numberEq(file_number).
                    and().
                    tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friend_number)).
                    orderByIdDesc().
                    toList().get(0);

            if (f.direction == TRIFA_FT_DIRECTION_INCOMING.value)
            {
                if (f.kind == TOX_FILE_KIND_DATA.value)
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);

                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // set state for FT in message
                    set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // remove link to any message
                    set_filetransfer_for_message_from_friendnum_and_filenum(friend_number, file_number, -1);
                    // delete FT in DB
                    Log.i(TAG, "FTFTFT:002");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                    // update UI
                    // TODO: updates all messages, this is bad
                    // update_all_messages_global(false);
                    try
                    {
                        if (f.id != -1)
                        {
                            update_single_message_from_messge_id(msg_id, true);
                        }
                    }
                    catch (Exception e)
                    {
                    }
                }
                else // avatar FT
                {
                    long ft_id = get_filetransfer_id_from_friendnum_and_filenum(friend_number, file_number);
                    long msg_id = get_message_id_from_filetransfer_id_and_friendnum(ft_id, friend_number);
                    set_filetransfer_state_from_id(ft_id, TOX_FILE_CONTROL_CANCEL.value);
                    set_message_state_from_id(msg_id, TOX_FILE_CONTROL_CANCEL.value);
                    // delete tmp file
                    delete_filetransfer_tmpfile(friend_number, file_number);
                    // delete FT in DB
                    Log.i(TAG, "FTFTFT:003");
                    delete_filetransfers_from_friendnum_and_filenum(friend_number, file_number);
                }
            }
            else // outgoing FT
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void update_filetransfer_db_fos_open(final Filetransfer f)
    {
        orma.updateFiletransfer().
                tox_public_key_stringEq(f.tox_public_key_string).
                and().
                file_numberEq(f.file_number).
                fos_open(f.fos_open).
                execute();
    }

    static void update_filetransfer_db_current_position(final Filetransfer f)
    {
        orma.updateFiletransfer().
                tox_public_key_stringEq(f.tox_public_key_string).
                and().
                file_numberEq(f.file_number).
                current_position(f.current_position).
                execute();
    }

    static void update_filetransfer_db_full(final Filetransfer f)
    {
        orma.updateFiletransfer().
                tox_public_key_stringEq(f.tox_public_key_string).
                and().
                file_numberEq(f.file_number).
                direction(f.direction).
                file_number(f.file_number).
                kind(f.kind).
                state(f.state).
                path_name(f.path_name).
                message_id(f.message_id).
                file_name(f.file_name).
                fos_open(f.fos_open).
                filesize(f.filesize).
                current_position(f.current_position).
                execute();
    }

    static long insert_into_filetransfer_db(final Filetransfer f)
    {
        //Thread t = new Thread()
        //{
        //    @Override
        //    public void run()
        //    {
        try
        {
            long row_id = orma.insertIntoFiletransfer(f);
            Log.i(TAG, "insert_into_filetransfer_db:row_id=" + row_id);

            Cursor cursor = orma.getConnection().rawQuery("SELECT id FROM Filetransfer where rowid='" + row_id + "'");
            cursor.moveToFirst();
            Log.i(TAG, "insert_into_filetransfer_db:id res count=" + cursor.getColumnCount());
            long ft_id = cursor.getLong(0);
            cursor.close();

            Log.i(TAG, "insert_into_filetransfer_db:ft_id=" + ft_id);
            return ft_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "insert_into_filetransfer_db:EE:" + e.getMessage());
            return -1;
        }
        //    }
        //};
        //t.start();
    }

    static void set_friend_avatar(String friend_pubkey, String avatar_path_name, String avatar_file_name)
    {
        try
        {
            Log.i(TAG, "set_friend_avatar:update:pubkey=" + friend_pubkey + " path=" + avatar_path_name + " file=" + avatar_file_name);

            orma.updateFriendList().tox_public_key_stringEq(friend_pubkey).
                    avatar_pathname(avatar_path_name).
                    avatar_filename(avatar_file_name).
                    execute();

            update_display_friend_avatar(friend_pubkey, avatar_path_name, avatar_file_name);
        }
        catch (Exception e)
        {
            Log.i(TAG, "set_friend_avatar:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static void update_display_friend_avatar(String friend_pubkey, String avatar_path_name, String avatar_file_name)
    {
        // TODO: write me ----
        // try to load avatar image, and set in friendlist fragment
    }

    static void move_tmp_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        Log.i(TAG, "move_tmp_file_to_real_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" + dst_file_name);
        try
        {
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(src_path_name + "/" + src_file_name);
                info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(dst_path_name + "/" + dst_file_name);
                info.guardianproject.iocipher.File dst_dir = new info.guardianproject.iocipher.File(dst_path_name + "/");
                dst_dir.mkdirs();
                f1.renameTo(f2);
            }
            else
            {
                java.io.File f1 = new java.io.File(src_path_name + "/" + src_file_name);
                java.io.File f2 = new java.io.File(dst_path_name + "/" + dst_file_name);
                java.io.File dst_dir = new java.io.File(dst_path_name + "/");
                dst_dir.mkdirs();
                f1.renameTo(f2);
            }
            Log.i(TAG, "move_tmp_file_to_real_file:OK");
        }
        catch (Exception e)
        {
            Log.i(TAG, "move_tmp_file_to_real_file:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static String get_uniq_tmp_filename(String filename_with_path, long filesize)
    {
        String ret = null;

        try
        {
            java.security.MessageDigest md5_ = java.security.MessageDigest.getInstance("MD5");
            byte[] md5_digest = md5_.digest((filesize + ":" + filename_with_path).getBytes());

            BigInteger bigInt = new BigInteger(1, md5_digest);
            String hashtext = bigInt.toString(16);

            // Now we need to zero pad it if you actually want the full 32 chars.
            while (hashtext.length() < 32)
            {
                hashtext = "0" + hashtext;
            }

            ret = hashtext;
            // Log.i(TAG, "get_uniq_tmp_filename:ret=" + ret);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_uniq_tmp_filename:EE:" + e.getMessage());
            ret = "temp__" + System.currentTimeMillis() + (int) (Math.random() * 10000d);
        }

        return ret;
    }

    static void copy_real_file_to_vfs_file(String src_path_name, String src_file_name, String dst_path_name, String dst_file_name)
    {
        Log.i(TAG, "copy_real_file_to_vfs_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" + dst_file_name);
        try
        {
            if (VFS_ENCRYPT)
            {
                java.io.File f_real = new java.io.File(src_path_name + "/" + src_file_name);

                String uniq_temp_filename = get_uniq_tmp_filename(f_real.getAbsolutePath(), f_real.length());
                Log.i(TAG, "copy_real_file_to_vfs_file:uniq_temp_filename=" + uniq_temp_filename);

                info.guardianproject.iocipher.File f2 = new info.guardianproject.iocipher.File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + uniq_temp_filename);
                info.guardianproject.iocipher.File dst_dir = new info.guardianproject.iocipher.File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
                dst_dir.mkdirs();

                java.io.FileInputStream is = null;
                info.guardianproject.iocipher.FileOutputStream os = null;
                try
                {
                    is = new java.io.FileInputStream(f_real);
                    os = new info.guardianproject.iocipher.FileOutputStream(f2);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0)
                    {
                        os.write(buffer, 0, length);
                    }
                }
                finally
                {
                    is.close();
                    os.close();
                }

                move_tmp_file_to_real_file(VFS_PREFIX + VFS_TMP_FILE_DIR, uniq_temp_filename, dst_path_name, dst_file_name);
            }
            else
            {
                java.io.File f_real = new java.io.File(src_path_name + "/" + src_file_name);

                String uniq_temp_filename = get_uniq_tmp_filename(f_real.getAbsolutePath(), f_real.length());
                Log.i(TAG, "copy_real_file_to_vfs_file:uniq_temp_filename=" + uniq_temp_filename);

                java.io.File f2 = new java.io.File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/" + uniq_temp_filename);
                java.io.File dst_dir = new java.io.File(VFS_PREFIX + VFS_TMP_FILE_DIR + "/");
                dst_dir.mkdirs();

                java.io.FileInputStream is = null;
                java.io.FileOutputStream os = null;
                try
                {
                    is = new java.io.FileInputStream(f_real);
                    os = new java.io.FileOutputStream(f2);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0)
                    {
                        os.write(buffer, 0, length);
                    }
                }
                finally
                {
                    is.close();
                    os.close();
                }

                move_tmp_file_to_real_file(VFS_PREFIX + VFS_TMP_FILE_DIR, uniq_temp_filename, dst_path_name, dst_file_name);
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "copy_real_file_to_vfs_file:EE:" + e.getMessage());
            e.printStackTrace();
        }
    }

    static String make_some_static_dummy_file(Context context)
    {
        String ret = null;

        try
        {
            java.io.File dst_dir = new java.io.File(SD_CARD_STATIC_DIR + "/");
            dst_dir.mkdirs();

            java.io.File fout = new java.io.File(SD_CARD_STATIC_DIR + "/" + "__dummy__dummy_.jpg");
            java.io.FileOutputStream os = new java.io.FileOutputStream(fout);
            //            int len = 2 + 2 + 2 + 5 + 2 + 1 + 2 + 2;
            //            byte[] buffer = new byte[len];
            //
            //            int a = 0;
            //            buffer[a] = (byte) 0xff;
            //            a++;
            //            buffer[a] = (byte) 0xd8;
            //            a++;
            //
            //            buffer[a] = (byte) 0xff;
            //            a++;
            //            buffer[a] = (byte) 0xe0;
            //            a++;
            //
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //            buffer[a] = (byte) 0x10;
            //            a++;
            //
            //            buffer[a] = (byte) 0x4a;
            //            a++;
            //            buffer[a] = (byte) 0x46;
            //            a++;
            //            buffer[a] = (byte) 0x49;
            //            a++;
            //            a++;
            //            buffer[a] = (byte) 0x46;
            //            a++;
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //
            //            buffer[a] = (byte) 0x01;
            //            a++;
            //            buffer[a] = (byte) 0x02;
            //            a++;
            //
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //            buffer[a] = (byte) 0x0a;
            //            a++;
            //
            //            buffer[a] = (byte) 0x0;
            //            a++;
            //            buffer[a] = (byte) 0x0a;
            //            a++;
            //
            //            os.write(buffer, 0, len);
            //            os.close();

            java.io.InputStream ins = context.getResources().
                    openRawResource(context.getResources().
                            getIdentifier("ic_plus_sign", "drawable", context.getPackageName()));

            byte[] buffer = new byte[1024];
            int length;
            while ((length = ins.read(buffer)) > 0)
            {
                os.write(buffer, 0, length);
            }

            ins.close();
            os.close();

            ret = fout.getAbsolutePath();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    static String copy_vfs_file_to_real_file(String src_path_name, String src_file_name, String dst_path_name, String appl)
    {
        String uniq_temp_filename = null;

        try
        {
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f_real = new info.guardianproject.iocipher.File(src_path_name + "/" + src_file_name);

                uniq_temp_filename = get_uniq_tmp_filename(f_real.getAbsolutePath(), f_real.length()) + appl;
                // Log.i(TAG, "copy_vfs_file_to_real_file:" + src_path_name + "/" + src_file_name + " -> " + dst_path_name + "/" + uniq_temp_filename);

                java.io.File f2 = new java.io.File(dst_path_name + "/" + uniq_temp_filename);
                java.io.File dst_dir = new java.io.File(dst_path_name + "/");
                dst_dir.mkdirs();

                info.guardianproject.iocipher.FileInputStream is = null;
                java.io.FileOutputStream os = null;
                try
                {
                    is = new info.guardianproject.iocipher.FileInputStream(f_real);
                    os = new java.io.FileOutputStream(f2);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0)
                    {
                        os.write(buffer, 0, length);
                    }
                }
                finally
                {
                    is.close();
                    os.close();
                }
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "copy_vfs_file_to_real_file:EE:" + e.getMessage());
            e.printStackTrace();
        }

        return uniq_temp_filename;
    }

    static long insert_into_message_db(final Message m, final boolean update_message_view_flag)
    {
        // Thread t = new Thread()
        //{
        //    @Override
        //    public void run()
        //    {
        // Log.i(TAG, "insert_into_message_db:m=" + m);
        long row_id = orma.insertIntoMessage(m);

        try
        {
            Cursor cursor = orma.getConnection().rawQuery("SELECT id FROM Message where rowid='" + row_id + "'");
            cursor.moveToFirst();
            Log.i(TAG, "insert_into_message_db:id res count=" + cursor.getColumnCount());
            long msg_id = cursor.getLong(0);
            cursor.close();

            if (update_message_view_flag)
            {
                add_single_message_from_messge_id(msg_id, true);
            }

            return msg_id;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return -1;
        }
        //    }
        //};
        //t.start();
    }

    static String get_vfs_image_filename_own_avatar()
    {
        return get_g_opts("VFS_OWN_AVATAR_FNAME");
    }

    static String get_vfs_image_filename_friend_avatar(String friend_pubkey)
    {
        try
        {
            FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(friend_pubkey).toList().get(0);
            return f.avatar_pathname + "/" + f.avatar_filename;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static String get_vfs_image_filename_friend_avatar(long friendnum)
    {
        try
        {
            FriendList f = orma.selectFromFriendList().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).toList().get(0);
            return f.avatar_pathname + "/" + f.avatar_filename;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static Drawable get_drawable_from_vfs_image(String vfs_image_filename)
    {
        try
        {
            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(vfs_image_filename);
                info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                byte[] byteArray = new byte[(int) f1.length()];
                fis.read(byteArray, 0, (int) f1.length());

                return new BitmapDrawable(BitmapFactory.decodeByteArray(byteArray, 0, (int) f1.length()));
            }
            else
            {
                java.io.File f1 = new java.io.File(vfs_image_filename);
                java.io.FileInputStream fis = new java.io.FileInputStream(f1);

                byte[] byteArray = new byte[(int) f1.length()];
                fis.read(byteArray, 0, (int) f1.length());

                return new BitmapDrawable(BitmapFactory.decodeByteArray(byteArray, 0, (int) f1.length()));
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }

    static void put_vfs_image_on_imageview(Context c, ImageView v, Drawable placholder, String vfs_image_filename)
    {
        try
        {
            // Log.i(TAG, "put_vfs_image_on_imageview:" + vfs_image_filename);

            if (VFS_ENCRYPT)
            {
                info.guardianproject.iocipher.File f1 = new info.guardianproject.iocipher.File(vfs_image_filename);
                // info.guardianproject.iocipher.FileInputStream fis = new info.guardianproject.iocipher.FileInputStream(f1);

                //byte[] byteArray = new byte[(int) f1.length()];
                // fis.read(byteArray, 0, (int) f1.length());

                GlideApp.
                        with(c).
                        load(f1).
                        placeholder(placholder).
                        diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                        skipMemoryCache(false).
                        into(v);
            }
            else
            {
                java.io.File f1 = new java.io.File(vfs_image_filename);
                java.io.FileInputStream fis = new java.io.FileInputStream(f1);

                byte[] byteArray = new byte[(int) f1.length()];
                fis.read(byteArray, 0, (int) f1.length());

                GlideApp.
                        with(c).
                        load(byteArray).
                        placeholder(placholder).
                        diskCacheStrategy(DiskCacheStrategy.RESOURCE).
                        skipMemoryCache(false).
                        into(v);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static String get_g_opts(String key)
    {
        try
        {
            if (orma.selectFromTRIFADatabaseGlobals().keyEq(key).count() == 1)
            {
                TRIFADatabaseGlobals g_opts = orma.selectFromTRIFADatabaseGlobals().keyEq(key).get(0);
                return g_opts.value;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "get_g_opts:EE1:" + e.getMessage());
            return null;
        }
    }

    static void set_g_opts(String key, String value)
    {
        try
        {
            TRIFADatabaseGlobals g_opts = new TRIFADatabaseGlobals();
            g_opts.key = key;
            g_opts.value = value;

            try
            {
                orma.insertIntoTRIFADatabaseGlobals(g_opts);
                Log.i(TAG, "set_g_opts:(INSERT)");
            }
            catch (android.database.sqlite.SQLiteConstraintException e)
            {
                e.printStackTrace();
                try
                {
                    orma.updateTRIFADatabaseGlobals().keyEq(key).value(value).execute();
                    Log.i(TAG, "set_g_opts:(UPDATE)");
                }
                catch (Exception e2)
                {
                    e2.printStackTrace();
                    Log.i(TAG, "set_g_opts:EE1:" + e2.getMessage());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "set_g_opts:EE2:" + e.getMessage());
        }
    }

    synchronized static void insert_into_friendlist_db(final FriendList f)
    {
        //        Thread t = new Thread()
        //        {
        //            @Override
        //            public void run()
        //            {
        try
        {
            if (orma.selectFromFriendList().tox_public_key_stringEq(f.tox_public_key_string).count() == 0)
            {
                orma.insertIntoFriendList(f);
                Log.i(TAG, "friend added to DB: " + f.tox_public_key_string);
            }
            else
            {
                // friend already in DB
                Log.i(TAG, "friend already in DB: " + f.tox_public_key_string);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "friend added to DB:EE:" + e.getMessage());
        }
        //            }
        //        };
        //        t.start();
    }

    static void delete_friend_all_files(final long friendnum)
    {
        try
        {
            orma.deleteFromFileDB().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_friend_all_filetransfers(final long friendnum)
    {
        try
        {
            orma.deleteFromFiletransfer().tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).execute();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    static void delete_friend_all_messages(final long friendnum)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                orma.deleteFromMessage().tox_friendpubkeyEq(tox_friend_get_public_key__wrapper(friendnum)).execute();
            }
        };
        t.start();
    }

    static void delete_friend(final String friend_pubkey)
    {
        Thread t = new Thread()
        {
            @Override
            public void run()
            {
                orma.deleteFromFriendList().
                        tox_public_key_stringEq(friend_pubkey).
                        execute();
            }
        };
        t.start();
    }

    //    static void update_message_view_on_UI()
    //    {
    //        Runnable myRunnable = new Runnable()
    //        {
    //            @Override
    //            public void run()
    //            {
    //                try
    //                {
    //                    update_message_view();
    //                }
    //                catch (Exception e)
    //                {
    //                    e.printStackTrace();
    //                }
    //            }
    //        };
    //        main_handler_s.post(myRunnable);
    //    }

    static void update_message_view()
    {
        try
        {
            // Log.i(TAG, "update_message_view:001 " + message_list_fragment);
            // Log.i(TAG, "update_message_view:002 " + message_list_fragment.isAdded() + " " + message_list_fragment.isVisible());
            // update the message view (if possbile)
            if (message_list_fragment != null)
            {
                Log.i(TAG, "update_message_view:005");
                MainActivity.message_list_fragment.update_all_messages();
                Log.i(TAG, "update_message_view:006");
            }
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            Log.i(TAG, "update_message_view:EE:" + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AddFriendActivity_ID)
        {
            if (resultCode == RESULT_OK)
            {
                String friend_tox_id1 = data.getStringExtra("toxid");
                String friend_tox_id = "";
                friend_tox_id = friend_tox_id1.toUpperCase().replace(" ", "").replaceFirst("tox:", "").replaceFirst("TOX:", "").replaceFirst("Tox:", "");

                add_friend_real(friend_tox_id);
            }
            else
            {
                // (resultCode == RESULT_CANCELED)
            }
        }
    }

    static void add_friend_real(String friend_tox_id)
    {
        Log.i(TAG, "add_friend_real:add friend ID:" + friend_tox_id);

        // add friend ---------------
        long friendnum = tox_friend_add(friend_tox_id, "please add me"); // add friend
        Log.i(TAG, "add_friend_real:add friend  #:" + friendnum);
        update_savedata_file(); // save toxcore datafile (new friend added)

        if (friendnum > -1)
        {
            // nospam=8 chars, checksum=4 chars
            String friend_public_key = friend_tox_id.substring(0, friend_tox_id.length() - 12);
            Log.i(TAG, "add_friend_real:add friend PK:" + friend_public_key);

            FriendList f = new FriendList();
            f.tox_public_key_string = friend_public_key;
            try
            {
                // set name as the last 5 char of TOXID (until we get a name sent from friend)
                f.name = friend_public_key.substring(friend_public_key.length() - 5, friend_public_key.length());
            }
            catch (Exception e)
            {
                e.printStackTrace();
                f.name = "Unknown";
            }
            f.TOX_USER_STATUS = 0;
            f.TOX_CONNECTION = 0;
            f.avatar_filename = null;
            f.avatar_pathname = null;

            try
            {
                insert_into_friendlist_db(f);
            }
            catch (Exception e)
            {
                // e.printStackTrace();
            }

            try
            {
                if (friend_list_fragment != null)
                {
                    Log.i(TAG, "add_friend_real:add:001:friendnum=" + friendnum);
                    friend_list_fragment.modify_friend(f, friendnum);
                    Log.i(TAG, "add_friend_real:add:002:friendnum=" + friendnum);
                }
                else
                {
                    Log.i(TAG, "add_friend_real:add:003:no friend_list_fragment yet!!");
                }
            }
            catch (Exception e)
            {
                Log.i(TAG, "add_friend_real:EE1:" + e.getMessage());
                e.printStackTrace();
            }
        }

        if (friendnum == -1)
        {
            Log.i(TAG, "add_friend_real:friend already added, or request already sent");
        }
        // add friend ---------------
    }

    static String get_friend_name_from_num(long friendnum)
    {
        String result = "Unknown";
        try
        {
            if (orma != null)
            {
                result = orma.selectFromFriendList().
                        tox_public_key_stringEq(tox_friend_get_public_key__wrapper(friendnum)).
                        toList().get(0).name;
            }
        }
        catch (Exception e)
        {
            result = "Unknown";
            e.printStackTrace();
        }

        return result;
    }

    synchronized static boolean audio_buffer_read_write(long sample_count, int channels, long sampling_rate, boolean write)
    {
        if (write)
        {
            // Log.i(TAG, "audio_buffer_read_write:write:START");
            int j = 0;
            if (audio_in_buffer_element_count < audio_in_buffer_max_count)
            {
                if (audio_in_buffer_element_count > 0)
                {
                    for (j = 0; j < audio_in_buffer_element_count; j++)
                    {
                        audio_buffer_2[audio_in_buffer_element_count - 1 - j].rewind();
                        audio_buffer_2[audio_in_buffer_element_count - j].rewind();
                        // Log.i(TAG, "audio_play:write:buffer size src=" + audio_buffer_2[audio_in_buffer_element_count - 1 - j].limit());
                        // Log.i(TAG, "audio_play:write:buffer pos src=" + audio_buffer_2[audio_in_buffer_element_count - 1 - j].position());
                        // Log.i(TAG, "audio_play:write:buffer size dst=" + audio_buffer_2[audio_in_buffer_element_count - j].limit());
                        // Log.i(TAG, "audio_play:write:buffer pos dst=" + audio_buffer_2[audio_in_buffer_element_count - j].position());
                        // audio_buffer_2[audio_in_buffer_element_count - j].put(audio_buffer_2[audio_in_buffer_element_count - 1 - j].array());
                        audio_buffer_2[audio_in_buffer_element_count - j].put(audio_buffer_2[audio_in_buffer_element_count - 1 - j].array(), 0, AudioReceiver.buffer_size);
                        audio_buffer_2[audio_in_buffer_element_count - j].rewind();
                        audio_buffer_2_read_length[audio_in_buffer_element_count - j] = audio_buffer_2_read_length[audio_in_buffer_element_count - 1 - j];
                        // Log.i(TAG, "audio_play:write:mv " + (audio_in_buffer_element_count - 1 - j + " -> " + (audio_in_buffer_element_count - j)));
                    }
                }
                // Log.i(TAG, "audio_play:write:set buffer 0:len=" + sample_count);
                audio_buffer_2_read_length[0] = (int) (sample_count * channels * 2);
                audio_in_buffer_element_count++;
                // Log.i(TAG, "audio_play:write:element count new=" + audio_in_buffer_element_count);
                // Log.i(TAG, "audio_play:write:element count new=" + audio_in_buffer_element_count);

                // wake up audio thread -----------
                try
                {
                    audio_thread.interrupt();
                }
                catch (Exception e)
                {
                    Log.i(TAG, "audio_buffer_read_write:write:wake up audio thread:EE:" + e.getMessage());
                }
                // wake up audio thread -----------
            }
            else
            {
                Log.i(TAG, "audio_buffer_read_write:write:* buffer FULL *");
            }

            // Log.i(TAG, "audio_buffer_read_write:write:END");

            return true;
        }
        else // read
        {
            // Log.i(TAG, "audio_buffer_read_write:READ:START");

            if (audio_in_buffer_element_count > 0)
            {
                // Log.i(TAG, "audio_play:read:load buffer " + (audio_in_buffer_element_count - 1) + ":len=" + audio_buffer_2_read_length[audio_in_buffer_element_count - 1]);

                audio_buffer_play.rewind();
                audio_buffer_play.put(audio_buffer_2[audio_in_buffer_element_count - 1].array(), 0, AudioReceiver.buffer_size);
                audio_buffer_play_length = audio_buffer_2_read_length[audio_in_buffer_element_count - 1];
                audio_in_buffer_element_count--;
                // Log.i(TAG, "audio_play:read:element count new=" + audio_in_buffer_element_count);

                // Log.i(TAG, "audio_buffer_read_write:READ:END01");

                return true;
            }

            // Log.i(TAG, "audio_buffer_read_write:READ:END02");

            return false;
        }
    }

    static int add_tcp_relay_single_wrapper(String ip, long port, String key_hex)
    {
        return add_tcp_relay_single(ip, key_hex, port);
    }

    static int bootstrap_single_wrapper(String ip, long port, String key_hex)
    {
        return bootstrap_single(ip, key_hex, port);
    }

    void sendEmailWithAttachment(Context c, final String recipient, final String subject, final String message, final String full_file_name, final String full_file_name_suppl)
    {
        try
        {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", recipient, null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            ArrayList<Uri> uris = new ArrayList<>();
            uris.add(Uri.parse("file://" + full_file_name));
            try
            {
                if (new File(full_file_name_suppl).length() > 0)
                {
                    uris.add(Uri.parse("file://" + full_file_name_suppl));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            List<ResolveInfo> resolveInfos = getPackageManager().queryIntentActivities(emailIntent, 0);
            List<LabeledIntent> intents = new ArrayList<>();

            if (resolveInfos.size() != 0)
            {
                for (ResolveInfo info : resolveInfos)
                {
                    Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    System.out.println("email:" + "comp=" + info.activityInfo.packageName + " " + info.activityInfo.name);
                    intent.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
                    if (subject != null)
                    {
                        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
                    }
                    if (message != null)
                    {
                        intent.putExtra(Intent.EXTRA_TEXT, message);
                    }
                    intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                    intents.add(new LabeledIntent(intent, info.activityInfo.packageName, info.loadLabel(getPackageManager()), info.icon));
                }
                Intent chooser = Intent.createChooser(intents.remove(intents.size() - 1), "Send email with attachments");
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toArray(new LabeledIntent[intents.size()]));
                startActivity(chooser);
            }
            else
            {
                System.out.println("email:" + "No Email App found");
                new AlertDialog.Builder(c).setMessage("No Email App found").setPositiveButton("Ok", null).show();
            }
        }
        catch (ActivityNotFoundException e)
        {
            // cannot send email for some reason
        }
    }

    static String safe_string_XX(byte[] in)
    {
        Log.i(TAG, "safe_string:in=" + in);
        String out = "";

        try
        {
            out = new String(in, "UTF-8");  // Best way to decode using "UTF-8"
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.i(TAG, "safe_string:EE:" + e.getMessage());
            try
            {
                out = new String(in);
            }
            catch (Exception e2)
            {
                e2.printStackTrace();
                Log.i(TAG, "safe_string:EE2:" + e2.getMessage());
            }
        }

        Log.i(TAG, "safe_string:out=" + out);
        return out;
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float dp2px(float dp)
    {
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @return A float value to represent dp equivalent to px value
     */
    public static float px2dp(float px)
    {
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    void getVersionInfo()
    {
        try
        {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public static void update_bitrates()
    {
        // these were updated: Callstate.audio_bitrate, Callstate.video_bitrate
        try
        {
            if (CallingActivity.ca != null)
            {
                if (CallingActivity.ca.callactivity_handler != null)
                {
                    final Runnable myRunnable = new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                CallingActivity.ca.right_top_text_1.setText("V " + Callstate.video_bitrate + " kbit/s");
                                CallingActivity.ca.right_top_text_2.setText("A " + Callstate.audio_bitrate + " kbit/s");
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    };

                    if (CallingActivity.ca.callactivity_handler != null)
                    {
                        CallingActivity.ca.callactivity_handler.post(myRunnable);
                    }

                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static String format_timeduration_from_seconds(long seconds)
    {
        String positive = "";
        final long absSeconds = Math.abs(seconds);
        // Log.i(TAG,"format_timeduration_from_seconds:seconds="+seconds+" absSeconds="+absSeconds);
        int hours = (int) (absSeconds / 3600);
        if (hours < 1)
        {
            positive = String.format("%02d:%02d", (absSeconds % 3600) / 60, absSeconds % 60);
        }
        else
        {
            positive = String.format("%d:%02d:%02d", hours, (absSeconds % 3600) / 60, absSeconds % 60);
        }
        return seconds < 0 ? "-" + positive : positive;
    }

    public static ByteBuffer string_to_bytebuffer(String input_chars, int output_number_of_bytes)
    {
        try
        {
            ByteBuffer ret = ByteBuffer.allocateDirect(output_number_of_bytes);
            ret.rewind();
            ret.put(input_chars.getBytes());
            return ret;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static ByteBuffer file_to_bytebuffer(String filename_with_fullpath, boolean is_vfs)
    {
        if (is_vfs)
        {
            info.guardianproject.iocipher.File file = new info.guardianproject.iocipher.File(filename_with_fullpath);
            int size = (int) file.length();
            ByteBuffer ret = ByteBuffer.allocateDirect(size);
            byte[] bytes = new byte[size];
            try
            {
                BufferedInputStream buf = new BufferedInputStream(new info.guardianproject.iocipher.FileInputStream(file));
                buf.read(bytes, 0, bytes.length);
                buf.close();
                ret = ret.put(bytes);
                return ret;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        return null;
    }

    public static String bytes_to_hex(ByteBuffer in)
    {
        try
        {
            final StringBuilder builder = new StringBuilder();
            for (byte b : in.array())
            {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return "*ERROR*";
    }

    public static String bytes_to_hex(byte[] in)
    {
        try
        {
            final StringBuilder builder = new StringBuilder();
            for (byte b : in)
            {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "*ERROR*";

    }

    public static com.bumptech.glide.load.Key StringSignature2(final String in)
    {
        com.bumptech.glide.load.Key ret = new StringObjectKey(in);
        return ret;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth)
    {

        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        // create a matrix for the manipulation
        Matrix matrix = new Matrix();

        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);

        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);

        return resizedBitmap;
    }

    public static PackageInfo get_my_pkg_info()
    {
        return packageInfo_s;
    }

    static void get_network_connections()
    {
        Detector.updateReportMap();
        Collector.updateReports();
    }

    static String long_date_time_format(long timestamp_in_millis)
    {
        try
        {
            return df_date_time_long.format(new Date(timestamp_in_millis));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "*Datetime ERROR*";
        }
    }

    // --------- make app crash ---------
    // --------- make app crash ---------
    // --------- make app crash ---------
    public static void crash_app_java(int type)
    {
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======================+++++");
        System.out.println("+++++======= TYPE:J =======+++++");
        System.out.println("+++++======================+++++");
        if (type == 1)
        {
            Java_Crash_001();
        }
        else if (type == 2)
        {
            Java_Crash_002();
        }
        else
        {
            stackOverflow();
        }
    }

    public static void Java_Crash_001()
    {
        Integer i = null;
        i.byteValue();
    }

    public static void Java_Crash_002()
    {
        View v = null;
        v.bringToFront();
    }

    public static void stackOverflow()
    {
        stackOverflow();
    }

    public static void crash_app_C()
    {
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======= CRASH  =======+++++");
        System.out.println("+++++======================+++++");
        System.out.println("+++++======= TYPE:C =======+++++");
        System.out.println("+++++======================+++++");
        AppCrashC();
    }

    public static native void AppCrashC();
    // --------- make app crash ---------
    // --------- make app crash ---------
    // --------- make app crash ---------

}

