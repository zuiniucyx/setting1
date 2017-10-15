package com.zuiniukeji.android.setting;

import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zuiniukeji.android.setting.util.Wifi;
import com.zuiniukeji.android.setting.widget.SlideToggle;
import com.zuiniukeji.android.setting.widget.SlideToggle.OnChangeListener;

public class WifiSetting extends Activity {
	private List<ScanResult> wifiResultList;
	private ListView wifiListView;
	private SlideToggle wifi_toggle;
	private WifiAdapter wifiAdapter;
	private Handler handler = new Handler();
	private TextView wifi_state;
	private EditText et_pwd;
	private TextView btnSure;
	private WifiManager mWifiManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_wifi_setting);
		wifi_toggle = (SlideToggle) findViewById(R.id.wifi_toggle);

		wifi_state = (TextView) findViewById(R.id.wifi_state);
		wifiListView = (ListView) findViewById(R.id.wifiListView);
		wifiAdapter = new WifiAdapter();
		wifiListView.setAdapter(wifiAdapter);

		wifi_toggle.setOnChangeListener(new OnChangeListener() {
			@Override
			public void onChange(SlideToggle sb, boolean state) {
				if (wifi_toggle.mSwitchOn) {
					openWifi();
				} else {
					closeWifi();
				}
			}
		});

		wifiListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				mNumOpenNetworksKept = Settings.Secure.getInt(
						WifiSetting.this.getContentResolver(),
						Settings.Secure.WIFI_NUM_OPEN_NETWORKS_KEPT, 10);
				ScanResult scanResult = wifiResultList.get(position);
				String security = Wifi.ConfigSec.getScanResultSecurity(scanResult);
				WifiConfiguration config = Wifi.getWifiConfiguration(mWifiManager,
						scanResult, security);
				String capabilities = scanResult.capabilities;
				if (config == null) {
					if (capabilities.contains("WPA") || capabilities.contains("WEP")) {
						showInputPwd(position);
					} else {
						isWifiConned=false;
						currWifiBSSID=scanResult.BSSID;
						wifiAdapter.notifyDataSetChanged();
						Wifi.connectToNewNetwork(WifiSetting.this, mWifiManager, scanResult, null,
								mNumOpenNetworksKept);
					}
				} else {
					if (config.status == WifiConfiguration.Status.CURRENT) {
						showMoreDialog(position, true);
					} else {
						isWifiConned=false;
						currWifiBSSID=scanResult.BSSID;
						wifiAdapter.notifyDataSetChanged();
						Wifi.connectToConfiguredNetwork(WifiSetting.this, mWifiManager, config,
								false);
					}
				}
			}

		});

		wifiListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					final int position, long id) {
				ScanResult scanResult = wifiResultList.get(position);
				boolean isSavePwd = Wifi.getWifiConfiguration(mWifiManager,
						scanResult,
						Wifi.ConfigSec.getScanResultSecurity(scanResult)) != null;
				showMoreDialog(position, isSavePwd);
				return true;
			}
		});
		
		mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
//		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION); //wifi状态，是否连上，密码
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);//连上与否

	}
	
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			switch (action) {
			case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION:		
				wifiResultList = mWifiManager.getScanResults();
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mWifiManager.startScan();
					}
				}, 2000);
				break;
			case ConnectivityManager.CONNECTIVITY_ACTION:
				isWifiConned=true;
				break;
