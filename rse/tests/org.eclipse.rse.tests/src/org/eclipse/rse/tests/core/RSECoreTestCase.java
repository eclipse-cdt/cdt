/* *******************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial contribution.
 * *******************************************************************************/
package org.eclipse.rse.tests.core;

import java.util.Properties;

import junit.framework.TestCase;
import junit.framework.TestResult;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.swt.widgets.Display;

/**
 * Core RSE test case infra structure implementation.
 */
public class RSECoreTestCase extends TestCase {
	private final Properties properties = new Properties();
	
	/**
	 * Constructor.
	 */
	public RSECoreTestCase() {
		this(null);
	}

	/**
	 * Constructor.
	 * 
	 * @param name The test name.
	 */
	public RSECoreTestCase(String name) {
		super(name);
		// clear out all properties on construction.
		properties.clear();
		// initialize the core test properties
		initializeProperties();
	}

	// ***** Test properties management and support methods *****
	
	/**
	 * Initialize the core test properties. Override to modify core
	 * test properties or to add additional ones.
	 */
	protected void initializeProperties() {
		setProperty(IRSECoreTestCaseProperties.PROP_MAXIMIZE_REMOTE_SYSTEMS_VIEW, true);
		setProperty(IRSECoreTestCaseProperties.PROP_SWITCH_TO_PERSPECTIVE, "org.eclipse.rse.ui.view.SystemPerspective"); //$NON-NLS-1$
		setProperty(IRSECoreTestCaseProperties.PROP_FORCE_BACKGROUND_EXECUTION, false);
	}
	
	/**
	 * Enables or disables the specified property.
	 * 
	 * @param key The key of the property to enable or disable. Must be not <code>null</code>!
	 * @param enable Specify <code>true</code> to enable the property, <code>false</code> to disable the property.
	 */
	protected final void setProperty(String key, boolean enable) {
		setProperty(key, enable ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
	}
	
	/**
	 * Test if the specified property is equal to the specified value.
	 * 
	 * @param key The key of the property to test. Must be not <code>null</code>!
	 * @param value The value to compare the property with.
	 * @return <code>true</code> if the property is equal to the specified value, <code>false</code> otherwise.
	 */
	protected final boolean isProperty(String key, boolean value) {
		assert key != null;
		return (value ? Boolean.TRUE : Boolean.FALSE).equals(Boolean.valueOf(properties.getProperty(key, "false"))); //$NON-NLS-1$
	}
	
	/**
	 * Sets the specified string value for the specified property. If the specified
	 * value is <code>null</code>, the specified property will be removed.
	 * 
	 * @param key The key of the property to set. Must be not <code>null</code>!
	 * @param value The string value to set or <code>null</code>.
	 */
	protected final void setProperty(String key, String value) {
		assert key != null;
		if (value != null) {
			properties.setProperty(key, value);
		} else {
			properties.remove(key);
		}
	}
	
	/**
	 * Test if the specified property is equal to the specified value. If the specified
	 * value is <code>null</code>, this method returns <code>true</code> if the specified
	 * property key does not exist. The comparisation is case insensitive.
	 * 
	 * @param key The key of the property to test. Must be not <code>null</code>!
	 * @param value The value to compare the property with or <code>null</code>
	 * @return <code>true</code> if the property is equal to the specified value 
	 *         or the specified value is <code>null</code> and the property does not exist,
	 *         <code>false</code> otherwise.
	 */
	protected final boolean isProperty(String key, String value) {
		assert key != null;
		if (value != null) {
			return value.equalsIgnoreCase(properties.getProperty(key));
		} else {
			return !properties.containsKey(key);
		}
	}

	// ***** Test case life cycle management and support methods *****

	private final static QualifiedName BACKGROUND_TEST_EXECUTION_FINISHED = new QualifiedName(RSETestsPlugin.getDefault().getBundle().getSymbolicName(), "background_test_execution_finished"); //$NON-NLS-1$
	
	private final class RSEBackgroundTestExecutionJob extends Job {
		private final TestResult result;

		/**
		 * Constructor.
		 */
		public RSEBackgroundTestExecutionJob(TestResult result) {
			super("RSE JUnit Test Case Execution Job"); //$NON-NLS-1$
			setUser(false);
			setPriority(Job.INTERACTIVE);
			setRule(ResourcesPlugin.getWorkspace().getRoot());
			
			assert result != null;
			this.result = result;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		protected IStatus run(IProgressMonitor monitor) {
			monitor.beginTask("Running test " + RSECoreTestCase.this.getName() + " ...", IProgressMonitor.UNKNOWN); //$NON-NLS-1$ //$NON-NLS-2$

			// Execute the test now.
			RSECoreTestCase.super.run(result);
			
			monitor.done();
			
			setProperty(BACKGROUND_TEST_EXECUTION_FINISHED, Boolean.TRUE);

			// The job never fails. The test result is the real result.
			return Status.OK_STATUS;
		} 
	}
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#run(junit.framework.TestResult)
	 */
	public final void run(TestResult result) {
		if (isProperty(IRSECoreTestCaseProperties.PROP_FORCE_BACKGROUND_EXECUTION, false)) {
			// do not force test execution into background, just call super.run(result)
			// from with the current thread.
			super.run(result);
		} else {
			// Create the background job
			final Job job = new RSEBackgroundTestExecutionJob(result);
			// Initialize the BACKGROUND_EXECUTION_TEST_RESULT property
			job.setProperty(BACKGROUND_TEST_EXECUTION_FINISHED, Boolean.FALSE);
			// schedule the job to run immediatelly
			job.schedule();
			
			// wait till the job finished executing
			Boolean isFinished = (Boolean)job.getProperty(BACKGROUND_TEST_EXECUTION_FINISHED);
				Display display = Display.findDisplay(Thread.currentThread());
				if (display != null) {
					// The current thread is a display thread. The display event queue
					// must be not blocked as otherwise asynchronous events are blocked too!!!!!
					while (!Boolean.TRUE.equals(isFinished)) {
						if (!display.readAndDispatch()) display.sleep();
						isFinished = (Boolean)job.getProperty(BACKGROUND_TEST_EXECUTION_FINISHED);
					}
				} else {
					// The current thread is not a display thread. The thread can be put asleep
					// for while till the test result is retried to poll.
					while (!Boolean.TRUE.equals(isFinished)) {
						try { Thread.sleep(500); } catch (InterruptedException e) { /* ignored on purpose */ }
						isFinished = (Boolean)job.getProperty(BACKGROUND_TEST_EXECUTION_FINISHED);
					}
				}
		}
	}

	
	
}
