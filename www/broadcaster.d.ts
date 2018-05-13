interface CordovaBroadcaster {
  addEventListener(name: string, listener: (event: Event) => void): void;
  removeEventListener(name: string, listener: (event: Event) => void): void;
  fireNativeEvent(type: string, data: object | null, success?: () => void, error?: (message: string) => void): void;
}

interface Window {
  broadcaster: CordovaBroadcaster;
}

declare var broadcaster: CordovaBroadcaster;
