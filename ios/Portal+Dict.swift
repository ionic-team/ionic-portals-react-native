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

extension Portal {
    init?(_ dict: [String: Any], _ liveUpdateManager: LiveUpdateManager) {
        guard let name = dict["name"] as? String else { return nil }
        self.init(
            name: name,
            startDir: dict["startDir"] as? String,
            index: dict["index"] as? String ?? "index.html",
            initialContext: JSTypes.coerceDictionaryToJSObject(dict["initialContext"] as? [String: Any]) ?? [:],
            liveUpdateManager: liveUpdateManager,
            liveUpdateConfig: (dict["liveUpdate"] as? [String: Any]).flatMap(LiveUpdate.init)
        )
    }
    
    var dict: [String: Any] {
        return [
            "name": name,
            "startDir": startDir,
            "index": index,
            "initialContext": initialContext,
            "liveUpdateConfig": liveUpdateConfig?.dict as Any
        ]
    }
}
