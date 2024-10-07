import { WebPlugin } from '@capacitor/core';

import type { CapacitorUSBPluginPlugin } from './definitions';

export class CapacitorUSBPluginWeb
  extends WebPlugin
  implements CapacitorUSBPluginPlugin
{
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
