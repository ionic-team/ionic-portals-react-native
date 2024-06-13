//
//  PortalWebVitals.m
//  ReactNativePortals
//
//  Created by Steven Sherry on 3/30/23.
//  Copyright © 2023 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface RCT_EXTERN_MODULE(IONPortalsWebVitals, RCTEventEmitter)
RCT_EXTERN_METHOD(registerOnFirstContentfulPaint: (NSString *) portalName resolver: (RCTPromiseResolveBlock) resolver rejector: (RCTPromiseRejectBlock) rejector)
@end

