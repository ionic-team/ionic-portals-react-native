//
//  Portal+Dict.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import Capacitor
import IonicLiveUpdates
import IonicPortals

@dynamicMemberLookup
struct Portal {
    struct Plugin: Codable {
        var iosClassName: String
        var androidClassPath: String
    }

    var _portal: IonicPortals.Portal
    var plugins: [Plugin]

    subscript<T>(dynamicMember keypath: Swift.WritableKeyPath<IonicPortals.Portal, T>) -> T {
        get { _portal[keyPath: keypath] }
        set { _portal[keyPath: keypath] = newValue }
    }

    subscript<T>(dynamicMember keypath: Swift.KeyPath<IonicPortals.Portal, T>) -> T {
        _portal[keyPath: keypath]
    }
}

extension Portal: Encodable {
    enum CodingKeys: String, CodingKey {
        case name, startDir, plugins, index, initialContext, assetMaps, liveUpdate
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(self.name, forKey: .name)
        try container.encode(self.startDir, forKey: .startDir)
        try container.encode(self.plugins, forKey: .plugins)
        try container.encode(self.index, forKey: .index)
        #warning("How do we encode this")
//        try container.encode(self.initialContext, forKey: .initialContext)
        try container.encode(self.assetMaps, forKey: .assetMaps)
        try container.encode(self.liveUpdateConfig, forKey: .liveUpdate)
    }
}

extension Portal: Decodable {
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let name = try container.decode(String.self, forKey: .name)
        let startDir = try container.decodeIfPresent(String.self, forKey: .startDir) ?? ""
        let plugins = try container.decodeIfPresent([Plugin].self, forKey: .plugins) ?? []
        let index = try container.decodeIfPresent(String.self, forKey: .index) ?? "index.html"
        #warning("How do we decode this")
//        let initialContext = try container.decodeIfPresent(JSObject.self, forKey: .initialContext)
        let assetMaps = try container.decodeIfPresent([AssetMap].self, forKey: .assetMaps) ?? []
        let liveUpdateConfig = try container.decodeIfPresent(LiveUpdate.self, forKey: .liveUpdate)
        
        let portal = IonicPortals.Portal(name: name, startDir: startDir, index: index, assetMaps: assetMaps, plugins: plugins.toCapPlugin, liveUpdateManager: PortalsReactNative.lum, liveUpdateConfig: liveUpdateConfig)
        self.init(_portal: portal, plugins: plugins)
    }
}

extension Portal {
    private static let encoder = JSValueEncoder(optionalEncodingStrategy: .undefined)
    private static let decoder = JSValueDecoder()

//    init?(_ dict: [String: Any], _ liveUpdateManager: LiveUpdateManager) {
//        guard let name = dict["name"] as? String else { return nil }
//        var plugins: [Portal.Plugin] = []
//
//        if let capPlugins = dict["plugins"] as? Array<[String: String]> {
//            plugins = capPlugins.compactMap(Portal.Plugin.init)
//        }
//        
//        var assetMaps: [AssetMap] = []
//        
//        if let maps = dict["assetMaps"] as? Array<[String: String]> {
//            assetMaps = maps.compactMap(AssetMap.init)
//        }
//
//        self._portal = IonicPortals.Portal(
//            name: name,
//            startDir: dict["startDir"] as? String,
//            index: dict["index"] as? String ?? "index.html",
//            initialContext: JSTypes.coerceDictionaryToJSObject(dict["initialContext"] as? [String: Any]) ?? [:],
//            assetMaps: assetMaps,
//            plugins: plugins.toCapPlugin,
//            liveUpdateManager: liveUpdateManager,
//            liveUpdateConfig: (dict["liveUpdate"] as? JSObject).flatMap { try? Self.decoder.decode(LiveUpdate.self, from: $0) }
//        )
//
//        self.plugins = plugins
//    }

    var dict: [String: Any] {
        var base = [
            "name": self.name,
            "startDir": self.startDir,
            "index": self.index,
            "initialContext": self.initialContext,
            "liveUpdate": self.liveUpdateConfig.flatMap { try? Self.encoder.encode($0) } as Any
        ]

        if !plugins.isEmpty {
            base["plugins"] = plugins.map(\.dict)
        }

        return base
    }
}

extension Portal.Plugin {
    init?(_ dict: [String: String]) {
        guard let iosClassName = dict["iosClassName"],
              let androidClassPath = dict["androidClassPath"]
        else { return nil }

        self.iosClassName = iosClassName
        self.androidClassPath = androidClassPath
    }

    var dict: [String: String] {
        return [
            "iosClassName": iosClassName,
            "androidClassPath": androidClassPath
        ]
    }
}

extension Portal {
    var capPlugins: [IonicPortals.Portal.Plugin] {
        plugins.toCapPlugin
    }
}

extension Array where Element == Portal.Plugin {
    var toCapPlugin: [IonicPortals.Portal.Plugin] {
        compactMap { NSClassFromString($0.iosClassName) as? CAPPlugin.Type }
            .compactMap(IonicPortals.Portal.Plugin.type)
    }
}
