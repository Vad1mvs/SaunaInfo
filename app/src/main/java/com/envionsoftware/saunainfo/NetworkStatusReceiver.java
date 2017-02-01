package com.envionsoftware.saunainfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.widget.Toast;

public class NetworkStatusReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
//		Toast.makeText(context, "Network Connectivity Status Changed!", Toast.LENGTH_SHORT).show();
		Bundle extras = intent.getExtras();
		boolean noNetwork = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
		if (extras != null) {
			String networkInfoStatus = ConnectivityManager.EXTRA_NETWORK_INFO;
			NetworkInfo netInfo = (NetworkInfo) extras.get(networkInfoStatus);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				Toast.makeText(context, context.getString(R.string.network_ok) + netInfo.getTypeName(),
						Toast.LENGTH_SHORT).show();
			} else if (noNetwork) {
				Toast.makeText(context, context.getString(R.string.network_fail),
						Toast.LENGTH_SHORT).show();
			}
		}
		
	}

}
