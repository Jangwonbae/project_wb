package com.example.bewith.javaclass;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class UUIDClass {
    private final static String CACHE_DEVICE_ID = "CacheDeviceID";
    public static String GetDeviceUUID(Context context)
    {   //QR만들기
        UUID deviceUUID = null;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        String cachedDeviceID = sharedPreferences.getString(CACHE_DEVICE_ID, "");
        if ( cachedDeviceID != "" )
        {
            deviceUUID = UUID.fromString( cachedDeviceID );
        }
        else
        {
            final String androidUniqueID = Settings.Secure.getString( context.getContentResolver(), Settings.Secure.ANDROID_ID );
            try
            {
                if ( androidUniqueID != "" )
                {
                    deviceUUID = UUID.nameUUIDFromBytes( androidUniqueID.getBytes("utf8") );
                }
                else
                {
                    final String anotherUniqueID = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    if ( anotherUniqueID != null )
                    {
                        deviceUUID = UUID.nameUUIDFromBytes( anotherUniqueID.getBytes("utf8") );
                    }
                    else
                    {
                        deviceUUID = UUID.randomUUID();
                    }
                }
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new RuntimeException(e);
            }
        }
        // save cur UUID.
        sharedPreferences.edit().putString(CACHE_DEVICE_ID, deviceUUID.toString()).apply();
        return deviceUUID.toString();
    }
}
