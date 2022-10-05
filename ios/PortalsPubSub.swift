//
//  PortalsPubSub.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import IonicPortals
import React

@objc(IONPortalPubSub)
class PortalsPubSub: RCTEventEmitter {
    private let eventName = "PortalsSubscription"
    
    override func supportedEvents() -> [String]! {
        [eventName]
    }
    
    @objc func subscribe(_ topic: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        let subRef = IonicPortals.PortalsPubSub.subscribe(topic) { [weak self] result in
            guard let self = self else { return }
            self.sendEvent(
                withName: self.eventName,
                body: [
                    "subscriptionRef": result.subscriptionRef,
                    "topic": result.topic,
                    "data": result.data
                ]
            )
        }
        
        resolver(subRef)
    }
    
    @objc func unsubscribe(_ topic: String, subscriptionRef: NSNumber, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        IonicPortals.PortalsPubSub.unsubscribe(from: topic, subscriptionRef: subscriptionRef.intValue)
        resolver(())
    }
    
    @objc func publish(_ topic: String, data: Any, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        IONPortalsPubSub.publish(message: data, topic: topic)
        resolver(())
    }
    
    override class func requiresMainQueueSetup() -> Bool { true }
}
