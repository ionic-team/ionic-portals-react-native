import {
  EmitterSubscription,
  NativeEventEmitter,
  NativeModules,
  ViewProps,
} from 'react-native';

const { IONPortalPubSub, IONPortalManager, IONLiveUpdatesManager } =
  NativeModules;

export { default as PortalView } from './PortalView';

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

const subscriptionMap = new Map<number, EmitterSubscription>();

/**
* Subscribes to messages for a topic
*
* @param topic The topic to subscribe to
* @param onMessageReceived The callback to invoke when a message is received
* @returns A Promise<number> containing the unique subscription reference. This will need to be stored for calling {@link unsubscribe}.
*/
export const subscribe = async (
  topic: string,
  onMessageReceived: (message: Message) => void
): Promise<number> => {
  const subscriptionRef = await IONPortalPubSub.subscribe(topic);

  const subscriber = PortalsPubSub.addListener(
    'PortalsSubscription',
    (message: Message) => {
      if (message.subscriptionRef === subscriptionRef) {
        onMessageReceived(message);
      }
    }
  );

  subscriptionMap.set(subscriptionRef, subscriber);

  return subscriptionRef;
};

/**
* Unsubscribes from events for the provided topic and subscription reference
*
* @param topic The topic to unsubscribe from
* @param subRef The unique subscription reference received when initially calling {@link subscribe}
*/
export const unsubscribe = (topic: string, subRef: number) => {
  IONPortalPubSub.unsubscribe(topic, subRef);

  const subscription = subscriptionMap.get(subRef);
  if (subscription !== undefined) {
    subscription.remove();
    subscriptionMap.delete(subRef);
  }
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

/**
* Validates that a valid registration key has been procured from http://ionic.io/register-portals
* @param key The registration key
*/
export const register = (key: string) => {
  IONPortalManager.register(key);
};

/**
* The configuration of a web application to be embedded in a React Native application.
*/
export interface Portal {
  /** The name of the Portal to be referenced. Must be **unique** */
  name: string;
  /** The classpath of all Capacitor plugins used in Android. (e.g. com.capacitorjs.plugins.camera.CameraPlugin) */
  androidPlugins?: string[];
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
  liveUpdate?: LiveUpdateConfig;
}

/**
* A subset of {@link Portal} properties needed for rendering a Portal. `initialContext` can be used to override 
* any initialContext defined in the original {@link Portal} definition. 
*/
export type PortalProp = {
  portal: Pick<Portal, 'name' | 'initialContext'>;
};

/**
* Props needed for rendering a {@link Portal}
*/
export type PortalProps = PortalProp & ViewProps;

/**
* Adds a Portal to an internal registry. Must be called before attempting to render a {@link PortalView}.
* 
* @param portal The portal to add to the internal registry.
*/
export const addPortal = (portal: Portal) => {
  IONPortalManager.addPortal(portal);
};

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

/** Used for communicating sync results of multiple live updates */
export interface SyncResults {
  liveUpdates: LiveUpdate[];
  errors: LiveUpdateError[];
}

/**
* Syncs a single live update.
* 
* @param appId The AppFlow application ID to sync.
* @returns A Promise<LiveUpdate>. A failure should result in a {@link LiveUpdateError}.
*/
export const syncOne = (appId: string): Promise<LiveUpdate> => {
  return IONLiveUpdatesManager.syncOne(appId);
};

/**
* Syncs many live updates.
*
* @param appIds The AppFlow application IDs to sync.
* @returns Promise<SyncResults>
*/
export const syncSome = (appIds: string[]): Promise<SyncResults> => {
  return IONLiveUpdatesManager.syncSome(appIds);
};

/**
* Syncs all registered LiveUpdates
* @returns Promise<SyncResults>
*/
export const syncAll = (): Promise<SyncResults> => {
  return IONLiveUpdatesManager.syncAll();
};
