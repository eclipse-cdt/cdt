/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.internal;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.IRSECoreTestCaseProperties;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.tests.core.RSEWaitAndDispatchUtil;
import org.eclipse.rse.tests.core.RSEWaitAndDispatchUtil.IInterruptCondition;
import org.eclipse.ui.PlatformUI;

/**
 * Tests the very core RSE test framework functionality.
 */
public class RSEInternalFrameworkTestCase extends RSECoreTestCase {

	/**
	 * Test the properties managment and support methods of the
	 * <code>RSECoreTestCase</code> implementation.
	 */
	public void testCoreTestPropertiesHandling() {
		if (!RSETestsPlugin.isTestCaseEnabled("RSEInternalFrameworkTestCase.testCoreTestPropertiesHandling")) return; //$NON-NLS-1$
		
		// test for our defaults
		assertTrue("Unexpected default for property PROP_MAXIMIZE_REMOTE_SYSTEMS_VIEW!", isProperty(IRSECoreTestCaseProperties.PROP_MAXIMIZE_REMOTE_SYSTEMS_VIEW, false)); //$NON-NLS-1$
		assertEquals("Unexpected default for property PROP_SWITCH_TO_PERSPECTIVE!", "org.eclipse.rse.ui.view.SystemPerspective", getProperty(IRSECoreTestCaseProperties.PROP_SWITCH_TO_PERSPECTIVE)); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected default for property PROP_FORCE_BACKGROUND_EXECUTION!", isProperty(IRSECoreTestCaseProperties.PROP_FORCE_BACKGROUND_EXECUTION, false)); //$NON-NLS-1$
		assertTrue("Unexpected default for property PROP_PERFORMANCE_TIMING_INCLUDE_SETUP_TEARDOWN!", isProperty(IRSECoreTestCaseProperties.PROP_PERFORMANCE_TIMING_INCLUDE_SETUP_TEARDOWN, false)); //$NON-NLS-1$

		// test the specific methods with simulated data
		setProperty("testBooleanProperty", true); //$NON-NLS-1$
		assertTrue("Unexpected stored value for testBooleanProperty!", isProperty("testBooleanProperty", true)); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("testBooleanProperty", false); //$NON-NLS-1$
		assertTrue("Unexpected stored value for testBooleanProperty!", isProperty("testBooleanProperty", false)); //$NON-NLS-1$ //$NON-NLS-2$
		setProperty("testBooleanProperty", null); //$NON-NLS-1$
		assertNull("testBooleanProperty not removed!", getProperty("testBooleanProperty")); //$NON-NLS-1$ //$NON-NLS-2$

		setProperty("testStringProperty", "stringPropertyValue"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected stored value for testStringProperty!", isProperty("testStringProperty", "stringPropertyValue")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setProperty("testStringProperty", "0123456789"); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Unexpected stored value for testStringProperty!", isProperty("testStringProperty", "0123456789")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		setProperty("testStringProperty", null); //$NON-NLS-1$
		assertNull("testStringProperty not removed!", getProperty("testStringProperty")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Test waiter interrupt condition implementation.
	 */
	private static class TestWaiter implements IInterruptCondition {
		private final List params;
		public TestWaiter(List params) {
			assert params != null;
			this.params = params;
		}
		public boolean isTrue() { return params.size() > 0; }
		public void dispose() { params.clear(); }
	}
	
	private static class TestJob extends Job {
		private final List params;
		public TestJob(List params) {
			super("Test Job"); //$NON-NLS-1$
			assert params != null;
			this.params = params;
		}
		
		protected IStatus run(IProgressMonitor monitor) {
			PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
				public void run() {
					params.add(Boolean.TRUE);
				}
			});
			return Status.OK_STATUS;
		}
	}
	
	/**
	 * Test the <code>RSEWaitAndDispatchUtil</code> wait methods.
	 */
	public void testWaitAndDispatch() {
		if (!RSETestsPlugin.isTestCaseEnabled("RSEInternalFrameworkTestCase.testWaitAndDispatch")) return; //$NON-NLS-1$
		
		// the simple wait and dispatch is time out based
		long start = System.currentTimeMillis();
		RSEWaitAndDispatchUtil.waitAndDispatch(2500);
		long end = System.currentTimeMillis();
		assertTrue("Failed to wait a given timeout!", (end - start) >= 2500); //$NON-NLS-1$
		
		// the more complex wait and dispatch method has to stop
		// on a given condition.
		final List params = new ArrayList();
		
		// the trick here is to make the condition true only if a
		// runnable passed through the display thread. That should
		// give us the asurance that the display event dispatching
		// is kept running.
		Job job = new TestJob(params);
		job.setUser(false);
		job.setSystem(true);
		job.setPriority(Job.SHORT);
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		job.schedule(3000);
		
		boolean timeout = RSEWaitAndDispatchUtil.waitAndDispatch(10000, new TestWaiter(params));
		assertFalse("Interrupt condition failed to stop wait method!", timeout); //$NON-NLS-1$
		assertEquals("Interrupt condition failed to dispose!", 0, params.size()); //$NON-NLS-1$
	}
	
	/**
	 * Test accessing the test data location.
	 */
	public void testTestDataLocationManagement() {
		if (!RSETestsPlugin.isTestCaseEnabled("RSEInternalFrameworkTestCase.testTestDataLocationManagement")) return; //$NON-NLS-1$
		
		// get the pure test data location root path.
		IPath root = getTestDataLocation("", false); //$NON-NLS-1$
		assertNotNull("Failed to query test data location root!", root); //$NON-NLS-1$
		assertTrue("Test data root location " + root.toOSString() + " is not a directory!", root.toFile().isDirectory()); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Test data root location " + root.toOSString() + " cannot be read!", root.toFile().canRead()); //$NON-NLS-1$ //$NON-NLS-2$
		
		// get a test data location path under the root
		String relative = "unittest_" + System.currentTimeMillis(); //$NON-NLS-1$
		// as the directories should not exist yet, a call to getTestDataLocation must return null
		IPath path = getTestDataLocation(relative, false);
		assertNull("Test data location exist but should not!", path); //$NON-NLS-1$
		
		// go and create the path now (including the OS)
		String os = Platform.getOS();
		assertNotNull("Failed to query current execution host operating system string!", os); //$NON-NLS-1$
		path = root.append(relative + "/" + os); //$NON-NLS-1$
		assertTrue("Failed to create test data location directories. Permission problem?", path.toFile().mkdirs()); //$NON-NLS-1$
		
		// Now, the re-query must be successful.
		IPath path2 = getTestDataLocation(relative, false);
		assertNotNull("Test data location " + root.append(relative).toOSString() + " seems not to exist!", path2); //$NON-NLS-1$ //$NON-NLS-2$
		path2 = getTestDataLocation(relative, true);
		assertNotNull("Test data location " + path.toOSString() + " seems not to exist!", path2); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Delete the created pathes again
		assertTrue("Failed to delete test data location " + path.toOSString() + "!", path.toFile().delete()); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("Failed to delete test data location " + root.append(relative).toOSString() + "!", root.append(relative).toFile().delete()); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
