package org.zywx.wbpalmstar.plugin.ueximagetag.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapUtil {

	/**
	 * 得到Bitmap
	 * 
	 * @param filePath
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static Bitmap getBitmap(String filePath, int reqWidth, int reqHeight) {

		MLog.getIns().i("filePath = " + filePath);
		MLog.getIns().i("reqWidth = " + reqWidth);
		MLog.getIns().i("reqHeight = " + reqHeight);

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;// 不返回实际的bitmap，也不给其分配内存空间,但是允许我们查询图片的信息这其中就包括图片大小信息

		try {

			// 从文件中生成一个不占内存的bitmap,获取图片宽高信息
			BitmapFactory.decodeStream(new FileInputStream(filePath), null, options);

			// 根据压缩图片目标宽高计算压缩比
			int inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
			MLog.getIns().i("inSampleSize = " + inSampleSize);

			options.inSampleSize = inSampleSize;
			options.inPurgeable = true;// 为True的话表示使用BitmapFactory创建的Bitmap用于存储Pixel的内存空间在系统内存不足时可以被回收;在Android5.0已过时;http://blog.sina.com.cn/s/blog_7607703f0101fzl7.html
			options.inInputShareable = true;// inInputShareable与inPurgeable一起使用，如果inPurgeable为false那该设置将被忽略，如果为true，那么它可以决定位图是否能够共享一个指向数据源的引用，或者是进行一份拷贝;在Android5.0已过时;http://blog.csdn.net/xu_fu/article/details/7340454
			options.inJustDecodeBounds = false;// decode得到的bitmap将写入内存

			Bitmap bitmap = BitmapFactory.decodeStream(new FileInputStream(filePath), null, options);
			return bitmap;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MLog.getIns().e(e);
			return null;
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			MLog.getIns().e(e);
			// 缩小一倍目标宽高，再次进行获取
			return getBitmap(filePath, reqWidth / 2, reqHeight / 2);
		}
	}

	/**
	 * 计算SampleSize
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;

		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// 计算出实际宽高和目标宽高的比率,Math.round()四舍五入
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	/**
	 * 旋转Bitmap
	 * 
	 * Rotates the bitmap by the specified degree. If a new bitmap is created,
	 * the original bitmap is recycled.
	 * 
	 * @param bitmap
	 * @param degrees
	 * @return
	 */
	public static Bitmap rotate(Bitmap bitmap, int degree) {
		return rotateAndMirror(bitmap, degree, false);
	}

	/**
	 * 旋转和镜像Bitmap
	 * 
	 * Rotates and/or mirrors the bitmap. If a new bitmap is created, the
	 * original bitmap is recycled.
	 * 
	 * @param bitmap
	 * @param degree
	 * @param isMirror
	 * @return
	 * @throws OutOfMemoryError
	 */
	public static Bitmap rotateAndMirror(Bitmap bitmap, int degree, boolean isMirror) throws OutOfMemoryError {

		if ((degree != 0 || isMirror) && bitmap != null) {
			Matrix m = new Matrix();
			m.setRotate(degree, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);

			if (isMirror) {
				m.postScale(-1, 1);
				degree = (degree + 360) % 360;
				if (degree == 0 || degree == 180) {
					m.postTranslate((float) bitmap.getWidth(), 0);
				} else if (degree == 90 || degree == 270) {
					m.postTranslate((float) bitmap.getHeight(), 0);
				} else {
					throw new IllegalArgumentException("Invalid degrees=" + degree);
				}
			}

			Bitmap bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
			if (bitmap != bitmap2) {
				bitmap.recycle();
				System.gc();
				bitmap = bitmap2;
			}
		}
		return bitmap;
	}

}
