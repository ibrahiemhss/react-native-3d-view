# react-native-model-viewer

GLB and GLTF model viewer

## Installation

```sh
npm react-native-3d-view
```

## Usage


```js
import { ModelViewerView } from "react-native-model-viewer";

// ...
export default function App() {
  return (
    <View style={styles.container}>
      <ThreeDView
        url="http://176.126.237.165/beem/ship_no_materials.glb"
        color="#78909C"
        duration={20}
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
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
