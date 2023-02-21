//
//  GameViewController.swift
//  GLTFSceneKitSampler
//
//  Created by magicien on 2017/08/26.
//  Copyright © 2017年 DarkHorse. All rights reserved.
//

import UIKit
import QuartzCore
import GLTFSceneKit
import SceneKit

class ModelViewController: UIViewController {

    static var viewRect = CGRectMake(0, 0, 100, 100)
    static var scenViewRect = CGRectMake(0, 0, 100, 100)

    var scnView : SCNView?
    var scene: SCNScene?
    var scnViewColor :UIColor = UIColor.gray
    func startLoad( url :String,rect :CGRect){
        ModelViewController.viewRect=rect
        DispatchQueue.main.async {
          self.scnView  = SCNView(frame: rect, options: nil)
          self.view = self.scnView
          self.view.frame=rect
        //self.scnView = self.view as? SCNView
       // self.view.frame=ModelViewController.viewRect
       // self.scnView?.frame=ModelViewController.viewRect
      
            
       // initiateBackgroundWork(stringUrl: url)
       var scene: SCNScene
       
        do {
            let sceneSource = try GLTFSceneSource(url: URL.init(string: url)!)
            scene = try sceneSource.scene()
        } catch {
            print("\(error.localizedDescription)")
            return
        }
     
            //scene.rootNode.scale = SCNVector3(0.8, 0.8, 0.8)
            //scene.rootNode.position = SCNVector3(x: 0, y:0.02, z:0)
          //  scene.rootNode.scale = SCNVector3(rect.width*0.002, rect.height*0.0012, scene.fogDensityExponent)
           // scene.rootNode.position = SCNVector3(x: 0, y:Float(rect.height)*0.00002, z:0)
            //scene.rootNode.constraints = CGRect(x: 0, y:0, width: rect.width/2, height: rect.height/2)
        self.scnView?.scene  = scene
        self.scene = scene
          

           //to give nice reflections :)
          // scene.lightingEnvironment.contents = "art.scnassets/shinyRoom.jpg"
         scene.lightingEnvironment.intensity = 1;

        self.scnView?.autoenablesDefaultLighting = true

        // allows the user to manipulate the camera
        self.scnView?.allowsCameraControl = true

        // show statistics such as fps and timing information
        //self.gameView!.showsStatistics = true

        // configure the view
            self.scnView?.backgroundColor = self.scnViewColor

        self.scnView?.delegate = self
            if #available(iOS 13.0, *) {
                self.scnView?.scalesLargeContentImage = true
            } else {
                // Fallback on earlier versions
            };

        }
        
    }
      
    func setScnColor( color :UIColor){
      
        scnViewColor = color

       // self.scnView!.delegate = self
        
    }
    
    func setLoadingColor( color :UIColor){
      
        //self.scnView!.backgroundColor = color
       // self.scnView!.delegate = self
        
    }
    
    func initiateBackgroundWork(stringUrl: String) {
        let dispatchSemaphore = DispatchSemaphore(value: 0)
        let backgroundQueue = DispatchQueue(label: "background_queue",
                                            qos: .background)
        
        backgroundQueue.async {
            // Perform work on a separate thread at background QoS and
            // signal when the work completes.
            
            
            var scene: SCNScene
             do {
                 let sceneSource = try GLTFSceneSource(url: URL.init(string: stringUrl)!)
                 scene = try sceneSource.scene()
             } catch {
                 print("\(error.localizedDescription)")
                 return
             }
          
            
             self.scnView?.scene  = scene
             self.scene = scene
               

                //to give nice reflections :)
               // scene.lightingEnvironment.contents = "art.scnassets/shinyRoom.jpg"
              scene.lightingEnvironment.intensity = 2;

             self.scnView?.autoenablesDefaultLighting = true

             // allows the user to manipulate the camera
             self.scnView?.allowsCameraControl = true

             // show statistics such as fps and timing information
             //self.gameView!.showsStatistics = true

             // configure the view
             self.scnView?.backgroundColor = UIColor.gray

             self.scnView?.delegate = self
            //doBackgroundWorkAsync {
            dispatchSemaphore.signal()
            // }
            
            _ = dispatchSemaphore.wait(timeout: DispatchTime.distantFuture)
            
            //DispatchQueue.main.async { [weak self] in
               // self?.label.text = "Background work completed"
            //}
        }
    }
  
    func setModelFromStringrURL(stringUrl: String) {
        if let url = URL(string: stringUrl) {
        URLSession.shared.dataTask(with: url) { (data, response, error) in
          // Error handling...
          guard let modelData = data else { return }

            DispatchQueue.global(qos: .userInitiated).async  {
              var scene: SCNScene

              do {
                  let sceneSource = try GLTFSceneSource(data:modelData)
                  scene = try sceneSource.scene()
              } catch {
                  print("\(error.localizedDescription)")
                  return
              }
           
             
              self.scnView?.scene  = scene
              self.scene = scene
                

                 //to give nice reflections :)
                // scene.lightingEnvironment.contents = "art.scnassets/shinyRoom.jpg"
              scene.lightingEnvironment.intensity = 2;

              self.scnView?.autoenablesDefaultLighting = true

              // allows the user to manipulate the camera
              self.scnView?.allowsCameraControl = true

              // show statistics such as fps and timing information
              //self.gameView!.showsStatistics = true

              // configure the view
              self.scnView?.backgroundColor = UIColor.gray

              self.scnView?.delegate = self
          }
        }.resume()
      }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
      
    }


    override var shouldAutorotate: Bool {
        return true
    }

    override var prefersStatusBarHidden: Bool {
        return true
    }

    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        if UIDevice.current.userInterfaceIdiom == .phone {
            return .allButUpsideDown
        } else {
            return .all
        }
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Release any cached data, images, etc that aren't in use.
    }

}

extension ModelViewController: SCNSceneRendererDelegate {
  func renderer(_ renderer: SCNSceneRenderer, didApplyAnimationsAtTime time: TimeInterval) {
   // self.scene?.rootNode.updateVRMSpringBones(time: time)
  }
}
