<br />
<div align="center">
  <img src="https://user-images.githubusercontent.com/5769389/134952353-7d7b4145-3a80-4946-9b08-17b3a22c03a1.png" width="560" />
</div>
<div align="center">
  ⚡️ A supercharged native Web View for iOS and Android ⚡️
</div>
<br />
<p align="center">
  <a href="https://github.com/ionic-team/react-native-ionic-portals/actions?query=workflow%3ACI"><img src="https://img.shields.io/github/workflow/status/ionic-team/ionic-portals/CI?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@ionic/react-native-portals"><img src="https://img.shields.io/npm/l/@ionic/react-native-portals?style=flat-square" /></a>
  <a href="https://www.npmjs.com/package/@ionic/react-native-portals"><img src="https://img.shields.io/npm/v/@ionic/react-native-portals?style=flat-square" /></a>
</p>
<p align="center">
  <a href="https://ionic.io/docs/portals"><img src="https://img.shields.io/static/v1?label=docs&message=ionic.io/portals&color=blue&style=flat-square" /></a>
  <a href="https://twitter.com/ionicframework"><img src="https://img.shields.io/twitter/follow/ionicframework" /></a>
</p>

---

Ionic Portals is a supercharged native Web View component for iOS and Android that lets you add web-based experiences to React Native mobile apps. It enables React Native and web teams to better collaborate and bring new and existing web experiences to mobile in a safe, controlled way.

## Getting Started

### Installation
`npm install @ionic/portals-react-native`
or
`yarn add @ionic/portals-react-native`

### Usage
Register Portals with your [product key](#Registration):
```javascript
import { register } from '@ionic/portals-react-native';

register('YOUR_PORTAL_KEY_HERE');
```

Create a Portal and add it to the portal registry:
```javascript
import { addPortal } from '@ionic/portals-react-native';
const helloPortal = {
  // A unique name to reference later
  name: 'hello',
  // This is the location of your web bundle relative to the asset directory in Android and Bundle.main in iOS
  // This will default to the name of the portal
  startDir: 'portals/hello', 
  // Any initial state to be provided to a Portal if needed
  initialContext: {
    greeting: 'Hello, world!'
  }
};

addPortal(helloPortal);
```

Create a PortalView in your view hierarchy:
```javascript
import { PortalView } from '@ionic/portals-react-native';

<PortalView 
  // The name of the portal to be used in the view
  name='hello' 

  // Set any initial context you may want to override.
  initialContext={{ greeting: 'Goodbye!' }}

  // Setting a size is required
  style={{ flex: 1, height: 300 }} 
  />
```

#### iOS Specific Configuration
##### AppDelegate
Both Capacitor and React Native have classes named `AppDelegate`. To prevent a clash that can prevent your React Native application from launching,
you will need to rename `AppDelegate` to something else:
```objective-c
// AppDelegate.h
@interface RNAppDelegate : UIResponder <UIApplicationDelegate, RCTBridgeDelegate>
```

```objective-c
// AppDelegate.m
@implementation RNAppDelegate
@end
```

```objective-c
// main.m
#import <UIKit/UIKit.h>

#import "AppDelegate.h"

int main(int argc, char *argv[])
{
  @autoreleasepool {
    return UIApplicationMain(argc, argv, nil, NSStringFromClass([RNAppDelegate class]));
  }
}
```

##### Podfile
Because many of the Ionic Portals dependencies are comprised of Swift code and have custom module maps, you will need to add `use_frameworks!` to your iOS Podfile and remove `use_flipper!()`

### Communicating between React Native and Web
One of the key features of Ionic Portals for React Native is facilitating communication between the web and React Native layers of your application.
Publishing a message to the web:
```javascript
import { publish } from '@ionic/portals-react-native';

publish('topic', { number: 1 })
```

Subscribe to messages from the web:
```javascript
import { subscribe } from '@ionic/portals-react-native';

let subscriptionReference = await subscribe('topic', message => {
  // Here you have access to:
  // message.data - Any data sent from the web
  // message.subscriptionRef - The subscription reference used to manage the lifecycle of the subscription
  // message.topic - The topic the message was published on
})
```

When you no longer need to receive events, unsubscribe:
```javascript
import { unsubscribe } from '@ionic/portals-react-native';

unsubscribe('channel:topic', subscriptionReference)
```

To see an example of Portals Pub/Sub in action that manages the lifecycle of a subscription with the lifecycle of a React Native component, refer to the [`PubSubLabel`](https://github.com/ionic-team/react-native-ionic-portals/blob/af19df0d66059d85ab8dde493504368c3bf39127/example/App.tsx#L53) implementation in the example project.

### Using Capacitor Plugins
If you need to use any Capacitor plugins, you will have to register them in your Android project. This will also require creating and registering your Portals in native code as well:

**Android**
```java
public class MainApplication extends Application implements ReactApplication {
  @Override    
  public void onCreate() {
    super.onCreate();

    PortalManager.register("YOUR_PORTAL_KEY_HERE");
    PortalManager.newPortal("hello")
      .addPlugin(MyCapacitorPlugin.class) // Plugin registration
      .setInitialContext(Map.of("greeting", "Hello, world!"))
      .setStartDir("portals/hello")
      .create();
  }
}
```

**iOS**
```objective-c
@implementation RNAppDelegate
- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDicationary *)launchOptions {
    // React Native boilerplate
    [PortalManager register:@"YOUR_PORTAL_KEY_HERE"];
    PortalBuilder *builder = [[PortalBuilder alloc] init:@"hello"];
    [builder setStartDir:@"portals/hello"];
    [builder setInitialContext: @{ @"greeting": @"Hello, world!" }] 
    Portal *portal = [builder create];
    [PortalManager addPortal:portal];
}
@end
```

### Bundling Your Web Apps
Currently there is no tooling for bundling your web apps directly as part of @ionic/portals-react-native. Please follow the [native guides](https://ionic.io/docs/portals/how-to/pull-in-web-bundle#setup-the-web-asset-directory) to manage this as part of the native build process.


## Registration

Ionic Portals for React Native requires a key to use. Once you have integrated Portals into your project, login to your ionic account to get a key. See our doc on [how to register for free and get your Portals license key](https://ionic.io/docs/portals/how-to/get-a-product-key) and refer to the [usage](#Usage) section on how to add your key to your React Native application.

## FAQ

### What is the pricing for Portals use?

Portals is free to use in non-production environments. Businesses with more than USD $1 million in annual revenue are required to purchase a license from Ionic before using Portals in production.

### Is Portals Open Source?

See our [license](https://github.com/ionic-team/ionic-portals/blob/main/LICENSE.md).

### How is Portals Related to Capacitor and Ionic?

Ionic Portals is a solution that lets you add web-based experiences to your native mobile apps. Portals uses [Capacitor](https://capacitorjs.com) as a bridge between the native code and the web code to allow for cross-communication between the two layers. Because Portals uses Capacitor under the hood, you are able to use any existing [Capacitor Plugins](https://capacitorjs.com/docs/plugins) and even most [Cordova Plugins](https://capacitorjs.com/docs/plugins/cordova) while continuing to use your existing native workflow. Portals for React Native brings these capabilities to React Native applications.

[Ionic Framework](https://ionicframework.com/) is the open-source mobile app development framework that makes it easy to build top quality native and progressive web apps with web technologies. Your web experiences can be developed with Ionic, but it is not necessary to use Portals.

