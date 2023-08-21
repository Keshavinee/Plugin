import { registerPlugin } from '@capacitor/core';

import type { ExamplePlugin } from './definitions';

const ExamplePlugin = registerPlugin<ExamplePlugin>('Example', {
  web: () => import('./web').then(m => new m.ExampleWeb()),
});

export * from './definitions';
export { ExamplePlugin };
