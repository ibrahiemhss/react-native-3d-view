package com.modelviewer

import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager


class RNViewPackage : ReactPackage {

  override fun createNativeModules(p0: ReactApplicationContext): List<NativeModule> {
    return ArrayList()
  }

  override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
    return listOf(RNViewManager(reactContext))
  }
}
