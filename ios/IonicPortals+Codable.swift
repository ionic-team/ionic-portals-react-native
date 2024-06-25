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

//export interface Portal {
//  /** The name of the Portal to be referenced. Must be **unique** */
//  name: string;
//  /** Any Capacitor plugins to be made available to the Portal */
//  plugins?: CapacitorPlugin[];
//  /**
//   * The root directory of the web application relative to Bundle.main on iOS
//   * and src/main/assets on Android. If omitted, `name` is used.
//   */
//  startDir?: string;
//  /** The name of the initial file to load. If omitted, 'index.html' is used. */
//  index?: string;
//  /** Any data needed at initial render when a portal is loaded. */
//  initialContext?: {
//    [key: string]: any;
//  };
//  assetMaps?: AssetMap[];
//  liveUpdate?: LiveUpdateConfig;
//}

//export interface LiveUpdate {
//  /** The AppFlow application ID */
//  appId: string;
//  /** The AppFlow distribution channel */
//  channel: string;
//}
//
///** Data needed to register a live update to be managed */
//export type LiveUpdateConfig = LiveUpdate & { syncOnAdd: boolean };

//export interface AssetMap {
//  /** The name to index the asset map by */
//  name: string;
//  /** Any path to match via the web. If omitted, {@link AssetMap#name} will be used. */
//  virtualPath?: string;
//  /** The root directory of the assets relative to Bundle.main on iOS
//   * and src/main/assets on Android. If omitted, the root of Bundle.main
//   * and src/main/assets will be used.
//   */
//  startDir?: string;
//}


