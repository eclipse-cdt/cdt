/*******************************************************************************
 * Copyright (c) 2011, 2014 Freescale and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	Freescale  - Initial Implementation
 * 	Alvaro Sanchez-Leon (Ericsson) - Bug 437562 - Convert the dsf-gdb test fragment back to a plug-in
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class MIThreadTests {
	private Class<?> fMIThreadCls;
	private Method fParseOsId;
	private Method fParseParentId;
	
	@Before
	public void doBeforeClass() {
		try {
			fMIThreadCls = Class.forName("org.eclipse.cdt.dsf.mi.service.command.output.MIThread");
		} catch (ClassNotFoundException e) {
			fail("Unable to resolve the MIThread class");
		}
		
		assertNotNull(fMIThreadCls);
		Class<?>[] param = {String.class};

		try {
			//resolve methods and override accessibility
			fParseOsId = fMIThreadCls.getDeclaredMethod("parseOsId", param);
			fParseOsId.setAccessible(true);
			
			fParseParentId = fMIThreadCls.getDeclaredMethod("parseParentId", param);
			fParseParentId.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail("NoSuchMethodException");
		} catch (SecurityException e) {
			e.printStackTrace();
			fail("SecurityException");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail("IllegalArgumentException");
		}
		
		assertNotNull(fParseOsId);
		assertNotNull(fParseParentId);
	}
	
	@Test
	public void testOsIdParsing() {
		assertEquals("7010", invoke(fParseOsId, "Thread 0xb7c8ab90 (LWP 7010)"));
		assertEquals("32942", invoke(fParseOsId, "Thread 162.32942"));
		assertEquals("abc123", invoke(fParseOsId, "Thread abc123"));
		assertEquals("abc123", invoke(fParseOsId, "thread abc123"));
		assertEquals("abc123", invoke(fParseOsId, "THREAD abc123"));
		assertEquals("abc123", invoke(fParseOsId, "process abc123"));
	}
	
	@Test
	public void testParentIdParsing() {
		assertEquals("162", invoke(fParseParentId, "Thread 162.32942"));
	}
	
	// invoke a static method receiving a arg:String and returning a String
	private String invoke(Method method, String arg) {
		String strRes = null;
		try {
			strRes = (String) method.invoke(null, arg);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail("IllegalAccessException");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail("IllegalArgumentException");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail("InvocationTargetException");
		}
		
		return strRes;
	}
}
