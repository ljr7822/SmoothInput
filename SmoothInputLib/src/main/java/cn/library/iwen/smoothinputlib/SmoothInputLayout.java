package cn.library.iwen.smoothinputlib;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import java.util.List;

/**
 * @author iwen大大怪
 * @Create to 2021/05/04 21:39
 */
public class SmoothInputLayout extends LinearLayout {

    private static final String TAG = "SmoothInputLayout";
    // 默认键盘高度
    public static final int DEFAULT_KEYBOARD_HEIGHT = 387;
    // 最小键盘高度
    public static final int MIN_KEYBOARD_HEIGHT = 20;
    // Sp-Key
    private static final String SP_KEYBOARD = "keyboard";
    private static final String KEY_HEIGHT = "height";
    private int mMaxKeyboardHeight = Integer.MIN_VALUE;
    private int mDefaultKeyboardHeight = 387;
    private int mMinKeyboardHeight;
    private int mKeyboardHeight;
    private int mInputViewId;
    private View mInputView;
    // 是否输入法已打开
    private boolean mKeyboardOpen = false;
    private boolean mKeyboardOpenOne = false;
    private int mInputPaneId;
    // 特殊输入面板
    private View mInputPane;
    private OnVisibilityChangeListener mListener;
    private OnKeyboardChangeListener keyboardChangeListener;
    // 自动保存键盘高度
    private boolean mAutoSaveKeyboardHeight;
    private KeyboardProcessor mKeyboardProcessor;
    private boolean tShowInputPane = false;

    // 构造函数会在new的时候调用
    public SmoothInputLayout(Context context) {
        this(context, null);
    }

