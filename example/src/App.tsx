/* eslint-disable */
import * as React from 'react';
import PubSubLabel from './PubSubLabel';
import { StyleSheet, View } from 'react-native';
import {
  PortalView,
  addPortal,
  registerWebVitals,
  type Portal,
} from '@ionic/portals-react-native';
import { useEffect } from 'react';

var portal: Portal = {
  name: 'button',
  startDir: 'portals/buttonapp',
  initialContext: {
    initialNumber: 1,
  },
};

export default function App() {
  // If using the old deprecated API, you can use the following code to register web vitals:
  // const [ready, setReady] = React.useState(false);
  // const setupPortal = async () => {
  //   await addPortal(portal);
  //   await registerWebVitals(
  //     portal.name,
  //     (duration: number) => console.log('firstContentfulPaint', duration),
  //     (duration: number) => console.log('firstInputDelay', duration),
  //     (duration: number) => {
  //       console.log('timeToFirstByte', duration);
  //     }
  //   );
  // };

  // useEffect(() => {
  //   setupPortal().then(() => {
  //     setReady(true);
  //   });
  // }, []);
  const initialNumber = 0;
  return (
    <View style={styles.container}>
      <PubSubLabel initialNumber={initialNumber} />
      {/* {ready ? ( */}
      <PortalView
        portal={portal}
        style={styles.portalView}
        webVitals={{
          firstContentfulPaint: (duration: number) =>
            console.log('firstContentfulPaint', duration),
          firstInputDelay: (duration: number) =>
            console.log('firstInputDelay', duration),
          timeToFirstByte: (duration: number) => {
            console.log('timeToFirstByte', duration);
          },
        }}
      />
      {/* ) : (
        <></>
      )} */}
    </View>
  );
}

const styles = StyleSheet.create({
  portalView: {
    width: 300,
    height: 150,
    borderWidth: 2,
    borderColor: 'black',
  },
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
});
