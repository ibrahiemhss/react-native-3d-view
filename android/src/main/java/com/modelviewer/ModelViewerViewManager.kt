package com.modelviewer

import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class ModelViewerViewManager (reactApplicationContext: ReactApplicationContext) : SimpleViewManager<DirectNativeView>() {
  override fun getName() = "ModelViewerView"
  private var mReactApplicationContext: ReactApplicationContext? = null

  init {
    mReactApplicationContext=reactApplicationContext
  }
  override fun createViewInstance(reactContext: ThemedReactContext): DirectNativeView {
    return DirectNativeView(reactContext, mReactApplicationContext!!)
  }

  @ReactProp(name = "color")
  fun setColor(view: DirectNativeView, color: String) {
    view.setModelViewerColor(Color.parseColor(color));
    view.setBackgroundColor(Color.parseColor(color))
  }
  @ReactProp(name = "loadingColor")
  fun setLoadingColor(view: DirectNativeView, color: String) {
    view.setProgressColor(color)
  }
  @RequiresApi(Build.VERSION_CODES.N)
  @ReactProp(name = "url")
  fun setUrl(view: DirectNativeView, url: String) {
    if(url!=null){
      view.loadGlbUrl(url)
    }
  }
}
