//
//  AssetMap+Dict.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 3/29/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import IonicPortals

extension AssetMap {
    init?(_ dict: [String: Any]) {
        guard let name = dict["name"] as? String else { return nil }
        self.init(
            name: name,
            virtualPath: dict["virtualPath"] as? String,
            startDir: dict["startDir"] as? String ?? ""
        )
    }
    
    var dict: [String: Any] {
        return [
            "name": name,
            "virtualPath": virtualPath,
            "startDir": startDir
        ]
    }
}
