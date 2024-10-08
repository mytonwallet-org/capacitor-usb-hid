import { registerPlugin } from '@capacitor/core';
import { identifyUSBProductId, ledgerUSBVendorId } from '@ledgerhq/devices';
import { DisconnectedDevice, DisconnectedDeviceDuringOperation } from '@ledgerhq/errors';
import Transport from '@ledgerhq/hw-transport';
import { log } from '@ledgerhq/logs';
import { concat, from, Subject } from 'rxjs/dist/types';
import { mergeMap } from 'rxjs/operators';
const CapacitorUSBPlugin = registerPlugin('CapacitorUSBPlugin', {
    web: () => import('./web').then(m => new m.CapacitorUSBPluginWeb()),
});
const disconnectedErrors = [
    'I/O error',
    "Attempt to invoke virtual method 'int android.hardware.usb.UsbDevice.getDeviceClass()' on a null object reference",
    'Invalid channel',
    'Permission denied by user for device',
];
export async function listLedgerDevices() {
    const devices = (await CapacitorUSBPlugin.getDeviceList()).devices;
    return devices.filter((d) => d.vendorId === ledgerUSBVendorId);
}
const liveDeviceEventsSubject = new Subject();
CapacitorUSBPlugin.addListener('onDeviceConnect', (device) => {
    if (device.vendorId !== ledgerUSBVendorId)
        return;
    const deviceModel = identifyUSBProductId(device.productId);
    liveDeviceEventsSubject.next({
        type: 'add',
        descriptor: device,
        deviceModel,
    });
});
CapacitorUSBPlugin.addListener('onDeviceDisconnect', (device) => {
    if (device.vendorId !== ledgerUSBVendorId)
        return;
    const deviceModel = identifyUSBProductId(device.productId);
    liveDeviceEventsSubject.next({
        type: 'remove',
        descriptor: device,
        deviceModel,
    });
});
const liveDeviceEvents = liveDeviceEventsSubject;
/**
 * Ledger's React Native HID Transport implementation
 * @example
 * import TransportHID from "@ledgerhq/react-native-hid";
 * ...
 * TransportHID.create().then(transport => ...)
 */
export class HIDTransport extends Transport {
    constructor(deviceId, productId) {
        super();
        this.deviceId = deviceId;
        this.deviceModel = identifyUSBProductId(productId);
    }
    /**
     * List currently connected devices.
     * @returns Promise of devices
     */
    static async list() {
        if (!this.isSupported)
            return Promise.resolve([]);
        return listLedgerDevices();
    }
    /**
     * Listen to ledger devices events
     */
    static listen(observer) {
        if (!this.isSupported) {
            return {};
        }
        return concat(from(listLedgerDevices()).pipe(mergeMap((devices) => from(devices.map((device) => ({
            type: 'add',
            descriptor: device,
            deviceModel: identifyUSBProductId(device.productId),
        }))))), liveDeviceEvents).subscribe(observer);
    }
    /**
     * Open a the transport with a Ledger device
     */
    static async open(deviceObj) {
        try {
            const result = await CapacitorUSBPlugin.openDevice({ deviceId: deviceObj.id });
            if (result.success) {
                return new HIDTransport(deviceObj.id, deviceObj.productId);
            }
            else {
                throw new Error();
            }
        }
        catch (error) {
            if (disconnectedErrors.includes(error.message)) {
                throw new DisconnectedDevice(error.message);
            }
            throw error;
        }
    }
    /**
     * @param {*} apdu input value
     * @returns Promise of apdu response
     */
    async exchange(apdu) {
        return this.exchangeAtomicImpl(async () => {
            try {
                const apduHex = apdu.toString('hex');
                log('apdu', `=> ${apduHex}`);
                const result = await CapacitorUSBPlugin.exchange({ deviceId: this.deviceId, apduHex });
                const resultHex = Buffer.from(result.response, 'hex');
                log('apdu', `<= ${resultHex}`);
                return resultHex;
            }
            catch (error) {
                if (disconnectedErrors.includes(error.message)) {
                    this.emit('disconnect', error);
                    throw new DisconnectedDeviceDuringOperation(error.message);
                }
                throw error;
            }
        });
    }
    /**
     * Close the transport
     * @returns Promise
     */
    async close() {
        await this.exchangeBusyPromise;
        void CapacitorUSBPlugin.closeDevice({ deviceId: this.deviceId });
    }
}
/**
 * Check if the transport is supported (basically true on Android)
 */
HIDTransport.isSupported = () => Promise.resolve(true);
export * from './definitions';
export { CapacitorUSBPlugin };
//# sourceMappingURL=index.js.map