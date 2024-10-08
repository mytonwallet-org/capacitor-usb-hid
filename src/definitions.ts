import type { PluginListenerHandle } from '@capacitor/core';

export interface ICapacitorUSBDevice {
  id: number;
  name: string;
  vendorId: number;
  productId: number;
}

export interface CapacitorUSBPluginPlugin {
  getDeviceList(): Promise<{
    devices: ICapacitorUSBDevice[]
  }>;
  openDevice(options: { deviceId: number }): Promise<{ success: boolean }>;
  exchange(options: { deviceId: number; apduHex: string }): Promise<{ response: string }>;
  closeDevice(options: { deviceId: number }): Promise<{ response: string }>;

  addListener(
    eventName: 'onDeviceConnect',
    handler: (device: ICapacitorUSBDevice) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;

  addListener(
    eventName: 'onDeviceDisconnect',
    handler: (device: ICapacitorUSBDevice) => void,
  ): Promise<PluginListenerHandle> & PluginListenerHandle;
}
