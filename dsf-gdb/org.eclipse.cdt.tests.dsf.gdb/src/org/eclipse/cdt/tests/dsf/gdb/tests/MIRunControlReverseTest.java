/*******************************************************************************
 * Copyright (c) 2017 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.service.IReverseRunControl;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MILogStreamOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Tests MIRunControl class for some reverse debugging scenarios.
 */
@RunWith(Parameterized.class)
public class MIRunControlReverseTest extends BaseParametrizedTestCase {

	private DsfServicesTracker fServicesTracker;
	private DsfSession fSession;
	private IGDBControl fGDBCtrl;
	private IMIRunControl fRunCtrl;
	private IExpressions fExpressions;

	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "ExpressionTestApp.exe";

	@Override
	public void doBeforeTest() throws Exception {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_0);
		super.doBeforeTest();

		fSession = getGDBLaunch().getSession();

		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
			fGDBCtrl = fServicesTracker.getService(IGDBControl.class);
			fRunCtrl = fServicesTracker.getService(IMIRunControl.class);
			fExpressions = fServicesTracker.getService(IExpressions.class);
		};
		fSession.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		if (fServicesTracker != null) {
			fServicesTracker.dispose();
		}
	}

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	/**
	 * This test verifies that we properly handle a query sent from GDB.
	 * When running the basic console (GDB < 7.12) GDB answer's the query itself
	 * and so we should not answer again.  When using the full Console, GDB does
	 * not answer so we must answer ourselves.  When using the full console
	 * we have both an MI and a CLI channel open; this test verifies how
	 * we react to getting query on the MI-only channel, which is actually
	 * not something GDB should do, but at least in 7.12, it does happen.
	 */
	@Test
	public void testQueryHandling() throws Throwable {
		SyncUtil.runToLocation("testLocals");

		assertTrue("Reverse debugging is not supported", fRunCtrl instanceof IReverseRunControl);
		final IReverseRunControl reverseService = (IReverseRunControl) fRunCtrl;

		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				reverseService.enableReverseMode(fGDBCtrl.getContext(), true, new ImmediateRequestMonitor(rm) {
					@Override
					protected void handleSuccess() {
						reverseService.isReverseModeEnabled(fGDBCtrl.getContext(), rm);
					}
				});
			}
		};

		fSession.getExecutor().execute(query);
		Boolean enabled = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertTrue("Reverse debugging should be enabled", enabled);

		// Step forward a couple of times
		SyncUtil.step(2, StepType.STEP_OVER);
		// Step back once to start using the record buffer
		MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER, true);

		IExpressionDMContext expr = SyncUtil.createExpression(stoppedEvent.getDMContext(), "lIntVar");

		// Register an event listener to check that we don't send out a 'y'
		// command when we don't need to.
		boolean yesCommandSent[] = new boolean[1];
		yesCommandSent[0] = false;
		fGDBCtrl.addEventListener(new IEventListener() {

			@Override
			public void eventReceived(Object output) {
				for (MIOOBRecord oobr : ((MIOutput) output).getMIOOBRecords()) {
					if (oobr instanceof MILogStreamOutput) {
						MILogStreamOutput stream = (MILogStreamOutput) oobr;
						if (stream.getCString().indexOf("Undefined command: \"y") != -1) {
							yesCommandSent[0] = true;
						}
					}
				}
			}
		});

		String newValue = "8989";
		// Now modify a variable to trigger the query as we are modifying a recorded value
		Query<Void> writeQuery = new Query<Void>() {
			@Override
			protected void execute(final DataRequestMonitor<Void> rm) {
				fExpressions.writeExpression(expr, newValue, IFormattedValues.DECIMAL_FORMAT, rm);
			}
		};
		fSession.getExecutor().execute(writeQuery);
		try {
			writeQuery.get(TestsPlugin.massageTimeout(2000), TimeUnit.MILLISECONDS);
		} catch (TimeoutException e) {
			assert false : "Timed-out waiting to write to a variable, probably because of a query.";
		}

		// Now verify that the write work and that GDB is answering
		String value = SyncUtil.getExpressionValue(expr, IFormattedValues.DECIMAL_FORMAT);
		assertEquals("Value was not writtent to variable", newValue, value);

		assertTrue("Sent a 'y' command unnecessarily", !yesCommandSent[0]); //$NON-NLS-1$
	}
}
