/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.BackgroundRunner;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BackgroundRunner.class)
public class LaunchConfigurationAndRestartTest extends BaseTestCase {

	protected static final String PROGRAM_DIR = "data/launch/bin/";
	protected static final String PROGRAM_NAME = "LaunchConfigurationAndRestartTestApp.exe";
	protected static final String PROGRAM = PROGRAM_DIR + PROGRAM_NAME;
	
	protected static final int FIRST_LINE_IN_MAIN = 27;
	protected static final int LAST_LINE_IN_MAIN = 30;

	protected DsfSession fSession;
    protected DsfServicesTracker fServicesTracker;
    protected IExpressions fExpService;
    protected IGDBControl fGdbControl;
    
    // Indicates if a restart operation should be done
    // This allows us to re-use tests for restarts tests
    protected boolean fRestart;
    
    @Override
	@Before
 	public void baseBeforeMethod() throws Exception {
    	// The class BaseTestCase sets up the launch in its @BeforeClass method.
    	// Usually this is ok, because every test uses the same launch configuration.
    	// However, for the tests of this class, we are changing the launch
    	// configuration every time.  Therefore, we need to reset it to the default
    	// before every test; that means in the @Before method instead of @BeforeClass

    	// Reset the launch configuration
    	super.baseBeforeClassMethod();
    	// Set the binary
        setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, PROGRAM);
        
