//
//  SyncResult+Dict.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 3/28/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import IonicLiveUpdates

extension LiveUpdateManager.SyncResult: Encodable {
    enum TopLevelKeys: String, CodingKey { case liveUpdate, snapshot, source, activeApplicationPathChanged }
    enum LiveUpdateKeys: String, CodingKey { case appId, channel }
    enum SnapshotKeys: String, CodingKey { case id, buildId }

    public func encode(to encoder: Encoder) throws {
        var topLevelContainer = encoder.container(keyedBy: TopLevelKeys.self)

        var liveUpdateContainer = topLevelContainer.nestedContainer(keyedBy: LiveUpdateKeys.self, forKey: .liveUpdate)
        try liveUpdateContainer.encode(liveUpdate.appId, forKey: .appId)
        try liveUpdateContainer.encode(liveUpdate.channel, forKey: .channel)

        if let snapshot {
            var snapshotContainer = topLevelContainer.nestedContainer(keyedBy: SnapshotKeys.self, forKey: .snapshot)
            try snapshotContainer.encode(snapshot.id, forKey: .id)
            try snapshotContainer.encode(snapshot.buildId, forKey: .buildId)
        }

        if case let .cache(pathsChanged) = source {
            try topLevelContainer.encode("cache", forKey: .source)
            try topLevelContainer.encode(pathsChanged, forKey: .activeApplicationPathChanged)
        } else {
            try topLevelContainer.encode("download", forKey: .source)
            try topLevelContainer.encode(true, forKey: .activeApplicationPathChanged)
        }
    }
}

extension LiveUpdateManager.SyncError: Encodable {
    enum CodingKeys: String, CodingKey { case appId, failStep, message }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        let failStep: String
        if case .secureUpdateError = reason {
            failStep = "VERIFY"
        } else {
            failStep = self.failStep.rawValue.uppercased()
        }

        try container.encode(appId, forKey: .appId)
        try container.encode(failStep, forKey: .failStep)
        try container.encode(errorDescription, forKey: .message)
    }
}
