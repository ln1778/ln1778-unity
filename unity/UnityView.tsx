import * as React from "react";
import { requireNativeComponent, findNodeHandle, NativeModules, NativeSyntheticEvent, View } from 'react-native';
import * as PropTypes from "prop-types"; 
import MessageHandler from "./MessageHandler";
import { UnityModule, UnityViewMessage } from "./UnityModule";

const { UIManager,FileManager } = NativeModules;

export interface UnityViewProps{
    /** 
     * Receive string message from unity. 
     */
    onMessage?: (message: string) => void;
    /** 
     * Receive unity message from unity. 
     */
    onUnityMessage?: (handler: MessageHandler) => void;
}

export default class UnityView extends React.Component<UnityViewProps> {
    public static propTypes = { 
        onMessage: PropTypes.func
    }

    private handle: number;
  
    constructor(props:any) {
        super(props);
        this.state={
            loaddata:{objName:"GameObject",eventName:"LoadUrl",path:""},
            speeddata:{objName:"GameObject",value:"0.3,1,0.2"}
        }
    }

    public componentWillMount() {
        this.handle = UnityModule.addMessageListener(message => {
            if (this.props.onUnityMessage && message instanceof MessageHandler) {
                this.props.onUnityMessage(message);
            }
            if (this.props.onMessage && typeof message === 'string') {
                this.props.onMessage(message);
            }
        });
    }
   public componentWillReceiveProps(nextProps: any, nextContext: any): void {
       if(nextProps.loaddata!=this.state.objName){
          this.setState({objName:nextProps.loaddata},()=>{
            this.onLoad(nextProps.loaddata);
          })
       }
   }
    public componentWillUnmount() {
        UnityModule.removeMessageListener(this.handle);
    }

    /**
     * [Deprecated] Use `UnityModule.pause` instead.
     */
    public pause() {
        UnityModule.pause();
    };

    /**
     * [Deprecated] Use `UnityModule.resume` instead.
     */
    public resume() {
        UnityModule.resume();
    };

    /**
     * [Deprecated] Use `UnityModule.postMessage` instead.
     */
    public postMessage(gameObject: string, methodName: string, message: string) {
        UnityModule.postMessage(gameObject, methodName, message);
    };

    /**
     * [Deprecated] Use `UnityModule.postMessageToUnityManager` instead.
     */
    public postMessageToUnityManager(message: string | UnityViewMessage) {
        UnityModule.postMessageToUnityManager(message);
    };

    public onLoad=(params:any)=>{
       try{
        UnityModule.isReady().then(()=>{
             FileManager.sendMessage(params?.objName?params.objName:"GameObject","LoadUrl",params.path?params.path:"").then((rs)=>{
               console.log("load success!",rs);
             })
           })
       }catch(err){
        console.log(err,"isReadyerr");
       }
    }

    public setSpped=(params:any)=>{
        try{
         UnityModule.isReady().then(()=>{
              FileManager.sendMessage(params?.objName?params.objName:"GameObject","SetSpeed",params.value?params.value:"0.3,1,0.2").then((rs)=>{
                console.log("load success!",rs);
              })
            })
        }catch(err){
         console.log(err,"isReadyerr");
        }
     }

    public render() {
        const { onUnityMessage, onMessage, ...props } = this.props;
        return (
            <View {...props}>
                <NativeUnityView
                    style={{ flex:1, }}
                    onUnityMessage={onUnityMessage}
                    onMessage={onMessage}
                >
                </NativeUnityView>
                {this.props.children}
            </View>
        );
    }
}

const NativeUnityView = requireNativeComponent<UnityViewProps>('UnityView', UnityView);

