
import UIKit
import QuartzCore
import GLTFSceneKit
import SceneKit

class RN3dViewController: UIViewController, CAAnimationDelegate {
    
    static var viewRect = CGRectMake(0, 0, 100, 100)
    static var scenViewRect = CGRectMake(0, 0, 100, 100)
    var scnView : SCNView?
    var sceneAnimateDuration : Double=8
    var scene: SCNScene?
    var scnViewColor :UIColor = UIColor.gray
    var progressColor :UIColor = UIColor.lightGray

    @IBOutlet weak var progressBar: UIProgressView!

    func startLoad( url :String,rect :CGRect){
        RN3dViewController.viewRect=rect
        DispatchQueue.main.async { [self] in
            self.scnView  = SCNView(frame: rect, options: nil)
            self.view.frame=rect
            let progressView =  CircularProgressView(frame: CGRect(x: 0, y: 0, width: 100, height: 100), lineWidth: 15, rounded: false)
           // view.insertSubview(progressView, at: 0)
           // progressView.progress = 100
           // progressView.center = view.center
           // progressView.progressColor = progressColor

            var scene: SCNScene
            
            do {
                let sceneSource = try GLTFSceneSource(url: URL.init(string: url)!)
                scene = try sceneSource.scene()
               // progressView.isHidden=true

            } catch {
               // progressView.isHidden=true
                print("\(error.localizedDescription)")
                return
            }
            
            
            self.view = self.scnView
          
            //scene.rootNode.scale = SCNVector3(0.8, 0.8, 0.8)
            //scene.rootNode.position = SCNVector3(x: 0, y:0.02, z:0)
            //scene.rootNode.scale = SCNVector3(rect.width*0.002, rect.height*0.0012, scene.fogDensityExponent)
            // scene.rootNode.position = SCNVector3(x: 0, y:Float(rect.height)*0.00002, z:0)
            //scene.rootNode.constraints = CGRect(x: 0, y:0, width: rect.width/2, height: rect.height/2)
            self.scnView?.scene  = scene
            self.scene = scene
            self.animateScen(nodeToAnimate: scene.rootNode)
            
            scene.lightingEnvironment.intensity = 1;
            
            self.scnView?.autoenablesDefaultLighting = true
            self.scnView?.loops = true

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
       
    }
    
    func setLoadingColor( color :UIColor){
        progressColor = color
        //self.scnView!.backgroundColor = color
        // self.scnView!.delegate = self
        
    }
    
    func setDuartion( duration :Double){
        sceneAnimateDuration = duration
       
    }
    
    func animateScen(nodeToAnimate: SCNNode){
        let pos = nodeToAnimate.position
        let animation = CAKeyframeAnimation(keyPath: "position")
        let pos1 = SCNVector3(pos.x, pos.y, pos.z)
        let pos2 = SCNVector3(pos.x + 1 , pos.y, pos.z)
        let pos3 = SCNVector3(pos.x + 1 , pos.y, pos.z + 1)
        
        animation.values = [pos1,pos2, pos3]
        animation.keyTimes = [0,0.5,1]
        animation.calculationMode = .linear
        animation.duration = 20
        animation.repeatCount = Float.infinity
        animation.isAdditive = true
        
        let animation2 = CAKeyframeAnimation(keyPath: "rotation")
        let pos1rot = SCNVector4(0, 0, 0, 0)
        let pos2rot = SCNVector4(0, 1, 0, CGFloat.pi * 2)
        animation2.values = [pos1rot, pos2rot]
        animation2.keyTimes = [0, 1]
        animation2.duration = sceneAnimateDuration
        animation2.repeatCount = Float.infinity
        animation2.isAdditive = true
        
        // nodeToAnimate.addAnimation(animation, forKey: "position")
        nodeToAnimate.addAnimation(animation2, forKey: "spin around")
    }
    
    func deg2rad(_ number: Double) -> Double {
        return number * .pi / 180
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

extension RN3dViewController: SCNSceneRendererDelegate {
    func renderer(_ renderer: SCNSceneRenderer, didApplyAnimationsAtTime time: TimeInterval) {
        // self.scene?.rootNode.updateVRMSpringBones(time: time)
    }
}
extension CAAnimation {
    class func animationWithSceneNamed(_ name: String) -> CAAnimation? {
        var animation: CAAnimation?
        if let scene = SCNScene(named: name) {
            scene.rootNode.enumerateChildNodes({ (child, stop) in
                if child.animationKeys.count > 0 {
                    animation = child.animation(forKey: child.animationKeys.first!) 
                    
                    stop.initialize(to: true)
                }
            })
        }
        return animation
    }
}