    	// Can't run the launch right away because each test needs to first set some 
        // parameters.  The individual tests will be responsible for starting the launch. 
    }
    
    // This method cannot be tagged as @Before, because the launch is not
    // running yet.  We have to call this manually after all the proper
    // parameters have been set for the launch
    public void performLaunch() throws Exception {
    	// perform the launch
        super.baseBeforeMethod();
 
        fSession = getGDBLaunch().getSession();
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
            	fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
            	
            	fExpService = fServicesTracker.getService(IExpressions.class);
            	fGdbControl = fServicesTracker.getService(IGDBControl.class);
            }
        };
        fSession.getExecutor().submit(runnable).get();
        
        // Restart the program if we are testing such a case
        if (fRestart) {
        	synchronized (this) {
				wait(1000);
			}
    		fRestart = false;
        	try {
				SyncUtil.restart(getGDBLaunch());
			} catch (Throwable e) {
				fail("Restart failed: " + e.getMessage());
			}
        }
    }

    @After
    public void shutdown() throws Exception {
        if (fServicesTracker != null) fServicesTracker.dispose();
    }


    // HACK to get the full path of the program, which we need in other
    // tests.  There must be a proper eclipse way to do this!
    private static String fFullProgramPath;
	@Test
    public void getFullPath() throws Throwable {
		performLaunch();
		MIStoppedEvent stopped = getInitialStoppedEvent();
		fFullProgramPath = stopped.getFrame().getFullname();
	}

    // *********************************************************************
    // Below are the tests for the launch configuration.
    // *********************************************************************

    /**
     * This test will tell the launch to set the working directory to data/launch/src/
     * and will verify that we can find the file LaunchConfigurationAndRestartTestApp.cpp.
     * This will confirm that GDB has been properly configured with the working dir.
     */
    @Test
    public void testSettingWorkingDirectory() throws Throwable {
    	IPath path = new Path(fFullProgramPath);
    	String dir = path.removeLastSegments(4).toPortableString() + "/" + PROGRAM_DIR;
        setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, dir);
        setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, dir + PROGRAM_NAME);

       	performLaunch();
        
    	Query<MIInfo> query = new Query<MIInfo>() {
    		@Override
    		protected void execute(DataRequestMonitor<MIInfo> rm) {
    			fGdbControl.queueCommand(
    					fGdbControl.getCommandFactory().createMIFileExecFile(
    							fGdbControl.getContext(), PROGRAM_NAME),
    				    rm);
    		}
    	};
    	try {
    		fExpService.getExecutor().execute(query);
    		query.get(500, TimeUnit.MILLISECONDS);
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    }
    	
	/**
	 * This test will verify that a launch will fail if the gdbinit file
	 * does not exist and is not called ".gdbinit".
	 */
    @Test
    public void testSourceInvalidGdbInit() throws Throwable {
        setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, 
                           "gdbinitThatDoesNotExist");
        try {
        	performLaunch();
        } catch (CoreException e) {
        	// Success of the test
        	return;
        }
        
        fail("Launch seems to have succeeded even though the gdbinit file did not exist");
    }

	/**
	 * This test will verify that a launch does not fail if the gdbinit file
	 * is called ".gdbinit" and does not exist
	 */
    @Test
    public void testSourceDefaultGdbInit() throws Throwable {
        setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, 
                           ".gdbinit");
        try {
        	performLaunch();
        } catch (CoreException e) {
        	fail("Launch has failed even though the gdbinit file has the default name of .gdbinit");
        }
    }
    
    /**
     * This test will tell the launch to use data/launch/src/launchConfigTestGdbinit
     * as the gdbinit file.  We then verify the that the content was properly read.
     * launchConfigTestGdbinit will simply set some arguments for the program to read;
     * the arguments are "1 2 3 4 5 6".
     */    
    @Test
    public void testSourceGdbInit() throws Throwable {
        setLaunchAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, 
                           "data/launch/src/launchConfigTestGdbinit");
        performLaunch();
        
    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();

    	// Check that argc is correct
    	final IExpressionDMContext argcDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "argc");
    	Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
    		@Override
    		protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
    			fExpService.getFormattedExpressionValue(
    					fExpService.getFormattedValueContext(argcDmc, MIExpressions.DETAILS_FORMAT), rm);
    		}
    	};
    	try {
    		fExpService.getExecutor().execute(query);
    		FormattedValueDMData value = query.get(500, TimeUnit.MILLISECONDS);
    		
    		// Argc should be 7: the program name and the six arguments
    		assertTrue("Expected 7 but got " + value.getFormattedValue(),
    				value.getFormattedValue().trim().equals("7"));
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    	
    	// Check that argv is also correct.  For simplicity we only check the last argument
    	final IExpressionDMContext argvDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "argv[argc-1]");
    	Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
    		@Override
    		protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
    			fExpService.getFormattedExpressionValue(
    					fExpService.getFormattedValueContext(argvDmc, MIExpressions.DETAILS_FORMAT), rm);
    		}
    	};
    	try {
    		fExpService.getExecutor().execute(query2);
    		FormattedValueDMData value = query2.get(500, TimeUnit.MILLISECONDS);
    		assertTrue("Expected \"6\" but got " + value.getFormattedValue(),
    				value.getFormattedValue().trim().endsWith("\"6\""));
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    }

    /**
     * Repeat the test testSourceGdbInit, but after a restart.
     */
    @Test
    public void testSourceGdbInitRestart() throws Throwable {
    	fRestart = true;
    	testSourceGdbInit();
    }

    /**
     * This test will tell the launch to clear the environment variables.  We will
     * then check that the variable $HOME cannot be found by the program.
     */
    @Test
    public void testClearingEnvironment() throws Throwable {
        setLaunchAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
        performLaunch();
        
        SyncUtil.runToLocation("envTest");
        MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);
        
        // The program has stored the content of $HOME into a variable called 'home'.
        // Let's verify this variable is 0x0 which means $HOME does not exist.
        final IExpressionDMContext exprDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "home");
        Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(exprDmc, MIExpressions.DETAILS_FORMAT), rm);
			}
        };
        try {
        	fExpService.getExecutor().execute(query);
        	FormattedValueDMData value = query.get(500, TimeUnit.MILLISECONDS);
        	assertTrue("Expected 0x0 but got " + value.getFormattedValue(),
        			   value.getFormattedValue().equals("0x0"));
        } catch (InterruptedException e) {
        	fail(e.getMessage());
        } catch (ExecutionException e) {
        	fail(e.getCause().getMessage());
        } catch (TimeoutException e) {
        	fail(e.getMessage());
        }
    }
    
    /**
     * Repeat the test testClearingEnvironment, but after a restart.
     */
    @Test
    public void testClearingEnvironmentRestart() throws Throwable {
    	fRestart = true;
    	testClearingEnvironment();
    }

    /**
     * This test will tell the launch to set a new environment variable LAUNCHTEST.  
     * We will then check that this new variable can be read by the program.
     */
    @Test
    public void testSettingEnvironment() throws Throwable {
    	setLaunchAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);

    	Map<String, String> map = new HashMap<String, String>(1);
    	map.put("LAUNCHTEST", "IS SET");
    	setLaunchAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
    	performLaunch();

    	SyncUtil.runToLocation("envTest");
    	MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

    	// The program has stored the content of $LAUNCHTEST into a variable called 'launchTest'.
    	// Let's verify this variable is set to "IS SET".
    	final IExpressionDMContext exprDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "launchTest");
    	Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
    		@Override
    		protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
    			fExpService.getFormattedExpressionValue(
    					fExpService.getFormattedValueContext(exprDmc, MIExpressions.DETAILS_FORMAT), rm);
    		}
    	};
    	try {
    		fExpService.getExecutor().execute(query);
    		FormattedValueDMData value = query.get(500, TimeUnit.MILLISECONDS);
    		assertTrue("Expected a string ending with \"IS SET\" but got " + value.getFormattedValue(),
    				value.getFormattedValue().trim().endsWith("\"IS SET\""));
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    	
    	// Check that the normal environment is there by checking that $HOME (which is stored in 'home" exists.
        final IExpressionDMContext exprDmc2 = SyncUtil.createExpression(stoppedEvent.getDMContext(), "home");
        Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(exprDmc2, MIExpressions.DETAILS_FORMAT), rm);
			}
        };
        try {
        	fExpService.getExecutor().execute(query2);
        	FormattedValueDMData value = query2.get(500, TimeUnit.MILLISECONDS);
        	assertFalse("Expected something else than 0x0",
        			   value.getFormattedValue().equals("0x0"));
        } catch (InterruptedException e) {
        	fail(e.getMessage());
        } catch (ExecutionException e) {
        	fail(e.getCause().getMessage());
        } catch (TimeoutException e) {
        	fail(e.getMessage());
        }

    }
    
    /**
     * Repeat the test testSettingEnvironment, but after a restart.
     */
    @Test
    public void testSettingEnvironmentRestart() throws Throwable {
    	fRestart = true;
    	testSettingEnvironment();
    }

    /**
     * This test will tell the launch to clear the environment variables and then
     * set a new environment variable LAUNCHTEST.  We will then check that the variable 
     * $HOME cannot be found by the program and that the new variable LAUNCHTEST can be
     * read by the program.
     */
    @Test
    public void testClearingAndSettingEnvironment() throws Throwable {
        setLaunchAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
        
       	Map<String, String> map = new HashMap<String, String>(1);
    	map.put("LAUNCHTEST", "IS SET");
    	setLaunchAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, map);
    	performLaunch();

    	SyncUtil.runToLocation("envTest");
    	MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

    	// The program has stored the content of $LAUNCHTEST into a variable called 'launchTest'.
    	// Let's verify this variable is set to "IS SET".
    	final IExpressionDMContext exprDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "launchTest");
    	Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
    		@Override
    		protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
    			fExpService.getFormattedExpressionValue(
    					fExpService.getFormattedValueContext(exprDmc, MIExpressions.DETAILS_FORMAT), rm);
    		}
    	};
    	try {
    		fExpService.getExecutor().execute(query);
    		FormattedValueDMData value = query.get(500, TimeUnit.MILLISECONDS);
    		assertTrue("Expected a string ending with \"IS SET\" but got " + value.getFormattedValue(),
    				value.getFormattedValue().trim().endsWith("\"IS SET\""));
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    	
        // The program has stored the content of $HOME into a variable called 'home'.
        // Let's verify this variable is 0x0 which means it does not exist.
        final IExpressionDMContext exprDmc2 = SyncUtil.createExpression(stoppedEvent.getDMContext(), "home");
        Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(exprDmc2, MIExpressions.DETAILS_FORMAT), rm);
			}
        };
        try {
        	fExpService.getExecutor().execute(query2);
        	FormattedValueDMData value = query2.get(500, TimeUnit.MILLISECONDS);
        	assertTrue("Expected 0x0 but got " + value.getFormattedValue(),
        			   value.getFormattedValue().equals("0x0"));
        } catch (InterruptedException e) {
        	fail(e.getMessage());
        } catch (ExecutionException e) {
        	fail(e.getCause().getMessage());
        } catch (TimeoutException e) {
        	fail(e.getMessage());
        }
    }

    /**
     * Repeat the test testClearingAndSettingEnvironment, but after a restart.
     */
    @Test
    public void testClearingAndSettingEnvironmentRestart() throws Throwable {
    	fRestart = true;
    	testClearingAndSettingEnvironment();
    }
    
    /**
     * This test will tell the launch to set some arguments for the program.  We will
     * then check that the program has the same arguments.
     */
    @Test
    public void testSettingArguments() throws Throwable {
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "1 2 3\n4 5 6");
    	performLaunch();

    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();

    	// Check that argc is correct
    	final IExpressionDMContext argcDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "argc");
    	Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
    		@Override
    		protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
    			fExpService.getFormattedExpressionValue(
    					fExpService.getFormattedValueContext(argcDmc, MIExpressions.DETAILS_FORMAT), rm);
    		}
    	};
    	try {
    		fExpService.getExecutor().execute(query);
    		FormattedValueDMData value = query.get(500, TimeUnit.MILLISECONDS);
    		
    		// Argc should be 7: the program name and the six arguments
    		assertTrue("Expected 7 but got " + value.getFormattedValue(),
    				value.getFormattedValue().trim().equals("7"));
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    	
    	// Check that argv is also correct.  For simplicity we only check the last argument
    	final IExpressionDMContext argvDmc = SyncUtil.createExpression(stoppedEvent.getDMContext(), "argv[argc-1]");
    	Query<FormattedValueDMData> query2 = new Query<FormattedValueDMData>() {
    		@Override
    		protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
    			fExpService.getFormattedExpressionValue(
    					fExpService.getFormattedValueContext(argvDmc, MIExpressions.DETAILS_FORMAT), rm);
    		}
    	};
    	try {
    		fExpService.getExecutor().execute(query2);
    		FormattedValueDMData value = query2.get(500, TimeUnit.MILLISECONDS);
    		assertTrue("Expected \"6\" but got " + value.getFormattedValue(),
    				value.getFormattedValue().trim().endsWith("\"6\""));
    	} catch (InterruptedException e) {
    		fail(e.getMessage());
    	} catch (ExecutionException e) {
    		fail(e.getCause().getMessage());
    	} catch (TimeoutException e) {
    		fail(e.getMessage());
    	}
    }
    
    /**
     * Repeat the test testSettingArguments, but after a restart.
     */
    @Test
    public void testSettingArgumentsRestart() throws Throwable {
    	fRestart = true;
    	testSettingArguments();
    }

    /**
     * This test will tell the launch to "stop on main" at method main(), which we will verify.
     */
     @Test
    public void testStopAtMain() throws Throwable {
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
    	performLaunch();

    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
    	assertTrue("Expected to stop at main:27 but got " +
    			   stoppedEvent.getFrame().getFunction() + ":" +
    			   Integer.toString(stoppedEvent.getFrame().getLine()),
    			   stoppedEvent.getFrame().getFunction().equals("main") &&
    			   stoppedEvent.getFrame().getLine() == 27);
    }
    
    /**
     * Repeat the test testStopAtMain, but after a restart.
     */
    @Test
    public void testStopAtMainRestart() throws Throwable {
    	fRestart = true;
    	testStopAtMain();
    }
    
    /**
     * This test will tell the launch to "stop on main" at method stopAtOther(), 
     * which we will then verify.
     */
    @Test
    public void testStopAtOther() throws Throwable {
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "stopAtOther");
    	performLaunch();

    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
    	assertTrue("Expected to stop at stopAtOther but got " +
    			   stoppedEvent.getFrame().getFunction() + ":",
    			   stoppedEvent.getFrame().getFunction().equals("stopAtOther"));
    }
    
    /**
     * Repeat the test testStopAtOther, but after a restart.
     */
    @Test
    public void testStopAtOtherRestart() throws Throwable {
    	fRestart = true;
    	testStopAtOther();
    }

    
    /**
     * This test will set a breakpoint at some place in the program and will tell 
     * the launch to NOT "stop on main".  We will verify that the first stop is
     * at the breakpoint that we set.
     */
    @Ignore
    @Test
    public void testNoStopAtMain() throws Throwable {
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, false);
    	// Set this one as well to make sure it gets ignored
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN_SYMBOL, "main");
    	
    	// We need to set the breakpoint before the launch is started, but the only way to do that is
    	// to set it in the platorm.  Ok, but how do I get an IResource that points to my binary?
    	// The current workspace is the JUnit runtime workspace instead of the workspace containing
    	// the JUnit tests.
    	
    	IFile fakeFile = null;
        CDIDebugModel.createLineBreakpoint(PROGRAM, fakeFile, ICBreakpointType.REGULAR, LAST_LINE_IN_MAIN + 1, true, 0, "", true); //$NON-NLS-1$
    	performLaunch();

    	MIStoppedEvent stoppedEvent = getInitialStoppedEvent();
    	assertTrue("Expected to stop at envTest but got " +
    			   stoppedEvent.getFrame().getFunction() + ":",
    			   stoppedEvent.getFrame().getFunction().equals("envTest"));
    }
    
    /**
     * Repeat the test testNoStopAtMain, but after a restart.
     */
    @Ignore
    @Test
    public void testNoStopAtMainRestart() throws Throwable {
    	fRestart = true;
    	testNoStopAtMain();
    }


}