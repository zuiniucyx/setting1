package com.zuiniukeji.android.setting;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zuiniukeji.android.setting.util.MyToast;
import com.zuiniukeji.android.setting.widget.ListViewForScrollView;
import com.zuiniukeji.android.setting.widget.SlideToggle;
import com.zuiniukeji.android.setting.widget.SlideToggle.OnChangeListener;

public class BlueToothSetting extends Activity implements OnClickListener {
	private SlideToggle blueToothSwitch;
	private ImageView testSwitch;
	private TextView txtMyBlueTooth;
	private ListViewForScrollView lvPairing, lvCan;
	private LinearLayout llPairing, llCan;
	private RelativeLayout rlTest, rlMyBlueTooth;
	private BluetoothAdapter mBluetoothAdapter;
	private boolean blueToothSwitch_state = false;// 蓝牙是否开启
	private boolean canUpdateName = false;// 蓝牙改名
	private boolean isSearch = false;// 蓝牙改名
	private List<BluetoothDevice> canPairing = new ArrayList<BluetoothDevice>(); // 可以配对的设备
	private List<BluetoothDevice> hasPairing = new ArrayList<BluetoothDevice>();; // 已配对的设备
	private BluetoothReceiver bluetoothReceiver;
	private MyAdapter hasAdapter, canAdapter;
	private TextView btnSearch;
	private Set<BluetoothDevice> devices;// 已经配对 的设备
	BluetoothSocket socket = null;
	BluetoothA2dp mBTA2DP = null;
	BluetoothHeadset bHeadset = null;
	private int testSwitchState;
//	private TextView tvBlueToothState=null;
//	private int statePosition=0;
//	private boolean connected=false; //判断是否连接
	// private ProgressBar progressBar;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_blue_tooth_setting);
		init();
		// 注册广播接收器
		bluetoothReceiver = new BluetoothReceiver();
		IntentFilter intentFilter = new IntentFilter(
				BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		registerReceiver(bluetoothReceiver, intentFilter);

		blueToothSwitch.setOnChangeListener(new OnChangeListener() {
			@Override
			public void onChange(SlideToggle sb, boolean state) {
				if (state) {
					rlTest.setVisibility(View.VISIBLE);
					llPairing.setVisibility(View.VISIBLE);
					llCan.setVisibility(View.VISIBLE);
					if (!mBluetoothAdapter.isEnabled()) {
						mBluetoothAdapter.enable();
					}
					canUpdateName = true;

					if (hasPairing.size() == 0) {
						llPairing.setVisibility(View.GONE);
					} else {
						llPairing.setVisibility(View.VISIBLE);
					}

				} else {
					rlTest.setVisibility(View.GONE);
					llPairing.setVisibility(View.GONE);
					llCan.setVisibility(View.GONE);
					mBluetoothAdapter.disable();
					canUpdateName = false;

					mBluetoothAdapter.cancelDiscovery();
					isSearch = false;
				}
			}
		});
		testSwitch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (testSwitchState==0) {
					testSwitchState=1;
					closeDiscoverableTimeout(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
					testSwitch.setBackgroundResource(R.drawable.btn_setting_switch_off);
				}else {
					testSwitchState=0;
					closeDiscoverableTimeout(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
					testSwitch.setBackgroundResource(R.drawable.btn_setting_switch_on);
				}
			}
		});
//		testSwitch.setOnChangeListener(new OnChangeListener() {
//			@Override
//			public void onChange(SlideToggle sb, boolean state) {
//				if (!state) {
//					closeDiscoverableTimeout(BluetoothAdapter.SCAN_MODE_CONNECTABLE);
//				} else {
//					closeDiscoverableTimeout(BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE);
//				}
//			}
//		});

