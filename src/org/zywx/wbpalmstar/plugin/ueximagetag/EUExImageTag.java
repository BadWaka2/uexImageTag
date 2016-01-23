package org.zywx.wbpalmstar.plugin.ueximagetag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zywx.wbpalmstar.base.BUtility;
import org.zywx.wbpalmstar.engine.EBrowserView;
import org.zywx.wbpalmstar.engine.universalex.EUExBase;
import org.zywx.wbpalmstar.engine.universalex.EUExCallback;
import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
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

	private View mView = null;
	private PictureTagLayout mPictureTagLayout;
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
			Log.i("uexImageTag",
					"x---->" + x + " , y---->" + y + " , inWidth---->" + inWidth + " , inHeight---->" + inHeight);
			imgPath = parm[4];
			Log.i("uexImageTag", "imgPath---->" + imgPath);
		} catch (NumberFormatException e) {
			Log.i("uexImageTag", "NumberFormatException");
			e.printStackTrace();
		}
		Log.i(TAG, "jsonTagIn----->" + jsonTagIn);
		if (imgPath != null && mView == null) {

			// NEW 动态添加布局
			/*
			 * mPictureTagLayout = (PictureTagLayout)
			 * LayoutInflater.from(mContext) .inflate(EUExUtil.getResLayoutID(
			 * "plugin_uex_image_tag_picture_tag_layout"), null);
			 */

			// OLD 从布局文件中加载布局
			int myViewID = EUExUtil.getResLayoutID("plugin_uex_image_tag_main");
			mView = View.inflate(mContext, myViewID, null);

			Log.i(TAG, "绝对路径---->"
					+ BUtility.makeRealPath(imgPath, mBrwView.getWidgetPath(), mBrwView.getCurrentWidget().m_wgtType));
			Bitmap bitmap = Utils.compressImage(mContext,
					BUtility.makeRealPath(imgPath, mBrwView.getWidgetPath(), mBrwView.getCurrentWidget().m_wgtType));
			if (bitmap == null) {
				formatError();
				removeImage(null);
				return;
			}
			bitmap.getWidth();
			bitmap.getHeight();

			mPictureTagLayout = (PictureTagLayout) mView.findViewById(EUExUtil.getResIdID("picture"));

			mPictureTagLayout.setmEuExImageTag(this);// 传入EuExImageTag
			mPictureTagLayout.setWidth(inWidth);// 设置layout宽度
			mPictureTagLayout.setHeight(inHeight);// 设置layout高度
			mPictureTagLayout.setBackgroundDrawable(new BitmapDrawable(bitmap));
			final RelativeLayout.LayoutParams lparam = new RelativeLayout.LayoutParams(inWidth, inHeight);
			lparam.leftMargin = x;
			lparam.topMargin = y;
			addView2CurrentWindow(mView, lparam);

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
					formatError();
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
		if (mView != null) {
			mapTagViews.clear();
			removeViewFromCurrentWindow(mView);
			mView = null;
		}
	}

	/**
	 * 长按标签回调
	 */
	public void onLongClickTag(String id, float x, float y, String title, float textSize, String textColor,
			String message) {
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
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_LONG_CLICK_TAG_CALLBACK + "){" + FUNC_ON_LONG_CLICK_TAG_CALLBACK
				+ "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
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
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_LONG_CLICK_IMAGE_CALLBACK + "){" + FUNC_ON_LONG_CLICK_IMAGE_CALLBACK
				+ "(" + 0 + "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 数据变更回调
	 */
	public void onChangeToFront(String id, float x, float y, String title, float textSize, String textColor,
			String message) {
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
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_CHANGE_CALLBACK + "){" + FUNC_ON_CHANGE_CALLBACK + "(" + 0 + ","
				+ EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 点击标签回调
	 */
	public void onClickTag(String id, float x, float y, String title, float textSize, String textColor,
			String message) {
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
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_CLICK_TAG_CALLBACK + "){" + FUNC_ON_CLICK_TAG_CALLBACK + "(" + 0
				+ "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
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
		String js = SCRIPT_HEADER + "if(" + FUNC_ON_CLICK_IMAGE_CALLBACK + "){" + FUNC_ON_CLICK_IMAGE_CALLBACK + "(" + 0
				+ "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
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
		String js = SCRIPT_HEADER + "if(" + FUNC_DELETE_TAG_CALLBACK + "){" + FUNC_DELETE_TAG_CALLBACK + "(" + 0 + ","
				+ EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 添加或更新TagView标签，id不存在则添加，存在则更新
	 */
	public void setTag(String[] parm) {
		if (parm.length < 1) {
			return;
		}
		String jsonStr = parm[0];// 得到JSON字符串
		Log.i(TAG, "jsonStr------->" + jsonStr);
		try {
			// 解析JSON，获得标签数据
			JSONObject jsonObject = new JSONObject(jsonStr);
			String id = jsonObject.getString("id");
			// float x = (float) jsonObject.getDouble("x");
			// float y = (float) jsonObject.getDouble("y");
			// final String title = jsonObject.getString("title");
			// float textSize = (float) jsonObject.getDouble("textSize");
			// String textColor = jsonObject.getString("textColor");
			// final String message = jsonObject.getString("message");
			float x = (float) jsonObject.optDouble("x", 0);
			float y = (float) jsonObject.optDouble("y", 0);
			Log.i("uexImageTag", "id---->" + id + " , x---->" + x + " , y---->" + y);

			final String title = jsonObject.optString("title", "Tag");
			float textSize = (float) jsonObject.optDouble("textSize", 15);
			String textColor = jsonObject.optString("textColor", "#ffffffff");
			final String message = jsonObject.optString("message", "");
			// 判断xy格式
			if (x < 0 || x > 1 || y < 0 || y > 1) {
				// xyFormatError();
				formatError();
			}
			// x，y容错修正
			if (x < 0) {
				x = 0;
				Log.i("uexImageTag", "容错修正x=0");
			} else if (x > 1) {
				x = 1;
			}

			if (y < 0) {
				y = 0;
				Log.i("uexImageTag", "容错修正y=0");
			} else if (y > 1) {
				y = 1;
			}
			// 判断颜色格式，第一道关卡，字符数不等于7或9的直接return;
			if (!(textColor.length() == 7 || textColor.length() == 9)) {
				// colorFormatError();
				formatError();
				return;
			}
			// 如果textColor第一位不是'#'，return
			if (textColor.charAt(0) != '#') {
				// colorFormatError();
				formatError();
				return;
			}
			// 如果是7位颜色代码，则增加透明度代码'ff'
			if (textColor.length() == 7) {
				StringBuffer stringBuffer = new StringBuffer(textColor);
				stringBuffer.insert(1, 'f');
				stringBuffer.insert(2, 'f');
				textColor = stringBuffer.toString();
			}
			// 判断颜色格式，第二道关卡，每个字符的ASCII码必须在48~57(0~9的ASCII码)或65~70(A~F的ASCII码)或97~102(a~f的ASCII码)之间
			for (int i = 1; i < 7; i++) {
				char c = textColor.charAt(i);// 得到每个字符
				int ascii_c = c;// 获得该字符ascii码
				// 如果该字符不在规定范围内，return
				if (ascii_c < 48 || (ascii_c > 57 && ascii_c < 65) || (ascii_c > 70 && ascii_c < 97) || ascii_c > 102) {
					// colorFormatError();
					formatError();
					return;
				}
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
				// 在主线程中更新UI
				((Activity) mContext).runOnUiThread(new Runnable() {
					@Override
					public void run() {
						try {
							mPictureTagLayout.addTagView(idNew, xNew, yNew, title, textSizeNew, textColorNew, message);// 添加标签View
						} catch (Exception e) {
							// TODO: handle exception
						}
					}
				});
			}
			// 否则进行更新操作____(在布局上移除它的View,然后在HashMap中删除这个标签，再添加新标签，相当于更新操作)
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
		} catch (JSONException e) {
			// if (id != -1) {
			// addOrUpdateDefalutTagView(id);
			// }
			formatError();
			e.printStackTrace();
		} catch (Exception e) {
			formatError();
			e.printStackTrace();
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
				formatError();
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
		// // 他娘的删不完再删一次
		// if (mapTagViews.containsKey(deleteId)) {
		// Log.i("waka", "before 再remove" + deleteId);
		// mapTagViews.remove(deleteId);// 在表中删除标签数据
		// Log.i("waka", "after 再remove" + deleteId);
		// }
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

					Log.i("uexImageTag",
							" key---->" + key + " id---->" + id + " x---->" + x + " y---->" + y + " title---->" + title
									+ " textSize---->" + textSize + " textColor---->" + textColor + " message---->"
									+ message);
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
		String js = SCRIPT_HEADER + "if(" + FUNC_GET_ALL_TAGS_CALLBACK + "){" + FUNC_GET_ALL_TAGS_CALLBACK + "(" + 0
				+ "," + EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
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
			formatError();
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
		String js = SCRIPT_HEADER + "if(" + FUNC_ERROR_CALLBACK + "){" + FUNC_ERROR_CALLBACK + "(" + 0 + ","
				+ EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
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
		String js = SCRIPT_HEADER + "if(" + FUNC_ERROR_CALLBACK + "){" + FUNC_ERROR_CALLBACK + "(" + 0 + ","
				+ EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
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
		String js = SCRIPT_HEADER + "if(" + FUNC_ERROR_CALLBACK + "){" + FUNC_ERROR_CALLBACK + "(" + 0 + ","
				+ EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
		onCallback(js);
	}

	/**
	 * 数据格式错误
	 */
	public void formatError() {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("error", FORMAT_ERROR_TIPS);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 给前端回调一个JSON对象，不是JSON字符串哦，是JSON对象
		String js = SCRIPT_HEADER + "if(" + FUNC_ERROR_CALLBACK + "){" + FUNC_ERROR_CALLBACK + "(" + 0 + ","
				+ EUExCallback.F_C_JSON + "," + jsonObject.toString() + SCRIPT_TAIL;
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