//			case WifiManager.WIFI_STATE_CHANGED_ACTION:
//				Log.e("zero",WifiManager.WIFI_STATE_CHANGED_ACTION);
//				isWifiConned=false;
//				break;
			}
			wifiAdapter.notifyDataSetChanged();
		}
	};
	private IntentFilter filter;
	private String mScanResultSecurity;
	private int mNumOpenNetworksKept;
	private boolean isWifiConned=false;
	private String currWifiBSSID="";

	void swapList(int position) {
		Collections.swap(wifiResultList, 0, position);
		wifiAdapter.notifyDataSetChanged();
	}

	// 取消保存
	private boolean forget(ScanResult scanResult) {
		currWifiBSSID="";
		final WifiConfiguration config = Wifi.getWifiConfiguration(
				mWifiManager, scanResult, mScanResultSecurity);
		boolean result=false;
		if (config != null) {
			result = mWifiManager.removeNetwork(config.networkId)
					&& mWifiManager.saveConfiguration();
		}
		return result;
	}

	private void closeWifi() {
		if (wifi_toggle.getAlpha() == 0.5f) {
			return;
		}
		if (wifiResultList != null) {
			wifiResultList.clear();
			wifiResultList = null;
		}
		wifi_toggle.setLoading();
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
			unregisterReceiver(mReceiver);
		}
		wifiAdapter.notifyDataSetChanged();
		wifi_toggle.setLoadFinish();

	}

	void openWifi() {
		// wifiResultList.clear();
		if (wifi_toggle.getAlpha() == 0.5f) {
			return;
		}
		// IsResume=false;
		wifi_state.setVisibility(View.VISIBLE);
		wifi_toggle.setLoading();
		new Thread() {
			public void run() {
				if (!mWifiManager.isWifiEnabled()) {
					mWifiManager.setWifiEnabled(true);
				}
				registerReceiver(mReceiver, filter);
				mWifiManager.startScan();
				// 0正在关闭,1WIFi不可用,2正在打开,3可用,4状态不可知
				while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
					SystemClock.sleep(100);
				}// 等待Wifi开启
				handler.postDelayed(new Runnable() {
					@Override
					public void run() {
						wifi_state.setVisibility(View.GONE);
						wifi_toggle.setLoadFinish();
						// wifiThread();
					}
				}, 3000);
			};
		}.start();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
			if (wifiResultList != null) {
				closeWifi();
			}
			wifi_toggle.mSwitchOn = false;
		} else {
			wifi_toggle.mSwitchOn = true;
			wifiResultList=mWifiManager.getScanResults();
			registerReceiver(mReceiver, filter);
			mWifiManager.startScan();
		}
	}

	protected void onPause() {
		// IsResume=false;
		super.onPause();
		if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			unregisterReceiver(mReceiver);
		}
	};

	private void showMoreDialog(final int position, final boolean isSavePwd) {
		View view = LayoutInflater.from(this).inflate(
				R.layout.dialog_wifi_long, null);
		((TextView) view.findViewById(R.id.dialog_title))
				.setText(wifiResultList.get(position).SSID);
		((TextView) view.findViewById(R.id.tv_wifi_message))
				.setText(isSavePwd ? "取消保存网络" : "连接此网络");
		View tvCancel = view.findViewById(R.id.tv_cancel);
		View tvSure = view.findViewById(R.id.tv_sure);

		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		tvSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击“确认”后的操作
				ScanResult scanResult = wifiResultList.get(position);
				if (isSavePwd) {
					forget(scanResult);
				} else {
					String capabilities = wifiResultList.get(position).capabilities;
					if (capabilities.contains("WPA")
							|| capabilities.contains("WEP")) {
						showInputPwd(position);
					} else {
						isWifiConned=false;
						currWifiBSSID=scanResult.BSSID;
						wifiAdapter.notifyDataSetChanged();
						Wifi.connectToNewNetwork(WifiSetting.this,
								mWifiManager, scanResult, null,
								mNumOpenNetworksKept);
					}
				}
				dialog.dismiss();
			}
		});
		tvCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});

		dialog.show();
		dialog.setContentView(view);
	}

	private void showInputPwd(final int position) {
		// String pwd = sp.getString(wifiResultList.get(position).BSSID, "");
		View view = LayoutInflater.from(this).inflate(
				R.layout.dialog_input_pwd, null);
		((TextView) view.findViewById(R.id.dialog_title))
				.setText(wifiResultList.get(position).SSID);
		et_pwd = (EditText) view.findViewById(R.id.et_dialog_pwd);
		CheckBox cb_show_pwd = (CheckBox) view
				.findViewById(R.id.dialog_show_pwd);
		View btnCancel = view.findViewById(R.id.tv_cancel);
		btnSure = (TextView) view.findViewById(R.id.tv_sure);
		btnSure.setTextColor(0xff999999);
		btnSure.setEnabled(false);
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.getWindow().getAttributes().dimAmount = 0.5f;
		dialog.setView(view, 0, 0, 0, 0);
		et_pwd.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				if (s.length() > 7) { // 密码最少输入8位
					btnSure.setTextColor(0xff000000);
					btnSure.setEnabled(true);
				} else {
					btnSure.setTextColor(0xff999999);
					btnSure.setEnabled(false);
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		cb_show_pwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				et_pwd.setInputType(InputType.TYPE_CLASS_TEXT
						| (isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
								: InputType.TYPE_TEXT_VARIATION_PASSWORD));
				if (et_pwd.getText().length() > 7) { // 密码最少输入8位
					btnSure.setTextColor(0xff000000);
					btnSure.setEnabled(true);
				}
			}
		});
		//
		dialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				// 打开输入框
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(et_pwd, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		dialog.show();
		btnSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String pwd = et_pwd.getText().toString();
				if (!TextUtils.isEmpty(pwd)) {
					ScanResult scanResult = wifiResultList.get(position);
					isWifiConned=false;
					currWifiBSSID=scanResult.BSSID;
					wifiAdapter.notifyDataSetChanged();
					Wifi.connectToNewNetwork(WifiSetting.this, mWifiManager,scanResult, pwd,mNumOpenNetworksKept);
					dialog.dismiss();
				}
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	}

	@Override
	protected void onStop() {
		// finish();
		super.onStop();
	}

	class WifiAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return wifiResultList == null ? 0 : wifiResultList.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			Tag tag;
			if (convertView == null) {
				convertView = View.inflate(WifiSetting.this,
						R.layout.item_wifi, null);
				tag = new Tag();
				tag.rl = (RelativeLayout) convertView
						.findViewById(R.id.wifi_item_rl);
				tag.tv = (TextView) convertView
						.findViewById(R.id.tv_wifi_title);
				tag.tv_state = (TextView) convertView
						.findViewById(R.id.wifi_state);
				tag.iv = (ImageView) convertView
						.findViewById(R.id.iv_wifi_lock);
				tag.iv_level = (ImageView) convertView
						.findViewById(R.id.iv_wifi_level);
				convertView.setLayoutParams(new ListView.LayoutParams(742, 75));
				convertView.setTag(tag);
			} else {
				tag = (Tag) convertView.getTag();
			}
			if (getCount() == 1) {
				convertView
						.setBackgroundResource(R.drawable.selector_textview_bg);
			} else {
				if (position == 0) {
					convertView
							.setBackgroundResource(R.drawable.selector_wifi_list_up);
				} else if (position == getCount() - 1) {
					convertView
							.setBackgroundResource(R.drawable.selector_wifi_list_down);
				} else {
					convertView
							.setBackgroundResource(R.drawable.selector_wifi_list_mid);
				}
				final ScanResult scanResult = wifiResultList.get(position);
				WifiInfo connectionInfo = mWifiManager.getConnectionInfo();
				try{
					if(connectionInfo!=null&&isWifiConned&&connectionInfo.getBSSID().equals(scanResult.BSSID)){
						tag.tv_state.setText("已连接");
						tag.tv_state.setVisibility(View.VISIBLE);
						tag.iv.setVisibility(View.GONE);
					}else if(!isWifiConned&&scanResult.BSSID.equals(currWifiBSSID)&&!connectionInfo.getSSID().equals("0x")){
						tag.tv_state.setText("正在连接...");
						tag.tv_state.setVisibility(View.VISIBLE);
						tag.iv.setVisibility(View.GONE);
					} else {
						tag.tv_state.setVisibility(View.GONE);
						String capabilities = scanResult.capabilities;
						if (capabilities.contains("WPA")
								|| capabilities.contains("WEP")) {
							tag.iv.setVisibility(View.VISIBLE);
						} else {
							tag.iv.setVisibility(View.GONE);
						}
					}
				}catch(Exception e){
					tag.tv_state.setVisibility(View.GONE);
					String capabilities = scanResult.capabilities;
					if (capabilities.contains("WPA")
							|| capabilities.contains("WEP")) {
						tag.iv.setVisibility(View.VISIBLE);
					} else {
						tag.iv.setVisibility(View.GONE);
					}
					e.printStackTrace();
				}
				
				
				tag.rl.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						boolean isSavePwd = Wifi.getWifiConfiguration(
								mWifiManager, scanResult, Wifi.ConfigSec
										.getScanResultSecurity(scanResult)) != null;
						showMoreDialog(position, isSavePwd);
					}
				});
				tag.tv.setText(scanResult.SSID);
				tag.iv_level.setImageLevel(Math.abs(scanResult.level));
			}

			return convertView;
		}
	}

	private class Tag {
		TextView tv;
		ImageView iv;
		ImageView iv_level;
		RelativeLayout rl;
		TextView tv_state;
	}

	// 保存已连接过的、下次自动连接
	// public void saveWifi(String ssid,String pwd,String key_mgmt) {
	// Process sh = null;
	// DataOutputStream os = null;
	// try {
	// sh = Runtime.getRuntime().exec("per-up");
	// os = new DataOutputStream(sh.getOutputStream());
	// if(key_mgmt==null){
	// os.writeBytes("echo -e \"\nnetwork={\n\tssid=\\\""+ssid+"\\\"\n\tkey_mgmt=NONE\n}\" >> /data/misc/wifi/wpa_supplicant.conf\n");
	// }else{
	// os.writeBytes("echo -e \"\nnetwork={\n\tssid=\\\""+ssid+"\\\"\n\tpsk=\\\""+pwd+"\\\"\n\tkey_mgmt="+key_mgmt+"\n\tgroup=CCMP TKIP\n\tauth_alg=OPEN\n}\""
	// +
	// " >> /data/misc/wifi/wpa_supplicant.conf\n");
	// }
	// os.flush();
	// } catch (IOException e){}
	// }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		AudioManager mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) // 音量增大键响应撤销
		{
			// 如果收音机打开状态,音量范围从0~15
			int volume;
			if (mAudioManager.getMode() == 5) {
				volume = mAudioManager.getStreamVolume(10) + 1;
				mAudioManager.setStreamVolume(10, volume,
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			} else {
				// 媒体音量，范围0~15
				volume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC) + 1;
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			}
			if (volume >= 0 && volume < 16) {
				Intent mainIntent = new Intent("com.zuiniukeji.VOLUME_CHANGED");
				mainIntent.putExtra("volume", volume);
				this.sendBroadcast(mainIntent);
			}
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)// 音量减小键响应重做
		{
			int volume;
			if (mAudioManager.getMode() == 5) {
				volume = mAudioManager.getStreamVolume(10) - 1;
				mAudioManager.setStreamVolume(10, volume,
						AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			} else {
				// 媒体音量，范围0~15
				volume = mAudioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC) - 1;
				mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
						volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			}
			if (volume >= 0 && volume < 16) {
				Intent mainIntent = new Intent("com.zuiniukeji.VOLUME_CHANGED");
				mainIntent.putExtra("volume", volume);
				this.sendBroadcast(mainIntent);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
