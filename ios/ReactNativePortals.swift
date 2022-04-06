import Foundation
import IonicPortals
import React
import UIKit

@objc(IONPortalManager)
class PortalManager: NSObject {
    private typealias IonPortalManager = IonicPortals.PortalManager
    
    @objc func register(_ key: String) {
        IonPortalManager.register(key)
    }
    
    @objc func addPortal(_ portalDict: [String: Any]) {
        guard let name = portalDict["name"] as? String else { return }
        let portal = Portal(name, portalDict["startDir"] as? String)
        portal.initialContext = portalDict["initialContext"] as? [String: Any]
        IonPortalManager.addPortal(portal)
    }
    
    @objc static func requiresMainQueueSetup() -> Bool { true }
}

@objc(IONPortalsPubSub)
class PortalsPubSub: RCTEventEmitter {
    private let eventName = "PortalsSubscription"
    
    override func supportedEvents() -> [String]! {
        [eventName]
    }
    
    @objc func subscribe(_ topic: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        let subRef = PortalsPlugin.subscribe(topic) { [weak self] result in
            guard let self = self else { return }
            self.sendEvent(
                withName: self.eventName,
                body: [
                    "subscriptionRef": result.subscriptionRef,
                    "topic": result.topic,
                    "data": result.data
                ]
            )
        }
        
        resolver(subRef)
    }
    
    @objc func unsubscribe(_ topic: String, subscriptionRef: Int) {
        PortalsPlugin.unsubscribe(topic, subscriptionRef)
    }
    
    @objc func publish(_ topic: String, data: Any) {
        PortalsPlugin.publish(topic, data)
    }
    
    override class func requiresMainQueueSetup() -> Bool { true }
}

@objc(IONPortalViewManager)
class PortalViewManager: RCTViewManager {
    override class func requiresMainQueueSetup() -> Bool { true }
    override func view() -> UIView! { PortalView() }
}

class PortalView: UIView {
    private var webView: PortalWebView?
    
    @objc var portal: String? {
        get {
            guard let _portal = _portal else { return nil }
            return _portal.name
        }
        
        set {
            guard let portalName = newValue else { return }
            _portal = try? IonicPortals.PortalManager.getPortal(portalName)
        }
    }
    
    @objc var initialContext: [String: Any]? {
        get {
            guard let portal = _portal else { return nil }
            return portal.initialContext
        }
        
        set {
            guard let portalName = portal else { return }
            _portal = try? IonicPortals.PortalManager.getPortal(portalName)
            _portal?.initialContext = newValue
        }
    }
    
    private var _portal: Portal? {
        didSet {
            guard let portal = _portal else { return }
            DispatchQueue.main.async { [weak self] in
                guard let self = self else { return }
                self.webView?.removeFromSuperview()
                let webView = PortalWebView(portal: portal)
                webView.translatesAutoresizingMaskIntoConstraints = false
                self.addSubview(webView)
                NSLayoutConstraint.activate([
                    webView.topAnchor.constraint(equalTo: self.topAnchor),
                    webView.bottomAnchor.constraint(equalTo: self.bottomAnchor),
                    webView.leadingAnchor.constraint(equalTo: self.leadingAnchor),
                    webView.trailingAnchor.constraint(equalTo: self.trailingAnchor)
                ])
                self.webView = webView
            }
        }
    }
}

