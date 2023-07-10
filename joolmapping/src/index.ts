import { registerPlugin } from '@capacitor/core';

import type { JoolMappingPlugin } from './definitions';

const JoolMapping = registerPlugin<JoolMappingPlugin>('JoolMapping', {
  web: () => import('./web').then(m => new m.JoolMappingWeb()),
});

export * from './definitions';
export { JoolMapping };
