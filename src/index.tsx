import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-3d-view' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type ModelViewerProps = {
  color: string;
  url: string;
  loadingColor: string;
  duration: number;
  style: ViewStyle;
};

const ComponentName = 'RN3dView';

export const ThreeDView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<ModelViewerProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };
