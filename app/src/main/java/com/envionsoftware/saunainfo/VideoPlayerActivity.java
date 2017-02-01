package com.envionsoftware.saunainfo;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class VideoPlayerActivity extends Activity {
    private static final boolean D = true;
    private static final String TAG = "VideoPlayerActivity";
    private static final String VIDEO_NAME = "/video.mp4";
    VideoView videoPlayerView;
    DisplayMetrics dm;
    SurfaceView surView;
    MediaController mediaController;
    private FrameLayout frameLayout;
    private TextView textView;
    boolean mDownloading;
    private Context mContext;
    private Intent serviceIntent, uartServiceIntent, autoStartIntent;
    private LocalBroadcastManager lbm;
    private String serverURI, videoPath;
    private boolean mExiting;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video_player);
        mContext = this;
        textView = (TextView) findViewById(R.id.video_msg);
        textView.setVisibility(View.GONE);

        frameLayout = (FrameLayout) findViewById(R.id.video_frame);
        videoPlayerView = (VideoView) findViewById(R.id.video_player_view);
        videoPlayerView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer vmp) {
                if (!mExiting)
                    startPlay();
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

        lbm = LocalBroadcastManager.getInstance(this);
        videoPath = android.os.Environment.getExternalStorageDirectory() + VIDEO_NAME;

        serverURI = CommonClass.getServerIP(mContext);
        File f = new File(videoPath);
        if (f.exists() && !f.isDirectory()) {
            startPlay();
        } else {
            frameLayout.setVisibility(View.GONE);
            LoadVideoTask loadApk = new LoadVideoTask();
            loadApk.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverURI + VIDEO_NAME);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (D) Log.d(TAG, "+ ON RESUME +");
        CommonClass.setActiveFlag(mContext, true);
        serverURI = CommonClass.getServerIP(mContext);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onBackPressed() {
        stopPlaying();
        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_video_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startPlay() {
        videoPlayerView.setVideoPath(videoPath);
        videoPlayerView.start();
    }

    private void stopPlaying() {
        mExiting = true;
        if (videoPlayerView.isPlaying())
            videoPlayerView.stopPlayback();
    }

    private class LoadVideoTask extends AsyncTask<String, Integer, String> {
        private static final int CONNECTION_TIMEOUT = 20*1000;
        public static final int SOCKET_TIMEOUT_MIN = 18*1000;
        public static final int SOCKET_TIMEOUT = 18*1000;
        boolean downloaded = false;
        private String error = "";

        protected String doInBackground(String... sUrl) {
            String path =  videoPath;
            mDownloading = true;
            try {
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, new TrustManager[]{
                        new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String authType) {
                            }

                            public void checkServerTrusted(X509Certificate[] chain, String authType) {
                            }

                            public X509Certificate[] getAcceptedIssuers() {
                                return new X509Certificate[]{};
                            }
                        }
                }, null);
                HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

                HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });

                URL url = new URL(sUrl[0]);
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
/*
			        URL url = new URL("http://192.168.2.7/TimeTracking.apk");
			        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
*/
                long startTime = System.currentTimeMillis();
                connection.connect();
                Toast.makeText(VideoPlayerActivity.this, "11", Toast.LENGTH_SHORT).show();
//		        int fileLength = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(path);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
//			            publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
                long endTime = System.currentTimeMillis();
                String msg = String.format("[LoadVideoTask] Time to download %s (size=%d bytes): %s mc",
                        url.getFile(), total, String.valueOf(endTime - startTime));
//                dbSch.addLogItem(LogsRecord.INFO, new Date(), msg);
                Log.i(TAG, msg);

                downloaded = true;
            } catch (FileNotFoundException e) {
                error = String.format(getString(R.string.m_video_not_found), path);
            } catch (Exception e) {
                error = e.getMessage();
                Log.e(TAG, error);
            }
            return path;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            String msg = getString(R.string.m_video_download);
//            dbSch.addLogItem(LogsRecord.DEBUG, new Date(), msg);
            textView.setText(msg);
            textView.setVisibility(View.VISIBLE);
            Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        }

        // begin the video playback
        @Override
        protected void onPostExecute(String path) {
            if (downloaded) {
                textView.setText("");
                textView.setVisibility(View.GONE);
                frameLayout.setVisibility(View.VISIBLE);
                startPlay();
            } else {
                textView.setText(error);

            }
        }
    }


}
