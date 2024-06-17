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

extension Portal {
    func encode(to encoder: JSValueEncoder) throws -> JSObject {
        var object = try encoder.encodeJSObject(self)
        object["initialContext"] = _portal.initialContext
        return object
    }

    static func decode(from jsObject: JSObject, with decoder: JSValueDecoder) throws -> Portal {
        var portal = try decoder.decode(Portal.self, from: jsObject)
        portal.initialContext = jsObject["initialContext"] as? JSObject ?? [:]
        return portal
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
        let assetMaps = try container.decodeIfPresent([AssetMap].self, forKey: .assetMaps) ?? []
        let liveUpdateConfig = try container.decodeIfPresent(LiveUpdate.self, forKey: .liveUpdate)
        
        let portal = IonicPortals.Portal(name: name, startDir: startDir, index: index, assetMaps: assetMaps, plugins: plugins.toCapPlugin, liveUpdateManager: PortalsReactNative.lum, liveUpdateConfig: liveUpdateConfig)
        self.init(_portal: portal, plugins: plugins)
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
