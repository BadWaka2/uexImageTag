package org.zywx.wbpalmstar.plugin.ueximagetag;

import org.zywx.wbpalmstar.plugin.ueximagetag.PictureTagView.Status;
import org.zywx.wbpalmstar.plugin.ueximagetag.callback.CallbackRemoveView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.RelativeLayout;

/**
 * 自定义布局，用来显示图片并操作标签
 * 
 * @author xiaolong.fan,waka
 *
 */
@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
public class PictureTagLayout extends RelativeLayout
		implements OnTouchListener, OnLongClickListener, CallbackRemoveView {

	private static final String TAG = "PictureTagLayout";
	private static final int CLICKRANGE = 5;
	int startX = 0;
	int startY = 0;
	int startTouchViewLeft = 0;
	int startTouchViewTop = 0;
	private float screenWidth;// 屏幕宽
	private float screenHeight;// 屏幕高
	private float width;// layout width
	private float height;// layout height
	private Context mContext;
	private EUExImageTag mEuExImageTag;// 传入的入口类对象，我们需要调用它的方法，来给前段传回调
	private PictureTagView touchView, clickView;// touchView代表TextView，用来显示和移动；clickView代表EditText，用来编辑文本
	public String imagePath = "";// 图片路径

	private int isMoveable = 1;// 设置是否可以移动标签的标志位

	// 自定义长按事件
	private boolean isLongClick = false;// 长按事件标识，用来区分长按事件和点击事件
	private boolean isMoved;// 是否移动了
	private Runnable mLongPressRunnable;// 长按的Runnable
	private static final int MOVE_THRESHOLD = 10;// 移动的阈值

	/**
	 * 一个参数的构造方法，它将会调用两个参数的构造方法
	 * 
	 * @param context
	 */
	public PictureTagLayout(Context context) {
		this(context, null);
	}

	/**
	 * 两个参数的构造方法,如果想在布局里使用自定义View，必须实现两个参数的构造方法
	 * 
	 * @param context
	 * @param attrs
	 */
	public PictureTagLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
		Log.i(TAG, "screenWidth---->" + screenWidth);
		Log.i(TAG, "screenHeight---->" + screenHeight);
		initView();
		initData();
		initEvent();
	}

	/**
	 * 设置mEuExImageTag
	 * 
	 * @param mEuExImageTag
	 */
	public void setmEuExImageTag(EUExImageTag mEuExImageTag) {
		this.mEuExImageTag = mEuExImageTag;
	}

	/**
	 * initView
	 */
	private void initView() {

	}

	/**
	 * initData
	 */
	private void initData() {
		// 初始化长按Runnable
		mLongPressRunnable = new Runnable() {
			@Override
			public void run() {
				performLongClick();// 这个方法View是用来分发事件的
			}
		};
	}

	/**
	 * initEvent
	 */
	private void initEvent() {
		this.setOnTouchListener(this);
		this.setOnLongClickListener(this);
	}

	/**
	 * onTouch，这里使用onTouch模拟了点击事件(距离判定)和长按事件(自己分发系统长按事件)，以避免Android系统机制的拦截， 以
	 * 实现更灵活的监听方式
	 * 
	 * @param v
	 * @param event
	 * @return
	 */
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		// 按下
		case MotionEvent.ACTION_DOWN:
			touchView = null;
			if (clickView != null) {
				((PictureTagView) clickView).setStatus(Status.Normal);
				clickView = null;
			}
			startX = (int) event.getX();
			startY = (int) event.getY();
			// 如果有View存在，则选中这个View
			if (hasView(startX, startY)) {
				startTouchViewLeft = touchView.getLeft();
				startTouchViewTop = touchView.getTop();
			}
			isLongClick = false;// 在每次按下之时初始化长按标识
			isMoved = false;
			postDelayed(mLongPressRunnable, ViewConfiguration.getLongPressTimeout());// 在500ms后执行mLongPressRunnable,500ms是获取到的系统延时，可以自己改

			break;
		// 移动
		case MotionEvent.ACTION_MOVE:
			if (isMoveable == 1) {
				moveView((int) event.getX(), (int) event.getY());
			}
			if (isMoved) {
				break;
			}
			if ((Math.abs(startX - event.getX())) > MOVE_THRESHOLD
					|| (Math.abs(startY - event.getY())) > MOVE_THRESHOLD) {
				// 移动超过阈值，则表示移动了
				isMoved = true;
				removeCallbacks(mLongPressRunnable);// 将Runnable从事件队列中remove掉，长按事件也就不会再触发了
			}
			break;
		// 抬起
		case MotionEvent.ACTION_UP:
			// 释放长按事件
			removeCallbacks(mLongPressRunnable);
			int endX = (int) event.getX();
			int endY = (int) event.getY();
			// 如果挪动的范围很小，且长按事件没有被触发，则判定为单击
			if (touchView != null && isLongClick == false && Math.abs(endX - startX) < CLICKRANGE
					&& Math.abs(endY - startY) < CLICKRANGE) {
				mEuExImageTag.onClickTag(touchView.id, touchView.x, touchView.y, touchView.title, touchView.textSize,
						touchView.textColor, touchView.message);
			}
			// 移动后抬起
			else if (touchView != null && clickView == null && isLongClick == false && isMoveable == 1) {
				mEuExImageTag.onChangeToFront(touchView.id, touchView.x, touchView.y, touchView.title,
						touchView.textSize, touchView.textColor, touchView.message);
			} else if (isLongClick == false && Math.abs(endX - startX) < CLICKRANGE
					&& Math.abs(endY - startY) < CLICKRANGE) {
				float x = (float) startX / mEuExImageTag.inWidth;
				float y = (float) startY / mEuExImageTag.inHeight;
				mEuExImageTag.onClickImage(x, y);
			}
			touchView = null;
			break;
		}
		return true;// 虽然默认情况下这样会接收不到系统的长按事件，但是我们是自己分发的，所以无所谓了
	}

	/**
	 * 长按事件，我们自己使用mLongPressRunnable分发的系统长按事件
	 * 
	 * @param v
	 * @return
	 */
	@Override
	public boolean onLongClick(View v) {
		isLongClick = true;// 将长按标识设为true
		if (hasView(startX, startY)) {
			// 设置为编辑状态
			// ((PictureTagView) touchView).setStatus(Status.Edit);
			// clickView = touchView;
			mEuExImageTag.onLongClickTag(touchView.id, touchView.x, touchView.y, touchView.title, touchView.textSize,
					touchView.textColor, touchView.message);
		} else {
			// float x = (float) startX / EUExImageTag.inWidth;
			// float y = (float) startY / EUExImageTag.inHeight;
			// mEuExImageTag.onLongClickImage(x, y);// 向前端发送长按回调
		}
		return false;
	}

	/**
	 * 添加标签项，每次新建TagView时都将TagView添加到EUExImageTag.listTagViews中，主键为id
	 * 
	 * @param x
	 */
	public void addTagView(String id, float x, float y, String title, float textSize, String textColor,
			String message) {
		PictureTagView view = null;
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int) (x * mEuExImageTag.inWidth);// 设置x
		Log.i("uexImageTag", "addTagView params.leftMargin---->" + params.leftMargin);
		params.topMargin = (int) (y * mEuExImageTag.inHeight);// 设置y
		Log.i("uexImageTag", "addTagView params.topMargin---->" + params.topMargin);
		view = new PictureTagView(getContext(), id);// 初始化，设置id
		view.setmEuExImageTag(mEuExImageTag);// 设置上下文
		view.tvPictureTagLabel.setText(title);// 设置标题
		view.tvPictureTagLabel.setTextSize(textSize);// 设置字体大小
		view.tvPictureTagLabel.setTextColor(Color.parseColor(textColor));// 设置字体颜色
		view.etPictureTagLabel.setText(title);
		view.etPictureTagLabel.setTextSize(textSize);
		view.etPictureTagLabel.setTextColor(Color.parseColor(textColor));
		view.setCallbackRemoveView(this);// CallbackRemoveView注册回调

		// 上下位置在视图内
		if ((params.topMargin + PictureTagView.getViewHeight()) > height) {
			params.topMargin = (int) (height - PictureTagView.getViewHeight());
			Log.i("uexImageTag", "修正偏移 height---->" + height + " params.topMargin---->" + params.topMargin);
		}
		// 左右位置在视图内
		if ((params.leftMargin + PictureTagView.getViewWidth()) > width) {
			params.leftMargin = (int) (width - PictureTagView.getViewWidth());
			Log.i("uexImageTag", "修正偏移 width---->" + width + "params.leftMargin---->" + params.leftMargin);
		}

		view.id = id;
		view.x = x;
		view.y = y;
		view.title = title;
		view.textSize = textSize;
		view.textColor = textColor;
		view.message = message;
		mEuExImageTag.mapTagViews.put(id, view);
		mEuExImageTag.onChangeToFront(id, x, y, title, textSize, textColor, message);// 添加Tag也改变了，所以也需要调用这个

		Log.i("uexImageTag", "before addView");
		addView(view, params);
		Log.i("uexImageTag", "after addView");
	}

	/**
	 * 移动标签
	 * 
	 * @param x
	 * @param y
	 */
	private void moveView(int x, int y) {
		if (touchView == null) {
			return;
		}
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.leftMargin = x - startX + startTouchViewLeft;
		params.topMargin = y - startY + startTouchViewTop;
		// 限制子控件移动必须在视图范围内
		if (params.leftMargin < 0 || (params.leftMargin + touchView.getWidth()) > width)
			params.leftMargin = touchView.getLeft();
		if (params.topMargin < 0 || (params.topMargin + touchView.getHeight()) > height)
			params.topMargin = touchView.getTop();
		touchView.x = mEuExImageTag.percentX(params.leftMargin);
		touchView.y = mEuExImageTag.percentY(params.topMargin);
		mEuExImageTag.mapTagViews.put(touchView.id, touchView);// 更新HashMap中的数据
		touchView.setLayoutParams(params);
	}

	/**
	 * 判断当前点是否存在标签
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean hasView(int x, int y) {
		// 循环获取子view，判断xy是否在子view上，即判断是否按住了子view
		Log.i(TAG, "-->this.getChildCount():" + this.getChildCount());
		for (int index = 0; index < this.getChildCount(); index++) {
			View view = this.getChildAt(index);
			int left = (int) view.getX();
			int top = (int) view.getY();
			int right = view.getRight();
			int bottom = view.getBottom();
			Rect rect = new Rect(left, top, right, bottom);
			boolean contains = rect.contains(x, y);
			// 如果是与子view重叠则返回真,表示已经有了view不需要添加新view了
			if (contains) {
				touchView = (PictureTagView) view;
				touchView.bringToFront();
				return true;
			}
		}
		touchView = null;
		return false;
	}

	/**
	 * 来自PictureTagView的CallbackRemoveView回调
	 */
	@Override
	public void callbackRemoveView() {
		if (clickView != null) {
			removeView(clickView);
			clickView = null;
			touchView = null;
		}
	}

	/**
	 * 获得是否允许移动标志
	 * 
	 * @return
	 */
	public int getIsMoveable() {
		return isMoveable;
	}

	/**
	 * 设置是否允许移动标志
	 * 
	 * @param moveAllowedFlag
	 */
	public void setIsMoveable(int isMoveable) {
		this.isMoveable = isMoveable;
	}

	/**
	 * 设置layout width
	 * 
	 * @param width
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * 设置layout height
	 * 
	 * @param height
	 */
	public void setHeight(float height) {
		this.height = height;
	}
}
