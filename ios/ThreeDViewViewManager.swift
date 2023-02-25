
import UIKit
import QuartzCore
import SceneKit
import GLTFSceneKit
import AVFoundation
import React

@objc(ThreeDViewViewManager)
class ThreeDViewViewManager: RCTViewManager {

  override func view() -> (ThreeDViewView) {
    return ThreeDViewView()
  }

    func methodQueue() -> DispatchQueue {
            return bridge.uiManager.methodQueue
        }
        
        @objc override static func requiresMainQueueSetup() -> Bool {
            return true
        }
        
    }


class ThreeDViewView : UIView {

    var _rootController = RN3dViewController.init();
        var _url = String()
        var _numberOfLoad = 0
        var _rect = CGRectMake(0, 0, 300, 300)
        
        override public init(frame: CGRect) {
            super.init(frame: frame)
            addSubview(_rootController.view);
        }
        
        
        required init?(coder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
        
        
        override func layoutSubviews() {
            super.layoutSubviews()
            if(_numberOfLoad == 0){
                self._rect = CGRect(x: 0, y:0, width: frame.width, height: frame.height)
                self._rootController.startLoad(url: url,rect: self._rect)
                _numberOfLoad = 1
            }
        }
        
        
        @objc var color: String = "" {
            didSet {
                self._rootController.setScnColor(color:  hexStringToUIColor(hexColor: color))
                self.backgroundColor = hexStringToUIColor(hexColor: color)
            }
        }
        
        @objc var duration: NSInteger = 8 {
            didSet {
                self._rootController.setDuartion(duration: Double(duration))
            }
        }
        
        
        @objc var url: String = "" {
            didSet {
                //self._rect = CGRect(x: 0, y:0, width: frame.width, height: frame.height)
                // self._rootController.startLoad(url: url,rect: self._rect)
                _url=url
            }
        }
        
        
        @objc var loadingColor: String = "" {
            didSet {
                self._rootController.setLoadingColor(color:hexStringToUIColor(hexColor: loadingColor))
            }
        }
        
        
        func hexStringToUIColor(hexColor: String) -> UIColor {
            let stringScanner = Scanner(string: hexColor)
            
            if(hexColor.hasPrefix("#")) {
                stringScanner.scanLocation = 1
            }
            var color: UInt32 = 0
            stringScanner.scanHexInt32(&color)
            
            let r = CGFloat(Int(color >> 16) & 0x000000FF)
            let g = CGFloat(Int(color >> 8) & 0x000000FF)
            let b = CGFloat(Int(color) & 0x000000FF)
            
            return UIColor(red: r / 255.0, green: g / 255.0, blue: b / 255.0, alpha: 1)
        }
    }
