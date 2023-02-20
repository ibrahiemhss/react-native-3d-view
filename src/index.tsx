import {
  requireNativeComponent,
  UIManager,
  Platform,
  ViewStyle,
} from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-model-viewer' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

type ModelViewerProps = {
  color: string;
  style: ViewStyle;
};

const ComponentName = 'ModelViewerView';

export const ModelViewerView =
  UIManager.getViewManagerConfig(ComponentName) != null
    ? requireNativeComponent<ModelViewerProps>(ComponentName)
    : () => {
        throw new Error(LINKING_ERROR);
      };
