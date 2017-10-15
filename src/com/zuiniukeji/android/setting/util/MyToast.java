package com.zuiniukeji.android.setting.util;

import java.lang.reflect.Field;

import com.zuiniukeji.android.setting.R;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 自定义动画的Toast
 * @author ziyao
 *
 */
public class MyToast extends Toast {

	public MyToast(Context context) {
		super(context);
	}

	/**
	 * 调用有动画的Toast
	 * @param context
	 * @param text
	 * @param duration
	 * @param 自定义的动画id
	 * @return
	 */
	public static Toast makeTextAnim(Context context, CharSequence text) {
		 View layout = LayoutInflater.from(context).inflate(R.layout.my_toast,null);  
		 TextView tv_toast = (TextView) layout.findViewById(R.id.tv_toast); 
		 tv_toast.setText(text);
		Toast toast = new Toast(context);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);  
		try {
			Object mTN = null;
			mTN = getField(toast, "mTN");
			if (mTN != null) {
				Object mParams = getField(mTN, "mParams");
				if (mParams != null
						&& mParams instanceof WindowManager.LayoutParams) {
					WindowManager.LayoutParams params = (WindowManager.LayoutParams) mParams;
					params.windowAnimations = R.style.Lite_Animation_Toast;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return toast;
	}

	/**
	 * 反射字段
	 * @param object 要反射的对象
	 * @param fieldName 要反射的字段名称
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 */
	private static Object getField(Object object, String fieldName)
			throws NoSuchFieldException, IllegalAccessException {
		Field field = object.getClass().getDeclaredField(fieldName);
		if (field != null) {
			field.setAccessible(true);
			return field.get(object);
		}
		return null;
	}

}
