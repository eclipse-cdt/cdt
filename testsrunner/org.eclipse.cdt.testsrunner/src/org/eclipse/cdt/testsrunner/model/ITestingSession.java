/*******************************************************************************
 * Copyright (c) 2011 Anton Gorenkov 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Gorenkov - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.model;

import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProviderInfo;
import org.eclipse.debug.core.ILaunch;


/**
 * Stores the information about tests running.
 * 
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ITestingSession {

	/**
	 * Returns the count of already finished tests.
	 *
	 * @return already finished tests count
	 */
	public int getCurrentCounter();

	/**
	 * Returns the total tests count (if available).
	 * 
	 * @note Usually testing frameworks do not provide the information about
	 * total tests count (at least Boost.Test, Google Test and Qt Test do not).
	 * So the total tests count from previous launch of the same test module is
	 * used instead. That is why, it is unavailable for the first run and it may
	 * be not accurate. When testing is finished the total tests count is
	 * updated to the accurate value.
	 * 
	 * @return total tests count or -1 if unavailable
	 */
	public int getTotalCounter();
	
	/**
	 * Returns the count tests with the specified status at the moment.
	 * 
	 * @note Tests count with the specified status may change if testing session
	 * is not terminated.
	 * 
	 * @param status the required status
	 * @return tests count with status 'status'
	 */
	public int getCount(ITestItem.Status status);

	/**
	 * Returns whether the testing session contains errors at the moment.
	 * Testing session may contain errors if one of its tests failed or if there
	 * were some errors during tests running (e.g. test module cannot be
	 * launched or output parsing failed).
	 * 
	 * @return whether the testing session contains errors
	 */
	public boolean hasErrors();

	/**
	 * Returns whether the testing session was stopped by user.
	 * 
	 * @note Testing session is not considered as stopped when it was terminated
	 * by the <code>ILaunch.terminate()</code> method (e.g. when user clicks on
	 * 'Stop' button of the 'Console' or 'Debug' view). In this case it will be
	 * considered as finished (probably, with errors cause not all output was
	 * obtained from launched process). To stop testing session properly the
	 * <code>stop()</code> method should be used.
	 * 
	 * @return whether the testing session was stopped
	 */
	public boolean wasStopped();

	/**
	 * Returns whether the testing session has been finished (with or without
	 * errors).
	 * 
	 * @return whether the testing session has been finished
	 */
	public boolean isFinished();
	
	/**
	 * Returns the testing model accessor that is associated with this session.
	 * 
	 * @return testing model accessor
	 */
	public ITestModelAccessor getModelAccessor();

	/**
	 * Returns the launch that is associated with this testing session.
	 * 
	 * @return launch
	 */
	public ILaunch getLaunch();
	
	/**
	 * Returns the information about the tests runner which was used to launch
	 * this testing session.
	 * 
	 * @return tests runner information
	 */
	public ITestsRunnerProviderInfo getTestsRunnerProviderInfo();

	/**
	 * Returns the current status message for the current testing session. It
	 * may contain error, some session information or simple statistics.
	 * 
	 * @return text status of the testing session
	 */
	public String getStatusMessage();

	/**
	 * Returns the name of the current testing session. Usually it contains
	 * launch configuration name with some additional information.
	 * 
	 * @return testing session name
	 */
	public String getName();

	/**
	 * Stops the current session with a special status setting. Does nothing if
	 * the launched process of testing session has already been terminated or
	 * cannot been terminated.
	 */
	public void stop();
	
}
	
