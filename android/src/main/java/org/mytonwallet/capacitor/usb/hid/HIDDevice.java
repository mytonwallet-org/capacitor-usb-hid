
package org.mytonwallet.capacitor.usb.hid;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import androidx.annotation.Nullable;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class HIDDevice {

    private final UsbDevice connectedDevice;
    private final UsbDeviceConnection connection;
    private final UsbInterface dongleInterface;
    private final UsbEndpoint in;
    private final UsbEndpoint out;
    private final byte[] transferBuffer;
    private boolean debug;
    private final ExecutorService executor;

    public HIDDevice(UsbManager manager, UsbDevice device) throws Exception {
        UsbInterface dongleInterface = device.getInterface(0);
        UsbEndpoint in = null;
        UsbEndpoint out = null;
        for (int i = 0; i < dongleInterface.getEndpointCount(); i++) {
            UsbEndpoint tmpEndpoint = dongleInterface.getEndpoint(i);
            if (tmpEndpoint.getDirection() == UsbConstants.USB_DIR_IN) {
                in = tmpEndpoint;
            } else {
                out = tmpEndpoint;
            }
        }
        UsbDeviceConnection connection = manager.openDevice(device);
        if (connection == null) {
            throw new Exception();
        }
        connection.claimInterface(dongleInterface, true);

        this.connectedDevice = device;
        this.connection = connection;
        this.dongleInterface = dongleInterface;
        this.in = in;
        this.out = out;

        transferBuffer = new byte[HID_BUFFER_SIZE];
        executor = Executors.newSingleThreadExecutor();
    }

    public void exchange(final byte[] commandSource, final PluginCall p) throws Exception {
        Runnable exchange = new Runnable() {
            public void run() {
                try {
                    ByteArrayOutputStream response = new ByteArrayOutputStream();
                    byte[] responseData = null;
                    int offset = 0;
                    int responseSize;
                    byte[] command = LedgerHelper.wrapCommandAPDU(LEDGER_DEFAULT_CHANNEL, commandSource, HID_BUFFER_SIZE);
                    if (debug) {
                        Log.d("SHIDDevice", "=> " + toHex(command));
                    }

                    UsbRequest request = new UsbRequest();
                    if (!request.initialize(connection, out)) {
                        throw new Exception("I/O error");
                    }
                    while (offset != command.length) {
                        int blockSize = (Math.min(command.length - offset, HID_BUFFER_SIZE));
                        System.arraycopy(command, offset, transferBuffer, 0, blockSize);
                        if (!request.queue(ByteBuffer.wrap(transferBuffer), HID_BUFFER_SIZE)) {
                            request.close();
                            throw new Exception("I/O error");
                        }
                        connection.requestWait();
                        offset += blockSize;
                    }
                    ByteBuffer responseBuffer = ByteBuffer.allocate(HID_BUFFER_SIZE);
                    request = new UsbRequest();
                    if (!request.initialize(connection, in)) {
                        request.close();
                        throw new Exception("I/O error");
                    }

                    while ((responseData = LedgerHelper.unwrapResponseAPDU(LEDGER_DEFAULT_CHANNEL, response.toByteArray(),
                            HID_BUFFER_SIZE)) == null) {
                        responseBuffer.clear();
                        if (!request.queue(responseBuffer, HID_BUFFER_SIZE)) {
                            request.close();
                            throw new Exception("I/O error");
                        }
                        connection.requestWait();
                        responseBuffer.rewind();
                        responseBuffer.get(transferBuffer, 0, HID_BUFFER_SIZE);
                        response.write(transferBuffer, 0, HID_BUFFER_SIZE);
                    }

                    if (debug) {
                        Log.d("SHIDDevice", "<= " + toHex(responseData));
                    }

                    request.close();
                    p.resolve(new JSObject().put("response", toHex(responseData)));
                } catch (Exception e) {
                    e.printStackTrace();
                    p.reject(e.getMessage());
                }
            }
        };
        this.executor.submit(exchange);
    }

    public static String toHex(byte[] buffer, int offset, int length) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            String temp = Integer.toHexString((buffer[offset + i]) & 0xff);
            if (temp.length() < 2) {
                temp = "0" + temp;
            }
            result.append(temp);
        }
        return result.toString();
    }

    public static String toHex(byte[] buffer) {
        return toHex(buffer, 0, buffer.length);
    }

    public void close(@Nullable PluginCall p) {
        try {
            connection.releaseInterface(dongleInterface);
            connection.close();
            this.executor.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (p != null)
            p.resolve(null);
    }

    public void setDebug(boolean debugFlag) {
        this.debug = debugFlag;
    }

    public int getDeviceId() {
        return connectedDevice.getDeviceId();
    }

    private static final int HID_BUFFER_SIZE = 64;
    private static final int LEDGER_DEFAULT_CHANNEL = 1;

}
