declare var exec: any;
declare var channel: any;
declare type Listener = (event: Event) => void;
declare type AndroidData = {
    extras: object;
    flag: number;
    category: string;
};
interface Channel {
    subscribe(handler: Listener): void;
    unsubscribe(handler: Listener): void;
    fire(event: Event): void;
    numHandlers: number;
}
declare type Channels = {
    [key: string]: Channel;
};
declare class Broadcaster {
    /**
     * fire native evet
     *
     * @param type
     * @param data
     * @param success
     * @param error
     */
    fireNativeEvent(type: string, data: object | AndroidData | null, success?: () => void, error?: (message: string) => void): void;
    /**
     * fire global native evet (valid only for android) 
     * @param type 
     * @param isGlobal 
     * @param data 
     * @param success 
     * @param error 
     */    
    fireNativeEvent(type: string, isGlobal:boolean, data: object | AndroidData | null, success?: () => void, error?: (message: string) => void): void;    
    /**
     * fire local event
     *
     * @param type
     * @param data
     */
    fireEvent(type: string, data: object | null): void;
    /**
     * add a listener
     *
     * @param eventname
     * @param listener
     */
    addEventListener(eventname: string, listener: Listener): void;
    /**
     * add a global listener (valid only for android)
     * 
     * @param eventname 
     * @param isGlobal 
     * @param listener 
     */
    addEventListener(eventname: string, isGlobal: boolean, listener: Listener): void;
    /**
     * remove a listener
     *
     * @param eventname
     * @param listener
     */
    removeEventListener(eventname: string, listener: Listener): void;
    /**
     * remove a global listener (valid only for android)
     *
     * @param eventname
     * @param globalFlagOrListener
     * @param listener
     */
    removeEventListener(eventname: string, isGlobal: boolean, listener: Listener): void;
}
