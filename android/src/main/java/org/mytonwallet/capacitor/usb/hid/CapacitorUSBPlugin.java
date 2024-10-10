package org.mytonwallet.capacitor.usb.hid;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;

@CapacitorPlugin(name = "CapacitorUSBPlugin")
public class CapacitorUSBPlugin extends Plugin {

    private HIDDevice hidDevice;
    private UsbManager usbManager;

    private BroadcastReceiver usbReceiver;
    private boolean usbReceiverRegistered = false;

    private static final String ACTION_USB_PERMISSION = "org.mytonwallet.capacitor.usb.hid.USB_PERMISSION";

    @Override
    public void load() {
        super.load();

        usbManager = (UsbManager) getContext().getSystemService(Context.USB_SERVICE);
        usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        onDeviceStateChanged("onDeviceConnect", device);
                    }
                } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device != null) {
                        onDeviceStateChanged("onDeviceDisconnect", device);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        ContextCompat.registerReceiver(getContext(), usbReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        usbReceiverRegistered = true;
    }

    @Override
    protected void handleOnDestroy() {
        super.handleOnDestroy();
        if (usbReceiverRegistered && usbReceiver != null) {
            getContext().unregisterReceiver(usbReceiver);
            usbReceiverRegistered = false;
        }
    }

    private JSObject deviceAsJSObject(UsbDevice device) {
        JSObject ret = new JSObject();
        ret.put("id", device.getDeviceId());
        ret.put("name", device.getDeviceName());
        ret.put("vendorId", device.getVendorId());
        ret.put("productId", device.getProductId());
        return ret;
    }

    private void onDeviceStateChanged(String event, UsbDevice device) {
        notifyListeners(event, deviceAsJSObject(device));
    }

    @PluginMethod
    public void getDeviceList(PluginCall call) {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        JSArray devices = new JSArray();
        for (UsbDevice device : deviceList.values()) {
            devices.put(deviceAsJSObject(device));
        }

        JSObject responseObject = new JSObject();
        responseObject.put("devices", devices);
        call.resolve(responseObject);
    }

    @PluginMethod
    public void openDevice(PluginCall call) {
        int deviceId = call.getInt("deviceId");

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        if (hidDevice != null) {
            hidDevice.close(null);
            hidDevice = null;
        }
        UsbDevice selectedDevice = null;
        for (UsbDevice device : deviceList.values()) {
            if (device.getDeviceId() == deviceId) {
                selectedDevice = device;
                break;
            }
        }

        if (selectedDevice != null) {
            if (usbManager.hasPermission(selectedDevice)) {
                openSelectedDevice(selectedDevice, call);
            } else {
                requestUsbPermission(usbManager, selectedDevice, call);
            }
        } else {
            call.reject("Device not found");
        }
    }

    private void requestUsbPermission(UsbManager manager, UsbDevice device, PluginCall p) {
        try {
            PendingIntent permIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
            registerBroadcastReceiver(p);
            manager.requestPermission(device, permIntent);
        } catch (Exception e) {
            p.reject(e.getMessage());
        }
    }

    private void registerBroadcastReceiver(final PluginCall p) {
        IntentFilter intFilter = new IntentFilter(ACTION_USB_PERMISSION);
        final BroadcastReceiver receiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                    synchronized (this) {
                        UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            openSelectedDevice(device, p);
                        } else {
                            p.reject("Permission denied by user for device");
                        }
                    }
                }
                unregisterReceiver(this);
            }
        };
        ContextCompat.registerReceiver(getContext(), receiver, intFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterReceiver(BroadcastReceiver receiver) {
        getActivity().getApplicationContext().unregisterReceiver(receiver);
    }

    private void openSelectedDevice(UsbDevice device, @Nullable PluginCall call) {
        try {
            hidDevice = new HIDDevice(usbManager, device);
            if (call != null)
                call.resolve(new JSObject().put("success", true));
        } catch (Exception e) {
            e.printStackTrace();
            if (call != null)
                call.resolve(new JSObject().put("success", false));
        }
    }

    @PluginMethod
    public void closeDevice(PluginCall call) {
        int deviceId = call.getInt("deviceId");

        if (hidDevice == null || hidDevice.getDeviceId() != deviceId)
            return;

        hidDevice.close(null);
        hidDevice = null;
    }

    @PluginMethod
    public void exchange(PluginCall call) {
        int deviceId = call.getInt("deviceId");
        String apduHex = call.getString("apduHex");

        if (hidDevice == null || hidDevice.getDeviceId() != deviceId) {
            call.reject("No device connected");
            return;
        }

        byte[] apduCommand = hexToBin(apduHex);
        try {
            hidDevice.exchange(apduCommand, call);
        } catch (Exception e) {
            e.printStackTrace();
            call.reject(e.getMessage());
        }
    }

    public static byte[] hexToBin(String src) {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        int i = 0;
        while (i < src.length()) {
            char x = src.charAt(i);
            if (!((x >= '0' && x <= '9') || (x >= 'A' && x <= 'F') || (x >= 'a' && x <= 'f'))) {
                i++;
                continue;
            }
            try {
                result.write(Integer.valueOf("" + src.charAt(i) + src.charAt(i + 1), 16));
                i += 2;
            } catch (Exception e) {
                return null;
            }
        }
        return result.toByteArray();
    }

}
