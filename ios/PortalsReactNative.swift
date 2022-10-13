import Foundation
import IonicPortals
import IonicLiveUpdates
import React

@objc(IONPortalsReactNative)
public class PortalsReactNative: NSObject {
    private var lum: LiveUpdateManager
    private static var portals: [String: Portal] = [:]
    
    public override init() {
        guard let configUrl = Bundle.main.url(forResource: "portals.config.json", withExtension: nil) else {
            lum = .shared
            return
        }
        
        guard let configData = try? Data(contentsOf: configUrl),
              let jsonData = try? JSONSerialization.jsonObject(with: configData) as? [String: Any],
              let portalsConfig = PortalsConfig(jsonData)
        else { fatalError("Portals config data is malformed. Aborting.") }
        
        if let registrationKey = portalsConfig.registrationKey {
            PortalsRegistrationManager.shared.register(key: registrationKey)
        }
        
        let liveUpdateManager: LiveUpdateManager
        if let publicKeyPath = portalsConfig.secureLiveUpdatesPublicKey {
            guard let publicKeyUrl = Bundle.main.url(forResource: publicKeyPath, withExtension: nil) else { fatalError("Public key not found at \(publicKeyPath)") }
            liveUpdateManager = SecureLiveUpdateManager(named: "secure-updates", publicKeyUrl: publicKeyUrl)
        } else {
            liveUpdateManager = .shared
        }
        
        lum = liveUpdateManager
        
        let portals = portalsConfig.portals.map { $0.portal(with: liveUpdateManager) }
        
        for portal in portals {
            Self.portals[portal.name] = portal
        }
    }
    
    @objc func register(_ key: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        PortalsRegistrationManager.shared.register(key: key)
        resolver(())
    }
    
    @objc func enableSecureLiveUpdates(_ publicKeyPath: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        guard let publicKeyUrl = Bundle.main.url(forResource: publicKeyPath, withExtension: nil) else { fatalError("Public key not found at \(publicKeyPath)") }
        lum = SecureLiveUpdateManager(named: "secure-updates", publicKeyUrl: publicKeyUrl)
        resolver(())
    }
    
    @objc func addPortal(_ portalDict: [String: Any], resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        guard let portal = Portal(portalDict, lum) else { return rejector(nil, "Invalid Portal configuration", nil) }
        Self.portals[portal.name] = portal
        resolver(portal.dict)
    }
    
    @objc func addPortals(_ portalsArray: [[String: Any]], resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        let portals = portalsArray.compactMap { Portal($0, lum) }
        
        for portal in portals {
            Self.portals[portal.name] = portal
        }
        
        resolver(portals.map(\.dict))
    }
    
    static func getPortal(named name: String) -> Portal? { portals[name] }
    
    @objc func getPortal(_ name: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        guard let portal = Self.getPortal(named: name) else { return rejector(nil, "Portal named \(name) not registered", nil) }
        resolver(portal.dict)
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
