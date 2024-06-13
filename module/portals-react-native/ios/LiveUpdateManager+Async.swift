//
//  LiveUpdateManager+Async.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import IonicLiveUpdates
import Capacitor

struct SyncResults: Encodable {
    var results: [LiveUpdateManager.SyncResult]
    var errors: [LiveUpdateManager.SyncError]
}

extension SyncResults {
    init(_ results: ([LiveUpdateManager.SyncResult], [LiveUpdateManager.SyncError])) {
        self.results = results.0
        self.errors = results.1
    }

    var dict: JSValue {
        get throws {
            try JSValueEncoder(optionalEncodingStrategy: .undefined)
                .encode(self)
        }
    }
}

extension LiveUpdateManager {
    func syncSome(_ appIds: [String]) async -> SyncResults {
        SyncResults(await sync(appIds: appIds))
    }

    func syncAll() async -> SyncResults {
        SyncResults(await sync())
    }
}
