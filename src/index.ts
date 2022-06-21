import {
  EmitterSubscription,
  NativeEventEmitter,
  NativeModules,
  ViewProps,
} from 'react-native';

const { IONPortalPubSub, IONPortalManager } = NativeModules;

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
  channel: 'production' | string;
}

export interface Portal {
  name: string;
  startDir?: string;
  initialContext?: {
    [key: string]: any;
  };
  liveUpdate?: LiveUpdate;
}

export type PortalProps = Pick<Portal, 'name' | 'initialContext' | 'liveUpdate'> & ViewProps;

export const addPortal = (portal: Portal) => {
  IONPortalManager.addPortal(portal);
};
