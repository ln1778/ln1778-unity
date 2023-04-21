package com.un3d.modules;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.un3d.R;
import com.un3d.views.UnityUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class FileManager extends ReactContextBaseJavaModule {

    private  static FileManager INSTANCE;
    private ReactApplicationContext reactContext;

    private static final String DB_NAME="model_list";
    private static final String TB_NAME="gltf_list";
    private static final String InfoTag="file manager";
    private SQLiteDatabase db;

    private Map<String,Promise> cbQueen;
    private Handler mHandler ;

    public  static FileManager getInstance(@Nullable ReactApplicationContext reactContext){
        if(INSTANCE==null){
            INSTANCE=new FileManager(reactContext);
        }
        return INSTANCE;
    }

    private FileManager(@Nullable ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext=reactContext;
        cbQueen=new HashMap<String,Promise>();
        String dbpath=reactContext.getFilesDir().getAbsolutePath()+"/databases/gltf.db";
        File f=new File(dbpath);
        if (!f.getParentFile().exists()){
            f.getParentFile().mkdirs();
        }
        db=SQLiteDatabase.openOrCreateDatabase(dbpath,null);
        checkTable();
    }




    public void callPromise(String id,String message){
        Promise pm=cbQueen.get(id);
        if(null!=pm){
            pm.reject("success",message);
            cbQueen.remove(id);
        }
    }

    @ReactMethod
    public void sendMessage(String gameObject, String methodName, String message ,Promise promise){
        if (null!=promise){
            String id=randomStr(12);
            message+=","+id;
            cbQueen.put(id,promise);
        }
        UnityUtils.postMessage(gameObject, methodName, message);
    }

    public String randomStr(int len){
        String s="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random r=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<len;i++){
            int num=r.nextInt(62);
            sb.append(s.charAt(num));
        }
        return sb.toString();
    }

    @NonNull
    @Override
    public String getName() {
        return "FileManager";
    }


    private void checkTable(){
        boolean result = false ;
        Cursor cursor = null ;
        try{
            //查询一行
            cursor = db.rawQuery( "select count(*) as c from sqlite_master where type ='table' and name ='"+TB_NAME+"'"
                    , null );
          if(cursor.moveToNext()){
              int count =cursor.getInt(0);
              if(count>0){
                  result=true;
              }
          }
          if(!result){
              String create="create table "+ TB_NAME +"(hash  text primary key,filepath text)";
              db.execSQL(create);
          }

        }catch (Exception e){
          e.printStackTrace();
        }finally{
            if(null != cursor && !cursor.isClosed()){
                cursor.close() ;
            }
        }
    }



    @ReactMethod
    public void QueryOrDownload(String hash, String url, boolean fc, Promise promise){

        mHandler=new Handler(Looper.myLooper());
        Runnable f=new Runnable(){
            @Override
            public void run() {
                String filepath=null;
                String qe[]= {hash};
                Cursor cur=null;
                WritableMap map = Arguments.createMap();
                try {
                    boolean force=fc;
                    cur=db.query(TB_NAME,null,"hash=?",qe,null,null,null);
                    if(cur.getCount()>0){
                        cur.moveToFirst();
                        filepath=cur.getString(1);
                        Log.i(InfoTag,"found "+filepath);
                        if (force){
                            db.delete(TB_NAME,"hash=?",qe);
                        }
                        map.putString("file_path",filepath);
                    }else{
                        force=true;
                    }

                    if(force){
                        filepath= download(url,"gltf_model",hash);
                        ContentValues cv=new ContentValues();
                        cv.put("hash",hash);
                        cv.put("filepath",filepath);
                        db.insert(TB_NAME,null,cv);
                        map.putString("file_path",filepath);
                    }
                    promise.resolve(map);
                }catch (Exception e){
                    promise.reject("error",e.toString());
                }finally {
                    if(null != cur && !cur.isClosed()){
                        cur.close() ;
                    }
                }
            }
        };
        mHandler.post(f);
    }

    public String getExt(String path){
        String[] tmp =path.split("\\.");
        if (tmp.length==0){
            return "";
        }
        return tmp[tmp.length-1];
    }


    public String download(String urlStr,String savePath,String md5) {

        FileOutputStream fos=null;
        InputStream ir=null;
        String finalPath="";
        try {
            URL url = null;
            // 创建一个URL对象
            url = new URL(urlStr);
            File af=new File(this.getReactApplicationContext().getFilesDir().getAbsolutePath()+"/"+savePath);
            if (!af.exists()){
                af.mkdirs();
            }


            String fname=md5+"."+getExt(url.getPath());
            File save=new File(af.getAbsolutePath()+"/"+fname);
            finalPath=save.getAbsolutePath();
            fos=new FileOutputStream(save);

            // 创建一个Http连接
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            // 使用IO流读取数据
            ir=urlConn.getInputStream();
            byte[] buffer = new byte[1024];

            int len = 0;
            while ((len = ir.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }

            fos.flush();


        } catch (Exception e) {
            e.printStackTrace();
            finalPath="";
        } finally {
            try {
                fos.close();
                ir.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!"".equals(finalPath)){
            try {
                String hash=toHex(hashV2(finalPath));
                if (!hash.equals(md5)){
                    finalPath="";
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return finalPath;
    }






    public static byte [] hashV2(String filePath) throws IOException, NoSuchAlgorithmException {
        File file = new File(filePath);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        int bufferSize = 16384;
        byte [] buffer = new byte[bufferSize];
        int sizeRead = -1;
        while ((sizeRead = in.read(buffer)) != -1) {
            digest.update(buffer, 0, sizeRead);
        }
        in.close();
        byte [] hash = null;
        hash = new byte[digest.getDigestLength()];
        hash = digest.digest();
        return hash;
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            String hex = Integer.toHexString(data[i]);
            if (hex.length() == 1) {
                sb.append("0");
            } else if (hex.length() == 8) {
                hex = hex.substring(6);
            }
            sb.append(hex);
        }
        return sb.toString().toLowerCase(Locale.getDefault());
    }


}
