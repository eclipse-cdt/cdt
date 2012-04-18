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
package org.eclipse.cdt.testsrunner.internal.model;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import org.eclipse.cdt.testsrunner.internal.TestsRunnerPlugin;
import org.eclipse.cdt.testsrunner.internal.launcher.TestsRunnerProviderInfo;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.IModelVisitor;
import org.eclipse.cdt.testsrunner.model.ITestCase;
import org.eclipse.cdt.testsrunner.model.ITestItem;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage;
import org.eclipse.cdt.testsrunner.model.ITestModelAccessor;
import org.eclipse.cdt.testsrunner.model.ITestSuite;
import org.eclipse.cdt.testsrunner.model.ITestingSession;
import org.eclipse.cdt.testsrunner.model.ITestingSessionListener;
import org.eclipse.cdt.testsrunner.model.TestingException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;

/**
 * Stores the information about tests running.
 */
public class TestingSession implements ITestingSession {

	/** Launch object the is connected to the tests running. */
	private ILaunch launch;
	
	/** Information about used Tests Runner provider plug-in. */
	private TestsRunnerProviderInfo testsRunnerProviderInfo;

	/** Main interface to Tests Runner provider plug-in. */
	private ITestsRunnerProvider testsRunnerProvider;
	
	/**
	 * Test Model manager that is used to fill and update the model for the
	 * session.
	 */
	private TestModelManager modelManager;
	
	/**
	 * Total tests counter. It is -1 by default, that means that total tests
	 * count is not available.
	 * 
	 * @see getTotalCounter()
	 */
	private int totalCounter = -1;

	/** Already finished tests counter. */
	private int currentCounter = 0;
	
	/**
	 * Test counters map by test status. They are used to quickly provide simple
	 * statistics without model scanning.
	 * 
	 */
	private Map<ITestItem.Status, Integer> statusCounters = new EnumMap<ITestItem.Status, Integer>(ITestItem.Status.class);

	/**
	 * The flag stores whether the testing session contains errors at the
	 * moment.
	 * 
	 * @see hasErrors()
	 */
	private boolean hasErrors = false;
	
	/**
	 * The flag stores whether the testing session was stopped by user.
	 * 
	 * @see wasStopped()
	 */
	private boolean wasStopped = false;
	
	/**
	 * The flag stores whether the testing session has been finished (with or
	 * without errors).
	 */
	private boolean finished = false;
	
	/** Stores current status of the testing session. */
	private String statusMessage = ModelMessages.TestingSession_starting_status;
	
	/** Stores the time when the testing session was created. */
	private long startTime;
	
	
	/**
	 * Counts the number of the test cases in tests hierarchy.
	 */
	private class TestCasesCounter implements IModelVisitor {
		
		public int result = 0;
		
		@Override
		public void visit(ITestCase testCase) {
			++result;
		}
		
		@Override
		public void visit(ITestSuite testSuite) {}
		@Override
		public void visit(ITestMessage testMessage) {}
		@Override
		public void leave(ITestSuite testSuite) {}
		@Override
		public void leave(ITestCase testCase) {}
		@Override
		public void leave(ITestMessage testMessage) {}
	}

	
	/**
	 * The constructor.
	 * 
	 * @param launch connected launch object
	 * @param testsRunnerProviderInfo the information about the tests runner
	 * @param previousSession is used to determine total tests count & for tests
	 * hierarchy reusing if it is considered as similar
	 */
	public TestingSession(ILaunch launch, TestsRunnerProviderInfo testsRunnerProviderInfo, TestingSession previousSession) {
		this.launch = launch;
		this.testsRunnerProviderInfo = testsRunnerProviderInfo;
		this.testsRunnerProvider = testsRunnerProviderInfo.instantiateTestsRunnerProvider();
		this.startTime = System.currentTimeMillis();
		// Calculate approximate tests count by the previous similar testing session (if available)
		if (previousSession != null) {
			TestCasesCounter testCasesCounter = new TestCasesCounter();
			previousSession.getModelAccessor().getRootSuite().visit(testCasesCounter);
			totalCounter = testCasesCounter.result;
		}
		ITestSuite rootTestSuite = previousSession != null ? previousSession.getModelAccessor().getRootSuite() : null;
		this.modelManager = new TestModelManager(rootTestSuite, testsRunnerProviderInfo.isAllowedTestingTimeMeasurement());
		this.modelManager.addChangesListener(new ITestingSessionListener() {
			
			@Override
			public void testingStarted() {}
			
			@Override
			public void testingFinished() {
				// This is necessary if totalCounter was -1 (tests count was unknown)
				// or if tests count was estimated not accurately
				totalCounter = currentCounter;
			}
			
			@Override
			public void exitTestSuite(ITestSuite testSuite) {}
			
			@Override
			public void exitTestCase(ITestCase testCase) {
				// Update testing session info (counters, flags)
				Status testStatus = testCase.getStatus();
				statusCounters.put(testStatus, getCount(testStatus)+1);
				++currentCounter;
				if (testStatus.isError())
					hasErrors = true;
			}
			
			@Override
			public void enterTestSuite(ITestSuite testSuite) {}
			
			@Override
			public void enterTestCase(ITestCase testCase) {}
			
			@Override
			public void childrenUpdate(ITestSuite parent) {}
		});
	}

	/**
	 * Starts the processing of the test module output.
	 * 
	 * @param inputStream test module output stream
	 */
	public void run(InputStream inputStream) {
		modelManager.testingStarted();
		try {
			testsRunnerProvider.run(modelManager, inputStream);
			// If testing session was stopped, the status is set in stop()
			if (!wasStopped()) {
				double testingTime = getModelAccessor().getRootSuite().getTestingTime();
				statusMessage = MessageFormat.format(ModelMessages.TestingSession_finished_status, testingTime/1000.0);
			}
		} catch (TestingException e) {
			// If testing session was stopped, the status is set in stop()
			if (!wasStopped()) {
				statusMessage = e.getLocalizedMessage();
				hasErrors = true;
			}
		}
		finished = true;
		modelManager.testingFinished();
	}

	@Override
	public int getCurrentCounter() {
		return currentCounter;
	}

	@Override
	public int getTotalCounter() {
		return totalCounter;
	}
	
	@Override
	public int getCount(ITestItem.Status status) {
		Integer counterValue = statusCounters.get(status);
		return (counterValue == null) ? 0 : counterValue;
	}

	@Override
	public boolean hasErrors() {
		return hasErrors;
	}

	@Override
	public boolean wasStopped() {
		return wasStopped;
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

	@Override
	public ITestModelAccessor getModelAccessor() {
		return modelManager;
	}
	
	@Override
	public ILaunch getLaunch() {
		return launch;
	}
	
	@Override
	public TestsRunnerProviderInfo getTestsRunnerProviderInfo() {
		return testsRunnerProviderInfo;
	}
	
	@Override
	public String getStatusMessage() {
		return statusMessage;
	}

	@Override
	public String getName() {
		String launchConfName = launch.getLaunchConfiguration().getName();
		String startTimeStr = DateFormat.getDateTimeInstance().format(new Date(startTime));
		return MessageFormat.format(ModelMessages.TestingSession_name_format, launchConfName, startTimeStr);
	}

	@Override
	public void stop() {
		if (!launch.isTerminated() && launch.canTerminate()) {
			try {
				launch.terminate();
				wasStopped = true;
				statusMessage = ModelMessages.TestingSession_stopped_status;
			} catch (DebugException e) {
				TestsRunnerPlugin.log(e);
			}
		}
	}
	
}
