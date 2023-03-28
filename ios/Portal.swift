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
    struct Plugin {
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
    init?(_ dict: [String: Any], _ liveUpdateManager: LiveUpdateManager) {
        guard let name = dict["name"] as? String else { return nil }
        var plugins: [Portal.Plugin] = []

        if let capPlugins = dict["plugins"] as? Array<[String: String]> {
            plugins = capPlugins.compactMap(Portal.Plugin.init)
        }

        self._portal = IonicPortals.Portal(
            name: name,
            startDir: dict["startDir"] as? String,
            index: dict["index"] as? String ?? "index.html",
            initialContext: JSTypes.coerceDictionaryToJSObject(dict["initialContext"] as? [String: Any]) ?? [:],
            pluginRegistrationMode: .manual(plugins.toCapPlugin),
            liveUpdateManager: liveUpdateManager,
            liveUpdateConfig: (dict["liveUpdate"] as? [String: Any]).flatMap(LiveUpdate.init)
        )

        self.plugins = plugins
    }

    var dict: [String: Any] {
        var base = [
            "name": self.name,
            "startDir": self.startDir,
            "index": self.index,
            "initialContext": self.initialContext,
            "liveUpdateConfig": self.liveUpdateConfig?.dict as Any
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
