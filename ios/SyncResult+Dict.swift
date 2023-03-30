//
//  SyncResult+Dict.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 3/28/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import IonicLiveUpdates

extension LiveUpdateManager.SyncResult {
    var dict: [String: Any] {
        var base: [String: Any] = [
            "liveUpdate": [
                "appId": liveUpdate.appId,
                "channel": liveUpdate.channel
            ],
            "snapshot": [
                "id": snapshot.id,
                "buildId": snapshot.buildId
            ]
        ]
        
        switch source {
        case .cache(let latestAppDirectoryChanged):
            base["source"] = "cache"
            base["activeApplicationPathChanged"] = latestAppDirectoryChanged
        @unknown default:
            base["source"] = "download"
            base["activeApplicationPathChanged"] = true
        }
        
        return base
    }
}
