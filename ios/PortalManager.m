//
//  PortalManager.m
//  ReactNativePortals
//
//  Created by Steven Sherry on 4/1/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(IONPortalsReactNative, NSObject)
RCT_EXTERN_METHOD(register: (NSString *) key resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(enableSecureLiveUpdates: (NSString *) publicKeyPath resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(addPortal: (NSDictionary) portal resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(addPortals: (NSArray) portals resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(getPortalNamed: (NSString *) name resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(syncOne: (NSString *) appId resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(syncSome: (NSArray) appIds resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(syncAll: (RCTPromiseResolveBlock) resolver)
@end
