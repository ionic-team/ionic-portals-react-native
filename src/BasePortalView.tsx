import React from 'react';
import { requireNativeComponent } from 'react-native';

const HostComponentPortal = requireNativeComponent('IONPortalView');

const BasePortalView = (props: any) => {
  return <HostComponentPortal {...props} />;
};

export default BasePortalView;
