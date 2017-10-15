package com.zuiniukeji.android.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class DisplaySetting extends Activity implements OnClickListener{
	private TextView tvSleepTime;
//	private float textSize=1.0f;
//	private String currentSize;
	private String textText;
//	private String textText1;
//	private SharedPreferences sharedPreferences;
//	private Editor editor;
	private String[] sleepTime=new String[]{
			"一分钟","五分钟","永不",
	};  
	private int checkedIndex=0;  //选择的屏幕休眠时间
//	private int checkedtext=0;  //选择的字体
	private int[] sleep=new int[]{60*1000,60*5*1000,Integer.MAX_VALUE}; //屏幕休眠时间 毫秒
//	private RelativeLayout llFontSize;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_display_setting);
		
		init();
	}
	
	private int getScreenOffTime(){
		int screenOffTime = 0;
		try {
		screenOffTime = Settings.System.getInt(getContentResolver(),
		Settings.System.SCREEN_OFF_TIMEOUT);
		} catch (Exception localException) {
		}
		return screenOffTime;
	}
	
	private void init(){
		tvSleepTime=(TextView) findViewById(R.id.tv_sleeptime);
//		tvFontSize=(TextView) findViewById(R.id.tv_fontsize);
//		llFontSize=(RelativeLayout) findViewById(R.id.ll_fontsize);
		tvSleepTime.setOnClickListener(this);
//		llFontSize.setOnClickListener(this);
		findViewById(R.id.rl_locktime).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_locktime:
			showDialog();
			break;
//		case R.id.ll_fontsize:
//			fontSizeDialog();
//			break;
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
//		sharedPreferences=getSharedPreferences("soundstate", Context.MODE_PRIVATE); 
//		editor=sharedPreferences.edit();
		for (int i = 0; i < sleep.length; i++) {
			if (sleep[i]==getScreenOffTime()) {
				if (i!=2) {
					tvSleepTime.setText("无操作"+sleepTime[i]+"之后");
				}else{
					tvSleepTime.setText("永不休眠");
				}
				checkedIndex=i;
			} 
		}
//		currentSize=sharedPreferences.getString("textText", "1.0");
//		if (currentSize.equals("0.75")) {
//			textText1="小";
//			checkedtext=0;
//		}else if (currentSize.equals("1.0")) {
//			textText1="正常";
//			checkedtext=1;
//		}else {
//			textText1="大";
//			checkedtext=2;
//		}
//		tvFontSize.setText(textText1);
	}

	@Override
	protected void onStop() {
		finish();
		super.onStop();
	}

	
	//修改休眠时间
	private void showDialog(){
		View view=LayoutInflater.from(this).inflate(R.layout.alertdialog, null);
		RadioGroup radioGroup=(RadioGroup) view.findViewById(R.id.rg_textsize);
		Button btnCancel=(Button) view.findViewById(R.id.btn_cancel);
		Button btnSure=(Button) view.findViewById(R.id.btn_sure);
		((RadioButton) radioGroup.getChildAt(checkedIndex)).setChecked(true);
		final Dialog dialog=new AlertDialog.Builder(this).create();
		dialog.show();
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.tv_small:
					textText="无操作一分钟之后";
					checkedIndex=0;
					break;
				case R.id.tv_normal:
					textText="无操作五分钟之后";
					checkedIndex=1;
					break;
				case R.id.tv_big:
					textText="永不休眠";
					checkedIndex=2;
					break;
				}
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (dialog!=null) {
					dialog.cancel();
				}
			}
		});
		btnSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 tvSleepTime.setText(textText);
				 Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_OFF_TIMEOUT,sleep[checkedIndex]);
				if (dialog!=null) {
					dialog.cancel();
				}
			}
		});
		dialog.getWindow().setContentView(view);
		dialog.getWindow().setLayout(556, 356);
	}
	
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
//	//修改字体大小
//		private void fontSizeDialog(){
//			View view=LayoutInflater.from(this).inflate(R.layout.fontsize_dialog, null);
//			RadioGroup radioGroup=(RadioGroup) view.findViewById(R.id.rg_textsize);
//			Button btnCancel=(Button) view.findViewById(R.id.btn_cancel);
//			Button btnSure=(Button) view.findViewById(R.id.btn_sure);
//			final TextView tvName=(TextView) view.findViewById(R.id.tv_name);
//			
//			((RadioButton) radioGroup.getChildAt(checkedtext)).setChecked(true);
//			switch (checkedtext) {
//			case 0:
//				tvName.setTextSize(18);
//				break;
//			case 1:
//				tvName.setTextSize(24);
//				break;
//			case 2:
//				tvName.setTextSize(30);
//				break;
//			}
//			
//			final Dialog dialog=new AlertDialog.Builder(this).create();
//			dialog.show();
//			radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//				@Override
//				public void onCheckedChanged(RadioGroup group, int checkedId) {
//					switch (checkedId) {
//					case R.id.tv_small:
//						textSize=0.75f;
//						textText1="小";
//						tvName.setTextSize(18);
//						break;
//					case R.id.tv_normal:
//						textSize=1.0f;
//						textText1="正常";
//						tvName.setTextSize(24);
//						break;
//					case R.id.tv_big:
//						textSize=1.25f;
//						textText1="大";
//						tvName.setTextSize(30);
//						break;
//					}
////					mconfig.fontScale =textSize;
//				}
//			});
//			btnCancel.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					if (dialog!=null) {
//						dialog.cancel();
//					}
//				}
//			});
//			btnSure.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					 tvFontSize.setText(textText1);
//					 if (textText1.equals("小")) {
//						checkedtext=0;
//					}else if (textText1.equals("正常")) {
//						checkedtext=1;
//					}else {
//						checkedtext=2;
//					}
//					Settings.System.putFloat(getBaseContext().getContentResolver(),
//				            Settings.System.FONT_SCALE, textSize);
//					editor.putString("textText",textSize+"");
//					editor.commit();
//					if (dialog!=null) {
//						dialog.cancel();
//					}
//				}
//			});
//			dialog.getWindow().setContentView(view);
//			dialog.getWindow().setLayout(556, 394);
//		}
}
