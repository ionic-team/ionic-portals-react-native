/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * Generated with the TypeScript template
 * https://github.com/react-native-community/react-native-template-typescript
 *
 * @format
 */

import React, {useEffect, useRef, useState} from 'react';

import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';

import {
  register,
  addPortal,
  subscribe,
  unsubscribe,
  publish,
  Message,
  PortalView,
} from '@ionic/portals-react-native';

register('YOUR_PORTAL_KEY_HERE');

const portal = {
  name: 'button',
  startDir: 'portals/buttonapp',
  initialContext: {
    initialNumber: 2,
  },
};

addPortal(portal);

const PubSubLabel: React.FC<{initialNumber: number}> = ({initialNumber}) => {
  const isDarkMode = useColorScheme() === 'dark';
  const [immutableNumber, setNumber] = useState(initialNumber);
  const [subRef, setSubRef] = useState(0);
  const number = useRef(initialNumber);

  useEffect(() => {
    const subscribeToButtonTapped = async () => {
      console.log('subscribing');
      const subRef = await subscribe('button:tapped', (message: Message) => {
        console.log(`Received message ${JSON.stringify(message.data, null, 2)} on topic ${message.topic} from IonicPortals`);

        number.current = number.current + 1;
        setNumber(number.current);

        publish('button:received', number.current + 1);
      });

      console.log('subscribed with subRef ', subRef);
      setSubRef(subRef);
    };

    subscribeToButtonTapped().catch(reason =>
      console.log('Failed for ', reason),
    );

    return () => {
      console.log('Unsubscribing from ref ', subRef);
      unsubscribe('button:tapped', subRef);
    };
  }, []);

  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[
          styles.sectionDescription,
          {color: isDarkMode ? Colors.white : Colors.black},
        ]}>
        React Native Current Number: {immutableNumber}
      </Text>
    </View>
  );
};

const Section: React.FC<{
  title: string;
}> = ({children, title}) => {
  const isDarkMode = useColorScheme() === 'dark';
  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[
          styles.sectionTitle,
          {
            color: isDarkMode ? Colors.white : Colors.black,
          },
        ]}>
        {title}
      </Text>
      <Text
        style={[
          styles.sectionDescription,
          {
            color: isDarkMode ? Colors.light : Colors.dark,
          },
        ]}>
        {children}
      </Text>
    </View>
  );
};

const App = () => {
  const isDarkMode = useColorScheme() === 'dark';

  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  return (
    <SafeAreaView style={backgroundStyle}>
      <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        style={backgroundStyle}>
        <Header />
        <View
          style={{
            backgroundColor: isDarkMode ? Colors.black : Colors.white,
          }}>
          <PubSubLabel initialNumber={1} />
          <PortalView name="button" style={{flex: 1, height: 150}} />
          <Section title="Step One">
            Edit <Text style={styles.highlight}>App.tsx</Text> to change this
            screen and then come back to see your edits.
          </Section>
          <Section title="See Your Changes">
            <ReloadInstructions />
          </Section>
          <Section title="Debug">
            <DebugInstructions />
          </Section>
          <Section title="Learn More">
            Read the docs to discover what to do next:
          </Section>
          <LearnMoreLinks />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
