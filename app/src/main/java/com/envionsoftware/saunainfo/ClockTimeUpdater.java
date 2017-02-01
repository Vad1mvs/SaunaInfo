package com.envionsoftware.saunainfo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class ClockTimeUpdater {
	private static final boolean D = true;
	private static final String TAG = "ClockUpdater";
	public static final String TIME_ELAPSE_ACTION = "TimeElapseAction";
	private Handler mHandler;
	private TextView mClockView;
	private Context mContext;
	private LocalBroadcastManager lbm;
    private boolean startCountdown = false;
    private int mDuration, leftDuration;

    public boolean isStartCountdown() {
        return startCountdown;
    }

    public void setStartCountdown(boolean startCountdown) {
        this.startCountdown = startCountdown;
        if (startCountdown) {
            showTime();
            mHandler.postDelayed(timerTask, TimeUnit.SECONDS.toMillis(1));
        } else {
            hideTime();
            mHandler.removeCallbacks(timerTask);
        }
    }

    private void showTime() {
        if (mClockView != null && mDuration > 0) {
            mClockView.setVisibility(View.VISIBLE);
            mClockView.setTextColor(Color.RED);
            mClockView.setText(CommonClass.printSecondsInterval(mDuration));
        }
    }

    private void hideTime() {
        if (mClockView != null) {
            mClockView.setVisibility(View.GONE);
        }
    }

    public ClockTimeUpdater(Context context, Handler handler, TextView clockView, int duration) {
        mContext = context;
        mHandler = handler;
        mClockView = clockView;
        lbm = LocalBroadcastManager.getInstance(context);
        mDuration = duration;
        leftDuration = mDuration;
        showTime();
//        mHandler.post(timerTask);
//        mHandler.postDelayed(timerTask, TimeUnit.SECONDS.toMillis(1));
    }

	private void sendMsg() {
		Intent intent = new Intent(TIME_ELAPSE_ACTION);
		lbm.sendBroadcast(intent);
	}

	public Runnable timerTask = new Runnable() {
        int duration, overtime = 0, dictDuration;
        String sTime;
        boolean flash;

        @Override
		public void run() {
            if (startCountdown) {
//                duration = dbSch.getCardZoneDuration(idCard);
                duration = leftDuration;
                if (duration >= 0) {
                    sTime = CommonClass.printSecondsInterval(duration--);
                } else {
                    sTime = CommonClass.printSecondsInterval(++overtime);
                    if (1 == overtime && mClockView != null)
                        mClockView.setTextColor(Color.YELLOW);
                }
                if (mClockView != null)
                    mClockView.setText(sTime);
//                dbSch.updateCardZoneDuration(idCard, duration);
                leftDuration = duration;
            } else {
                flash = !flash;
                mClockView.setVisibility(flash ? View.VISIBLE : View.INVISIBLE);
            }
			// Schedule the next update in one second
			mHandler.postDelayed(timerTask, TimeUnit.SECONDS.toMillis(1));
		}
	};

    public int getLeftDuration() {
        return leftDuration;
    }
}
