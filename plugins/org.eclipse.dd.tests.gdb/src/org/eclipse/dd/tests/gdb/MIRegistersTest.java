package org.eclipse.dd.tests.gdb;


import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.service.IRegisters;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.dd.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterDMContext;
import org.eclipse.dd.dsf.debug.service.IRegisters.IRegisterGroupDMContext;
import org.eclipse.dd.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.dd.dsf.service.DsfServicesTracker;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.gdb.service.command.GDBControl;
import org.eclipse.dd.gdb.service.command.GDBControlDMContext;
import org.eclipse.dd.mi.service.IMIExecutionDMContext;
import org.eclipse.dd.mi.service.MIRunControl;
import org.eclipse.dd.mi.service.MIStack;
import org.eclipse.dd.mi.service.command.events.MIStoppedEvent;
import org.eclipse.dd.tests.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.dd.tests.gdb.framework.BackgroundRunner;
import org.eclipse.dd.tests.gdb.framework.BaseTestCase;
import org.eclipse.dd.tests.gdb.framework.SyncUtil;
import org.eclipse.dd.tests.gdb.launching.TestsPlugin;
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
	
    // Will be used to wait for asynchronous call to complete
    //private final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
	private DsfSession fSession;
	private DsfServicesTracker fServicesTracker;
	private GDBControlDMContext fGdbControlDmc;
    private IRegisters fRegService;
    private MIRunControl fRunControl;
    private MIStack fStack;
    
	@Before
	public void init() throws Exception {
	    fSession = getGDBLaunch().getSession();
		// We obtain the services we need after the new
		// launch has been performed
		fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());

		GDBControl gdbControl = fServicesTracker.getService(GDBControl.class);
		fGdbControlDmc = gdbControl.getGDBDMContext();
		
		fRegService = fServicesTracker.getService(IRegisters.class);
		fRunControl = fServicesTracker.getService(MIRunControl.class);
		fStack = fServicesTracker.getService(MIStack.class);
		
// This is the way to have the entire application run		
//		final IDMContext<IExecutionDMData> execDMContext = ((MIRunControl)fRunControl).getExecutionDMC();
//	   	fRunControl.getExecutor().submit(new Runnable() {
//   			public void run() {
//   				fRunControl.resume(execDMContext, null);
//   			}
//   		});

	}
	
	@BeforeClass
	public static void beforeClassMethod() {
		setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, 
				           EXEC_PATH + EXEC_NAME);
	}


	@After
	public void tearDown() {
		fRegService = null;
		fRunControl = null;
		fStack = null;
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
            	fRegService.getRegisterGroups(fGdbControlDmc, regGroupDone);
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

    	final DataRequestMonitor<IRegisterDMContext[]> regDone = 
    		new DataRequestMonitor<IRegisterDMContext[]>(fRegService.getExecutor(), null) {
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
//   				fRegService.getRegisters(regGroupsDMC, frameDmc, regDone);
   				fWait.waitFinished(
   						new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
						"Commented out some code until it can compile", null));
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
    	
    	assertTrue("The name of the main group should be: General Registers instead of: " +
    			   regGroupsDMC.getName(),
    			   regGroupsDMC.getName().equals("General Registers"));    	
    }
    
    @Test
    public void getRegistersLength() throws Throwable {   
    	IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
    	IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);
    	assertTrue("The number of registers should have been " + NUMBER_OF_REGISTERS +
    		     " instead of " + regDMCs.length,
		         regDMCs.length == NUMBER_OF_REGISTERS);

    }
    
    @Test
    public void getRegisters() throws Throwable {
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);
    	List<String> regNames = Arrays.asList("eax","ecx","edx","ebx","esp","ebp","esi","edi","eip","eflags","cs","ss","ds","es","fs","gs","st0","st1","st2","st3","st4","st5","st6","st7","fctrl","fstat","ftag","fiseg","fioff","foseg","fooff","fop","xmm0","xmm1","xmm2","xmm3","xmm4","xmm5","xmm6","xmm7","mxcsr","orig_eax","mm0","mm1","mm2","mm3","mm4","mm5","mm6","mm7");
    	for(IRegisterDMContext reg: regDMCs){
    		String regName = reg.getName();
   			Assert.assertFalse("GDB does not support register name: " + regName, !regNames.contains(regName));
    	}
    }
    
    //private static String REGISTER_VALUE = "16";
    private String getModelDataForRegisterDataValue(IFrameDMContext frameDmc, String format, int regNo) throws Throwable {
    	final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    	
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);
    	final FormattedValueDMContext valueDmc = fRegService.getFormattedValueContext(regDMCs[regNo], format);
    	
        final DataRequestMonitor<FormattedValueDMData> regRm = 
        	new DataRequestMonitor<FormattedValueDMData>(fRegService.getExecutor(), null) {
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
            	fRegService.getFormattedExpressionValue(valueDmc, regRm);
            }
        });
        
        fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(fWait.getMessage(), fWait.isOK());

        
        FormattedValueDMData data = (FormattedValueDMData)fWait.getReturnInfo();
        String val = data.getFormattedValue();
        fWait.waitReset();
        return val;
    }


    private static String REGISTER_VALUE = "";
    @Test
    public void getModelDataForRegisterDataValueNatural() throws Throwable {
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 0);
    	REGISTER_VALUE = val;
    	assertTrue("Register Value is not in NATURAL format " , Integer.parseInt(val)== Integer.parseInt(REGISTER_VALUE));
    }

    @Test
    public void getModelDataForRegisterDataValueHex() throws Throwable {
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.HEX_FORMAT, 0);
    	assertTrue("Register Value is not in HEX_FORMAT " ,val.startsWith("0x"));
    }

    @Test
    public void getModelDataForRegisterDataValueBinary() throws Throwable {
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);

    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.BINARY_FORMAT, 0);
    	assertTrue("Register Value is not in BINARY_FORMAT " , val.equals(Integer.toBinaryString(Integer.parseInt(REGISTER_VALUE))));
    }

    @Test
    public void getModelDataForRegisterDataValueDecimal() throws Throwable {
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);

    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.DECIMAL_FORMAT , 0);
    	assertTrue("Register Value is not in DECIMAL_FORMAT" ,Integer.parseInt(val) == Integer.parseInt(REGISTER_VALUE));
    }

    @Test
    public void getModelDataForRegisterDataValueOctal() throws Throwable {
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.OCTAL_FORMAT, 0);
    	assertTrue("Register Value is not in OCTAL_FORMAT " ,val.startsWith("0"));
    }

    
    @Test
    public void compareRegisterForMultipleExecutionContexts() throws Throwable {
    	final AsyncCompletionWaitor fWait = new AsyncCompletionWaitor();
    	
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);

    	String regVal0 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 0);
    	String regVal1 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 1);
    	String regVal2 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 2);
    	String regVal3 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 3);
    	String regVal4 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 4);
    	String regVal5 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 5);

    	MIStoppedEvent stoppedEvent = SyncUtil.SyncRunToLine(EXEC_NAME + ".cc", "22");
        execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 2);
    	frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	String thread2RegVal0 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 0);
    	String thread2RegVal1 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 1);
    	String thread2RegVal2 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 2);
    	String thread2RegVal3 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 3);
    	String thread2RegVal4 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 4);
    	String thread2RegVal5 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 5);

    	// Set execution context to 1
        execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 0);

    	// Re-set the execution context to 2 and Fetch from the Cache
        execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 2);
        frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	String dupliThread2RegVal0 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 0);
    	String dupliThread2RegVal1 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 1);
    	String dupliThread2RegVal2 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 2);
    	String dupliThread2RegVal3 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 3);
    	String dupliThread2RegVal4 = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 4);
    	String dupliThread2RegVal5= getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, 5);

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
    	
