/* eslint-disable */
import React, { useState } from 'react';
import {
  View,
  Text,
  Button,
  StyleSheet,
  ScrollView,
  ActivityIndicator,
} from 'react-native';
import { PortalView, type Portal } from '@ionic/portals-react-native';

/**
 * Example component demonstrating how to use the Mock Live Updates Provider.
 *
 * The mock provider is configured via the portal's liveUpdate.liveUpdateConfig property.
 * You can test different scenarios by setting different configuration options:
 *
 * - appId: (required) The app ID for the live update
 * - channel: (optional) The channel name (defaults to "production")
 * - providerId: Set to "ionic-mock" to use the mock provider
 * - shouldFail: Set to true to simulate a sync failure
 * - didUpdate: Set to true to simulate a successful update
 * - failureDetails: Custom error message for failed syncs
 */
export default function MockLiveUpdatesExample() {
  const [syncResult, setSyncResult] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  // Example 1: Successful sync with update
  const portalWithUpdate: Portal = {
    name: 'mock-portal-success',
    startDir: 'portals/buttonapp',
    initialContext: { message: 'Mock Live Updates - Success Case' },
    liveUpdate: {
      appId: 'mock-app-123',
      channel: 'production',
      syncOnAdd: false,
      // @ts-ignore - Additional mock provider config
      providerId: 'ionic-mock',
      providerConfig: {
        shouldFail: false,
        didUpdate: true,
        endpoint: 'https://mockprovider.io/sync',
        apiKey: '<PROVIDER_API_KEY>',
      },
    },
  };

  // Example 2: Successful sync with no update
  const portalNoUpdate: Portal = {
    name: 'mock-portal-no-update',
    startDir: 'portals/buttonapp',
    initialContext: { message: 'Mock Live Updates - No Update Case' },
    liveUpdate: {
      appId: 'mock-app-456',
      channel: 'development',
      syncOnAdd: false,
      // @ts-ignore - Additional mock provider config
      providerId: 'ionic-mock',
      providerConfig: {
        shouldFail: false,
        didUpdate: false,
        endpoint: 'https://mockprovider.io/sync',
        apiKey: '<PROVIDER_API_KEY>',
      },
    },
  };

  // Example 3: Failed sync
  const portalFailed: Portal = {
    name: 'mock-portal-failed',
    startDir: 'portals/buttonapp',
    initialContext: { message: 'Mock Live Updates - Failure Case' },
    liveUpdate: {
      appId: 'mock-app-789',
      channel: 'staging',
      syncOnAdd: false,
      // @ts-ignore - Additional mock provider config
      providerId: 'ionic-mock',
      providerConfig: {
        shouldFail: true,
        didUpdate: false,
        failureDetails: 'Network error: Unable to reach update server',
        endpoint: 'https://mockprovider.io/sync',
        apiKey: '<PROVIDER_API_KEY>',
      },
    },
  };

  const testMockSync = async (testCase: 'success' | 'no-update' | 'failure') => {
    setIsLoading(true);
    setSyncResult('');

    try {
      // Import syncOne dynamically to avoid module errors
      const { syncOne } = require('@ionic/portals-react-native');

      let appId: string;
      switch (testCase) {
        case 'success':
          appId = 'mock-app-123';
          break;
        case 'no-update':
          appId = 'mock-app-456';
          break;
        case 'failure':
          appId = 'mock-app-789';
          break;
      }

      const result = await syncOne(appId);
      setSyncResult(
        `✅ Sync Successful!\n\n` +
        `App ID: ${result.appId || result.liveUpdate?.appId || appId}\n` +
        `Did Update: ${result.didUpdate ?? result.activeApplicationPathChanged}\n` +
        (result.latestAppDirectory ? `Latest Directory: ${result.latestAppDirectory}\n` : '') +
        (result.liveUpdate ? `Channel: ${result.liveUpdate.channel}\n` : '') +
        (result.source ? `Source: ${result.source}\n` : '') +
        (result.snapshot?.id ? `Snapshot ID: ${result.snapshot.id}` : '')
      );
    } catch (error: any) {
      setSyncResult(
        `❌ Sync Failed!\n\n` +
        `Error: ${error.message || JSON.stringify(error)}`
      );
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>Mock Live Updates Provider Test</Text>
      <Text style={styles.description}>
        This example demonstrates the Mock Live Updates Provider using the
        providerId and providerConfig in the portal's liveUpdate configuration.
        The portal views are initialized below (hidden) to register the custom
        managers, then you can test sync operations without real network requests.
      </Text>

      {/* Hidden PortalViews to initialize the managers */}
      <View style={{ height: 0, overflow: 'hidden' }}>
        <PortalView
          portal={portalWithUpdate}
          style={{ height: 1, width: 1 }}
        />
        <PortalView
          portal={portalNoUpdate}
          style={{ height: 1, width: 1 }}
          onLoad={() => console.log('Portal no update initialized')}
        />
        <PortalView
          portal={portalFailed}
          style={{ height: 1, width: 1 }}
          onLoad={() => console.log('Portal failed initialized')}
        />
      </View>

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Test Scenarios</Text>

        <View style={styles.buttonContainer}>
          <Button
            title="Test Successful Sync (with update)"
            onPress={() => testMockSync('success')}
            disabled={isLoading}
          />
        </View>

        <View style={styles.buttonContainer}>
          <Button
            title="Test Successful Sync (no update)"
            onPress={() => testMockSync('no-update')}
            disabled={isLoading}
          />
        </View>

        <View style={styles.buttonContainer}>
          <Button
            title="Test Failed Sync"
            onPress={() => testMockSync('failure')}
            disabled={isLoading}
          />
        </View>
      </View>

      {isLoading && (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" />
          <Text style={styles.loadingText}>Syncing...</Text>
        </View>
      )}

      {syncResult !== '' && (
        <View style={styles.resultContainer}>
          <Text style={styles.resultTitle}>Result:</Text>
          <Text style={styles.resultText}>{syncResult}</Text>
        </View>
      )}

      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Configuration Example</Text>
        <Text style={styles.codeText}>
          {`const portal: Portal = {
  name: 'my-portal',
  startDir: 'portals/myapp',
  liveUpdate: {
    appId: 'mock-app-123',
    channel: 'production',
    syncOnAdd: false,
    providerId: 'ionic-mock',
    didUpdate: true,
    shouldFail: false,
  }
};`}
        </Text>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    padding: 20,
    backgroundColor: '#f5f5f5',
  },
  title: {
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  description: {
    fontSize: 14,
    color: '#666',
    marginBottom: 20,
    lineHeight: 20,
  },
  section: {
    marginBottom: 20,
    backgroundColor: 'white',
    padding: 15,
    borderRadius: 8,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  buttonContainer: {
    marginBottom: 10,
  },
  initializingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    padding: 15,
    backgroundColor: '#e3f2fd',
    borderRadius: 8,
    marginBottom: 15,
  },
  initializingText: {
    marginLeft: 10,
    color: '#1976d2',
    fontSize: 14,
  },
  loadingContainer: {
    alignItems: 'center',
    padding: 20,
  },
  loadingText: {
    marginTop: 10,
    color: '#666',
  },
  resultContainer: {
    backgroundColor: 'white',
    padding: 15,
    borderRadius: 8,
    marginBottom: 20,
  },
  resultTitle: {
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 10,
    color: '#333',
  },
  resultText: {
    fontSize: 14,
    fontFamily: 'monospace',
    color: '#444',
    lineHeight: 20,
  },
  codeText: {
    fontSize: 12,
    fontFamily: 'monospace',
    backgroundColor: '#f0f0f0',
    padding: 10,
    borderRadius: 4,
    color: '#333',
  },
});
