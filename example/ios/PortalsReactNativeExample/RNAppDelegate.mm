#import "RNAppDelegate.h"

#import <React/RCTBundleURLProvider.h>
#import <React/RCTDefaultReactNativeFactoryDelegate.h>
#import <React/RCTReactNativeFactory.h>
#import <ReactAppDependencyProvider/RCTAppDependencyProvider.h>

@interface ReactNativeDelegate : RCTDefaultReactNativeFactoryDelegate
@end

@implementation ReactNativeDelegate

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge
{
  return [self bundleURL];
}

- (NSURL *)bundleURL
{
#if DEBUG
  return [[RCTBundleURLProvider sharedSettings] jsBundleURLForBundleRoot:@"index"];
#else
  return [[NSBundle mainBundle] URLForResource:@"main" withExtension:@"jsbundle"];
#endif
}

@end

@interface RNAppDelegate ()

@property (nonatomic, strong) ReactNativeDelegate *reactNativeDelegate;
@property (nonatomic, strong) RCTReactNativeFactory *reactNativeFactory;

@end

@implementation RNAppDelegate

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
  self.reactNativeDelegate = [ReactNativeDelegate new];
  self.reactNativeDelegate.dependencyProvider = [RCTAppDependencyProvider new];
  self.reactNativeFactory = [[RCTReactNativeFactory alloc] initWithDelegate:self.reactNativeDelegate];

  self.window = [[UIWindow alloc] initWithFrame:[UIScreen mainScreen].bounds];
  [self.reactNativeFactory startReactNativeWithModuleName:@"PortalsReactNativeExample"
                                                 inWindow:self.window
                                            launchOptions:launchOptions];
  return YES;
}

@end
