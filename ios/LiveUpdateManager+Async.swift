//
//  LiveUpdateManager+Async.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import IonicLiveUpdates

struct SyncResults {
    var liveUpdates: [LiveUpdate]
    var errors: [LiveUpdateManager.Error]
}

extension SyncResults {
    var dict: [String: Any] {
        return [
            "liveUpdates": liveUpdates.map(\.dict),
            "errors": errors.map(\.dict)
        ]
    }
}

extension LiveUpdateManager {
    func syncSome(_ appIds: [String]) async -> SyncResults {
        await _syncSome(appIds).syncResults
    }
    
    private func _syncSome(_ appIds: [String]) -> AsyncStream<Result<LiveUpdate, LiveUpdateManager.Error>> {
        AsyncStream { continuation in
            sync(appIds: appIds, isParallel: true) {
                continuation.finish()
            } appComplete: { result in
                continuation.yield(result)
            }
        }
    }
    
    func syncAll() async -> SyncResults {
        await _syncAll().syncResults
    }
    
    
    private func _syncAll() -> AsyncStream<Result<LiveUpdate, LiveUpdateManager.Error>> {
        AsyncStream { continuation in
            sync(isParallel: true) {
                continuation.finish()
            } appComplete: { result in
                continuation.yield(result)
            }
        }
    }
}

extension AsyncStream where Element == Result<LiveUpdate, LiveUpdateManager.Error> {
    var syncResults: SyncResults {
        get async {
            await reduce(into: SyncResults(liveUpdates: [], errors: [])) { acc, next in
                switch next {
                case .success(let liveUpdate):
                    acc.liveUpdates.append(liveUpdate)
                case .failure(let error):
                    acc.errors.append(error)
                }
            }
        }
    }
}
