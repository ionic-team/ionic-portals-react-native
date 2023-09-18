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

    override func supportedEvents() -> [String] {
        [fcp]
    }
    
    @objc func registerOnFirstContentfulPaint(_ portalName: String, resolver: @escaping RCTPromiseResolveBlock, rejector: RCTPromiseRejectBlock) {
        guard var portal = PortalsReactNative.portals[portalName] else {
            return resolver(())
        }

        var portalPlugins = portal._portal.plugins.filter { plugin in
            switch plugin {
            case .instance(let plugin):
                return type(of: plugin) != WebVitalsPlugin.self
            case .type:
                return true
            }
        }


        var vitalsPlugin = WebVitalsPlugin { [weak self] _, duration in
            guard let self = self else { return }
            self.sendEvent(
                withName: self.fcp,
                body: [
                    "portalName": portalName,
                    "duration": duration
                ]
            )
        }

        portalPlugins.append(.instance(vitalsPlugin))
        portal._portal.plugins = portalPlugins

        PortalsReactNative.portals[portalName] = portal

        resolver(())
    }
    
    override class func requiresMainQueueSetup() -> Bool { true }
}
