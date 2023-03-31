//
//  WebVitals.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 3/30/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import IonicPortals
import React

@objc(IONPortalsWebVitals)
class WebVitals: RCTEventEmitter {
    private let fcp = "vitals:fcp"
//    private let fid = "vitals:fid"
//    private let ttfb = "vitals:ttfb"
    
    override func supportedEvents() -> [String] {
        [fcp]
    }
    
    @objc func registerOnFirstContentfulPaint(_ portalName: String, resolver: RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        PortalsReactNative.portals[portalName]?.performanceReporter = WebPerformanceReporter { [weak self] _, duration in
            guard let self = self else { return }
            self.sendEvent(
                withName: self.fcp,
                body: [
                    "portalName": portalName,
                    "duration": duration
                ]
            )
            
            resolver(())
        }
    }
    
    override class func requiresMainQueueSetup() -> Bool { true }
}
