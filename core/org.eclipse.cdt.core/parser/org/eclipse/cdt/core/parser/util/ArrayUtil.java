/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
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
     * Assumes that array contains nulls at the end, only. 
     * Appends element after the last non-null element. 
     * If the array is null or not large enough, a larger one is allocated, using
     * the given class object.
     */
    static public Object [] append( Class c, Object[] array, Object obj ){
    	if( obj == null )
    		return array;
    	if( array == null || array.length == 0){
    		array = (Object[]) Array.newInstance( c, DEFAULT_LENGTH );
    		array[0] = obj;
    		return array;
    	}

    	int i= findFirstNull(array);
    	if (i >= 0) {
    		array[i]= obj;
    		return array;
    	}

    	Object [] temp = (Object[]) Array.newInstance( c, array.length * 2 );
    	System.arraycopy( array, 0, temp, 0, array.length );
    	temp[array.length] = obj;
    	return temp;
    }

    /**
     * Assumes that array contains nulls at the end, only.
     * @returns index of first null, or -1
     */ 
    private static int findFirstNull(Object[] array) {
    	boolean haveNull= false;
    	int left= 0;
    	int right= array.length-1;
    	while (left <= right) {
    		int mid= (left+right)/2;
    		if (array[mid] == null) {
    			haveNull= true;
    			right= mid-1;
    		}
    		else {
    			left= mid+1;
    		}
    	}
		return haveNull ? right+1 : -1;
	}
    

    /**
     * Assumes that array contains nulls at the end, only. 
     * Appends object using the current length of the array.
     * @since 4.0
     */
    static public Object [] append(Class c, Object[] array, int currentLength, Object obj ){
    	if( obj == null )
    		return array;
    	if( array == null || array.length == 0){
    		array = (Object[]) Array.newInstance( c, DEFAULT_LENGTH );
    		array[0] = obj;
    		return array;
    	}

    	if (currentLength < array.length) {
    		assert array[currentLength] == null;
    		assert currentLength == 0 || array[currentLength-1] != null;
    		array[currentLength]= obj;
    		return array;
    	}

    	Object [] temp = (Object[]) Array.newInstance( c, array.length * 2 );
    	System.arraycopy( array, 0, temp, 0, array.length );
    	temp[array.length] = obj;
    	return temp;
    }

    static public Object [] append( Object[] array, Object obj ){
        return append( Object.class, array, obj );
    }
    
    /**
     * Trims the given array and returns a new array with no null entries.
     * Assumes that nulls can be found at the end, only.
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
        
        int i = array.length;
        if (i==0 || array[i-1] != null) {
        	if (!forceNew) {
        		return array;
        	}
        }
        else {
        	i=findFirstNull(array);
        	assert i>=0;
        }

        Object [] temp = (Object[]) Array.newInstance( c, i );
        System.arraycopy( array, 0, temp, 0, i );
        return temp;
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
     * Assumes that both arrays contain nulls at the end, only.
     */
    public static Object[] addAll( Class c, Object[] dest, Object[] source ) {
        if( source == null || source.length == 0 )
            return dest;
        
        int numToAdd = findFirstNull(source);
        if (numToAdd <= 0) {
        	if (numToAdd == 0) {
        		return dest;
        	}
        	numToAdd= source.length;
        }
        
        if( dest == null || dest.length == 0 ){
            dest = (Object[]) Array.newInstance( c, numToAdd );
            System.arraycopy( source, 0, dest, 0, numToAdd );
            return dest;
        }
        
        int firstFree = findFirstNull(dest);
        if (firstFree < 0) {
        	firstFree= dest.length;
        }
        
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
     * Returns whether the specified array contains the specified object. Comparison is by
     * object identity.
     * @param array the array to search
     * @param obj the object to search for
     * @return true if the specified array contains the specified object, or the specified array is null
     */
    public static boolean contains( Object [] array, Object obj ){
    	return indexOf(array, obj)!=-1;
    }
    
    /**
     * Returns the index into the specified array of the specified object, or -1 if the array does not
     * contain the object, or if the array is null.  Comparison is by object identity.
     * @param array the array to search
     * @param obj the object to search for
     * @return the index into the specified array of the specified object, or -1 if the array does not
     * contain the object, or if the array is null
     */
    public static int indexOf(Object[] array, Object obj) {
    	int result = -1;
    	if(array!=null) {
    		for(int i=0; i<array.length; i++) {
    			if(array[i] == obj)
    				return i;
    		}
    	}
    	return result;
    }
    
    /**
     * Assumes that array contains nulls at the end, only. 
     * Returns whether the specified array contains the specified object. Comparison is by
     * object identity.
     * @param array the array to search
     * @param obj the object to search for
     * @return true if the specified array contains the specified object, or the specified array is null
     */
    public static boolean containsEqual( Object [] array, Object obj ){
    	return indexOfEqual(array, obj)!=-1;
    }

    /**
     * Assumes that array contains nulls at the end, only. 
     * Returns the index into the specified array of the specified object, or -1 if the array does not
     * contain the object, or if the array is null.  Comparison is by equals().
     * @param array the array to search
     * @param obj the object to search for
     * @return the index into the specified array of the specified object, or -1 if the array does not
     * contain an equal object, or if the array is null
     */    
	public static int indexOfEqual(Object[] comments, Object comment) {
    	int result = -1;
    	if(comments!=null) {
    		for(int i=0; (i<comments.length) && (comments[i]!=null); i++) {
    			if(comments[i].equals(comment))
    				return i;
    		}
    	}
    	return result;
    }

	
	/**
	 * Note that this should only be used when the placement of 
	 * nulls within the array is unknown (due to performance efficiency).  
	 * 
	 * Removes all of the nulls from the array and returns a new
     * array that contains all of the non-null elements.
     *
     * If there are no nulls in the original array then the original
     * array is returned.
	 */
	public static Object[] removeNulls(Class c, Object[] array) {
        if( array == null )
            return (Object[]) Array.newInstance( c, 0 );
        
        int i;
		int validEntries = 0;
		for( i = 0; i < array.length; i++ ){
	         if( array[i] != null ) validEntries++;
	    }
		
		if (array.length == validEntries) 
			return array;
		
		Object[] newArray = (Object[]) Array.newInstance(c, validEntries);
		int j = 0;
        for( i = 0; i < array.length; i++ ){
            if( array[i] != null ) newArray[j++] = array[i];
        }
		
		return newArray;
	}

	/**
	 * To improve performance, this method should be used instead of ArrayUtil#removeNulls(Class, Object[]) when
	 * all of the non-null elements in the array are grouped together at the beginning of the array
	 * and all of the nulls are at the end of the array.  
	 * The position of the last non-null element in the array must also be known. 
     *
	 * @return
	 */
	public static Object[] removeNullsAfter(Class c, Object[] array, int index) {
        if( array == null || index < 0)
            return (Object[]) Array.newInstance( c, 0 );
        
        final int newLen= index+1;
        if( array.length == newLen)
        	return array;
        
        Object[] newArray = (Object[]) Array.newInstance(c, newLen);
        System.arraycopy(array, 0, newArray, 0, newLen);
		return newArray;
	}


	/**
	 * Insert the obj at the beginning of the array, shifting the whole thing one index
	 * Assumes that array contains nulls at the end, only. 
	 */
	public static Object[] prepend(Class c, Object[] array, Object obj) {
		if( obj == null )
    		return array;
        if( array == null || array.length == 0){
            array = (Object[]) Array.newInstance( c, DEFAULT_LENGTH );
            array[0] = obj;
            return array;
        }
        
        int i = findFirstNull(array);
        if (i >= 0) {
			System.arraycopy( array, 0, array, 1, i);
			array[0] = obj;
        }
        else {
			Object [] temp = (Object[]) Array.newInstance( c, array.length * 2 );
	        System.arraycopy( array, 0, temp, 1, array.length );
	        temp[0] = obj;
	        array = temp;	
		}
        
        return array;
	}
	
	/**
	 * Removes first occurrence of element in array and moves objects behind up front.
	 * @since 4.0
	 */
	public static void remove(Object[] array, Object element) {
		if( array != null ) {
			for (int i = 0; i < array.length; i++) {
				if( element == array[i] ) {
					System.arraycopy(array, i+1, array, i, array.length-i-1);
					array[array.length-1]= null;
					return;
				}
			}
		}
	}
	
    static public int [] setInt( int[] array, int idx, int val ){
        if( array == null ){
            array = new int [ DEFAULT_LENGTH > idx + 1 ? DEFAULT_LENGTH : idx + 1];
            array[idx] = val;
            return array;
        }
        
        if( array.length <= idx ){
            int newLen = array.length * 2;
            while( newLen <= idx ) newLen *=2;
            int [] temp = new int [newLen];
            System.arraycopy( array, 0, temp, 0, array.length );
            
            array = temp;
        }
        array[idx] = val;
        return array;
    }
    

	
}
