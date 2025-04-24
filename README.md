# mtw-capacitor-usb-hid

A Capacitor USB HID Plugin

## Install

```bash
npm install mtw-capacitor-usb-hid
npx cap sync
```

## API

<docgen-index>

* [`getDeviceList()`](#getdevicelist)
* [`openDevice(...)`](#opendevice)
* [`exchange(...)`](#exchange)
* [`closeDevice(...)`](#closedevice)
* [`addListener('onDeviceConnect', ...)`](#addlistenerondeviceconnect-)
* [`addListener('onDeviceDisconnect', ...)`](#addlistenerondevicedisconnect-)
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getDeviceList()

```typescript
getDeviceList() => Promise<{ devices: ICapacitorUSBDevice[]; }>
```

**Returns:** <code>Promise&lt;{ devices: ICapacitorUSBDevice[]; }&gt;</code>

--------------------


### openDevice(...)

```typescript
openDevice(options: { deviceId: number; }) => Promise<{ success: boolean; }>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ deviceId: number; }</code> |

**Returns:** <code>Promise&lt;{ success: boolean; }&gt;</code>

--------------------


### exchange(...)

```typescript
exchange(options: { deviceId: number; apduHex: string; }) => Promise<{ response: string; }>
```

| Param         | Type                                                |
| ------------- | --------------------------------------------------- |
| **`options`** | <code>{ deviceId: number; apduHex: string; }</code> |

**Returns:** <code>Promise&lt;{ response: string; }&gt;</code>

--------------------


### closeDevice(...)

```typescript
closeDevice(options: { deviceId: number; }) => Promise<{ response: string; }>
```

| Param         | Type                               |
| ------------- | ---------------------------------- |
| **`options`** | <code>{ deviceId: number; }</code> |

**Returns:** <code>Promise&lt;{ response: string; }&gt;</code>

--------------------


### addListener('onDeviceConnect', ...)

```typescript
addListener(eventName: 'onDeviceConnect', handler: (device: ICapacitorUSBDevice) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param           | Type                                                                                     |
| --------------- | ---------------------------------------------------------------------------------------- |
| **`eventName`** | <code>'onDeviceConnect'</code>                                                           |
| **`handler`**   | <code>(device: <a href="#icapacitorusbdevice">ICapacitorUSBDevice</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### addListener('onDeviceDisconnect', ...)

```typescript
addListener(eventName: 'onDeviceDisconnect', handler: (device: ICapacitorUSBDevice) => void) => Promise<PluginListenerHandle> & PluginListenerHandle
```

| Param           | Type                                                                                     |
| --------------- | ---------------------------------------------------------------------------------------- |
| **`eventName`** | <code>'onDeviceDisconnect'</code>                                                        |
| **`handler`**   | <code>(device: <a href="#icapacitorusbdevice">ICapacitorUSBDevice</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt; & <a href="#pluginlistenerhandle">PluginListenerHandle</a></code>

--------------------


### Interfaces


#### ICapacitorUSBDevice

| Prop            | Type                |
| --------------- | ------------------- |
| **`id`**        | <code>number</code> |
| **`name`**      | <code>string</code> |
| **`vendorId`**  | <code>number</code> |
| **`productId`** | <code>number</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |

</docgen-api>

This project is tested with BrowserStack