//    	final MIRegisterGroupDMC grpDmc = new MIRegisterGroupDMC( (MIRegisters)fRegService , 0 , "General Registers" ) ;
    	final IRegisterDMContext[] regDMCs = getRegisters(frameDmc);
    	
 	
		final RequestMonitor writeDone = 
			new RequestMonitor(fRegService.getExecutor(), null) {
				@Override
				protected void handleCompleted() {
					fWait.waitFinished(getStatus());
				}
			};

		fRegService.getExecutor().submit(new Runnable() {
			public void run() {
//				fRegService.writeRegister(grpDmc, regDMCs[regIndex], regValue, 
//						formatId, writeDone);
   				fWait.waitFinished(
   						new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
						"Commented out some code until it can compile", null));

			}
		});

		fWait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);   
		fWait.waitReset();
    }
    

    @Test
    public void writeRegisterNaturalFormat() throws Throwable{
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	String regValue = "10";
    	int regIndex = 3;
    	writeRegister(frameDmc, 3, regValue, IFormattedValues.NATURAL_FORMAT);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.NATURAL_FORMAT, regIndex);
    	assertTrue("Failed writing register. New value should have been " + regValue, regValue.equals(val));
    }
    
    @Test
    public void writeRegisterHEXFormat() throws Throwable{
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	String regValue = "0x10";
    	int regIndex = 3;
    	writeRegister(frameDmc, 3, regValue, IFormattedValues.HEX_FORMAT);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.HEX_FORMAT, regIndex);
    	assertTrue("Failed writing register. New value should have been " + regValue, regValue.equals(val));
    }
    
    @Test
    @Ignore
    public void writeRegisterBinaryFormat() throws Throwable{
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	//String regValue = "0100101001";
    	String regValue = "10";
    	int regIndex = 3;
    	writeRegister(frameDmc, 3, regValue, IFormattedValues.BINARY_FORMAT);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.BINARY_FORMAT, regIndex);
    	assertTrue("Failed writing register. New value should have been " + regValue + " instead of " + val, regValue.equals(val));
    }

    @Test
    public void writeRegisterOctalFormat() throws Throwable{
        IMIExecutionDMContext execDmc = fRunControl.createMIExecutionContext(fGdbControlDmc, 1);
        IFrameDMContext frameDmc = SyncUtil.SyncGetStackFrame(execDmc, 0);
    	//String regValue = "10";
    	String regValue = "012";
    	int regIndex = 3;
    	writeRegister(frameDmc, 3, regValue, IFormattedValues.OCTAL_FORMAT);
    	String val = getModelDataForRegisterDataValue(frameDmc, IFormattedValues.OCTAL_FORMAT, regIndex);
    	assertTrue("Failed writing register. New value should have been " + regValue + "instead of " + val, regValue.equals(val));
    }
    
}
