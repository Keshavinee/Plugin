import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'io.ionic.starter',
  appName: 'sampleApp',
  webDir: 'www',
  server: {
    androidScheme: 'https'
  },
  plugins: {
    ExamplePlugin: {
      pluginConfig: true
   }
  }
};

export default config;
