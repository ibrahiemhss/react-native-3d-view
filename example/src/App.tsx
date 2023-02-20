import * as React from 'react';

import { StyleSheet, View } from 'react-native';
import { ModelViewerView } from 'react-native-model-viewer';

export default function App() {
  return (
    <View style={styles.container}>
      <ModelViewerView
        url="https://res.cloudinary.com/demo/image/upload/DamagedHelmet3D.glb"
        color="#AB47BC"
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
    height: '100%',
    marginVertical: 20,
  },
});
