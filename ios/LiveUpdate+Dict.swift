//
//  LiveUpdate+Dict.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import IonicLiveUpdates

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
