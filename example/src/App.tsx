import * as React from 'react';
import PubSubLabel from './PubSubLabel';
import { StyleSheet, View } from 'react-native';
import {
  PortalView,
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  addPortal,
  register,
  registerWebVitals,
  type Portal,
} from '@ionic/portals-react-native';
import { useEffect } from 'react';

register(
  'eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiIzMDU2ZjBlNC1kYTFkLTQ1YWMtYWJjZi1hNDg3MzMyZTQwNGYifQ.0H_gnwXCL1Z-GtFCwQ3J9YrybMxQO56CYo3PFGzoueB56DMvKT4jiQhLzhDYKEE5GwlqX-r0H_qklYKg_jtMyK9QZ_-kTNWi6LyjrJTcgFVwxjz27PZaqZPoKWyJLotSIbBhN8BF5flunCGW8kWL4_nY6FUmswatPDgcvyPmOydr9InbEHHDUVvi9mGwy_G78BjDrl9bThezpGRseBTOI7KH5FUdXwH9DCZJ2RC4_ukTNKMqaKFh-OcD8KDBUIdSP8GE0quO7zL4qSINvxMMzpupTdQKf3Td5B1mvLlrS4kF_8VPoQtvB8JqMrmH2fa8f31fCiz1EV4Wkngb_5yC7w'
);

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
          // webVitals={{
          //   firstContentfulPaint: (duration: number) =>
          //     console.log('firstContentfulPaint', duration),
          //   firstInputDelay: (duration: number) =>
          //     console.log('firstInputDelay', duration),
          //   timeToFirstByte: (duration: number) => {
          //     console.log('timeToFirstByte', duration);
          //   },
          // }}
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
