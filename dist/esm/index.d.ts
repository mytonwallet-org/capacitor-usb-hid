/// <reference types="node" />
import type { DeviceModel } from '@ledgerhq/devices';
import Transport from '@ledgerhq/hw-transport';
import type { CapacitorUSBPluginPlugin, ICapacitorUSBDevice } from './definitions';
declare const CapacitorUSBPlugin: CapacitorUSBPluginPlugin;
declare type DeviceObj = ICapacitorUSBDevice;
export declare function listLedgerDevices(): Promise<DeviceObj[]>;
/**
 * Ledger's React Native HID Transport implementation
 * @example
 * import TransportHID from "@ledgerhq/react-native-hid";
 * ...
 * TransportHID.create().then(transport => ...)
 */
export declare class HIDTransport extends Transport {
    deviceId: number;
    deviceModel: DeviceModel | null | undefined;
    constructor(deviceId: number, productId: number);
    /**
     * Check if the transport is supported (basically true on Android)
     */
    static isSupported: () => Promise<boolean>;
    /**
     * List currently connected devices.
     * @returns Promise of devices
     */
    static list(): Promise<any[]>;
    /**
     * Listen to ledger devices events
     */
    static listen(observer: any): any;
    /**
     * Open a the transport with a Ledger device
     */
    static open(deviceObj: DeviceObj): Promise<HIDTransport>;
    /**
     * @param {*} apdu input value
     * @returns Promise of apdu response
     */
    exchange(apdu: Buffer): Promise<any>;
    /**
     * Close the transport
     * @returns Promise
     */
    close(): Promise<void>;
}
export * from './definitions';
export { CapacitorUSBPlugin };
