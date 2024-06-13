//
//  PortalsPubSub.m
//  ReactNativePortals
//
//  Created by Steven Sherry on 4/1/22.
//  Copyright © 2022 Ionic. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(IONPortalPubSub, RCTEventEmitter)
RCT_EXTERN_METHOD(publish: (NSString *) topic data: (id) data resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
@end
