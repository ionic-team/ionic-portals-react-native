//
//  PortalsPubSub.m
//  ReactNativePortals
//
//  Created by Steven Sherry on 4/1/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(IONPortalsPubSub, RCTEventEmitter)
RCT_EXTERN_METHOD(subscribe: (NSString *) topic resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
RCT_EXTERN_METHOD(unsubscribe: (NSString *) topic subscriptionRef: (NSNumber) subscriptionRef)
RCT_EXTERN_METHOD(publish: (NSString *) topic data: (id) data)
@end
