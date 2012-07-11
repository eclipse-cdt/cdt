/*******************************************************************************
 * Copyright (c) 2012 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMLocation;
import org.eclipse.cdt.dsf.debug.service.IExpressions3.IExpressionDMDataExtension;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.mi.service.ClassAccessor.MIExpressionDMCAccessor;
import org.eclipse.cdt.dsf.mi.service.IGDBPatternMatchingExpressions;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterDMC;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class GDBPatternMatchingExpressionsTest extends BaseTestCase {

	private DsfSession fSession;

	private DsfServicesTracker fServicesTracker;

	protected IMIExpressions fExpService;
	protected IRegisters fRegService;

	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, "data/launch/bin/PatternMatchingExpressionsTestApp.exe");
	}

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		fSession = getGDBLaunch().getSession();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

				fExpService = fServicesTracker.getService(IMIExpressions.class);
				assertTrue(fExpService instanceof IGDBPatternMatchingExpressions);

				fRegService = fServicesTracker.getService(IRegisters.class);
			}
		};
		fSession.getExecutor().submit(runnable).get();
	}

	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();
		fExpService = null;
		fServicesTracker.dispose();
	}

	//**************************************************************************************
	// Utility methods
	//**************************************************************************************
	
	protected List<String> get_X86_REGS() {
		// Because we are dealing with expressions for the registers, we must prefix them with '$'
		List<String> list = new LinkedList<String>(Arrays.asList("$eax","$ecx","$edx","$ebx","$esp","$ebp","$esi","$edi","$eip","$eflags",
																 "$cs","$ss","$ds","$es","$fs","$gs","$st0","$st1","$st2","$st3",
																 "$st4","$st5","$st6","$st7","$fctrl","$fstat","$ftag","$fiseg","$fioff","$foseg",
																 "$fooff","$fop","$xmm0","$xmm1","$xmm2","$xmm3","$xmm4","$xmm5","$xmm6","$xmm7",
																 "$mxcsr","$orig_eax","$mm0","$mm1","$mm2","$mm3","$mm4","$mm5","$mm6","$mm7"));
		// On Windows, gdb doesn't report "orig_eax" as a register. Apparently it does on Linux
		if (Platform.getOS().equals(Platform.OS_WIN32)) {
    		list.remove("$orig_eax");
	    }
		return list;
	}
	
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
				fRegService.getRegisterGroups(threadDmc, new ImmediateDataRequestMonitor<IRegisterGroupDMContext[]>(rm) {
					@Override
					protected void handleSuccess() {
						fRegService.getRegisters(
								new CompositeDMContext(new IDMContext[] { getData()[0], threadDmc } ), 
								new ImmediateDataRequestMonitor<IRegisterDMContext[]>(rm) {
									@Override
									protected void handleSuccess() {
										assert getData() instanceof MIRegisterDMC[];
										for (MIRegisterDMC register : (MIRegisterDMC[])getData()) {
											if (register.getName().equals(regName)) {
												final FormattedValueDMContext valueDmc = fRegService.getFormattedValueContext(register, IFormattedValues.HEX_FORMAT);
												fRegService.getFormattedExpressionValue(valueDmc, new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
													@Override
													protected void handleSuccess() {
														rm.done(getData().getFormattedValue());
													};
												});
												return;
											}
										}
										// If we get here, we didn't find the register!
										assertTrue("Invalid register: " + regName, false);
									}
								});
					}
				});
			}
		};

		fSession.getExecutor().execute(query);
		return query.get(); 
	}

	protected String getExpressionValue(final IExpressionDMContext exprDmc) throws Throwable 
	{
		Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				final FormattedValueDMContext valueDmc = 
						fExpService.getFormattedValueContext(exprDmc, IFormattedValues.HEX_FORMAT);
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
	protected IExpressionDMContext[] checkChildren(final IExpressionDMContext parentDmc, final int startIndex, final int length,
			String[] expectedValues) throws Throwable {

		Query<IExpressionDMContext[]> query = new Query<IExpressionDMContext[]>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMContext[]> rm) {
				fExpService.getSubExpressions(parentDmc, startIndex, length, rm);
			}
		};

		fSession.getExecutor().execute(query);
		IExpressionDMContext[] childDmcs =  query.get(); 

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
	 * Test that we can access a single register
	 */
	@Test
	public void testMatchSingleReg() throws Throwable {
		final String regName = "esp";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		IMIExecutionDMContext threadDmc = DMContexts.getAncestorOfType(frameDmc, IMIExecutionDMContext.class);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "$"+regName);
		checkChildrenCount(exprDmc, 0);

		// get value of expression and compare with register
		assertEquals(getRegisterValue(regName, threadDmc), getExpressionValue(exprDmc));
	}

	/**
	 * Test that we can access a single variable, without using groups or patterns
	 */
	@Test
	public void testMatchSingleLocal() throws Throwable {    	
		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "secondvar");
		checkChildrenCount(exprDmc, 0);

		// get value of expression and compare with register
		assertEquals(getExpressionValue(exprDmc), "0x12");
	}

	/**
	 * Test that we can create the all-register match
	 */
	@Test
	public void testMatchAllRegs() throws Throwable {
		final String exprString = "$*";
		List<String> regList = get_X86_REGS();
		Collections.sort(regList);
		final String[] children = regList.toArray(new String[0]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that we can create the all-locals match
	 */
	@Test
	public void testMatchAllLocals() throws Throwable {
		final String exprString = "*";
		final String[] children = new String[] { "firstarg", "firstvar", "secondarg", "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

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
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "$e*";
		final String[] children = new String[] { "$eax","$ebp","$ebx","$ecx","$edi","$edx","$eflags","$eip","$es", "$esi","$esp" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be matched using '?'
	 */
	@Test
	public void testMatchRegWithQuestionMark() throws Throwable {
		final String exprString = "$e??";
		final String[] children = new String[] { "$eax","$ebp","$ebx","$ecx","$edi","$edx","$eip", "$esi","$esp" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be matched using [] with a single index
	 */
	@Test
	public void testMatchRegWithBracketsOneDigit() throws Throwable {
		final String exprString = "$st[4]";
		final String[] children = new String[] { "$st4" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that registers can be matched using '[]' using a range
	 */
	@Test
	public void testMatchRegWithBracketsRange() throws Throwable {
		final String exprString = "$st[2-5]";
		final String[] children = new String[] { "$st2","$st3", "$st4","$st5" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "$*, *";
		List<String> list = get_X86_REGS();
		Collections.sort(list);
		list.addAll(Arrays.asList(new String[] { "firstarg", "firstvar", "secondarg", "secondvar" }));
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "*, $*";
		List<String> list = get_X86_REGS();
		Collections.sort(list);
		list.addAll(0, Arrays.asList(new String[] { "firstarg", "firstvar", "secondarg", "secondvar" }));
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "$eax, $es, *";
		final String[] children = new String[] { "$es", "firstarg", "firstvar", "secondarg" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "firstvar, $*";
		List<String> list = get_X86_REGS();
		Collections.sort(list);
		list.addAll(0, Arrays.asList(new String[] { "firstvar" }));
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "$eax, $e?x, $eb?";
		final String[] children = new String[] { "$eax","$ebx","$ecx","$edx", "$ebp" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "firstvar,*,firstvar";
		final String[] children = new String[] { "firstvar", "firstarg", "secondarg", "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		List<String> regList = get_X86_REGS();
		Collections.sort(regList);
		final String[] children = regList.toArray(new String[0]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		List<String> list = Arrays.asList(new String[] { "firstarg", "firstvar", "secondarg", "secondvar" });
		Collections.sort(list);
		final String[] children = list.toArray(new String[list.size()]);		

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "$*, *";
		List<String> list = get_X86_REGS();
		Collections.sort(list);
		List<String> localsList = Arrays.asList(new String[] { "firstarg", "firstvar", "secondarg", "secondvar" });
		Collections.sort(localsList);
		list.addAll(localsList);
		final String[] children = list.toArray(new String[list.size()]);

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that group-expression can use a comma as a separator
	 */
	@Test
	public void testCommaSeparation() throws Throwable {
		final String exprString = "firstvar,$eax";
		final String[] children = new String[] { "firstvar","$eax" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that group-expression can use a semi-colon as a separator
	 */
	@Test
	public void testSemiColonSeparation() throws Throwable {
		final String exprString = "firstvar;$eax";
		final String[] children = new String[] { "firstvar","$eax" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);

	}

	/**
	 * Test that group-expression can use a comma and a semi-colon as a 
	 * separator at the same time
	 */
	@Test
	public void testCommaAndSemiColonSeparation() throws Throwable {
		final String exprString = "firstvar,$eax;$es";
		final String[] children = new String[] { "firstvar","$eax","$es" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);

	}
	
	/**
	 * Test that group-expression can have empty terms with commas.
	 */
	@Test
	public void testGroupCommaEmptyTerm() throws Throwable {
		final String exprString = ",,firstvar,,$eax,,";
		final String[] children = new String[] { "firstvar","$eax" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, exprString);

		checkChildren(exprDmc, -1, -1, children);
		checkChildrenCount(exprDmc, children.length);
	}

	/**
	 * Test that group-expression can have empty terms with semi-colon.
	 */
	@Test
	public void testGroupSemiColonEmptyTerm() throws Throwable {
		final String exprString = ";;firstvar;;$eax;;";
		final String[] children = new String[] { "firstvar","$eax" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "  firstvar  ,  $eax  , ,  ";
		final String[] children = new String[] { "firstvar","$eax" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "$eax,*";
//		final String[] children = new String[] { "$eax", "firstarg", "firstvar", "secondarg", "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		// teh same thing, to make sure we didn't forget to update one of the two.
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
		final String exprString = "$eax,*";
//		final String[] children = new String[] { "$eax", "firstarg", "firstvar", "secondarg", "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		assertEquals("", ((IExpressionDMLocation)data).getLocation());
	}

	/**
	 * Test the call to IExpressions.getSubExpressions(IExpressionDMC, DRM);
	 * which is not tested by the method checkChildren() used by our other tests
	 */
	@Test
	public void testGroupGetSubExpressions() throws Throwable {
		final String exprString = "$eax,*";
		final String[] children = new String[] { "$eax", "firstarg", "firstvar", "secondarg", "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "$eax,*";
//		final String[] children = new String[] { "$eax", "firstarg", "firstvar", "secondarg", "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String exprString = "$eax,*";
//		final String[] children = new String[] { "$eax", "firstarg", "firstvar", "secondarg", "secondvar" };

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

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
		final String noMatchExpr = "$zzz*";
		final String singleMatchExpr = "$eax,";
		final String doubleMatchExpr = "$eax,$ebx";

		SyncUtil.runToLocation("foo");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final IExpressionDMContext noMatchExprDmc = SyncUtil.createExpression(frameDmc, noMatchExpr);
		final IExpressionDMContext singleMatchExprDmc = SyncUtil.createExpression(frameDmc, singleMatchExpr);
		final IExpressionDMContext doubleMatchExprDmc = SyncUtil.createExpression(frameDmc, doubleMatchExpr);

		assertEquals("No matches", getExpressionValue(noMatchExprDmc));
		assertEquals("1 unique match", getExpressionValue(singleMatchExprDmc));
		assertEquals("2 unique matches", getExpressionValue(doubleMatchExprDmc));
	}	
}
