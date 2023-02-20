package com.modelviewer

import android.os.AsyncTask
import android.util.Log
import android.view.Choreographer
import android.view.SurfaceView
import com.google.android.filament.Skybox
import com.google.android.filament.utils.ModelViewer
import com.google.android.filament.utils.Utils
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.Buffer
import java.nio.ByteBuffer


class CustomViewer: AsyncResponse {
  var loadingDelegate: Loading? = null
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

    fun setSurfaceView(mSurfaceView: SurfaceView) {
        asyncTask.delegate = this;
        modelViewer = ModelViewer(mSurfaceView)
        mSurfaceView.setOnTouchListener(modelViewer)
    //  modelViewer.view.blendMode = com.google.android.filament.View.BlendMode.TRANSLUCENT
     // modelViewer.scene.skybox = null
        //Skybox and background color
        //without this part the scene'll appear broken
        modelViewer.scene.skybox = Skybox.Builder().build(modelViewer.engine)
        modelViewer.scene.skybox?.setColor(1.0f, 1.0f, 1.0f, 1.0f) //White color
    }

fun setModelViewerColor(color:Int){
 // val intColor= Color.parseColor(color);
  val r: Float = (color shr 16 and 0xff) / 255.0f
  val g: Float = (color shr 8 and 0xff) / 255.0f
  val b: Float = (color and 0xff) / 255.0f
  val a: Float = (color shr 24 and 0xff) / 255.0f
  modelViewer.scene.skybox?.setColor(r, g, b, a) //White color

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



    override fun processFinish(output: Buffer) {
        try {
            onMainThread {
                modelViewer.apply {
                    loadModelGlb(output)
                    transformToUnitCube()
                  loadingDelegate?.onFinish()
                }
             /* Handler(Looper.getMainLooper()).postDelayed(
                {
                  loadingDelegate?.onFinish()
                },
                1000 // value in milliseconds
              )*/
            }
        } catch (e:Exception){
            Log.e("Exception modelViewer", e.message.toString())
        }

    }
}

interface Loading {
  fun onFinish()
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
