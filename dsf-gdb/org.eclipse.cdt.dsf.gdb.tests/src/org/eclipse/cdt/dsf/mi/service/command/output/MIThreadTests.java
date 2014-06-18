/*******************************************************************************
 * Copyright (c) 2011, 2014 Freescale and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Freescale  - Initial API and Implementation
 *     Alvaro Sanchez-Leon (Ericsson) - Moved from cdt.tests.dsf.gdb (Bug 437562)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MIThreadTests {
	@Test
	public void testOsIdParsing() {
		assertEquals("7010", MIThread.parseOsId("Thread 0xb7c8ab90 (LWP 7010)")); 
		assertEquals("32942", MIThread.parseOsId("Thread 162.32942"));
		assertEquals("abc123", MIThread.parseOsId("Thread abc123"));
		assertEquals("abc123", MIThread.parseOsId("thread abc123"));
		assertEquals("abc123", MIThread.parseOsId("THREAD abc123"));		
		assertEquals("abc123", MIThread.parseOsId("process abc123"));		
	}
	
	@Test
	public void testParentIdParsing() {
		assertEquals("162", MIThread.parseParentId("Thread 162.32942"));
	}
}
