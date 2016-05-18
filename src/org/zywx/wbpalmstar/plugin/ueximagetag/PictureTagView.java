package org.zywx.wbpalmstar.plugin.ueximagetag;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.ueximagetag.callback.CallbackRemoveView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * 自定义TagView，标签View
 * 
 * @author xiaolong.fan,waka
 *
 */
public class PictureTagView extends RelativeLayout implements OnEditorActionListener, OnClickListener {

	private Context mContext;
	private EUExImageTag mEUExImageTag;

	private View mView;
	// 标签
	public RelativeLayout tagLayout;
	public TextView tvPictureTagLabel;
	public EditText etPictureTagLabel;
	private ImageView imgClose;
	// 点
	public ImageView imgPoint;

	// Bean属性
	public String id;
	public float x;
	public float y;
	// 标签
	public String title;
	public float textSize;
	public String textColor;
	public String message;
	// 点
	public String imgUrl;
	public int width;
	public int height;

	public enum Status {
		Normal, Edit// 定义状态枚举
	}

	private InputMethodManager imm;
	private static final int ViewWidth = 80;
	private static final int ViewHeight = 50;
	private CallbackRemoveView callbackRemoveView;

	/**
	 * 构造方法
	 * 
	 * @param context
	 * @param direction
	 */
	public PictureTagView(Context context, String id, EUExImageTag uexImageTag) {
		super(context);
		this.mContext = context;
		this.id = id;
		this.mEUExImageTag = uexImageTag;
		initViews();
		initData();
		initEvents();
	}

	/** 初始化视图 **/
	protected void initViews() {

		mView = LayoutInflater.from(mContext).inflate(EUExUtil.getResLayoutID("plugin_uex_image_tag_picturetagview"), this, true);

		// 标签
		tagLayout = (RelativeLayout) mView.findViewById(EUExUtil.getResIdID("plugin_uex_image_tag_tagLayout"));
		tvPictureTagLabel = (TextView) mView.findViewById(EUExUtil.getResIdID("plugin_uex_image_tag_tvPictureTagLabel"));
		etPictureTagLabel = (EditText) mView.findViewById(EUExUtil.getResIdID("plugin_uex_image_tag_etPictureTagLabel"));
		imgClose = (ImageView) mView.findViewById(EUExUtil.getResIdID("plugin_uex_image_tag_imgClose"));

		// 点
		imgPoint = (ImageView) mView.findViewById(EUExUtil.getResIdID("plugin_uex_image_tag_imgPoint"));
	}

	/** 初始化 **/
	protected void initData() {
		imm = (InputMethodManager) EUExUtil.mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
	}

	/** 初始化事件 **/
	protected void initEvents() {
		etPictureTagLabel.setOnEditorActionListener(this);
		imgClose.setOnClickListener(this);
	}

	/**
	 * 设置状态，正常状态和可编辑状态
	 * 
	 * @param status
	 */
	public void setStatus(Status status) {
		switch (status) {
		case Normal:
			tvPictureTagLabel.setVisibility(View.VISIBLE);
			etPictureTagLabel.clearFocus();
			tvPictureTagLabel.setText(etPictureTagLabel.getText());
			etPictureTagLabel.setVisibility(View.GONE);
			imgClose.setVisibility(View.GONE);
			// 隐藏键盘
			imm.hideSoftInputFromWindow(etPictureTagLabel.getWindowToken(), 0);
			// 更新HashMap中的title数据
			title = etPictureTagLabel.getText().toString();
			mEUExImageTag.mapTagViews.put(id, this);
			mEUExImageTag.onChangeToFront(id, x, y, title, textSize, textColor, message);
			break;
		case Edit:
			tvPictureTagLabel.setVisibility(View.GONE);
			etPictureTagLabel.setVisibility(View.VISIBLE);
			etPictureTagLabel.setSelection(etPictureTagLabel.getText().length());// 光标移到最后一位方便编辑
			imgClose.setVisibility(View.VISIBLE);
			etPictureTagLabel.requestFocus();
			// 弹出键盘
			imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
			break;
		}
	}

	/**
	 * 当编辑器执行动作时的回调函数的接口
	 */
	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		setStatus(Status.Normal);
		return true;
	}

	/**
	 * onClick
	 */
	@Override
	public void onClick(View v) {
		// 关闭
		if (v.getId() == EUExUtil.getResIdID("plugin_uex_image_tag_imgClose")) {
			// 提示对话框，确认是否要删除
			AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
			builder.setTitle(EUExUtil.getString("prompt"));
			builder.setMessage(EUExUtil.getString("plugin_uex_image_tag_confirm_delete"));
			builder.setPositiveButton(EUExUtil.getString("confirm"), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mEUExImageTag.mapTagViews.remove(id);
					mEUExImageTag.deleteTagToFront(id);
					callbackRemoveView.callbackRemoveView();
				}
			});
			builder.setNegativeButton(EUExUtil.getString("cancel"), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			builder.create().show();

		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	public static int getViewWidth() {
		return ViewWidth;
	}

	public static int getViewHeight() {
		return ViewHeight;
	}

	/**
	 * RemoveView回调注册方法
	 */
	public void setCallbackRemoveView(CallbackRemoveView callbackRemoveView) {
		this.callbackRemoveView = callbackRemoveView;
	}
}
