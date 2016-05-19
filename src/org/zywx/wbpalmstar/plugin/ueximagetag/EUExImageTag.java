package org.zywx.wbpalmstar.plugin.ueximagetag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.ueximagetag.utils.BitmapUtil;
import org.zywx.wbpalmstar.plugin.ueximagetag.utils.FileUtil;
import org.zywx.wbpalmstar.plugin.ueximagetag.utils.FormatAmendUtil;
import org.zywx.wbpalmstar.plugin.ueximagetag.utils.MLog;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

/**
 * 入口类
 * 
 * @author xiaolong.fan,waka
 *
 */
public class EUExImageTag extends EUExBase {

	public static final String TAGALL = "uexImageTag";
	private static final String TAG = "EUExImageTag";

	// 回调
	private static final String FUNC_ON_LONG_CLICK_TAG_CALLBACK = "uexImageTag.cbOnLongClickTag";// 长按标签回调
	private static final String FUNC_ON_LONG_CLICK_IMAGE_CALLBACK = "uexImageTag.cbOnLongClickImage";// 长按图片回调
	private static final String FUNC_ON_CHANGE_CALLBACK = "uexImageTag.cbOnChange";
	private static final String FUNC_ON_CLICK_TAG_CALLBACK = "uexImageTag.cbOnClickTag";// 点击标签回调
	private static final String FUNC_ON_CLICK_IMAGE_CALLBACK = "uexImageTag.cbOnClickImage";// 长按图片回调
	private static final String FUNC_DELETE_TAG_CALLBACK = "uexImageTag.cbDeleteTag";
	private static final String FUNC_GET_ALL_TAGS_CALLBACK = "uexImageTag.cbGetAllTags";
	private static final String FUNC_SET_IS_MOVEABLE_CALLBACK = "uexImageTag.cbSetIsMoveable";// 设置标签可移动标志回调
	private static final String FUNC_ERROR_CALLBACK = "uexImageTag.cbError";

	// 提示文本
	@SuppressWarnings("unused")
	private static final String FORMAT_ERROR_TIPS = EUExUtil.getString("plugin_uex_image_tag_format_error");// 数据格式错误
	private static final String XY_FORMAT_ERROR_TIPS = EUExUtil.getString("plugin_uex_image_tag_xy_format_error");// xy格式错误
	private static final String COLOR_FORMAT_ERROR_TIPS = EUExUtil.getString("plugin_uex_image_tag_color_format_error");// color格式错误
	private static final String ID_NOT_EXIST_ERROR_TIPS = EUExUtil.getString("plugin_uex_image_tag_id_not_exist_error");// id不存在，不能删除

	// 图片参数
	public static int x; // view 距左边宽度
	public static int y;// view 距顶部宽度
	public int inWidth;// view宽度
	public int inHeight;// view图高度
	private String imgPath;// 图片路径
	private String jsonTagIn;// 传入的JSON，记录了图片上已有的标签信息

	private PictureTagLayout mPictureTagLayout;
	public int mType = 1;// 标签类型
	public static final int TYPE_TAG = 1;// 标签
	public static final int TYPE_POINT = 2;// 点
	public Map<String, PictureTagView> mapTagViews;// 静态Map，用来存储标签
	private List<String> deleteList;// 删除的id名列表

	/**
	 * 构造方法
	 * 
	 * @param mContext
	 * @param arg1
	 */
	@SuppressLint("UseSparseArrays")
	public EUExImageTag(Context mContext, EBrowserView arg1) {
		super(mContext, arg1);
		EUExUtil.init(mContext);
		mapTagViews = new HashMap<String, PictureTagView>();// 初始化mapTagViews
		deleteList = new ArrayList<String>();
	}

	/**
	 * 拦截Pause方法
	 * 
	 * @param context
	 */
	public static void onActivityPause(Context context) {

	}

	/**
	 * 拦截Resume方法
	 * 
	 * @param context
	 */
	public static void onActivityResume(Context context) {

	}

