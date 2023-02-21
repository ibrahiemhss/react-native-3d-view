package com.modelviewer

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PixelFormat
import android.opengl.Matrix
import android.os.AsyncTask
import android.os.Parcelable
import android.util.Log
import android.view.*
import android.view.GestureDetector
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.facebook.react.bridge.*
import com.google.android.filament.*
import com.google.android.filament.utils.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.*


@SuppressLint("MissingInflatedId")
class DirectNativeView(
  context: Context,
  reactApplicationContext: ReactApplicationContext) :
  RelativeLayout(context),LifecycleOwner,Loading, AsyncResponse {
  var asyncTask: MyAsyncTask = MyAsyncTask()
  private val animator = ValueAnimator.ofFloat(1.0f, 360.0f)

  private lateinit var choreographer: Choreographer
  private val frameScheduler = FrameCallback()
  private lateinit var modelViewer: ModelViewer

  // private lateinit var titlebarHint: TextView
  private val doubleTapListener = DoubleTapListener()
  private lateinit var doubleTapDetector: GestureDetector
  private var remoteServer: RemoteServer? = null
  private var statusToast: Toast? = null
  private var statusText: String? = null
  private var latestDownload: String? = null
  private val automation = AutomationEngine()
  private var loadStartTime = 0L
  private var loadStartFence: Fence? = null
  private val viewerContent = AutomationEngine.ViewerContent()
  private var mReconnectingProgressBar: ProgressBar? = null

  private lateinit var lifecycleRegistry: LifecycleRegistry
  private val TAG = "NativeGlbView"
  var mainView: View? = null
  var surfaceView: SurfaceView? = null
  private var mReactApplicationContext: ReactApplicationContext? = null

  companion object {
    // Load the library for the utility layer, which in turn loads gltfio and the Filament core.
    init {
      Utils.init()
    }

    private const val TAG = "gltf-viewer"
  }

  init {
    asyncTask.delegate = this;
    mReactApplicationContext = reactApplicationContext
    val inflater = LayoutInflater.from(context)
    //context.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    mainView = inflater.inflate(R.layout.native_view, this)

    mReconnectingProgressBar =
      mainView!!.findViewById<View>(R.id.progress_par) as ProgressBar?
    surfaceView =
      mainView!!.findViewById<View>(R.id.surface_view) as SurfaceView?

    surfaceView!!.setZOrderOnTop(true)
    surfaceView!!.setBackgroundColor(Color.TRANSPARENT)
    surfaceView!!.getHolder().setFormat(PixelFormat.TRANSLUCENT)


    choreographer = Choreographer.getInstance()

    doubleTapDetector = GestureDetector(context, doubleTapListener)

    modelViewer = ModelViewer(surfaceView!!)
    viewerContent.view = modelViewer.view
    viewerContent.sunlight = modelViewer.light
    viewerContent.lightManager = modelViewer.engine.lightManager
    viewerContent.scene = modelViewer.scene
    viewerContent.renderer = modelViewer.renderer

    surfaceView!!.setOnTouchListener { _, event ->
      modelViewer.onTouchEvent(event)
      doubleTapDetector.onTouchEvent(event)
      true
    }
    modelViewer.scene.skybox =
      Skybox.Builder().color(0.035f, 0.035f, 0.035f, 1.0f).build(modelViewer.engine)
   // createDefaultRenderables()
      createIndirectLight()

    setStatusText("To load a new model, go to the above URL on your host machine.")

    val view = modelViewer.view

    /*
     * Note: The settings below are overriden when connecting to the remote UI.
     */

    // on mobile, better use lower quality color buffer
    view.renderQuality = view.renderQuality.apply {
      hdrColorBuffer = com.google.android.filament.View.QualityLevel.MEDIUM
    }

    // dynamic resolution often helps a lot
    view.dynamicResolutionOptions = view.dynamicResolutionOptions.apply {
      enabled = true
      quality = com.google.android.filament.View.QualityLevel.MEDIUM
    }

    // MSAA is needed with dynamic resolution MEDIUM
    view.multiSampleAntiAliasingOptions = view.multiSampleAntiAliasingOptions.apply {
      enabled = true
    }

    // FXAA is pretty cheap and helps a lot
    view.antiAliasing = com.google.android.filament.View.AntiAliasing.FXAA

    // ambient occlusion is the cheapest effect that adds a lot of quality
    view.ambientOcclusionOptions = view.ambientOcclusionOptions.apply {
      enabled = true
    }

    // bloom is pretty expensive but adds a fair amount of realism
    view.bloomOptions = view.bloomOptions.apply {
      enabled = true
    }

    remoteServer = RemoteServer(8082)

  }

  fun loadGlbUrl(url: String) {
    asyncTask.execute(url).get();
  }

  private fun createDefaultRenderables() {
    val buffer = context.assets.open("models/scene.gltf").use { input ->
      val bytes = ByteArray(input.available())
      input.read(bytes)
      ByteBuffer.wrap(bytes)
    }

    modelViewer.loadModelGltfAsync(buffer) { uri -> readCompressedAsset("models/$uri") }
    updateRootTransform()
  }

  private fun createIndirectLight() {
    val scene = modelViewer.scene
    val ibl = "default_env"

    readCompressedAsset("envs/$ibl/${ibl}_ibl.ktx").let {
      KTXLoader.createIndirectLight(modelViewer.engine, it).apply {
        intensity = 20_000.0f
        viewerContent.indirectLight = modelViewer.scene.indirectLight
        modelViewer.scene.indirectLight = this

      }
    }
    readCompressedAsset("envs/$ibl/${ibl}_skybox.ktx").let {
      KTXLoader.createSkybox(modelViewer.engine, it).apply {
        //scene.skybox = this
      }
    }
  }

  fun setModelViewerColor(color:Int){
    // val intColor= Color.parseColor(color);
    val r: Float = (color shr 16 and 0xff) / 255.0f
    val g: Float = (color shr 8 and 0xff) / 255.0f
    val b: Float = (color and 0xff) / 255.0f
    val a: Float = (color shr 24 and 0xff) / 255.0f

    modelViewer.scene.skybox?.setColor(r, g, b, a) //White color

  }
  fun setProgressColor(color:String){
    mReconnectingProgressBar?.setProgressTintList(ColorStateList.valueOf(Color.parseColor(color)));
  }
  fun loadIndirectLight() {
    val ibl = "default_env"
    // Create the indirect light source and add it to the scene.
    val buffer =readCompressedAsset( "envs/$ibl/${ibl}_ibl.ktx")
    buffer.let {
      KTXLoader.createIndirectLight(modelViewer.engine, it).apply {
        intensity = 30_000.0f
        //modelViewer.viewerContent.indirectLight = modelViewer.scene.indirectLight
        /*  val light = EntityManager.get().create()
          val lm = modelViewer.engine.lightManager

          val lightInstance = lm.getInstance(light)
          val (r, g, b) = Colors.cct(9_500.0f)
          LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(r, g, b)
            .intensity(100_000.0f)
            .direction(0.0f, -1.0f, 0.0f)
            .castShadows(true)
            .build(modelViewer.engine, light)
  */
        // modelViewer.scene.addEntity(light)
        modelViewer.scene.indirectLight = this
      }
    }
  }
  private fun readCompressedAsset(assetName: String): ByteBuffer {
    val input = context.assets.open(assetName)
    val bytes = ByteArray(input.available())
    input.read(bytes)
    return ByteBuffer.wrap(bytes)
  }


  private fun clearStatusText() {
    statusToast?.let {
      it.cancel()
      statusText = null
    }
  }

  private fun setStatusText(text: String) {
    onMainThread {
      if (statusToast == null || statusText != text) {
        statusText = text
        statusToast = Toast.makeText(context, text, Toast.LENGTH_SHORT)
        statusToast!!.show()

      }
    }
  }

  private fun loadGlb(buffer:Buffer) {
    onMainThread {
      modelViewer.destroyModel()
      modelViewer.loadModelGlb(buffer)
      updateRootTransform()
      loadStartTime = System.nanoTime()
      loadStartFence = modelViewer.engine.createFence()
    //startAnimation()
    }
    mReconnectingProgressBar?.visibility =INVISIBLE
    surfaceView?.visibility =VISIBLE

  }
  private fun startAnimation() {
    // Animate the triangle
    animator.interpolator = LinearInterpolator()
    animator.duration = 4000
    animator.repeatMode = ValueAnimator.RESTART
    animator.repeatCount = ValueAnimator.INFINITE
    animator.addUpdateListener(object : ValueAnimator.AnimatorUpdateListener {
      val transformMatrix = FloatArray(16)
      override fun onAnimationUpdate(a: ValueAnimator) {
        //Matrix.setRotateM(transformMatrix, 0, -(a.animatedValue as Float), 20.0f, 2.0f, 180.0f)
        Matrix.setRotateM(transformMatrix, 0,( a .animatedValue as Float  *10.583f) , 0.0f, 1.0f, 0.0f)
        val tcm = modelViewer.engine.transformManager

        tcm.setTransform(tcm.getInstance(modelViewer.asset!!.root), transformMatrix)

      }
    })
    animator.start()
  }
  private suspend fun loadHdr(message: RemoteServer.ReceivedMessage) {
    onMainThread {
      val engine = modelViewer.engine
      val equirect = HDRLoader.createTexture(engine, message.buffer)
      if (equirect == null) {
        setStatusText("Could not decode HDR file.")
      } else {
        setStatusText("Successfully decoded HDR file.")

        val context = IBLPrefilterContext(engine)
        val equirectToCubemap = IBLPrefilterContext.EquirectangularToCubemap(context)
        val skyboxTexture = equirectToCubemap.run(equirect)!!
        engine.destroyTexture(equirect)

        val specularFilter = IBLPrefilterContext.SpecularFilter(context)
        val reflections = specularFilter.run(skyboxTexture)

        val ibl = IndirectLight.Builder()
          .reflections(reflections)
          .intensity(30000.0f)
          .build(engine)

        val sky = Skybox.Builder().environment(skyboxTexture).build(engine)

        specularFilter.destroy()
        equirectToCubemap.destroy()
        context.destroy()

        // destroy the previous IBl
        engine.destroyIndirectLight(modelViewer.scene.indirectLight!!)
        engine.destroySkybox(modelViewer.scene.skybox!!)

        modelViewer.scene.skybox = sky
        modelViewer.scene.indirectLight = ibl
        viewerContent.indirectLight = ibl
      }
    }
  }


  // life cycle ------------------------------------
  override fun getLifecycle(): Lifecycle {
    Log.d(TAG, "lifecycleRegistry=${lifecycleRegistry.currentState}")
    return lifecycleRegistry

  }

  fun loadModelData(message: RemoteServer.ReceivedMessage) {
    Log.i(TAG, "Downloaded model ${message.label} (${message.buffer.capacity()} bytes)")
    clearStatusText()
    onMainThread {
     // loadGlb(message)
    }
  }

  fun loadSettings(message: RemoteServer.ReceivedMessage) {
    val json = StandardCharsets.UTF_8.decode(message.buffer).toString()
    viewerContent.assetLights = modelViewer.asset?.lightEntities
    //automation.applySettings(modelViewer.engine, json, viewerContent)
    modelViewer.view.colorGrading = automation.getColorGrading(modelViewer.engine)
    modelViewer.cameraFocalLength = automation.viewerOptions.cameraFocalLength
    //  modelViewer.cameraNear = automation.viewerOptions.cameraNear
    // modelViewer.cameraFar = automation.viewerOptions.cameraFar
    updateRootTransform()
  }

  private fun updateRootTransform() {
    if (automation.viewerOptions.autoScaleEnabled) {
      modelViewer.transformToUnitCube()
    } else {
      modelViewer.clearRootTransform()
    }
  }

  inner class FrameCallback : Choreographer.FrameCallback {
    private val startTime = System.nanoTime()
    override fun doFrame(frameTimeNanos: Long) {
      choreographer.postFrameCallback(this)

      loadStartFence?.let {
        if (it.wait(Fence.Mode.FLUSH, 0) == Fence.FenceStatus.CONDITION_SATISFIED) {
          val end = System.nanoTime()
          val total = (end - loadStartTime) / 1_000_000
          Log.i(TAG, "The Filament backend took $total ms to load the model geometry.")
          modelViewer.engine.destroyFence(it)
          loadStartFence = null
        }
      }

      modelViewer.animator?.apply {
        if (animationCount > 0) {
          val elapsedTimeSeconds = (frameTimeNanos - startTime).toDouble() / 1_000_000_000
          applyAnimation(0, elapsedTimeSeconds.toFloat())
        }
        updateBoneMatrices()
      }

      modelViewer.render(frameTimeNanos)

      // Check if a new download is in progress. If so, let the user know with toast.
      val currentDownload = remoteServer?.peekIncomingLabel()
      if (RemoteServer.isBinary(currentDownload) && currentDownload != latestDownload) {
        latestDownload = currentDownload
        Log.i(TAG, "Downloading $currentDownload")
        setStatusText("Downloading $currentDownload")
      }

      // Check if a new message has been fully received from the client.
      val message = remoteServer?.acquireReceivedMessage()
      if (message != null) {
        if (message.label == latestDownload) {
          latestDownload = null
        }
        if (RemoteServer.isJson(message.label)) {
          loadSettings(message)
        } else {
         // loadModelData(message)
        }
      }
    }
  }

  // Just for testing purposes, this releases the current model and reloads the default model.
  inner class DoubleTapListener : GestureDetector.SimpleOnGestureListener() {
    override fun onDoubleTap(e: MotionEvent): Boolean {
      modelViewer.destroyModel()
      createDefaultRenderables()
      return super.onDoubleTap(e)
    }
  }

  override fun onAttachedToWindow() {
    Log.d(TAG, "onAttachedToWindow")
    super.onAttachedToWindow()
    choreographer.postFrameCallback(frameScheduler)
  }

  override fun onStartTemporaryDetach() {
    super.onStartTemporaryDetach()
    choreographer.removeFrameCallback(frameScheduler)
    Log.d(TAG, "onStartTemporaryDetach")
  }


  override fun onDetachedFromWindow() {
    Log.d(TAG, "onDetachedFromWindow")
    super.onDetachedFromWindow()
    choreographer.removeFrameCallback(frameScheduler)
    remoteServer?.close()
  }

  override fun onFinishInflate() {
    Log.d(TAG, "onFinishInflate")

    super.onFinishInflate()
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    Log.d(TAG, "onLayout")

    super.onLayout(changed, l, t, r, b)
  }


  override fun onSaveInstanceState(): Parcelable? {
    Log.d(TAG, "onSaveInstanceState")
    return super.onSaveInstanceState()
  }
  override fun processFinish(output: Buffer) {
    try {
      onMainThread {

        loadGlb(output)
       // modelViewer.loadModelGltfAsync(output) { uri -> readCompressedAsset("models/$uri") }
        updateRootTransform()
      }
    } catch (e:Exception){
      Log.e("Exception modelViewer", e.message.toString())
    }

  }
  override fun onFinish() {
    TODO("Not yet implemented")
  }

}

interface AsyncResponse {
  fun processFinish(output: Buffer)
}

class MyAsyncTask : AsyncTask<String?, Void?, String?>() {
  var delegate: AsyncResponse? = null

  @Deprecated("Deprecated in Java")
  override fun doInBackground(vararg params: String?): String {
    try {

      val url =
        URL(params[0])
      val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
      urlConnection.connect()
      val inputStream = BufferedInputStream(urlConnection.getInputStream())
      val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
      val byteArrayOutputStream = ByteArrayOutputStream()
      var bytesRead: Int
      while ((inputStream.read(buffer).also { bytesRead = it }) != -1) {
        byteArrayOutputStream.write(buffer, 0, bytesRead)
      }
      val byteArr = byteArrayOutputStream.toByteArray()
      val byteBuffer = ByteBuffer.wrap(byteArr)
      delegate?.processFinish(byteBuffer)

    } catch (e: Exception) {
      e.printStackTrace()
    }
    return ""
  }

  @Deprecated("Deprecated in Java")
  override fun onPostExecute(result: String?) {
    if (result != null) {
      Log.e("xx", result)
    }
    // how do I pass this result back to the thread, that created me?
  }
}

