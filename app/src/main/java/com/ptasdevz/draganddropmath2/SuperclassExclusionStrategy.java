package com.ptasdevz.draganddropmath2;

import android.util.Log;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.lang.reflect.Field;

import static com.ptasdevz.draganddropmath2.MainActivity.TAG;

public class SuperclassExclusionStrategy implements ExclusionStrategy
{
    public boolean shouldSkipClass(Class<?> arg0)
    {
        return false;
    }

    public boolean shouldSkipField(FieldAttributes fieldAttributes)
    {
        String fieldName = fieldAttributes.getName();
        Class<?> theClass = fieldAttributes.getDeclaringClass();
        String simpleName = theClass.getSimpleName();
        Log.d(TAG, "shouldSkipField: field class "+fieldAttributes.getDeclaredClass());

        return true;
//        return isFieldInSuperclass(theClass, fieldName);
    }

    private boolean isFieldInSuperclass(Class<?> subclass, String fieldName)
    {
        Class<?> superclass = subclass.getSuperclass();
        Field field;

        while(superclass != null)
        {
            field = getField(superclass, fieldName);

            if(field != null)
                return true;

            superclass = superclass.getSuperclass();
        }

        return false;
    }

    private Field getField(Class<?> theClass, String fieldName)
    {
        try
        {
            return theClass.getDeclaredField(fieldName);
        }
        catch(Exception e)
        {
            return null;
        }
    }
}