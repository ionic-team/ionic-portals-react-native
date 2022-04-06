//
//  PortalManager.m
//  ReactNativePortals
//
//  Created by Steven Sherry on 4/1/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(IONPortalManager, NSObject)
RCT_EXTERN_METHOD(register: (NSString *) key)
RCT_EXTERN_METHOD(addPortal: (NSDictionary) portal)
@end
