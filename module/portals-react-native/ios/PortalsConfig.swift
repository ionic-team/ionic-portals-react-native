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

struct PortalsConfig: Decodable {
    var registrationKey: String?
    var secureLiveUpdatesPublicKey: String?
}
