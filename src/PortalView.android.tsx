import React, { useEffect, useRef } from 'react';
import { findNodeHandle, requireNativeComponent, UIManager } from 'react-native';

const PortalViewManager = requireNativeComponent("AndroidPortalView")

const createFragment = (viewId:any) =>
  UIManager.dispatchViewManagerCommand(
    viewId,
    // we are calling the 'create' command
    // @ts-expect-error
    UIManager.AndroidPortalView.Commands.create.toString(),
    [viewId]
  );

export default (props:any) => {
  const ref = useRef(null);

  useEffect(() => {
    const viewId = findNodeHandle(ref.current);
    createFragment(viewId);
  }, []);

  return (
    <PortalViewManager 
    {...props}
    portal={props.portal}
    ref={ref}/>
  );
};
