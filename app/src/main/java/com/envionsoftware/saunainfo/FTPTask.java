package com.envionsoftware.saunainfo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetAddress;

public class FTPTask extends AsyncTask<String, Void, Object> {
	private static final String TAG = "FTPTask";
	private static final int CONNECTION_TIMEOUT = 20*1000;
	public static final int SOCKET_TIMEOUT_MIN = 18*1000;
	public static int SOCKET_TIMEOUT = 18*1000;
    private static final boolean SHOW_PROGRESS = false;
	private static final String FTP_SERVER = "192.168.2.239";//"ftp://192.168.2.239";
	private static final String FTP_USER = "sauna";
	public static final String FTP_GET_FILE = "goods.txt";
	public static final String FTP_PUT_FILE = "inventory.txt";
	public static final String FTP_PUT_NO_GDS_FILE = "inv_no_gds.txt";
	public static final int TASK_EMPTY = 0;
	public static final int TASK_GET_FILE = 4;
	public static final int TASK_GET_GOODS = 1;
	public static final int TASK_PUT_INVENTORY = 2;
	public static final int TASK_PUT_INV_NO_GDS = 3;
	private ProgressDialog mProgress = null;
	String title;

	public interface FTPResponseCallback { 
		public void onRequestSuccess(String response);
		public void onRequestError(Exception error);
	}

	final FTPClient ftpClient;
	private boolean error;
	private int curMode;
	Context mContext;
	
	private WeakReference<FTPResponseCallback> mCallback;
	
	public FTPTask(Context context) {
		mContext = context;
		curMode = TASK_EMPTY;
		ftpClient = new FTPClient();
	}	
	
	public FTPTask(Context context, int gMode) {
		mContext = context;
		curMode = gMode;
		ftpClient = new FTPClient();
	}	
	
	public void setResponseCallback(FTPResponseCallback callback) { 
		mCallback = new WeakReference<FTPResponseCallback>(callback);
	} 
	
	public void setMode(int mode) {
		curMode = mode;
	}
	
	public int getMode() {
		return curMode;
	}
	
	private static String getServerURI(Context context) {
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		String serv = sharedPrefs.getString("server_addr", FTP_SERVER);
		return serv;
	}
	
		
	@Override
	protected Object doInBackground(String... params) {
		try {
//			String servAddr = getServerURI(mContext);
            String servAddr = CommonClass.getServerIP(mContext);
			switch (curMode) {
				case TASK_GET_FILE:
					getFile(servAddr, params[0]);
					break;
                case TASK_GET_GOODS:
                    getFile(servAddr, params[0]);
                    break;
                case TASK_PUT_INV_NO_GDS:
                case TASK_PUT_INVENTORY:
                    putFile(servAddr, params[0]);
                    break;
			}
			if (!error) {
				return params[0];
			} else {
				return "";
			}			
		} catch (Exception e) {
			Log.w(TAG, e);
			return e; 
		} finally {
			curMode = TASK_EMPTY;
		}
	}
	
	@Override
	protected void onPreExecute() {
		String msg = "";
		switch (curMode) {
			case TASK_GET_FILE:
				title = mContext.getString(R.string.m_wait_file);
				msg = mContext.getString(R.string.m_get_file);
				break;
//            case TASK_GET_GOODS:
//                title = mContext.getString(R.string.m_wait_data);
//                msg = mContext.getString(R.string.m_get_data);
//                break;
//            case TASK_PUT_INV_NO_GDS:
//            case TASK_PUT_INVENTORY:
//                title = mContext.getString(R.string.m_send_data);
//                msg = mContext.getString(R.string.m_put_data);
//                break;
		}
        if (SHOW_PROGRESS)
		    mProgress = ProgressDialog.show(mContext, title, msg, true);
	}

