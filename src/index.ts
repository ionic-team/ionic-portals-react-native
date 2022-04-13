import { NativeEventEmitter, NativeModules, ViewProps } from 'react-native';

const { IONPortalsPubSub, IONPortalManager } = NativeModules;

export { default as PortalView } from './PortalView';

export interface Message {
  subscriptionRef: number;
  data: any;
  topic: string;
}

const PortalsPubSub = new NativeEventEmitter(IONPortalsPubSub);

let subscriptionRefDict: any = {};

export const subscribe = async (
  topic: string,
  onMessageReceived: (message: Message) => void
): Promise<number> => {
  const subscriptionRef = await IONPortalsPubSub.subscribe(topic);
  subscriptionRefDict[subscriptionRef] = PortalsPubSub.addListener(
    'PortalsSubscription',
    onMessageReceived
  );
  return subscriptionRef;
};

export const unsubscribe = (topic: string, subRef: number) => {
  IONPortalsPubSub.unsubscribe(topic, subRef);
  subscriptionRefDict[subRef] = null;
};

export const publish = (topic: string, data: any) => {
  const msg = { message: data };
  IONPortalsPubSub.publish(topic, msg);
};

export const register = (key: string) => {
  IONPortalManager.register(key);
};

export interface Portal {
  name: string;
  startDir?: string;
  initialContext?: any;
}

export type PortalProps = Pick<Portal, 'name' | 'initialContext'> & ViewProps;

export const addPortal = (portal: Portal) => {
  IONPortalManager.addPortal(portal);
};
