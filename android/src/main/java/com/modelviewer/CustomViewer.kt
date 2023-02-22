package com.modelviewer

import android.animation.ValueAnimator
import android.content.Context
import android.opengl.Matrix
import android.util.Log
import android.view.Choreographer
import android.view.SurfaceView
import android.view.animation.LinearInterpolator
import com.google.android.filament.*
import com.google.android.filament.utils.KTXLoader
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import com.gorisse.thomas.sceneform.Color
import com.gorisse.thomas.sceneform.light.color
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.channels.Channels


class CustomViewer() :AsyncResponse{
  var loadingDelegate: Loading? = null
  private val animator = ValueAnimator.ofFloat(0.0f, 360.0f)
  private lateinit var renderer: Renderer
  @Entity private var renderable = 0
  private lateinit var vertexBuffer: VertexBuffer
  private lateinit var indexBuffer: IndexBuffer
  private lateinit var material: Material

  //private var mReactApplicationContext: ReactApplicationContext? = null
  var  mContext: Context?=null
  var asyncTask: MyAsyncTask = MyAsyncTask()
    companion object {
      init {
            Utils.init()
        }
    }

    private lateinit var choreographer: Choreographer
    private lateinit var modelViewer: ModelViewer

    fun loadEntity() {
        choreographer = Choreographer.getInstance()
    }

    fun setSurfaceView(mSurfaceView: SurfaceView,context: Context) {
        asyncTask.delegate = this;
        mContext=context;
        modelViewer = ModelViewer(mSurfaceView)
        mSurfaceView.setOnTouchListener(modelViewer)
       // modelViewer.makeItTransparent()
      //modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.OPAQUE
      //modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)

      //  modelViewer.scene.skybox =Skybox.Builder().color(0.035f, 0.035f, 0.035f, 1.0f).build(modelViewer.engine)
        renderer = modelViewer.engine.createRenderer()
      // clear the swapchain with transparent pixels
  //    val options = renderer.clearOptions
    //  options.clear = true
    //  renderer.clearOptions = options
     // mContext?.let { setupScene(it) }

      //Skybox.Builder().build(modelViewer.engine)
      //  modelViewer.scene.skybox?.setColor(1.0f, 1.0f, 1.0f, 1.0f) //White color
    }
fun setModelViewerColor(color:Int){
  val r = (color shr 16 and 0xff) / 255.0f
  val g = (color shr 8 and 0xff) / 255.0f
  val b = (color and 0xff) / 255.0f
  val a = (color shr 24 and 0xff) / 255.0f
  modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.OPAQUE
  //modelViewer.scene.skybox?.setColor(r,g,b,a)
  modelViewer.scene.skybox =Skybox.Builder().color(r,g,b,a).build(modelViewer.engine)
  modelViewer.light.color= Color(color.toFloat())
  //modelViewer.scene.skybox =Skybox.Builder().color(R.toFloat(), G.toFloat(), B.toFloat(), A.toFloat()).build(modelViewer.engine)

}
    fun loadGlb( url: String) {
        asyncTask.execute(url).get();
    }


    private val frameCallback = object : Choreographer.FrameCallback {
        private val startTime = System.nanoTime()
        override fun doFrame(currentTime: Long) {
            val seconds = (currentTime - startTime).toDouble() / 1_000_000_000
            choreographer.postFrameCallback(this)
            modelViewer.animator?.apply {
                if (animationCount > 0) {
                    applyAnimation(0, seconds.toFloat())
                }
                updateBoneMatrices()
            }
            modelViewer.render(currentTime)
        }
    }

    fun onResume() {
        choreographer.postFrameCallback(frameCallback)
    }

    fun onPause() {
        choreographer.removeFrameCallback(frameCallback)
    }

    fun onDestroy() {
        choreographer.removeFrameCallback(frameCallback)
    }



  /*  override fun processFinish(output: Buffer) {
        try {
            onMainThread {
                modelViewer.apply {
                    loadModelGlb(output)
                    transformToUnitCube()
                  loadingDelegate?.onFinish()
                }
              startAnimation()
              //loadEnviroment("venetian_crossroads_2k");
              loadIndirectLight( "venetian_crossroads_2k")
            }
        } catch (e:Exception){
            Log.e("Exception modelViewer", e.message.toString())
        }

    }*/

  private fun loadMaterial(context: Context) {
    readUncompressedAsset(context,"materials/baked_color.mat").let {
      material = Material.Builder().payload(it, it.remaining()).build(modelViewer.engine)
    }
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
        Matrix.setRotateM(transformMatrix, 0, -(a.animatedValue as Float), 0.0f, 1.0f, 1.0f)
        val tcm = modelViewer.engine.transformManager
        tcm.setTransform(tcm.getInstance(modelViewer.asset?.root!!), transformMatrix)

      }
    })
    animator.start()
  }

  fun loadEnviroment(ibl: String) {
    // Create the sky box and add it to the scene.
    val buffer = mContext?.let { readAsset(it, "environments/venetian_crossroads_2k/${ibl}_skybox.ktx") }
    buffer?.let {
      KTXLoader.createSkybox(modelViewer.engine, it).apply {
        modelViewer.scene.skybox = this
    }
    }
  }

  fun loadIndirectLight() {
    val ibl = "default_env"
    // Create the indirect light source and add it to the scene.
    val buffer = mContext?.let { readAsset(it, "envs/$ibl/${ibl}_ibl.ktx") }
    buffer?.let {
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
  private fun readAsset(context: Context, assetName: String): ByteBuffer {
    val input = context.assets.open(assetName)
    val bytes = ByteArray(input.available())
    input.read(bytes)
    return ByteBuffer.wrap(bytes)
  }

  private fun readUncompressedAsset(context: Context,assets: String): ByteBuffer {
    context.assets.openFd(assets).use { fd ->
      val input = fd.createInputStream()
      val dst = ByteBuffer.allocate(fd.length.toInt())

      val src = Channels.newChannel(input)
      src.read(dst)
      src.close()

      return dst.apply { rewind() }
    }
  }

  override fun processFinish(output: Buffer) {
    try {
      onMainThread {
        modelViewer.apply {
          loadModelGlb(output)
          transformToUnitCube()
          loadingDelegate?.onFinish()
        }
       // startAnimation()
        //loadEnviroment("venetian_crossroads_2k");
        loadIndirectLight()
      }
    } catch (e:Exception){
      Log.e("Exception modelViewer", e.message.toString())
    }
  }
}

interface Loading {
  fun onFinish()
}
