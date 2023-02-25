package com.modelviewer

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.facebook.react.bridge.*
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.sceneform.*
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.QuaternionEvaluator
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.FootprintSelectionVisualizer
import com.google.ar.sceneform.ux.TransformableNode
import com.google.ar.sceneform.ux.TransformationSystem
import com.nouman.sceneview.nodes.DragTransformableNode
import java.util.*
import java.util.concurrent.CompletionException


@SuppressLint("MissingInflatedId")
class RNSceneView(
  context: Context,
  reactApplicationContext: ReactApplicationContext
) :
  RelativeLayout(context), LifecycleOwner {
  lateinit var mainHandler: Handler
  val transformationSystem = makeTransformationSystem()
  var dragTransformableNode = DragTransformableNode(1f, transformationSystem)

  private lateinit var lifecycleRegistry: LifecycleRegistry
  private val TAG = "NativeGlbView"
  var mainView: View? = null

  //var surfaceView: SurfaceView? = null
  var sceneView: SceneView? = null
  private var mReconnectingProgressBar: ProgressBar? = null
  private var mReactApplicationContext: ReactApplicationContext? = null
  var secondsLeft = 0
  private val updateAnaimationTask = object : Runnable {
    override fun run() {
      minusOneSecond()
      mainHandler.postDelayed(this, 1000)
    }
  }

  fun minusOneSecond() {
    if (secondsLeft > 0) {
      secondsLeft -= 1
      // val camera = transformableNode!!.scene?.camera

    }
  }

  init {

    mReactApplicationContext = reactApplicationContext
    val inflater = LayoutInflater.from(context)
    mainView = inflater.inflate(R.layout.native_view, this)

    mReconnectingProgressBar =
      mainView!!.findViewById<View>(R.id.progress_par) as ProgressBar?
    sceneView =
      mainView!!.findViewById<View>(R.id.sceneView) as SceneView?
    this.sceneView!!.minimumWidth = width
    this.sceneView!!.minimumHeight = height
  }

  fun setModelViewerColor(color: Int) {
    sceneView!!.setBackgroundColor(color)
    // customViewer.setModelViewerColor(color)
  }

  @RequiresApi(Build.VERSION_CODES.N)
  fun setModelUri(url: String) {
    renderRemoteObject(url);
  }

  fun setProgressColor(color: String) {
    mReconnectingProgressBar?.setProgressTintList(ColorStateList.valueOf(Color.parseColor(color)));
  }


  // life cycle ------------------------------------
  override fun getLifecycle(): Lifecycle {
    Log.d(TAG, "lifecycleRegistry=${lifecycleRegistry.currentState}")
    return lifecycleRegistry

  }

  override fun onStartTemporaryDetach() {
    super.onStartTemporaryDetach()


    Log.d(TAG, "onStartTemporaryDetach")
  }

  override fun onAttachedToWindow() {
    Log.d(TAG, "onAttachedToWindow")
    super.onAttachedToWindow()
    try {
      sceneView!!.resume()
    } catch (e: CameraNotAvailableException) {
      e.printStackTrace()
    }
  }

  override fun onDetachedFromWindow() {
    Log.d(TAG, "onAttachedToWindow")
    super.onDetachedFromWindow()
    try {

      sceneView!!.destroy()
    } catch (e: Exception) {
      e.printStackTrace()
    }
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

  @RequiresApi(Build.VERSION_CODES.N)
  private fun renderRemoteObject(url: String) {

    mReconnectingProgressBar!!.setVisibility(View.VISIBLE)
    ModelRenderable.builder()
      .setSource(
        context, RenderableSource.Builder().setSource(
          context,
          Uri.parse(url),
          RenderableSource.SourceType.GLB
        ).setScale(0.15f)
          .setRecenterMode(RenderableSource.RecenterMode.CENTER)
          .build()
      )
      .setRegistryId(url)
      .build()
      .thenAccept { modelRenderable: ModelRenderable ->
        mReconnectingProgressBar!!.setVisibility(View.GONE)
        addNodeToScene(modelRenderable)


      }
      .exceptionally { throwable: Throwable? ->
        var message: String?
        message = if (throwable is CompletionException) {
          mReconnectingProgressBar!!.setVisibility(View.GONE)
          "Internet is not working"
        } else {
          mReconnectingProgressBar!!.setVisibility(View.GONE)
          "Can't load Model"
        }
        val mainHandler = Handler(Looper.getMainLooper())
        val finalMessage: String = message
        val myRunnable = Runnable {
          AlertDialog.Builder(context)
            .setTitle("Error")
            .setMessage(finalMessage + "")
            .setPositiveButton("Retry") { dialogInterface: DialogInterface, _: Int ->
              renderRemoteObject(url)
              dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface, _ -> dialogInterface.dismiss() }
            .show()
        }
        mainHandler.post(myRunnable)
        null
      }

  }

  @RequiresApi(Build.VERSION_CODES.N)
  private fun addNodeToScene(model: ModelRenderable) {
    if (sceneView != null) {
      dragTransformableNode.renderable = model
      sceneView!!.getScene().addChild(dragTransformableNode)
      dragTransformableNode.select()
      sceneView!!.getScene()
        .addOnPeekTouchListener { hitTestResult: HitTestResult?, motionEvent: MotionEvent? ->
          transformationSystem.onTouch(
            hitTestResult,
            motionEvent
          )
        }

     // val nodeAnimator = createAnimator();
     // nodeAnimator.setTarget(this);
     // nodeAnimator.setDuration(4);
     // nodeAnimator.start();

    }

  }

  private fun makeTransformationSystem(): TransformationSystem {
    val footprintSelectionVisualizer = FootprintSelectionVisualizer()
    return TransformationSystem(resources.displayMetrics, footprintSelectionVisualizer)
  }

  private fun createAnimator(): ObjectAnimator {
    // Node's setLocalRotation method accepts Quaternions as parameters.
    // First, set up orientations that will animate a circle.
    val orientation1 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 0f)
    val orientation2 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 120f)
    val orientation3 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 240f)
    val orientation4 = Quaternion.axisAngle(Vector3(0.0f, 1.0f, 0.0f), 360f)
    val orbitAnimation = ObjectAnimator()
    orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4)

    // Next, give it the localRotation property.
    orbitAnimation.setPropertyName("localRotation")
    dragTransformableNode.localRotation = orientation1
    dragTransformableNode.localRotation = orientation2
    dragTransformableNode.localRotation = orientation3
    dragTransformableNode.localRotation = orientation4

    // Use Sceneform's QuaternionEvaluator.
    orbitAnimation.setEvaluator(QuaternionEvaluator())

    //  Allow orbitAnimation to repeat forever
    orbitAnimation.repeatCount = ObjectAnimator.INFINITE
    orbitAnimation.repeatMode = ObjectAnimator.RESTART
    orbitAnimation.interpolator = LinearInterpolator()
    orbitAnimation.setAutoCancel(true)

    return orbitAnimation
  }

}
