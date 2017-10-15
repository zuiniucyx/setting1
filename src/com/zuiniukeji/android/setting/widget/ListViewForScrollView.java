package com.zuiniukeji.android.setting.widget;

//自定义可适应ScrollView的ListView
//自定义一个类继承自ListView，通过重写其onMeasure方法，达到对ScrollView适配的效果。
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
public class ListViewForScrollView extends ListView {
    public ListViewForScrollView(Context context) {
        super(context);
    }
    public ListViewForScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public ListViewForScrollView(Context context, AttributeSet attrs,
        int defStyle) {
        super(context, attrs, defStyle);
    }
    @Override
    /**
     * 重写该方法，达到使ListView适应ScrollView的效果
     */
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
        MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec-getChildCount());
    }
}
//    三个构造方法完全不用动，只要重写onMeasure方法
//    在xml布局中和Activty中使用的ListView改成这个自定义ListView就行了。
//    这个方法有一个同样的毛病，就是默认显示的首项是ListView，需要手动把ScrollView滚动至最顶端:
//	解决方案一：
//	sv = (ScrollView) findViewById(R.id.act_solution_4_sv);
//	sv.smoothScrollTo(0, 0);
//	解决方案二：
//	若scrollView里面ListView的上面还有其他的控件，那么使得一开始的时候就让上面其中一个控件获得焦点，滚动条自然就到顶部去了
//	在xml文件
//	android:focusable="true"
//    android:focusableInTouchMode="true"
//	或java文件中设置都行
//	rl_ot_sum_two_all.setFocusableInTouchMode(true);
//	rl_ot_sum_two_all.requestFocus();
//	rl_ot_sum_two_all.setFocusable(true);
