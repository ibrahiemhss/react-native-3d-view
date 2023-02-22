package com.modelviewer

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.facebook.react.bridge.*
import com.google.ar.sceneform.SceneView
import java.nio.ByteBuffer
import java.util.*


@SuppressLint("MissingInflatedId")
class NativeView(
  context: Context,
  reactApplicationContext: ReactApplicationContext) :
  RelativeLayout(context),LifecycleOwner,Loading {

  private lateinit var lifecycleRegistry: LifecycleRegistry
  private val TAG = "NativeGlbView"
  var mainView: View? = null
  var surfaceView: SurfaceView? = null
  var customViewer: CustomViewer = CustomViewer()
  private var mReconnectingProgressBar: ProgressBar? = null
  private var mReactApplicationContext: ReactApplicationContext? = null

  init {
    customViewer.loadingDelegate = this;
    mReactApplicationContext = reactApplicationContext
    val inflater = LayoutInflater.from(context)
    mainView = inflater.inflate(R.layout.native_view, this)

    mReconnectingProgressBar=
      mainView!!.findViewById<View>(R.id.progress_par) as ProgressBar?
    surfaceView=
      mainView!!.findViewById<View>(R.id.surface_view) as SurfaceView?

    //surfaceView!!.setZOrderOnTop(true)
    surfaceView!!.setBackgroundColor(Color.TRANSPARENT)
    surfaceView!!.getHolder().setFormat(PixelFormat.TRANSLUCENT)

  }

  fun setModelViewerColor(color: Int){
    surfaceView!!.setBackgroundColor(color)
    customViewer.setModelViewerColor(color)
  }
fun loadGlbUrl(url:String){
  customViewer.run {
    loadEntity()
    setSurfaceView(requireNotNull(surfaceView),context)
    loadGlb(url)
  }
}
  private fun readAsset(context: Context, assetName: String): ByteBuffer {
    val input = context.assets.open(assetName)
    val bytes = ByteArray(input.available())
    input.read(bytes)
    return ByteBuffer.wrap(bytes)
  }
  fun setProgressColor(color:String){
    mReconnectingProgressBar?.setProgressTintList(ColorStateList.valueOf(Color.parseColor(color)));
  }
  override fun onFinish() {
    surfaceView?.visibility =VISIBLE
    Handler(Looper.getMainLooper()).postDelayed(
      {
       // surfaceView!!.setZOrderOnTop(true)
        surfaceView!!.setBackgroundColor(Color.TRANSPARENT)
        surfaceView!!.getHolder().setFormat(PixelFormat.TRANSLUCENT)
        mReconnectingProgressBar?.visibility =INVISIBLE
      },
      1000 // value in milliseconds
    )
  }

  // life cycle ------------------------------------
  override fun getLifecycle(): Lifecycle {
    Log.d(TAG, "lifecycleRegistry=${lifecycleRegistry.currentState}")
    return lifecycleRegistry

  }

  override fun onStartTemporaryDetach() {
    super.onStartTemporaryDetach()
    customViewer.onPause()

    Log.d(TAG, "onStartTemporaryDetach")
  }

  override fun onAttachedToWindow() {
    Log.d(TAG, "onAttachedToWindow")
    customViewer.onResume()

    super.onAttachedToWindow()
  }

  override fun onDetachedFromWindow() {
    Log.d(TAG, "onAttachedToWindow")
    customViewer.onDestroy()
    super.onDetachedFromWindow()
  }

  override fun onFinishInflate() {
    Log.d(TAG, "onFinishInflate")

    super.onFinishInflate()
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    Log.d(TAG, "onLayout")

    super.onLayout(changed, l, t, r, b)
  }

  override fun onViewAdded(child: View?) {
    Log.d(TAG, "onViewAdded")
    super.onViewAdded(child)
  }

  override fun onSaveInstanceState(): Parcelable? {
    Log.d(TAG, "onSaveInstanceState")
    return super.onSaveInstanceState()
  }
}
