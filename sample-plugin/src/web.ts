import { WebPlugin } from '@capacitor/core';

import type { ExamplePlugin } from './definitions';

export class ExampleWeb extends WebPlugin implements ExamplePlugin {
  async startWork(): Promise<{ workId: string }> {
    console.log('Fetching files from firebase!!');
    const options = { workId: 'work_id' };
    return options;
  }
}
