package org.zywx.wbpalmstar.plugin.ueximagetag.utils;

import android.content.Context;

/**
 * dp和px转换工具类
 * <p>
 * Created by waka on 2016/3/2.
 */
public class DensityUtil {

	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {

		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {

		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
