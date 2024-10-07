package org.mytonwallet.capacitor.usb.hid;

import android.util.Log;

public class CapacitorUSBPlugin {

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }
}
