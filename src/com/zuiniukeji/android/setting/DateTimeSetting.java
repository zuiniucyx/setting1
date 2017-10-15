package com.zuiniukeji.android.setting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.zuiniukeji.android.setting.widget.PickerView;
import com.zuiniukeji.android.setting.widget.PickerView.onSelectListener;
import com.zuiniukeji.android.setting.widget.SlideToggle;
import com.zuiniukeji.android.setting.widget.SlideToggle.OnChangeListener;

public class DateTimeSetting extends Activity implements OnClickListener{
	private SlideToggle slideToggle;
	private static Calendar calendar;
	private RelativeLayout rlTime,rlDate;
	private String hour1,minute1;
	private String year1,month1,day1; 
	private String[] month_30=new String[]{"04","06","09","11"};
	private String[] month_31=new String[]{"01","03","05","07","08","10","12",};
	private PickerView day_pv;
	private List<String> yearList,monthList;
	private List<String> dayList;
	private TextView tvMyTime,tvMydate;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_date_time_setting);
		
		init();
		
		slideToggle.setOnChangeListener(new OnChangeListener() {
			@Override
			public void onChange(SlideToggle sb, boolean state) {
				if (state) {
					Settings.Global.putInt(getContentResolver(),Settings.Global.AUTO_TIME, 1);
					tvMyTime.setText(getFormatTime("HH:mm"));
					tvMydate.setText(getFormatTime("yyyy-MM-dd"));
					openAutoTime();
				}else {
					Settings.Global.putInt(getContentResolver(), Settings.Global.AUTO_TIME, 0);
					closeAutoTime();
				}
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		tvMyTime.setText(getFormatTime("HH:mm"));
		tvMydate.setText(getFormatTime("yyyy-MM-dd"));
		try {
			if(Settings.System.getInt(getContentResolver(), Settings.Global.AUTO_TIME)==1){
				slideToggle.mSwitchOn=true;
				openAutoTime();
			}
		} catch (SettingNotFoundException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void onStop() {
		finish();
		super.onStop();
	}

	void openAutoTime(){
		rlTime.setEnabled(false);
		rlDate.setEnabled(false);
		rlTime.setAlpha(0.5f);
		rlDate.setAlpha(0.5f);
	}
	
	void closeAutoTime(){
		rlTime.setEnabled(true);
		rlDate.setEnabled(true);
		rlTime.setAlpha(1);
		rlDate.setAlpha(1);
	}
	
	private void init(){
		rlTime=(RelativeLayout) findViewById(R.id.rl_time);
		rlTime.setOnClickListener(this);
		rlDate=(RelativeLayout) findViewById(R.id.rl_date);
		tvMyTime=(TextView) findViewById(R.id.tv_mytime);
		tvMydate=(TextView) findViewById(R.id.tv_mydate);
		rlDate.setOnClickListener(this);
		calendar=Calendar.getInstance();
		slideToggle=(SlideToggle) findViewById(R.id.date_toggle);
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_time:
			timeDialog();
			break;
		case R.id.rl_date:
			dateDialog();
			break;
		}
	}
	
	
	
	//修改日期
	private void setDate(int year, int month, int day) {
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month-1);
		calendar.set(Calendar.DAY_OF_MONTH, day);
        long when = calendar.getTimeInMillis();
        AlarmManager am = (AlarmManager)getApplicationContext()
            	.getSystemService(Context.ALARM_SERVICE);
            	am.setTime(when);
    }

	//修改时间
	private void setTime(int hourOfDay, int minute) {
    	 calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    	 calendar.set(Calendar.MINUTE, minute);
    	 calendar.set(Calendar.SECOND, 0);
    	 calendar.set(Calendar.MILLISECOND, 0);
        long when = calendar.getTimeInMillis();

        AlarmManager am = (AlarmManager)getApplicationContext()
            	.getSystemService(Context.ALARM_SERVICE);
            	am.setTime(when);
    }
	
	private void timeDialog(){
		 PickerView hour_pv=null;
		 PickerView minute_pv=null;
		 Button btnCancel,btnSure;
		 Calendar c;
		 View view=LayoutInflater.from(this).inflate(R.layout.activity_time_pick_dialog, null);
		 	hour_pv = (PickerView) view.findViewById(R.id.minute_pv);
			minute_pv = (PickerView) view.findViewById(R.id.second_pv);
			btnCancel=(Button) view.findViewById(R.id.btn_cancel);
			btnSure=(Button) view.findViewById(R.id.btn_sure);
			List<String> hourList = new ArrayList<String>();
			List<String> minuteList = new ArrayList<String>();
			final Dialog dialog=new AlertDialog.Builder(this).create();
			dialog.show();
			for (int i = 0; i < 24; i++)
			{
				hourList.add(i < 10 ? "0" + i : "" + i);
			}
			for (int i = 0; i < 60; i++)
			{
				minuteList.add(i < 10 ? "0" + i : "" + i);
			}
			
			if (hourList!=null) {
				hour_pv.setData(hourList);
			}
			hour_pv.setOnSelectListener(new onSelectListener() 
			{ 
				@Override
				public void onSelect(String text)
				{
					hour1=text;
				}
			});
			
			if (minuteList!=null) {
				minute_pv.setData(minuteList);
			}
			minute_pv.setOnSelectListener(new onSelectListener()
			{
				@Override
				public void onSelect(String text)
				{
					minute1=text;
				}
			});
			btnSure.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
//					setTime(getDate("yyyyMMdd")+"."+hour1+minute1+"00");
					setTime(Integer.parseInt(hour1), Integer.parseInt(minute1));
					tvMyTime.setText(hour1+":"+minute1);
					if (dialog!=null) {
						dialog.cancel();
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
			//初始化时间
			c=Calendar.getInstance();
					hour1=c.get(Calendar.HOUR_OF_DAY)<10? "0"+c.get(Calendar.HOUR_OF_DAY):c.get(Calendar.HOUR_OF_DAY)+"";
					minute1=c.get(Calendar.MINUTE)<10? "0"+c.get(Calendar.MINUTE):c.get(Calendar.MINUTE)+"";
			hour_pv.setSelected(hour1);
			minute_pv.setSelected(minute1);
			dialog.getWindow().setContentView(view);
			dialog.getWindow().setLayout(554, 355);
	}
	private void dateDialog(){
		 PickerView year_pv=null;
		 PickerView month_pv=null;
		 Button btnCancel,btnSure;
		 
		 Calendar c;
		View view=LayoutInflater.from(this).inflate(R.layout.activity_my_date_pick_dialog, null);
		year_pv = (PickerView) view.findViewById(R.id.year_pv);
		month_pv = (PickerView) view.findViewById(R.id.month_pv);
		day_pv = (PickerView) view.findViewById(R.id.day_pv);
		btnCancel=(Button) view.findViewById(R.id.btn_cancel);
		btnSure=(Button) view.findViewById(R.id.btn_sure);
		
		final Dialog dialog=new AlertDialog.Builder(this).create();
		dialog.show();
		yearList= new ArrayList<String>();
		monthList = new ArrayList<String>();
		dayList = new ArrayList<String>();
		c=Calendar.getInstance();
		year1=c.get(Calendar.YEAR)+"";
		int intMonth=c.get(Calendar.MONTH)+1;
		month1=intMonth<10? "0"+intMonth : intMonth+"";
//		Log.e("22222", month1);
		int intDay=c.get(Calendar.DAY_OF_MONTH);
		day1=intDay<10? "0"+intDay : intDay+"";
		
		for (int i = 1970; i < 2037; i++)
		{
			yearList.add("" + i);
		}
		for (int i = 1; i <= 12; i++)
		{
			monthList.add(i < 10 ? "0" + i : "" + i);
		}
		
		for (int i = 1; i <= initDays(year1,month1); i++)
		{
			dayList.add(i < 10 ? "0" + i : "" + i);
		}
		
		if (yearList!=null) {
			year_pv.setData(yearList);
		}
		if (monthList!=null) {
			
			month_pv.setData(monthList);
		}
		if (dayList!=null) {
			day_pv.setData(dayList);
		}
		year_pv.setOnSelectListener(new onSelectListener() 
		{
			@Override
			public void onSelect(String text)
			{
				year1=text;
				dayList.clear();
				for (int i = 1; i <= initDays(year1,month1); i++)
				{
					dayList.add(i < 10 ? "0" + i : "" + i);
				}
				day_pv.setSelected(day1);
			}
		});
		
		month_pv.setOnSelectListener(new onSelectListener()
		{
			@Override
			public void onSelect(String text)
			{
				month1=text;
				dayList.clear();
				for (int i = 1; i <= initDays(year1,month1); i++)
				{
					dayList.add(i < 10 ? "0" + i : "" + i);
				}
				day_pv.setSelected(day1);
			}
		});
		
		day_pv.setOnSelectListener(new onSelectListener() {
			@Override
			public void onSelect(String text) {
				day1=text;
			}
		});
		
		btnSure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setDate(Integer.parseInt(year1), Integer.parseInt(month1), Integer.parseInt(day1));
				tvMydate.setText(year1+"-"+month1+"-"+day1);
//				setTime(year1+month1+day1+"."+getDate("HHmmss"));
				if (dialog!=null) {
					dialog.cancel();
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
		
		year_pv.setSelected(c.get(Calendar.YEAR)-1970);
		month_pv.setSelected(month1);
		day_pv.setSelected(day1);
		
		dialog.getWindow().setContentView(view);
		dialog.getWindow().setLayout(554, 355);
	}
	
	//计算每个月的天数
	private int initDays(String year,String month){
		int days=31;
		for (int i = 0; i < month_30.length; i++) {
			if (month.equals(month_30[i])) {
				days=30;
			}
		}
		for (int i = 0; i < month_31.length; i++) {
			if (month.equals(month_31[i])) {
				days=31;
			}
		}
		if (month.equals("2")||month.equals("02")) {
			if(Integer.parseInt(year)%4==0&&Integer.parseInt(year)%100!=0||Integer.parseInt(year)%400==0){
				days=29;
			}else {
				days=28;
			}
		}
		return days;
	}
	
	
	private String getFormatTime(String type){
		SimpleDateFormat format=new SimpleDateFormat(type);
		Date date=new Date();
		String time=format.format(date);
		return time;
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