		lvCan.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (isSearch) {
					MyToast.makeTextAnim(getApplicationContext(), "取消搜索").show();
					mBluetoothAdapter.cancelDiscovery();
				} else {
					createBond(canPairing.get(arg2)); // 配对
				}
			}
		});
	}

	// 蓝牙设备一直是否可见
	public void closeDiscoverableTimeout(int isSee) {
		try {
			Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod(
					"setDiscoverableTimeout", int.class);
			setDiscoverableTimeout.setAccessible(true);
			Method setScanMode = BluetoothAdapter.class.getMethod(
					"setScanMode", int.class, int.class);
			setScanMode.setAccessible(true);
			setDiscoverableTimeout.invoke(mBluetoothAdapter, 1);
			setScanMode.invoke(mBluetoothAdapter, isSee, 1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void init() {
		txtMyBlueTooth = (TextView) findViewById(R.id.tv_mybluetooth);
		lvPairing = (ListViewForScrollView) findViewById(R.id.lv_pairing);
		lvCan = (ListViewForScrollView) findViewById(R.id.lv_canuse);
		blueToothSwitch = (SlideToggle) findViewById(R.id.toggle_offon);
		testSwitch = (ImageView) findViewById(R.id.toggle_test);
		llPairing = (LinearLayout) findViewById(R.id.ll_pairing);
		llCan = (LinearLayout) findViewById(R.id.ll_canuse);
		rlTest = (RelativeLayout) findViewById(R.id.rl_test);
		rlMyBlueTooth = (RelativeLayout) findViewById(R.id.rl_mybt);
		// rlCanPairing=(RelativeLayout) findViewById(R.id.rl_canpairing);
		// progressBar=(ProgressBar) findViewById(R.id.pb_search);
		btnSearch = (TextView) findViewById(R.id.btn_search);
		btnSearch.setOnClickListener(this);
		rlMyBlueTooth.setOnClickListener(this);

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			MyToast.makeTextAnim(getApplicationContext(), "本机没有找到蓝牙硬件或驱动！").show();
			finish();
		}
		txtMyBlueTooth.setText(mBluetoothAdapter.getName().toString());// 本地蓝牙名称

		canAdapter = new MyAdapter(1);
		lvCan.setAdapter(canAdapter);
		hasAdapter = new MyAdapter(0);
		lvPairing.setAdapter(hasAdapter);
	}

	// 取消配对
	private void cancelPairing(final BluetoothDevice device) {
		View view = LayoutInflater.from(this).inflate(
				R.layout.dialog_cancel_bluetooth, null);
		TextView tvDeviceName = ((TextView) view
				.findViewById(R.id.dialog_title));
		View tvCancel = view.findViewById(R.id.tv_cancel);
		View tvSure = view.findViewById(R.id.tv_sure);

		String name = device.getName();
		if (null == name) {
			name = device.getAddress();
		}
		tvDeviceName.setText(name);
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		tvSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 点击“确认”后的操作
				try {
					Method m = device.getClass().getMethod("removeBond",
							(Class[]) null);
					m.invoke(device, (Object[]) null);
				} catch (Exception e) {
				}
				if (null != dialog) {
					dialog.dismiss();
				}
				hasPairing.remove(device);
				if (hasPairing.size() == 0) {
					llPairing.setVisibility(View.GONE);
				}
				hasAdapter.notifyDataSetChanged();
			}
		});
		tvCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != dialog) {
					dialog.dismiss();
				}
			}
		});

		dialog.show();
		dialog.setContentView(view);
	}

	// 修改我的蓝牙名字
	private void updateMyBlueTooth() {
		View view = LayoutInflater.from(this).inflate(
				R.layout.dialog_updatebluetooth, null);
		final EditText etMyName = (EditText) view.findViewById(R.id.et_btname);
		Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
		Button btnSure = (Button) view.findViewById(R.id.btn_sure);

		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.setView(view, 0, 0, 0, 0);
		dialog.setOnShowListener(new OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				// 打开输入框
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(etMyName, InputMethodManager.SHOW_IMPLICIT);
			}
		});
		dialog.show();
		// dialog.getWindow().setLayout(300, 320);
		btnSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (null != etMyName.getText().toString()
						&& !"".equals(etMyName.getText().toString())) {
					mBluetoothAdapter.setName(etMyName.getText().toString());
					txtMyBlueTooth.setText(mBluetoothAdapter.getName());
					if (dialog != null) {
						dialog.cancel();
					}
				}
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialog != null) {
					dialog.cancel();
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
		blueToothSwitch_state = mBluetoothAdapter.isEnabled();
		blueToothSwitch.mSwitchOn = blueToothSwitch_state; // 设置初始值
		if (blueToothSwitch_state) {
			llPairing.setVisibility(View.VISIBLE);
			llCan.setVisibility(View.VISIBLE);
			canUpdateName = true;
			mBluetoothAdapter.startDiscovery();
		} else {
			llPairing.setVisibility(View.GONE);
			llCan.setVisibility(View.GONE);
			canUpdateName = false;
		}
		
		testSwitchState = mBluetoothAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE ? 1
				: 0;
		if (testSwitchState==1) {
			testSwitch.setBackgroundResource(R.drawable.btn_setting_switch_off);
		}else {
			testSwitch.setBackgroundResource(R.drawable.btn_setting_switch_on);
		}
		
		
		if (!blueToothSwitch_state) {
			rlTest.setVisibility(View.GONE);
		} else {
			rlTest.setVisibility(View.VISIBLE);
		}

		hasPairing.clear();
		devices = mBluetoothAdapter.getBondedDevices(); // 已经配对的蓝牙
		for (BluetoothDevice bluetoothDevice : devices) {
			hasPairing.add(bluetoothDevice);
		}
		if (hasPairing.size() == 0) {
			llPairing.setVisibility(View.GONE);
		} else {
			llPairing.setVisibility(View.VISIBLE);
		}
		hasAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(bluetoothReceiver);
	}

	/**
	 * 方法二
	 * */
	private class ConnectThread extends Thread {
		// ffffffff-f9d3-ee08-0033-c5870033c587
		// 00002902-0000-1000-8000-00805f9b34fb
		// 00001101-0000-1000-8000-00805F9B34FB
		final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
		UUID uuid = UUID.fromString(SPP_UUID);

		public ConnectThread(BluetoothDevice mBluetoothDevice) {
			device = mBluetoothDevice;
		}

		public void run() {
			if (mBluetoothAdapter == null) {
				mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			}
			mBluetoothAdapter.cancelDiscovery();
			
			if(socket==null){
				try {
					socket = device.createRfcommSocketToServiceRecord(uuid);
				} catch (IOException e) {
//					Log.e("socket", e.toString());
				}
			}
			mBluetoothAdapter.getProfileProxy(BlueToothSetting.this, mBluetoothHeadsetServiceListener, BluetoothProfile.A2DP);
			mBluetoothAdapter.getProfileProxy(BlueToothSetting.this, mBluetoothHeadsetServiceListener, BluetoothProfile.HEADSET);
			
		}
	}
	/**
	 * 蓝牙配对
	 * */
	private boolean createBond(BluetoothDevice btDevice) {
		Method createBondMethod;
		Boolean returnValue = false;
		try {
			createBondMethod = btDevice.getClass().getMethod("createBond");
			returnValue = (Boolean) createBondMethod.invoke(btDevice);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return returnValue.booleanValue();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_mybt:
			if (canUpdateName) {
				updateMyBlueTooth();
			} else {
				MyToast.makeTextAnim(getApplicationContext(), "请先开启蓝牙").show();
			}
			break;
		case R.id.btn_search:
			canPairing.clear();
			mBluetoothAdapter.startDiscovery();
			btnSearch.setText("正在搜索...");
			btnSearch.setTextColor(0xff00aa00);
			isSearch = true;
			lvCan.setEnabled(false);
		}
	}

	@Override
	protected void onStop() {
		finish();
		super.onStop();
	}

	class MyAdapter extends BaseAdapter {
		private int type = 0;

		public MyAdapter(int type) {
			super();
			this.type = type;
		}

		@Override
		public int getCount() {
			if (type == 0) {
				return hasPairing.size();
			} else {
				return canPairing.size();
			}
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final Holder holder;
			if (convertView == null) {
				holder = new Holder();
				convertView = View.inflate(BlueToothSetting.this,
						R.layout.item_bluetooth, null);
				holder.iv_icon = (ImageView) convertView
						.findViewById(R.id.iv_icon);
				holder.textView = (TextView) convertView
						.findViewById(R.id.tv_bluetooth_name);
//				holder.tvState = (TextView) convertView
//						.findViewById(R.id.tv_bluetooth_state);
				convertView.setLayoutParams(new ListView.LayoutParams(742, 75));
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			if (type == 0) {
				String name = hasPairing.get(position).getName();
				if (null == name) {
					name = hasPairing.get(position).getAddress();
				}
				holder.textView.setText(name);

//				int state=hasPairing.get(position).getBondState();
//				if (state==BluetoothDevice.BOND_BONDED) {
//					holder.tvState.setVisibility(View.INVISIBLE);
//				}
//				if (position==statePosition&&connected) {
//					holder.tvState.setVisibility(View.VISIBLE);
//				}
				if (hasPairing.size() == 1) {
					convertView
							.setBackgroundResource(R.drawable.selector_textview_bg);
				} else if (hasPairing.size() == 0) {
					llPairing.setVisibility(View.GONE);
				} else {
					llPairing.setVisibility(View.VISIBLE);
					if (position == 0) {
						convertView.setBackgroundResource(R.drawable.selector_wifi_list_up);
					} else if (position == hasPairing.size() - 1) {
						convertView.setBackgroundResource(R.drawable.selector_wifi_list_down);
					} else {
						convertView.setBackgroundResource(R.drawable.selector_wifi_list_mid);
					}
				}

				holder.iv_icon.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { // 调整到蓝牙详情
						cancelPairing(hasPairing.get(position));
					}
				});
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) { // 连接蓝牙
//						holder.tvState.setVisibility(View.VISIBLE);
//						tvBlueToothState=holder.tvState;
//						statePosition=position;
//						holder.tvState.setText("正在连接...");
						new ConnectThread(hasPairing.get(position)).start();
						MyToast.makeTextAnim(getApplicationContext(), "连接蓝牙").show();
					}
				});
			} else {
				if (canPairing.size() == 0) {
					btnSearch
							.setBackgroundResource(R.drawable.selector_textview_bg);
				} else {
					btnSearch
							.setBackgroundResource(R.drawable.selector_wifi_list_down);
					
					String name = canPairing.get(position).getName();
					if (null == name) {
						name = canPairing.get(position).getAddress();
					}
					holder.textView.setText(name);
//					holder.tvState.setVisibility(View.INVISIBLE);
					if (position == 0) {
						convertView
								.setBackgroundResource(R.drawable.selector_wifi_list_up);
					} else {
						convertView
								.setBackgroundResource(R.drawable.selector_wifi_list_mid);
					}
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
							742, 75);
					params.setMargins(0, -position - 1, 0, 0);
					btnSearch.setLayoutParams(params);
				}
			}
			return convertView;
		}

		class Holder {
			ImageView iv_icon;
			TextView textView;
		}
	}

	class BluetoothReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// 搜索可适配的蓝牙对象
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				// 可以从收到的Intent对象当中，将 代表远程蓝牙适配器的对象取出
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					if (!canPairing.contains(device)) {
						canPairing.add(device);
					}
					canAdapter.notifyDataSetChanged();
				}
			} else if (intent.getAction().equals(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) { // 搜索完成
				btnSearch.setText("搜索附近设备");
				btnSearch.setTextColor(0xff000000);
				lvCan.setEnabled(true);
				isSearch = false;
			} else if (intent.getAction().equals(
					BluetoothDevice.ACTION_BOND_STATE_CHANGED)) { // 配对状态改变
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				String name = device.getName();
				if (null == name) { // 如果没有名字，则显示地址
					name = device.getAddress();
				}
				switch (device.getBondState()) {
				case BluetoothDevice.BOND_BONDING:
					MyToast.makeTextAnim(getApplicationContext(), "正在配对...").show();
					break;
				case BluetoothDevice.BOND_BONDED:
					MyToast.makeTextAnim(getApplicationContext(), "完成配对").show();
					hasPairing.add(device);
					llPairing.setVisibility(View.VISIBLE);
					canPairing.remove(device);
					if (canPairing.size() == 0) {
						btnSearch
								.setBackgroundResource(R.drawable.selector_textview_bg);
					} else {
						btnSearch
								.setBackgroundResource(R.drawable.selector_wifi_list_down);
					}
					hasAdapter.notifyDataSetChanged();
					canAdapter.notifyDataSetChanged();
					break;
				case BluetoothDevice.BOND_NONE:
					MyToast.makeTextAnim(getApplicationContext(), "取消配对").show();
				}
			}

		}
	}
	
	
	private BluetoothDevice device;

    BluetoothProfile.ServiceListener mBluetoothHeadsetServiceListener = new BluetoothProfile.ServiceListener() {
        private BluetoothHeadset bh;
		private BluetoothA2dp a2dp;

		@Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
			try {
                if (profile == BluetoothProfile.HEADSET) {
                    bh = (BluetoothHeadset) proxy;
                    if (bh.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED){
                        bh.getClass().getMethod("connect", BluetoothDevice.class).invoke(bh, device);
                    }
                } else if (profile == BluetoothProfile.A2DP) {
                    a2dp = (BluetoothA2dp) proxy;

                    if (a2dp.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED){
                        a2dp.getClass()
                                .getMethod("connect", BluetoothDevice.class)
                                .invoke(a2dp, device);
                    }
                }
//                	tvBlueToothState.setText("已连接");
//                	connected=true;
//                	hasAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
        }
    };
    
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		AudioManager mAudioManager=(AudioManager) getSystemService(Service.AUDIO_SERVICE);
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) // 音量增大键响应撤销
        {
            //如果收音机打开状态,音量范围从0~15
            int volume;
            if(mAudioManager.getMode()==5){
                volume=mAudioManager.getStreamVolume(10)+1;
                mAudioManager.setStreamVolume(10, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }else{
                //媒体音量，范围0~15
                volume=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)+1;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
            if(volume>=0&&volume<16){
                Intent mainIntent=new Intent("com.zuiniukeji.VOLUME_CHANGED");
                mainIntent.putExtra("volume", volume);
                this.sendBroadcast(mainIntent);
            }
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)// 音量减小键响应重做
        {
            int volume;
            if(mAudioManager.getMode()==5){
                volume=mAudioManager.getStreamVolume(10)-1;
                mAudioManager.setStreamVolume(10, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }else{
                //媒体音量，范围0~15
                volume=mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)-1;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            }
            if(volume>=0&&volume<16){
                Intent mainIntent=new Intent("com.zuiniukeji.VOLUME_CHANGED");
                mainIntent.putExtra("volume", volume);
                this.sendBroadcast(mainIntent);
            }
            return true;
        }
		return super.onKeyDown(keyCode, event);
	}
	

}
