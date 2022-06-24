//
//  LiveUpdatesManager.m
//  ReactNativePortals
//
//  Created by Steven Sherry on 6/21/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(IONLiveUpdatesManager, NSObject)
RCT_EXTERN_METHOD(addLiveUpdate: (NSDictionary) liveUpdate)
RCT_EXTERN_METHOD(syncOne: (NSString *) appId resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(syncSome: (NSArray) appIds resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(syncAll: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
@end
