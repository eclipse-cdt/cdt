/*******************************************************************************
 * Copyright (c) 2015 Colin Leitner
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Colin Leitner - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.testsrunner.testsrunners;

import org.eclipse.cdt.testsrunner.internal.tap.TAPTestsRunnerProvider;
import org.eclipse.cdt.testsrunner.launcher.ITestsRunnerProvider;
import org.eclipse.cdt.testsrunner.model.ITestItem.Status;
import org.eclipse.cdt.testsrunner.model.ITestMessage.Level;

@SuppressWarnings("nls")
public class TAPTestCase extends BaseTestCase {

	@Override
	protected ITestsRunnerProvider createTestsRunner() {
		return new TAPTestsRunnerProvider();
	}

	//
	public void testNoTestCases() {
	}
	
	//TAP version 1
	public void testIgnoreVersion()	{		
	}
	
	//
	//TAP version 1
	public void testVersionMustBeOnFirstLine()	{
		expectTestingException();
	}
	
	//ok
	//not ok
	//ok # skip
	//not ok # SKiPped
	//ok # todo
	//not ok # toDO
	public void testBasicTestCases()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("2");
		mockModelUpdater.setTestStatus(Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("3");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "skip");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("4");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "SKiPped");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("5");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "todo");
		mockModelUpdater.setTestStatus(Status.NotRun);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("6");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "toDO");
		mockModelUpdater.setTestStatus(Status.NotRun);
		mockModelUpdater.exitTestCase();
	}
	
	//1..3
	//ok
	//not ok
	public void testMorePlannedThanExecutedTestCases()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("2");
		mockModelUpdater.setTestStatus(Status.Failed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("3");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
	}
	
	//ok
	//ok 4
	public void testForwardJump()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("2");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("3");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("4");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
	}
	
	//ok
	//ok 1
	public void testNoBackwardJump()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
		expectTestingException();
	}
		
	//ok some test name
	//not ok 3 other test name
	public void testTestCaseName()	{
		mockModelUpdater.enterTestCase("some test name");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("2");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("other test name");
		mockModelUpdater.setTestStatus(Status.Failed);
		mockModelUpdater.exitTestCase();
	}
	
	//1..3
	//ok 1
	//Bail out! because I'm done with this testing
	//Ignored trailing data
	public void testBailOut()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("2");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "because I'm done with this testing");
		mockModelUpdater.setTestStatus(Status.Aborted);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("3");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "because I'm done with this testing");
		mockModelUpdater.setTestStatus(Status.Aborted);
		mockModelUpdater.exitTestCase();
	}

	//1..3
	//ok
	//1..2
	public void testAtMostOnePlan()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
		expectTestingException();
	}
	
	//1..2 # skipped for some reason
	public void testSkippedAllTestsWithReason()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "skipped for some reason");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("2");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "skipped for some reason");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
	}
	
	//output for 1 (1)
	//output for 1 (2)
	//ok
	//output for 2 (1)
	//output for 2 (2)
	//not ok second test # skipped for some reason
	//ignored output
	public void testOutput()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "output for 1 (1)");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "output for 1 (2)");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
		mockModelUpdater.enterTestCase("second test");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "skipped for some reason");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "output for 2 (1)");
		mockModelUpdater.addTestMessage(null, 0, Level.Message, "output for 2 (2)");
		mockModelUpdater.setTestStatus(Status.Skipped);
		mockModelUpdater.exitTestCase();
	}
	
	//filenameA: info: info text
	//filenameB: warning: warning text
	//filenameC:17: error: error text
	//ok
	public void testGCCDiagnosticOutput()	{
		mockModelUpdater.enterTestCase("1");
		mockModelUpdater.addTestMessage("filenameA", 0, Level.Info, "info text");
		mockModelUpdater.addTestMessage("filenameB", 0, Level.Warning, "warning text");
		mockModelUpdater.addTestMessage("filenameC", 17, Level.Error, "error text");
		mockModelUpdater.setTestStatus(Status.Passed);
		mockModelUpdater.exitTestCase();
	}
}
