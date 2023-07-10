import { registerPlugin } from '@capacitor/core';
const JoolMapping = registerPlugin('JoolMapping', {
    web: () => import('./web').then(m => new m.JoolMappingWeb()),
});
export * from './definitions';
export { JoolMapping };
//# sourceMappingURL=index.js.map