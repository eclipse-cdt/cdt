/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Alvaro Sanchez-Leon (Ericsson) - Make Registers View specific to a frame (Bug 323552)
 *     Alvaro Sanchez-Leon (Ericsson) - Allow user to edit the register groups (Bug 235747)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateCountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegistersChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IRegisters2;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.gdb.service.GDBRegisterManager;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterDMC;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class MIRegistersTest extends BaseTestCase {
	// Static list of register names as obtained directly from GDB.
	// We make it static it does not get re-set for every test
	protected static List<String> fRegisterNames = null;

	@BeforeClass
	public static void initializeGlobals() {
		// In case we run multiple GDB versions of this test
		// in the same suite, we need to re-initialize the registers
		// as they may change between GDB versions.
		fRegisterNames = null;
	}

	protected List<String> get_X86_REGS() throws Throwable {
		if (fRegisterNames == null) {
			// The tests must run on different machines, so the set of registers can change.
			// To deal with this we ask GDB for the list of registers.
			// Note that we send an MI Command in this code and do not use the IRegister service;
			// this is because we want to test the service later, comparing it to what we find
			// by asking GDB directly.
			Query<MIDataListRegisterNamesInfo> query = new Query<MIDataListRegisterNamesInfo>() {
				@Override
				protected void execute(DataRequestMonitor<MIDataListRegisterNamesInfo> rm) {
					IMICommandControl controlService = fServicesTracker.getService(IMICommandControl.class);
					IContainerDMContext containerDmc = DMContexts.getAncestorOfType(fContainerDmc, IContainerDMContext.class);
					controlService.queueCommand(controlService.getCommandFactory().createMIDataListRegisterNames(containerDmc), rm);
				}
			};
			fSession.getExecutor().execute(query);

			MIDataListRegisterNamesInfo data = query.get();
			String[] names = data.getRegisterNames();

			// Remove registers with empty names since the service also
			// remove them. I don't know why GDB returns such empty names.
			fRegisterNames = new LinkedList<String>();
			for (String name : names) {
				if (!name.isEmpty()) {
					fRegisterNames.add(name);
				}
			}
		}
		return fRegisterNames;
	}

	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";

	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	private static final String SRC_NAME = "MultiThread.cc";

	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private IDMContext fContainerDmc;
	private IRegisters2 fRegService;
	private IRunControl fRunControl;
	private IRegisterGroupDMContext fRootGroup;

	@Override
	public void doBeforeTest() throws Exception {
		super.doBeforeTest();

		fSession = getGDBLaunch().getSession();

		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// We obtain the services we need after the new
				// launch has been performed
				fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

				fRegService = (IRegisters2) fServicesTracker.getService(IRegisters.class);
				fRunControl = fServicesTracker.getService(IRunControl.class);
			}
		};
		
		fSession.getExecutor().submit(runnable).get();

		//resolve the execution context
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = null;
		try {
			frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		} catch (Throwable e1) {
			e1.printStackTrace();
			fail("Initialization problem");
		}

		//resolve the container context
		IContainerDMContext containerDmc = DMContexts.getAncestorOfType(getInitialStoppedEvent().getDMContext(), IContainerDMContext.class);
		//The container dmc is expected to contain the execution and container context
		fContainerDmc = new CompositeDMContext(new IDMContext[] { containerDmc, frameDmc });

		try {
			// Get a handle to the root group
			fRootGroup = getRegisterGroup();
			// Get Register handles from gdb
			get_X86_REGS();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();

		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);

	}
	
	
	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();

		fServicesTracker.dispose();
		fRegService = null;
	}

	/*
	 * This is a common support method which gets the Register Group Information and verifies there is only one before adding any new one
	 */
	private IRegisterGroupDMContext getRegisterGroup() throws Throwable {
		// Only one expected on this call
		IRegisterGroupDMContext[] regGroupsDMCs = getRegisterGroups(1);
		return (regGroupsDMCs[0]);
	}

	/*
	 * This is a common support method which gets the Registers names.
	 */
	private IRegisterDMContext[] getAllRegisters(final IFrameDMContext frameDmc) throws Throwable {

		Query<IRegisterDMContext[]> queryGrpRegisters = new Query<IRegisterDMContext[]>() {
			@Override
			protected void execute(DataRequestMonitor<IRegisterDMContext[]> rm) {
				fRegService.getRegisters(new CompositeDMContext(new IDMContext[] { fRootGroup, frameDmc }), rm);
			}
		};

		fRegService.getExecutor().submit(queryGrpRegisters);

		IRegisterDMContext[] regContexts = queryGrpRegisters.get(5000, TimeUnit.SECONDS);

		assertEquals("Wrong number of registers", get_X86_REGS().size(), regContexts.length);

		return regContexts;
	}

    /*
     *  Get the Registers for the specified composite context
     */
    private IRegisterDMContext[] getRegisters(final IDMContext dmc) throws Throwable {
    	
		Query<IRegisterDMContext[]> queryRegistersDmc = new Query<IRegisterDMContext[]>() {
			@Override
			protected void execute(DataRequestMonitor<IRegisterDMContext[]> rm) {
				fRegService.getRegisters(dmc, rm);
			}
		};

		fRegService.getExecutor().submit(queryRegistersDmc);           
        IRegisterDMContext[] regContexts = queryRegistersDmc.get(5000, TimeUnit.SECONDS);

        return(regContexts);
    }
	
    /*
     *  This is a common support method which gets the Register context of root group over the specified frame context 
     */
    private IRegisterDMContext[] getRegisters(final IFrameDMContext frameDmc) throws Throwable {
    	final IRegisterGroupDMContext regGroupsDMC = getRegisterGroup();
    	
    	IRegisterDMContext[] regContexts = getRegisters(new CompositeDMContext(new IDMContext[] { regGroupsDMC, frameDmc}));    	
    	assertEquals("Wrong number of registers", get_X86_REGS().size(), regContexts.length); 
    	
    	return regContexts;
    }

	/*************************************************************************
	 * 
	 * The tests for the register service.
	 * 
	 *************************************************************************/
	@Test
	public void getRegisterGroups() throws Throwable {
		final IRegisterGroupDMContext regGroupsDMC = getRegisterGroup();
		IRegisterGroupDMData data = getRegisterGroupData(regGroupsDMC);

		assertTrue("The name of the main group should be: General Registers instead of: " + data.getName(), data.getName().equals("General Registers"));
	}

	@Test
	public void getRegistersLength() throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		final IRegisterDMContext[] regDMCs = getAllRegisters(frameDmc);
		assertEquals("Wrong number of registers", get_X86_REGS().size(), regDMCs.length);
	}

	@Test
	public void getRegisters() throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		final IRegisterDMContext[] regDMCs = getAllRegisters(frameDmc);
		List<String> regNames = get_X86_REGS();

        IRegisterDMData[] datas = getRegistersData(regDMCs);
        
    	for(IRegisterDMData data: datas){
    		String regName = data.getName();
   			Assert.assertFalse("GDB does not support register name: " + regName, !regNames.contains(regName));
    	}
    }
    
    private IRegisterDMData[] getRegistersData(final IRegisterDMContext[] regDMCs) throws InterruptedException, ExecutionException {
   
		Query<IRegisterDMData[]> query = new Query<IRegisterDMData[]>() {
			@Override
			protected void execute(DataRequestMonitor<IRegisterDMData[]> rm) {
				final IRegisterDMData[] datas = new IRegisterDMData[regDMCs.length];
				rm.setData(datas);
				final CountingRequestMonitor countingRm = new ImmediateCountingRequestMonitor(rm);
				countingRm.setDoneCount(regDMCs.length);
				for (int i = 0; i < regDMCs.length; i++) {
					final int index = i;
					fRegService.getRegisterData(regDMCs[index], new ImmediateDataRequestMonitor<IRegisterDMData>(countingRm) {
						@Override
						protected void handleSuccess() {
							datas[index] = getData();
							countingRm.done();
						}
					});
				}

			}
		};

		fSession.getExecutor().execute(query);

        return query.get();
	}

	private String getModelDataForRegisterDataValue(IFrameDMContext frameDmc, String format, int regNo) throws Throwable {
		final IRegisterDMContext[] regDMCs = getAllRegisters(frameDmc);
		return getModelDataForRegisterDataValue(regDMCs[regNo], format);
	}

	private String getModelDataForRegisterDataValue(IRegisterDMContext registerDmc, String format) throws Throwable {

		final FormattedValueDMContext valueDmc = fRegService.getFormattedValueContext(registerDmc, format);

		Query<FormattedValueDMData> queryFormattedData = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fRegService.getFormattedExpressionValue(valueDmc, rm);
			}
		};

		fRegService.getExecutor().submit(queryFormattedData);

		FormattedValueDMData data = queryFormattedData.get(5000, TimeUnit.SECONDS);
		return data.getFormattedValue();
	}
	
	
	@Test
	public void getModelDataForRegisterDataValueInDifferentNumberFormats() throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 0);
		try {
			Long.parseLong(val);
		} catch (NumberFormatException e) {
			assertTrue("Register Value is not in NATURAL_FORMAT: " + val, false);
		}

		val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.HEX_FORMAT, 0);
		assertTrue("Register Value is not in HEX_FORMAT: " + val, val.startsWith("0x"));
		try {
			Long.parseLong(val.substring(2), 16);
		} catch (NumberFormatException e) {
			assertTrue("Register Value is not in HEX_FORMAT: " + val, false);
		}

		val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.BINARY_FORMAT, 0);
		try {
			Long.parseLong(val, 2);
		} catch (NumberFormatException e) {
			assertTrue("Register Value is not in BINARY_FORMAT: " + val, false);
		}

		val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.DECIMAL_FORMAT, 0);
		try {
			Long.parseLong(val);
		} catch (NumberFormatException e) {
			assertTrue("Register Value is not in DECIMAL_FORMAT: " + val, false);
		}

		val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.OCTAL_FORMAT, 0);
		assertTrue("Register Value is not in OCTAL_FORMAT: " + val, val.startsWith("0"));
		try {
			Long.parseLong(val.substring(1), 8);
		} catch (NumberFormatException e) {
			assertTrue("Register Value is not in OCTAL_FORMAT: " + val, false);
		}
	}

	@Test
	public void compareRegisterForMultipleExecutionContexts() throws Throwable {

		// Run past the line that creates a thread and past the sleep that follows it. This is a bit tricky because the code that creates the thread
		// is conditional depending on environment. Run to the printf before it (which is common), then do step operations over the non-common code
		// (but same number of lines)
		SyncUtil.runToLine(SRC_NAME, Integer.toString(MIRunControlTest.LINE_MAIN_PRINTF));

		// Because the program is about to go multi-threaded, we have to select the thread we want to keep stepping. If we don't, we will ask GDB to step
		// the entire process which is not what we want. We can fetch the thread from the stopped event but we should do that before the second thread is
		// created, to be sure the stopped event is for the main thread.
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_OVER); // over the printf
		SyncUtil.step(stoppedEvent.getDMContext(), StepType.STEP_OVER); // over the create-thread call
		stoppedEvent = SyncUtil.step(stoppedEvent.getDMContext(), StepType.STEP_OVER, TestsPlugin.massageTimeout(2000)); // over the one second sleep

		// Get the thread IDs
		final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(stoppedEvent.getDMContext(), IContainerDMContext.class);

		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
		final DataRequestMonitor<IExecutionDMContext[]> drm = new DataRequestMonitor<IExecutionDMContext[]>(fRegService.getExecutor(), null) {
			@Override
			protected void handleCompleted() {
				if (isSuccess()) {
					wait.setReturnInfo(getData());
				}
				wait.waitFinished(getStatus());
			}
		};

		fRegService.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
				fRunControl.getExecutionContexts(containerDmc, drm);
			}
		});
		wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
		Assert.assertTrue(wait.getMessage(), wait.isOK());

		IExecutionDMContext[] ctxts = (IExecutionDMContext[]) wait.getReturnInfo();
		wait.waitReset();

		Assert.assertNotNull(ctxts);
		Assert.assertTrue(ctxts.length > 1);

		int tid1 = ((IMIExecutionDMContext) ctxts[0]).getThreadId();
		int tid2 = ((IMIExecutionDMContext) ctxts[1]).getThreadId();

		// Get execution context to thread 2
		IExecutionDMContext execDmc = SyncUtil.createExecutionContext(containerDmc, tid2);
		IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(execDmc, 0);

		String thread2RegVal0 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 0);
		String thread2RegVal1 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 1);
		String thread2RegVal2 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 2);
		String thread2RegVal3 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 3);
		String thread2RegVal4 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 4);
		String thread2RegVal5 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 5);

		// Get execution context to thread 1
		execDmc = SyncUtil.createExecutionContext(containerDmc, tid1);
		IFrameDMContext frameDmc1 = SyncUtil.getStackFrame(execDmc, 0);

		getModelDataForRegisterDataValue(frameDmc1, IFormattedValues.NATURAL_FORMAT, 0);

		// Re-set the execution context to 2 and Fetch from the Cache
		String dupliThread2RegVal0 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 0);
		String dupliThread2RegVal1 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 1);
		String dupliThread2RegVal2 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 2);
		String dupliThread2RegVal3 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 3);
		String dupliThread2RegVal4 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 4);
		String dupliThread2RegVal5 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 5);

		// If Values not equal , then context haven't been re-set properly
		assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal0.equals(dupliThread2RegVal0));
		assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal1.equals(dupliThread2RegVal1));
		assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal2.equals(dupliThread2RegVal2));
		assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal3.equals(dupliThread2RegVal3));
		assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal4.equals(dupliThread2RegVal4));
		assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal5.equals(dupliThread2RegVal5));

	}

	private void writeRegister(IFrameDMContext frameDmc, final int regIndex, final String regValue, final String formatId) throws Throwable {
		final IRegisterDMContext[] regDMCs = getAllRegisters(frameDmc);
		writeRegister(regDMCs[regIndex], regValue, formatId);
	}

	private void writeRegister(final IRegisterDMContext registerDmc, final String regValue, final String formatId) throws Throwable {

		Query<Object> queryAction = new Query<Object>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				fRegService.writeRegister(registerDmc, regValue, formatId, rm);
			}
		};

		fRegService.getExecutor().submit(queryAction);
		queryAction.get(5000, TimeUnit.SECONDS);
	}
	
	/**
	 * Waits for IRegistersChangedDMEvent(s) during a time interval, collects them and returns them after timeout
	 */
	private List<IRegistersChangedDMEvent> writeRegisterWaitNotication(final IRegisterDMContext registerDmc, final String regValue, final String formatId)
	throws Throwable {
		ServiceEventWaitor<IRegistersChangedDMEvent> eventWaitor =
				new ServiceEventWaitor<IRegistersChangedDMEvent>(fSession, IRegistersChangedDMEvent.class);

		writeRegister(registerDmc, regValue, formatId);
		
		return eventWaitor.waitForEvents(TestsPlugin.massageTimeout(3000));			
	}
	
	
	@Test
	public void writeRegisterNaturalFormat() throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		String regValue = "10";
		int regIndex = 3;
		writeRegister(frameDmc, 3, regValue, IFormattedValues.NATURAL_FORMAT);
		String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, regIndex);
		assertTrue("Failed writing register. New value should have been " + regValue, regValue.equals(val));
	}

	@Test
	public void writeRegisterHEXFormat() throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		String regValue = "0x10";
		int regIndex = 3;
		writeRegister(frameDmc, 3, regValue, IFormattedValues.HEX_FORMAT);
		String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.HEX_FORMAT, regIndex);
		assertTrue("Failed writing register. New value should have been " + regValue, regValue.equals(val));
	}

	@Test
	@Ignore
	public void writeRegisterBinaryFormat() throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		// String regValue = "0100101001";
		String regValue = "10";
		int regIndex = 3;
		writeRegister(frameDmc, 3, regValue, IFormattedValues.BINARY_FORMAT);
		String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.BINARY_FORMAT, regIndex);
		assertTrue("Failed writing register. New value should have been " + regValue + " instead of " + val, regValue.equals(val));
	}

	@Test
	public void writeRegisterOctalFormat() throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		// String regValue = "10";
		String regValue = "012";
		int regIndex = 3;
		writeRegister(frameDmc, 3, regValue, IFormattedValues.OCTAL_FORMAT);
		String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.OCTAL_FORMAT, regIndex);
		assertTrue("Failed writing register. New value should have been " + regValue + "instead of " + val, regValue.equals(val));
	}

    /**
     * This test validates retrieval of different values for the same register used on different frames
     */
    @Test
	public void frameSpecificValues() throws Throwable {
		// Step to a multi-level stack to be able to test different stack frames
		SyncUtil.runToLocation("PrintHello");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_OVER);
		int depth = SyncUtil.getStackDepth(stoppedEvent.getDMContext());

		// validate expected stack depth
		assertEquals(4, depth);

		// Resolve the register name of the stack pointer
		String sp_name = resolveStackPointerName();
		assertNotNull(sp_name);

		// Get the stack pointer value for frame0
		IFrameDMContext frame0 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		IRegisterDMContext[] registers_f0 = getRegisters(frame0);
		MIRegisterDMC sp_reg_f0 = (MIRegisterDMC) findStackPointerRegister(sp_name, registers_f0);
		assertNotNull(sp_reg_f0);
		String sp_f0_str = getModelDataForRegisterDataValue(frame0, IFormattedValues.HEX_FORMAT, sp_reg_f0.getRegNo());
	
		// Get the stack pointer value for frame1
		IFrameDMContext frame1 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 1);
		IRegisterDMContext[] registers_f1 = getRegisters(frame1);
		MIRegisterDMC sp_reg_f1 = (MIRegisterDMC) findStackPointerRegister(sp_name, registers_f1);
		assertNotNull(sp_reg_f1);
		String sp_f1_str = getModelDataForRegisterDataValue(frame1, IFormattedValues.HEX_FORMAT, sp_reg_f1.getRegNo());
		
		//The stack pointer's are not expected to be the same among frames
		assertNotEquals("Stack pointers shall be different among frames", sp_f0_str, sp_f1_str);
	}

	private IRegisterDMContext findStackPointerRegister(String sp_name, IRegisterDMContext[] registerDMCs) throws InterruptedException, ExecutionException {
		IRegisterDMData[] registersData = getRegistersData(registerDMCs);
		for (int i = 0; i < registersData.length; i++) {
			IRegisterDMData registerData = registersData[i];
			
			if (registerData.getName().equals(sp_name)) {
				return registerDMCs[i];
			}
		}
		
		return null;
	}

	private String resolveStackPointerName() throws Throwable {
		List<String> regNames = get_X86_REGS();
		
		// for 64 bits
		String sp_name = "rsp";
		if (regNames.contains(sp_name)) {
			return sp_name;
		}
		
		// for 32 bits
		sp_name = "esp";
		if (regNames.contains(sp_name)) {
			return sp_name;
		}
		
		// for 16 bits
		sp_name = "sp";
		if (regNames.contains(sp_name)) {
			return sp_name;
		}
		
		return null;
	}
	public void canAddRegisterGroup() throws Throwable {
		// only root group expected
		final IRegisterGroupDMContext[] groups = getRegisterGroups(1);
		assertTrue("Unexpected groups present, only root was expected", groups.length == 1);
		assertTrue("Can not Add register groups", canAddRegisterGroup(groups[0]));
	}
	
	@Test
	public void canNotEditRootRegisterGroup() throws Throwable {
		// only root group expected
		final IRegisterGroupDMContext[] groups = getRegisterGroups(1);
		assertTrue("Unexpected groups present, only root was expected", groups.length == 1);

		assertFalse("No expected to allow the creation of a new register group", canEditRegisterGroup(groups[0]));
	}
	
	@Test
	public void canNotEditUnknownRegisterGroup() throws Throwable {
		// only root group expected
		final IRegisterGroupDMContext group = new IRegisterGroupDMContext() {

			@Override
			public String getSessionId() {
				return "session";
			}

			@Override
			public IDMContext[] getParents() {
				return new IDMContext[0];
			}

			@Override
			public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
				return null;
			}
		};
				
		assertFalse("No expected to allow editing of a non registered group", canEditRegisterGroup(group));
	}
	
	@Test
	public void canEditRegisterGroup() throws Throwable {
		// only root group expected
		final IRegisterGroupDMContext[] groups = getRegisterGroups(1);
		assertTrue("Unexpected groups present, only root was expected", groups.length == 1);
		
		IRegisterGroupDMContext group = addDefaultUserGroup();
		assertTrue("Could not add register group", canAddRegisterGroup(group));
	}
	
	/**
	 * @throws Throwable
	 */
	@Test
	public void addRegisterGroupProposedName() throws Throwable {
		// Define a subset of registers to create a register group (from, to)
		final IRegisterDMContext[] regInGroup = getRootRegisters(0, 4);

		// Adding three groups with default names
		String groupName = proposeGroupName();
		addGroup(groupName, regInGroup);
		// Resolve the new group's  sequence number from the pattern "Group_sequenceN"
		int starting_sequence = resolveGroupNameSequence(groupName);

		groupName = proposeGroupName();
		addGroup(groupName, regInGroup);

		groupName = proposeGroupName();
		String groupName2 = proposeGroupName();
		assertEquals("Proposed group name shall be the same until it's actually used", groupName, groupName2);

		addGroup(groupName, regInGroup);

		// Retrieve the existing groups, expected and validated to root + 3
		IRegisterGroupDMContext[] groups = getRegisterGroups(4);

		// The groups are returned in reversed order to present latest created
		// first i.e. index 0
		// The newest group shall be at the top then i.e. 0
		
		//Add a valid execution context to resolve the register values
		IFrameDMContext frameDmc = DMContexts.getAncestorOfType(regInGroup[0], IFrameDMContext.class);
		CompositeDMContext compositeDmc = new CompositeDMContext(new IDMContext[]{frameDmc, groups[0]});
		IRegisterDMContext[] readRegisters = getRegisters(compositeDmc);
		
		// Same order, same data and same values are expected, although different instances to different parents
		assertTrue(sameData(regInGroup, readRegisters, false));

		// Assert the last created group name
		IRegisterGroupDMData groupData = getRegisterGroupData(groups[0]);
		// 2 additional groups after creation of the base group in this test case
		assertEquals("Group_" + (starting_sequence + 2), groupData.getName());
	}

	@Test
	public void editRegisterGroup() throws Throwable {
		// Get Register Groups
		IRegisterGroupDMContext[] groups = getRegisterGroups(1);
		assertEquals("unexpected groups present", 1, groups.length);

		IRegisterGroupDMContext group = addDefaultUserGroup();
		IRegisterDMContext[] origRegisters = getRegisters(group);

		// Assert the default group name
		IRegisterGroupDMData groupData = getRegisterGroupData(group);
		assertEquals("GroupX", groupData.getName());

		// Registers to associate to exiting default group
		IRegisterDMContext[] newRegisters = getRootRegisters(5, 8);

		// A different set of registers being assigned to the group
		assertFalse((sameData(origRegisters, newRegisters, true)));

		// Modify the name and associated registers of the default group
		editGroup(group, "GroupY", newRegisters);

		groupData = getRegisterGroupData(group);
		assertEquals("GroupY", groupData.getName());

		IFrameDMContext frameDmc = DMContexts.getAncestorOfType(newRegisters[0], IFrameDMContext.class);
		
		//Read the context with a valid execution context
		CompositeDMContext compositeDmc = new CompositeDMContext(new IDMContext[]{group, frameDmc});
		IRegisterDMContext[] readRegisters = getRegisters(compositeDmc);
		
		//Same data but not from the same parent group, newRegisters from root, readRegisters from GroupY
		assertTrue(sameData(newRegisters, readRegisters, false));

	}

	@Test
	public void removeRegisterGroups() throws Throwable {
		int grpsIncrement = 3;
		addRegisterGroups(grpsIncrement);

		// Retrieve the existing groups, expected and validated to 1 root + 3
		IRegisterGroupDMContext[] groups = getRegisterGroups(1 + grpsIncrement);

		// remove one and assert the new size
		IRegisterGroupDMContext[] remGroups = new IRegisterGroupDMContext[] { groups[0] };
		removeGroups(remGroups);
		getRegisterGroups(grpsIncrement); // assert this to root + 2

		// remove two more and assert the new size
		remGroups = new IRegisterGroupDMContext[] { groups[1], groups[2] };
		removeGroups(remGroups);
		getRegisterGroups(1); // assert this to only one i.e. root

		// attempt to remove root -- Shall not be allowed
		remGroups = new IRegisterGroupDMContext[] { groups[3] };
		removeGroups(remGroups);
		getRegisterGroups(1); // assert this to only one i.e. root
	}

	/**
	 * The root group shall not be deleted i.e. ignore and preserved
	 */
	@Test
	public void removeRegisterGroupsWithRoot() throws Throwable {
		int grpsIncrement = 3;
		addRegisterGroups(grpsIncrement);

		// Retrieve the existing groups, expected and validated to 1 root + 3
		IRegisterGroupDMContext[] groups = getRegisterGroups(1 + grpsIncrement);

		// Attempt to remove all i.e. root + user defined register groups
		removeGroups(groups);
		getRegisterGroups(1); // assert root is preserved
	}

	@Test
	public void restoreRegisterGroups() throws Throwable {
		int grpsIncrement = 3;
		addRegisterGroups(grpsIncrement);

		restoreDefaultGroups();

		// assert all groups are gone except root
		getRegisterGroups(1);
	}

	@Test
	public void getRegisterNames() throws Throwable {
		int grpsIncrement = 2;
		addRegisterGroups(grpsIncrement);
		IRegisterGroupDMContext[] groups = getRegisterGroups(3); // root +
																	// increment

		IRegisterDMContext[] regDmcs1 = getRegisters(groups[0]);
		IRegisterDMContext[] regDmcs2 = getRegisters(groups[1]);

		IRegisterDMData[] regNames1 = getRegisterNames(regDmcs1);
		IRegisterDMData[] regNames2 = getRegisterNames(regDmcs2);

		// assert the register names match on both groups
		assertTrue(sameRegisterNames(regNames1, regNames2));
	}

	@Test
	public void saveAndReadRegisterGroupData() throws Throwable {
		//save groups count starting point e.g. static counter
		String groupName = proposeGroupName();
		//latest created group name = proposed name (not existing yet) -1
		int starting_sequence = resolveGroupNameSequence(groupName) -1 ;

		int grpsIncrement = 2;
		addRegisterGroups(grpsIncrement);
		getRegisterGroups(3); // root + 2

		// Resolve group sequence number
		
		//The two steps below would ideally use a shutdown and and the start of a new launch configuration within the same case.
		//However the approach below accomplishes verification of saving and reading with out over complicating the current base test case structure.
		
		//trigger groups saving
		saveRegGroups();
		
		//trigger group reading from launch configuration
		resetRegService();

		IRegisterGroupDMContext[] groups = getRegisterGroups(3); // root + 2
		// Assert the last created group name
		IRegisterGroupDMData groupData = getRegisterGroupData(groups[0]);
		// 2 additional groups after creation of the base group in this test case
		assertEquals("Group_" + (starting_sequence + grpsIncrement), groupData.getName());
	}
	
	/**
	 * All groups shall be able to write / update register values, These new value shall be propagated to any other
	 * group(s) containing this register
	 */
	@Test
	public void writeRegisterFromUserGroup() throws Throwable {
		// Define a subset of registers common to other register groups
		// indexes (from, to)
		final IRegisterDMContext[] regInRootGroup = getRootRegisters(0, 4);
		String oirigVal = getModelDataForRegisterDataValue(regInRootGroup[0], IFormattedValues.NATURAL_FORMAT);

		// Get a handle to the execution contexts, same frame execution context for all actions
		IFrameDMContext frameDmc = DMContexts.getAncestorOfType(regInRootGroup[0], IFrameDMContext.class);

		// create two user groups containing the same registers (new register instances based on root)
		String groupNameOne = proposeGroupName();
		addGroup(groupNameOne, regInRootGroup);

		String groupNameTwo = proposeGroupName();
		addGroup(groupNameTwo, regInRootGroup);

		// Retrieve the existing groups, expected and validated to root + 2
		IRegisterGroupDMContext[] groups = getRegisterGroups(3);

		// Read registers from group one
		// index 0 -> root group; index1 -> group one, index2 -> group Two
		CompositeDMContext compositeDmc = new CompositeDMContext(new IDMContext[] { frameDmc, groups[1] });
		IRegisterDMContext[] groupOneRegisters = getRegisters(compositeDmc);

		// write a register value from register group one, register index 0
		Long iWrittenVal = Long.valueOf(oirigVal) + Long.valueOf(5);
		String writtenValue = iWrittenVal.toString();
		List<IRegistersChangedDMEvent> eventNotifications = writeRegisterWaitNotication(groupOneRegisters[0],
				writtenValue, IFormattedValues.NATURAL_FORMAT);

		// Validate IRegistersChangedDMEvent event notifications, one notification per group to trigger UI refresh
		assertNotNull("No IRegistersChangedDMEvent were generated from the register value update", eventNotifications);
		assertEquals("Incorrect number of IRegistersChangedDMEvent notifications, expecting one per group", 3,
				eventNotifications.size());

		// read the register value from user group two
		compositeDmc = new CompositeDMContext(new IDMContext[] { frameDmc, groups[2] });
		IRegisterDMContext[] groupTwoRegisters = getRegisters(compositeDmc);
		String readVal = getModelDataForRegisterDataValue(groupTwoRegisters[0], IFormattedValues.NATURAL_FORMAT);

		// assert the value from group two has been updated
		assertEquals(
				"Register[0] Value read from group two does not correspond to updated value written from group one",
				writtenValue, readVal);

		// read the register value from the root register group
		readVal = getModelDataForRegisterDataValue(regInRootGroup[0], IFormattedValues.NATURAL_FORMAT);

		// assert the value from root group has also been updated
		assertEquals(
				"Register[0] Value read from root group does not correspond to updated value written from group one",
				writtenValue, readVal);

	}
		
	/**
	 * Request the existing groups and validate an expected count
	 * 
	 * @param expectedCount
	 * @return
	 * @throws Throwable
	 */
	private IRegisterGroupDMContext[] getRegisterGroups(int expectedCount) throws Throwable {

		Query<IRegisterGroupDMContext[]> queryGroupsCtx = new Query<IRegisterGroupDMContext[]>() {
			@Override
			protected void execute(DataRequestMonitor<IRegisterGroupDMContext[]> rm) {
				fRegService.getRegisterGroups(fContainerDmc, rm);
			}
		};

		fRegService.getExecutor().submit(queryGroupsCtx);

		IRegisterGroupDMContext[] regGroupsDMCs = queryGroupsCtx.get(5000, TimeUnit.SECONDS);
		assertTrue("Number of groups present (" + regGroupsDMCs.length + ")" + ", and expected (" + expectedCount + ")", //$NON-NLS-1$
				regGroupsDMCs.length == expectedCount);

		return (regGroupsDMCs);
	}

	private String proposeGroupName() throws Throwable {

		Query<String> query = new Query<String>() {
			@Override
			protected void execute(DataRequestMonitor<String> rm) {
				fRegService.proposeGroupName(rm);
			}
		};

		fRegService.getExecutor().submit(query);

		String groupName = query.get(5000, TimeUnit.SECONDS);
		assertNotNull(groupName);
		assertTrue(groupName.length() > 0);
		return groupName;
	}

	private boolean canAddRegisterGroup(final IDMContext context) throws Throwable {
		Query<Boolean> queryAction = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				fRegService.canAddRegisterGroup(context, rm);
			}
		};

		fRegService.getExecutor().submit(queryAction);
		return queryAction.get(5000, TimeUnit.SECONDS);
	}

	private boolean canEditRegisterGroup(final IRegisterGroupDMContext context) throws Throwable {
		Query<Boolean> queryAction = new Query<Boolean>() {
			@Override
			protected void execute(DataRequestMonitor<Boolean> rm) {
				fRegService.canEditRegisterGroup(context, rm);
			}
		};

		fRegService.getExecutor().submit(queryAction);
		return queryAction.get(5000, TimeUnit.SECONDS);
	}	
	
	private void addGroup(final String groupName, final IRegisterDMContext[] regIndexes) throws Throwable {

		Query<Object> queryAction = new Query<Object>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				fRegService.addRegisterGroup(groupName, regIndexes, rm);
			}
		};

		fRegService.getExecutor().submit(queryAction);
		queryAction.get(5000, TimeUnit.SECONDS);
	}

	private void editGroup(final IRegisterGroupDMContext group, final String newGroupName, final IRegisterDMContext[] regIndexes) throws Throwable {

		Query<Object> queryAction = new Query<Object>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				fRegService.editRegisterGroup(group, newGroupName, regIndexes, rm);
			}
		};

		fRegService.getExecutor().submit(queryAction);
		queryAction.get(5000, TimeUnit.SECONDS);
	}

	private void removeGroups(final IRegisterGroupDMContext[] groups) throws Throwable {

		Query<Object> queryAction = new Query<Object>() {
			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				fRegService.removeRegisterGroups(groups, rm);
			}
		};

		fRegService.getExecutor().submit(queryAction);
		queryAction.get(5000, TimeUnit.SECONDS);
	}

	private void restoreDefaultGroups() throws Throwable {
		Query<Object> queryRestore = new Query<Object>() {

			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				fRegService.restoreDefaultGroups(rm);
			}

		};

		fRegService.getExecutor().submit(queryRestore);

		queryRestore.get(5000, TimeUnit.SECONDS);
	}

	private void resetRegService() throws Throwable {
		assert(fRegService instanceof GDBRegisterManager);
		final GDBRegisterManager regManager = (GDBRegisterManager) fRegService;
		Query<Object> queryReset = new Query<Object>() {

			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				regManager.reset(rm);
			}

		};

		regManager.getExecutor().submit(queryReset);

		queryReset.get(5000, TimeUnit.SECONDS);
	}
	
	private void saveRegGroups() throws Throwable {
		assert(fRegService instanceof GDBRegisterManager);
		final GDBRegisterManager regManager = (GDBRegisterManager) fRegService;
		Query<Object> querySave = new Query<Object>() {

			@Override
			protected void execute(DataRequestMonitor<Object> rm) {
				regManager.save();
				rm.done();
			}

		};

		regManager.getExecutor().submit(querySave);

		querySave.get(5000, TimeUnit.SECONDS);
	}
	
	private IRegisterDMData[] getRegisterNames(final IRegisterDMContext[] registersDmc) throws Throwable {

		Query<IRegisterDMData[]> queryRegistersData = new Query<IRegisterDMData[]>() {
			@Override
			protected void execute(DataRequestMonitor<IRegisterDMData[]> rm) {
				fRegService.getRegisterNames(registersDmc, rm);
			}
		};

		fRegService.getExecutor().submit(queryRegistersData);
		IRegisterDMData[] regNames = queryRegistersData.get(5000, TimeUnit.SECONDS);

		return regNames;
	}

	private IRegisterDMData getRegisterData(final IRegisterDMContext registerDmc) throws Throwable {
		Query<IRegisterDMData> registerDataQ = new Query<IRegisterDMData>() {
			@Override
			protected void execute(DataRequestMonitor<IRegisterDMData> rm) {
				fRegService.getRegisterData(registerDmc, rm);
			}
		};

		fRegService.getExecutor().submit(registerDataQ);
		IRegisterDMData registerData = registerDataQ.get(5000, TimeUnit.SECONDS);
		assertNotNull(registerData);

		return registerData;
	}
	
	private FormattedValueDMData getRegisterValue(final IRegisterDMContext registerDmc) throws Throwable {
		Query<FormattedValueDMData> registerValueQ = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				FormattedValueDMContext valueDmc = new FormattedValueDMContext(fRegService, registerDmc, IFormattedValues.NATURAL_FORMAT);
				fRegService.getFormattedExpressionValue(valueDmc, rm);
			}
		};

		fRegService.getExecutor().submit(registerValueQ);
		FormattedValueDMData registerValue = registerValueQ.get(5000, TimeUnit.SECONDS);
		assertNotNull(registerValue);

		return registerValue;
	}
	
	private IRegisterGroupDMData getRegisterGroupData(final IRegisterGroupDMContext groupDmc) throws Throwable {
		Query<IRegisterGroupDMData> groupDataQ = new Query<IRegisterGroupDMData>() {
			@Override
			protected void execute(DataRequestMonitor<IRegisterGroupDMData> rm) {
				fRegService.getRegisterGroupData(groupDmc, rm);
			}
		};

		fRegService.getExecutor().submit(groupDataQ);
		IRegisterGroupDMData groupData = groupDataQ.get(5000, TimeUnit.SECONDS);
		assertNotNull(groupData);

		return groupData;
	}

	private IRegisterDMContext[] getRootRegisters(int from, int to) throws Throwable {
		MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		final IRegisterDMContext[] regDMCs = getAllRegisters(frameDmc);

		// Shall not ask for more than is available
		assertTrue(regDMCs.length > to);

		// Retrieve the register range
		final IRegisterDMContext[] regRange = Arrays.copyOfRange(regDMCs, from, to);

		return regRange;
	}

	private IRegisterGroupDMContext addDefaultUserGroup() throws Throwable {
		// Define a subset of registers to create a register group
		final IRegisterDMContext[] regInGroup = getRootRegisters(0, 4);
		// Request the addition of the new group
		addGroup("GroupX", regInGroup);

		// Retrieve the existing groups, expected and validated to 2
		IRegisterGroupDMContext[] groups = getRegisterGroups(2);

		// The groups are returned in reversed order to present latest created
		// first i.e. index 0
		// Our new group shall be at the top then i.e. 0
		IFrameDMContext frameDmc = DMContexts.getAncestorOfType(regInGroup[0], IFrameDMContext.class);
		CompositeDMContext compositeDmc = new CompositeDMContext(new IDMContext[]{groups[0], frameDmc});
		IRegisterDMContext[] readRegisters = getRegisters(compositeDmc);

		// Same objects and same order expected, although different parents
		assertTrue(sameData(regInGroup, readRegisters, false));
		return groups[0];
	}

	private IRegisterGroupDMContext[] addRegisterGroups(int numberOfNewGroups) throws Throwable {
		// Define a subset of registers to associate to the new register groups
		final IRegisterDMContext[] regInGroup = getRootRegisters(0, 4);
		for (int i = 0; i < numberOfNewGroups; i++) {
			String groupName = proposeGroupName();
			addGroup(groupName, regInGroup);
		}

		// Expected number of groups = Root + numberofNewGroups
		return getRegisterGroups(1 + numberOfNewGroups);
	}

	/**
	 * Check the register Data entry names are in the same order
	 */
	private boolean sameRegisterNames(IRegisterDMData[] regNames1, IRegisterDMData[] regNames2) {
		boolean same = false;
		if (regNames1.length == regNames2.length) {
			for (int i = 0; i < regNames1.length; i++) {
				if (regNames1[i].getName().equals(regNames2[i].getName())) {
					continue;
				} else {
					// Found a different name, Not the same !!
					return false;
				}
			}

			// All names matched !!
			return true;
		}

		return same;
	}

	private boolean sameData(IRegisterDMContext[] regArrOne, IRegisterDMContext[] regArrTwo, boolean sameParentGroup) throws Throwable {
		if (regArrOne.length != regArrTwo.length) {
			return false;
		}
		
		for (int i=0; i< regArrOne.length; i++) {
			if (sameParentGroup) {
				//same group parent expected
				final IRegisterGroupDMContext parentGroupOne = DMContexts.getAncestorOfType(regArrOne[i], IRegisterGroupDMContext.class);
				final IRegisterGroupDMContext parentGroupTwo = DMContexts.getAncestorOfType(regArrTwo[i], IRegisterGroupDMContext.class);
				if(sameParentGroup != (parentGroupOne == parentGroupTwo)) {
					return false;
				}
			}
	
			//same data
			IRegisterDMData dataOne = getRegisterData(regArrOne[i]);
			IRegisterDMData dataTwo = getRegisterData(regArrTwo[i]);
			
			if (!dataOne.getName().equals(dataTwo.getName())) {
				return false;
			}
	
			//same value
			FormattedValueDMData valueOne = getRegisterValue(regArrOne[i]);
			FormattedValueDMData valueTwo = getRegisterValue(regArrTwo[i]);
			if (!valueOne.getFormattedValue().equals(valueTwo.getFormattedValue())) {
				return false;
			}
		}
		
		//All data is the same
		return true;
	}

	/**
	 * Resolve from proposed name to group sequence number e.g Group_5 -> 5
	 */
	private int resolveGroupNameSequence(String groupName) {
		int sequence = 0;
		String[] strSequence = groupName.split("_");
		assertTrue(strSequence.length == 2);
		sequence = Integer.parseInt(strSequence[1]);
		return sequence;
	}

}
