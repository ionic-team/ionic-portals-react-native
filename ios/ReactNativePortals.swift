import Foundation
import IonicPortals
import React
import UIKit
import Capacitor

@objc(IONPortalManager)
public class PortalManager: NSObject {
    private static var portals: [String: Portal] = [:]
    
    public static func register(_ key: String) {
        PortalsRegistrationManager.shared.register(key: key)
    }
    
    public static func add(_ portal: Portal) {
        portals[portal.name] = portal
    }
    
    static func getPortal(named name: String) -> Portal? { portals[name] }
    
    @objc func register(_ key: String) {
        Self.register(key)
    }
    
    @objc func addPortal(_ portalDict: [String: Any]) {
        guard let portal = Portal(portalDict) else { return }
        Self.add(portal)
    }
    
    @objc static func requiresMainQueueSetup() -> Bool { true }
}

@objc(IONPortalPubSub)
class PortalsPubSub: RCTEventEmitter {
    private let eventName = "PortalsSubscription"
    
    override func supportedEvents() -> [String]! {
        [eventName]
    }
    
    @objc func subscribe(_ topic: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        let subRef = IonicPortals.PortalsPubSub.subscribe(topic) { [weak self] result in
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
    
    @objc func unsubscribe(_ topic: String, subscriptionRef: NSNumber) {
        IonicPortals.PortalsPubSub.unsubscribe(from: topic, subscriptionRef: subscriptionRef.intValue)
    }
    
    @objc func publish(_ topic: String, data: Any) {
        IONPortalsPubSub.publish(message: data, topic: topic)
    }
    
    override class func requiresMainQueueSetup() -> Bool { true }
}

@objc(IONPortalViewManager)
class PortalViewManager: RCTViewManager {
    override class func requiresMainQueueSetup() -> Bool { true }
    override func view() -> UIView! { PortalView() }
}

class PortalView: UIView {
    private var webView: PortalUIView?
    
    @objc var name: String? {
        get {
            guard let _portal = _portal else { return nil }
            return _portal.name
        }
        
        set {
            guard let portalName = newValue else { return }
            _portal = PortalManager.getPortal(named: portalName)
        }
    }
    
    @objc var initialContext: [String: Any]? {
        get {
            guard let portal = _portal else { return nil }
            return portal.initialContext
        }
        
        set {
            guard let name = name else { return }
            _portal = PortalManager.getPortal(named: name)
            _portal?.initialContext = JSTypes.coerceDictionaryToJSObject(newValue) ?? [:]
        }
    }
    
    private var _portal: Portal? {
        didSet {
            guard let portal = _portal else { return }
            DispatchQueue.main.async { [weak self] in
                guard let self = self else { return }
                self.webView?.removeFromSuperview()
                let webView = PortalUIView(portal: portal)
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

extension Portal {
    init?(_ dict: [String: Any]) {
        guard let name = dict["name"] as? String else { return nil }
        self.init(
            name: name,
            startDir: dict["startDir"] as? String,
            initialContext: JSTypes.coerceDictionaryToJSObject(dict["initialContext"] as? [String: Any]) ?? [:]
        )
    }
}