	/**
	 * 打开图片
	 * 
	 * @param parm
	 */
	@SuppressWarnings("deprecation")
	public void openImage(final String[] parm) {
		Log.i("uexImageTag", "openImage start");
		// 传入的参数不能少于5个，第6个标签信息选填
		if (parm.length < 5) {
			Log.i("uexImageTag", "parm.length<5");
			return;
		}
		try {
			x = (int) Float.parseFloat(parm[0]);
			y = (int) Float.parseFloat(parm[1]);
			inWidth = (int) Float.parseFloat(parm[2]);
			inHeight = (int) Float.parseFloat(parm[3]);
			Log.i("uexImageTag", "x---->" + x + " , y---->" + y + " , inWidth---->" + inWidth + " , inHeight---->" + inHeight);
			imgPath = parm[4];
			Log.i("uexImageTag", "imgPath---->" + imgPath);
		} catch (NumberFormatException e) {
			Log.i("uexImageTag", "NumberFormatException");
			e.printStackTrace();
		}
		Log.i(TAG, "jsonTagIn----->" + jsonTagIn);
		if (imgPath != null && mPictureTagLayout == null) {

			// NEW 动态添加布局
			mPictureTagLayout = (PictureTagLayout) LayoutInflater.from(mContext).inflate(EUExUtil.getResLayoutID("plugin_uex_image_tag_picture_tag_layout"), null);

			String absPath = FileUtil.getAbsPath(imgPath, mBrwView);// 获得绝对路径
			imgPath = FileUtil.makeFile(mContext, mBrwView, absPath);// 获得文件位置
			if (imgPath == null || imgPath.isEmpty()) {
				MLog.getIns().e("imgPath == null || imgPath.isEmpty()");
				formatError("imgPath == null || imgPath.isEmpty()");
				removeImage(null);
				return;
			}
			Bitmap bitmap = BitmapUtil.getBitmap(imgPath, inWidth, inHeight);
			if (bitmap == null) {
				MLog.getIns().e("bitmap == null");
				formatError("bitmap == null");
				removeImage(null);
				return;
			}

			mPictureTagLayout.setmEuExImageTag(this);// 传入EuExImageTag
			mPictureTagLayout.setWidth(inWidth);// 设置layout宽度
			mPictureTagLayout.setHeight(inHeight);// 设置layout高度
			mPictureTagLayout.setBackgroundDrawable(new BitmapDrawable(bitmap));
			final RelativeLayout.LayoutParams lparam = new RelativeLayout.LayoutParams(inWidth, inHeight);
			lparam.leftMargin = x;
			lparam.topMargin = y;
			addView2CurrentWindow(mPictureTagLayout, lparam);

			// 如果传入了第6个标签信息，则解析一个添加一个
			if (parm.length == 6) {
				jsonTagIn = parm[5];
				try {
					JSONObject jsonObject = new JSONObject(jsonTagIn);
					JSONArray tags = jsonObject.getJSONArray("tag");
					for (int i = 0; i < tags.length(); i++) {
						JSONObject tag = tags.getJSONObject(i);
						setTag(new String[] { tag.toString() });// 调用setTag方法
					}
				} catch (JSONException e) {
					formatError("JSONException");
					MLog.getIns().e(e);
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 移除图片
	 * 
	 * @param parm
	 */
	public void removeImage(String[] parm) {
		if (mPictureTagLayout != null) {
			mapTagViews.clear();
			removeViewFromCurrentWindow(mPictureTagLayout);
			mPictureTagLayout = null;
		}
	}

	/**
	 * 长按标签回调
	 */
	public void onLongClickTag(String id, float x, float y, String title, float textSize, String textColor, String message, int width, int height, String imgUrl) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", id);
			jsonObject.put("x", x);
			jsonObject.put("y", y);
			jsonObject.put("title", title);
			jsonObject.put("textSize", textSize);
			jsonObject.put("textColor", textColor);
			jsonObject.put("message", message);
			jsonObject.put("width", width);
			jsonObject.put("height", height);
			jsonObject.put("imgUrl", imgUrl);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_LONG_CLICK_TAG_CALLBACK + "){" + FUNC_ON_LONG_CLICK_TAG_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString()
				+ SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 长按图片回调
	 */
	public void onLongClickImage(float x, float y) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("x", x);
			jsonObject.put("y", y);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_LONG_CLICK_IMAGE_CALLBACK + "){" + FUNC_ON_LONG_CLICK_IMAGE_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString()
				+ SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 数据变更回调
	 */
	public void onChangeToFront(String id, float x, float y, String title, float textSize, String textColor, String message) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", id);
			jsonObject.put("x", x);
			jsonObject.put("y", y);
			jsonObject.put("title", title);
			jsonObject.put("textSize", textSize);
			jsonObject.put("textColor", textColor);
			jsonObject.put("message", message);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_CHANGE_CALLBACK + "){" + FUNC_ON_CHANGE_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 点击标签回调
	 */
	public void onClickTag(String id, float x, float y, String title, float textSize, String textColor, String message, int width, int height, String imgUrl) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", id);
			jsonObject.put("x", x);
			jsonObject.put("y", y);
			jsonObject.put("title", title);
			jsonObject.put("textSize", textSize);
			jsonObject.put("textColor", textColor);
			jsonObject.put("message", message);
			jsonObject.put("width", width);
			jsonObject.put("height", height);
			jsonObject.put("imgUrl", imgUrl);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_CLICK_TAG_CALLBACK + "){" + FUNC_ON_CLICK_TAG_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 点击图片回调
	 */
	public void onClickImage(float x, float y) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("x", x);
			jsonObject.put("y", y);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_CLICK_IMAGE_CALLBACK + "){" + FUNC_ON_CLICK_IMAGE_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 给前端发送删除标签回调
	 */
	public void deleteTagToFront(String id) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("id", id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_DELETE_TAG_CALLBACK + "){" + FUNC_DELETE_TAG_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 添加或更新TagView标签，id不存在则添加，存在则更新
	 */
	public void setTag(String[] params) {

		MLog.getIns().d("start");

		if (params.length < 1) {
			MLog.getIns().e("params.length < 1");
			return;
		}

		try {
			JSONObject jsonObject = new JSONObject(params[0]);

			// 基础属性
			String id = jsonObject.getString("id");
			float x = (float) jsonObject.optDouble("x", 0);
			float y = (float) jsonObject.optDouble("y", 0);
			MLog.getIns().i("id---->" + id + " , x---->" + x + " , y---->" + y);

			// 标签特有属性
			final String title = jsonObject.optString("title", "Tag");// 标题
			float textSize = (float) jsonObject.optDouble("textSize", 15);// 字号
			String textColor = jsonObject.optString("textColor", "#ffffffff");// 字体颜色
			final String message = jsonObject.optString("message", "");// 附加信息

			// x，y容错修正
			x = FormatAmendUtil.between0and1(x);
			y = FormatAmendUtil.between0and1(y);

			// 判断颜色格式
			if (!FormatAmendUtil.isColorStr(textColor)) {
				formatError("颜色格式错误");
				MLog.getIns().e("颜色格式错误");
				return;
			}

			// 判断字号大小
			if (textSize < 0) {
				textSize = 0;
			}

			final String idNew = id;
			final float xNew = x;
			final float yNew = y;
			final float textSizeNew = textSize;
			final String textColorNew = textColor;
			// 如果HashMap中没有这个id，添加
			if (!mapTagViews.containsKey(idNew)) {
				((Activity) mContext).runOnUiThread(new Runnable() {// 在主线程中更新UI
					@Override
					public void run() {
						try {
							mPictureTagLayout.addTagView(idNew, xNew, yNew, title, textSizeNew, textColorNew, message);// 添加标签View
						} catch (Exception e) {
							e.printStackTrace();
							MLog.getIns().e(e);
						}
					}
				});
			}
			// 否则进行更新操作(在布局上移除它的View,然后在HashMap中删除这个标签，再添加新标签，相当于更新操作)
			else {
				final View view = mapTagViews.get(idNew);// 现在HashMap中获得对应id标签的实例
				// 在主线程中更新UI
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mPictureTagLayout.removeView(view);// 移除旧标签View
						mapTagViews.remove(idNew);// 在HashMap中删除这个标签
						mPictureTagLayout.addTagView(idNew, xNew, yNew, title, textSizeNew, textColorNew, message);// 添加新标签
					}
				});
			}
		} catch (Exception e) {
			formatError(e.getMessage());
			e.printStackTrace();
			MLog.getIns().e(e);
		}
	}

	/**
	 * 设置点
	 * 
	 * @param params
	 */
	@SuppressWarnings("deprecation")
	public void setPoint(String[] params) {
		Log.i("uexImageTag", "openImage start");
		// 传入的参数不能少于5个，第6个标签信息选填
		if (params.length < 5) {
			Log.i("uexImageTag", "parm.length<5");
			return;
		}
		try {
			x = (int) Float.parseFloat(params[0]);
			y = (int) Float.parseFloat(params[1]);
			inWidth = (int) Float.parseFloat(params[2]);
			inHeight = (int) Float.parseFloat(params[3]);
			Log.i("uexImageTag", "x---->" + x + " , y---->" + y + " , inWidth---->" + inWidth + " , inHeight---->" + inHeight);
			imgPath = params[4];
			Log.i("uexImageTag", "imgPath---->" + imgPath);
		} catch (NumberFormatException e) {
			Log.i("uexImageTag", "NumberFormatException");
			e.printStackTrace();
		}
		Log.i(TAG, "jsonTagIn----->" + jsonTagIn);
		if (imgPath != null && mPictureTagLayout == null) {

			// NEW 动态添加布局
			mPictureTagLayout = (PictureTagLayout) LayoutInflater.from(mContext).inflate(EUExUtil.getResLayoutID("plugin_uex_image_tag_picture_tag_layout"), null);

			String absPath = FileUtil.getAbsPath(imgPath, mBrwView);// 获得绝对路径
			imgPath = FileUtil.makeFile(mContext, mBrwView, absPath);// 获得文件位置
			if (imgPath == null || imgPath.isEmpty()) {
				MLog.getIns().e("imgPath == null || imgPath.isEmpty()");
				formatError("imgPath == null || imgPath.isEmpty()");
				removeImage(null);
				return;
			}
			Bitmap bitmap = BitmapUtil.getBitmap(imgPath, inWidth, inHeight);
			if (bitmap == null) {
				MLog.getIns().e("bitmap == null");
				formatError("bitmap == null");
				removeImage(null);
				return;
			}

			mPictureTagLayout.setmEuExImageTag(this);// 传入EuExImageTag
			mPictureTagLayout.setWidth(inWidth);// 设置layout宽度
			mPictureTagLayout.setHeight(inHeight);// 设置layout高度
			mPictureTagLayout.setBackgroundDrawable(new BitmapDrawable(bitmap));
			final RelativeLayout.LayoutParams lparam = new RelativeLayout.LayoutParams(inWidth, inHeight);
			lparam.leftMargin = x;
			lparam.topMargin = y;
			addView2CurrentWindow(mPictureTagLayout, lparam);

			// 如果传入了第6个标签信息，则解析一个添加一个
			if (params.length == 6) {
				jsonTagIn = params[5];
				try {
					JSONObject jsonObject = new JSONObject(jsonTagIn);
					JSONArray tags = jsonObject.getJSONArray("tag");
					for (int i = 0; i < tags.length(); i++) {
						JSONObject tag = tags.getJSONObject(i);
						setPointSingle(new String[] { tag.toString() });// 调用setPointSingle方法
					}
				} catch (JSONException e) {
					formatError(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 单个设置点
	 * 
	 * @param params
	 */
	public void setPointSingle(String[] params) {

		MLog.getIns().d("start");

		if (params.length < 1) {
			MLog.getIns().e("params.length < 1");
			return;
		}

		try {
			JSONObject jsonObject = new JSONObject(params[0]);

			// 基础属性
			String id = jsonObject.getString("id");
			float x = (float) jsonObject.optDouble("x", 0);
			float y = (float) jsonObject.optDouble("y", 0);
			MLog.getIns().i("id---->" + id + " , x---->" + x + " , y---->" + y);

			// 点特有属性
			String imgUrl = jsonObject.optString("imgUrl", "");// 图片路径
			if (!imgUrl.isEmpty()) {
				String absPath = FileUtil.getAbsPath(imgUrl, mBrwView);// 获得绝对路径
				imgUrl = FileUtil.makeFile(mContext, mBrwView, absPath);// 获得文件位置
			}
			int width = (int) jsonObject.optDouble("width", 10);// 图片宽度
			int height = (int) jsonObject.optDouble("height", 10);// 图片高度

			// x，y容错修正
			x = FormatAmendUtil.between0and1(x);
			y = FormatAmendUtil.between0and1(y);

			final String idNew = id;
			final float xNew = x;
			final float yNew = y;
			final int widthNew = width;
			final int heightNew = height;
			final String imgUrlNew = imgUrl;
			// 如果HashMap中没有这个id，添加
			if (!mapTagViews.containsKey(idNew)) {
				((Activity) mContext).runOnUiThread(new Runnable() {// 在主线程中更新UI
					@Override
					public void run() {
						try {
							mPictureTagLayout.addTagView(idNew, xNew, yNew, widthNew, heightNew, imgUrlNew);// 添加标签View
						} catch (Exception e) {
							e.printStackTrace();
							MLog.getIns().e(e);
						}
					}
				});
			}
			// 否则进行更新操作(在布局上移除它的View,然后在HashMap中删除这个标签，再添加新标签，相当于更新操作)
			else {
				final View view = mapTagViews.get(idNew);// 现在HashMap中获得对应id标签的实例
				// 在主线程中更新UI
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mPictureTagLayout.removeView(view);// 移除旧标签View
						mapTagViews.remove(idNew);// 在HashMap中删除这个标签
						mPictureTagLayout.addTagView(idNew, xNew, yNew, widthNew, heightNew, imgUrlNew);// 添加新标签
					}
				});
			}
		} catch (Exception e) {
			formatError(e.getMessage());
			e.printStackTrace();
			MLog.getIns().e(e);
		}

	}

	/**
	 * 根据id删除标签
	 * 
	 * @param para
	 */
	public void deleteTag(String[] para) {
		if (para.length < 1) {
			return;
		}
		// 判断字符串是否是int型
		for (int i = 0; i < para[0].length(); i++) {
			char c = para[0].charAt(i);// 得到每个字符
			int ascii_c = c;// 获得该字符ascii码
			if (ascii_c < 48 || (ascii_c > 57)) {
				formatError("字符串不是int型");
				return;
			}
		}
		final String deleteId = para[0];
		// 如果表中没有这个id
		if (!mapTagViews.containsKey(deleteId)) {
			idNotExistError();
			return;
		}
		deleteList.add(deleteId);// 把要删除的id放在删除列表里
		final PictureTagView deleteView = mapTagViews.get(deleteId);
		// 在主线程中更新UI
		((Activity) mContext).runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mPictureTagLayout.removeView(deleteView);// 在视图上移除TagView
				Log.i("waka", "before remove" + deleteId);
				mapTagViews.remove(deleteId);// 在表中删除标签数据
				Log.i("waka", "after remove" + deleteId);
			}
		});
		deleteTagToFront(deleteId);// 给前端删除回调
	}

	/**
	 * 获取所有标签
	 */
	public void getAllTags(String[] parm) {
		JSONObject jsonObject = new JSONObject();
		JSONArray jsonArray = new JSONArray();
		try {
			for (Map.Entry<String, PictureTagView> entry : mapTagViews.entrySet()) {
				String key = entry.getKey();
				// 他娘的删不完再删一次
				if (deleteList.contains(key)) {
					Log.i("waka", "before 再remove" + key);
					mapTagViews.remove(key);
					Log.i("waka", "after 再remove" + key);
				} else {
					PictureTagView pView = entry.getValue();
					String id = pView.id;
					float x = pView.x;
					float y = pView.y;
					String title = pView.title;
					float textSize = pView.textSize;
					String textColor = pView.textColor;
					String message = pView.message;

					JSONObject jsonTag = new JSONObject();
					jsonTag.put("key", key);
					jsonTag.put("id", id);
					jsonTag.put("x", x);
					jsonTag.put("y", y);
					jsonTag.put("title", title);
					jsonTag.put("textSize", textSize);
					jsonTag.put("textColor", textColor);
					jsonTag.put("message", message);
					jsonArray.put(jsonTag);

					Log.i("uexImageTag", " key---->" + key + " id---->" + id + " x---->" + x + " y---->" + y + " title---->" + title + " textSize---->" + textSize + " textColor---->" + textColor
							+ " message---->" + message);
				}
			}
			jsonObject.put("tag", jsonArray);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			// deleteList.clear();// 最后清空删除列表
		}
		Log.i(TAG, jsonObject.toString());
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_GET_ALL_TAGS_CALLBACK + "){" + FUNC_GET_ALL_TAGS_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 设置是否允许移动标签
	 * 
	 * @param parm
	 */
	public void setIsMoveable(String[] parm) {
		if (parm.length < 1) {
			return;
		}
		if (mPictureTagLayout == null) {
			return;
		}
		if (parm[0].equals("0") || parm[0].equals("1")) {
			int flag = Integer.valueOf(parm[0]);
			mPictureTagLayout.setIsMoveable(flag);
			jsCallback(FUNC_SET_IS_MOVEABLE_CALLBACK, 0, EUExCallback.F_C_TEXT, flag);
		} else {
			formatError("参数不为0或1");
		}
	}

	/**
	 * xy格式错误
	 */
	public void xyFormatError() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("error", XY_FORMAT_ERROR_TIPS);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ERROR_CALLBACK + "){" + FUNC_ERROR_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 颜色数据格式错误
	 */
	public void colorFormatError() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("error", COLOR_FORMAT_ERROR_TIPS);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ERROR_CALLBACK + "){" + FUNC_ERROR_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 删除时id不存在
	 */
	public void idNotExistError() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("error", ID_NOT_EXIST_ERROR_TIPS);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ERROR_CALLBACK + "){" + FUNC_ERROR_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 数据格式错误
	 */
	public void formatError(String errorInfo) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("error", errorInfo);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ERROR_CALLBACK + "){" + FUNC_ERROR_CALLBACK + "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * clean
	 */
	@Override
	protected boolean clean() {
		deleteList.clear();
		return false;
	}

	/**
	 * 添加View到当前Window
	 * 
	 * @param child
	 * @param parms
	 */
	private void addView2CurrentWindow(View child, RelativeLayout.LayoutParams parms) {
		final View cView = child;
		int l = (int) (parms.leftMargin);
		int t = (int) (parms.topMargin);
		int w = parms.width;
		int h = parms.height;
		final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(w, h);
		lp.gravity = Gravity.NO_GRAVITY;
		lp.leftMargin = l;
		lp.topMargin = t;
		mBrwView.addViewToCurrentWindow(cView, lp);
	}

	/**
	 * 将绝对坐标变为图片的百分比,X
	 * 
	 * @param x
	 * @return
	 */
	public float percentX(float x) {
		float x_new = x / inWidth;
		return x_new;
	}

	/**
	 * Y
	 * 
	 * @param y
	 * @return
	 */
	public float percentY(float y) {
		float y_new = y / inHeight;
		return y_new;
	}

}
