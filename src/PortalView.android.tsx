import React, { useEffect, useRef } from 'react';
import { findNodeHandle, requireNativeComponent, UIManager } from 'react-native';
import type { PortalProps } from '.';

const PortalViewManager = requireNativeComponent("AndroidPortalView")

const createFragment = (viewId: number | null) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command
    // @ts-expect-error
    UIManager.AndroidPortalView.Commands.create.toString(),
    [viewId]
  );

const PortalView = (props: PortalProps) => {
  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    createFragment(viewId);
  }, []);

  return <PortalViewManager  {...props} ref={ref} />
}

export default PortalView;
