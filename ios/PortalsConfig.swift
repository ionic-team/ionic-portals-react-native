//
//  PortalsConfig.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import Capacitor
import IonicLiveUpdates
import IonicPortals

struct PortalsConfig {
    var portals: [Portal]
    var registrationKey: String?
    var secureLiveUpdatesPublicKey: String?
    
    struct Portal {
        var name: String
        var startDir: String?
        var index: String?
        var initialContext: JSObject?
        var plugins: [ReactNativePortals.Portal.Plugin]?
        var liveUpdate: LiveUpdate?
        
        func portal(with liveUpdateManager: LiveUpdateManager) -> ReactNativePortals.Portal {
            return .init(
                _portal: .init(
                    name: name,
                    startDir: startDir,
                    index: index ?? "index.html",
                    initialContext: initialContext ?? [:],
                    pluginRegistrationMode: .manual(plugins?.toCapPlugin ?? []),
                    liveUpdateManager: liveUpdateManager,
                    liveUpdateConfig: liveUpdate.map { .init(appId: $0.appId, channel: $0.channel, syncOnAdd: $0.syncOnAdd) }
                ),
                plugins: plugins ?? []
            )
        }
        
        struct LiveUpdate {
            var channel: String
            var appId: String
            var syncOnAdd: Bool
        }
    }
}

extension PortalsConfig {
    init?(_ dict: [String: Any]) {
        guard let rawPortals = dict["portals"] as? [[String: Any]] else {
            print("Portals configuration must contain a 'portals' property.")
            return nil
        }
        
        let portals = rawPortals.compactMap(Portal.init)
        guard portals.count == rawPortals.count else {
            print("Invalid portals configuration.")
            return nil
        }
        
        self.portals = portals
        registrationKey = dict["registrationKey"] as? String
        secureLiveUpdatesPublicKey = dict["liveUpdatesKey"] as? String
    }
}

extension PortalsConfig.Portal {
    init?(_ dict: [String: Any]) {
        guard let name = dict["name"] as? String else {
            print("Portal confifguration must contain a 'name' property.")
            return nil
        }
        
        self.name = name
        startDir = dict["startDir"] as? String
        index = dict["index"] as? String
        initialContext = (dict["initialContext"] as? [String: Any])
            .flatMap { JSTypes.coerceDictionaryToJSObject($0) }
        plugins = (dict["plugins"] as? Array<[String: String]>)
            .flatMap { $0.compactMap(ReactNativePortals.Portal.Plugin.init) }
        liveUpdate = (dict["liveUpdate"] as? [String: Any])
            .flatMap(LiveUpdate.init)
    }
}

extension PortalsConfig.Portal.LiveUpdate {
    init?(_ dict: [String: Any]) {
        guard let appId = dict["appId"] as? String else {
            print("LiveUpdate configuration must contain an 'appId' property.")
            return nil
        }
        
        self.appId = appId
        channel = dict["channel"] as? String ?? "production"
        syncOnAdd = dict["syncOnAdd"] as? Bool ?? true
    }
}

