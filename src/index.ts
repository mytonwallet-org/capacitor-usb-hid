import { registerPlugin } from '@capacitor/core';

import type { CapacitorUSBPluginPlugin } from './definitions';

const CapacitorUSBPlugin = registerPlugin<CapacitorUSBPluginPlugin>(
  'CapacitorUSBPlugin',
  {
    web: () => import('./web').then(m => new m.CapacitorUSBPluginWeb()),
  },
);

export * from './definitions';
export { CapacitorUSBPlugin };
