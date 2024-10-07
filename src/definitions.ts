export interface CapacitorUSBPluginPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
