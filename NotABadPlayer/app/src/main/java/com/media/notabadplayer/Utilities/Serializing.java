package com.media.notabadplayer.Utilities;

import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Serializing 
{
    public static String serializeObject(Serializable object)
    {
        if (object == null)
        {
            return null;
        }
        
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream so = new ObjectOutputStream(bo);
            so.writeObject(object);
            so.flush();
            
            String result = new String(Base64.encode(bo.toByteArray(), Base64.DEFAULT));
            
            bo.close();
            so.close();
            
            return result;
        }
        catch (Exception e)
        {
            Log.v(Serializing.class.getSimpleName(), "Cannot serialize object: " + e.toString());
        }
        
        return null;
    }
    
    public static Object deserializeObject(String data)
    {
        if (data == null || data.isEmpty())
        {
            return null;
        }

        try {
            byte b[] = Base64.decode(data.getBytes(), Base64.DEFAULT);
            ByteArrayInputStream bi = new ByteArrayInputStream(b);
            ObjectInputStream si = new ObjectInputStream(bi);
            
            Object result = si.readObject();
            
            bi.close();
            si.close();
            
            return result;
        }
        catch (Exception e)
        {
            Log.v(Serializing.class.getSimpleName(), "Cannot deserialize object: " + e.toString());
        }
        
        return null;
    }
}
