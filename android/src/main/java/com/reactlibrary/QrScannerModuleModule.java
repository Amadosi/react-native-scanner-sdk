package com.reactlibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Environment;

import androidx.core.content.ContextCompat;

import com.extbit.client.FunguaPay.QRPayload;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.utils.Utils;

import java.io.File;


public class QrScannerModuleModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private final String ERR_NO_NULL_DATA = "ERR_NO_NULL_DATA";

    private static String PUB_KEY;

    private Promise mPromise;

    private final ActivityEventListener mActivityEventListener = new ActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                if(result.getContents() == null) {
                    rejectPromise("CANCELLED","Scan cancelled by user");
                } else {
                    Intent intent = result.getOriginalIntent();
                    byte[] dataBytes = intent.getByteArrayExtra("SCAN_RESULT_BYTE_SEGMENTS_0");

                    System.out.println("BYTES HEX DUMP: "+ Utils.bytesToHex(dataBytes));

                    if (ContextCompat.checkSelfPermission(
                            getCurrentActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        // You can use the API that requires the permission.
                        Utils.writeToFile(dataBytes,"dump.bin");
                        System.out.println("FILE PATH: dump.bin");
                    }else{
                        // You can directly ask for the permission.
                        getCurrentActivity().requestPermissions(new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE },
                                100);
                    }

                    if(dataBytes != null){
                        //Utils.writeToFile(result.getRawBytes());
                        //validate(result.getRawBytes());
                        validate(dataBytes);
                    }else{
                        System.err.println("MISSING INTENT: SCAN_RESULT_BYTE_SEGMENTS_0");
                    }
                }
            }
        }

        @Override
        public void onNewIntent(Intent intent){

        }

    };

    public QrScannerModuleModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "QrScannerModule";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    public void validate(byte[] data) {
        if(data == null){
            rejectPromise(ERR_NO_NULL_DATA,"Data can not be null");
            return;
        }

        //initialize the QR payload object
        QRPayload t = new QRPayload();
        //load data into memory
        t.Load(data);

        System.out.println("KEY: "+PUB_KEY);
        System.out.println("JSON: "+t.ToJson());

        int result = t.Validate(PUB_KEY);


        if (result == QRPayload.OKAY) {
            System.out.println("Good to go!");
            mPromise.resolve("Good to go");
        }else{
            System.out.println("Problem with library ERR:"+result);
            rejectPromise("ERR","Problem with library ERR:"+result);
        }
    }

    @ReactMethod
    public static void config(ReadableMap options) throws Exception{
        String publicKey = options.getString("publicKey");

        if (publicKey != null) {
            PUB_KEY = publicKey;
        }else{
            throw new Exception("Public key not set");
        }
    }

    @ReactMethod
    public void scanQR(Promise promise) {
        mPromise = promise;

        IntentIntegrator integrator = new IntentIntegrator(getCurrentActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a barcode");
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    private void rejectPromise(String code, String message) {
        if (this.mPromise != null) {
            this.mPromise.reject(code, message);
            this.mPromise = null;
        }
    }
}
