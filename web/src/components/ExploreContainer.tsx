import './ExploreContainer.css';
import { IonButton } from '@ionic/react';
import { subscribe, publish } from '@ionic/portals';
import { useEffect, useRef, useState } from 'react';
import { PluginListenerHandle } from '@capacitor/core';

interface ContainerProps {
  initialNumber: number
}

interface Message {
  data: { message: number };
  topic: string;
}

const ExploreContainer: React.FC<ContainerProps> = (props: ContainerProps) => {
  const subscription = useRef<PluginListenerHandle | null>(null);
  const [appMessage, setAppMessage] = useState<number>();

  const topic = "button:tapped";
  const subTopic = "button:received";
  const message = "tapped";

  useEffect(() => {
    sub();

    return () => {
      subscription.current?.remove()
    };
  }, [])

  const sub = async () => {
    subscription.current = await subscribe(
      subTopic,
      (result: Message) => {
        console.log(`Got message from ReactNative ${JSON.stringify(result)}`)
        setAppMessage(result.data.message);
      }
    );
  };

  const pub = async () => {
    publish({ topic, data: { message } });
  };

  const openCameraPub = async () => {
    publish({ topic: "openCameraButton:tapped", data: { message } });
  };

  return (
    <div className="container">
      <strong>Hello from Ionic Portals</strong>
      <IonButton onClick={async () => await pub()}>Next number on tap will be {appMessage === undefined ? props.initialNumber : appMessage}</IonButton>
      <IonButton onClick={async () => await openCameraPub()}>This button will open Camera</IonButton>
    </div>
  );
};

export default ExploreContainer;
