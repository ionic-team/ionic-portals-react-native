<br />
<div align="center">
  <img src="https://user-images.githubusercontent.com/5769389/134952353-7d7b4145-3a80-4946-9b08-17b3a22c03a1.png" width="560" />
</div>
<div align="center">
  ⚡️ A supercharged native Web View for React Native ⚡️
</div>
<br />
<p align="center">
  <img src="https://img.shields.io/badge/platform-iOS%2015.1%2B-lightgrey?style=flat-square" alt="Supports iOS 15.1 and up" />
  <img src="https://img.shields.io/badge/platform-Android%20SDK%2024%2B-brightgreen?style=flat-square" alt="Supports Android SDK 24 and up" />
  <img src="https://img.shields.io/badge/platform-React%20Native%200.80.3%2B-blue?style=flat-square" alt="Supports React Native 0.80.3 and up" />
</p>
<p align="center">
  <a href="https://www.npmjs.com/package/@ionic/portals-react-native"><img src="https://img.shields.io/npm/l/@ionic/portals-react-native?style=flat-square" /></a>
</p>
<p align="center">
  <a href="https://ionic.io/docs/portals"><img src="https://img.shields.io/static/v1?label=docs&message=ionic.io/portals&color=blue&style=flat-square" /></a>
  <a href="https://twitter.com/ionicframework"><img src="https://img.shields.io/badge/follow-%40ionicframework-1DA1F2?logo=twitter" alt="Follow @ionicframework"></a>
</p>

---

Ionic Portals is a supercharged native Web View component for React Native that lets you add web-based experiences to native mobile apps. It enables native and web teams to better collaborate and bring new and existing web experiences to mobile in a safe, controlled way.

## Getting Started

See our docs to [get started with Portals](https://ionic.io/docs/portals/getting-started/guide).

## Experimental: Swift Package Manager (React Native 0.87+)

React Native 0.87 added an opt-in mode where an iOS app uses Swift Package Manager instead of CocoaPods (`npx react-native spm`). This package ships a `Package.swift` for that mode. React Native marks the mode as experimental and not for production, and CocoaPods remains the supported integration.

In SwiftPM mode, Portals depends on Capacitor through [`capacitor-swift-pm`](https://github.com/ionic-team/capacitor-swift-pm). Add any Capacitor plugins your portals use through SwiftPM too, so the app links a single copy of Capacitor. If a plugin comes from CocoaPods while Portals comes from SwiftPM, the app links two copies of Capacitor and Portals skips the plugin with a `not a CAPPlugin subclass` message in the Xcode console.

React Native autolinks Capacitor plugins installed from npm, but it derives the Swift package name from the npm name (`@capacitor/preferences` becomes `Preferences`). Capacitor plugins name their package after the podspec (`CapacitorPreferences`), so resolution fails with `product 'Preferences' ... not found`. Until the plugin ships its own `react-native.config.js`, add one to it with [`patch-package`](https://github.com/ds300/patch-package), using the product name from the plugin's `Package.swift`:

```js
// node_modules/@capacitor/preferences/react-native.config.js
module.exports = { spm: { name: 'CapacitorPreferences' } };
```

## Registration

The Ionic Portals library for React Native requires a license key to use. Once you have integrated Portals into your project, login to your ionic account to get a key. See our doc on [how to register for free and get your Portals license key](https://ionic.io/docs/portals/how-to/get-a-product-key) and refer to the [React Native](https://ionic.io/docs/portals/getting-started/react-native) getting started guides to see where to add your key.

## FAQ

### What is the pricing for Portals use?

[Contact our sales team](https://ionic.io/portals#sales) for more information about pricing.

### Is Portals Open Source?

See our [license](https://github.com/ionic-team/ionic-portals/blob/main/LICENSE.md).

### How is Portals Related to Capacitor and Ionic?

Ionic Portals is a solution that lets you add web-based experiences to your native mobile apps. Portals uses [Capacitor](https://capacitorjs.com) as a bridge between the native code and the web code to allow for cross-communication between the two layers. Because Portals uses Capacitor under the hood, you are able to use any existing [Capacitor Plugins](https://capacitorjs.com/docs/plugins) while continuing to use your existing native workflow.

[Ionic Framework](https://ionicframework.com/) is the open-source mobile app development framework that makes it easy to build top quality native and progressive web apps with web technologies. Your web experiences can be developed with Ionic, but it is not necessary to use Portals.
