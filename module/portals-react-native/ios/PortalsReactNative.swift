import Foundation
import Capacitor
import IonicPortals
import IonicLiveUpdates
import React

@objc(IONPortalsReactNative)
public class PortalsReactNative: NSObject {
    internal private(set) static var lum: LiveUpdateManager = .shared
    @available(*, deprecated, message: "This will be removed in the next release")
    internal static var portals = ConcurrentDictionary<String, Portal>(label: "com.portals.reactnative", dict: [:])
    let encoder = JSValueEncoder(optionalEncodingStrategy: .undefined)
    let decoder = JSValueDecoder()
    
    public override init() {
        guard let configUrl = Bundle.main.url(forResource: "portals.config.json", withExtension: nil) else {
            return
        }
        
        guard let configData = try? Data(contentsOf: configUrl),
              let jsonData = try? JSONSerialization.jsonObject(with: configData) as? [String: Any],
              let portalsConfig = PortalsConfig(jsonData)
        else { fatalError("Portals config data is malformed. Aborting.") }
        
        if let registrationKey = portalsConfig.registrationKey {
            PortalsRegistrationManager.shared.register(key: registrationKey)
        }
        
        if let publicKeyPath = portalsConfig.secureLiveUpdatesPublicKey {
            guard let publicKeyUrl = Bundle.main.url(forResource: publicKeyPath, withExtension: nil) else { fatalError("Public key not found at \(publicKeyPath)") }
            Self.lum = SecureLiveUpdateManager(named: "secure-updates", publicKeyUrl: publicKeyUrl)
        }
        
        let portals = portalsConfig.portals.map { $0.portal(with: Self.lum) }
        
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
        Self.lum = SecureLiveUpdateManager(named: "secure-updates", publicKeyUrl: publicKeyUrl)
        resolver(())
    }
    
    @available(*, deprecated, message: "This will be removed in the next release")
    @objc func addPortal(_ portalDict: [String: Any], resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        do {
            let portal = try decoder.decode(Portal.self, from: JSTypes.coerceDictionaryToJSObject(portalDict) ?? [:])
            Self.portals[portal.name] = portal
            resolver(try encoder.encode(portal))
        } catch {
            rejector(nil, "Invalid Portal configuration", error)
        }
    }
    
    @available(*, deprecated, message: "This will be removed in the next release")
    @objc func addPortals(_ portalsArray: [[String: Any]], resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        do {
            let portals = try decoder.decode([Portal].self, from: JSTypes.coerceArrayToJSArray(portalsArray) ?? [])
            for portal in portals {
                Self.portals[portal.name] = portal
            }
            resolver(try encoder.encode(portals))
        } catch {
            rejector(nil, "Invalid Portal configuration", error)
        }
    }
    
    @available(*, deprecated, message: "This will be removed in the next release")
    static func getPortal(named name: String) -> Portal? { portals[name] }
    
    @available(*, deprecated, message: "This will be removed in the next release")
    @objc func getPortal(_ name: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        guard let portal = Self.getPortal(named: name) else { return rejector(nil, "Portal named \(name) not registered", nil) }
        do {
            resolver(try encoder.encode(portal))
        } catch {
            rejector(nil, "Invalid Portal configuration", error)
        }
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
