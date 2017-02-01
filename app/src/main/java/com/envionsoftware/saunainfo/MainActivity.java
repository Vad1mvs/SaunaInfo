package com.envionsoftware.saunainfo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.internal.view.menu.ExpandedMenuView;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TimeUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements
        FTPTask.FTPResponseCallback {
    public static final String TAG = "MainActivity";
    public static final String PFX_FTP = "ftp:";
    public static final String PFX_VIDEO = "video:";
    public static final String PFX_PHOTO = "photo:";
    public static final String PFX_SOUND_IN = "soundin:";
    public static final String PFX_SOUND_OUT = "soundout:";
    public static final String ASSET_SOUND_IN = "sound_in.mp3";
    public static final String ASSET_SOUND_OUT = "sound_out.mp3";
    private static boolean DEBUG_MODE = false;
    private static final int CMD_UNKNOWN = 0;
    private static final int CMD_IN = 1;
    private static final int CMD_OUT = 2;
    private static final int CMD_VIDEO = 3;
    private static final int CMD_PHOTO = 4;
    private static final int CMD_SOUND_IN = 5;
    private static final int CMD_SOUND_OUT = 6;
    private static final int CMD_FTP = 7;
    private static final int VIDEO_DELAY_INTERVAL = 5; // in seconds
    private Context mContext;
    private TelephonyManager tm;
    private String ipWiFi, ipEth, devModel, devVer;
    private SharedPreferences prefs;
    private boolean isEthOn, firstRun, showInfo = true;
    private int lastCommand = CMD_UNKNOWN;
    private TextView txtHeader, txtFooter, txtSolariumTime, txtSolarium;
    private ImageView solariumImageView;
    private LinearLayout linearLayoutInfo;
    private FrameLayout frameLayoutMedia;
    VideoView videoPlayerView;
    DisplayMetrics dm;
    MediaController mediaController;

    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    public static final int SERVERPORT = 6000;
    private String serverInfo, videoPath;
    ClockTimeUpdater clockTimeUpdater;
    private Handler mHandler = new Handler();
    private String ftpAddr, videoName, photoName, soundInName, soundOutName;
    private MediaPlayer mediaPlayer;
    private int currentTask = FTPTask.TASK_EMPTY, mStopPosition;
    private FTPTask ftpTask;
    private boolean mExiting, mDebugMode, mShowVideo, mVideoFileAvailable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        linearLayoutInfo = (LinearLayout) findViewById(R.id.ll_info);
        frameLayoutMedia = (FrameLayout) findViewById(R.id.video_frame);
        videoPlayerView = (VideoView) findViewById(R.id.video_player_view);
        videoPlayerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer vmp) {
                if (!mExiting) {
                    if (CommonClass.D) Log.d(TAG, "onCompletion");
                    startVideoPlay();
                }
            }
        });
        mediaController = new MediaController(this);
        dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int height = dm.heightPixels;
        int width = dm.widthPixels;
        videoPlayerView.setMinimumWidth(width);
        videoPlayerView.setMinimumHeight(height);
        videoPlayerView.setMediaController(mediaController);
        txtHeader = (TextView) findViewById(R.id.header_text);
        txtHeader.setText("");
        txtFooter = (TextView) findViewById(R.id.footer_text);
        txtSolariumTime = (TextView) findViewById(R.id.txtSolariumTime);
        txtSolarium = (TextView) findViewById(R.id.txtSolarium);
        txtSolarium.setText(getString(R.string.solarium_free));
        txtSolarium.setVisibility(View.GONE);
        solariumImageView = (ImageView) findViewById(R.id.imgSolarium);
        Bitmap bitmap = ((BitmapDrawable)solariumImageView.getDrawable()).getBitmap();

        mContext = this;

        tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        String srl = android.os.Build.SERIAL;
        devModel = android.os.Build.MODEL; // Device model
        devVer = android.os.Build.VERSION.RELEASE; // Device OS version

        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        ipWiFi = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        String mac = wm.getConnectionInfo().getMacAddress();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        CommonClass.dispHeight = displaymetrics.heightPixels;
        CommonClass.dispWidth = displaymetrics.widthPixels;
        double x = Math.pow(displaymetrics.widthPixels / displaymetrics.xdpi, 2);
        double y = Math.pow(displaymetrics.heightPixels / displaymetrics.ydpi, 2);
        CommonClass.screenInches = Math.sqrt(x + y);

        prefs = getSharedPreferences(CommonClass.PREF_NAME, MODE_PRIVATE);

        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();

        videoPath = Environment.getExternalStorageDirectory().toString() +"/"+
                CommonClass.getServerVideo(mContext);
        photoName = //Environment.getExternalStorageDirectory().toString() +"/"+
                CommonClass.getServerPhoto(mContext);
        showBackgroundImage(photoName);
        soundInName = //Environment.getExternalStorageDirectory().toString() +"/"+
                CommonClass.getServerSoundIn(mContext);
        soundOutName = //Environment.getExternalStorageDirectory().toString() +"/"+
                CommonClass.getServerSoundOut(mContext);
        updateDebugInfo();
        if (clockTimeUpdater == null) {
            mHandler.postDelayed(mStartVideoPlaying, TimeUnit.SECONDS.toMillis(VIDEO_DELAY_INTERVAL));
            Log.d(TAG, "clockTimeUpdater == null");
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        isEthOn = CommonClass.isEthOn();
//		String line = "eth0     UP                   192.168.2.230/24  0x0000 00";
        ipEth = CommonClass.ipEth0;
        showDeviceInfo();
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();

        firstRun = false;
        CommonClass.setActiveFlag(mContext, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        serverThread.interrupt();
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateDebugInfo() {
        txtHeader.setVisibility(DEBUG_MODE || mDebugMode ? View.VISIBLE : View.GONE);
        txtFooter.setVisibility(DEBUG_MODE || mDebugMode ? View.VISIBLE : View.GONE);
    }

    private void showMedia(boolean showInfo) {
        if (CommonClass.D) Log.d(TAG, "showMedia=" + showInfo);
        if (showInfo) {
                stopPlaying();
        }
        if (videoPath != null && !videoPath.isEmpty()) {
            File file = new File(videoPath);
            mVideoFileAvailable = file.exists() && file.isFile();
        }
        if (mVideoFileAvailable) {
            linearLayoutInfo.setVisibility(showInfo ? View.VISIBLE : View.INVISIBLE);
            frameLayoutMedia.setVisibility(!showInfo ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void startVideoPlay() {
        if (CommonClass.D) Log.d(TAG, "trying to startVideoPlay: "+ videoPath);
        if (videoPath != null && !videoPath.isEmpty()) {
            File file = new File(videoPath);
            mVideoFileAvailable = file.exists() && file.isFile();
            if (mVideoFileAvailable) {
                if (CommonClass.D) Log.d(TAG, "startVideoPlay: "+ videoPath);
                videoPlayerView.setVideoPath(videoPath);
                mExiting = false;
                videoPlayerView.seekTo(mStopPosition);
                mStopPosition = 0;
                videoPlayerView.start();
            }
        }
    }

    private void stopPlaying() {
        mExiting = true;
        if (videoPlayerView.isPlaying()) {
            if (CommonClass.D) Log.d(TAG, "stopPlaying");
            if (videoPlayerView.canPause()) {
                mStopPosition = videoPlayerView.getCurrentPosition();
                videoPlayerView.pause();
                Toast.makeText(this, "stopVideoPlay", Toast.LENGTH_SHORT).show();
            } else
                videoPlayerView.stopPlayback();
        }
    }

    private void showDeviceInfo() {
        String adapter, ip;
        ip = CommonClass.getDeviceIpAddress();
        if (isEthOn) {
            adapter = "eth0";
            ip = ipEth;
        } else {
            adapter = "wifi";
            ip = ipWiFi;
            Log.d(TAG, "showDeviceInfo");
        }
        serverInfo = String.format("Device info: %s; ver=%s %s=%s", devModel, devVer, adapter, ip);
        txtFooter.setText(serverInfo);

    }

    public void onImageClick(View v) {
        showInfo = !showInfo;
        txtHeader.setVisibility(showInfo ? View.VISIBLE : View.GONE);
        txtFooter.setVisibility(showInfo ? View.VISIBLE : View.GONE);
        txtSolarium.setVisibility(!showInfo ? View.VISIBLE : View.GONE);
    }

    class ServerThread implements Runnable {

        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
                updateConversationHandler.post(new updateUIThreadServer(":"+ SERVERPORT));
                int timeout = serverSocket.getSoTimeout();
                Log.d(TAG, "server timeout: " + timeout);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted() && serverSocket != null) {
                try {
                    socket = serverSocket.accept();
                    updateConversationHandler.post(new updateUIThreadServer("; connected " + socket.getRemoteSocketAddress()));
                    Log.d(TAG, "connected " + socket.getRemoteSocketAddress());
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();

                } catch (SocketTimeoutException e) {

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                serverSocket.close();
                serverSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class CommunicationThread implements Runnable {
        private Socket clientSocket;
        private BufferedReader input;
        PrintWriter out;

        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    if (read != null && read.length() > 0) {
                        Log.d(TAG, "read: " + read);
                        String resMsg = (!checkInput(read)) ? "error" : "ok";
                        //Write data to the data output stream
                        out.println(resMsg);

                        updateConversationHandler.post(new updateUIThread(read));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private boolean checkInput(String input) {
        boolean res = false;
        try {
            if (input.indexOf(PFX_FTP) >= 0) {
                lastCommand = CMD_FTP;
                ftpAddr = input.substring(input.indexOf(PFX_FTP) + PFX_FTP.length());
                ftpAddr = ftpAddr.trim();
                CommonClass.setServerIP(mContext, ftpAddr);
                res = true;

            } else if (input.indexOf(PFX_VIDEO) >= 0) {
                lastCommand = CMD_VIDEO;
                videoName = input.substring(input.indexOf(PFX_VIDEO)+ PFX_VIDEO.length());
                videoName = videoName.trim();
                CommonClass.setServerVideo(mContext, videoName);

                videoPath = Environment.getExternalStorageDirectory().toString() +"/"+
                        CommonClass.getServerVideo(mContext);
                Log.d(TAG, "input.indexOf");
                getFtpFile(videoName);
                res = true;

            } else if (input.indexOf(PFX_PHOTO) >= 0) {
                lastCommand = CMD_PHOTO;
                photoName = input.substring(input.indexOf(PFX_PHOTO)+ PFX_PHOTO.length());
                photoName = photoName.trim();
                CommonClass.setServerPhoto(mContext, photoName);
                getFtpFile(photoName);
                res = true;
                Log.d(TAG, "input.indexOf");
            } else if (input.indexOf(PFX_SOUND_IN) >= 0) {
                lastCommand = CMD_SOUND_IN;
                soundInName = input.substring(input.indexOf(PFX_SOUND_IN)+ PFX_SOUND_IN.length());
                soundInName = soundInName.trim();
                CommonClass.setServerSoundIn(mContext, soundInName);
                getFtpFile(soundInName);
                res = true;
                Log.d(TAG, "input.indexOf");
            } else if (input.indexOf(PFX_SOUND_OUT) >= 0) {
                lastCommand = CMD_SOUND_OUT;
                soundOutName = input.substring(input.indexOf(PFX_SOUND_OUT)+ PFX_SOUND_OUT.length());
                soundOutName = soundOutName.trim();
                CommonClass.setServerSoundOut(mContext, soundOutName);
                getFtpFile(soundOutName);
                res = true;
                Log.d(TAG, "input.indexOf");
            } else {
                int waitTime = 0;
                try {
                    waitTime = Integer.parseInt(input);
                } catch (Exception e) {
                    lastCommand = CMD_UNKNOWN;
                }
                if (waitTime > 0) {
                    lastCommand = CMD_IN;
                    if (clockTimeUpdater == null)
                        res = true;
                } else {
                    lastCommand = CMD_OUT;
//                    if (clockTimeUpdater != null)
                        res = true;
                }
            }
        } catch (Exception e) {
        }
        return res;
    }

    class updateUIThread implements Runnable {
        private boolean timeOk = false;
        private int waitTime;
        private String msg;

        public updateUIThread(String str) {
            waitTime = -1;
            this.msg = str;
            try {
                waitTime = Integer.parseInt(str);
                Log.d(TAG, "updateUIThread ");
            } catch (Exception e) {
            }
        }

        @Override
        public void run() {
            txtHeader.setText(txtHeader.getText().toString()+/*"Client Says: "+*/ msg + ";" /*"\n"*/);
            Log.d(TAG, "Client says: "+ msg);
            if (waitTime > 0) {
                mHandler.removeCallbacks(mStartVideoPlaying);
                showMedia(true);
                if (clockTimeUpdater == null) {
                    txtSolarium.setText(getString(R.string.solarium_busy));
                    clockTimeUpdater = new ClockTimeUpdater(mContext, mHandler, txtSolariumTime, waitTime);
                    clockTimeUpdater.setStartCountdown(true);
                    playSoundIn();
                }
            } else if (0 == waitTime) {
                showMedia(true);
                txtSolarium.setText(getString(R.string.solarium_free));
                if (clockTimeUpdater != null) {
                    clockTimeUpdater.setStartCountdown(false);
                    clockTimeUpdater = null;
                }
                playSoundOut();
                mHandler.postDelayed(mStartVideoPlaying, TimeUnit.SECONDS.toMillis(VIDEO_DELAY_INTERVAL));
            }
        }
    }

    private Runnable mStartVideoPlaying = new Runnable() {
        @Override
        public void run() {
            if (!videoPath.isEmpty()) {
                showMedia(false);
                startVideoPlay();
            }
        }
    };

    class updateUIThreadServer implements Runnable {
        private String msg;

        public updateUIThreadServer(String str) {
            this.msg = str;
        }

        @Override
        public void run() {
            txtFooter.setText(serverInfo + msg);
        }
    }

    /**
     * AsyncTask which handles the commiunication with clients
     */
    class ServerAsyncTask extends AsyncTask<Socket, Void, String> {
        boolean timeOk = false;
        int waitTime = -1;

        //Background task which serve for the client
        @Override
        protected String doInBackground(Socket... params) {
            String result = null;
            //Get the accepted socket object
            Socket mySocket = params[0];
            try {
                //Get the data input stream comming from the client
                InputStream is = mySocket.getInputStream();
                //Buffer the data input stream
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                //Read the contents of the data buffer
                String resMsg;
                result = br.readLine();
                Log.d(TAG, "received: "+ result);
                try {
                    waitTime = Integer.parseInt(result);
                } catch (Exception e) {
                }
                resMsg = (waitTime < 0) ? "error" : "ok";
                //Get the output stream to the client
                PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
                //Write data to the data output stream
                out.println(resMsg);

                //Close the client connection
                mySocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String msg) {
            //After finishing the execution of background task data will be write the text view
            showMedia(true);
//            stopPlaying();
            txtHeader.setText(txtHeader.getText().toString()+ msg + ";");
            if (waitTime > 0) {
                txtSolarium.setText(getString(R.string.solarium_busy));
                clockTimeUpdater = new ClockTimeUpdater(mContext, mHandler, txtSolariumTime, waitTime);
                clockTimeUpdater.setStartCountdown(true);
            } else if (0 == waitTime) {
                txtSolarium.setText(getString(R.string.solarium_free));
                if (clockTimeUpdater != null) {
                    clockTimeUpdater.setStartCountdown(false);
                    clockTimeUpdater = null;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean debug = DEBUG_MODE || mDebugMode;

        MenuItem mi = menu.findItem(R.id.action_play_in_sound);
        if (mi != null) {
            mi.setEnabled(debug );
            mi.setVisible(debug);
        }
        mi = menu.findItem(R.id.action_play_out_sound);
        if (mi != null) {
            mi.setEnabled(debug);
            mi.setVisible(debug);
        }
        mi = menu.findItem(R.id.action_play_video);
        if (mi != null) {
            mi.setEnabled(debug);
            mi.setVisible(debug);
        }
        mi = menu.findItem(R.id.action_debug_mode);
        if (mi != null) {
            mi.setTitle(mDebugMode ? getString(R.string.action_debug_mode_off) : getString(R.string.action_debug_mode_on));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_play_in_sound:
                showMedia(true);
                playSoundIn();
                return true;
            case R.id.action_play_out_sound:
                showMedia(true);
                playSoundOut();
                return true;
            case R.id.action_play_video:
                mShowVideo = !mShowVideo;
                showMedia(!mShowVideo);
                if (mShowVideo)
                    startVideoPlay();
                else
                    stopPlaying();
                return true;
            case R.id.action_exit:
                serverThread.interrupt();
                finish();
                return true;
            case R.id.action_debug_mode:
                mDebugMode = !mDebugMode;
                updateDebugInfo();
                invalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void playSoundIn() {
        if (soundInName != null && !soundInName.isEmpty()) {
            File file = new File(Environment.getExternalStorageDirectory().toString() +"/"+ soundInName);
            if (file.exists() && file.isFile()) {
                playSoundFile(soundInName);
            } else
                playAssetsFile(ASSET_SOUND_IN);
        } else
            playAssetsFile(ASSET_SOUND_IN);
    }

    private void playSoundOut() {
        if (soundOutName != null && !soundOutName.isEmpty()) {
            File file = new File(Environment.getExternalStorageDirectory().toString() +"/"+ soundOutName);
            if (file.exists() && file.isFile()) {
                playSoundFile(soundOutName);
            } else
                playAssetsFile(ASSET_SOUND_OUT);
        } else
        playAssetsFile(ASSET_SOUND_OUT);
    }

    private void playSoundFile(String soundName) {
        String filePath = Environment.getExternalStorageDirectory().toString() + "/"+ soundName;
        mediaPlayer = new  MediaPlayer();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            Log.d(TAG, "playSoundFile");
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();
    }

    private void playAssetsFile(String assetsName) {
        AssetFileDescriptor afd = null;
        try {
            afd = getAssets().openFd(assetsName);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
            Log.d(TAG, "playAssetsFile");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showBackgroundImage(String photoName) {
        if (photoName != null && !photoName.isEmpty()) {
            File file = new File(Environment.getExternalStorageDirectory().toString() +"/"+ photoName);
            if (file.exists() && file.isFile()) {
                String filePath = Environment.getExternalStorageDirectory().toString() + "/" + photoName;
                Bitmap srcBitmap;
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inJustDecodeBounds = true;
                srcBitmap = BitmapFactory.decodeFile(filePath, options);

                int reqWidth = solariumImageView.getWidth();
                int reqHeight = solariumImageView.getHeight();
                if (reqWidth > 0 && reqHeight > 0)
                    options.inSampleSize = CommonClass.calculateInSampleSize(options, reqWidth, reqHeight);
                options.inJustDecodeBounds = false;
                srcBitmap = BitmapFactory.decodeFile(filePath, options);
                solariumImageView.setImageBitmap(srcBitmap);
            }
        }
    }

    private void getFtpFile(final String fileName) {
        if (currentTask == FTPTask.TASK_EMPTY) {
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    currentTask = FTPTask.TASK_GET_FILE;
                    ftpTask = new FTPTask(mContext);
                    ftpTask.setResponseCallback((FTPTask.FTPResponseCallback) mContext);
                    ftpTask.setMode(currentTask);
                    ftpTask.execute(fileName);
//                    ftpTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileName);
                }
            });
        }
    }


    private void getFTPFile(String fileName) {
        FTPTask task;
        task = new FTPTask(mContext);
        task.setResponseCallback((FTPTask.FTPResponseCallback) mContext);
        task.setMode(FTPTask.TASK_GET_FILE);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, fileName);
    }


    @Override
    public void onRequestSuccess(String response) {
        String result = "";
        if (response.length() > 0) {
            switch (currentTask) {
                case FTPTask.TASK_GET_FILE:
                    switch (lastCommand) {
                        case CMD_PHOTO:
                            if (CommonClass.D) Log.d(TAG, "FTP got background photo: "+ photoName);
                            showBackgroundImage(photoName);
                            break;
                        case CMD_SOUND_IN:
                            playSoundIn();
                            break;
                        case CMD_SOUND_OUT:
                            playSoundOut();
                            break;
                    }

                    break;
            }
        } else {
            switch (currentTask) {
                case FTPTask.TASK_GET_GOODS:
                    result = getString(R.string.m_download_result_err) + FTPTask.FTP_GET_FILE;
                    break;
            }
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();
        }
        currentTask = FTPTask.TASK_EMPTY;
    }

    @Override
    public void onRequestError(Exception error) {
        currentTask = FTPTask.TASK_EMPTY;
        Toast.makeText(getApplicationContext(), "Exception: " + error.getMessage(), Toast.LENGTH_LONG).show();
    }


}