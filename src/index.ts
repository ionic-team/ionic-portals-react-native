import {
  EmitterSubscription,
  NativeEventEmitter,
  NativeModules,
  ViewProps,
} from 'react-native';

const { IONPortalPubSub, IONPortalManager, IONLiveUpdatesManager } =
  NativeModules;

export { default as PortalView } from './PortalView';

export interface Message {
  subscriptionRef: number;
  data: any;
  topic: string;
}

const PortalsPubSub = new NativeEventEmitter(IONPortalPubSub);

const subscriptionMap = new Map<number, EmitterSubscription>();

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

export const unsubscribe = (topic: string, subRef: number) => {
  IONPortalPubSub.unsubscribe(topic, subRef);

  const subscription = subscriptionMap.get(subRef);
  if (subscription !== undefined) {
    subscription.remove();
    subscriptionMap.delete(subRef);
  }
};

export const publish = (topic: string, data: any) => {
  const msg = { message: data };
  IONPortalPubSub.publish(topic, msg);
};

export const register = (key: string) => {
  IONPortalManager.register(key);
};

export interface LiveUpdate {
  appId: string;
  channel: string;
  syncOnAdd: boolean;
}

export interface LiveUpdateError {
  appId: string;
  failStep: string;
  message: string;
}

export interface Portal {
  name: string;
  startDir?: string;
  initialContext?: {
    [key: string]: any;
  };
  liveUpdate?: LiveUpdate;
}

export interface SyncResults {
  liveUpdates: LiveUpdate[];
  errors: LiveUpdateError[];
}

export type PortalProps = Pick<Portal, 'name' | 'initialContext'> & ViewProps;

export const addPortal = (portal: Portal) => {
  IONPortalManager.addPortal(portal);
};

export const addLiveUpdate = (liveUpdate: LiveUpdate) => {
  IONLiveUpdatesManager.addLiveUpdate(liveUpdate);
};

export const syncOne = (appId: string): Promise<LiveUpdate> => {
  return IONLiveUpdatesManager.syncOne(appId);
};

export const syncSome = (appIds: string[]): Promise<SyncResults> => {
  return IONLiveUpdatesManager.syncSome(appIds);
};

export const syncAll = (): Promise<SyncResults> => {
  return IONLiveUpdatesManager.syncAll();
};
