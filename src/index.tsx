import React from 'react';
import {
  type EmitterSubscription,
  NativeEventEmitter,
  NativeModules,
  Platform,
  type ViewProps,
} from 'react-native';
import BasePortalView from './BasePortalView';

const { IONPortalPubSub, IONPortalsReactNative, IONPortalsWebVitals } =
  NativeModules;

export const PortalView = (props: PortalProps) => {
  let webVitals: string[] | undefined;
  if (props.webVitals) {
    webVitals = [];
    if (props.webVitals.firstContentfulPaint) {
      webVitals.push('fcp');
      registerVital(
        props.portal.name,
        'fcp',
        props.webVitals.firstContentfulPaint
      );
    }
    if (props.webVitals.firstInputDelay) {
      webVitals.push('fid');
      registerVital(props.portal.name, 'fid', props.webVitals.firstInputDelay);
    }
    if (props.webVitals.timeToFirstByte) {
      webVitals.push('ttfb');
      registerVital(props.portal.name, 'ttfb', props.webVitals.timeToFirstByte);
    }
  }

  const newProps = { ...props, webVitals: undefined };
  // @ts-ignore
  newProps.portal.webVitals = webVitals;

  return <BasePortalView {...newProps} />;
};

/**
 * The data that is received from a subscription event.
 */
export interface Message {
  /** The unique subscription reference received from {@link subscribe}*/
  subscriptionRef: number;
  data: any;
  /** The topic the message was sent from */
  topic: string;
}

const PortalsPubSub = new NativeEventEmitter(IONPortalPubSub);

/**
 * Subscribes to messages for a topic
 *
 * @param topic The topic to subscribe to
 * @param onMessageReceived The callback to invoke when a message is received
 * @returns A Promise<number> containing the unique subscription reference. This will need to be stored for calling {@link unsubscribe}.
 */
export const subscribe = (
  topic: string,
  onMessageReceived: (message: Message) => void
): EmitterSubscription => {
  return PortalsPubSub.addListener(
    `PortalsSubscription:${topic}`,
    (message: Message) => {
      onMessageReceived(message);
    }
  );
};

/**
 * Publishes a message to the provided topic
 *
 * @param topic The topic to publish the message to
 * @param data The data to publish to subscribers
 */
export const publish = (topic: string, data: any) => {
  const msg = { message: data };
  IONPortalPubSub.publish(topic, msg);
};

const webVitalsMap = new Map<string, EmitterSubscription>();
const WebVitals = new NativeEventEmitter(IONPortalsWebVitals);

interface WebVitalsEvent {
  portalName: string;
  duration: number;
}

const registerVital = (
  portalName: string,
  vital: 'fcp' | 'fid' | 'ttfb',
  callback: (duration: number) => void
) => {
  if (Platform.OS === 'ios' && vital !== 'fcp') return;
  const listener = WebVitals.addListener(
    `vitals:${vital}`,
    (event: WebVitalsEvent) => {
      if (event.portalName === portalName) {
        callback(event.duration);
      }
    }
  );

  webVitalsMap.set(`${portalName}-vitals:${vital}`, listener);
};

export type WebVitals = {
  firstContentfulPaint?: (duration: number) => void;
  firstInputDelay?: (duration: number) => void;
  timeToFirstByte?: (duration: number) => void;
};

/**
 * Validates that a valid registration key has been procured from http://ionic.io/register-portals
 * @param key The registration key
 * @returns Promise<void>
 */
export const register = async (key: string): Promise<void> => {
  return IONPortalsReactNative.register(key);
};

/**
 * The configuration of a web application to be embedded in a React Native application.
 */
export interface Portal {
  /** The name of the Portal to be referenced. Must be **unique** */
  name: string;
  /** Any Capacitor plugins to be made available to the Portal */
  plugins?: CapacitorPlugin[];
  /**
   * The root directory of the web application relative to Bundle.main on iOS
   * and src/main/assets on Android. If omitted, `name` is used.
   */
  startDir?: string;
  /** The name of the initial file to load. If omitted, 'index.html' is used. */
  index?: string;
  /** Any data needed at initial render when a portal is loaded. */
  initialContext?: {
    [key: string]: any;
  };
  assetMaps?: AssetMap[];
  liveUpdate?: LiveUpdateConfig;
}

