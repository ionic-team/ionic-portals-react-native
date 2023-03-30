//
//  LiveUpdateManager+Async.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import IonicLiveUpdates

struct SyncResults {
    var results: [LiveUpdateManager.SyncResult]
    var errors: [LiveUpdateManager.Error]
}

extension SyncResults {
    var dict: [String: Any] {
        return [
            "results": results.map(\.dict),
            "errors": errors.map(\.dict)
        ]
    }
}

extension LiveUpdateManager {
    func syncSome(_ appIds: [String]) async -> SyncResults {
        await _syncSome(appIds).syncResults
    }
    
    private func _syncSome(_ appIds: [String]) -> AsyncStream<Result<LiveUpdateManager.SyncResult, LiveUpdateManager.Error>> {
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
    
    
    private func _syncAll() -> AsyncStream<Result<LiveUpdateManager.SyncResult, LiveUpdateManager.Error>> {
        AsyncStream { continuation in
            sync(isParallel: true) {
                continuation.finish()
            } appComplete: { result in
                continuation.yield(result)
            }
        }
    }
}

extension AsyncStream where Element == Result<LiveUpdateManager.SyncResult, LiveUpdateManager.Error> {
    var syncResults: SyncResults {
        get async {
            await reduce(into: SyncResults(results: [], errors: [])) { acc, next in
                switch next {
                case .success(let liveUpdate):
                    acc.results.append(liveUpdate)
                case .failure(let error):
                    acc.errors.append(error)
                }
            }
        }
    }
}
