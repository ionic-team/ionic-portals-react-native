import Foundation
import IonicPortals
import IonicLiveUpdates
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
    private var _initialContext: [String: JSValue]?
    
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
        get { _initialContext }
        set { _initialContext = JSTypes.coerceDictionaryToJSObject(newValue) }
    }
    
    private var _portal: Portal? {
        didSet {
            guard var portal = _portal else { return }
            
            if let initialContext = _initialContext {
                portal.initialContext = initialContext
            }
            
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
            initialContext: JSTypes.coerceDictionaryToJSObject(dict["initialContext"] as? [String: Any]) ?? [:],
            liveUpdateConfig: (dict["liveUpdate"] as? [String: Any]).flatMap(LiveUpdate.init)
        )
    }
}

extension LiveUpdate {
    init?(_ dict: [String: Any]) {
        guard let appId = dict["appId"] as? String,
              let channel = dict["channel"] as? String,
              let syncOnAdd = dict["syncOnAdd"] as? Bool
        else { return nil }
        
        self.init(appId: appId, channel: channel, syncOnAdd: syncOnAdd)
    }
}

extension LiveUpdate {
    var dict: [String: Any] {
        return [
            "appId": appId,
            "channel": channel,
            "syncOnAdd": syncOnAdd
        ]
    }
}

extension LiveUpdateManager.Error {
    var dict: [String: Any] {
        return [
            "appId": appId,
            "failStep": failStep.rawValue.uppercased(),
            "message": localizedDescription
        ]
    }
}

private struct SyncResults {
    var liveUpdates: [LiveUpdate]
    var errors: [LiveUpdateManager.Error]
}

extension SyncResults {
    var dict: [String: Any] {
        return [
            "liveUpdates": liveUpdates.map(\.dict),
            "errors": errors.map(\.dict)
        ]
    }
}

@objc(IONLiveUpdatesManager)
public class LiveUpdatesManager: NSObject {
    private var lum = LiveUpdateManager.shared
    
    @objc func addLiveUpdate(_ dict: [String: Any]) {
        guard let liveUpdate = LiveUpdate(dict) else { return }
        try? lum.add(liveUpdate)
    }
    
    @objc func syncOne(_ appId: String, resolver: @escaping RCTPromiseResolveBlock, rejector: @escaping RCTPromiseRejectBlock) {
        lum.sync(appId: appId, isParallel: true) { result in
            switch result {
            case .success(let update):
                resolver(update.dict)
            case .failure(let error):
                rejector(nil, nil, error)
            }
        }
    }
    
    @objc func syncSome(_ appIds: [String], resolver: @escaping RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        Task {
            let syncResult = await lum.syncSome(appIds)
            resolver(syncResult.dict)
        }
    }
    
    @objc func syncAll(_ resolver: @escaping RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        Task {
            let syncResult = await lum.syncAll()
            resolver(syncResult.dict)
        }
    }

    @objc static func requiresMainQueueSetup() -> Bool { true }
}

extension LiveUpdateManager {
    fileprivate func syncSome(_ appIds: [String]) async -> SyncResults {
        await _syncSome(appIds).syncResults
    }
    
    private func _syncSome(_ appIds: [String]) -> AsyncStream<Result<LiveUpdate, LiveUpdateManager.Error>> {
        AsyncStream { continuation in
            sync(appIds: appIds, isParallel: true) {
                continuation.finish()
            } appComplete: { result in
                continuation.yield(result)
            }
        }
    }
    
    fileprivate func syncAll() async -> SyncResults {
        await _syncAll().syncResults
    }
    
    
    private func _syncAll() -> AsyncStream<Result<LiveUpdate, LiveUpdateManager.Error>> {
        AsyncStream { continuation in
            sync(isParallel: true) {
                continuation.finish()
            } appComplete: { result in
                continuation.yield(result)
            }
        }
    }
}

extension AsyncStream where Element == Result<LiveUpdate, LiveUpdateManager.Error> {
    fileprivate var syncResults: SyncResults {
        get async {
            await reduce(into: SyncResults(liveUpdates: [], errors: [])) { acc, next in
                switch next {
                case .success(let liveUpdate):
                    acc.liveUpdates.append(liveUpdate)
                case .failure(let error):
                    acc.errors.append(error)
                }
            }
        }
    }
}
