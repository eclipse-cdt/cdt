/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [197167] initial contribution.
 * David Dykstal (IBM) = [226958] add status values to waitForInitCompletion(phase)
 *******************************************************************************/

package org.eclipse.rse.tests.initialization;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.rse.core.IRSEInitListener;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.tests.RSETestsPlugin;

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
		RSECorePlugin.addInitListener(new IRSEInitListener() {
			public void phaseComplete(int phase) {
				System.out.println("I see phase " + phase);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.tests.core.RSECoreTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testInitialization() {
		//-test-author-:DavidDykstal
		if (!RSETestsPlugin.isTestCaseEnabled("InitializationTest.testInitialization"))return; //$NON-NLS-1$
		try {
			IStatus status = null;
			status = RSECorePlugin.waitForInitCompletion(RSECorePlugin.INIT_MODEL);
			assertEquals(Status.OK_STATUS, status);
			status = RSECorePlugin.waitForInitCompletion(RSECorePlugin.INIT_INITIALIZER);
			assertEquals(IStatus.WARNING, status.getSeverity()); // because of BadInitializer
			status = RSECorePlugin.waitForInitCompletion(RSECorePlugin.INIT_ALL);
			assertEquals(IStatus.WARNING, status.getSeverity()); // because of BadInitializer
			status = RSECorePlugin.waitForInitCompletion();
			assertEquals(IStatus.WARNING, status.getSeverity()); // because of BadInitializer
		} catch (InterruptedException e) {
			fail("interrupted");
		} catch (RuntimeException e) {
			throw e;
		}
		System.out.println("Init job should be done");
		assertTrue("not indicating complete", RSECorePlugin.isInitComplete(RSECorePlugin.INIT_ALL));
		GoodInitializer goodInitializer = GoodInitializer.getInstance();
		BadInitializer badInitializer = BadInitializer.getInstance();
		UglyInitializer uglyInitializer = UglyInitializer.getInstance();
		ListenerInitializer listenerInitializer = ListenerInitializer.getInstance();
		assertTrue("good initializer not run", goodInitializer.wasRun());
		assertTrue("bad initializer not run", badInitializer.wasRun());
		assertTrue("ugly initializer not run", uglyInitializer.wasRun());
		assertTrue("listener initializer not run", listenerInitializer.wasRun());
		InitListener listener = listenerInitializer.getListener();
		assertFalse("listener saw phase INIT_MODEL", listener.sawPhase(RSECorePlugin.INIT_MODEL)); // shouldn't see this since it occurs before the listener is added
		assertTrue("listener missed phase INIT_ALL", listener.sawPhase(RSECorePlugin.INIT_ALL));
	}

}