    // 在布局中使用
    public SmoothInputLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    // 布局layout中调用,但是会有style
    public SmoothInputLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SmoothInputLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(attrs);
    }

    // 初始化
    private void initView(AttributeSet attrs) {
        // 键盘默认高度
        int defaultInputHeight = (int) (DEFAULT_KEYBOARD_HEIGHT * getResources().getDisplayMetrics().density);
        // 键盘最小高度
        int minInputHeight = (int) (MIN_KEYBOARD_HEIGHT * getResources().getDisplayMetrics().density);
        mInputViewId = NO_ID;
        mInputPaneId = NO_ID;
        boolean autoSave;
        // 获取自定义控件的属性
        // 使用 TypedArray 来获取 XML layout 中的属性值
        TypedArray custom = getContext().obtainStyledAttributes(attrs, R.styleable.SmoothInputLayout);
        // 获取指定资源id对应的尺寸
        defaultInputHeight = custom.getDimensionPixelOffset(R.styleable.SmoothInputLayout_silDefaultKeyboardHeight, defaultInputHeight);
        minInputHeight = custom.getDimensionPixelOffset(R.styleable.SmoothInputLayout_silMinKeyboardHeight, minInputHeight);
        mInputViewId = custom.getResourceId(R.styleable.SmoothInputLayout_silInputView, mInputViewId);
        mInputPaneId = custom.getResourceId(R.styleable.SmoothInputLayout_silInputPane, mInputPaneId);
        autoSave = custom.getBoolean(R.styleable.SmoothInputLayout_silAutoSaveKeyboardHeight, true);
        // 调用 recycle() 方法将 TypedArray 回收
        custom.recycle();
        // 设置默认系统输入面板高度
        setDefaultKeyboardHeight(defaultInputHeight);
        // 设置最小系统输入面板高度
        setMinKeyboardHeight(minInputHeight);
        // 设置自动保存键盘高度
        setAutoSaveKeyboardHeight(autoSave);
    }

    /**
     * 当XML布局被加载完后,初始化控件和数据
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (mInputViewId != NO_ID) {
            // 设置输入框
            setInputView(findViewById(mInputViewId));
        }
        if (mInputPaneId != NO_ID) {
            // 设置特殊输入面板
            setInputPane(findViewById(mInputPaneId));
        }
    }

    /**
     * onMeasure方法的作用是测量控件的大小，当我们创建一个View(执行构造方法)的时候不需要测量控件的大小，
     * 只有将这个view放入一个容器（父控件）中的时候才需要测量，而这个测量方法就是父控件唤起调用的。
     * 当控件的父控件要放置该控件的时候，父控件会调用子控件的onMeasure方法询问子控件：“你有多大的尺寸，我要给你多大的地方才能容纳你？”，
     * 然后传入两个参数（widthMeasureSpec和heightMeasureSpec），
     * 这两个参数就是父控件告诉子控件可获得的空间以及关于这个空间的约束条件，子控件拿着这些条件就能正确的测量自身的宽高了。
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if (mMaxKeyboardHeight > 3000) {
            // 此处代码是为了限制横竖屏切换的时候值过高问题 。
            mMaxKeyboardHeight = heightSize;
        }
        if (heightSize > mMaxKeyboardHeight) {
            mMaxKeyboardHeight = heightSize;
        }
        final int heightChange = mMaxKeyboardHeight - heightSize;
        Log.i(TAG, heightSize + "= heightSize=" + mMaxKeyboardHeight + "= mMaxKeyboardHeight = " + heightChange + "=heightChange = " + mMinKeyboardHeight + "=mMinKeyboardHeight");
        if (heightChange > mMinKeyboardHeight) {
            if (mKeyboardHeight != heightChange) {
                mKeyboardHeight = heightChange;
                saveKeyboardHeight();
            }
            mKeyboardOpen = true;
            // 输入法弹出，隐藏功能面板
            if (mInputPane != null && mInputPane.getVisibility() == VISIBLE) {
                mInputPane.setVisibility(GONE);
                if (mListener != null)
                    mListener.onVisibilityChange(GONE);
            }
        } else {
            mKeyboardOpen = false;
            if (tShowInputPane) {
                tShowInputPane = false;
                if (mInputPane != null && mInputPane.getVisibility() == GONE) {
                    updateLayout();
                    mInputPane.setVisibility(VISIBLE);
                    if (mListener != null)
                        mListener.onVisibilityChange(VISIBLE);
                    forceLayout();
                }
            }
        }

        Log.i(TAG, mKeyboardOpen + "=========");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (keyboardChangeListener != null) {
            keyboardChangeListener.onKeyboardChanged(mKeyboardOpen);
        }
    }

    /**
     * 获取键盘SP
     *
     * @return 键盘SP
     */
    private SharedPreferences getKeyboardSharedPreferences() {
        return getContext().getSharedPreferences(SP_KEYBOARD, Context.MODE_PRIVATE);
    }

    /**
     * 存储键盘高度
     */
    private void saveKeyboardHeight() {
        if (mAutoSaveKeyboardHeight)
            getKeyboardSharedPreferences().edit().putInt(KEY_HEIGHT, mKeyboardHeight).apply();
        else {
            if (mKeyboardProcessor != null)
                mKeyboardProcessor.onSaveKeyboardHeight(mKeyboardHeight);
        }
    }

    /**
     * 更新子项高度
     */
    private void updateLayout() {
        if (mInputPane == null) {
            return;
        }
        if (mKeyboardHeight == 0) {
            mKeyboardHeight = getKeyboardHeight(mDefaultKeyboardHeight);
        }
        ViewGroup.LayoutParams layoutParams = mInputPane.getLayoutParams();
        if (layoutParams != null) {
            layoutParams.height = mKeyboardHeight;
            mInputPane.setLayoutParams(layoutParams);
        }
    }

    /**
     * 获取键盘高度
     *
     * @param defaultHeight 默认高度
     * @return 键盘高度
     */
    private int getKeyboardHeight(int defaultHeight) {
        if (mAutoSaveKeyboardHeight) {
            return getKeyboardSharedPreferences().getInt(KEY_HEIGHT, defaultHeight);
        } else {
            return mKeyboardProcessor != null ? mKeyboardProcessor.getSavedKeyboardHeight(defaultHeight) : defaultHeight;
        }
    }

    /**
     * 设置默认系统输入面板高度
     *
     * @param height 输入面板高度
     */
    public void setDefaultKeyboardHeight(int height) {
        if (mDefaultKeyboardHeight != height) {
            mDefaultKeyboardHeight = height;
        }
    }

    /**
     * 设置最小系统输入面板高度
     *
     * @param height 输入面板高度
     */
    public void setMinKeyboardHeight(int height) {
        if (mMinKeyboardHeight != height) {
            mMinKeyboardHeight = height;
        }
    }

    /**
     * 设置输入框
     *
     * @param edit 输入框
     */
    public void setInputView(View edit) {
        if (mInputView != edit) {
            mInputView = edit;
        }
    }

    /**
     * 设置特殊输入面板
     *
     * @param pane 面板
     */
    public void setInputPane(View pane) {
        if (mInputPane != pane) {
            mInputPane = pane;
        }
    }

    /**
     * 设置面板可见改变监听
     *
     * @param listener 面板可见改变监听
     */
    public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
        mListener = listener;
    }

    /**
     * 设置键盘改变监听
     *
     * @param listener 键盘改变监听
     */
    public void setOnKeyboardChangeListener(OnKeyboardChangeListener listener) {
        keyboardChangeListener = listener;
    }

    /**
     * 设置自动保存键盘高度
     *
     * @param auto 是否自动
     */
    public void setAutoSaveKeyboardHeight(boolean auto) {
        mAutoSaveKeyboardHeight = auto;
    }

    /**
     * 设置键盘处理器
     * 仅在关闭自动保存键盘高度时设置的处理器才有效{@link #setAutoSaveKeyboardHeight(boolean)}
     *
     * @param processor 处理器
     */
    public void setKeyboardProcessor(KeyboardProcessor processor) {
        mKeyboardProcessor = processor;
    }

    /**
     * 是否输入法已打开
     *
     * @return 是否输入法已打开
     */
    public boolean isKeyBoardOpen() {
        return mKeyboardOpen;
    }

    public boolean isKeyboardOpenOne() {
        return mKeyboardOpenOne;
    }

    public void setKeyboardOpenOne(boolean mKeyboardOpenOne) {
        this.mKeyboardOpenOne = mKeyboardOpenOne;
    }

    public void setKeyboardOpen(boolean mKeyboardOpen) {
        this.mKeyboardOpen = mKeyboardOpen;
    }

    /**
     * 是否特殊输入面板已打开
     *
     * @return 特殊输入面板已打开
     */
    public boolean isInputPaneOpen() {
        return mInputPane != null && mInputPane.getVisibility() == VISIBLE;
    }

    /**
     * 关闭特殊输入面板
     */
    public void closeInputPane() {
        if (isInputPaneOpen()) {
            mInputPane.setVisibility(GONE);
            if (mListener != null) {
                mListener.onVisibilityChange(GONE);
            }
        }
    }

    /**
     * 显示特殊输入面板
     *
     * @param focus 是否让输入框拥有焦点
     */
    public void showInputPane(boolean focus) {
        // 判断键盘是否打开
        if (isKeyBoardOpen()) {
            tShowInputPane = true;
            InputMethodManager InputMethodManager = ((InputMethodManager) (getContext().getSystemService(Context.INPUT_METHOD_SERVICE)));
            if (InputMethodManager != null) {
                // 请求从当前正在接受输入的窗口的上下文中隐藏软输入窗口。
                InputMethodManager.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        } else {
            // 软键盘没有打开
            if (mInputPane != null && mInputPane.getVisibility() == GONE) {
                updateLayout();
                mInputPane.setVisibility(VISIBLE);
                if (mListener != null) {
                    mListener.onVisibilityChange(VISIBLE);
                }
            }
        }
        if (focus) {
            if (mInputView != null) {
                mInputView.requestFocus();
                mInputView.requestFocusFromTouch();
            }
        } else {
            if (mInputView != null) {
                setFocusable(true);
                setFocusableInTouchMode(true);
                mInputView.clearFocus();
            }
        }
    }

    /**
     * 关闭键盘
     *
     * @param clearFocus 是否清除输入框焦点
     */
    public void closeKeyboard(boolean clearFocus) {
        InputMethodManager imm = ((InputMethodManager) (getContext().getSystemService(Context.INPUT_METHOD_SERVICE)));
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        if (clearFocus && mInputView != null) {
            setFocusable(true);
            setFocusableInTouchMode(true);
            mInputView.clearFocus();
        }
    }

    /**
     * 打开键盘
     */
    public void showKeyboard() {
        if (mInputView == null) {
            return;
        }
        mInputView.requestFocus();
        mInputView.requestFocusFromTouch();
        InputMethodManager imm = ((InputMethodManager) (getContext().getSystemService(Context.INPUT_METHOD_SERVICE)));
        if (imm != null) {
            imm.showSoftInput(mInputView, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    /**
     * 当点击其他View时隐藏软键盘
     *
     * @param activity     Activity
     * @param ev           MotionEvent
     * @param excludeViews List<View>
     */
    public static void hideInputWhenTouchOtherView(Activity activity, MotionEvent ev, List<View> excludeViews) {
        // 获取点击事件
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (excludeViews != null && !excludeViews.isEmpty()) {
                for (int i = 0; i < excludeViews.size(); i++) {
                    if (isTouchView(excludeViews.get(i), ev)) {
                        return;
                    }
                }
            }
            // 获取当前焦点视图
            View view = activity.getCurrentFocus();
            if (isShouldHideInput(view, ev)) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager != null) {
                    // 隐藏软键盘
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    // 其他面板
                    //closeInputPane();
                }
            }
        }
    }

    /**
     * @param view 当前焦点视图
     * @param ev   点击事件
     * @return
     */
    private static boolean isShouldHideInput(View view, MotionEvent ev) {
        if (view != null && (view instanceof EditText)) {
            return !isTouchView(view, ev);
        }
        return false;
    }

    /**
     * 判断是否是焦点的view
     *
     * @param view 当前焦点视图
     * @param ev   点击事件
     * @return 默认true为是
     */
    private static boolean isTouchView(View view, MotionEvent ev) {
        if (view == null || ev == null) {
            return false;
        }
        int[] leftTop = {0, 0};
        // 获取一个控件在其父窗口中的坐标位置
        view.getLocationInWindow(leftTop);
        // 获取当前位置的横坐标
        int left = leftTop[0];
        // 获取当前位置的纵坐标
        int top = leftTop[1];
        // 获取当前位置的bottom坐标
        int bottom = top + view.getHeight();
        // 获取当前位置的right坐标
        int right = left + view.getWidth();
        // 开始判断点击是否在当前view中
        if (ev.getRawX() > left && ev.getRawX() < right && ev.getRawY() > top && ev.getRawY() < bottom) {
            return true;
        }
        return false;
    }

    /**
     * 面板可见改变监听
     */
    public interface OnVisibilityChangeListener {
        void onVisibilityChange(int visibility);
    }

    /**
     * 键盘改变监听
     */
    public interface OnKeyboardChangeListener {
        void onKeyboardChanged(boolean open);
    }

    /**
     * 键盘处理器
     */
    public interface KeyboardProcessor {
        /**
         * 存储键盘高度
         *
         * @param height 高度
         */
        void onSaveKeyboardHeight(int height);

        /**
         * 获取存储的键盘高度
         *
         * @param defaultHeight 默认高度
         * @return 键盘高度
         */
        int getSavedKeyboardHeight(int defaultHeight);
    }
}

