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
            return [
                "name": _portal.name,
                "initialContext": _portal.initialContext
            ]
        }
        
        set {
            guard let portalDict = newValue,
                  let name = portalDict["name"] as? String
            else { return }
            
            var portal = PortalsReactNative.getPortal(named: name)
            
            if let initialContext = portalDict["initialContext"] as? [String: Any] {
                portal?.initialContext = JSTypes.coerceDictionaryToJSObject(initialContext) ?? [:]
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
