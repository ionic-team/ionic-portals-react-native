//
//  LiveUpdateManagerError+Dict.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import IonicLiveUpdates

extension LiveUpdateManager.Error {
    var dict: [String: Any] {
        return [
            "appId": appId,
            "failStep": failStep.rawValue.uppercased(),
            "message": localizedDescription
        ]
    }
}
