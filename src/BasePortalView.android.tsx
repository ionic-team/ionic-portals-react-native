import React, { useEffect, useRef } from 'react';
import {
  findNodeHandle,
  requireNativeComponent,
  UIManager,
} from 'react-native';

const PortalViewManager = requireNativeComponent('AndroidPortalView');

const createFragment = (viewId: number) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command
    // @ts-expect-error
    UIManager.AndroidPortalView.Commands.create.toString(),
    [viewId]
  );

const BasePortalView = (props: any) => {
  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    if (viewId != null) {
      createFragment(viewId);
    }
  }, []);

  return <PortalViewManager {...props} ref={ref} />;
};

export default BasePortalView;
