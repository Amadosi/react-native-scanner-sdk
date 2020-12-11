package com.extbit.client.FunguaPay;

public class QRPayload
{
    private byte[] data = null;

    // Error codes
    public static final int OKAY = 0;
    public static final int FAIL = 1;
    public static final int InvalidRawLen   = 2;
    public static final int InvalidType     = 3;
    public static final int InvalidLength   = 4;
    public static final int InvalidGzipData = 5;
    public static final int InvalidGzipNend = 6;
    public static final int AbsentObjM      = 7;
    public static final int AbsentObjD      = 8;
    public static final int AbsentObjS      = 9;
    public static final int InvalidDTObjM   = 11;
    public static final int InvalidDTObjD   = 12;
    public static final int InvalidDTObjS   = 13;
    public static final int TooShortObjM    = 21;
    public static final int TooShortObjD    = 22;
    public static final int TooShortObjS    = 23;
    public static final int TooLongObjM     = 31;
    public static final int TooLongObjD     = 32;
    public static final int TooLongObjS     = 33;
    public static final int KeyNotValid     = 41;
    public static final int VerifyFailed    = 44;

    public int Load(byte[] buffer)
    {
        System.out.println("TASK: Loading payload into memory");
        data = buffer;
        return this.load(buffer);
    }

    public String ToJson()
    {
        return this.json(data);
    }

    public int Validate(String key)
    {
        return this.validate(data, key);
    }

    public int Validate(String key, String message, byte[] signature)
    {
        return this.validate(data, key, message, signature);
    }

    static {
        try {
            System.out.println("TASK: Loading QRPayload library");
            System.loadLibrary("QRPayload");
            System.out.println("SUCCESS: QRPayload library loaded");
        } catch (Exception ex) {
            System.err.println("WARNING: Could not QRPayload load library");
        }
    }
    private native int load(byte[] buffer);
    private native int validate(byte[] buffer, String key);
    private native int validate(byte[] buffer, String key, String msg, byte[] signature);
    private native String json(byte[] buffer);
}
