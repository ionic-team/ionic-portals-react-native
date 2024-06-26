import Foundation
import Capacitor
import IonicPortals
import IonicLiveUpdates
import React

@objc(IONPortalsReactNative)
public class PortalsReactNative: NSObject {
    internal private(set) static var lum: LiveUpdateManager = .shared
    let encoder = JSValueEncoder(optionalEncodingStrategy: .undefined)
    let decoder = JSValueDecoder()
    
    public override init() {
        guard let configUrl = Bundle.main.url(forResource: "portals.config.json", withExtension: nil) else {
            return
        }
        
        guard let configData = try? Data(contentsOf: configUrl),
              let portalsConfig = try? JSONDecoder().decode(PortalsConfig.self, from: configData)
        else { fatalError("Portals config data is malformed. Aborting.") }
        
        if let registrationKey = portalsConfig.registrationKey {
            PortalsRegistrationManager.shared.register(key: registrationKey)
        }
        
        if let publicKeyPath = portalsConfig.secureLiveUpdatesPublicKey {
            guard let publicKeyUrl = Bundle.main.url(forResource: publicKeyPath, withExtension: nil) else { fatalError("Public key not found at \(publicKeyPath)") }
            Self.lum = SecureLiveUpdateManager(named: "secure-updates", publicKeyUrl: publicKeyUrl)
        }
    }
    
    @objc func register(_ key: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        PortalsRegistrationManager.shared.register(key: key)
        resolver(())
    }
    
    @objc func enableSecureLiveUpdates(_ publicKeyPath: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        guard let publicKeyUrl = Bundle.main.url(forResource: publicKeyPath, withExtension: nil) else { fatalError("Public key not found at \(publicKeyPath)") }
        Self.lum = SecureLiveUpdateManager(named: "secure-updates", publicKeyUrl: publicKeyUrl)
        resolver(())
    }

    @objc func syncOne(_ appId: String, resolver: @escaping RCTPromiseResolveBlock, rejector: @escaping RCTPromiseRejectBlock) {
        Task {
            do {
                let result = try await Self.lum.sync(appId: appId)
                resolver(try? JSValueEncoder(optionalEncodingStrategy: .undefined).encode(result))
            } catch {
                rejector(nil, nil, error)
            }
        }
    }
    
    @objc func syncSome(_ appIds: [String], resolver: @escaping RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        Task {
            let syncResult = await Self.lum.syncSome(appIds)
            resolver(try? syncResult.dict)
        }
    }
    
    @objc func syncAll(_ resolver: @escaping RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        Task {
            let syncResult = await Self.lum.syncAll()
            resolver(try? syncResult.dict)
        }
    }
    
    
    @objc static func requiresMainQueueSetup() -> Bool { true }
}
