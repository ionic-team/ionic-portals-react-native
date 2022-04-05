//
//  PortalView.m
//  ReactNativePortals
//
//  Created by Steven Sherry on 4/1/22.
//  Copyright © 2022 Facebook. All rights reserved.
//

#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(IONPortalViewManager, RCTViewManager)
RCT_EXPORT_VIEW_PROPERTY(portal, NSString)
RCT_EXPORT_VIEW_PROPERTY(initialContext, NSDictionary)
@end
