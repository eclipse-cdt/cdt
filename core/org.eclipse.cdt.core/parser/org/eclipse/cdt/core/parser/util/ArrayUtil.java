/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Mike Kucera (IBM)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

import java.lang.reflect.Array;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class ArrayUtil {
    private static final int DEFAULT_LENGTH = 2;

    /**
     * Assumes that array contains nulls at the end, only. 
     * Appends element after the last non-null element. 
     * If the array is null or not large enough, a larger one is allocated, using
     * the given class object.
     */
    static public Object[] append(Class<?> c, Object[] array, Object obj) {
    	if (obj == null)
    		return array;
    	if (array == null || array.length == 0) {
    		array = (Object[]) Array.newInstance(c, DEFAULT_LENGTH);
    		array[0] = obj;
    		return array;
    	}

    	int i= findFirstNull(array);
    	if (i >= 0) {
    		array[i]= obj;
    		return array;
    	}

    	Object[] temp = (Object[]) Array.newInstance(c, Math.max(array.length * 2, DEFAULT_LENGTH));
    	System.arraycopy(array, 0, temp, 0, array.length);
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
    	int right= array.length - 1;
    	while (left <= right) {
    		int mid= (left + right) / 2;
    		if (array[mid] == null) {
    			haveNull= true;
    			right= mid - 1;
    		} else {
    			left= mid + 1;
    		}
    	}
		return haveNull ? right + 1 : -1;
	}

    /**
     * Assumes that array contains nulls at the end, only. 
     * Appends object using the current length of the array.
     * @since 4.0
     */
    static public Object[] append(Class<?> c, Object[] array, int currentLength, Object obj) {
    	if (obj == null)
    		return array;
    	if (array == null || array.length == 0) {
    		array = (Object[]) Array.newInstance(c, DEFAULT_LENGTH);
    		array[0] = obj;
    		return array;
    	}

    	if (currentLength < array.length) {
    		assert array[currentLength] == null;
    		assert currentLength == 0 || array[currentLength - 1] != null;
    		array[currentLength]= obj;
    		return array;
    	}

    	Object[] temp = (Object[]) Array.newInstance(c, array.length * 2);
    	System.arraycopy(array, 0, temp, 0, array.length);
    	temp[array.length] = obj;
    	return temp;
    }

    /**
     * Assumes that array contains nulls at the end, only. 
     * Appends element after the last non-null element. 
     * If the array is not large enough, a larger one is allocated.
     * Null <code>array</code> is supported for backward compatibility only and only when T is Object.
     */
    @SuppressWarnings("unchecked")
	static public <T> T[] append(T[] array, T obj) {
    	if (obj == null)
    		return array;
    	if (array == null || array.length == 0) {
    		Class<? extends Object> c = array != null ? array.getClass().getComponentType() : Object.class;
    		array = (T[]) Array.newInstance(c, DEFAULT_LENGTH);
    		array[0] = obj;
    		return array;
    	}

    	int i= findFirstNull(array);
    	if (i >= 0) {
    		array[i]= obj;
    		return array;
    	}

    	T[] temp = (T[]) Array.newInstance(array.getClass().getComponentType(),
    			Math.max(array.length * 2, DEFAULT_LENGTH));
    	System.arraycopy(array, 0, temp, 0, array.length);
    	temp[array.length] = obj;
    	return temp;
    }

    /**
     * Type safe version of {@link #append(Class, Object[], int, Object)}
     * @since 5.1
     */
    @SuppressWarnings("unchecked")
	static public <T> T[] appendAt(Class<T> c, T[] array, int currentLength, T obj) {
    	return (T[]) append(c, array, currentLength, obj);
    }

    /**
     * Trims the given array and returns a new array with no null entries.
     * Assumes that nulls can be found at the end, only.
     * if array == null, a new array of length 0 is returned
     * if forceNew == true, a new array will always be created.
     * if forceNew == false, a new array will only be created if the original array
     * contained null entries.
     *  
     * @param c the type of the new array
     * @param array the array to be trimmed
     * @param forceNew
     */
    static public Object[] trim(Class<?> c, Object[] array, boolean forceNew) {
        if (array == null)
            return (Object[]) Array.newInstance(c, 0);

        int i = array.length;
        if (i == 0 || array[i - 1] != null) {
        	if (!forceNew) {
        		return array;
        	}
        } else {
        	i= findFirstNull(array);
        	assert i >= 0;
        }

        Object[] temp = (Object[]) Array.newInstance(c, i);
        System.arraycopy(array, 0, temp, 0, i);
        return temp;
    }

    public static Object[] trim(Class<?> c, Object[] array) {
        return trim(c, array, false);
    }

    /**
     * Trims the given array and returns a new array with no null entries.
     * Assumes that nulls can be found at the end, only.
     * if forceNew == true, a new array will always be created.
     * if forceNew == false, a new array will only be created if the original array
     * contained null entries.
     *  
     * @param array the array to be trimmed
     * @param forceNew
     * @since 5.2
     */
    @SuppressWarnings("unchecked")
	static public <T> T[] trim(T[] array, boolean forceNew) {
        int i = array.length;
        if (i == 0 || array[i - 1] != null) {
        	if (!forceNew) {
        		return array;
        	}
        } else {
        	i= findFirstNull(array);
        	assert i >= 0;
        }

        T[] temp = (T[]) Array.newInstance(array.getClass().getComponentType(), i);
        System.arraycopy(array, 0, temp, 0, i);
        return temp;
    }

    /**
     * Trims the given array and returns a new array with no null entries.
     * Assumes that nulls can be found at the end, only.
     *  
     * @param array the array to be trimmed
     * @since 5.2
     */
	static public <T> T[] trim(T[] array) {
		return trim(array, false);
	}

    /**
     * Takes contents of the two arrays up to the first <code>null</code> element and concatenates
     * them.
     * @param c The type of the element of the returned array if there was not enough free space
     *     in the destination array.
     * @param dest The destination array. The elements of the source array are added to this array
     *     if there is enough free space in it. May be <code>null</code>. 
     * @param source The source array. May not be <code>null</code>. 
     * @return The concatenated array, which may be the same as the first parameter. 
     */
    public static Object[] addAll(Class<?> c, Object[] dest, Object[] source) {
        if (source == null || source.length == 0)
            return dest;

        int numToAdd = findFirstNull(source);
        if (numToAdd <= 0) {
        	if (numToAdd == 0) {
        		return dest;
        	}
        	numToAdd= source.length;
        }

        if (dest == null || dest.length == 0) {
            dest = (Object[]) Array.newInstance(c, numToAdd);
            System.arraycopy(source, 0, dest, 0, numToAdd);
            return dest;
        }

        int firstFree = findFirstNull(dest);
        if (firstFree < 0) {
        	firstFree= dest.length;
        }

        if (firstFree + numToAdd <= dest.length) {
            System.arraycopy(source, 0, dest, firstFree, numToAdd);
            return dest;
        }
        Object[] temp = (Object[]) Array.newInstance(c, firstFree + numToAdd);
        System.arraycopy(dest, 0, temp, 0, firstFree);
        System.arraycopy(source, 0, temp, firstFree, numToAdd);
        return temp;
    }

    /**
     * Takes contents of the two arrays up to the first <code>null</code> element and concatenates
     * them.
     * @param dest The destination array. The elements of the source array are added to this array
     *     if there is enough free space in it. May be <code>null</code>. 
     * @param source The source array. May not be <code>null</code>. 
     * @return The concatenated array, which may be the same as the first parameter. 
     * @since 5.2
     */
    @SuppressWarnings("unchecked")
	public static <T> T[] addAll(T[] dest, T[] source) {
        if (source == null || source.length == 0)
            return dest;

        int numToAdd = findFirstNull(source);
        if (numToAdd <= 0) {
        	if (numToAdd == 0) {
        		return dest;
        	}
        	numToAdd= source.length;
        }

        if (dest == null || dest.length == 0) {
    		Class<? extends Object> c =	dest != null ?
    				dest.getClass().getComponentType() : source.getClass().getComponentType();
            dest = (T[]) Array.newInstance(c, numToAdd);
            System.arraycopy(source, 0, dest, 0, numToAdd);
            return dest;
        }

        int firstFree = findFirstNull(dest);
        if (firstFree < 0) {
        	firstFree= dest.length;
        }

        if (firstFree + numToAdd <= dest.length) {
            System.arraycopy(source, 0, dest, firstFree, numToAdd);
            return dest;
        }
        T[] temp = (T[]) Array.newInstance(dest.getClass().getComponentType(), firstFree + numToAdd);
        System.arraycopy(dest, 0, temp, 0, firstFree);
        System.arraycopy(source, 0, temp, firstFree, numToAdd);
        return temp;
    }

    /**
     * Returns whether the specified array contains the specified object. Comparison is by
     * object identity.
     * @param array the array to search
     * @param obj the object to search for
     * @return true if the specified array contains the specified object, or the specified array is null
     */
    public static boolean contains(Object[] array, Object obj) {
    	return indexOf(array, obj) >= 0;
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
    	if (array != null) {
    		for (int i = 0; i < array.length; i++) {
    			if (array[i] == obj)
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
    public static boolean containsEqual(Object[] array, Object obj) {
    	return indexOfEqual(array, obj) != -1;
    }

    /**
     * Assumes that array contains nulls at the end, only. 
     * Returns the index into the specified array of the specified object, or -1 if the array does not
     * contain the object, or if the array is null.  Comparison is by equals().
     * @param comments the array to search
     * @param comment the object to search for
     * @return the index into the specified array of the specified object, or -1 if the array does not
     * contain an equal object, or if the array is null
     */    
	public static int indexOfEqual(Object[] comments, Object comment) {
    	int result = -1;
    	if (comments != null) {
    		for (int i= 0; (i < comments.length) && (comments[i] != null); i++) {
    			if (comments[i].equals(comment))
    				return i;
    		}
    	}
    	return result;
    }

	/**
	 * Note that this should only be used when the placement of nulls within the array
	 * is unknown (due to performance efficiency).  
	 * 
	 * Removes all of the nulls from the array and returns a new array that contains all
	 * of the non-null elements.
     *
     * If there are no nulls in the original array then the original array is returned.
	 */
	public static Object[] removeNulls(Class<?> c, Object[] array) {
        if (array == null)
            return (Object[]) Array.newInstance(c, 0);

        int i;
		int validEntries = 0;
		for (i = 0; i < array.length; i++) {
	         if (array[i] != null)
	        	 validEntries++;
	    }

		if (array.length == validEntries) 
			return array;

		Object[] newArray = (Object[]) Array.newInstance(c, validEntries);
		int j = 0;
        for (i = 0; i < array.length; i++) {
            if (array[i] != null)
            	newArray[j++] = array[i];
        }

		return newArray;
	}

	/**
	 * Note that this should only be used when the placement of nulls within the array
	 * is unknown (due to performance efficiency).  
	 * 
	 * Removes all of the nulls from the array and returns a new array that contains all
	 * of the non-null elements.
     *
     * If there are no nulls in the original array then the original array is returned.
	 * @since 5.2
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] removeNulls(T[] array) {
        int i;
		int validEntries = 0;
		for (i = 0; i < array.length; i++) {
	         if (array[i] != null)
	        	 validEntries++;
	    }

		if (array.length == validEntries) 
			return array;

		T[] newArray = (T[]) Array.newInstance(array.getClass().getComponentType(), validEntries);
		int j = 0;
        for (i = 0; i < array.length; i++) {
            if (array[i] != null)
            	newArray[j++] = array[i];
        }

		return newArray;
	}

	/**
	 * To improve performance, this method should be used instead of ArrayUtil#removeNulls(Class, Object[]) when
	 * all of the non-null elements in the array are grouped together at the beginning of the array
	 * and all of the nulls are at the end of the array.  
	 * The position of the last non-null element in the array must also be known. 
	 */
	public static Object[] removeNullsAfter(Class<?> c, Object[] array, int index) {
        final int newLen= index + 1;
        if (array != null && array.length == newLen)
			return array;

        Object[] newArray = (Object[]) Array.newInstance(c, newLen);
        if (array != null && newLen > 0)
        	System.arraycopy(array, 0, newArray, 0, newLen);
		return newArray;
	}

	/**
	 * Type safe version of {@link #removeNullsAfter(Class, Object[], int)}
	 * @since 5.1
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] trimAt(Class<T> c, T[] array, int index) {
		return (T[]) removeNullsAfter(c, array, index);
	}

	/**
	 * Inserts the obj at the beginning of the array, shifting the whole thing one index
	 * Assumes that array contains nulls at the end, only. 
	 */
	public static Object[] prepend(Class<?> c, Object[] array, Object obj) {
		if (obj == null)
    		return array;
        if (array == null || array.length == 0) {
            array = (Object[]) Array.newInstance(c, DEFAULT_LENGTH);
            array[0] = obj;
            return array;
        }

        int i = findFirstNull(array);
        if (i >= 0) {
			System.arraycopy(array, 0, array, 1, i);
			array[0] = obj;
        } else {
			Object[] temp = (Object[]) Array.newInstance(c, array.length * 2);
	        System.arraycopy(array, 0, temp, 1, array.length);
	        temp[0] = obj;
	        array = temp;
		}

        return array;
	}

	/**
	 * Inserts the obj at the beginning of the array, shifting the whole thing one index
	 * Assumes that array contains nulls at the end, only. 
	 * array must not be <code>null</code>.
	 * @since 5.2
	 */
	public static <T> T[] prepend(T[] array, T obj) {
		assert array != null;
		
		if (obj == null)
    		return array;
        if (array.length == 0) {
            array = newArray(array, DEFAULT_LENGTH);
            array[0] = obj;
            return array;
        }

        int i = findFirstNull(array);
        if (i >= 0) {
			System.arraycopy(array, 0, array, 1, i);
			array[0] = obj;
        } else {
			T[] temp = newArray(array, array.length*2);
	        System.arraycopy(array, 0, temp, 1, array.length);
	        temp[0] = obj;
	        array = temp;
		}

        return array;
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] newArray(T[] array, int newLen) {
		return (T[]) Array.newInstance(array.getClass().getComponentType(), newLen);
	}

	/**
	 * Removes first occurrence of element in array and moves objects behind up front.
	 * @since 4.0
	 */
	public static void remove(Object[] array, Object element) {
		if (array != null) {
			for (int i = 0; i < array.length; i++) {
				if (element == array[i]) {
					System.arraycopy(array, i + 1, array, i, array.length - i - 1);
					array[array.length - 1]= null;
					return;
				}
			}
		}
	}

    static public int[] setInt(int[] array, int idx, int val) {
        if (array == null) {
            array = new int[DEFAULT_LENGTH > idx + 1 ? DEFAULT_LENGTH : idx + 1];
            array[idx] = val;
            return array;
        }

        if (array.length <= idx) {
            int newLen = array.length * 2;
            while (newLen <= idx)
            	newLen *= 2;
            int[] temp = new int[newLen];
            System.arraycopy(array, 0, temp, 0, array.length);
            
            array = temp;
        }
        array[idx] = val;
        return array;
    }

    /**
     * Stores the specified array contents in a new array of specified
     * runtime type.
     * @param target the runtime type of the new array
     * @param source the source array
     * @return the current array stored in a new array with the
     * specified runtime type, or null if source is null.
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T[] convert(Class<T> target, S[] source) {
    	T[] result= null;
    	if (source != null) {
    		result= (T[]) Array.newInstance(target, source.length);
    		for (int i= 0; i < source.length; i++) {
    			result[i]= (T) source[i];
    		}
    	}
    	return result;
    }

    /**
     * Returns a new array that contains all of the elements of the 
     * given array except the first one.
     * 
	 * @since 5.1
	 * @throws NullPointerException if args is null
	 * @throws IllegalArgumentException if args.length <= 0
	 */
    @SuppressWarnings("unchecked")
	public static <T> T[] removeFirst(T[] args) {
    	int n = args.length;
    	if (n <= 0)
    		throw new IllegalArgumentException();
    	
    	T[] newArgs = (T[]) Array.newInstance(args.getClass().getComponentType(), n - 1);
    	for (int i = 1; i < n; i++) {
    		newArgs[i - 1] = args[i];
    	}
    	return newArgs;
    }
}
