// swift-tools-version: 6.0
// Experimental: for React Native's Swift Package Manager mode (React Native 0.87 or later), which
// React Native itself marks as not for production. CocoaPods apps use ReactNativePortals.podspec
// and never read this file.
//
// React Native resolves this manifest through a symlink at
// `<app>/ios/build/generated/autolinking/libs/ReactNativePortals`, and SwiftPM evaluates relative
// package paths against that symlink, so `../../../../xcframeworks` is the app's prebuilt React
// Native package. The name is pinned by `spm.name` in react-native.config.js.

import PackageDescription

let package = Package(
    name: "ReactNativePortals",
    platforms: [.iOS(.v15)],
    products: [
        .library(name: "ReactNativePortals", targets: ["ReactNativePortals"])
    ],
    dependencies: [
        .package(name: "ReactNative", path: "../../../../xcframeworks"),
        .package(url: "https://github.com/ionic-team/ionic-portals-ios", "0.13.1"..<"0.14.0"),
        .package(url: "https://github.com/ionic-team/ionic-live-updates-releases", "0.5.7"..<"0.6.0"),
        // Declared directly because the Swift sources import Capacitor. Same URL Capacitor plugins
        // use, so SwiftPM resolves a single Capacitor for the whole app.
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm", "8.0.0"..<"9.0.0")
    ],
    targets: [
        // SwiftPM cannot mix Swift and Objective-C++ in one target, so the Swift module compiles on
        // its own. The .mm files only reference its classes by Objective-C runtime name.
        .target(
            name: "ReactNativePortalsSwift",
            dependencies: [
                .product(name: "ReactHeaders", package: "ReactNative"),
                .product(name: "IonicPortals", package: "ionic-portals-ios"),
                .product(name: "IonicLiveUpdates", package: "ionic-live-updates-releases"),
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Swift",
            swiftSettings: [.swiftLanguageMode(.v5)]
        ),
        .target(
            name: "ReactNativePortals",
            dependencies: [
                "ReactNativePortalsSwift",
                .product(name: "ReactHeaders", package: "ReactNative"),
                .product(name: "ReactNativeHeaders", package: "ReactNative"),
                .product(name: "ReactNativeDependenciesHeaders", package: "ReactNative")
            ],
            path: "ios",
            exclude: ["Swift"],
            publicHeadersPath: "include",
            cSettings: [.define("RCT_NEW_ARCH_ENABLED", to: "1")],
            cxxSettings: [
                .define("RCT_NEW_ARCH_ENABLED", to: "1"),
                // Must match the prebuilt React framework's configuration-gated C++ ABI.
                .define("DEBUG", .when(configuration: .debug)),
                .define("NDEBUG", .when(configuration: .release))
            ]
        )
    ],
    cxxLanguageStandard: .cxx20
)
