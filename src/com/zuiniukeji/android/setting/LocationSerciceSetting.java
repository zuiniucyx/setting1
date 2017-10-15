package com.zuiniukeji.android.setting;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.view.KeyEvent;

import com.zuiniukeji.android.setting.widget.SlideToggle;
import com.zuiniukeji.android.setting.widget.SlideToggle.OnChangeListener;

public class LocationSerciceSetting extends Activity {
	private LocationManager locationManager;
	private SlideToggle locationToggle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_sercice_setting);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		init();
		
		locationToggle.setOnChangeListener(new OnChangeListener() {
			@Override
			public void onChange(SlideToggle sb, boolean state) {
				Secure.setLocationProviderEnabled(getApplicationContext().getContentResolver(),LocationManager.GPS_PROVIDER, state);
			}
		});
	}
	
	private void init(){
		locationToggle=(SlideToggle) findViewById(R.id.toggle);
	}
	
	@Override
	protected void onStart() {
		locationToggle.mSwitchOn=locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); 
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		finish();
		super.onStop();
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
}
