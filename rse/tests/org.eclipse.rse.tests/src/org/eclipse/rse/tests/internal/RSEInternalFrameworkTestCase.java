/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 * Martin Oberhuber (Wind River) - [184095] Replace systemTypeName by IRSESystemType
 * Martin Oberhuber (Wind River) - organize, enable and tag test cases
 *******************************************************************************/
package org.eclipse.rse.tests.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.tests.core.IRSECoreTestCaseProperties;
import org.eclipse.rse.tests.core.RSEWaitAndDispatchUtil;
import org.eclipse.rse.tests.core.RSEWaitAndDispatchUtil.IInterruptCondition;
import org.eclipse.rse.tests.core.connection.IRSEConnectionProperties;
import org.eclipse.rse.tests.core.connection.RSEBaseConnectionTestCase;
import org.eclipse.ui.PlatformUI;

/**
 * Tests the very core RSE test framework functionality.
 */
public class RSEInternalFrameworkTestCase extends RSEBaseConnectionTestCase {

	/**
	 * Test the properties managment and support methods of the
	 * <code>RSECoreTestCase</code> implementation.
	 */
	public void testCoreTestPropertiesHandling() {
		//-test-author-:UweStieber
		if (isTestDisabled())
			return;

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
		final List params;
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
		//-test-author-:UweStieber
		if (isTestDisabled())
			return;

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
		//-test-author-:UweStieber
		if (isTestDisabled())
			return;

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

	/**
	 * Test RSE connection manager and related functionality.
	 */
	public void testConnectionManager() {
		//-test-author-:UweStieber
		if (isTestDisabled())
			return;

		// get the pure test data location root path.
		IPath location = getTestDataLocation("testConnectionManager", false); //$NON-NLS-1$
		assertNotNull("Cannot locate test data! Missing test data location?", location); //$NON-NLS-1$
		location = location.append("connection.properties"); //$NON-NLS-1$
		assertNotNull("Failed to construct location to 'connection.properties' test data file!", location); //$NON-NLS-1$
		assertTrue("Required test data file seems to be not a file!", location.toFile().isFile()); //$NON-NLS-1$
		assertTrue("Required test data file is not readable!", location.toFile().canRead()); //$NON-NLS-1$

		// load the test connection properties from the data file.
		IRSEConnectionProperties properties = getConnectionManager().loadConnectionProperties(location, true);
		assertNotNull("Failed to load test connection properties from location " + location.toOSString(), properties); //$NON-NLS-1$
		assertEquals("Property name does not match!", "test_windows", properties.getProperty(IRSEConnectionProperties.ATTR_NAME)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Property profile name does not match!", "junit_test_profile", properties.getProperty(IRSEConnectionProperties.ATTR_PROFILE_NAME)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Property system type does not match!", IRSESystemType.SYSTEMTYPE_WINDOWS_ID, properties.getProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID)); //$NON-NLS-1$
		assertEquals("Property remote system address does not match!", "128.0.0.1", properties.getProperty(IRSEConnectionProperties.ATTR_ADDRESS)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Property user id does not match!", "test_user", properties.getProperty(IRSEConnectionProperties.ATTR_USERID)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Property password does not match!", "test_passwd", properties.getProperty(IRSEConnectionProperties.ATTR_PASSWORD)); //$NON-NLS-1$ //$NON-NLS-2$

		// test the loading with partial connection information (with defauls)
		Properties props = new Properties();
		props.setProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID, IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID);
		props.setProperty(IRSEConnectionProperties.ATTR_USERID, "local_user"); //$NON-NLS-1$
		props.setProperty(IRSEConnectionProperties.ATTR_PASSWORD, "local_passwd"); //$NON-NLS-1$
		properties = getConnectionManager().loadConnectionProperties(props, true);
		assertNotNull("Failed to load test connection properties from location " + location.toOSString(), properties); //$NON-NLS-1$
		assertEquals("Property name does not match!", "Local", properties.getProperty(IRSEConnectionProperties.ATTR_NAME)); //$NON-NLS-1$ //$NON-NLS-2$
		assertNull("Property profile name does not match!", properties.getProperty(IRSEConnectionProperties.ATTR_PROFILE_NAME)); //$NON-NLS-1$
		assertEquals("Property system type does not match!", IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID, properties.getProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID)); //$NON-NLS-1$
		assertEquals("Property remote system address does not match!", "localhost", properties.getProperty(IRSEConnectionProperties.ATTR_ADDRESS)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Property user id does not match!", "local_user", properties.getProperty(IRSEConnectionProperties.ATTR_USERID)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Property password does not match!", "local_passwd", properties.getProperty(IRSEConnectionProperties.ATTR_PASSWORD)); //$NON-NLS-1$ //$NON-NLS-2$

		// test the loading with partial connection information (without defauls)
		properties = getConnectionManager().loadConnectionProperties(props, false);
		assertNotNull("Failed to load test connection properties from location " + location.toOSString(), properties); //$NON-NLS-1$
		assertNull("Property name does not match!", properties.getProperty(IRSEConnectionProperties.ATTR_NAME)); //$NON-NLS-1$
		assertNull("Property profile name does not match!", properties.getProperty(IRSEConnectionProperties.ATTR_PROFILE_NAME)); //$NON-NLS-1$
		assertEquals("Property system type does not match!", IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID, properties.getProperty(IRSEConnectionProperties.ATTR_SYSTEM_TYPE_ID)); //$NON-NLS-1$
		assertNull("Property remote system address does not match!", properties.getProperty(IRSEConnectionProperties.ATTR_ADDRESS)); //$NON-NLS-1$
		assertEquals("Property user id does not match!", "local_user", properties.getProperty(IRSEConnectionProperties.ATTR_USERID)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("Property password does not match!", "local_passwd", properties.getProperty(IRSEConnectionProperties.ATTR_PASSWORD)); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
