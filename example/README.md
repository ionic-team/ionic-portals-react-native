# Getting Started

>**Note**: Make sure you have completed the [React Native - Environment Setup](https://reactnative.dev/docs/environment-setup) instructions till "Creating a new application" step, before proceeding.

## Step 1: Build the Portal
This application depends on the web project located in `../web`. To build the web project you will need to run `yarn install` and `yarn build` from the `../web` directory.
```bash
cd ../web
yarn install
yarn build
```

## Step 2: Install the Portals CLI
Install the Portals CLI according to the [documentation](https://ionic.io/docs/portals/cli/overview). This is used for syncing web app to the mobile app.

## Step 3: Start the Metro Server

```bash
yarn start
```

## Step 4: Start the Application

### For Android

```bash
yarn android
```

### For iOS
```bash
yarn ios
```
