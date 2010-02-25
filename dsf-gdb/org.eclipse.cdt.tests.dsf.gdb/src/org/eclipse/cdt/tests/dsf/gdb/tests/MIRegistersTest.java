/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;


import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.CompositeDMContext;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IRegisters;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IProcesses.IProcessDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterDMData;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.cdt.dsf.debug.service.IRegisters.IRegisterGroupDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)

public class MIRegistersTest extends BaseTestCase {

	final int NUMBER_OF_REGISTERS = 50;
	/*
	 * Path to executable
	 */
	private static final String EXEC_PATH = "data/launch/bin/";
	/*
	 * Name of the executable
	 */
	private static final String EXEC_NAME = "MultiThread.exe";
	private static final String SRC_NAME = "MultiThread.cc";

    // Will be used to wait for asynchronous call to complete
    //private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private IContainerDMContext fContainerDmc;
    private IRegisters fRegService;
    
	@Before
	public void init() throws Exception {
	    fSession = getGDBLaunch().getSession();
		// We obtain the services we need after the new
		// launch has been performed
		fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

		ICommandControlService commandControl = fServicesTracker.getService(ICommandControlService.class);
		IMIProcesses procService = fServicesTracker.getService(IMIProcesses.class);
   		IProcessDMContext procDmc = procService.createProcessContext(commandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID);
   		fContainerDmc = procService.createContainerContext(procDmc, MIProcesses.UNIQUE_GROUP_ID);

		
		fRegService = fServicesTracker.getService(IRegisters.class);
	}
	
	@BeforeClass
	public static void beforeClassMethod() {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);
	}


	@After
	public void tearDown() {
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

        assertTrue("The number of registers should have been " + NUMBER_OF_REGISTERS + 
        		   " instead of " + regContexts.length,
		           regContexts.length == NUMBER_OF_REGISTERS);

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
    	assertTrue("The number of registers should have been " + NUMBER_OF_REGISTERS +
    		     " instead of " + regDMCs.length,
		         regDMCs.length == NUMBER_OF_REGISTERS);

    }
    
    @Test
    public void getRegisters() throws Throwable {
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);
    	List<String> regNames = Arrays.asList("eax","ecx","edx","ebx","esp","ebp","esi","edi","eip","eflags","cs","ss","ds","es","fs","gs","st0","st1","st2","st3","st4","st5","st6","st7","fctrl","fstat","ftag","fiseg","fioff","foseg","fooff","fop","xmm0","xmm1","xmm2","xmm3","xmm4","xmm5","xmm6","xmm7","mxcsr","orig_eax","mm0","mm1","mm2","mm3","mm4","mm5","mm6","mm7");
    	
        Query<IRegisterDMData[]> query = new Query<IRegisterDMData[]>() {
            @Override
            protected void execute(DataRequestMonitor<IRegisterDMData[]> rm) {
                final IRegisterDMData[] datas = new IRegisterDMData[regDMCs.length];
                rm.setData(datas);
                final CountingRequestMonitor countingRm = new CountingRequestMonitor(ImmediateExecutor.getInstance(), rm);
                countingRm.setDoneCount(regDMCs.length);
                for (int i = 0; i < regDMCs.length; i++) {
                    final int index = i; 
                    fRegService.getRegisterData(
                        regDMCs[index], 
                        new DataRequestMonitor<IRegisterDMData>(ImmediateExecutor.getInstance(), countingRm) {
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
        
        IRegisterDMData[] datas = query.get();
    	
    	for(IRegisterDMData data: datas){
    		String regName = data.getName();
   			Assert.assertFalse("GDB does not support register name: " + regName, !regNames.contains(regName));
    	}
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
            public void run() {
            	fRegService.getFormattedExpressionValue(valueDmc, regRm);
            }
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        
        FormattedValueDMData data = (FormattedValueDMData)wait.getReturnInfo();
        return data.getFormattedValue();
    }


    private static String REGISTER_VALUE = "";
    @Test
    public void getModelDataForRegisterDataValueInDifferentNumberFormats() throws Throwable {
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 0);
    	REGISTER_VALUE = val;
    	assertTrue("Register Value is not in NATURAL format " , Integer.parseInt(val)== Integer.parseInt(REGISTER_VALUE));

    	val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.HEX_FORMAT, 0);
    	assertTrue("Register Value is not in HEX_FORMAT " ,val.startsWith("0x"));

    	val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.BINARY_FORMAT, 0);
    	Assert.assertEquals("Register Value is not in BINARY_FORMAT ", val, Integer.toBinaryString(Integer.parseInt(REGISTER_VALUE)));

    	val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.DECIMAL_FORMAT , 0);
    	Assert.assertEquals("Register Value is not in DECIMAL_FORMAT", Integer.parseInt(val), Integer.parseInt(REGISTER_VALUE));

    	val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.OCTAL_FORMAT, 0);
    	assertTrue("Register Value is not in OCTAL_FORMAT " ,val.startsWith("0"));
    }

    
    @Test
    public void compareRegisterForMultipleExecutionContexts() throws Throwable {
        MIStoppedEvent stoppedEvent = SyncUtil.runToLine(SRC_NAME, "22");
    	IContainerDMContext containerDmc = DMContexts.getAncestorOfType(stoppedEvent.getDMContext(), IContainerDMContext.class);

    	// Get execution context to thread 2
        IExecutionDMContext execDmc = SyncUtil.createExecutionContext(containerDmc, 2);
        IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(execDmc, 0);
        
    	String thread2RegVal0 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 0);
    	String thread2RegVal1 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 1);
    	String thread2RegVal2 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 2);
    	String thread2RegVal3 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 3);
    	String thread2RegVal4 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 4);
    	String thread2RegVal5 = getModelDataForRegisterDataValue(frameDmc2, IFormattedValues.NATURAL_FORMAT, 5);

    	// Get execution context to thread 1
        execDmc = SyncUtil.createExecutionContext(containerDmc, 2);
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
    
}
