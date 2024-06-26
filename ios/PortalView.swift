//
//  PortalView.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 10/5/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

import UIKit
import Capacitor
import IonicPortals
import React

@objc(IONPortalViewManager)
class PortalViewManager: RCTViewManager {
    override class func requiresMainQueueSetup() -> Bool { true }
    override func view() -> UIView! { PortalView() }
}

class PortalView: UIView {
    private var webView: PortalUIView?
    
    @objc var portal: [String: Any]? {
        get {
            guard let _portal = _portal else { return nil }
            return try? _portal.encode(to: JSValueEncoder(optionalEncodingStrategy: .undefined))
        }
        
        set {
            guard let portalDict = newValue else { return }

            var portal: Portal
            
            do {
                let jsObject = JSTypes.coerceDictionaryToJSObject(portalDict) ?? [:]
                portal = try Portal.decode(from: jsObject, with: JSValueDecoder())
            } catch {
                print(error.localizedDescription)
                return
            }

            if portal.usesWebVitals {
                let vitalsPlugin = WebVitalsPlugin { portalName, duration in
                    IonicPortals.PortalsPubSub
                        .shared
                        .publish(
                            ["portalName": portalName, "duration": duration],
                            to: "webVitals:received"
                        )
                }
                portal._portal.plugins.append(.instance(vitalsPlugin))
            }
            _portal = portal
        }
    }
    
    private var _portal: Portal? {
        didSet {
            guard let portal = _portal else { return }
            
            DispatchQueue.main.async { [weak self] in
                guard let self = self else { return }
                self.webView?.removeFromSuperview()
                let webView = PortalUIView(portal: portal._portal)
                webView.translatesAutoresizingMaskIntoConstraints = false
                self.addSubview(webView)
                NSLayoutConstraint.activate([
                    webView.topAnchor.constraint(equalTo: self.topAnchor),
                    webView.bottomAnchor.constraint(equalTo: self.bottomAnchor),
                    webView.leadingAnchor.constraint(equalTo: self.leadingAnchor),
                    webView.trailingAnchor.constraint(equalTo: self.trailingAnchor)
                ])
                self.webView = webView
            }
        }
    }
}
