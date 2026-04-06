import type { StorybookConfig } from "@storybook/react-vite";
import { mergeConfig, type Plugin } from "vite";
import path from "path";

// react-native → react-native-web 강제 해석 플러그인
function reactNativeWebAlias(): Plugin {
  const webRoot = path.dirname(
    require.resolve("react-native-web/package.json")
  );
  return {
    name: "react-native-web-alias",
    enforce: "pre",
    resolveId(source) {
      if (
        source === "react-native" ||
        source.startsWith("react-native/")
      ) {
        return { id: webRoot, moduleSideEffects: true };
      }
    },
  };
}

const config: StorybookConfig = {
  stories: [
    "../stories/**/*.stories.@(js|jsx|ts|tsx)",
    "../components/**/*.stories.@(js|jsx|ts|tsx)",
  ],
  addons: [
    "@storybook/addon-essentials",
    "@storybook/addon-interactions",
  ],
  framework: {
    name: "@storybook/react-vite",
    options: {},
  },
  viteFinal: async (config) => {
    return mergeConfig(config, {
      plugins: [reactNativeWebAlias()],
      resolve: {
        alias: {
          "react-native$": "react-native-web",
        },
      },
      optimizeDeps: {
        exclude: ["react-native"],
        include: ["react-native-web"],
      },
    });
  },
};

export default config;
