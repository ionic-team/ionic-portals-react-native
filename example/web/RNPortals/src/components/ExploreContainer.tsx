import './ExploreContainer.css';
import { IonButton } from '@ionic/react';
import Portals, { PortalSubscription } from '@ionic/portals';
import { useEffect, useState } from 'react';

interface ContainerProps { 
  initialNumber: number
}

interface Message {
  data: { message: number }; 
  topic: string;
}

const ExploreContainer: React.FC<ContainerProps> = (props: ContainerProps) => {
  const [ subscription, setSubscription ] = useState<PortalSubscription>();
  const [ appMessage, setAppMessage ] = useState<number>();

  const topic = "button:tapped";
  const subTopic = "button:received";
  const message = "tapped";

  useEffect(() => {
    subscribe(); 

    return () => {
      unsubscribe();
    };
  }, [])

  const subscribe = async () => {
    const subscription = await Portals.subscribe(
      { topic: subTopic },
      (result: Message) => { 
        console.log(`Got message from ReactNative ${JSON.stringify(result)}`)
        setAppMessage(result.data.message);
      }
    );
    setSubscription(subscription);
  };

  const unsubscribe = async () => {
    if (subscription !== undefined) {
      Portals.unsubscribe(subscription); 
    }
  };

  const publish = async () => {
    Portals.publish({ topic, data: { message }});
  };

  return (
    <div className="container">
      <strong>Hello from Ionic Portals</strong>
      <IonButton onClick={ async () => await publish() }>Next number on tap will be {appMessage === undefined ? props.initialNumber : appMessage }</IonButton>
    </div>
  );
};

export default ExploreContainer;
