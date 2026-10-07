//
//  IonicPortals+Codable.swift
//  ReactNativePortals
//
//  Created by Trevor Lambert on 6/5/24.
//  Copyright © 2024 Facebook. All rights reserved.
//

import Foundation
import IonicPortals

extension AssetMap: Encodable {
    enum CodingKeys: String, CodingKey {
        case startDir, virtualPath, name
    }
    
    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        try container.encode(startDir, forKey: .startDir)
        try container.encode(virtualPath, forKey: .virtualPath)
        try container.encode(name, forKey: .name)
    }
}

extension AssetMap: Decodable {
    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        let name = try container.decode(String.self, forKey: .name)
        let startDir = try container.decodeIfPresent(String.self, forKey: .startDir) ?? ""
        let virtualPath = try container.decodeIfPresent(String.self, forKey: .virtualPath)
        self.init(name: name, virtualPath: virtualPath, startDir: startDir)
    }
}
