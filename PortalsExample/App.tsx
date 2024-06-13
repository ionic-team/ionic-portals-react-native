import "@expo/metro-runtime"
import React, { useEffect, useRef, useState } from "react"
import { useColorScheme, EmitterSubscription, View, Text, StyleSheet } from "react-native"
import { Colors } from "react-native/Libraries/NewAppScreen"
// import { subscribe, publish, PortalView, Message }  from "../lib/typescript"
import { subscribe, publish, PortalView, Message } from "@ionic/portals-react-native"

const PubSubLabel: React.FC<{ initialNumber: number }> = ({ initialNumber }) => {
  const isDarkMode = useColorScheme() === "dark"
  const [immutableNumber, setNumber] = useState(initialNumber)
  const subRef = useRef<EmitterSubscription | null>(null)
  const number = useRef(initialNumber)

  useEffect(() => {
    subRef.current = subscribe("button:tapped", (message: Message) => {
      console.log(
        `Received message ${JSON.stringify(message.data, null, 2)} on topic ${
          message.topic
        } from IonicPortals`,
      )

      number.current = number.current + 1
      setNumber(number.current)

      publish("button:received", number.current + 1)
      if (number.current >= 5) {
        subRef.current?.remove()
      }
    })

    return () => {
      console.log("Unsubscribing from ref ", subRef)
      subRef.current?.remove()
    }
  }, [])

  return (
    <View style={styles.sectionContainer}>
      <Text
        style={[styles.sectionDescription, { color: isDarkMode ? Colors.white : Colors.black }]}
      >
        React Native Current Number: {immutableNumber}
      </Text>
    </View>
  )
}

function IgniteApp() {
  return (
    <>
      <PubSubLabel initialNumber={1} />
      <PortalView portal={{ name: "button" }} style={styles.portalView} />
    </>
  )
}

export default IgniteApp

const styles = StyleSheet.create({
  portalView: {
    flex: 1,
    height: 150,
  },
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionDescription: {
    fontSize: 18,
    fontWeight: "400",
    marginTop: 8,
  },
})
