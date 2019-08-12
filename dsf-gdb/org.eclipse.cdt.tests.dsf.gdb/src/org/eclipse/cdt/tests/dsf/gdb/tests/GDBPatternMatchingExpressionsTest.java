/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial Implementation
 *     Marc Khouzam (Ericsson) - Tests for Pattern Matching for variables (Bug 394408)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Allow user to edit register groups (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMLocation;
import org.eclipse.cdt.dsf.debug.service.IExpressions3.IExpressionDMDataExtension;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters2;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.ClassAccessor.MIExpressionDMCAccessor;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterDMC;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class GDBPatternMatchingExpressionsTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "PatternMatchingExpressionsTestApp.exe";

	private DsfSession fSession;

	private DsfServicesTracker fServicesTracker;

	protected IMIExpressions fExpService;
	protected IRegisters2 fRegService;

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
	}

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		fSession = getGDBLaunch().getSession();
		Runnable runnable = () -> {
			fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

			fExpService = fServicesTracker.getService(IMIExpressions.class);

			fRegService = fServicesTracker.getService(IRegisters2.class);
		};
		fSession.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();
		fExpService = null;
		if (fServicesTracker != null) {
			fServicesTracker.dispose();
			fServicesTracker = null;
		}
	}

	//**************************************************************************************
	// Utility methods
	//**************************************************************************************
	/**
	 * Return a new, mutable list with '$' prefixed on each register
	 */
	protected List<String> getRegistersFromGdb() throws Throwable {
		List<String> registersFromGdb = SyncUtil.getRegistersFromGdb(getGdbVersion(), SyncUtil.getContainerContext());
		return registersFromGdb.stream().map(n -> "$" + n).collect(Collectors.toCollection(LinkedList::new));
	}

	final static String[] fAllVariables = new String[] { "firstarg", "firstvar", "ptrvar", "secondarg", "secondvar",
			"var", "var2" };

	protected void checkChildrenCount(final IExpressionDMContext parentDmc, final int expectedCount) throws Throwable {
		Query<Integer> query = new Query<Integer>() {
			@Override
			protected void execute(final DataRequestMonitor<Integer> rm) {
				fExpService.getSubExpressionCount(parentDmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		int count = query.get();

		assertTrue(String.format("Expected %d but got %d", expectedCount, count), count == expectedCount);
	}

	protected String getRegisterValue(final String regName, final IMIExecutionDMContext threadDmc) throws Exception {
		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				fRegService.getRegisters(threadDmc, new ImmediateDataRequestMonitor<IRegisterDMContext[]>(rm) {
					@Override
					protected void handleSuccess() {
						assert getData() instanceof MIRegisterDMC[];
						for (MIRegisterDMC register : (MIRegisterDMC[]) getData()) {
							if (register.getName().equals(regName)) {
								final FormattedValueDMContext valueDmc = fRegService.getFormattedValueContext(register,
										IFormattedValues.HEX_FORMAT);
								fRegService.getFormattedExpressionValue(valueDmc,
										new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
											@Override
											protected void handleSuccess() {
												rm.done(getData().getFormattedValue());
											}
										});
								return;
							}
						}
						// If we get here, we didn't find the register!
						assertTrue("Invalid register: " + regName, false);
					}
				});
			}
		};

		fSession.getExecutor().execute(query);
		return query.get();
	}

	protected String getExpressionValue(final IExpressionDMContext exprDmc) throws Throwable {
		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				final FormattedValueDMContext valueDmc = fExpService.getFormattedValueContext(exprDmc,
						IFormattedValues.HEX_FORMAT);
				fExpService.getFormattedExpressionValue(valueDmc,
						new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
							@Override
							protected void handleSuccess() {
								rm.done(getData().getFormattedValue());
							}
						});
			}
		};

		fSession.getExecutor().execute(query);
		return query.get();
	}

	// This method tests IExpressions.getSubExpressions(IExpressionDMC, int, int, DRM);
	protected IExpressionDMContext[] checkChildren(final IExpressionDMContext parentDmc, final int startIndex,
			final int length, String[] expectedValues) throws Throwable {

		Query<IExpressionDMContext[]> query = new Query<IExpressionDMContext[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMContext[]> rm) {
				fExpService.getSubExpressions(parentDmc, startIndex, length, rm);
			}
		};

		fSession.getExecutor().execute(query);
		IExpressionDMContext[] childDmcs = query.get();

		String[] childExpressions = new String[childDmcs.length];
		MIExpressionDMCAccessor[] childDmcsAccessor = new MIExpressionDMCAccessor[childDmcs.length];

		// Convert to a MIExpressionDMCAccessor to be able to call getRelativeExpression
		// Also convert to String[] to be able to use Arrays.toString()
		for (int i = 0; i < childExpressions.length; i++) {
			childDmcsAccessor[i] = new MIExpressionDMCAccessor(childDmcs[i]);
			childExpressions[i] = childDmcsAccessor[i].getRelativeExpression();
		}
		assertTrue("Expected " + Arrays.toString(expectedValues) + " but got " + Arrays.toString(childExpressions),
				expectedValues.length == childExpressions.length);

		for (int i = 0; i < childDmcsAccessor.length; i++) {
			assertEquals(childDmcsAccessor[i].getRelativeExpression(), expectedValues[i]);
		}

		return childDmcs;
	}

	//**************************************************************************************
	// Tests methods
	//**************************************************************************************

	/**
	 * Test that we can access a single register, without using groups or patterns
	 */
	@Test
	public void testSingleReg() throws Throwable {
		final String regName = "cs";
		final String exprString = "$" + regName;

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildrenCount(exprDmc, 0);

		// get value of expression and compare with register
		assertEquals(getRegisterValue(regName, threadDmc), getExpressionValue(exprDmc));
	}

	/**
	 * Test that we can access a single variable, without using groups or patterns
	 */
	@Test
	public void testSingleLocal() throws Throwable {
		final String exprString = "secondvar";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildrenCount(exprDmc, 0);
		assertEquals(getExpressionValue(exprDmc), "0x12");
	}

	/**
	 * Test that we can match a single register
	 */
	@Test
	public void testMatchSingleReg() throws Throwable {
		final String exprString = "=$xmm0";
		final String[] children = new String[] { "$xmm0" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can match a single variable
	 */
	@Test
	public void testMatchSingleLocal() throws Throwable {
		final String exprString = "=secondvar";
		final String[] children = new String[] { "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can create the all-register match and that
	 * the '$*' form is treated just like '=$*'
	 */
	@Test
	public void testMatchAllRegs() throws Throwable {
		final String exprString = "$*";
		final String exprString2 = "=$*";
		List<String> regList = getRegistersFromGdb();
		Collections.sort(regList);
		final String[] children = regList.toArray(new String[0]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);

		exprDmc = SyncUtil.createExpression(frameDmc, exprString2);
		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can create the all-locals match and that
	 * the '*' form is treated just like '=*'
	 */
	@Test
	public void testMatchAllLocals() throws Throwable {
		final String exprString = "*";
		final String exprString2 = "=*";
		final String[] children = fAllVariables;

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);

		exprDmc = SyncUtil.createExpression(frameDmc, exprString2);
		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can create the all-locals match for another frame
	 */
	@Test
	public void testMatchAllLocalsOtherFrame() throws Throwable {
		final String exprString = "*";
		final String[] children = new String[] { "argc", "argv", "boolvar", "chararray", "intvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 1);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be matched using '*'
	 */
	@Test
	public void testMatchRegWithStar() throws Throwable {
		final String exprString = "=$f*";
		final String[] children;
		if (isGdbVersionAtLeast("7.12.50")) {
			// Starting in GDB 8.0 FS_BASE and GS_BASE are exposed
			// See GDB commit 2735833: amd64-linux: expose system register FS_BASE and GS_BASE for Linux.
			// Ubuntu is shipping pre-release GDB 8.0, which is versioned as 7.12.50.
			children = new String[] { "$fctrl", "$fioff", "$fiseg", "$fooff", "$fop", "$foseg", "$fs", "$fs_base",
					"$fstat", "$ftag" };
		} else {
			children = new String[] { "$fctrl", "$fioff", "$fiseg", "$fooff", "$fop", "$foseg", "$fs", "$fstat",
					"$ftag" };
		}

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be multiplied using '*'
	 * without conflicting with glob-expressions
	 */
	@Test
	public void testMultiplyReg() throws Throwable {
		final String exprString = "$fctrl*0";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildrenCount(exprDmc, 0);

		assertEquals(getExpressionValue(exprDmc), "0x0");
	}

	/**
	 * Test that variables can be matched using '*' at the start
	 * not to be confused with dereferencing
	 */
	@Test
	public void testMatchVarWithStarBefore() throws Throwable {
		final String exprString = "=*ptrvar";
		final String[] children = new String[] { "ptrvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that variables can be dereferenced using '*'
	 * without conflicting with glob-expressions
	 */
	@Test
	public void testDerefVar() throws Throwable {
		final String exprString = "*ptrvar";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildrenCount(exprDmc, 0);

		assertEquals(getExpressionValue(exprDmc), "0x12");
	}

	/**
	 * Test that variables can be matched using '*' at the end
	 * not to be confused with multiplication
	 */
	@Test
	public void testMatchVarWithStarAfter() throws Throwable {
		final String exprString = "=var*2";
		final String[] children = new String[] { "var2" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that variables can be multiplied using '*'
	 * without conflicting with glob-expressions
	 */
	@Test
	public void testMultiplyVar() throws Throwable {
		final String exprString = "var*0";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildrenCount(exprDmc, 0);

		assertEquals(getExpressionValue(exprDmc), "0x0");
	}

	/**
	 * Test that registers can be matched using '?'
	 */
	@Test
	public void testMatchRegWithQuestionMark() throws Throwable {
		final String exprString = "=$f????";
		final String[] children = new String[] { "$fctrl", "$fioff", "$fiseg", "$fooff", "$foseg", "$fstat" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that conditional operator can be used for registers
	 */
	@Test
	public void testRegWithConditionalOperator() throws Throwable {
		final String exprString = "$es?0x16:0x11";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildrenCount(exprDmc, 0);

		assertEquals(getExpressionValue(exprDmc), "0x11");
	}

	/**
	 * Test that variables can be matched using '?'
	 */
	@Test
	public void testMatchVarWithQuestionMark() throws Throwable {
		final String exprString = "=?ar?";
		final String[] children = new String[] { "var2" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that conditional operator can be used with variables
	 */
	@Test
	public void testVarWithConditionalOperator() throws Throwable {
		final String exprString = "var?0x16:0x11";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildrenCount(exprDmc, 0);

		assertEquals(getExpressionValue(exprDmc), "0x16");
	}

	/**
	 * Test that registers can be matched using [] with a single number.
	 * There should be no confusion about array index for registers.
	 */
	@Test
	public void testMatchRegWithOneDigitRange() throws Throwable {
		final String exprString = "=$st[4]";
		final String[] children = new String[] { "$st4" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be matched using [] with a single letter.
	 * There should be no confusion about array index for registers.
	 */
	@Test
	public void testMatchRegWithOneLetterRange() throws Throwable {
		final String exprString = "=$xmm[0]";
		final String[] children = new String[] { "$xmm0" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be matched using [] using a range of numbers
	 * There should be no confusion about array index for registers.
	 */
	@Test
	public void testMatchRegWithNumberRange() throws Throwable {
		final String exprString = "=$st[2-5]";
		final String[] children = new String[] { "$st2", "$st3", "$st4", "$st5" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be matched using [] using a range of letters
	 * There should be no confusion about array index for registers.
	 */
	@Test
	public void testMatchRegWithLetterRange() throws Throwable {
		final String exprString = "=$fo[a-z]";
		final String[] children = new String[] { "$fop" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be matched using [] using a range of letters
	 * and a * pattern
	 */
	@Test
	public void testMatchRegWithComplexLetterRange() throws Throwable {
		final String exprString = "=$fo[o-p]*";
		final String[] children = new String[] { "$fooff", "$fop" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that arrays can be matched using [] using a range of numbers.
	 * In this case, we want to show the user the range of array elements.
	 */
	@Test
	public void testMatchArrayWithNumberRange() throws Throwable {
		final String exprString = "=array[2-5]";
		final String[] children = new String[] { "array2", "array3", "array[2]", "array[3]", "array[4]", "array[5]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that arrays can be matched using [] using a single number range.
	 */
	@Test
	public void testMatchArrayWithSingleNumberRange() throws Throwable {
		final String exprString = "=array[2]";
		final String[] children = new String[] { "array2", "array[2]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that arrays can be matched using [] using a number range
	 * using the same number.
	 */
	@Test
	public void testMatchArrayWithSameNumberRange() throws Throwable {
		final String exprString = "=array[2-2]";
		final String[] children = new String[] { "array2", "array[2]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that arrays can be accessed using subtraction.
	 * No matching should be performed here, and the result should
	 * be the array element based on the subtraction result.
	 */
	@Test
	public void testArrayWithSubtraction() throws Throwable {
		final String exprString = "array[5-3]";

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
		checkChildrenCount(exprDmc, 0);
		assertEquals(getExpressionValue(exprDmc), "0x16");
	}

	/**
	 * Test that arrays can be matched using [] and a wildcard.
	 */
	@Test
	public void testMatchArrayWithWildCardAndNumberRange() throws Throwable {
		final String exprString = "=ar*[2-3]";
		final String[] children = new String[] { "array2", "array3", "arrayBool[2]", "arrayBool[3]", "arrayInt[2]",
				"arrayInt[3]", "array[2]", "array[3]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that arrays can be matched using [] and a ?.
	 */
	@Test
	public void testMatchArrayWithQuestionMarkAndNumberRange() throws Throwable {
		final String exprString = "=ar?a?[2-4]";
		final String[] children = new String[] { "array2", "array3", "array[2]", "array[3]", "array[4]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can match all arrays using [].
	 * In this case, we want to show the user the range of array elements.
	 */
	@Test
	public void testMatchAllArraysAndNumberRange() throws Throwable {
		final String exprString = "=*[2-3]";
		final String[] children = new String[] { "array2", "array3", "arrayBool[2]", "arrayBool[3]", "arrayInt[2]",
				"arrayInt[3]", "array[2]", "array[3]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can match arrays using [] and comma-separated indices.
	 */
	@Test
	public void testMatchArraysWithCommaSeparatedIndices() throws Throwable {
		final String exprString = "=array[2,5,8]";
		final String[] children = new String[] { "array2", "array[2]", "array[5]", "array[8]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can match arrays using [] and comma-separated ranges.
	 */
	@Test
	public void testMatchArraysWithCommaSeparatedNumberRanges() throws Throwable {
		final String exprString = "=array[2-3, 5, 7-8]";
		final String[] children = new String[] { "array2", "array3", "array[2]", "array[3]", "array[5]", "array[7]",
				"array[8]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can match arrays using [] and comma-separated ranges
	 * with an overlap.
	 */
	@Test
	public void testMatchArraysWithCommaSeparatedOverlappingRanges() throws Throwable {
		final String exprString = "=array[2-3, 5, 4-6]";
		final String[] children = new String[] { "array2", "array3", "array[2]", "array[3]", "array[4]", "array[5]",
				"array[6]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can match arrays using [] and comma-separated ranges
	 * that are not sorted.
	 */
	@Test
	public void testMatchArraysWithCommaSeparatedUnsortedRanges() throws Throwable {
		final String exprString = "=array[5-6, 3, 0-1]";
		final String[] children = new String[] { "array3", "array[0]", "array[1]", "array[3]", "array[5]", "array[6]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can match arrays using [] and comma-separated ranges
	 * containing invalid ranges.  Invalid ranges are not accepted by
	 * regular expressions and therefore will be ignored for the name.
	 * I.e., array2 and array3 will not be matches, but only array indices
	 * will be matched.
	 */
	@Test
	public void testMatchArraysWithCommaSeparatedInvalidRanges() throws Throwable {
		final String exprString = "=array[2-3, 5, 6-4]";
		final String[] children = new String[] { "array[2]", "array[3]", "array[5]", "array[6-4]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we properly handle a non-arrays when using []
	 */
	@Test
	public void testMatchNonArrayWithNumberRange() throws Throwable {
		final String exprString = "=arrayNot[2-3]";

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildrenCount(exprDmc, 0);
	}

	/**
	 * Test that arrays can be accessed using [] with a single letter range.
	 * In this case, since letters do not indicate an array index,
	 * we match a letter range within the _name_ of the array.
	 */
	@Test
	public void testMatchArrayWithSingleLetterRange() throws Throwable {
		final String exprString = "=array[B]*";
		final String[] children = new String[] { "arrayBool" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that arrays can be accessed using [] with a letter range.
	 * In this case, since letters do not indicate an array index,
	 * we match a letter range within the _name_ of the array.
	 */
	@Test
	public void testMatchArrayWithLetterRange() throws Throwable {
		final String exprString = "=array[B-I]*";
		final String[] children = new String[] { "arrayBool", "arrayInt" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that arrays can be accessed using [] with a letter range.
	 * In this case, since letters do not indicate an array index,
	 * we match a letter range within the _name_ of the array.
	 */
	@Test
	public void testMatchArrayWithLetterRange2() throws Throwable {
		final String exprString = "=ar*[B-I]*";
		final String[] children = new String[] { "arrayBool", "arrayInt" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that arrays can be accessed using [] with an invalid range.
	 * In this case, the range is used as-is to create the expression.
	 */
	@Test
	public void testMatchArrayWithInvalidNumberRange() throws Throwable {
		final String exprString = "=array[5-2]";
		final String[] children = new String[] { "array[5-2]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that all registers and all locals can be matched at the same time
	 */
	@Test
	public void testGroupAllRegsAllLocals() throws Throwable {
		final String exprString = "$*; *";
		List<String> list = getRegistersFromGdb();
		Collections.sort(list);
		list.addAll(Arrays.asList(fAllVariables));
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that all local and all registers can be matched at the same time.  This
	 * is the reverse order than the previous test.
	 */
	@Test
	public void testGroupAllLocalsAllRegs() throws Throwable {
		final String exprString = "*; $*";
		List<String> list = getRegistersFromGdb();
		Collections.sort(list);
		list.addAll(0, Arrays.asList(fAllVariables));
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can match a range
	 */
	@Test
	public void testGroupSubExprRange() throws Throwable {
		final String exprString = "$eax; $es; *";
		final String[] children = new String[] { "$es", "firstarg", "firstvar", "ptrvar" };
		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		// Check four children starting at position 1
		checkChildren(exprDmc, 1, 4, children);
	}

	/**
	 * Test that we can group a local with all registers
	 */
	@Test
	public void testGroupOneLocalAllReg() throws Throwable {
		final String exprString = "firstvar; $*";
		List<String> list = getRegistersFromGdb();
		Collections.sort(list);
		list.addAll(0, Arrays.asList(new String[] { "firstvar" }));
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can group a local with a pattern for variables
	 */
	@Test
	public void testGroupOneLocalMatchedLocals() throws Throwable {
		final String exprString = "*ptrvar; =var*";
		final String[] children = new String[] { "*ptrvar", "var", "var2" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we only return a single instance of a duplicate
	 * register when using a pattern that matches the register
	 * more than once.
	 */
	@Test
	public void testUniqueWhenOverlapReg() throws Throwable {
		final String exprString = "=$fioff; =$f?off; =$fo*";
		final String[] children = new String[] { "$fioff", "$fooff", "$fop", "$foseg" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we only return a single instance of a duplicate
	 * variable when using a pattern that matches the variable
	 * more than once.
	 */
	@Test
	public void testUniqueWhenOverlapLocal() throws Throwable {
		final String exprString = "firstvar;*;firstvar";
		final String[] children = new String[] { "firstvar", "firstarg", "ptrvar", "secondarg", "secondvar", "var",
				"var2" };
		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that the all-register pattern is sorted alphabetically
	 */
	@Test
	public void testSortedAllReg() throws Throwable {
		final String exprString = "$*";
		List<String> regList = getRegistersFromGdb();
		Collections.sort(regList);
		final String[] children = regList.toArray(new String[0]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that the all-local pattern is sorted alphabetically
	 */
	@Test
	public void testSortedAllLocals() throws Throwable {
		final String exprString = "*";
		List<String> list = Arrays.asList(fAllVariables);
		Collections.sort(list);
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that individual expressions within a group-expression
	 * are not sorted between each other, but that each part of the
	 * group is kept in the user-specified order and sorted within
	 * itself.
	 */
	@Test
	public void testSeparatlySorted() throws Throwable {
		final String exprString = "$*; *";
		List<String> list = getRegistersFromGdb();
		Collections.sort(list);
		List<String> localsList = Arrays.asList(fAllVariables);
		Collections.sort(localsList);
		list.addAll(localsList);
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that pattern-matched arrays are sorted properly by index instead
	 * of completely alphabetically.  An alphabetical sorting would cause the
	 * following poor sorting:
	 *   =a[1-11]
	 *     a[10]
	 *     a[11]
	 *     a[1]
	 *     a[2]
	 *     ...
	 */
	@Test
	public void testArraySorting() throws Throwable {
		final String exprString = "=array[1-11];=arrayInt[1-2,11,20-22]";
		final String[] children = new String[] { "array[1]", "array[2]", "array[3]", "array[4]", "array[5]", "array[6]",
				"array[7]", "array[8]", "array[9]", "array[10]", "array[11]", "arrayInt[1]", "arrayInt[2]",
				"arrayInt[11]", "arrayInt[20]", "arrayInt[21]", "arrayInt[22]" };

		SyncUtil.runToLocation("testArrayMatching");
		MIStoppedEvent stoppedEvent = SyncUtil.step(6, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);

	}

	// Cannot use comma separator because of templates (bug 393474)
	//	/**
	//	 * Test that group-expression can use a comma as a separator
	//	 */
	//	@Test
	//	public void testCommaSeparation() throws Throwable {
	//		final String exprString = "firstvar,$eax";
	//		final String[] children = new String[] { "firstvar","$eax" };
	//
	//		SyncUtil.runToLocation("foo");
	//		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);
	//
	//		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
	//
	//		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
	//
	//		checkChildren(exprDmc, -1, -1, children);
	//		checkChildrenCount(exprDmc, children.length);
	//	}

	/**
	 * Test that group-expression can use a semi-colon as a separator
	 */
	@Test
	public void testSemiColonSeparation() throws Throwable {
		final String exprString = "firstvar;$eax";
		final String[] children = new String[] { "firstvar", "$eax" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);

	}

	/**
	 * Test that a valid expression that contains a comma will not be converted
	 * to a group expression (bug 393474)
	 */
	@Test
	public void testExpressionWithCommand() throws Throwable {
		// The following expression is a valid one for a program using templates.
		// We will check that it does not get split into two expressions.
		final String exprStringComma = "((((((class std::_Vector_base<int, std::allocator<int> >) v))._M_impl))._M_start)";
		// The following expression is not valid.  However, it is identical to the previous
		// one except it uses a semi-colon.  We use it to confirm that such an expression
		// will be treated as a group-exrepssion and get split into two children.
		// This is a way to confirm that the test is valid for the above expression that
		// is separated by a command.
		final String exprStringSemiColon = exprStringComma.replace(',', ';');

		assertFalse("The two strings for this test should not be the same",
				exprStringComma.equals(exprStringSemiColon));

		MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("foo");

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmcSemiColon = SyncUtil.createExpression(frameDmc, exprStringSemiColon);
		final IExpressionDMContext exprDmcComma = SyncUtil.createExpression(frameDmc, exprStringComma);

		try {
			// This should get split into two children and not
			// sent to GDB at all, so we should not see a failure,
			// even though the expression is not valid for the
			// program we are debugging.
			checkChildrenCount(exprDmcSemiColon, 2);
		} catch (Exception e) {
			assertFalse("Expected two children for when using a semi-colon", true);
		}

		try {
			// Should throw an exception because this expression is not
			// valid and since it does not get split into children,
			// we'll be sending to GDB and seeing the failure.
			checkChildrenCount(exprDmcComma, 0);
		} catch (Exception e) {
			// Valid and expected
			return;
		}
		// Should not get here
		assertFalse("Should have seen an expression thrown", true);

	}

	// Cannot use comma separator because of templates (bug 393474)
	//	/**
	//	 * Test that group-expression can use a comma and a semi-colon as a
	//	 * separator at the same time
	//	 */
	//	@Test
	//	public void testCommaAndSemiColonSeparation() throws Throwable {
	//		final String exprString = "firstvar,$eax;$es";
	//		final String[] children = new String[] { "firstvar","$eax","$es" };
	//
	//		SyncUtil.runToLocation("foo");
	//		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);
	//
	//		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
	//
	//		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
	//
	//		checkChildren(exprDmc, -1, -1, children);
	//		checkChildrenCount(exprDmc, children.length);
	//	}

	// Cannot use comma separator because of templates (bug 393474)
	//	/**
	//	 * Test that group-expression can have empty terms with commas.
	//	 */
	//	@Test
	//	public void testGroupCommaEmptyTerm() throws Throwable {
	//		final String exprString = ",,firstvar,,$eax,,";
	//		final String[] children = new String[] { "firstvar","$eax" };
	//
	//		SyncUtil.runToLocation("foo");
	//		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);
	//
	//		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
	//
	//		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);
	//
	//		checkChildren(exprDmc, -1, -1, children);
	//		checkChildrenCount(exprDmc, children.length);
	//	}

	/**
	 * Test that group-expression can have empty terms with semi-colon.
	 */
	@Test
	public void testGroupSemiColonEmptyTerm() throws Throwable {
		final String exprString = ";;firstvar;;$eax;;";
		final String[] children = new String[] { "firstvar", "$eax" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that group-expression clean up extra spaces
	 */
	@Test
	public void testExtraSpaces() throws Throwable {
		final String exprString = "  firstvar  ;  $eax  ; ;  =var?  ;  =  second*  ";
		final String[] children = new String[] { "firstvar", "$eax", "var2", "secondarg", "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test the expression data associated with group-expressions.
	 */
	@Test
	public void testGroupExpressionData() throws Throwable {
		final String exprString = "$eax;*";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		Query<IExpressionDMData> query = new Query<IExpressionDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMData> rm) {
				fExpService.getExpressionData(exprDmc, rm);
			}
		};
		fSession.getExecutor().execute(query);
		IExpressionDMData data = query.get();

		Query<IExpressionDMDataExtension> query2 = new Query<IExpressionDMDataExtension>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMDataExtension> rm) {
				fExpService.getExpressionDataExtension(exprDmc, rm);
			}
		};

		fSession.getExecutor().execute(query2);
		IExpressionDMDataExtension dataExt = query2.get();

		// Make sure the two different ways to get the group-expression data return
		// the same thing, to make sure we didn't forget to update one of the two.
		assertEquals(data, dataExt);

		assertEquals(exprString, dataExt.getName());
		assertEquals(IExpressionDMData.BasicType.array, dataExt.getBasicType());
		assertEquals("Group-pattern", dataExt.getTypeName());
		assertTrue("IExpressionDMDataExtension.HasChildren should have been true", dataExt.hasChildren());
		assertEquals("Group-pattern", dataExt.getTypeName());
	}

	/**
	 * Test the expression address data associated with group-expressions.
	 */
	@Test
	public void testGroupExpressionAddressData() throws Throwable {
		final String exprString = "$eax;*";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		Query<IExpressionDMAddress> query = new Query<IExpressionDMAddress>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMAddress> rm) {
				fExpService.getExpressionAddressData(exprDmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		IExpressionDMAddress data = query.get();

		assertTrue("The address data shoudl be of type IExpressionDMLocation", data instanceof IExpressionDMLocation);
		assertEquals(IExpressions.IExpressionDMLocation.INVALID_ADDRESS, data.getAddress());
		assertEquals(0, data.getSize());
		assertEquals("", ((IExpressionDMLocation) data).getLocation());
	}

	/**
	 * Test the call to IExpressions.getSubExpressions(IExpressionDMC, DRM);
	 * which is not tested by the method checkChildren() used by our other tests
	 */
	@Test
	public void testGroupGetSubExpressions() throws Throwable {
		final String exprString = "$eax;*";
		List<String> list = new LinkedList<>();
		list.add("$eax");
		list.addAll(Arrays.asList(fAllVariables));
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		Query<IExpressionDMContext[]> query = new Query<IExpressionDMContext[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMContext[]> rm) {
				fExpService.getSubExpressions(exprDmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		IExpressionDMContext[] childDmcs = query.get();

		String[] childExpressions = new String[childDmcs.length];
		MIExpressionDMCAccessor[] childDmcsAccessor = new MIExpressionDMCAccessor[childDmcs.length];

		// Convert to a MIExpressionDMCAccessor to be able to call getRelativeExpression
		// Also convert to String[] to be able to use Arrays.toString()
		for (int i = 0; i < childExpressions.length; i++) {
			childDmcsAccessor[i] = new MIExpressionDMCAccessor(childDmcs[i]);
			childExpressions[i] = childDmcsAccessor[i].getRelativeExpression();
		}
		assertTrue("Expected " + Arrays.toString(children) + " but got " + Arrays.toString(childExpressions),
				children.length == childExpressions.length);

		for (int i = 0; i < childDmcsAccessor.length; i++) {
			assertEquals(childDmcsAccessor[i].getRelativeExpression(), children[i]);
		}
	}

	/**
	 * Test we cannot modify the value of a group-expression
	 */
	@Test
	public void testGroupExpressionNotModifiable() throws Throwable {
		final String exprString = "$eax;*";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		Query<Boolean> query = new Query<Boolean>() {
			@Override
			protected void execute(final DataRequestMonitor<Boolean> rm) {
				fExpService.canWriteExpression(exprDmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		boolean canWrite = query.get();

		assertFalse("Should not be able to modify the value of a group-expression", canWrite);
	}

	/**
	 * Test the only available format for the value of a group-expression is NATURAL
	 */
	@Test
	public void testGroupExpressionAvailableFormats() throws Throwable {
		final String exprString = "$eax;*";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		Query<String[]> query = new Query<String[]>() {
			@Override
			protected void execute(final DataRequestMonitor<String[]> rm) {
				fExpService.getAvailableFormats(exprDmc, rm);
			}
		};

		fSession.getExecutor().execute(query);
		String[] formats = query.get();

		assertEquals(1, formats.length);
		assertEquals(IFormattedValues.NATURAL_FORMAT, formats[0]);
	}

	/**
	 * Test the different values returned by a group-expression
	 */
	@Test
	public void testGroupExpressionValue() throws Throwable {
		final String noMatchExpr = "=$zzz*";
		final String singleMatchExpr = "=$ds;";
		final String doubleMatchExpr = "=$ds;=$es";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext noMatchExprDmc = SyncUtil.createExpression(frameDmc, noMatchExpr);
		final IExpressionDMContext singleMatchExprDmc = SyncUtil.createExpression(frameDmc, singleMatchExpr);
		final IExpressionDMContext doubleMatchExprDmc = SyncUtil.createExpression(frameDmc, doubleMatchExpr);

		assertEquals("No matches", getExpressionValue(noMatchExprDmc));
		assertEquals("1 unique match", getExpressionValue(singleMatchExprDmc));
		assertEquals("2 unique matches", getExpressionValue(doubleMatchExprDmc));
	}
}
