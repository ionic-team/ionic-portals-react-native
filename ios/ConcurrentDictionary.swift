//
//  ConcurrentHashMap.swift
//  ReactNativePortals
//
//  Created by Steven Sherry on 3/31/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

import Foundation

class ConcurrentDictionary<Key: Hashable, Value> {
    private var _dict: Dictionary<Key, Value>
    var dict: Dictionary<Key, Value> {
        queue.sync { _dict }
    }
    private let queue: DispatchQueue
    
    init(label: String, dict: [Key: Value] = [:]) {
        queue = DispatchQueue(label: label, qos: .userInitiated, attributes: .concurrent)
        self._dict = dict
    }
    
    subscript(_ key: Key) -> Value? {
        get { queue.sync { _dict[key] } }
        set {
            queue.async(flags: .barrier) { [weak self] in
                self?._dict[key] = newValue
            }
        }
    }
}

extension ConcurrentDictionary: Collection {
    var startIndex: Dictionary<Key, Value>.Index { dict.startIndex }
    var endIndex: Dictionary<Key, Value>.Index { dict.endIndex }
    func index(after i: Dictionary<Key, Value>.Index) -> Dictionary<Key, Value>.Index {
        dict.index(after: i)
    }
    
    subscript(position: Dictionary<Key, Value>.Index) -> Dictionary<Key, Value>.Element {
        get {
            dict[position]
        }
    }
}
