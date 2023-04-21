/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 */

import React,{useEffect,useState,useRef} from 'react';
import type {PropsWithChildren} from 'react';
import {
  SafeAreaView,
  ScrollView,
  StatusBar,
  StyleSheet,
  Text,
  useColorScheme,
  View,
  NativeModules,
  Pressable
} from 'react-native';

import {
  Colors,
  DebugInstructions,
  Header,
  LearnMoreLinks,
  ReloadInstructions,
} from 'react-native/Libraries/NewAppScreen';
import {UnityView,UnityModule} from './unity';

const {FileManager,UnityNativeModule}=NativeModules;
const {showUnity}=NativeModules;
console.log(NativeModules,'NativeModules');



function App(){
  const isDarkMode = useColorScheme() === 'dark';
const [once]=useState(null);
const unitview=useRef<any>(null);
  const backgroundStyle = {
    backgroundColor: isDarkMode ? Colors.darker : Colors.lighter,
  };

  const onMessage=(msg:any)=>{
     console.log(msg,"msg");
  }

  useEffect(()=>{
  if(once){
   
  }
   
  },[once]);

  const fn=()=>{
    FileManager.QueryOrDownload("2896b599fb109bb41ce26ac18e4f1114e7ceb8282721d27fbd976ee79c47b01a","http://testweb.hgmalls.com/assets/a1.gltf",false).then((rs)=>{
      console.log('file get',rs,unitview.current);
      if(rs.file_path!=""){
        if(unitview.current){
          unitview.current.onLoad({value:rs.file_path+',1'});
        }
      }
    });
  }

  return (
    <SafeAreaView style={[backgroundStyle,{flex:1}]}>
         <View style={{flex:1}}>
         <UnityView onMessage={onMessage.bind(null)} style={{flex:1,height:300}} ref={unitview} >
           </UnityView>
           <Pressable onPress={fn} style={{padding:20,
             backgroundColor:"red"}}>
          <Text>加载</Text>
        </Pressable>
         </View>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  sectionContainer: {
    marginTop: 32,
    paddingHorizontal: 24,
  },
  sectionTitle: {
    fontSize: 24,
    fontWeight: '600',
  },
  sectionDescription: {
    marginTop: 8,
    fontSize: 18,
    fontWeight: '400',
  },
  highlight: {
    fontWeight: '700',
  },
});

export default App;
