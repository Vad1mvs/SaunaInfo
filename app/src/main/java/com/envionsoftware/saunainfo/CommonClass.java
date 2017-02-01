package com.envionsoftware.saunainfo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class CommonClass {
	public static final boolean D = true;
	private static final String TAG = "CommonClass";
	public static final String PREF_NAME = "MyPref";

    public static boolean bootUp = true;//false;
	public static boolean timeSyncNTPError = true;
	public static final long UPD_DATE_SHIFT = 60*60*1000;
	public static int dispWidth, dispHeight, verCode;
	public static double screenInches;
    public static boolean hiResScreen = false;
	public static final int textSize = 18;
	public static boolean connectionError = false, sendLog = false;
	public static String ipEth0 = "";
    public static long idGoodsLastWeighing;
	public static final SimpleDateFormat dateFormatYY = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat dateFormatMM = new SimpleDateFormat("dd.MM.yyyy HH:mm");
	public static final SimpleDateFormat dateFormatMMM = new SimpleDateFormat("ccc, dd MMM");
	public static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

//	public static int idOwner, idEntGroup;
	private static final String LAST_DICTIONARY_UPDATE_FILENAME = "last_dict_update.txt";
	private static final String LAST_REPLICATOR_TIME_FILENAME = "last_repl_time.txt";
	private static final String OWNER_ID = "IdOwner";
	private static final String OWNER_NAME = "OwnerName";
	public static final String SERVER_IP = "serverIP";
	public static final String VIDEO_NAME = "video";
	public static final String PHOTO_NAME = "photo";
	public static final String SOUND_IN_NAME = "sound_in";
	public static final String SOUND_OUT_NAME = "sound_out";
	private static final String GROUP_NAME = "IdGroup";
	private static final String EMP_ID = "IdEmp";
	private static final String AWARDS_MONTH = "awardsMonth";
	private static final String ACTIVE_FLAG = "activeFlag";
	private static final String EVENT_PHOTO = "eventPhoto";
	private static final String EVENT_WRK_ID = "eventWrkId";
	private static final String LAST_UPD = "lastUpd";
	private static final String VER_DATE = "verDate";
	private static final String SU = "su";
	private static final String DEBUG_EVENTS = "debug_events";

    public static String mIMEI;
    public static long mDepId;



	public static void showMessage(Context mContext, String msgTitle, String msg) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
	    builder
	    .setTitle(msgTitle)
	    .setMessage(msg)
	    .setIcon(android.R.drawable.ic_dialog_alert)
	    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //do some thing here which you need
                dialog.dismiss();
            }
        });
	    AlertDialog alert = builder.create();
	    alert.show();
	}

	//Create a new file and write some data
	private static boolean writeDate2File(Context mContext, Date aDate, String fileName) {
		boolean res = false;
		try {
			FileOutputStream mOutput = mContext.openFileOutput(fileName, Activity.MODE_PRIVATE);
//			String data = aDate.toString();
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
			String data = df.format(aDate);
			if (D) Log.e(TAG, "writeLastUpdate = " + data);
			mOutput.write(data.getBytes());
			mOutput.close();
			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

    private static boolean writeString2File(Context mContext, String data, String fileName) {
        boolean res = false;
        try {
            FileOutputStream mOutput = mContext.openFileOutput(fileName, Activity.MODE_PRIVATE);
            mOutput.write(data.getBytes());
            mOutput.close();
            res = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    private static String readDateFromFile(Context mContext, String fileName) {
		String res = "";
		try {
			FileInputStream mInput = mContext.openFileInput(fileName);
			byte[] data = new byte[12];
			mInput.read(data);
			mInput.close();
			res = new String(data);
			if (res.length() == 0)
				res = "201309010000";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static boolean writeReplTime(Context mContext, Date aDate) {
		return writeDate2File(mContext, aDate, LAST_REPLICATOR_TIME_FILENAME);
	}

	public static String readReplTime(Context mContext) {
		return readDateFromFile(mContext, LAST_REPLICATOR_TIME_FILENAME);
	}

	//Create a new file and write some data
	public static boolean writeLastUpdate(Context mContext, Date aDate) {
		return writeDate2File(mContext, aDate, LAST_DICTIONARY_UPDATE_FILENAME);
	}

	public static String readLastUpdate(Context mContext) {
		return readDateFromFile(mContext, LAST_DICTIONARY_UPDATE_FILENAME);
	}


    public static void Copy2Clipboard(String text, Activity activity) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
		    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		    clipboard.setText(text);
		} else {
		    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		    android.content.ClipData clip = android.content.ClipData.newPlainText("", text);
		    clipboard.setPrimaryClip(clip);
		}
    }

	public static int convertDpToPixel(Context mContext, float dp) {
	       DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
	       float px = dp * (metrics.densityDpi / 160f);
	       return (int) px;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void showUndo(final View viewContainer) {
	    viewContainer.setVisibility(View.VISIBLE);
	    viewContainer.setAlpha(1);
	    viewContainer.animate().alpha(0.4f).setDuration(5000)
	        .withEndAction(new Runnable() {

	          @Override
	          public void run() {
	        	  viewContainer.setVisibility(View.GONE);
	          }
	    });
	}

	public static boolean isNetworkAvailable(Context mContext) {
	    ConnectivityManager connectivityManager
	          = (ConnectivityManager) mContext.getSystemService(mContext.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

//+++++++++++++++++
	public static int getOwner(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getInt(OWNER_ID, 0);
	}

	public static void setOwner(Context mContext, int idOwner) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putInt(OWNER_ID, idOwner);
    	editor.commit();
	}

	public static boolean getActiveFlag(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getBoolean(ACTIVE_FLAG, false);
	}

	public static void setActiveFlag(Context mContext, boolean flag) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putBoolean(ACTIVE_FLAG, flag);
    	editor.commit();
	}

	public static String getServerIP(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getString(SERVER_IP, "192.168.0.1");
	}

	public static void setServerVideo(Context mContext, String name) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(VIDEO_NAME, name);
		editor.commit();
	}

	public static String getServerVideo(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getString(VIDEO_NAME, "");
	}

	public static void setServerPhoto(Context mContext, String name) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PHOTO_NAME, name);
		editor.commit();
	}

	public static String getServerPhoto(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getString(PHOTO_NAME, "");
	}

	public static void setServerSoundIn(Context mContext, String name) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SOUND_IN_NAME, name);
		editor.commit();
	}

	public static String getServerSoundIn(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getString(SOUND_IN_NAME, "");
	}

	public static void setServerSoundOut(Context mContext, String name) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SOUND_OUT_NAME, name);
		editor.commit();
	}

	public static String getServerSoundOut(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getString(SOUND_OUT_NAME, "");
	}

	public static void setServerIP(Context mContext, String addr) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(SERVER_IP, addr);
		editor.commit();
	}

	public static String getVerDate(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getString(VER_DATE, "");
	}

	public static void setVerDate(Context mContext, String date) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
        editor.putString(VER_DATE, date);
    	editor.commit();
	}

	public static Date getLastDateUpdate(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		String sDate = prefs.getString(LAST_UPD, "");
		Date date = Calendar.getInstance().getTime();
        date.setTime(date.getTime() - UPD_DATE_SHIFT); // an hour earlier
		try {
			date = df.parse(sDate);
		} catch (ParseException e) {

		}
		return date;
	}

	public static void setLastDateUpd(Context mContext, Date date) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
   		SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
   		String sDate = df.format(date);
    	editor.putString(LAST_UPD, sDate);
    	editor.commit();
	}

	public static boolean getSUMode(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getBoolean(SU, false);
//        return true;
	}

	public static void setSUMode(Context mContext, boolean mode) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putBoolean(SU, mode);
    	editor.commit();
	}

	public static boolean getDebugMode(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getBoolean(DEBUG_EVENTS, false);
	}

	public static void setDebugMode(Context mContext, boolean mode) {
		SharedPreferences prefs = mContext.getSharedPreferences(PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putBoolean(DEBUG_EVENTS, mode);
    	editor.commit();
	}

	//+++++++++++++++++
	public static String getOwnerName(Context mContext) {
		SharedPreferences prefs = mContext.getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
		return prefs.getString(OWNER_NAME, "");
	}

	public static void setOwnerName(Context mContext, String ownerName) {
		SharedPreferences prefs = mContext.getSharedPreferences(CommonClass.PREF_NAME, mContext.MODE_PRIVATE);
   		SharedPreferences.Editor editor = prefs.edit();
    	editor.putString(OWNER_NAME, ownerName);
    	editor.commit();
	}

	// http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
	public static String bytesToHex(byte[] bytes)	{
	    char[] hexChars = new char[ bytes.length * 2 ];
	    for( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[ j ] & 0xFF;
	        hexChars[ j * 2 ] = hexArray[ v >>> 4 ];
	        hexChars[ j * 2 + 1 ] = hexArray[ v & 0x0F ];
	    }
	    return new String(hexChars);
	}

	public static String sha1Hash(String toHash) {
	    String hash = null;
	    try {
	        MessageDigest digest = MessageDigest.getInstance("SHA-1");
	        byte[] bytes = toHash.getBytes("UTF-8");
	        digest.update(bytes, 0, bytes.length);
	        bytes = digest.digest();
	        // This is ~55x faster than looping and String.formating()
	        hash = CommonClass.bytesToHex( bytes );
	    } catch( NoSuchAlgorithmException e ) {
	    	Log.e(TAG, "Error initializing SHA1 message digest");
	    } catch( UnsupportedEncodingException e ) {
	        e.printStackTrace();
	    }
	    return hash;
	}

	public static String getFormattedDate(String dateStr) {
		String result = "", dayStr;
		Calendar now = Calendar.getInstance();
		Calendar calDate = Calendar.getInstance();
		Date date, nowDate;
		int dateDiff;
     	try {
     		if (dateStr != null && dateStr.length() > 0) {
	     		date = dateFormatYY.parse(dateStr);
	     		calDate.setTime(date);

	     		if (calDate.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
//	     			if (calDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
	     				dateDiff = now.get(Calendar.DAY_OF_YEAR) - calDate.get(Calendar.DAY_OF_YEAR);
	     				if (dateDiff <= 31) {
		     				switch (dateDiff) {
		     				case 0:
		     					dayStr = "сегодня";
		     					break;
		     				case 1:
		     					dayStr = "вчера";
		     					break;
//		     				case 2:
//		     					dayStr = "позавчера";
//		     					break;
		     				default:
		     					dayStr = dateFormatMMM.format(date);
		     				}
		     				result = dayStr + " " + timeFormat.format(date);
	     				} else {
	     					result = dateFormatMM.format(date);
	     				}
//	     			} else
//	     				result = DBSchemaHelper.dateFormatMM.format(date);
	     		} else
	     			result = dateFormatMM.format(date);
     		} else {
     			result = "";
     		}
     	} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
     	}
		return result;
	}

    public static void copy2Clipboard(String text, Activity activity) {
		int sdk = android.os.Build.VERSION.SDK_INT;
		if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
		    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		    clipboard.setText(text);
		} else {
		    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		    android.content.ClipData clip = android.content.ClipData.newPlainText("", text);
		    clipboard.setPrimaryClip(clip);
		}
    }
    
    public static boolean isEthOn() {
        try {
            String line;
            boolean r = false;
            Process p = Runtime.getRuntime().exec("netcfg");
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {   
                if (line.contains("eth0")) {
                    if (line.contains("UP")) {
                        r = true;
						ipEth0 = getIP(line);
                    } else {
                        r = false;
						ipEth0 = "";
                    }
					break;
                }
            }   
            input.close();
            Log.e("OLE", "isEthOn: " + r);
            return r; 
        } catch (IOException e) {
            Log.e("OLE", "Runtime Error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

	private static String getIP(String line) {
		String res = "";
		boolean space = false;
		int space_cnt = 0;
		for (int i = 0; i < line.length(); i++) {
			if (!space && line.charAt(i) == ' ') {
				space = true;
				space_cnt++;
			} else if (space && line.charAt(i) != ' ') {
				space = false;
				if (space_cnt == 2) {
					int end = line.indexOf(" ", i);
					int end2 = line.indexOf("/", i);
					if (end2 > 0)
						end = end2;
					res = line.substring(i, end);
					break;
				}
			}
		}
		return res;
	}

	public static String getEthIP() {
		try {
			String line;
			String r = "";
			Process p = Runtime.getRuntime().exec("netcfg");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				if (line.contains("eth0")) {
					if (line.contains("UP")) {
						r = getIP(line);
						break;
					}
				}
			}
			input.close();
			return r;
		} catch (IOException e) {
			Log.e("OLE", "Runtime Error: " + e.getMessage());
			e.printStackTrace();
			return "";
		}
	}

	public static void turnEthOnOrOff() {
        try {
            if (isEthOn()) {
                Runtime.getRuntime().exec("ifconfig eth0 down");
            } else {
                Runtime.getRuntime().exec("ifconfig eth0 up");
            }

        } catch (IOException e) {
            Log.e("OLE", "Runtime Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static boolean doesEthExist() {
        List<String> list = getListOfNetworkInterfaces();
        return list.contains("eth0");
    }

    public static List<String> getListOfNetworkInterfaces() {
        List<String> list = new ArrayList<String>();
        Enumeration<NetworkInterface> nets;
        
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
            return null;
        }

        for (NetworkInterface netint : Collections.list(nets)) {
            list.add(netint.getName());
        }
        return list;
    }
    
	public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		if (bm != null) {
		    int width = bm.getWidth();
		    int height = bm.getHeight();
		    float scaleWidth = ((float) newWidth) / width;
		    float scaleHeight = ((float) newHeight) / height;
		    // CREATE A MATRIX FOR THE MANIPULATION
		    Matrix matrix = new Matrix();
		    // RESIZE THE BIT MAP
		    matrix.postScale(scaleWidth, scaleHeight);
		
		    // "RECREATE" THE NEW BITMAP
		    Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
		    return resizedBitmap;
		} else
			return null;
	}


	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if ((height > reqHeight || width > reqWidth) && reqHeight > 0 && reqWidth > 0) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while (((halfHeight / inSampleSize) > reqHeight && height > width)||
					(halfWidth / inSampleSize) > reqWidth && width >= height) {
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromStream(FileInputStream fIn, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeStream(fIn, null, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeStream(fIn, null, options);
	}	

	public static void changeSystemTime(String year,String month,String day,String hour,String minute,String second){
	    try {
	        Process process = Runtime.getRuntime().exec("su");
	        DataOutputStream os = new DataOutputStream(process.getOutputStream());
	        String command = "date -s "+year+month+day+"."+hour+minute+second+"\n";
	        Log.e("command", command);
            os.writeBytes(command);
	        os.flush();
            os.writeBytes("exit\n");
	        os.flush();
	        process.waitFor();
	    } catch (InterruptedException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}	
	
	public static String changeSystemTime(String dateDotTime){
		String res = "changeSystemTime: ", response;
	    try {
	        Process process = Runtime.getRuntime().exec("su");
	        DataOutputStream os = new DataOutputStream(process.getOutputStream());
	        String command = "date -s "+ dateDotTime +"\n";
	        Log.d("command", command);
	        os.writeBytes(command);
	        os.flush();
	        os.writeBytes("exit\n");
	        os.flush();
			int result = process.waitFor();

			InputStream in = process.getInputStream();
			response = "Response From Exe: "+ getInput(in);
			res += response;
			System.out.println(response);

			InputStream err = process.getErrorStream();
			response = "Error From Exe: " + getInput(err);
			System.out.println(response);
			res += "; " + response;

		} catch (InterruptedException e) {
			res = e.getMessage();
//	        e.printStackTrace();
	    } catch (IOException e) {
			res = e.getMessage();
//	        e.printStackTrace();
	    }
		return res;
	}	
	
	public static void hideNavigation(Activity activity) {  // seems doesn't work
		activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
	}
	
	public static String installPMApk(String apk) {
		// Finally! I've got a working sample.
		//
		//** PLEASE NOTE THAT YOU WILL NEED A ROOTED DEVICE AND GOOGLE STRICTLY FORBIDS THESE TYPES OF APPS ON GOOGLE PLAY. 
		//    YOU SHOULD ONLY USE THIS TECHNIQUE FOR PRIVATE DISTRIBUTION ONLY. **
		//
		// Firstly, I satisfied the following conditions.
		//
		//    1. Rooted the device
		//    2. Added permissions to my app for INSTALL PACKAGE, DELETE PACKAGE, RESTART PACKAGE, WRITE EXTERNAL STORAGE
		//    3. Installed SuperSU app from Google Play and set permissions to automatically elevate to super user when apps request it, without any user prompt
		//    4. Downloaded the new APK to be installed to a public folder -in my case the SD card.
		//
		// Then, in my application, I added this line:
		// Java.Lang.Runtime.GetRuntime().Exec (new String[]{"su", "-c", "pm install -r " + _Path +  _PackageName});
		//
		// And that's it, it works! No user prompts for silent updating.
		
		String res = "installPMApk error";
		try {
            ArrayList<String> envlist = new ArrayList<String>();
            Map<String, String> env = System.getenv();
            for (String envName : env.keySet()) {
                envlist.add(envName + "=" + env.get(envName));
            }
            String[] envp = (String[]) envlist.toArray(new String[0]);
            
	        Process process = Runtime.getRuntime().exec("su", envp);
	        DataOutputStream os = new DataOutputStream(process.getOutputStream());
	        String command = "pm install -r '"+ apk + "'\n";
	        Log.d("command", command);
	        os.writeBytes(command);
	        os.flush();
	        os.writeBytes("exit\n");
	        os.flush(); 
	        
//	        Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "pm", "install", apk }, envp);
//	        Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "pm" }, envp);
//	        Process process = Runtime.getRuntime().exec("pm", envp);
//            Process process = Runtime.getRuntime().exec(new String[] { "pm", "install", "-r", apk }, envp);

/*            
            Process process = new ProcessBuilder()
//			   .command("/system/bin/ping", "android.com")
//			   .command("su", "-c",  "ls", "-l", "/system/bin/am")
//			   .command("su", "-c",  "am", "startservice", "-n", "com.android.systemui/.SystemUIService")
//				.command("su", "-c",  "am", "start", "-n", "com.android.settings/.Settings")
//				.command("su", "-c",  "pm")
//				.command("su")
//				.command("pm")
//				.command("am", "start", "-n", "com.android.settings/.Settings")
//			   .command("su", "-c",  "pm", "install", "-r", apk)
			   .command("su", "-c",  "pm", "install -r " + apk)
//			   .command("su", "-c",  "pm install -r " + apk)
			   .redirectErrorStream(true)
			   .start();
*/            
            
	        int result = process.waitFor();
	        InputStream in = process.getInputStream();
	        System.out.println("Response From Exe: "+ getInput(in));

	        InputStream err = process.getErrorStream();
	        String sErr = getInput(err);
	        System.out.println("Error From Exe: "+ sErr);
	        res = sErr; 
	    } catch (InterruptedException e) {
			res = e.getMessage();
//	        e.printStackTrace();
		} catch (IOException e) {
			res = e.getMessage();
//			e.printStackTrace();
		}
		return res;
	}

    public static void restartSystem2() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            String command = "killall zygote"+ "\n"; // This will kill the root zygote process and cause a Android system refresh.
                                                    //This does not restart your phone's hardware, only the Android processes.
            Log.d("command", command);
            os.writeBytes(command);
            os.flush();

            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	public static void restartSystem() {
		try {
			Process process = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(process.getOutputStream());
			String command = "am broadcast android.intent.action.ACTION_SHUTDOWN"+ "\n";
			Log.d("command", command);
			os.writeBytes(command);
			os.flush();
            command = "sleep 5"+ "\n";
            Log.d("command", command);
            os.writeBytes(command);
            os.flush();
            command = "reboot"+ "\n";
            Log.d("command", command);
            os.writeBytes(command);
            os.flush();

			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void killNavigation() {
		try {
//			Process process = Runtime.getRuntime().exec(
//					new String[]{"su", "-c", "service call activity 42 s16 com.android.systemui"});
//			Process process = Runtime.getRuntime().exec("service call activity 42 s16 com.android.systemui");
			
	        Process process = Runtime.getRuntime().exec("su");
	        DataOutputStream os = new DataOutputStream(process.getOutputStream());
//	        String command = "service call activity 79 s16 com.android.systemui"+ "\n";  //before API 15
            String command = "service call activity 42 s16 com.android.systemui"+ "\n";
	        Log.d("command", command);
	        os.writeBytes(command);
	        os.flush();
	        os.writeBytes("exit\n");
	        os.flush();			
	        process.waitFor();
	    } catch (InterruptedException e) {
	        e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void startNavigation() {
		try {
	        Process process = Runtime.getRuntime().exec("su");
	        DataOutputStream os = new DataOutputStream(process.getOutputStream());
	        String command = "am startservice -n com.android.systemui/.SystemUIService"+ "\n";
	        Log.d("command", command);
	        os.writeBytes(command);
	        os.flush();
	        os.writeBytes("exit\n");
	        os.flush();			
//	        process.waitFor();


            int res = process.waitFor();

//	        InputStream in = process.getInputStream();
//            System.out.println("Response From Exe: "+ getInput(in));
//
//	        InputStream err = process.getErrorStream();
//	        String sErr = getInput(err);
//            System.out.println("Error From Exe: "+ sErr);

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            String sErr = output.toString();
            System.out.println("Error From Exe: "+ sErr);


	    } catch (InterruptedException e) {
	        e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
		
	public static void startNavigationTest() {  // doesn't work 
		try {
            String command;
//            command = "LD_LIBRARY_PATH=/vendor/lib:/system/lib service call activity 42 s16 com.android.systemui";
//            command = "/system/bin/am startservice -n com.android.systemui/.SystemUIService";
//            command = "am startservice -n com.android.systemui/.SystemUIService";
//            command = "\"ls -l /system/bin/am \"";
            ArrayList<String> envlist = new ArrayList<String>();
            Map<String, String> env = System.getenv();
            for (String envName : env.keySet()) {
                envlist.add(envName + "=" + env.get(envName));
            }
            String[] envp = (String[]) envlist.toArray(new String[0]);
//            String[] envp = new String[] {"LD_LIBRARY_PATH=/vendor/lib:/system/lib"};
//            Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "ls", "-l", "/system/bin/am" }, envp);
//            Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "am", 
//            		"startservice", "-n", "com.android.systemui/.SystemUIService" }, envp);
//            Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "am", 
//            		"start", "-n", "com.android.settings/.Settings" }, envp);
//            Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "am", 
//            		"start", "-n", "com.android.settings/.Settings" }, envp);
//            Process process = Runtime.getRuntime().exec(new String[] { "su", "-c", "/system/bin/am" }, envp);
			
			
			Process process = Runtime.getRuntime().exec(
					new String[]{"su", "-c", "am", "startservice", "-n", "com.android.systemui/.SystemUIService"}, envp);  //

//            Process process = Runtime.getRuntime().exec(
//					new String[]{"su", "-c", "am", "start", "-n", "com.android.settings/.Settings"}, envp);  

//            Process process = Runtime.getRuntime().exec(new String[]{"su", "-c",
//					"am", "start", "-a", "android.intent.action.MAIN", "-n", "com.android.browser/.BrowserActivity"}, envp);
			
//	        Process process = Runtime.getRuntime().exec("su", envp);
////	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//	        DataOutputStream os = new DataOutputStream(process.getOutputStream());
////	        command = "am startservice -n com.android.systemui/.SystemUIService"+ "\n";
//	        command = "am start -a android.intent.action.MAIN -n com.android.browser/.BrowserActivity"+ "\n";
//	        Log.d("command", command);
//	        os.writeBytes(command);
//	        os.flush();
//	        os.writeBytes("exit\n");
//	        os.flush();
			
	        int res = process.waitFor();
	        
//	        InputStream in = process.getInputStream();
//            System.out.println("Response From Exe: "+ getInput(in));
//
//	        InputStream err = process.getErrorStream();
//	        String sErr = getInput(err);
//            System.out.println("Error From Exe: "+ sErr);
            
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            String sErr = output.toString();
            System.out.println("Error From Exe: "+ sErr);

//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//	        StringBuilder log = new StringBuilder();
//	        String line;
//	        while ((line = bufferedReader.readLine()) != null) {
//	        	log.append(line);
//	        }
//	        System.out.println("Error From Exe: "+ log.toString());
	    } catch (InterruptedException e) {
	        e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static String getInput(InputStream in) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int c = -1;
        try {
			while ((c = in.read()) != -1) {
			    baos.write(c);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}             
        String response = new String(baos.toByteArray());
		return response;
	}
	
	public static void testProcess() {
		Process process = null;
		try {
			process = new ProcessBuilder()
//			   .command("/system/bin/ping", "android.com")
//			   .command("su", "-c",  "ls", "-l", "/system/bin/am")
//			   .command("su", "-c",  "am", "startservice", "-n", "com.android.systemui/.SystemUIService")
//				.command("su", "-c",  "am", "start", "-n", "com.android.settings/.Settings")
//				.command("su", "-c",  "am")
//				.command("su")
//				.command("am")
//				.command("am", "start", "-n", "com.android.settings/.Settings")
			   .command("am", "startservice", "-n", "com.android.systemui/.SystemUIService")
			   .redirectErrorStream(true)
			   .start();
			
////			String cmd = "am start -n com.android.settings/.Settings"+ "\n";
//			String cmd = "LD_LIBRARY_PATH=/vendor/lib:/system/lib am"+ "\n";
//			
////			InputStream in = process.getInputStream();
////			OutputStream out = process.getOutputStream();
////			out.write(cmd.getBytes());
//			DataOutputStream os = new DataOutputStream(process.getOutputStream());
//			os.write(cmd.getBytes());
//	        os.flush();
//	        os.writeBytes("exit\n");
//	        os.flush();			
			
//	        try {
//				process.waitFor();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
	        
			InputStream in = process.getInputStream();
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int c = -1;
            while ((c = in.read()) != -1) {
                baos.write(c);
            }             
            String response = new String(baos.toByteArray());
            System.out.println("Response From Exe : "+ response);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (process != null)
				process.destroy();
		}
	}
	
	public static void installNewApk() {
        try {
            Runtime.getRuntime().exec(new String[] {"su", "-c", "pm install -r /mnt/internal/Download/fp.apk"});
        } catch (IOException e) {
            System.out.println(e.toString());
            System.out.println("no root");
        }
	}	
	
	private void installApk(Activity activity) {
//		install from apk
		Intent promptInstall = new Intent(Intent.ACTION_VIEW)
	    	.setDataAndType(Uri.parse("file:///path/to/your.apk"),
	                    "application/vnd.android.package-archive");
		activity.startActivity(promptInstall); 

//		install from market
		Intent goToMarket = new Intent(Intent.ACTION_VIEW)
		    .setData(Uri.parse("market://details?id=com.package.name"));
		activity.startActivity(goToMarket);		
	}

	public static int dpToPx(Context context, float dp) {
        if (context == null) {
            return -1;
        }
        return (int) (dp * context.getResources().getDisplayMetrics().density);
    }

    public static int pxToDp(Context context, float px) {
        if (context == null) {
            return -1;
        }
        return (int) (px / context.getResources().getDisplayMetrics().density);
    }

	public static void updateConnStatus(Context context, TextView mConnStatusView) {
		if (connectionError) {
			mConnStatusView.setTextColor(Color.RED);
            mConnStatusView.setText(context.getString(R.string.conn_fail));
		} else {
			mConnStatusView.setTextColor(Color.BLACK);
            mConnStatusView.setText(context.getString(R.string.conn_ok));
		}
	}

	public static void updateConnStatusColor(Context context, TextView textView) {
		if (connectionError) {
			textView.setTextColor(Color.RED);
		} else {
			textView.setTextColor(context.getResources().getColor(R.color.title));
		}
	}

	public static int convertDayOfWeek(int dayOfWeek) {
//		if (1 == dayOfWeek)
//			dayOfWeek = 7;
//		else
//			dayOfWeek--;
		int day = 1;
		switch (dayOfWeek) {
			case Calendar.MONDAY:
				day = 1;
				break;
			case Calendar.TUESDAY:
				day = 2;
				break;
			case Calendar.WEDNESDAY:
				day = 3;
				break;
			case Calendar.THURSDAY:
				day = 4;
				break;
			case Calendar.FRIDAY:
				day = 5;
				break;
			case Calendar.SATURDAY:
				day = 6;
				break;
			case Calendar.SUNDAY:
				day = 7;
				break;
		}
		return day;
	}

	public static String bytesToHexString123(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	public static String byteToString(byte oneByte) {
		String hex = Integer.toHexString(oneByte & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		Log.i("info", "hex == " + hex);
		System.out.println(Integer.valueOf("F", 16));// 16
		return hex;
	}

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static String getSizeName(Context context) {
        int screenLayout = context.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "normal";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "large";
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:// is API >= 9
                return "xlarge";
            default:
                return "undefined";
        }
    }

	public static String printSecondsInterval(long interval) {
		long secInMin = 60;
		long minInHour = secInMin * 60;

		long elapsedHours = interval / minInHour;
		interval = interval % minInHour;

		long elapsedMinutes = interval / secInMin;
		interval = interval % secInMin;

		long elapsedSeconds = interval;
//		return String.format("%02d:%02d:%02d", elapsedHours, elapsedMinutes, elapsedSeconds);
		return String.format("%02d:%02d", elapsedMinutes, elapsedSeconds);
	}


	/**
	 * Get ip address of the device
	 */
	public static String getDeviceIpAddress() {
		String res = "";
		try {
			//Loop through all the network interface devices
			for (Enumeration<NetworkInterface> enumeration = NetworkInterface
					.getNetworkInterfaces(); enumeration.hasMoreElements();) {
				NetworkInterface networkInterface = enumeration.nextElement();
				//Loop through all the ip addresses of the network interface devices
				for (Enumeration<InetAddress> enumerationIpAddr = networkInterface.getInetAddresses(); enumerationIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumerationIpAddr.nextElement();
					//Filter out loopback address and other irrelevant ip addresses
					if (!inetAddress.isLoopbackAddress() && inetAddress.getAddress().length == 4) {
						//Print the device ip address in to the text view
						res = inetAddress.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			Log.e("ERROR:", e.toString());
		} finally {
			return res;
		}
	}
}
