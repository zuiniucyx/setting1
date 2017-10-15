package com.zuiniukeji.android.setting;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener{

	private Button btnLocationService,btnSound,btnDisplay,btnDate,btnBlueTooth;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		btnLocationService=(Button) findViewById(R.id.btn_locationservice);
		btnSound=(Button) findViewById(R.id.btn_sound);
		btnDisplay=(Button) findViewById(R.id.btn_display);
		btnDate=(Button) findViewById(R.id.btn_date);
		btnBlueTooth=(Button) findViewById(R.id.btn_bluetooth);
		
		btnLocationService.setOnClickListener(this);
		btnSound.setOnClickListener(this);
		btnDisplay.setOnClickListener(this);
		btnDate.setOnClickListener(this);
		btnBlueTooth.setOnClickListener(this);
		
		findViewById(R.id.btn_wifi).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_locationservice:
			Intent mIntent=new Intent(this,LocationSerciceSetting.class);
			startActivity(mIntent);
			break;
		case R.id.btn_sound:
			Intent intent1=new Intent(this,SoundSetting.class);
			startActivity(intent1);
			break;
		case R.id.btn_display:
			Intent intent2=new Intent(this,DisplaySetting.class);
			startActivity(intent2);
			break;
		case R.id.btn_date:
			Intent intent3=new Intent(this,DateTimeSetting.class);
			startActivity(intent3);
			break;
		case R.id.btn_bluetooth:
			Intent intent4=new Intent(this,BlueToothSetting.class);
			startActivity(intent4);
			break;
		case R.id.btn_wifi:
			Intent intent5=new Intent(this,WifiSetting.class);
			startActivity(intent5);
			break;
		}
	}


}
