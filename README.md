# SmoothInputLayout

表情面板和输入法平滑切换

## Download

or use Gradle:

**Step 1.** Add the JitPack repository to your build file

```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2.** Add the dependency

```
dependencies {
	        implementation 'com.github.ljr7822:SmoothInput:1.0.0'
	}
```

Or Maven:

**Step 1.** Add the JitPack repository to your build file

```
<repositories>
	<repository>
		   <id>jitpack.io</id>
		   <url>https://jitpack.io</url>
	</repository>
</repositories>
```

**Step 2.** Add the dependency

```
<dependency>
	   <groupId>com.github.ljr7822</groupId>
	   <artifactId>SmoothInput</artifactId>
	   <version>1.0.0</version>
</dependency>
```

## How do I use SmoothInputLayout?

**Step 1.** 在需要使用的xml布局中添加SmoothInputLayout标签；**必须为根布局**

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <cn.library.iwen.smoothinputlib.SmoothInputLayout
        android:id="@+id/lay_content"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:focusableInTouchMode="true"
        android:orientation="vertical"
        app:silInputPane="@+id/lay_panel"
        app:silInputView="@+id/edt_content">

        <androidx.coordinatorlayout.widget.CoordinatorLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1">

            <com.google.android.material.appbar.AppBarLayout
                android:id="@+id/abl_app_bar"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@color/black">

            </com.google.android.material.appbar.AppBarLayout>

            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/rv_recycler"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:paddingTop="8dp"
                android:paddingBottom="8dp"
                app:layout_behavior="@string/appbar_scrolling_view_behavior" />

        </androidx.coordinatorlayout.widget.CoordinatorLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="50dp"
            android:background="@color/white"
            android:elevation="2dp"
            android:orientation="horizontal"
            android:outlineProvider="bounds">

            <ImageView
                android:id="@+id/iv_face"
                android:layout_width="44dp"
                android:layout_height="44dp"
                android:contentDescription="@string/app_name"
                android:foreground="@drawable/ic_touch_fg"
                android:padding="8dp"
                android:src="@drawable/ic_emoji" />

            <ImageView
                android:id="@+id/iv_record"
                android:layout_width="44dp"
                android:layout_height="44dp"
                android:contentDescription="@string/app_name"
                android:foreground="@drawable/ic_touch_fg"
                android:padding="8dp"
                android:src="@drawable/ic_record" />

            <EditText
                android:id="@+id/edt_content"
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:background="@null"
                android:hint="请输入"
                android:inputType="text"
                android:lineSpacingExtra="0dp"
                android:lineSpacingMultiplier="1"
                android:maxHeight="68dp"
                android:minHeight="44dp"
                android:padding="4dp"
                android:textAppearance="@style/TextAppearance.AppCompat.Body1"
                android:textColor="@color/black"
                android:textCursorDrawable="@drawable/ic_cursor" />

            <ImageView
                android:id="@+id/iv_submit"
                android:layout_width="44dp"
                android:layout_height="44dp"
                android:contentDescription="@string/app_name"
                android:foreground="@drawable/ic_touch_fg"
                android:padding="8dp"
                android:src="@drawable/ic_submit" />

        </LinearLayout>

        <LinearLayout
            android:id="@+id/lay_panel"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:background="@color/white"
            android:visibility="gone">

            <fragment
                android:id="@+id/frag_panel"
                class="cn.library.iwen.smoothinputdemo.PanelFragment"
                android:layout_width="match_parent"
                android:layout_height="match_parent" />
        </LinearLayout>

    </cn.library.iwen.smoothinputlib.SmoothInputLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
```

**Step 2.** 添加一个fragment，并且连接到刚刚写的xml中

```xml
<LinearLayout
       android:id="@+id/lay_panel"
       android:layout_width="match_parent"
       android:layout_height="wrap_content"
       android:background="@color/white"
       android:visibility="gone">

       <fragment
           android:id="@+id/frag_panel"
           class="cn.library.iwen.smoothinputdemo.PanelFragment"
           android:layout_width="match_parent"
           android:layout_height="match_parent" />
</LinearLayout>
```

**Step 3.**添加每个不同面板的xml布局：lay_panel_face.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/lay_panel_face"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <androidx.viewpager.widget.ViewPager
        android:id="@+id/pager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="32dp"
        android:layout_marginTop="1px"
        android:background="@color/white"
        android:elevation="1dp"
        android:orientation="horizontal"
        android:outlineProvider="bounds"
        tools:targetApi="lollipop">

        <com.google.android.material.tabs.TabLayout
            android:id="@+id/tab"
            android:layout_width="0dp"
            android:layout_height="32dp"
            android:layout_gravity="bottom"
            android:layout_weight="2"
            app:tabGravity="fill"
            app:tabIndicatorColor="@color/black"
            app:tabIndicatorHeight="32dp"
            app:tabMode="fixed"
            app:tabSelectedTextColor="@color/black"
            app:tabTextAppearance="@style/TextAppearance.AppCompat.Small"
            app:tabTextColor="@color/white" />

        <ImageView
            android:id="@+id/im_backspace"
            android:layout_width="0dp"
            android:layout_height="32dp"
            android:layout_weight="1"
            android:contentDescription="@string/app_name"
            android:foreground="@drawable/ic_touch_fg"
            android:padding="6dp"
            android:scaleType="centerInside"
            android:src="@drawable/ic_backspace" />
    </LinearLayout>

</LinearLayout>
```

**Step 4.**使用`include`标签将每个面板导入到fragment中

```xml
<include
        layout="@layout/lay_panel_face"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

**Step 5.**在自定义的fragment中调用

```java
/**
 * 初始化表情
 *
 * @param root 界面view
 */
private void initFace(View root) {
    mFacePanel = root.findViewById(R.id.lay_panel_face);
}
```

