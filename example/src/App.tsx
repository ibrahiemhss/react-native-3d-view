import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import { ModelViewerView } from 'react-native-model-viewer';

export default function App() {
  return (
    <View style={styles.container}>
      <ModelViewerView
        url="http://176.126.237.165/beem/ship_no_materials.glb"
        color="#78909C"
        loadingColor="#1B5E20"
        style={styles.box}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: '100%',
    height: '50%',
  },
});
