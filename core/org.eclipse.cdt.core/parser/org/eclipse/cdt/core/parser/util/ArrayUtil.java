/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Feb 16, 2005
 */
package org.eclipse.cdt.core.parser.util;

import java.lang.reflect.Array;

/**
 * @author aniefer
 */
public class ArrayUtil {
    public static final class ArrayWrapper {
        public Object [] array = null;
    }
    
    public static final int DEFAULT_LENGTH = 2;
    /**
     * Adds obj to array in the first null slot.  
     * If array is null, a new array is created and obj is added to the new array.  
     * If the array is full, a new array of larger size is created, the contents 
     * of array are copied over and obj is added to the new array.
     * 
     * The type of any new arrays will be array of c, where c is given.
     * es:  c = IBinding.class results in an IBinding[]
     * @param Class c
     * @param Object [] array
     * @param Object obj
     * @return
     */
    static public Object [] append( Class c, Object[] array, Object obj ){
        if( array == null || array.length == 0){
            array = (Object[]) Array.newInstance( c, DEFAULT_LENGTH );
            array[0] = obj;
            return array;
        }
        
        int i = 0;
        for( ; i < array.length; i++ ){
            if( array[i] == null ){
                array[i] = obj;
                return array;
            }
        }
        Object [] temp = (Object[]) Array.newInstance( c, array.length * 2 );
        System.arraycopy( array, 0, temp, 0, array.length );
        temp[array.length] = obj;
        array = temp;
        return array;
    }
    
    static public Object [] append( Object[] array, Object obj ){
        return append( Object.class, array, obj );
    }
    
    /**
     * Trims the given array and returns a new array with no null entries.
     * if array == null, a new array of length 0 is returned
     * if forceNew == true, a new array will always be created.
     * if forceNew == false, a new array will only be created if the original array
     * contained null entries.
     *  
     * @param Class c: the type of the new array
     * @param Object [] array, the array to be trimmed
     * @param forceNew
     * @return
     */
    static public Object [] trim( Class c, Object [] array, boolean forceNew ){
        if( array == null )
            return (Object[]) Array.newInstance( c, 0 );
        
        int i = 0;
        for( ; i < array.length; i++ ){
            if( array[i] == null ) break;
        }
        if( forceNew || i < array.length ){
            Object [] temp = (Object[]) Array.newInstance( c, i );
            System.arraycopy( array, 0, temp, 0, i );
            array = temp;
        }
        return array;
    }

    /**
     * @param class1
     * @param fields
     * @return
     */
    public static Object[] trim( Class c, Object[] array ) {
        return trim( c, array, false );
    }

    /**
     * @param transitives
     * @param usings
     */
    public static Object[] addAll( Class c, Object[] dest, Object[] source ) {
        if( source == null || source.length == 0 )
            return dest;
        
        int numToAdd = 0;
        while( numToAdd < source.length && source[numToAdd] != null )
            numToAdd++;
        
        if( numToAdd == 0 )
            return dest;
        
        if( dest == null || dest.length == 0 ){
            dest = (Object[]) Array.newInstance( c, numToAdd );
            System.arraycopy( source, 0, dest, 0, numToAdd );
            return dest;
        }
        
        int firstFree = 0;
        while( firstFree < dest.length && dest[firstFree] != null )
            firstFree++;
        
        if( firstFree + numToAdd <= dest.length ){
            System.arraycopy( source, 0, dest, firstFree, numToAdd );
            return dest;
        }
        Object [] temp = (Object[]) Array.newInstance( c, firstFree + numToAdd );
        System.arraycopy( dest, 0, temp, 0, firstFree );
        System.arraycopy( source, 0, temp, firstFree, numToAdd );
        return temp;
    }

    /**
     * Replaces the item at index idx with the given object.  If the obj is an Object[],
     * then the contents of that array are inserted with the first element overwriting 
     * whatever was at idx.
     * @param class1
     * @param nodes
     * @param declarations
     * @return
     */
    public static Object[] replace( Class c, Object[] array, int idx, Object obj ) {
        if( array == null || idx >= array.length )
            return array;
        
        if( obj instanceof Object [] ){
            Object [] objs = (Object[]) obj;
            Object [] temp = (Object[]) Array.newInstance( c, array.length + objs.length - 1 );
            System.arraycopy( array, 0, temp, 0, idx );
            System.arraycopy( objs, 0, temp, idx, objs.length );
            System.arraycopy( array, idx + 1, temp, idx + objs.length, array.length - idx - 1);
            array = temp;
        } else {
            array[idx] = obj;
        }
        return array;
    }
    
    public static boolean contains( Object [] array, Object obj ){
        if( array == null ) return false;
        for( int i = 0; i < array.length; i++ )
            if( array[i] == obj ) return true;
        return false;
    }
}
