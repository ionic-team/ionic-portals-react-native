# Ionic Portals for React Native Example
## Getting Started
In the root of the example project:
```bash
npm install
# or 
yarn
```

Navigate to the web directory:
```bash
cd web/RNPortals
```

Install and build:
```bash
npm install
# or
yarn

npm run build
# or
yarn build
```

Navigate back to the example project:
```bash
cd ../../
```

### iOS
Navigate to the iOS folder and run pod install (requires Cocoapods v1.10 or higher):
```bash
cd ios
pod install
```

Run the iOS project:
```bash
npm run ios
```

### Android
```bash
npm run android
```

## Developing
Currently we don't have any project setup to make building the example project with any changes to the library very seamless. To check any changes made to the React Native Module first build a tarball in the module root:
```bash
# navigate up to the RN module directory
cd ../
npm pack
```

In the example `package.json`, delete the existing entry for `@ionic/portals-react-native` and add the path to the tarball:
```bash
npm install file:../ionic-portals-react-native-${version}.tgz
```

After any subsequent changes to the module source, the above steps will need to be repeated.