export interface CapacitorPlugin {
  /** The classpath of the plugin to be used in Android. (e.g. com.capacitorjs.plugins.camera.CameraPlugin) */
  androidClassPath: string;
  /** The class name of the plugin to be used in iOS.
   * This must be the name as it is exposed to the Objective-C runtime.
   * For example, The CameraPlugin swift class is exposed to Objective-C as CAPCameraPlugin.
   */
  iosClassName: string;
}

export interface AssetMap {
  /** The name to index the asset map by */
  name: string;
  /** Any path to match via the web. If omitted, {@link AssetMap#name} will be used. */
  virtualPath?: string;
  /** The root directory of the assets relative to Bundle.main on iOS
   * and src/main/assets on Android. If omitted, the root of Bundle.main
   * and src/main/assets will be used.
   */
  startDir?: string;
}

/**
 * Props needed for rendering a {@link Portal}
 */
export type PortalProps = { portal: Portal; webVitals?: WebVitals } & ViewProps;

export interface LiveUpdate {
  /** The AppFlow application ID */
  appId: string;
  /** The AppFlow distribution channel */
  channel: string;
}

/** Data needed to register a live update to be managed */
export type LiveUpdateConfig = LiveUpdate & { syncOnAdd: boolean };

export interface LiveUpdateError {
  /** The AppFlow application ID relating to the failure */
  appId: string;
  /** The step in the sync process the LiveUpdate failed on. (e.g. CHECK, UNPACK)*/
  failStep: string;
  /** A human readable error message */
  message: string;
}

export interface Snapshot {
  /** The snapshot id as found in AppFlow */
  id: string;
  /** The AppFlow build id that produced the snapshot */
  buildId: string;
}

export interface SyncResult {
  /** The {@link LiveUpdate} associated with the result  */
  liveUpdate: LiveUpdate;
  /** The {@link Snapshot} that was sync'd */
  snapshot: Snapshot | null;
  /** Whether the snapshot was downloaded or already on disk */
  source: 'download' | 'cache';
  /** If the active application path was changed. A `false` value would indicate
   * the application already has the latest code for the associated {@link LiveUpdate}
   * configuration.
   */
  activeApplicationPathChanged: boolean;
}

/** Used for communicating sync results of multiple live updates */
export interface SyncResults {
  results: SyncResult[];
  errors: LiveUpdateError[];
}

/**
 * Configures LiveUpdates to cyrptographically verify the contents of the downloaded bundles.
 * This method must be called before any LiveUpdates are registered otherwise they will no longer be tracked.
 *
 * @param pathToKey The *relative* path to the public key for verification.
 * This path should be the same relatibe to the main application bundle on iOS and the assets directory on Android.
 * @returns Promise<void>
 */
export const enableSecureLiveUpdates = async (
  pathToKey: string
): Promise<void> => {
  return IONPortalsReactNative.enableSecureLiveUpdates(pathToKey);
};

/**
 * Syncs a single live update.
 *
 * @param appId The AppFlow application ID to sync.
 * @returns A Promise<LiveUpdate>. A failure should result in a {@link LiveUpdateError}.
 */
export const syncOne = async (appId: string): Promise<SyncResult> => {
  return IONPortalsReactNative.syncOne(appId);
};

/**
 * Syncs many live updates.
 *
 * @param appIds The AppFlow application IDs to sync.
 * @returns Promise<SyncResults>
 */
export const syncSome = async (appIds: string[]): Promise<SyncResults> => {
  return IONPortalsReactNative.syncSome(appIds);
};

/**
 * Syncs all registered LiveUpdates
 * @returns Promise<SyncResults>
 */
export const syncAll = async (): Promise<SyncResults> => {
  return IONPortalsReactNative.syncAll();
};