	@Override
	protected void onPostExecute(Object result) {
		if (mProgress != null)
			mProgress.dismiss();
		if (mCallback != null && mCallback.get() != null) { 
			if (result instanceof String) {
				mCallback.get().onRequestSuccess((String) result);
			} else if (result instanceof Exception) {
				mCallback.get().onRequestError((Exception) result);
			} else { 
				mCallback.get().onRequestError(new IOException("Unknown Error Contacting Host"));
			} 
		} 
	}
	
	private void getFile(String server, String fileName) throws Exception {
		int reply;
		try {
			error = false;
            ftpClient.setConnectTimeout(CONNECTION_TIMEOUT);
            ftpClient.connect(InetAddress.getByName(server));
			// After connection attempt, you should check the reply code to verify
            // success.
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
            	ftpClient.disconnect();
            	error = true;
//                System.err.println("FTP server refused connection.");
//                System.exit(1);
            }			
		} catch (IOException e) {
            if (ftpClient.isConnected()) {
                try {
                	ftpClient.disconnect();
                } catch (IOException f) {
                    // do nothing
                }
            }
            throw new IOException(e);
		}
		try {
			if (ftpClient.isConnected()) {
				if (!ftpClient.login(FTP_USER, "")) {
					ftpClient.logout();
				}
				ftpClient.setBufferSize(8192*16);
				ftpClient.changeWorkingDirectory("/");
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//				ftpClient.enterLocalPassiveMode();

				String localName = Environment.getExternalStorageDirectory().toString() +"/"+ fileName;
				if (CommonClass.D) Log.d(TAG, "saving to file: " + localName);

                File file = new File(localName);
                FileOutputStream fos = new FileOutputStream(file);

//                FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
				
//				OutputStream output = null;
//	            output = new FileOutputStream(fileName);
	            ftpClient.retrieveFile(fileName, fos);
	            long size = fos.getChannel().size();
	            if (size <= 0)
	            	error = true;
	            fos.close();			
				
				ftpClient.noop(); // check that control connection is working OK
				ftpClient.logout();
			}
		} catch (FTPConnectionClosedException e) {
            error = true;
//            System.err.println("Server closed connection.");
            throw new FTPConnectionClosedException(e.getMessage());
        } catch (IOException e) {
            error = true;            
            throw new IOException(e);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                }
                catch (IOException f) {
                    // do nothing
                }
            }
        }
	}

	private void putFile(String server, String fileName) throws Exception {
		int reply;
		try {
			error = false;
			ftpClient.connect(InetAddress.getByName(server));
			// After connection attempt, you should check the reply code to verify
            // success.
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
            	ftpClient.disconnect();
//                System.err.println("FTP server refused connection.");
//                System.exit(1);
            }			
		} catch (IOException e) {
            if (ftpClient.isConnected()) {
                try {
                	ftpClient.disconnect();
                } catch (IOException f) {
                    // do nothing
                }
            }
            throw new IOException(e);
		}
		try {
			if (ftpClient.isConnected()) {
				if (!ftpClient.login(FTP_USER, "")) {
					ftpClient.logout();
				}
		//		ftpClient.changeWorkingDirectory(serverRoad);
				ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//				ftpClient.enterLocalPassiveMode();
//				InputStream buffIn = null;					
//				buffIn = new BufferedInputStream(new FileInputStream(FTP_GET_FILE/*fileName*/));
				FileInputStream fis = mContext.openFileInput(fileName);
				long size = fis.getChannel().size();
									
				ftpClient.storeFile(fileName, fis);
				fis.close();
				ftpClient.noop(); // check that control connection is working OK
				ftpClient.logout();
			}
		} catch (FTPConnectionClosedException e) {
            error = true;
//            System.err.println("Server closed connection.");
            throw new FTPConnectionClosedException(e.getMessage());
        } catch (IOException e) {
            error = true;
            e.printStackTrace();
            throw new IOException(e);
        } finally {
            if (ftpClient.isConnected()) {
                try {
                    ftpClient.disconnect();
                }
                catch (IOException f) {
                    // do nothing
                }
            }
        }
	}
	
	
}
