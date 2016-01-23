package org.zywx.wbpalmstar.plugin.ueximagetag;

import java.io.IOException;
import java.io.InputStream;

import org.zywx.wbpalmstar.base.BUtility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class Utils {
	public static int compressRate = 3;// 压缩比例，初始为3

	/**
	 * 读取图片
	 * 
	 * @param ctx
	 * @param imgUrl
	 * @return
	 */
	public static Bitmap getLocalImg(Context ctx, String imgUrl) {
		Log.i("uexImageTag", "getLocalImg imgUrl---->" + imgUrl);
		if (imgUrl == null || imgUrl.length() == 0) {
			return null;
		}
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			if (imgUrl.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
				is = BUtility.getInputStreamByResPath(ctx, imgUrl);
				bitmap = BitmapFactory.decodeStream(is);
			} else if (imgUrl.startsWith(BUtility.F_FILE_SCHEMA)) {
				imgUrl = imgUrl.replace(BUtility.F_FILE_SCHEMA, "");
				bitmap = BitmapFactory.decodeFile(imgUrl);
			} else if (imgUrl.startsWith(BUtility.F_Widget_RES_path)) {
				try {
					is = ctx.getAssets().open(imgUrl);
					if (is != null) {
						bitmap = BitmapFactory.decodeStream(is);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (imgUrl.startsWith("/")) {
				bitmap = BitmapFactory.decodeFile(imgUrl);
			}
		} catch (OutOfMemoryError e) {
			// 如果报了oom错误，增大压缩比例，调用压缩方法
			Log.i("uexImageTag", "OutOfMemoryError " + compressRate);
			compressRate = 10;
			bitmap = compressImage(ctx, imgUrl);
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			compressRate = 1;// 回归正常压缩比
		}
		return bitmap;
	}

	/**
	 * 读取图片，如果发生OOM错误，压缩图片
	 * 
	 * @param ctx
	 * @param imgUrl
	 * @return
	 */
	public static Bitmap compressImage(Context ctx, String imgUrl) {
		Log.i("uexImageTag", "compressImage imgUrl---->" + imgUrl);
		if (imgUrl == null || imgUrl.length() == 0) {
			return null;
		}
		Bitmap bitmap = null;
		InputStream is = null;
		try {
			if (imgUrl.startsWith(BUtility.F_Widget_RES_SCHEMA)) {
				is = BUtility.getInputStreamByResPath(ctx, imgUrl);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = compressRate;
				options.inPreferredConfig = Bitmap.Config.ARGB_4444;
				options.inPurgeable = true;
				options.inInputShareable = true;
				options.inTempStorage = new byte[64 * 1024];
				bitmap = BitmapFactory.decodeStream(is, null, options);
			} else if (imgUrl.startsWith(BUtility.F_FILE_SCHEMA)) {
				imgUrl = imgUrl.replace(BUtility.F_FILE_SCHEMA, "");
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = compressRate;
				options.inPreferredConfig = Bitmap.Config.ARGB_4444;
				options.inPurgeable = true;
				options.inInputShareable = true;
				options.inTempStorage = new byte[64 * 1024];
				bitmap = BitmapFactory.decodeFile(imgUrl, options);
			} else if (imgUrl.startsWith(BUtility.F_Widget_RES_path)) {
				try {
					is = ctx.getAssets().open(imgUrl);
					if (is != null) {
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inSampleSize = compressRate;
						options.inPreferredConfig = Bitmap.Config.ARGB_4444;
						options.inPurgeable = true;
						options.inInputShareable = true;
						options.inTempStorage = new byte[64 * 1024];
						bitmap = BitmapFactory.decodeStream(is, null, options);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if (imgUrl.startsWith("/")) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = compressRate;
				options.inPreferredConfig = Bitmap.Config.ARGB_4444;
				options.inPurgeable = true;
				options.inInputShareable = true;
				options.inTempStorage = new byte[64 * 1024];
				bitmap = BitmapFactory.decodeFile(imgUrl, options);
			}
		} catch (OutOfMemoryError e) {
			// 递归调用，如果报了oom错误，增大压缩比例，调用压缩方法
			Log.i("uexImageTag", "OutOfMemoryError " + compressRate);
			compressRate++;
			bitmap = compressImage(ctx, imgUrl);
			e.printStackTrace();
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			compressRate = 3;// 回归正常压缩比
		}
		return bitmap;
	}

}
