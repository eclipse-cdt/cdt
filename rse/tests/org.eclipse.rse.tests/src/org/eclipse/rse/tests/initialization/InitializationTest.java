/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [197167] initial contribution.
 *******************************************************************************/

package org.eclipse.rse.tests.initialization;

import junit.framework.TestCase;

import org.eclipse.rse.core.RSECorePlugin;

/**
 * Should be run on a clean workspace.
 */
public class InitializationTest extends TestCase {
	
	public InitializationTest(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testInitialization() {
		//-test-author-:DavidDykstal
		try {
			RSECorePlugin.waitForInitCompletion();
		} catch (InterruptedException e) {
			fail("interrupted");
		}
		assertTrue("not indicating complete", RSECorePlugin.isInitComplete(RSECorePlugin.INIT_ALL));
		GoodInitializer goodInitializer = GoodInitializer.getInstance();
		BadInitializer badInitializer = BadInitializer.getInstance();
		UglyInitializer uglyInitializer = UglyInitializer.getInstance();
		ListenerInitializer listenerInitializer = ListenerInitializer.getInstance();
		assertTrue("good initializer not run", goodInitializer.wasRun() && goodInitializer.isComplete());
		assertTrue("bad initializer not run", badInitializer.wasRun() && !badInitializer.isComplete());
		assertTrue("ugly initializer not run", uglyInitializer.wasRun() && uglyInitializer.isComplete());
		assertTrue("listener initializer not run", listenerInitializer.wasRun() && listenerInitializer.isComplete());
		InitListener listener = listenerInitializer.getListener();
		assertFalse("listener saw phase INIT_MODEL", listener.sawPhase(RSECorePlugin.INIT_MODEL)); // shouldn't see this since it occurs before the listener is added
		assertTrue("listener missed phase INIT_ALL", listener.sawPhase(RSECorePlugin.INIT_ALL));
	}
		
}
