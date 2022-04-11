import React from 'react';
import { requireNativeComponent } from 'react-native';
import type { PortalProps } from '.';

const HostComponentPortal = requireNativeComponent("IONPortalView")

const PortalView = (props: PortalProps) => {
  return <HostComponentPortal {...props} />
}

export default PortalView;
