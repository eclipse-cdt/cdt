/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.cdt.core.parser.tests;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.util.ArrayUtil;

public class ArrayUtilTest extends TestCase {
	private final Object o1= new Object();
	private final Object o2= new Object();
	private final Object o3= new Object();
	private final Object o4= new Object();
	
	public void testAppend() {
		Object[] array= null;
		array= ArrayUtil.append(array, o1);
		array= ArrayUtil.append(array, o2);
		array= ArrayUtil.append(array, o3);
		assertEquals(o1, array[0]);
		assertEquals(o2, array[1]);
		assertEquals(o3, array[2]);

		array= ArrayUtil.appendAt(Object.class, null, 0, o1);
		array= ArrayUtil.appendAt(Object.class, array, 1, o2);
		array= ArrayUtil.appendAt(Object.class, array, 2, o3);
		assertEquals(o1, array[0]);
		assertEquals(o2, array[1]);
		assertEquals(o3, array[2]);
	}

	public void testPrepend() {
		Object[] array= null;
		array= ArrayUtil.prepend(Object.class, array, o1);
		array= ArrayUtil.prepend(Object.class, array, o2);
		array= ArrayUtil.prepend(Object.class, array, o3);
		assertEquals(o3, array[0]);
		assertEquals(o2, array[1]);
		assertEquals(o1, array[2]);
	}

	public void testTrim() {
		Object[] array= new Object[] {null, null};
		array= ArrayUtil.trim(Object.class, array);
		assertEquals(0, array.length);

		array= new Object[] {o1, null};
		array= ArrayUtil.trim(Object.class, array);
		assertEquals(1, array.length);
		assertEquals(o1, array[0]);

		array= new Object[] {o1, o2};
		Object[] array2= ArrayUtil.trim(Object.class, array);
		assertEquals(2, array2.length);
		assertSame(o1, array2[0]);
		assertSame(o2, array2[1]);
		assertSame(array, array2);

		array= new Object[] {null, null};
		array= ArrayUtil.trim(Object.class, array, true);
		assertEquals(0, array.length);

		array= new Object[] {o1, null};
		array= ArrayUtil.trim(Object.class, array, true);
		assertEquals(1, array.length);
		assertEquals(o1, array[0]);
		
		array= new Object[] {o1, o2};
		array2= ArrayUtil.trim(Object.class, array, true);
		assertEquals(2, array2.length);
		assertSame(o1, array2[0]);
		assertSame(o2, array2[1]);
		assertNotSame(array, array2);
	}

	public void testAddAll() {
		Object[] array1= {o1, o2, null};
		Object[] array2= {o3, null};
		Object[] result;
		
		result= ArrayUtil.addAll(Object.class, array2, array1); 
		assertEquals(o3, result[0]);
		assertEquals(o1, result[1]);
		assertEquals(o2, result[2]);
		
		result= ArrayUtil.addAll(Object.class, array1, array2);
		assertEquals(o1, result[0]);
		assertEquals(o2, result[1]);
		assertEquals(o3, result[2]);
		assertSame(array1, result);
		
		array1= new Object[] {o1, o2};
		array2= new Object[] {o3, null};
		
		result= ArrayUtil.addAll(Object.class, array2, array1); 
		assertEquals(o3, result[0]);
		assertEquals(o1, result[1]);
		assertEquals(o2, result[2]);
		
		result= ArrayUtil.addAll(Object.class, array1, array2);
		assertEquals(o1, result[0]);
		assertEquals(o2, result[1]);
		assertEquals(o3, result[2]);

		array1= new Object[] {o1, o2};
		array2= new Object[] {o3};
		
		result= ArrayUtil.addAll(Object.class, array2, array1); 
		assertEquals(o3, result[0]);
		assertEquals(o1, result[1]);
		assertEquals(o2, result[2]);
		
		result= ArrayUtil.addAll(Object.class, array1, array2);
		assertEquals(o1, result[0]);
		assertEquals(o2, result[1]);
		assertEquals(o3, result[2]);
		
		array1= new Object[] {o1, o2};
		array2= new Object[] {};
		result= ArrayUtil.addAll(Object.class, array2, array1);
		assertEquals(o1, result[0]);
		assertEquals(o2, result[1]);
		assertNotSame(array1, result);

		result= ArrayUtil.addAll(Object.class, array1, array2);
		assertEquals(o1, result[0]);
		assertEquals(o2, result[1]);
		assertSame(array1, result);
	}
	
	public void testRemove() {
		Object[] array= new Object[] {o1, o2, o3, o4, null};
		ArrayUtil.remove(array, o3);
		assertSame(o1, array[0]);
		assertSame(o2, array[1]);
		assertSame(o4, array[2]);
		assertNull(array[3]);

		ArrayUtil.remove(array, o1);
		assertSame(o2, array[0]);
		assertSame(o4, array[1]);
		assertNull(array[2]);

		ArrayUtil.remove(array, o4);
		assertSame(o2, array[0]);
		assertNull(array[1]);

		ArrayUtil.remove(array, o2);
		assertNull(array[0]);

		array= new Object[] {o1, o2, o3, o4};
		ArrayUtil.remove(array, o3);
		assertSame(o1, array[0]);
		assertSame(o2, array[1]);
		assertSame(o4, array[2]);
		assertNull(array[3]);

		ArrayUtil.remove(array, o1);
		assertSame(o2, array[0]);
		assertSame(o4, array[1]);
		assertNull(array[2]);

		ArrayUtil.remove(array, o4);
		assertSame(o2, array[0]);
		assertNull(array[1]);

		ArrayUtil.remove(array, o2);
		assertNull(array[0]);
	}
	
	public void testRemoveNulls() {
		Object[] array= new Object[0];
		Object[] result;
		
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(0, result.length);
		assertSame(result, array);
		
		array= new Object[]{null};
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(0, result.length);

		array= new Object[]{o1};
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(1, result.length);
		assertSame(result[0], o1);
		assertSame(result, array);

		array= new Object[]{o1, null};
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(1, result.length);
		assertSame(result[0], o1);

		array= new Object[]{null, o1};
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(1, result.length);
		assertSame(result[0], o1);

		array= new Object[]{o1, o2};
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(2, result.length);
		assertSame(result[0], o1);
		assertSame(result[1], o2);
		assertSame(result, array);

		array= new Object[]{null, o1, o2};
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(2, result.length);
		assertSame(result[0], o1);
		assertSame(result[1], o2);

		array= new Object[]{o1, null, o2};
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(2, result.length);
		assertSame(result[0], o1);
		assertSame(result[1], o2);

		array= new Object[]{o1, o2, null};
		result= ArrayUtil.removeNulls(Object.class, array);
		assertEquals(2, result.length);
		assertSame(result[0], o1);
		assertSame(result[1], o2);
	}
}
