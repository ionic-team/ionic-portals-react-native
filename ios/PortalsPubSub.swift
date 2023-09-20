//
//  PortalsPubSub.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import IonicPortals
import React
import Combine

@objc(IONPortalPubSub)
class PortalsPubSub: RCTEventEmitter {
    private let eventPrefix = "PortalsSubscription:"
    private var events: Set<String> = []
    
    override func supportedEvents() -> [String] {
        Array(events)
    }

    private let publishers = ConcurrentDictionary<String, AnyCancellable>(label: "io.ionic.rn.portalspubsub")

    override func addListener(_ eventName: String) {
        var topic = eventName
        if topic.hasPrefix(eventPrefix) {
            topic = String(eventName.suffix(from: eventPrefix.endIndex))
        }
        events.insert(eventName)
        super.addListener(eventName)

        if let _ = publishers[topic] { return }
        publishers[topic] = IonicPortals.PortalsPubSub.subscribe(to: topic) { [weak self] result in
            self?.sendEvent(
                withName: eventName,
                body: [
                    "topic": result.topic,
                    "data": result.data
                ]
            )
        }
    }

    @objc func publish(_ topic: String, data: Any, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        IONPortalsPubSub.publish(message: data, topic: topic)
        resolver(())
    }
    
    override class func requiresMainQueueSetup() -> Bool { true }
}

