import { WebPlugin } from '@capacitor/core';
import type { CapacitorUSBPluginPlugin } from './definitions';
export declare class CapacitorUSBPluginWeb extends WebPlugin implements CapacitorUSBPluginPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
