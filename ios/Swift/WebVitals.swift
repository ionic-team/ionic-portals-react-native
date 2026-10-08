//
//  WebVitals.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 3/30/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import IonicPortals
import React
import Combine

@objc(IONPortalsWebVitals)
public class WebVitals: RCTEventEmitter {
    private let fcp = "vitals:fcp"
    private var subscription: AnyCancellable?
    
    public override init() {
        super.init()
        subscription = IonicPortals.PortalsPubSub
            .shared
            .publisher(for: "webVitals:received")
            .data()
            .sink { [weak self] data in
                guard let self = self else { return }
                self.sendEvent(
                    withName: self.fcp,
                    body: data
                )
            }
    }

    public override func supportedEvents() -> [String] {
        [fcp]
    }

    public override class func requiresMainQueueSetup() -> Bool { true }
}
