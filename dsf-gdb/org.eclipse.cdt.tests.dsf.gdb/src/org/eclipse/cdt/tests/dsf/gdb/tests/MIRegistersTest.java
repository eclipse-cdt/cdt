/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Alvaro Sanchez-Leon (Ericsson) - Make Registers View specific to a frame (Bug 323552)
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateCountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.IMIExecutionDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIRegisters.MIRegisterDMC;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataListRegisterNamesInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
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
					controlService.queueCommand(
							controlService.getCommandFactory().createMIDataListRegisterNames(fContainerDmc), rm);
				}
			};
			fSession.getExecutor().execute(query);

			MIDataListRegisterNamesInfo data = query.get();
			String[] names = data.getRegisterNames();

			// Remove registers with empty names since the service also
			// remove them.  I don't know why GDB returns such empty names.
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
	private IContainerDMContext fContainerDmc;
    private IRegisters fRegService;
    private IRunControl fRunControl;

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

	    		ICommandControlService commandControl = fServicesTracker.getService(ICommandControlService.class);
	    		IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
	       		IProcessDMContext procDmc = procService.createProcessContext(commandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID);
	       		fContainerDmc = procService.createContainerContext(procDmc, MIProcesses.UNIQUE_GROUP_ID);
	    		
	    		fRegService = fServicesTracker.getService(IRegisters.class);
	    		fRunControl = fServicesTracker.getService(IRunControl.class);
            }
	    };
	    fSession.getExecutor().submit(runnable).get();
	}
	
	@Override
	protected void setLaunchAttributes() {
		super.setLaunchAttributes();
		
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);
	}


	@Override
	public void doAfterTest() throws Exception {
		super.doAfterTest();
		
		fServicesTracker.dispose();
		fRegService = null;
	}

    /*
     *  This is a common support method which gets the Register Group Information 
     *  and verifies it.
     */
    private IRegisterGroupDMContext getRegisterGroup() throws Throwable {
    	final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    	
        final DataRequestMonitor<IRegisterGroupDMContext[]> regGroupDone = 
        	new DataRequestMonitor<IRegisterGroupDMContext[]>(fRegService.getExecutor(), null) {
            @Override
            protected void handleCompleted() {
                if (isSuccess()) {
                    fWait.setReturnInfo(getData());
                }
                
                fWait.waitFinished(getStatus());
            }
        };
        
        fRegService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
            	fRegService.getRegisterGroups(fContainerDmc, regGroupDone);
            }
        });
        
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(fWait.getMessage(), fWait.isOK());

        IRegisterGroupDMContext[] regGroupsDMCs = (IRegisterGroupDMContext[])fWait.getReturnInfo();
        assertTrue("There was more than one register group (" + regGroupsDMCs.length + ")", //$NON-NLS-1$
        		   regGroupsDMCs.length == 1 ); 
        fWait.waitReset();
        
        return(regGroupsDMCs[0]);
    }

    /*
     *  This is a common support method which gets the Registers names. 
     */
    
    private IRegisterDMContext[] getRegisters(final IFrameDMContext frameDmc) throws Throwable {
    	final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    	final IRegisterGroupDMContext regGroupsDMC = getRegisterGroup();

   		fRegService.getExecutor().submit(new Runnable() {
   			@Override
			public void run() {
   				fRegService.getRegisters(
   				    new CompositeDMContext(new IDMContext[] { regGroupsDMC, frameDmc} ), 
   		            new DataRequestMonitor<IRegisterDMContext[]>(fRegService.getExecutor(), null) {
   		                @Override
   		                protected void handleCompleted() {
   		                    if (isSuccess()) {
   		                        fWait.setReturnInfo(getData());
   		                    }

   		                    fWait.waitFinished(getStatus());
   		                }
   		            });
   			}
   		});

   		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);   
        assertTrue(fWait.getMessage(), fWait.isOK());
            
        IRegisterDMContext[] regContexts = (IRegisterDMContext[]) fWait.getReturnInfo();
            
        fWait.waitReset();

        assertEquals("Wrong number of registers", get_X86_REGS().size(), regContexts.length); 

        return(regContexts);
    }
    
    /*************************************************************************
     *
     * The tests for the register service.
     * 
     *************************************************************************/
    
     @Test
    public void getRegisterGroups() throws Throwable {     
    	final IRegisterGroupDMContext regGroupsDMC = getRegisterGroup();
    	
        Query<IRegisterGroupDMData> query = new Query<IRegisterGroupDMData>() {
            @Override
            protected void execute(DataRequestMonitor<IRegisterGroupDMData> rm) {
                fRegService.getRegisterGroupData(regGroupsDMC, rm);
            }
        };
        fSession.getExecutor().execute(query);
        
        IRegisterGroupDMData data = query.get();
    	
    	assertTrue("The name of the main group should be: General Registers instead of: " +
    			   data.getName(),
    			   data.getName().equals("General Registers"));    	
    }
    
    @Test
    public void getRegistersLength() throws Throwable {   
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);
    	assertEquals("Wrong number of registers", get_X86_REGS().size(), regDMCs.length);
    }
    
    @Test
    public void getRegisters() throws Throwable {
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);
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
                    fRegService.getRegisterData(
                        regDMCs[index], 
                        new ImmediateDataRequestMonitor<IRegisterDMData>(countingRm) {
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
    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
    	
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);
    	final FormattedValueDMContext valueDmc = fRegService.getFormattedValueContext(regDMCs[regNo], format);
    	
        final DataRequestMonitor<FormattedValueDMData> regRm = 
        	new DataRequestMonitor<FormattedValueDMData>(fRegService.getExecutor(), null) {
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
            	fRegService.getFormattedExpressionValue(valueDmc, regRm);
            }
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        
        FormattedValueDMData data = (FormattedValueDMData)wait.getReturnInfo();
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

    	val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.DECIMAL_FORMAT , 0);
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

    	// Run past the line that creates a thread and past the sleep that
		// follows it. This is a bit tricky because the code that creates the
		// thread is conditional depending on environment. Run to the printf
		// before it (which is common), then do step operations over the
		// non-common code (but same number of lines)
    	SyncUtil.runToLine(SRC_NAME, Integer.toString(MIRunControlTest.LINE_MAIN_PRINTF));

        // Because the program is about to go multi-threaded, we have to select the thread
        // we want to keep stepping.  If we don't, we will ask GDB to step the entire process
        // which is not what we want.  We can fetch the thread from the stopped event
        // but we should do that before the second thread is created, to be sure the stopped
        // event is for the main thread.
        MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_OVER);	// over the printf
        SyncUtil.step(stoppedEvent.getDMContext(), StepType.STEP_OVER);	// over the create-thread call
        stoppedEvent = SyncUtil.step(stoppedEvent.getDMContext(), StepType.STEP_OVER, TestsPlugin.massageTimeout(2000));	// over the one second sleep
    	
        // Get the thread IDs
    	final IContainerDMContext containerDmc = DMContexts.getAncestorOfType(stoppedEvent.getDMContext(), IContainerDMContext.class);
    	
    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        final DataRequestMonitor<IExecutionDMContext[]> drm = 
        	new DataRequestMonitor<IExecutionDMContext[]>(fRegService.getExecutor(), null) {
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

        IExecutionDMContext[] ctxts = (IExecutionDMContext[])wait.getReturnInfo();
        wait.waitReset();

		Assert.assertNotNull(ctxts);
		Assert.assertTrue(ctxts.length > 1);
		
		int tid1 = ((IMIExecutionDMContext)ctxts[0]).getThreadId();
		int tid2 = ((IMIExecutionDMContext)ctxts[1]).getThreadId();
    	
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
    	String dupliThread2RegVal5= getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 5);

    	// If Values not equal , then context haven't been re-set properly
    	assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal0.equals(dupliThread2RegVal0));
    	assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal1.equals(dupliThread2RegVal1));
    	assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal2.equals(dupliThread2RegVal2));
    	assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal3.equals(dupliThread2RegVal3));
    	assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal4.equals(dupliThread2RegVal4));
    	assertTrue("Multiple context not working. Execution Context is not reset to 2", thread2RegVal5.equals(dupliThread2RegVal5));
    	
    }
    
    private void writeRegister(IFrameDMContext frameDmc, final int regIndex, final String regValue, final String formatId) throws Throwable {
    	final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    	
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);

		fRegService.getExecutor().submit(new Runnable() {
			@Override
			public void run() {
			    fRegService.writeRegister(
	                regDMCs[regIndex], 
			        regValue, formatId, 
		            new RequestMonitor(fRegService.getExecutor(), null) {
		                @Override
		                protected void handleCompleted() {
		                    fWait.waitFinished(getStatus());
		                }
		            });

			}
		});

		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);   
		fWait.waitReset();
    }
    

    @Test
    public void writeRegisterNaturalFormat() throws Throwable{
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	String regValue = "10";
    	int regIndex = 3;
    	writeRegister(frameDmc, 3, regValue, IFormattedValues.NATURAL_FORMAT);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, regIndex);
    	assertTrue("Failed writing register. New value should have been " + regValue, regValue.equals(val));
    }
    
    @Test
    public void writeRegisterHEXFormat() throws Throwable{
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
    public void writeRegisterBinaryFormat() throws Throwable{
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	//String regValue = "0100101001";
    	String regValue = "10";
    	int regIndex = 3;
    	writeRegister(frameDmc, 3, regValue, IFormattedValues.BINARY_FORMAT);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.BINARY_FORMAT, regIndex);
    	assertTrue("Failed writing register. New value should have been " + regValue + " instead of " + val, regValue.equals(val));
    }

    @Test
    public void writeRegisterOctalFormat() throws Throwable{
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	//String regValue = "10";
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
		// Step to a multi-level stack level to be able to test different stack frames
		SyncUtil.runToLocation("PrintHello");
		MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_OVER);
		Integer depth = SyncUtil.getStackDepth(stoppedEvent.getDMContext());

		// validate expected stack depth
		assertTrue((depth == Integer.valueOf(4)));

		// Resolve the register name of the stack pointer
		String sp_name = resolveStackPointerName();

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
		get_X86_REGS();
		
		// for 64 bits
		String sp_name = "rsp";
		if (fRegisterNames.contains(sp_name)) {
			return sp_name;
		}
		
		// for 32 bits
		sp_name = "esp";
		if (fRegisterNames.contains(sp_name)) {
			return sp_name;
		}
		
		// for 16 bits
		sp_name = "sp";
		if (fRegisterNames.contains(sp_name)) {
			return sp_name;
		}
		
		return null;
	}
}
