package com.modelviewer

import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

class RNViewManager (reactApplicationContext: ReactApplicationContext) : SimpleViewManager<RNSceneView>() {
  override fun getName() = "RN3dView"
  private var mReactApplicationContext: ReactApplicationContext? = null

  init {
    mReactApplicationContext=reactApplicationContext
  }
  override fun createViewInstance(reactContext: ThemedReactContext): RNSceneView {
   // mModelView = RNGLModelView(reactContext)

   // return RNGLModelView(reactContext)
    return RNSceneView(reactContext, mReactApplicationContext!!)
  }

  @ReactProp(name = "color")
  fun setColor(view: RNSceneView, color: String) {
   view.setModelViewerColor(Color.parseColor(color));
    view.setBackgroundColor(Color.parseColor(color))
  }
  @ReactProp(name = "loadingColor")
  fun setLoadingColor(view: RNSceneView, color: String) {
    view.setProgressColor(color)
  }

  @ReactProp(name = "duration")
  fun setDuration(view: RNSceneView, duration: Double) {

  }

  @RequiresApi(Build.VERSION_CODES.N)
  @ReactProp(name = "url")
  fun setUrl(view: RNSceneView, url: String) {
    if(url!=null){
      view.setModelUri(url)
    }
  }
}
