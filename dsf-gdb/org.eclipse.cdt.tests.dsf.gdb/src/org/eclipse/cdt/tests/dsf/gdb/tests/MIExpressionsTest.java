/*******************************************************************************
 * Copyright (c) 2007, 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson			  - Initial Implementation
 *     Simon Marchi (Ericsson) - Move some tests from AsyncCompletionWaitor to Query
 *******************************************************************************/
package org.eclipse.cdt.tests.dsf.gdb.tests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateDataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionChangedDMEvent;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMAddress;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMData;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IIndexedPartitionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions2;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.CastInfo;
import org.eclipse.cdt.dsf.debug.service.IExpressions2.ICastedExpressionDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions3.IExpressionDMDataExtension;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMContext;
import org.eclipse.cdt.dsf.debug.service.IFormattedValues.FormattedValueDMData;
import org.eclipse.cdt.dsf.debug.service.IRunControl.StepType;
import org.eclipse.cdt.dsf.debug.service.IStack.IFrameDMContext;
import org.eclipse.cdt.dsf.debug.service.IStack.IVariableDMData;
import org.eclipse.cdt.dsf.mi.service.ClassAccessor.MIExpressionDMCAccessor;
import org.eclipse.cdt.dsf.mi.service.MIExpressions;
import org.eclipse.cdt.dsf.mi.service.MIExpressions.MIExpressionDMC;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.tests.dsf.gdb.framework.AsyncCompletionWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.BaseParametrizedTestCase;
import org.eclipse.cdt.tests.dsf.gdb.framework.ServiceEventWaitor;
import org.eclipse.cdt.tests.dsf.gdb.framework.SyncUtil;
import org.eclipse.cdt.tests.dsf.gdb.launching.TestsPlugin;
import org.eclipse.cdt.utils.Addr32;
import org.eclipse.cdt.utils.Addr64;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class MIExpressionsTest extends BaseParametrizedTestCase {
	private static final String EXEC_NAME = "ExpressionTestApp.exe";
	private static final String SOURCE_NAME = "ExpressionTestApp.cc";

    private DsfSession fSession;

    private DsfServicesTracker fServicesTracker;

    protected IExpressions fExpService;

    private int fExprChangedEventCount = 0;

    private IExpressionDMContext fExprChangedCtx = null;
    
    private IExpressionDMContext globalExpressionCtx1 = null;
    private IExpressionDMContext globalExpressionCtx2 = null;

    @Override
    protected void setLaunchAttributes() {
    	super.setLaunchAttributes();
    	    	
    	setLaunchAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, EXEC_PATH + EXEC_NAME);
    }

	/* Line tags in the source file. */
	private static final String[] LINE_TAGS = new String[] {
		"testUpdateOfPointer_1",
		"testUpdateOfPointer_2",
		"testUpdateOfPointerTypedef_1",
		"testUpdateOfPointerTypedef_2",
	};

    @Override
    public void doBeforeTest() throws Exception {
    	super.doBeforeTest();

		/* Resolve line tags in source file. */
		resolveLineTagLocations(SOURCE_NAME, LINE_TAGS);

    	fSession = getGDBLaunch().getSession();
        Runnable runnable = new Runnable() {
            @Override
			public void run() {
           	fServicesTracker = new DsfServicesTracker(TestsPlugin.getBundleContext(), fSession.getId());
            	
            	fExpService = fServicesTracker.getService(IExpressions.class);
            	fSession.addServiceEventListener(MIExpressionsTest.this, null);
            	clearExprChangedData();
            }
        };
        fSession.getExecutor().submit(runnable).get();
    }

    @Override
    public void doAfterTest() throws Exception {
    	super.doAfterTest();
		if (fSession != null) {
			fSession.getExecutor().submit(()->fSession.removeServiceEventListener(MIExpressionsTest.this)).get();
		}
		fExpService = null;
		if (fServicesTracker != null) {    	
			fServicesTracker.dispose();
		}
    }

    // Handles ExpressionChangedEvent
    @DsfServiceEventHandler
    public void eventDispatched(IExpressionChangedDMEvent e) {
        fExprChangedEventCount++;
        fExprChangedCtx = e.getDMContext();
    }

    // Clears the counters
    private void clearExprChangedData() {
        fExprChangedEventCount = 0;
        fExprChangedCtx = null;
    }

    // Returns the total number of events received
    private int getExprChangedCount() {
        return fExprChangedEventCount;
    }

    private IExpressionDMContext getExprChangedContext() {
        return fExprChangedCtx;
    }

    // *********************************************************************
    // Below are the tests for the expression service.
    // *********************************************************************

    /**
     * Test that we can correctly evaluate integer expressions.
     */
    @Test
    public void testLiteralIntegerExpressions() throws Throwable {
        MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testLocals");

        // Create a map of expressions and their expected values.
        Map<String, String[]> tests = new HashMap<String, String[]>();

        tests.put("0 + 0 - 0", new String[] { "0x0", "0", "0", "0", "0", "0" });
        tests.put("3 + 4", new String[] { "0x7", "07", "111", "7", "7", "7" });
        tests.put("3 + 4 * 5", new String[] { "0x17", "027", "10111", "23", "23", "23" });
        tests.put("5 * 3 + 4", new String[] { "0x13", "023", "10011", "19", "19", "19" });
        tests.put("5 * (3 + 4)", new String[] { "0x23", "043", "100011", "35", "35", "35" });
        tests.put("10 - 15", new String[] { "0xFFFFFFFB", "037777777773", "11111111111111111111111111111011", "-5",
            "-5", "-5" });
        tests.put("10 + -15", new String[] { "0xFFFFFFFB", "037777777773", "11111111111111111111111111111011", "-5",
            "-5", "-5" });

        executeExpressionSubTests(tests, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0));
    }

    /**
     * Test that we can correctly evaluate floating-point expressions.
     */
    @Test
    public void testLiteralFloatingPointExpressions() throws Throwable {
        MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testLocals");

        // Create a map of expressions and their expected values.
        Map<String, String[]> tests = new HashMap<String, String[]>();

        tests.put("3.14159 + 1.1111", new String[] { "0x4", "04", "100", "4", "4.2526", "4.2526" });
        tests.put("100.0 / 3.0", new String[] { "0x21", "041", "100001", "33", "33.3333", "33.3333" });
        tests.put("-100.0 / 3.0", new String[] { "0xffffffffffffffdf", "01777777777777777777737",
            "1111111111111111111111111111111111111111111111111111111111011111", "-33", "-33.3333", "-33.3333" });
        tests.put("-100.0 / -3.0", new String[] { "0x21", "041", "100001", "33", "33.3333", "33.3333" });
        executeExpressionSubTests(tests, false, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0));

        tests.clear();
        tests.put("100.0 / 0.5", new String[] { "0xc8", "0310", "11001000", "200", "200", "200" });
        executeExpressionSubTests(tests, true, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0));        

    }

    /**
     * Test that we can correctly evaluate C expressions involving local
     * variables.
     */
    @Test
    public void testLocalVariables() throws Throwable {
        // Run to the point where all local variables are initialized
        SyncUtil.runToLocation("testLocals");
        MIStoppedEvent stoppedEvent = SyncUtil.step(16, StepType.STEP_OVER);

        // Create a map of expressions to expected values.
        Map<String, String[]> tests1 = new HashMap<String, String[]>();

        tests1.put("lIntVar", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345", "12345" });
        tests1.put("lDoubleVar", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345.123449999999", "12345.123449999999" });
        tests1.put("lCharVar", new String[] { "0x6d", "0155", "1101101", "109", "109 'm'", "109 'm'" });
        tests1.put("lBoolVar", new String[] { "0x0", "0", "0", "0", "false", "false" });

        tests1.put("lIntArray[1]", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345", "12345" });
        tests1.put("lDoubleArray[1]",  new String[] { "0x3039", "030071", "11000000111001", "12345", "12345.123449999999", "12345.123449999999" });
        tests1.put("lCharArray[1]", new String[] { "0x6d", "0155", "1101101", "109", "109 'm'", "109 'm'" });
        tests1.put("lBoolArray[1]", new String[] { "0x0", "0", "0", "0", "false", "false" });

        tests1.put("*lIntPtr", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345", "12345" });
        tests1.put("*lDoublePtr", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345.123449999999", "12345.123449999999" });
        tests1.put("*lCharPtr", new String[] { "0x6d", "0155", "1101101", "109", "109 'm'", "109 'm'" });
        tests1.put("*lBoolPtr", new String[] { "0x0", "0", "0", "0", "false", "false" });

        tests1.put("lIntPtr2", new String[] { "0x1", "01", "1", "1", "0x1", "0x1" });
        tests1.put("lDoublePtr2", new String[] { "0x2345", "021505", "10001101000101", "9029", "0x2345", "0x2345" });
        // GDB says a char* is out of bounds, but not the other pointers???
        // tests1.put("CharPtr2", new String[] { "0x1234", "011064",
        // "1001000110100", "4660", "0x1234" });
        tests1.put("lBoolPtr2", new String[] { "0x123ABCDE", "02216536336", "10010001110101011110011011110", "305839326", "0x123ABCDE", "0x123ABCDE" });

        executeExpressionSubTests(tests1, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0));

        // Step into the method and stop until all new local variables are
        // initialized
        SyncUtil.step(StepType.STEP_INTO);
        stoppedEvent = SyncUtil.step(5, StepType.STEP_OVER);

        // Create a map of expressions to expected values.
        Map<String, String[]> tests2 = new HashMap<String, String[]>();

        tests2.put("lIntVar", new String[] { "0x1a85", "015205", "1101010000101", "6789", "6789", "6789" });
        tests2.put("lDoubleArray[1]",
            new String[] { "0x1a85", "015205", "1101010000101", "6789", "6789.6788999999999", "6789.6788999999999" });
        tests2.put("lCharVar", new String[] { "0x69", "0151", "1101001", "105", "105 'i'", "105 'i'" });
        tests2.put("*lCharPtr", new String[] { "0x69", "0151", "1101001", "105", "105 'i'", "105 'i'" });
        tests2.put("lBoolPtr2", new String[] { "0xABCDE123", "025363360443", "10101011110011011110000100100011",
            "2882396451", "0xABCDE123","0xABCDE123" });

        // check variables at current stack frame
        executeExpressionSubTests(tests2, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0));
        // check previous stack frame
        executeExpressionSubTests(tests1, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 1));

        // Now return from the method and check that we see the
        // original variables.  We must use the right context to restore the right stack frame
        stoppedEvent = SyncUtil.step(stoppedEvent.getDMContext(), StepType.STEP_RETURN);

        executeExpressionSubTests(tests1, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0));
    }

    /**
     * This tests verifies that we can deal with variables in a subblock hiding
     * variables with the same name in the outer block.
     */
    @Ignore("Sublocks do not work with GDB")
    @Test
    public void testSubBlock() throws Throwable {
        SyncUtil.runToLocation("testSubblock");
        MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        Map<String, String[]> tests = new HashMap<String, String[]>();

        tests.put("a", new String[] { "0x8", "010", "1000", "8", "8", "8" });
        tests.put("b", new String[] { "0x1", "01", "1", "1", "1", "1" });

        executeExpressionSubTests(tests, frameDmc);

        // Now enter a subblock with the same variable names
        SyncUtil.step(2, StepType.STEP_OVER);

        tests = new HashMap<String, String[]>();

        tests.put("a", new String[] { "0xc", "014", "1100", "12", "12", "12" });
        tests.put("b", new String[] { "0x1", "01", "1", "1", "1", "1" });

        executeExpressionSubTests(tests, frameDmc);

        // Now step to change the b variable
        SyncUtil.step(1, StepType.STEP_OVER);

        tests = new HashMap<String, String[]>();

        tests.put("a", new String[] { "0xc", "014", "1100", "12", "12", "12" });
        tests.put("b", new String[] { "0xc", "014", "1100", "12", "12", "12" });

        executeExpressionSubTests(tests, frameDmc);

        // Now exit the sub-block and check that we see the original a but the
        // same b
        SyncUtil.step(1, StepType.STEP_OVER);

        tests = new HashMap<String, String[]>();

        tests.put("a", new String[] { "0x8", "010", "1000", "8", "8", "8" });
        tests.put("b", new String[] { "0xc", "014", "1100", "12", "12", "12" });

        executeExpressionSubTests(tests, frameDmc);
    }

    /**
     * This tests verifies that we can obtain children properly.
     */
    @Test
    public void testChildren() throws Throwable {
      	assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_6_7);

    	// Get the children of some variables
        MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testChildren");
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        IExpressionDMContext exprDMC = SyncUtil.createExpression(frameDmc, "f");
        doTestChildren(exprDMC);
        
        // Now do a step and get the children again, to test the internal cache
        SyncUtil.step(1, StepType.STEP_OVER);
        doTestChildren(exprDMC);
    }
    
    /**
     * This test makes sure we get the right number of children.
     */
    @Test
    public void testChildrenCount() throws Throwable {
        // Next we test that we can retrieve children count while reading the
        // value and vice-versa

        SyncUtil.runToLocation("testChildren");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

    	// First we get the expected value of the array pointer.
        final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "f");

        assertChildrenCount(exprDmc, 5);
    }

	/**
	 * This test makes sure we get can tell if an expression has children based
	 * on the expression data.
	 */
	@Test
	public void testHasChildrenInExpressionData() throws Throwable {
		SyncUtil.runToLocation("testChildren");
		MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

		final IFrameDMContext frameDmc = SyncUtil.getStackFrame(
				stoppedEvent.getDMContext(), 0);

		// First we get the expected value of the array pointer.
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(
				frameDmc, "f");

		Query<IExpressionDMData> query = new Query<IExpressionDMData>() {
			@Override
			protected void execute(DataRequestMonitor<IExpressionDMData> rm) {
				fExpService.getExpressionData(exprDmc, rm);
			}
		};

		fExpService.getExecutor().submit(query);

		IExpressionDMData data = query.get();
		IExpressionDMDataExtension dataExtension = (IExpressionDMDataExtension) data;

		assertThat("expression has children", dataExtension.hasChildren());

	}

    /**
     * This test makes sure we properly deal with a GDB display bug.
     * See bug 320277
     * 
     * The following code causes a bug in GDB:
     * 
     * class Base {};
     * class BaseTest: public Base {
     *   public:
     *     BaseTest() {} // Removing this lines removes GDB's bug
     *     void test() { return; }
     * };
     * 
     * We see the bug with the following commands:
     * -var-create - * this
     * -var-list-children var1
     * -var-info-path-expression var1.BaseTest
     * -data-evaluate-expression "(*(Base*) this)"
     * 
     * which we can reproduce by creating the children of this
     * and asking for the DETAILS_FORMAT of the var1.BaseTest child.
     */
	@Test
	public void testBaseChildrenBug() throws Throwable {

		MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("BaseTest::test");

		final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// First we get 'this' and its children
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "this");
		final IExpressionDMContext[] children = getChildren(exprDmc, new String[] { "Base", "Base" });

		Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				FormattedValueDMContext dmc = fExpService.getFormattedValueContext(children[0], MIExpressions.DETAILS_FORMAT);
				fExpService.getFormattedExpressionValue(dmc, rm);
			}
		};

		fExpService.getExecutor().submit(query);

		query.get();

		// This second child is testing the fact that we could have the child named
		// the same as its type and we still want to be able to get the details without error.
		query = new Query<FormattedValueDMData>() {
			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				FormattedValueDMContext dmc = fExpService.getFormattedValueContext(children[1], MIExpressions.DETAILS_FORMAT);
				fExpService.getFormattedExpressionValue(dmc, rm);
			}
		};

		fExpService.getExecutor().submit(query);

		query.get();
	}

	/**
	 * This test makes sure we properly deal with a GDB display bug and nested
	 * children. See bug 320277.
	 */
	@Test
	public void testNestedBaseChildrenBug() throws Throwable {

		MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("BaseTest::test");

		final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// First we get 'this' and its children
		final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "this");
		IExpressionDMContext[] children1 = getChildren(exprDmc, new String[] { "Base", "Base" });
		final IExpressionDMContext[] children = getChildren(children1[0], new String[] { "nested", "pNested" });
		final IExpressionDMContext[] childOfPointer = getChildren(children[1], new String[] { "*pNested" });

		Query<FormattedValueDMData> query = new Query<FormattedValueDMData>() {

			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				FormattedValueDMContext dmc = fExpService.getFormattedValueContext(children[0],
						MIExpressions.DETAILS_FORMAT);
				fExpService.getFormattedExpressionValue(dmc, rm);
			}
		};

		fExpService.getExecutor().submit(query);
		query.get();

		query = new Query<FormattedValueDMData>() {

			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				FormattedValueDMContext dmc = fExpService.getFormattedValueContext(children[1],
						MIExpressions.DETAILS_FORMAT);
				fExpService.getFormattedExpressionValue(dmc, rm);
			}
		};

		fExpService.getExecutor().submit(query);
		query.get();

		query = new Query<FormattedValueDMData>() {

			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				FormattedValueDMContext dmc = fExpService.getFormattedValueContext(childOfPointer[0],
						MIExpressions.DETAILS_FORMAT);
				fExpService.getFormattedExpressionValue(dmc, rm);
			}
		};

		fExpService.getExecutor().submit(query);
		query.get();
	}

	/**
	 * This test verifies that the ExpressionService can write to a variable.
	 */
	@Test
	public void testWriteVariable() throws Throwable {
		SyncUtil.runToLocation("testWrite");
		MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

		final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
		IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "a[1]");

		writeAndCheck(exprDmc, "987", IFormattedValues.DECIMAL_FORMAT, "987");
		writeAndCheck(exprDmc, "16", IFormattedValues.HEX_FORMAT, "22");
		writeAndCheck(exprDmc, "0x2e", IFormattedValues.HEX_FORMAT, "46");
		writeAndCheck(exprDmc, "16", IFormattedValues.OCTAL_FORMAT, "14");
		writeAndCheck(exprDmc, "022", IFormattedValues.OCTAL_FORMAT, "18");
		writeAndCheck(exprDmc, "1011", IFormattedValues.BINARY_FORMAT, "11");
		writeAndCheck(exprDmc, "0b1001", IFormattedValues.BINARY_FORMAT, "9");
		writeAndCheck(exprDmc, "456", IFormattedValues.NATURAL_FORMAT, "456");

		exprDmc = SyncUtil.createExpression(frameDmc, "ptr");

		writeAndCheck(exprDmc, "0x10", IFormattedValues.HEX_FORMAT, "16");
	}

	/*
	 * This method does a write and then a read to make sure the new value was
	 * properly written.
	 */
	private void writeAndCheck(final IExpressionDMContext exprDmc, final String newValueFormatted, final String format,
			final String newValueInDecimal) throws Throwable {
		ServiceEventWaitor<IExpressionChangedDMEvent> eventWaitor = new ServiceEventWaitor<>(
				fSession, IExpressionChangedDMEvent.class);
		// Write the new value using its formatted value
		Query<Void> writeQuery = new Query<Void>() {

			@Override
			protected void execute(DataRequestMonitor<Void> rm) {
				fExpService.writeExpression(exprDmc, newValueFormatted, format, rm);
			}
		};

		fExpService.getExecutor().submit(writeQuery);
		writeQuery.get();

		IExpressionChangedDMEvent event = eventWaitor.waitForEvent(TestsPlugin.massageTimeout(1000));
		assertThat(event.getDMContext(), is(exprDmc));

		// Read the new value in decimal and check that it is what we expected
		Query<FormattedValueDMData> readQuery = new Query<FormattedValueDMData>() {

			@Override
			protected void execute(DataRequestMonitor<FormattedValueDMData> rm) {
				FormattedValueDMContext dmc = fExpService.getFormattedValueContext(exprDmc,
						IFormattedValues.DECIMAL_FORMAT);
				fExpService.getFormattedExpressionValue(dmc, rm);
			}
		};

		fExpService.getExecutor().submit(readQuery);
		String actualDecimalValue = readQuery.get().getFormattedValue();

		assertThat(actualDecimalValue.toLowerCase(), is(newValueInDecimal.toLowerCase()));
	}

    /**
     * This tests verifies that we handle invalid formats properly for a write.
     */
    @Test
    public void testWriteErrorFormat() throws Throwable {
        SyncUtil.runToLocation("testWrite");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "a[1]");

        writeAndCheckError(exprDmc, "goodbye", IFormattedValues.DECIMAL_FORMAT);
        writeAndCheckError(exprDmc, "abggg", IFormattedValues.HEX_FORMAT);
        writeAndCheckError(exprDmc, "99", IFormattedValues.OCTAL_FORMAT);
        writeAndCheckError(exprDmc, "234", IFormattedValues.BINARY_FORMAT);
        writeAndCheckError(exprDmc, "hello", IFormattedValues.NATURAL_FORMAT);
        writeAndCheckError(exprDmc, "1", "ThisFormatDoesNotExist");

        IExpressionDMContext notWritableExprDmc = SyncUtil.createExpression(frameDmc, "10+5");
        writeAndCheckError(notWritableExprDmc, "1", IFormattedValues.NATURAL_FORMAT);
    }

    /*
     * This method does a write that should use an invalid value or format, and
     * verifies that the operation fails
     */
    private void writeAndCheckError(final IExpressionDMContext exprDmc, final String invalidValueFormatted,
        final String format) throws Throwable {

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        // Write the new value using its formatted value
        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fExpService.writeExpression(exprDmc, invalidValueFormatted, format, new RequestMonitor(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        wait.waitFinished(getStatus());
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue("Got an OK status for an error test case.  Should not be able to write value "
            + invalidValueFormatted + " in " + format, !wait.isOK());

        assertTrue("ExprChangedEvent problem: expected 0, received " + getExprChangedCount(),
            getExprChangedCount() == 0);
    }

    /**
     * This test tries multiple format reads during the same executor cycle, to
     * make sure the internal MI commands are sequenced properly.
     */
    @Test
    public void testConcurrentReads() throws Throwable {
        // Next we test that we can read the value more than once
        // of the same variable object at the exact same time

        SyncUtil.runToLocation("testConcurrent");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);


        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                IExpressionDMContext exprDmc = fExpService.createExpression(frameDmc, "a[0]");
                
                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("28")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating natural format", null));
                            }
                        }
                    }
                });

                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.HEX_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equalsIgnoreCase("0x1c")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating hex format", null));
                            }
                        }
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        assertTrue("ExprChangedEvent problem: expected 0, received " + getExprChangedCount(),
            getExprChangedCount() == 0);

    }

    /**
     * This test tries reads and listChildren during the same executor cycle, to
     * make sure the internal MI commands are sequenced properly.
     */
    @Test
    public void testConcurrentReadChildren() throws Throwable {
        // Next we test that we can retrieve children while reading the value
        // and vice-versa

        SyncUtil.runToLocation("testConcurrent");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

    	// First we get the expected value of the array pointer.
        final IExpressionDMContext addrDmc = SyncUtil.createExpression(frameDmc, "&a");

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(addrDmc, IFormattedValues.NATURAL_FORMAT), 
                		new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                			@Override
                			protected void handleCompleted() {
                				if (isSuccess()) {
                					wait.setReturnInfo(getData());
                				}

                				wait.waitFinished(getStatus());
                			}
                		});
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        final String actualAddrStr = ((FormattedValueDMData) wait.getReturnInfo()).getFormattedValue();

        wait.waitReset();

        // Now perform the test
        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                IExpressionDMContext exprDmc = fExpService.createExpression(frameDmc, "a");
                
                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals(actualAddrStr)) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating natural format", null));
                            }
                        }
                    }
                });

                wait.increment();
                fExpService.getSubExpressions(exprDmc, new DataRequestMonitor<IExpressionDMContext[]>(
                    fExpService.getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            IExpressionDMContext[] children = getData();
                            int failedIndex = -1;
                            for (int i = 0; i < 2; i++) {
                                if (!children[i].getExpression().equals("a[" + i + "]")) {
                                    failedIndex = i;
                                }
                            }

                            if (failedIndex != -1) {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed getting child number: " + failedIndex, null));
                            } else {
                                wait.waitFinished();
                            }
                        }
                    }
                });

                // Use different format to avoid triggering the cache
                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.HEX_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals(actualAddrStr)) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating hex format", null));
                            }
                        }
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        assertTrue("ExprChangedEvent problem: expected 0, received " + getExprChangedCount(),
            getExprChangedCount() == 0);
    }

    /**
     * This test tries reads and getChildrenCount during the same executor
     * cycle, to make sure the internal MI commands are sequenced properly.
     */
    @Test
    public void testConcurrentReadChildrenCount() throws Throwable {
        // Next we test that we can retrieve children count while reading the
        // value and vice-versa

        SyncUtil.runToLocation("testConcurrent");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        
    	// First we get the expected value of the array pointer.
        final IExpressionDMContext addrDmc = SyncUtil.createExpression(frameDmc, "&a");

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(addrDmc, IFormattedValues.NATURAL_FORMAT), 
                		new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                			@Override
                			protected void handleCompleted() {
                				if (isSuccess()) {
                					wait.setReturnInfo(getData());
                				}

                				wait.waitFinished(getStatus());
                			}
                		});
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        final String actualAddrStr = ((FormattedValueDMData) wait.getReturnInfo()).getFormattedValue();

        wait.waitReset();

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                wait.increment();
                IExpressionDMContext exprDmc = fExpService.createExpression(frameDmc, "a");

                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals(actualAddrStr)) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating natural format", null));
                            }
                        }
                    }
                });

                wait.increment();
                fExpService.getSubExpressionCount(exprDmc, new DataRequestMonitor<Integer>(fExpService.getExecutor(),
                    null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            int count = getData();
                            if (count != 2) {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed getting count for children.  Got" + count + "instead of 2", null));
                            } else {
                                wait.waitFinished();
                            }
                        }
                    }
                });

                // Use different format to avoid triggering the cache
                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.HEX_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals(actualAddrStr)) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating hex format", null));
                            }
                        }
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        assertTrue("ExprChangedEvent problem: expected 0, received " + getExprChangedCount(),
            getExprChangedCount() == 0);
    }

    /**
     * This test tries reads and writes during the same executor cycle, to make
     * sure the internal MI commands are sequenced properly.
     */
    @Test
    public void testConcurrentReadWrite() throws Throwable {
        // Next we test that we can deal with a write request and read request
        // at
        // the same time and vice-versa

        SyncUtil.runToLocation("testConcurrent");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "a[1]");

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {

                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("32")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating natural format, got " + getData().getFormattedValue()
                                        + " instead of 32", null));
                            }
                        }
                    }
                });

                wait.increment();
                fExpService.writeExpression(exprDmc, "56", IFormattedValues.NATURAL_FORMAT, new RequestMonitor(
                    fExpService.getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            wait.waitFinished();
                        }
                    }
                });

                // Use different format to avoid triggering the cache
                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.HEX_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("0x38")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating hex format, got " + getData().getFormattedValue()
                                        + " instead of 0x38", null));
                            }
                        }
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        assertTrue("ExprChangedEvent problem: expected 1, received " + getExprChangedCount(),
            getExprChangedCount() == 1);
        exprDmc.equals(getExprChangedContext());
        clearExprChangedData();
    }

    /**
     * This test tries many different operations during the same executor cycle,
     * to make sure the internal MI commands are sequenced properly.
     */
    @Test
    public void testConcurrentReadWriteChildren() throws Throwable {
        // Finally, we go nuts and request two reads, while requesting
        // a get children and get children count.
    	// Note that we don't request a write, because a write is allowed to
    	// go through at any time and we don't exactly know when it will
    	// change the value we are reading.

        SyncUtil.runToLocation("testConcurrent");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "a[1]");

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {

                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("32")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating natural format, got " + getData().getFormattedValue()
                                        + " instead of 32", null));
                            }
                        }
                    }
                });

                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.HEX_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("0x20")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating hex format, got " + getData().getFormattedValue()
                                        + " instead of 0x20", null));
                            }
                        }
                    }
                });

                wait.increment();
                fExpService.getSubExpressionCount(exprDmc, new DataRequestMonitor<Integer>(fExpService.getExecutor(),
                    null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData() != 0) {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed getting child count; expecting 0 got " + getData(), null));
                            } else {
                                wait.waitFinished();
                            }
                        }
                    }
                });

                wait.increment();
                fExpService.getSubExpressions(exprDmc, new DataRequestMonitor<IExpressionDMContext[]>(
                    fExpService.getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().length != 0) {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed getting children; expecting 0 got " + getData().length, null));
                            } else {
                                wait.waitFinished();
                            }
                        }
                    }
                });

                // Must use a different format or else the cache will be triggered
                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.OCTAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("040")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating hex format, got " + getData().getFormattedValue()
                                        + " instead of 040", null));
                            }
                        }
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        assertTrue("ExprChangedEvent problem: expected 0, received " + getExprChangedCount(),
            getExprChangedCount() == 0);
        exprDmc.equals(getExprChangedContext());
        clearExprChangedData();
    }

    /**
     * This test verifies that the ExpressionService caches the evaluation of an
     * expression in a specific format. It verifies this by: 1- reading a
     * variable 2- writing to that variable 3- reading the variable in a new
     * format and seeing the new value 4- reading the variable in the same
     * format as step 1 and seeing the old value cached Note that all above
     * steps must be done within the same Runnable submitted to the executor.
     * This allows the cache to be triggered before it is invalidated by a write
     * command, since the write command will need an new executor cycle to send
     * an MI command to the back-end
     */
    @Test
    public void testWriteCache() throws Throwable {
        // Test the cache by changing a value but triggering a read before the
        // write clears the cache

        SyncUtil.runToLocation("testConcurrent");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);

        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "a[1]");

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {

                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("32")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating natural format, got " + getData().getFormattedValue()
                                        + " instead of 32", null));
                            }
                        }
                    }
                });

                wait.increment();
                fExpService.writeExpression(exprDmc, "56", IFormattedValues.NATURAL_FORMAT, new RequestMonitor(
                    fExpService.getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            wait.waitFinished();
                        }
                    }
                });

                // Must use a different format or else the cache will be
                // triggered
                // This will prove that the write has changed the backend
                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.OCTAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("070")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating hex format, got " + getData().getFormattedValue()
                                        + " instead of 070", null));
                            }
                        }
                    }
                });

                // Test that the cache is triggered, giving us the old value
                // This happens because we are calling this operation on the
                // same executor run call.
                // NOTE that this is not a problem, because the writeExpression
                // will eventually
                // reset the cache (we'll test this below).
                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("32")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating natural format, got " + getData().getFormattedValue()
                                        + " instead of 32", null));
                            }
                        }
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        // Now that we know the writeExpressions completed and the cache was
        // reset, do a similar
        // request as above to see that the cache has indeed been reset
        wait.waitReset();
        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {

                wait.increment();
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (!isSuccess()) {
                            wait.waitFinished(getStatus());
                        } else {
                            if (getData().getFormattedValue().equals("56")) {
                                wait.waitFinished();
                            } else {
                                wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                    "Failed evaluating natural format, got " + getData().getFormattedValue()
                                        + " instead of 56", null));
                            }
                        }
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        assertTrue("ExprChangedEvent problem: expected 1, received " + getExprChangedCount(),
            getExprChangedCount() == 1);
        exprDmc.equals(getExprChangedContext());
        clearExprChangedData();
    }

    /**
     * Test that we can correctly retrieve the address and type size of an
     * expression
     */
    @Test
    public void testExprAddress() throws Throwable {

        SyncUtil.runToLocation("testAddress");
        MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);

        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "a");

        final IExpressionDMContext exprDmc2 = SyncUtil.createExpression(frameDmc, "a_ptr");

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        // First get the address of 'a' through 'a_ptr'
        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fExpService.getFormattedExpressionValue(fExpService.getFormattedValueContext(exprDmc2,
                    IFormattedValues.NATURAL_FORMAT), new DataRequestMonitor<FormattedValueDMData>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            wait.setReturnInfo(getData());
                        }

                        wait.waitFinished(getStatus());
                    }
                });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        String actualAddrStr = ((FormattedValueDMData) wait.getReturnInfo()).getFormattedValue();
        wait.waitReset();

        // Now check the address through our getAddressData
        checkAddressData(exprDmc, actualAddrStr, 4);

        assertTrue("ExprChangedEvent problem: expected 0, received " + getExprChangedCount(),
            getExprChangedCount() == 0);
    }

    
    /**
     * Test that we can correctly evaluate C expressions involving global
     * variables.
     * 
     * @return void
     */
    @Test
    public void testGlobalVariables() throws Throwable {

        // Step to a stack level of 2 to be able to test differen stack frames
        MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("locals2");

        // Create a map of expressions to expected values.
        Map<String, String[]> tests = new HashMap<String, String[]>();

        // Global variables
        tests.put("gIntVar", new String[] { "0x21F", "01037", "1000011111", "543", "543", "543" });
        tests.put("gDoubleVar", new String[] { "0x21F", "01037", "1000011111", "543", "543.54300000000001", "543.54300000000001" });
        tests.put("gCharVar", new String[] { "0x67", "0147", "1100111", "103", "103 'g'", "103 'g'" });
        tests.put("gBoolVar", new String[] { "0x0", "0", "0", "0", "false", "false" });

        tests.put("gIntArray[1]", new String[] { "0x28E", "01216", "1010001110", "654", "654", "654" });
        tests.put("gDoubleArray[1]", new String[] { "0x28E", "01216", "1010001110", "654", "654.32100000000003", "654.32100000000003" });
        tests.put("gCharArray[1]", new String[] { "0x64", "0144", "1100100", "100", "100 'd'", "100 'd'" });
        tests.put("gBoolArray[1]", new String[] { "0x0", "0", "0", "0", "false", "false" });

        tests.put("*gIntPtr", new String[] { "0x21F", "01037", "1000011111", "543", "543", "543" });
        tests.put("*gDoublePtr", new String[] { "0x21F", "01037", "1000011111", "543", "543.54300000000001", "543.54300000000001" });
        tests.put("*gCharPtr", new String[] { "0x67", "0147", "1100111", "103", "103 'g'", "103 'g'" });
        tests.put("*gBoolPtr", new String[] { "0x0", "0", "0", "0", "false", "false" });

        tests.put("gIntPtr2", new String[] { "0x8", "010", "1000", "8", "0x8" , "0x8" });
        tests.put("gDoublePtr2", new String[] { "0x5432", "052062", "101010000110010", "21554", "0x5432", "0x5432" });
        // GDB says a char* is out of bounds, but not the other pointers???
        // tests.put("gCharPtr2", new String[] { "0x4321", "041441",
        // "100001100100001", "17185", "0x4321" });
        tests.put("gBoolPtr2", new String[] { "0x12ABCDEF", "02252746757", "10010101010111100110111101111",
            "313249263", "0x12ABCDEF", "0x12ABCDEF" });

        // Try different stack frames
        executeExpressionSubTests(tests, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0));
        executeExpressionSubTests(tests, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 1));
        executeExpressionSubTests(tests, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 2));
    }

    /**
     * This test verifies that the ExpressionService can handle having a
     * variable with the same name in two different methods but at the same
     * stack depth.
     */
    @Test
    public void testNamingSameDepth() throws Throwable {
    	SyncUtil.runToLocation("testName1");
    	MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
    	IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

    	Map<String, String[]> tests = new HashMap<String, String[]>();
    	tests.put("a", new String[] { "0x1", "01", "1", "1", "1", "1" });
    	executeExpressionSubTests(tests, frameDmc);

    	SyncUtil.runToLocation("testName2");
    	stoppedEvent = SyncUtil.step(1, StepType.STEP_INTO);
    	frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	tests = new HashMap<String, String[]>();
    	tests.put("a", new String[] { "0x2", "02", "10", "2", "2", "2" });
    	executeExpressionSubTests(tests, frameDmc);

    	SyncUtil.runToLocation("testName1");
    	stoppedEvent = SyncUtil.step(1, StepType.STEP_INTO);
    	frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	tests = new HashMap<String, String[]>();
    	tests.put("a", new String[] { "0x3", "03", "11", "3", "3", "3" });
    	executeExpressionSubTests(tests, frameDmc);
    }
    
    /**
     * This test verifies that the ExpressionService can handle having a
     * variable with the same name in two methods that also have the same name
     */
    @Test
    public void testNamingSameMethod() throws Throwable {
    	SyncUtil.runToLocation("testSameName");
    	MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_INTO);
    	IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

    	Map<String, String[]> tests = new HashMap<String, String[]>();
    	tests.put("a", new String[] { "0x1", "01", "1", "1", "1" , "1" });
    	executeExpressionSubTests(tests, frameDmc);

    	SyncUtil.step(StepType.STEP_RETURN);
    	stoppedEvent = SyncUtil.step(2, StepType.STEP_INTO);
    	frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	tests = new HashMap<String, String[]>();
    	tests.put("a", new String[] { "0x2", "02", "10", "2", "2", "2"  });
    	executeExpressionSubTests(tests, frameDmc);

    	SyncUtil.step(StepType.STEP_RETURN);
    	stoppedEvent = SyncUtil.step(2, StepType.STEP_INTO);
    	frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	tests = new HashMap<String, String[]>();
    	tests.put("a", new String[] { "0x3", "03", "11", "3", "3", "3"  });
    	executeExpressionSubTests(tests, frameDmc);
    }

    /**
     * This test makes sure that if a request for expression values are made with
     * a thread selected, the top-most stack frame is used for evaluation
     */
    @Test
    public void testThreadContext() throws Throwable {

        // Step to a stack level of 2 to be able to test differen stack frames
         SyncUtil.runToLocation("locals2");
         MIStoppedEvent stoppedEvent = SyncUtil.step(StepType.STEP_OVER);

        // Create a map of expressions to expected values.
        Map<String, String[]> tests = new HashMap<String, String[]>();

        // First make sure we have a different value on the other stack frame and that we select
        // a frame that is not the top frame
        tests.put("lIntVar", new String[] { "0x3039", "030071", "11000000111001", "12345", "12345", "12345" });
        executeExpressionSubTests(tests, SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 1));
        
        // Now check that we get the same values as the top stack when selecting the thread only
        tests = new HashMap<String, String[]>();
        tests.put("lIntVar", new String[] { "0x1a85", "015205", "1101010000101", "6789", "6789" , "6789" });
        executeExpressionSubTests(tests, stoppedEvent.getDMContext());
    }

    /**
     * This test verifies that the ExpressionService can handle having a
     * child variable with the same name in two methods that also have the same name
     */
    @Test
    public void testChildNamingSameMethod() throws Throwable {
    	SyncUtil.runToLocation("testSameName");
    	MIStoppedEvent stoppedEvent = SyncUtil.step(4, StepType.STEP_INTO);
    	final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

    	fExpService.getExecutor().submit(new Runnable() {
    		@Override
			public void run() {

    			// First create the var object and all its children
    			IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc, "z");

    			fExpService.getSubExpressions(
    					parentDmc, 
    					new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    						@Override
    						protected void handleCompleted() {
    							if (!isSuccess()) {
    								wait.waitFinished(getStatus());
    							} else if (getData().length != 2) {
    								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    										"Failed getting children; expecting 2 got " + getData().length, null));
    							} else {
									// now get the value of the child
    								final String valueStr = "1";
    								final IExpressionDMContext child = getData()[0];
    								fExpService.getFormattedExpressionValue(
    										fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
    										new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
    											@Override
    											protected void handleCompleted() {
    												if (!isSuccess()) {
    													wait.waitFinished(getStatus());
    												} else if (getData().getFormattedValue().equals(valueStr)) {
    													wait.waitFinished();
    												} else {
    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    															"Failed evaluating " + child.getExpression() + ", got " + getData().getFormattedValue()
    															+ " instead of " + valueStr, null));
    												}
    											}

    										});
    							}


    						}	
    					});
    		}
    	});

    	wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
    	assertTrue(wait.getMessage(), wait.isOK());
    	wait.waitReset();

    	SyncUtil.step(StepType.STEP_RETURN);
    	stoppedEvent = SyncUtil.step(4, StepType.STEP_INTO);
    	final IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	fExpService.getExecutor().submit(new Runnable() {
    		@Override
			public void run() {

    			// First create the var object and all its children
    			IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc2, "z");

    			fExpService.getSubExpressions(
    					parentDmc, 
    					new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    						@Override
    						protected void handleCompleted() {
    							if (!isSuccess()) {
    								wait.waitFinished(getStatus());
    							} else if (getData().length != 2) {
    								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    										"Failed getting children; expecting 2 got " + getData().length, null));
    							} else {
									// now get the value of the child
    								final String valueStr = "2";
    								final IExpressionDMContext child = getData()[0];
    								fExpService.getFormattedExpressionValue(
    										fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
    										new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
    											@Override
    											protected void handleCompleted() {
    												if (!isSuccess()) {
    													wait.waitFinished(getStatus());
    												} else if (getData().getFormattedValue().equals(valueStr)) {
    													wait.waitFinished();
    												} else {
    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    															"Failed evaluating " + child.getExpression() + ", got " + getData().getFormattedValue()
    															+ " instead of " + valueStr, null));
    												}
    											}

    										});
    							}


    						}	
    					});
    		}
    	});
    	
       	wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
    	assertTrue(wait.getMessage(), wait.isOK());
    	wait.waitReset();

    	SyncUtil.step(StepType.STEP_RETURN);
    	stoppedEvent = SyncUtil.step(4, StepType.STEP_INTO);
    	final IFrameDMContext frameDmc3 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	fExpService.getExecutor().submit(new Runnable() {
    		@Override
			public void run() {

    			// First create the var object and all its children
    			IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc3, "z");

    			fExpService.getSubExpressions(
    					parentDmc, 
    					new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    						@Override
    						protected void handleCompleted() {
    							if (!isSuccess()) {
    								wait.waitFinished(getStatus());
    							} else if (getData().length != 2) {
    								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    										"Failed getting children; expecting 2 got " + getData().length, null));
    							} else {
									// now get the value of the child
    								final String valueStr = "3";
    								final IExpressionDMContext child = getData()[0];
    								fExpService.getFormattedExpressionValue(
    										fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
    										new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
    											@Override
    											protected void handleCompleted() {
    												if (!isSuccess()) {
    													wait.waitFinished(getStatus());
    												} else if (getData().getFormattedValue().equals(valueStr)) {
    													wait.waitFinished();
    												} else {
    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    															"Failed evaluating " + child.getExpression() + ", got " + getData().getFormattedValue()
    															+ " instead of " + valueStr, null));
    												}
    											}

    										});
    							}


    						}	
    					});
    		}
    	});
    	
       	wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
    	assertTrue(wait.getMessage(), wait.isOK());
    	wait.waitReset();

    }

    /**
     * This test verifies that the ExpressionService properly updates
     * children variables, when we do not update the parent explicitly
     */
    @Test
    public void testUpdatingChildren() throws Throwable {
    	SyncUtil.runToLocation("testUpdateChildren");
    	MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);
    	final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	doUpdateTest(frameDmc, 0);
    	
    	// Re-run the test to test out-of-scope update again
    	SyncUtil.step(StepType.STEP_RETURN);
    	stoppedEvent = SyncUtil.step(3, StepType.STEP_INTO);
    	final IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	doUpdateTest(frameDmc2, 100);
    	
    	// Re-run the test within a different method test out-of-scope updates
    	SyncUtil.step(StepType.STEP_RETURN);
    	stoppedEvent = SyncUtil.step(3, StepType.STEP_INTO);
    	final IFrameDMContext frameDmc3 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	doUpdateTest(frameDmc3, 200);

    }
    

    public void doUpdateTest(final IFrameDMContext frameDmc, final int baseValue) throws Throwable {
    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

    	fExpService.getExecutor().submit(new Runnable() {
    		@Override
			public void run() {

    			// First create the var object and all its children
    			IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc, "a");

    			fExpService.getSubExpressions(
    					parentDmc, 
    					new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    						@Override
    						protected void handleCompleted() {
    							if (!isSuccess()) {
    								wait.waitFinished(getStatus());
    							} else if (getData().length != 1) {
    								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    										"Failed getting children; expecting 1 got " + getData().length, null));
    							} else {
    								// Now list the children of this child
    								fExpService.getSubExpressions(
    										getData()[0], 
    										new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    											@Override
    											protected void handleCompleted() {
    												final IExpressionDMContext[] childDmcs = getData();

    												if (!isSuccess()) {
    													wait.waitFinished(getStatus());
    												} else if (childDmcs.length != 2) {
    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    															"Failed getting children; expecting 2 got " + childDmcs.length, null));
    												} else {
    													// now get the value of the two children
    													for (int i =0; i<2; i++) {
    														final String valueStr = Integer.toString(baseValue + i + 10);
    														final int finali = i;

    														wait.increment();
    														fExpService.getFormattedExpressionValue(
    																fExpService.getFormattedValueContext(childDmcs[i], IFormattedValues.NATURAL_FORMAT), 
    																new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
    																	@Override
    																	protected void handleCompleted() {
    																		if (!isSuccess()) {
    																			wait.waitFinished(getStatus());
    																		} else if (getData().getFormattedValue().equals(valueStr)) {
    																			wait.waitFinished();
    																		} else {
    																			wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    																					"Failed evaluating " + childDmcs[finali].getExpression() + ", got " + getData().getFormattedValue()
    																					+ " instead of " + valueStr, null));
    																		}
    																	}

    																});
    													}
    												}
    											}
    										});
    							}


    						}	
    					});
    		}
    	});

    	wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
    	assertTrue(wait.getMessage(), wait.isOK());
    	wait.waitReset();

    	// Now step to change the value of a.z.x and a.z.y and verify the changed values.
    	// This will confirm that the parent "a" will have been properly updated
    	// It is a better test to do it for two children because it tests concurrent update requests
    	MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_OVER);
    	final IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

    	fExpService.getExecutor().submit(new Runnable() {
    		@Override
			public void run() {

    			// First create the var object and all its children
    			IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc2, "a");

    			fExpService.getSubExpressions(
    					parentDmc, 
    					new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    						@Override
    						protected void handleCompleted() {
    							if (!isSuccess()) {
    								wait.waitFinished(getStatus());
    							} else if (getData().length != 1) {
    								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    										"Failed getting children; expecting 1 got " + getData().length, null));
    							} else {
    								// Now list the children of this child
    								fExpService.getSubExpressions(
    										getData()[0], 
    										new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    											@Override
    											protected void handleCompleted() {
    												final IExpressionDMContext[] childDmcs = getData();

    												if (!isSuccess()) {
    													wait.waitFinished(getStatus());
    												} else if (childDmcs.length != 2) {
    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    															"Failed getting children; expecting 2 got " + childDmcs.length, null));
    												} else {
    													// now get the value of the two children
    													for (int i =0; i<2; i++) {
    														final String valueStr = Integer.toString(baseValue + i + 20);
    														final int finali = i;

    														wait.increment();
    														fExpService.getFormattedExpressionValue(
    																fExpService.getFormattedValueContext(childDmcs[i], IFormattedValues.NATURAL_FORMAT), 
    																new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
    																	@Override
    																	protected void handleCompleted() {
    																		if (!isSuccess()) {
    																			wait.waitFinished(getStatus());
    																		} else if (getData().getFormattedValue().equals(valueStr)) {
    																			wait.waitFinished();
    																		} else {
    																			wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    																					"Failed evaluating " + childDmcs[finali].getExpression() + ", got " + getData().getFormattedValue()
    																					+ " instead of " + valueStr, null));
    																		}
    																	}

    																});
    													}
    												}
    											}
    										});
    							}


    						}	
    					});
    		}
    	});

    	wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
    	assertTrue(wait.getMessage(), wait.isOK());
    	wait.waitReset();
    }
    
    /**
     * This test creates a variable object with children (not an array) and then gets these children
     * to be deleted because of a large number of other variable objects being created.
     * We then check that the expression service can handle a request for one of those deleted children,
     * which has a complex path.
     */
    @Test
    public void testDeleteChildren() throws Throwable {
    	assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_6_7);
    	assumeGdbVersionLowerThen(ITestConstants.SUFFIX_GDB_7_3);
    	
        SyncUtil.runToLocation("testDeleteChildren");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {

        		// First create the var object and all its children
        		IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc, "f");

        		fExpService.getSubExpressions(
        				parentDmc, 
        				new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else {
        							if (getData().length != 5) {
        								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        										"Failed getting children; expecting 5 got " + getData().length, null));
        							} else {
        								String childStr = "((bar) f)";
        								if (!getData()[0].getExpression().equals(childStr)) {
        									wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        											"Got child " + getData()[0].getExpression() + " instead of " + childStr, null));
        								} else {
        									// Now list the children of the first element
        									fExpService.getSubExpressions(
        											getData()[0], 
        											new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
        												@Override
        												protected void handleCompleted() {
        													if (!isSuccess()) {
        														wait.waitFinished(getStatus());
        													} else {
        														if (getData().length != 2) {
        															wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        																	"Failed getting children; expecting 2 got " + getData().length, null));
        														} else {
        					        								String childStr = "((((bar) f)).d)";
        					        								if (!getData()[0].getExpression().equals(childStr)) {
        					        									wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        					        											"Got child " + getData()[0].getExpression() + " instead of " + childStr, null));
        					        								} else {
        					        									wait.setReturnInfo(getData()[0]);
        								        						wait.waitFinished();
        					        								}
        														}
        													}
        												}
        											});
        								}
        							}
        						}
        					}	
        				});
        	}
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        final IExpressionDMContext deletedChildDmc = (IExpressionDMContext)wait.getReturnInfo();

        wait.waitReset();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {

        		// Now create more than 1000 expressions to trigger the deletion of the children
        		// that were created above
        		for (int i=0; i<1100; i++) {
        			IExpressionDMContext dmc = fExpService.createExpression(frameDmc, "a[" + i + "]");

        			wait.increment();
        			fExpService.getExpressionData(
        					dmc, 
        					new DataRequestMonitor<IExpressionDMData>(fExpService.getExecutor(), null) {
        						@Override
        						protected void handleCompleted() {
        							if (!isSuccess()) {
        								wait.waitFinished(getStatus());
        							} else {
        								wait.waitFinished();
        							}
        						}	
        					});
        		}
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {

        		// Evaluate the expression of a child that we know is deleted to make sure
        		// the expression service can handle that
        		fExpService.getExpressionData(
        				deletedChildDmc, 
        				new DataRequestMonitor<IExpressionDMData>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else {
        							wait.waitFinished();
        						}
        					}	
        				});
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        
    }
    
    /**
	 * GDB 6.7 has a bug which will cause var-update not to show
	 * the new value of 'a' if we switch the format to binary,
	 * since binary of 3 is 11 which is the same as the old value
	 * in natural format.  Our expression service should work around this.
	 * 
	 * int main() {
	 *    int a = 11;
	 *    a = 3;
	 *    return 0;
	 * }
     */
    @Test
    public void testUpdateGDBBug() throws Throwable {
        SyncUtil.runToLocation("testUpdateGDBBug");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		// First create the var object and all its children
        		IExpressionDMContext exprDmc = fExpService.createExpression(frameDmc, "a");

        		// This call will create the variable object in natural format and then change
        		// it to binary to fetch the value
                fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(exprDmc, IFormattedValues.BINARY_FORMAT), 
                		new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (!isSuccess()) {
                                wait.waitFinished(getStatus());
                            } else {
                                if (getData().getFormattedValue().equals("1011")) {
                                    wait.waitFinished();
                                } else {
                                    wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                        "Failed evaluating binary format, expected 1011 but got " +
                                        getData().getFormattedValue(), null));
                                }
                            }
                        }
                    });
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        
        // Now step to change the value of "a" and ask for it again
        stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        final IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		// First create the var object and all its children
        		IExpressionDMContext exprDmc = fExpService.createExpression(frameDmc2, "a");

        		// This call will create the variable object in natural format and then change
        		// it to binary to fetch the value
                fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(exprDmc, IFormattedValues.BINARY_FORMAT), 
                		new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (!isSuccess()) {
                                wait.waitFinished(getStatus());
                            } else {
                                if (getData().getFormattedValue().equals("11")) {
                                    wait.waitFinished();
                                } else {
                                    wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                        "Failed evaluating binary format, expected 11 but got " +
                                        getData().getFormattedValue(), null));
                                }
                            }
                        }
                    });
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
    }

    /**
	 * var-update will not show a change if eval-expression is the same
	 * in the current format.  This is a problem for us because we don't
	 * know if another format changed:
	 * 
	 * int main() {
	 *    double a = 1.99;
	 *    a = 1.11;
	 * }
	 * 
	 * If a is displayed in anything but natural, both values of a are the same
	 * and we won't know it changed in the natural format.
	 * 
	 * The test below is in case GDB fixes var-update to keep track of the last
	 * printed value through eval-expression.  Until they do that, we do not have
	 * a problem because of our caching: where, if we change formats since the last
	 * var-update, it is impossible for us to set the format back
	 * to the one of the last -var-update, since we already have that value in our cache.
	 * So, the -var-update will show a change because of the new current format.
	 * But if GDB has eval-expression reset their stored printed_value, this test
	 * will fail and we'll know we have to fix something.
     */
    @Test
    public void testUpdateIssue() throws Throwable {
        SyncUtil.runToLocation("testUpdateIssue");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		// First create the var object and all its children
        		IExpressionDMContext exprDmc = fExpService.createExpression(frameDmc, "a");

        		// check that we have the proper value
        		wait.increment();
        		fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(exprDmc, IFormattedValues.NATURAL_FORMAT), 
                		new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (!isSuccess()) {
                                wait.waitFinished(getStatus());
                            } else {
                                if (getData().getFormattedValue().equals("1.99")) {
                                    wait.waitFinished();
                                } else {
                                    wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                        "Failed evaluating a, expected 1.99 but got " +
                                        getData().getFormattedValue(), null));
                                }
                            }
                        }
                    });
        		
        		// ask for hex to set the format to hex
        		wait.increment();
                fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(exprDmc, IFormattedValues.HEX_FORMAT), 
                		new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (!isSuccess()) {
                                wait.waitFinished(getStatus());
                            } else {
                                if (getData().getFormattedValue().equals("0x1")) {
                                    wait.waitFinished();
                                } else {
                                    wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                        "Failed evaluating a, expected 0x1 but got " +
                                        getData().getFormattedValue(), null));
                                }
                            }
                        }
                    });
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        
        // Now step to change the value of "a" and ask for it again but in the natural format
        stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        final IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		// First create the var object and all its children
        		IExpressionDMContext exprDmc = fExpService.createExpression(frameDmc2, "a");

        		// trigger the var-update in the last format (hex)
        		// then request the actual value in natural which should not be taken from the cache 
           		wait.increment();
                fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(exprDmc, IFormattedValues.NATURAL_FORMAT), 
                		new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (!isSuccess()) {
                                wait.waitFinished(getStatus());
                            } else {
                                if (getData().getFormattedValue().equals("1.22")) {
                                    wait.waitFinished();
                                } else {
                                    wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                        "Failed evaluating natural format, expected 1.22 but got " +
                                        getData().getFormattedValue(), null));
                                }
                            }
                        }
                    });
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
    }

    /**
	 * var-update will not show a change if eval-expression is the same
	 * in the current format.  This is a problem for us because we don't
	 * know if another format changed:
	 * 
	 * int main() {
	 * 	 struct {
	 *    	double d;
	 * 	 } z;
	 * 
	 *   z.d = 1.0;
	 *   z.d = 1.22;
	 * }
	 * 
	 * If a is displayed in anything but natural, both values of a are the same
	 * and we won't know it changed in the natural format.
	 * This test uses a child to increase the value of the test.
	 * Also, it avoids the cache saving us since we start with the 1.0 value
	 * which is the same in natural and decimal
     */
    @Test
    public void testUpdateIssue2() throws Throwable {
        SyncUtil.runToLocation("testUpdateIssue2");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		
        		IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc, "z");

    			fExpService.getSubExpressions(
    					parentDmc, 
    					new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    						@Override
    						protected void handleCompleted() {
    							if (!isSuccess()) {
    								wait.waitFinished(getStatus());
    							} else if (getData().length != 1) {
    								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    										"Failed getting children; expecting 1 got " + getData().length, null));
    							} else {
    				        		// check that we have the proper value
    				        		// This will cache the value 1 in the natural format cache
    								final String valueStr = "1";
    								globalExpressionCtx1 = getData()[0];
    				        		
    								wait.increment();
    								fExpService.getFormattedExpressionValue(
    										fExpService.getFormattedValueContext(globalExpressionCtx1, IFormattedValues.NATURAL_FORMAT), 
    										new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
    											@Override
    											protected void handleCompleted() {
    												if (!isSuccess()) {
    													wait.waitFinished(getStatus());
    												} else if (getData().getFormattedValue().equals(valueStr)) {
														wait.waitFinished();
    												} else {
    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    															"Failed evaluating " + globalExpressionCtx1.getExpression() + ", got " + getData().getFormattedValue()
    															+ " instead of " + valueStr, null));
    												}
    											}
    										});
    								
						       		// ask for decimal to set the format to decimal
    				        		wait.increment();
									fExpService.getFormattedExpressionValue(
											fExpService.getFormattedValueContext(globalExpressionCtx1, IFormattedValues.DECIMAL_FORMAT), 
											new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
												@Override
												protected void handleCompleted() {
													if (!isSuccess()) {
														wait.waitFinished(getStatus());
													} else {
														if (getData().getFormattedValue().equals(valueStr)) {
															wait.waitFinished();
														} else {
	    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
	    															"Failed evaluating " + globalExpressionCtx1.getExpression() + ", got " + getData().getFormattedValue()
	    															+ " instead of " + valueStr, null));
														}
													}
												}
											});

    							}
    						}
    					});
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        
        // Now step to change the value of "a" in natural but it remains the same in decimal
        SyncUtil.step(1, StepType.STEP_OVER);

        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {

        		// trigger the var-update in the last format (decimal)
        		// then request the actual value in natural which should not be taken from the cache 
           		wait.increment();
                fExpService.getFormattedExpressionValue(
                		fExpService.getFormattedValueContext(globalExpressionCtx1, IFormattedValues.NATURAL_FORMAT), 
                		new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (!isSuccess()) {
                                wait.waitFinished(getStatus());
                            } else {
                                if (getData().getFormattedValue().equals("1.22")) {
                                    wait.waitFinished();
                                } else {
                                    wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
                                        "Failed evaluating natural format, expected 1.22 but got " +
                                        getData().getFormattedValue(), null));
                                }
                            }
                        }
                    });
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
    }

    /**
     * This test verifies the state handling of a child variable object
     * to make sure that our locking scheme works even though we must deal
     * with an update call, internally
     */
    @Test
    public void testConcurrentReadAndUpdateChild() throws Throwable {
        SyncUtil.runToLocation("testConcurrentReadAndUpdateChild");
        MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        
        // Ask for one value to create the var object
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		// First create the var object and all its children
        		IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc, "z");

        		wait.increment();
    			fExpService.getSubExpressions(
    					parentDmc, 
    					new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    						@Override
    						protected void handleCompleted() {
    							if (!isSuccess()) {
    								wait.waitFinished(getStatus());
    							} else if (getData().length != 1) {
    								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    										"Failed getting children; expecting 1 got " + getData().length, null));
    							} else {
									// now get the value of the child
    								final String valueStr = "01";
    								globalExpressionCtx1 = getData()[0];
    								fExpService.getFormattedExpressionValue(
    										fExpService.getFormattedValueContext(globalExpressionCtx1, IFormattedValues.OCTAL_FORMAT), 
    										new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
    											@Override
    											protected void handleCompleted() {
    												if (!isSuccess()) {
    													wait.waitFinished(getStatus());
    												} else if (getData().getFormattedValue().equals(valueStr)) {
    													wait.waitFinished();
    												} else {
    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    															"Failed evaluating " + globalExpressionCtx1.getExpression() + ", got " + getData().getFormattedValue()
    															+ " instead of " + valueStr, null));
    												}
    											}
    										});
    							}
    						}
    					}); 
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();

        // Now do two reads in two different formats
        // We need to make sure that the locking properly works although we are calling
        // the internal update method, which does affect the state of the object
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		wait.increment();
        		fExpService.getFormattedExpressionValue(
        				fExpService.getFormattedValueContext(globalExpressionCtx1, IFormattedValues.BINARY_FORMAT), 
        				new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
								final String valueStr = "1";
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else if (getData().getFormattedValue().equals(valueStr)) {
        							wait.waitFinished();
        						} else {
        							wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        									"Failed evaluating " + globalExpressionCtx1.getExpression() + ", got " + getData().getFormattedValue()
        									+ " instead of " + valueStr, null));
        						}
        					}
        				});

        		wait.increment();
        		fExpService.getFormattedExpressionValue(
        				fExpService.getFormattedValueContext(globalExpressionCtx1, IFormattedValues.HEX_FORMAT), 
        				new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
								final String valueStr = "0x1";
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else if (getData().getFormattedValue().equals(valueStr)) {
        							wait.waitFinished();
        						} else {
        							wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        									"Failed evaluating " + globalExpressionCtx1.getExpression() + ", got " + getData().getFormattedValue()
        									+ " instead of " + valueStr, null));
        						}
        					}

        				});
        	}
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
    }
    
    /**
     * This test verifies some of the logic of dealing with out-of-scope variables.
     * This particular scenario is that we create a parent with a child and then
     * have them go out of scope.  Then we request the child which will update the parent
     * and mark it as out-of-scope and recreate the child.  The parent is not re-created.
     * We then ask twice for the parent which is already known to be out-of-scope and we need
     * to make sure that the parent is re-created once and only once.
     * We had a bug where we would enter an infinite loop in this case.
     */
    @Test
    public void testConcurrentUpdateOutOfScopeChildThenParent() throws Throwable {
        SyncUtil.runToLocation("testConcurrentUpdateOutOfScopeChildThenParent");
        MIStoppedEvent stoppedEvent = SyncUtil.step(2, StepType.STEP_INTO);
        
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();
        
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		// First create the var object and its child
        		globalExpressionCtx1 = fExpService.createExpression(frameDmc, "z");

        		wait.increment();
    			fExpService.getSubExpressions(
    					globalExpressionCtx1, 
    					new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
    						@Override
    						protected void handleCompleted() {
    							if (!isSuccess()) {
    								wait.waitFinished(getStatus());
    							} else if (getData().length != 1) {
    								wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    										"Failed getting children; expecting 1 got " + getData().length, null));
    							} else {
									// now get the value of the child
    								final String valueStr = "1";
    								globalExpressionCtx2 = getData()[0];
    								fExpService.getFormattedExpressionValue(
    										fExpService.getFormattedValueContext(globalExpressionCtx2, IFormattedValues.NATURAL_FORMAT), 
    										new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
    											@Override
    											protected void handleCompleted() {
    												if (!isSuccess()) {
    													wait.waitFinished(getStatus());
    												} else if (getData().getFormattedValue().equals(valueStr)) {
    													wait.waitFinished();
    												} else {
    													wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
    															"Failed evaluating " + globalExpressionCtx2.getExpression() + ", got " + getData().getFormattedValue()
    															+ " instead of " + valueStr, null));
    												}
    											}
    										});
    							}
    						}
    					}); 
        	}
        });
        
        wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();

        SyncUtil.step(StepType.STEP_RETURN);
        stoppedEvent = SyncUtil.step(2, StepType.STEP_INTO);
        
        // Now step to another method to make the previous variable objects out-of-scope
        // then first request the child and then the parent.  We want to test this order
        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		wait.increment();
        		fExpService.getFormattedExpressionValue(
        				fExpService.getFormattedValueContext(globalExpressionCtx2, IFormattedValues.NATURAL_FORMAT), 
        				new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
								final String valueStr = "2";
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else if (getData().getFormattedValue().equals(valueStr)) {
        							wait.waitFinished();
        						} else {
        							wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        									"Failed evaluating " + globalExpressionCtx2.getExpression() + ", got " + getData().getFormattedValue()
        									+ " instead of " + valueStr, null));
        						}
        					}
        				});
        	}
        });
        
        wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();

        fExpService.getExecutor().submit(new Runnable() {
        	@Override
			public void run() {
        		wait.increment();
        		fExpService.getFormattedExpressionValue(
        				fExpService.getFormattedValueContext(globalExpressionCtx1, IFormattedValues.NATURAL_FORMAT), 
        				new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
								final String valueStr = "{...}";
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else if (getData().getFormattedValue().equals(valueStr)) {
        							wait.waitFinished();
        						} else {
        							wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        									"Failed evaluating " + globalExpressionCtx1.getExpression() + ", got " + getData().getFormattedValue()
        									+ " instead of " + valueStr, null));
        						}
        					}
        				});
        		
        		// Ask a second time but in a different format, to avoid the cache
        		wait.increment();
        		fExpService.getFormattedExpressionValue(
        				fExpService.getFormattedValueContext(globalExpressionCtx1, IFormattedValues.DECIMAL_FORMAT), 
        				new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
        					@Override
        					protected void handleCompleted() {
								final String valueStr = "{...}";
        						if (!isSuccess()) {
        							wait.waitFinished(getStatus());
        						} else if (getData().getFormattedValue().equals(valueStr)) {
        							wait.waitFinished();
        						} else {
        							wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
        									"Failed evaluating " + globalExpressionCtx1.getExpression() + ", got " + getData().getFormattedValue()
        									+ " instead of " + valueStr, null));
        						}
        					}
        				});

        	}
        });
        
        wait.waitUntilDone(TestsPlugin.massageTimeout(5000));
        assertTrue(wait.getMessage(), wait.isOK());
        wait.waitReset();
        
        //TODO although this test passes, the variable z is created twice, without being
        // deleted in GDB.  We should fix this
    }

    /**
     * This test verifies that we properly update a pointer and its child since they can both
     * change and be reported by var-update
     */
    @Test
    public void testUpdateOfPointer() throws Throwable {
		/* Places we're going to run to. */
		String tag1 = String.format("%s:%d", SOURCE_NAME, getLineForTag("testUpdateOfPointer_1"));
		String tag2 = String.format("%s:%d", SOURCE_NAME, getLineForTag("testUpdateOfPointer_2"));

		MIStoppedEvent stoppedEvent = SyncUtil.runToLocation(tag1);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		/* Create expression for the structure. */
		IExpressionDMContext structDmc = SyncUtil.createExpression(frameDmc, "z");

		/* Create sub-expressions for the structure field. */
		IExpressionDMContext[] fieldsDmc = SyncUtil.getSubExpressions(structDmc);
		assertThat(fieldsDmc.length, is(2));

		/* Get the value of the integer. */
		String integerValue = SyncUtil.getExpressionValue(fieldsDmc[0], IFormattedValues.NATURAL_FORMAT);
		assertThat(integerValue, is("1"));

		/* Get the value of the integer pointer. */
		String pointerFirstValue = SyncUtil.getExpressionValue(fieldsDmc[1], IFormattedValues.NATURAL_FORMAT);

		/* Get the value pointed by the pointer field. */
		IExpressionDMContext pointeeDmc = SyncUtil.getSubExpression(fieldsDmc[1]);
		String pointeeActualValue = SyncUtil.getExpressionValue(pointeeDmc, IFormattedValues.NATURAL_FORMAT);
		assertThat(pointeeActualValue, is("1"));

		/* Run to the second tag. */
		SyncUtil.runToLocation(tag2);

		/* Get the value of the integer. */
		integerValue = SyncUtil.getExpressionValue(fieldsDmc[0], IFormattedValues.NATURAL_FORMAT);
		assertThat(integerValue, is("2"));

		/* Get the value of the integer pointer. It should have changed from
		   last time. */
		String pointerSecondValue = SyncUtil.getExpressionValue(fieldsDmc[1], IFormattedValues.NATURAL_FORMAT);
		assertThat(pointerSecondValue, is(not(equalTo(pointerFirstValue))));

		/* Get the value pointed by the pointer field. */
		pointeeActualValue = SyncUtil.getExpressionValue(pointeeDmc, IFormattedValues.NATURAL_FORMAT);
		assertThat(pointeeActualValue, is("3"));
    }

    /**
	 * This test is similar to {@link #testUpdateOfPointer()
	 * testUpdateOfPointer}, but uses a pointer declared using a typedef. We
	 * test both a pointer that is a root varobj (a variable) and a pointer that
	 * is a child varobj (field in a structure).
	 */
    @Test
    public void testUpdateOfPointerTypedef() throws Throwable {
		/* Places we're going to run to. */
		String tag1 = String.format("%s:%d", SOURCE_NAME, getLineForTag("testUpdateOfPointerTypedef_1"));
		String tag2 = String.format("%s:%d", SOURCE_NAME, getLineForTag("testUpdateOfPointerTypedef_2"));

		MIStoppedEvent stoppedEvent = SyncUtil.runToLocation(tag1);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		/* Create expression for the structure. */
		IExpressionDMContext structDmc = SyncUtil.createExpression(frameDmc, "s");

		/* Create expression for the pointer variable and its target. */
		IExpressionDMContext pointerVarDmc = SyncUtil.createExpression(frameDmc, "ptr");
		IExpressionDMContext pointerVarTargetDmc = SyncUtil.getSubExpression(pointerVarDmc);

		/* Create expression for the pointer field and its target. */
		IExpressionDMContext pointerFieldDmc = SyncUtil.getSubExpression(structDmc);
		IExpressionDMContext pointerFieldTargetDmc = SyncUtil.getSubExpression(pointerFieldDmc);

		/* Get the values of the pointers. */
		String pointerVarValue1 = SyncUtil.getExpressionValue(pointerVarDmc, IFormattedValues.NATURAL_FORMAT);
		String pointerFieldValue1 = SyncUtil.getExpressionValue(pointerFieldDmc, IFormattedValues.NATURAL_FORMAT);

		/* Verify the pointed values. */
		String pointerVarTargetValue = SyncUtil.getExpressionValue(pointerVarTargetDmc, IFormattedValues.NATURAL_FORMAT);
		String pointerFieldTargetValue = SyncUtil.getExpressionValue(pointerFieldTargetDmc, IFormattedValues.NATURAL_FORMAT);
		assertThat(pointerVarTargetValue, is("1"));
		assertThat(pointerFieldTargetValue, is("2"));

		/* Run to the second tag. */
		SyncUtil.runToLocation(tag2);

		/* Get the new values of the pointers and make sure they have changed. */
		String pointerVarValue2 = SyncUtil.getExpressionValue(pointerVarDmc, IFormattedValues.NATURAL_FORMAT);
		String pointerFieldValue2 = SyncUtil.getExpressionValue(pointerFieldDmc, IFormattedValues.NATURAL_FORMAT);
		assertThat(pointerVarValue2, is(not(equalTo(pointerVarValue1))));
		assertThat(pointerFieldValue2, is(not(equalTo(pointerFieldValue1))));

		/* Verify the new pointed values. */
		pointerVarTargetValue = SyncUtil.getExpressionValue(pointerVarTargetDmc, IFormattedValues.NATURAL_FORMAT);
		pointerFieldTargetValue = SyncUtil.getExpressionValue(pointerFieldTargetDmc, IFormattedValues.NATURAL_FORMAT);
		assertThat(pointerVarTargetValue, is("3"));
		assertThat(pointerFieldTargetValue, is("4"));
    }

    /**
     * This test verifies that we properly return if we can write to different expressions
     */
    @Test
    public void testCanWrite() throws Throwable {
    	MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testCanWrite");
    	final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

    	fExpService.getExecutor().submit(new Runnable() {
    		@Override
			public void run() {

    			final int exprCount = 5;
    			final IExpressionDMContext dmcs[] = new IExpressionDMContext[exprCount];
    			final boolean expectedValues[] = new boolean[exprCount];

    			int exprIndex = 0;
    			dmcs[exprIndex] = fExpService.createExpression(frameDmc, "a");
    			expectedValues[exprIndex] = true;
    			exprIndex++;
    			dmcs[exprIndex] = fExpService.createExpression(frameDmc, "b");
    			expectedValues[exprIndex] = true;
    			exprIndex++;
    			dmcs[exprIndex] = fExpService.createExpression(frameDmc, "c");
    			expectedValues[exprIndex] = false;
    			exprIndex++;
    			dmcs[exprIndex] = fExpService.createExpression(frameDmc, "d");
    			expectedValues[exprIndex] = false;
    			exprIndex++;
    			dmcs[exprIndex] = fExpService.createExpression(frameDmc, "d[1]");
    			expectedValues[exprIndex] = true;
    			exprIndex++;

    			for (int index = 0; index < exprCount; index++) {
    				final int finalIndex = index;
    				wait.increment();
    				fExpService.canWriteExpression(
    						dmcs[finalIndex], 
    						new DataRequestMonitor<Boolean>(fExpService.getExecutor(), null) {
    							@Override
    							protected void handleCompleted() {
    								if (!isSuccess()) {
    									wait.waitFinished(getStatus());
    								} else if (getData() == expectedValues[finalIndex]) {
										wait.waitFinished();
									} else {
										wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
												"Failed establishing proper canWrite for  " + dmcs[finalIndex].getExpression() + 
												", got " + getData() + " instead of " + expectedValues[finalIndex], null));
									}


    							}
    						});
    			}
    		}
    	});

    	wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
    	assertTrue(wait.getMessage(), wait.isOK());
    	wait.waitReset();
    }

    /**
     * This test verifies that we properly return if we can write to an expression
     * that is an L-Value or a Constant
     */
    @Test
    public void testCanWriteLValue() throws Throwable {
    	assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_6_8);
    	MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testCanWrite");  // Re-use test
    	final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

    	final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

    	fExpService.getExecutor().submit(new Runnable() {
    		@Override
			public void run() {

    			final int exprCount = 2;
    			final IExpressionDMContext dmcs[] = new IExpressionDMContext[exprCount];
    			final boolean expectedValues[] = new boolean[exprCount];

    			int exprIndex = 0;
    			dmcs[exprIndex] = fExpService.createExpression(frameDmc, "&a");
    			expectedValues[exprIndex] = false;
    			exprIndex++;
    			dmcs[exprIndex] = fExpService.createExpression(frameDmc, "1");
    			expectedValues[exprIndex] = false;
    			exprIndex++;

    			for (int index = 0; index < exprCount; index++) {
    				final int finalIndex = index;
    				wait.increment();
    				fExpService.canWriteExpression(
    						dmcs[finalIndex], 
    						new DataRequestMonitor<Boolean>(fExpService.getExecutor(), null) {
    							@Override
    							protected void handleCompleted() {
    								if (!isSuccess()) {
    									wait.waitFinished(getStatus());
    								} else if (getData() == expectedValues[finalIndex]) {
										wait.waitFinished();
									} else {
										wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
												"Failed establishing proper canWrite for  " + dmcs[finalIndex].getExpression() + 
												", got " + getData() + " instead of " + expectedValues[finalIndex], null));
									}


    							}
    						});
    			}
    		}
    	});

    	wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
    	assertTrue(wait.getMessage(), wait.isOK());
    	wait.waitReset();
    }

	/**
	 * Executes a group of sub-tests.
	 * 
	 * @param tests
	 *            A Map in which the key is an expression to evaluate and the
	 *            value is an array of expected values, one for each of the
	 *            formats supported by the Expressions service (hex, octal,
	 *            binary, decimal, natural, details).
	 * @param exact
	 *            Indicates whether the natural and details format should
	 *            require an exact match to the expected value, or whether the
	 *            comparison should match only up to the number of characters
	 *            provided in the expected value. Where this is used is in
	 *            expressions that involve floating point calculation. Such
	 *            calculations are not exact (even when you'd think they should
	 *            be) and these tests cannot predict what exactly the result
	 *            will be. When this param is false, then we consider it a match
	 *            if, e.g., the gdb expression resolves to "1.23456789", but the
	 *            caller only supplied "1.2345".
	 */
    private void executeExpressionSubTests(final Map<String, String[]> tests, final boolean exact, IDMContext dmc)
        throws Throwable 
    {

        // Now evaluate each of the above expressions and compare the actual
        // value against
        // the expected value.
        for (final String expressionToEvaluate : tests.keySet()) {

            // Get an IExpressionDMContext object representing the expression to
            // be evaluated.
            final IExpressionDMContext exprDMC = SyncUtil.createExpression(dmc, expressionToEvaluate);

            final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

            // Get the list of available format IDs for this expression and for
            // each one,
            // get the value of the expression
            fExpService.getExecutor().submit(new Runnable() {
                @Override
				public void run() {
                    fExpService.getAvailableFormats(exprDMC, new DataRequestMonitor<String[]>(
                        fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (!isSuccess()) {
                                wait.waitFinished(getStatus());
                            } else {
                                final String[] formatIds = getData();

                                // Now run the current sub-test using each of
                                // the formats available for the type of
                                // the expression in the sub-test.

                                for (final String formatId : formatIds) {
                                    // Get a FormattedValueCMContext object for
                                    // the expression-formatID pair.
                                    final FormattedValueDMContext valueDmc = fExpService.getFormattedValueContext(
                                        exprDMC, formatId);

                                    // Increment the number of completed
                                    // requests to wait for, since we will send
                                    // multiple concurrent requests
                                    wait.increment();

                                    // Evaluate the expression represented by
                                    // the FormattedValueDMContext object
                                    // This actually evaluates the expression.
                                    fExpService.getFormattedExpressionValue(valueDmc,
                                        new DataRequestMonitor<FormattedValueDMData>(fExpService.getExecutor(), null) {
                                            @Override
                                            protected void handleCompleted() {
                                                if (!isSuccess()) {
                                                    wait.waitFinished(getStatus());
                                                } else {

                                                    // Get the
                                                    // FormattedValueDMData
                                                    // object from the waiter.
                                                    FormattedValueDMData exprValueDMData = getData();

                                                    final String[] expectedValues = tests.get(expressionToEvaluate);

                                                    // Check the value of the expression for correctness.
                                                    String actualValue = exprValueDMData.getFormattedValue();
                                                    String expectedValue;

                                                    if (formatId.equals(IFormattedValues.HEX_FORMAT))
                                                        expectedValue = expectedValues[0];
                                                    else if (formatId.equals(IFormattedValues.OCTAL_FORMAT))
                                                        expectedValue = expectedValues[1];
                                                    else if (formatId.equals(IFormattedValues.BINARY_FORMAT))
                                                        expectedValue = expectedValues[2];
                                                    else if (formatId.equals(IFormattedValues.DECIMAL_FORMAT))
                                                        expectedValue = expectedValues[3];
                                                    else if (formatId.equals(IFormattedValues.NATURAL_FORMAT))
                                                        expectedValue = expectedValues[4];
                                                    else if (formatId.equals(MIExpressions.DETAILS_FORMAT))
                                                    	expectedValue = expectedValues[5];
                                                    else
                                                        expectedValue = "[Unrecognized format ID: " + formatId + "]";

                                                    if ((exact == false) && 
                                                    		(formatId.equals(IFormattedValues.NATURAL_FORMAT) || formatId.equals(MIExpressions.DETAILS_FORMAT)) &&
                                                    		(expectedValue.length() < actualValue.length())) {
                                                    	actualValue = actualValue.substring(0, expectedValue.length());
                                                    }
                                                    
                                                    if (actualValue.equalsIgnoreCase(expectedValue)) {
                                                        wait.waitFinished();
                                                    } else {
                                                        String errorMsg = "Failed to correctly evalutate '"
                                                            + expressionToEvaluate + "': expected '" + expectedValue
                                                            + "', got '" + actualValue + "'";
                                                        wait.waitFinished(new Status(IStatus.ERROR,
                                                            TestsPlugin.PLUGIN_ID, errorMsg, null));
                                                    }
                                                }
                                            }
                                        });
                                }
                            }
                        }
                    });
                }
            });
            wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
            assertTrue(wait.getMessage(), wait.isOK());
            assertTrue("ExprChangedEvent problem: expected 0, received " + getExprChangedCount(),
                getExprChangedCount() == 0);
        }
    }
    
    private void executeExpressionSubTests(final Map<String, String[]> tests, IDMContext dmc) throws Throwable {
    	executeExpressionSubTests(tests, true, dmc);
    }
    
    private boolean addressesEqual(IExpressionDMAddress addrToTest, String addrStr, int size) {
        IAddress addr;
        if (addrStr.length() <= 10) {
            addr = new Addr32(addrStr);
        } else {
            addr = new Addr64(addrStr);
        }
        return addrToTest.getAddress().equals(addr) && addrToTest.getSize() == size;
    }
    
    private void checkAddressData(final IExpressionDMContext dmc, String actualAddrStr, int actualAddrSize) throws Throwable {
    	
        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {
                fExpService.getExpressionAddressData(dmc, new DataRequestMonitor<IExpressionDMAddress>(fExpService
                    .getExecutor(), null) {
                    @Override
                    protected void handleCompleted() {
                        if (isSuccess()) {
                            wait.setReturnInfo(getData());
                        }

                        wait.waitFinished(getStatus());
                    }
                });
            }
        });
        
        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());
        
       IExpressionDMAddress addr = (IExpressionDMAddress)wait.getReturnInfo();

        assertTrue("Unable to get address", addr != null);
        if (addr != null) {
        	assertTrue("Received wrong address of " + addr.toString() + " instead of (" + 
        			actualAddrStr + ", " + actualAddrSize + ")", 
        			addressesEqual(addr, actualAddrStr, actualAddrSize));
        }
    }
    
    private void doTestChildren(IExpressionDMContext exprDMC) throws Throwable 
    {
	    IExpressionDMContext[] children =
	    	getChildren(exprDMC, new String[] {"bar", "bar2", "a", "b", "c"});
	    
	    // f.bar
	    IExpressionDMContext[] children1 = 
	    	getChildren(children[0], new String[] {"d", "e"});
	    // f.bar.d
	    getChildren(children1[0], new String[0]);
	    // f.bar.e
	    IExpressionDMContext[] children2 = 
	    	getChildren(children1[1], new String[] {"e[0]", "e[1]"});
	    // f.bar.e[0]
	    getChildren(children2[0], new String[0]);
	    // f.bar.e[1]
	    getChildren(children2[1], new String[0]);
	
	    // f.bar2
	    children1 =	getChildren(children[1], new String[] {"f", "g"});
	    // f.bar2.f
	    getChildren(children1[0], new String[0]);
	    // f.bar2.g
	    children2 =	getChildren(children1[1], new String[] {"g[0]", "g[1]"});
	    // f.bar2.g[0]
	    getChildren(children2[0], new String[0]);
	    // f.bar2.g[1]
	    getChildren(children2[1], new String[0]);
	
	    // f.a
	    children1 =	getChildren(children[2], new String[] {"a[0]", "a[1]"});
	    // f.a[0]
	    getChildren(children1[0], new String[0]);
	    // f.a[1]
	    getChildren(children1[1], new String[0]);
	
	    // f.b
	    children1 =	getChildren(children[3], new String[] {"d", "e"});
	    // f.b.d
	    getChildren(children1[0], new String[0]);
	    // f.b.e
	    children2 =	getChildren(children1[1], new String[] {"e[0]", "e[1]"});
	    // f.b.e[0]
	    getChildren(children2[0], new String[0]);
	    // f.b.e[1]
	    getChildren(children2[1], new String[0]);
	
	    // f.c
	    getChildren(children[4], new String[0]);
	
	    assertTrue("ExprChangedEvent problem: expected 0, received " + getExprChangedCount(),
	        getExprChangedCount() == 0);
	}

    // This method tests IExspressions.getSubExpressions(IExpressionDMC, DRM);
    protected IExpressionDMContext[] getChildren(
    		final IExpressionDMContext parentDmc, 
    		String[] expectedValues) throws Throwable {

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {

                fExpService.getSubExpressions(parentDmc,
                    new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (isSuccess()) {
                            	wait.setReturnInfo(getData());
                            }
                            wait.waitFinished(getStatus());
                        }
                    });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        IExpressionDMContext[] childDmcs = 
        	(IExpressionDMContext[]) wait.getReturnInfo();

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
            assertEquals(expectedValues[i], childDmcsAccessor[i].getRelativeExpression());
        }
        
        return childDmcs;
    }

    // This method tests IExpressions.getSubExpressions(IExpressionDMC, int, int, DRM);
    protected IExpressionDMContext[] getChildren(
    		final IExpressionDMContext parentDmc,
    		final int startIndex,
    		final int length,
    		String[] expectedValues) throws Throwable {

        final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

        fExpService.getExecutor().submit(new Runnable() {
            @Override
			public void run() {

                fExpService.getSubExpressions(
                	parentDmc,
                	startIndex,
                	length,
                    new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (isSuccess()) {
                            	wait.setReturnInfo(getData());
                            }
                            wait.waitFinished(getStatus());
                        }
                    });
            }
        });

        wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
        assertTrue(wait.getMessage(), wait.isOK());

        IExpressionDMContext[] childDmcs = 
        	(IExpressionDMContext[]) wait.getReturnInfo();

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
            assertTrue("Expected: " + expectedValues[i] + " got: " + childDmcsAccessor[i].getRelativeExpression(),
            		childDmcsAccessor[i].getRelativeExpression().equals(expectedValues[i]));
        }
        
        return childDmcs;
    }

    /**
     * This test verifies that large arrays are properly partitioned and 
     * the handling of "small" arrays is not affected.
     */
    @Test
    public void testArrays() throws Throwable {
    	MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testArrays");

        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
	    
        // int array_simple[10];
	    IExpressionDMContext arraySimpleExprDMC = SyncUtil.createExpression(frameDmc, "array_simple");
	    
	    assertChildrenCount(arraySimpleExprDMC, 10);

	    // get all children
	    String[] expectedValues = new String[10];
	    for (int i = 0; i < expectedValues.length; ++i) {
	    	expectedValues[i] = String.format("array_simple[%d]", i);
	    }
	    IExpressionDMContext[] arraySimpleChildren = getChildren(arraySimpleExprDMC, expectedValues);
	    for (IExpressionDMContext ctx : arraySimpleChildren)
	    	getChildren(ctx, new String[0]);
	    
	    // get some parts of the children array
	    getChildren(arraySimpleExprDMC, 3, 2, new String[] { "array_simple[3]", "array_simple[4]" });
	    getChildren(arraySimpleExprDMC, 9, 3, new String[] { "array_simple[9]" });

	    // int array_int[24321];
	    IExpressionDMContext arrayIntExprDMC = SyncUtil.createExpression(frameDmc, "array_int");
	    assertChildrenCount(arrayIntExprDMC, 3);
	    
	    // get top level partitions: [0-9999], [10000-19999], [20000-24321]
	    IExpressionDMContext[] arrayIntPartitions =
		    	getChildren(arrayIntExprDMC, new String[] {"*((array_int)+0)@10000", "*((array_int)+10000)@10000", "*((array_int)+20000)@4321"});
	    assertTrue(String.format("Invalid number of partition: expected 3 got %d", arrayIntPartitions.length), arrayIntPartitions.length == 3);

	    // get children of the last partition: [20000-24321] 
	    expectedValues = new String[44];
	    for(int i = 0; i < expectedValues.length - 1; ++i) {
	    	expectedValues[i] = String.format("*((array_int)+%d)@100", 20000 + i*100);
	    }
	    expectedValues[expectedValues.length - 1] = "*((array_int)+24300)@21";
	    IExpressionDMContext[] arrayIntPartitions1 = getChildren(arrayIntPartitions[2], expectedValues);
	    expectedValues = new String[21];
	    for(int i = 0; i < expectedValues.length; ++i) {
	    	expectedValues[i] = String.format("array_int[%d]", 24300 + i);
	    }
	    getChildren(arrayIntPartitions1[arrayIntPartitions1.length - 1], expectedValues);

	    // foo array_foo[1200];
	    IExpressionDMContext arrayFooExprDMC = SyncUtil.createExpression(frameDmc, "array_foo");	    
	    assertChildrenCount(arrayFooExprDMC, 12);
	    expectedValues = new String[12];
	    for (int i = 0; i < expectedValues.length; ++i) {
	    	expectedValues[i] = String.format("*((array_foo)+%d)@%d", i*100, 100);
	    }
	    IExpressionDMContext[] arrayFooPartitions =	getChildren(arrayFooExprDMC, expectedValues);
	    for (int i = 0; i < arrayFooPartitions.length; ++i) {
	    	IExpressionDMContext ctx = arrayFooPartitions[i];
	    	assertTrue(String.format("Invalid DM context type: expected '%s' got '%s'", 
	    			IIndexedPartitionDMContext.class.getName(), ctx.getClass().getName()), 
	    			ctx instanceof IIndexedPartitionDMContext);
		    expectedValues = new String[100];
		    for (int j = 0; j < expectedValues.length; ++j) {
		    	expectedValues[j] = String.format("array_foo[%d]", i*100 + j);
		    }
		    IExpressionDMContext[] arrayFooChildren = getChildren(ctx, expectedValues);
		    // check the children of a couple of children
	    	getChildren(arrayFooChildren[0], new String[] {"bar", "bar2", "a", "b", "c"});
	    	getChildren(arrayFooChildren[80], new String[] {"bar", "bar2", "a", "b", "c"});
		    
		    // get parts of the children array
		    expectedValues = new String[] { String.format("array_foo[%d]", i*100 + 3), String.format("array_foo[%d]", i*100 + 4) };
		    getChildren(ctx, 3, 2, expectedValues);
		    getChildren(ctx, 99, 3, new String[] { String.format("array_foo[%d]", i*100 + 99) });
	    }
    }

    /**
     * This test verifies that large double arrays are properly partitioned
     */
    @Test
    public void testLargeDoubleArray() throws Throwable {
    	MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testArrays");
    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
        // char array_double_large[111][210]
	    IExpressionDMContext arrayDoubleLargeExprDMC = SyncUtil.createExpression(frameDmc, "array_double_large");
	    
	    assertChildrenCount(arrayDoubleLargeExprDMC, 2);

	    // get top level partitions: [0-99], [100-110]
	    IExpressionDMContext[] arrayTopPartitions =
		    	getChildren(arrayDoubleLargeExprDMC, new String[] {"*((array_double_large)+0)@100", "*((array_double_large)+100)@11"});
	    assertTrue(String.format("Invalid number of partition: expected 2 got %d", arrayTopPartitions.length), arrayTopPartitions.length == 2);

	    // get children child array_double_large[100-110]
	    IExpressionDMContext arrayDoubleLargeChildExprDMC = arrayTopPartitions[1];
	    
	    assertChildrenCount(arrayDoubleLargeChildExprDMC, 11);

	    String[] expectedValues = new String[11];
	    for(int i = 0; i < expectedValues.length; ++i) {
	    	expectedValues[i] = String.format("array_double_large[%d]", 100 +i);
	    }
	    IExpressionDMContext[] arrayChild = getChildren(arrayDoubleLargeChildExprDMC, expectedValues);

	    // get second level partitions: array_double_large[101][0-99], [100-199], [200-209]
	    IExpressionDMContext arrayDoubleLargeChildExprDMC2 = arrayChild[1];

	    assertChildrenCount(arrayDoubleLargeChildExprDMC2, 3);

	    IExpressionDMContext[] arraySecondLevelPartitions =
		    	getChildren(arrayDoubleLargeChildExprDMC2, new String[] {"*((array_double_large[101])+0)@100", 
		    			                                           "*((array_double_large[101])+100)@100",
		    			                                           "*((array_double_large[101])+200)@10"});
	    assertTrue(String.format("Invalid number of partition: expected 3 got %d", arraySecondLevelPartitions.length), arraySecondLevelPartitions.length == 3);

	    // get children of array_double_large[101][0-99]
	    IExpressionDMContext arrayDoubleLargeChildExprDMC3 = arraySecondLevelPartitions[0];
	    
	    assertChildrenCount(arrayDoubleLargeChildExprDMC3, 100);

	    expectedValues = new String[100];
	    for(int i = 0; i < expectedValues.length; ++i) {
	    	expectedValues[i] = String.format("array_double_large[101][%d]", i);
	    }
	    IExpressionDMContext[] arrayChild2 = getChildren(arrayDoubleLargeChildExprDMC3, expectedValues);

	    // No more children for array_double_large[101][*]	    
	    for (IExpressionDMContext ctx : arrayChild2)
	    	getChildren(ctx, new String[0]);
	    
	    // get some parts of the children array
	    getChildren(arrayDoubleLargeChildExprDMC3, 3, 2, new String[] { "array_double_large[101][3]", "array_double_large[101][4]" });
	    getChildren(arrayDoubleLargeChildExprDMC3, 98, 3, new String[] { "array_double_large[101][98]","array_double_large[101][99]" });
    }

    /**
     * This test verifies that "small" double arrays is not affected by partitions.
     */
    @Test
    public void testSmallDoubleArray() throws Throwable {
    	MIStoppedEvent stoppedEvent = SyncUtil.runToLocation("testArrays");
    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
        // int array_double_small[11][21];
	    IExpressionDMContext arrayDoubleSmallExprDMC = SyncUtil.createExpression(frameDmc, "array_double_small");
	    
	    assertChildrenCount(arrayDoubleSmallExprDMC, 11);

	    // get all children of array_double_small
	    String[] expectedValues = new String[11];
	    for (int i = 0; i < expectedValues.length; ++i) {
	    	expectedValues[i] = String.format("array_double_small[%d]", i);
	    }
	    IExpressionDMContext[] arrayDoubleSmallChildren = getChildren(arrayDoubleSmallExprDMC, expectedValues);
	    
	    // get all children of array_double_small[3]
	    IExpressionDMContext arrayDoubleSmallChildExprDMC = arrayDoubleSmallChildren[3];
	    
	    assertChildrenCount(arrayDoubleSmallChildExprDMC, 21);

	    expectedValues = new String[21];
	    for (int i = 0; i < expectedValues.length; ++i) {
	    	expectedValues[i] = arrayDoubleSmallChildExprDMC.getExpression() + "[" + i +"]";
	    }
	    IExpressionDMContext[] arrayDoubleSmallGrandChildren = getChildren(arrayDoubleSmallChildExprDMC, expectedValues);

	    // No more children for array_double_small[3][*]	    
	    for (IExpressionDMContext ctx : arrayDoubleSmallGrandChildren)
	    	getChildren(ctx, new String[0]);
	    
	    // get some parts of the children array
	    getChildren(arrayDoubleSmallChildExprDMC, 3, 2, new String[] { "array_double_small[3][3]", "array_double_small[3][4]" });
	    getChildren(arrayDoubleSmallChildExprDMC, 19, 3, new String[] { "array_double_small[3][19]","array_double_small[3][20]" });
    }
    
    /**
     * This test verifies that there is no RTTI support before GDB 7.5.
     */
    @Test
    public void testRTTI() throws Throwable {
    	assumeGdbVersionNot(ITestConstants.SUFFIX_GDB_6_7); // crashing
    	assumeGdbVersionLowerThen(ITestConstants.SUFFIX_GDB_7_5);
    	SyncUtil.runToLocation("testRTTI");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
        // The expression we will follow as it changes types: derived.ptr
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "derived.ptr");
	    
	    // Now, the expression should be type VirtualBase
	    getExpressionType(exprDmc, "VirtualBase *");
	    assertChildrenCount(exprDmc, 2);
	    // get all children
	    String[] expectedValues = new String[2];
	    expectedValues[0] = "a";
	    expectedValues[1] = "b";
	    getChildren(exprDmc, expectedValues);
	    
	    // Make the type of our expression change
	    SyncUtil.step(1, StepType.STEP_OVER);
	    // Now, the expression should be type Derived, but GDB < 7.5 does not tell us
	    // so we should still get the base type.
	    getExpressionType(exprDmc, "VirtualBase *");
	    assertChildrenCount(exprDmc, 2);
	    // The children are also the same as before
	    getChildren(exprDmc, expectedValues);
	    
	    // Make the type of our expression change
	    SyncUtil.step(1, StepType.STEP_OVER);
	    // Now, the expression should be type OtherDerived, but GDB < 7.5 does not tell us
	    // so we should still get the base type.
	    getExpressionType(exprDmc, "VirtualBase *");
	    assertChildrenCount(exprDmc, 2);
	    // The children are also the same as before
	    getChildren(exprDmc, expectedValues);
	}

    /**
     * This test verifies that we can cast to a type and then revert.
     */
    @Test
    public void testCastToType() throws Throwable {
    	SyncUtil.runToLocation("testCasting");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "int_ptr");
	    
	    assertTrue("Expression service does not support casting", fExpService instanceof IExpressions2);
	    
	    ICastedExpressionDMContext castExprDmc = 
	    		((IExpressions2)fExpService).createCastedExpression(exprDmc, new CastInfo("char*"));
	    
	    // Check type of original expression and new casted one
	    getExpressionType(exprDmc, "int *");
	    getExpressionType(castExprDmc, "char *");
	    
	    assertChildrenCount(castExprDmc, 1);
	    // get child and its value
	    final IExpressionDMContext[] children = getChildren(exprDmc, new String[] {"*int_ptr"});
	    
    	Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(children[0], IFormattedValues.NATURAL_FORMAT), 
						new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
							@Override
							protected void handleCompleted() {
								rm.done(getData().getFormattedValue());
							}	
						});
			}
    	};
    	
        fSession.getExecutor().execute(query);
        String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals("65", value);
	    
		final IExpressionDMContext[] castChildren = getChildren(castExprDmc, new String[] {"*((char*)(int_ptr))"});
    	query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(castChildren[0], IFormattedValues.NATURAL_FORMAT), 
						new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
							@Override
							protected void handleCompleted() {
								rm.done(getData().getFormattedValue());
							}	
						});
			}
    	};        
    	fSession.getExecutor().execute(query);
        value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals("65 'A'", value);
		
		// Now check that the casted type still remembers what its original type is
		assertEquals(castExprDmc.getParents()[0], exprDmc);
    }

    /**
     * This test verifies that we can display as array and then revert.
     */
    @Test
    public void testDisplayAsArray() throws Throwable {
    	SyncUtil.runToLocation("testCasting");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "int_ptr");
	    
	    assertTrue("Expression service does not support casting", fExpService instanceof IExpressions2);
	    
	    // Display as an array of 2 elements, starting at index 1
	    ICastedExpressionDMContext castExprDmc = 
	    		((IExpressions2)fExpService).createCastedExpression(exprDmc, new CastInfo(1,2));
	    
	    // Check type of original expression and new casted one
	    getExpressionType(exprDmc, "int *");
	    getExpressionType(castExprDmc, "int [2]");
	    
	    assertChildrenCount(castExprDmc, 2);
	    // get children and their values
	    final IExpressionDMContext[] children = getChildren(castExprDmc, new String[] {"int_ptr[1]", "int_ptr[2]"});
	    String[] expectedValues = new String[] {"1094861636", "1162233672"};
	    for (int i = 0; i<children.length;i++) {
	    	final IExpressionDMContext child = children[i];
	    	Query<String> query = new Query<String>() {
	    		@Override
	    		protected void execute(final DataRequestMonitor<String> rm) {
	    			fExpService.getFormattedExpressionValue(
	    					fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
	    					new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
	    						@Override
	    						protected void handleCompleted() {
	    							rm.done(getData().getFormattedValue());
	    						}	
	    					});
	    		}
	    	};

	    	fSession.getExecutor().execute(query);
	    	String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	    	assertEquals(expectedValues[i], value);
	    }
	    
		
		// Now check that the casted type still remembers what its original type is
		assertEquals(castExprDmc.getParents()[0], exprDmc);
    }

    /**
     * This test verifies that we can display as array and cast to a type together
     *  and then revert.
     */
    @Test
    public void testDisplayAsArrayAndCastToType() throws Throwable {
    	SyncUtil.runToLocation("testCasting");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "int_ptr");
	    
	    assertTrue("Expression service does not support casting", fExpService instanceof IExpressions2);

	    // We create the casted type and the displaying as an array in a single request.  This is because
	    // that is the way the UI does it.  Furthermore, the service handles the cast first, then the
	    // array, which is why our array of 2 ints becomes 8 chars, and then we only look at 4 of them
	    // starting at index 4.
	    ICastedExpressionDMContext castExprDmc = 
	    		((IExpressions2)fExpService).createCastedExpression(exprDmc, new CastInfo("char*", 4,4));
	    
	    getExpressionType(castExprDmc, "char [4]");
	    
	    assertChildrenCount(castExprDmc, 4);
	    // get children and their values
	    // The array index starts at 0 again because the cast to char[] creates a new array
	    final IExpressionDMContext[] children = 
	    		getChildren(castExprDmc, new String[] {"int_ptr[4]", "int_ptr[5]", "int_ptr[6]", "int_ptr[7]"});
	    String[] expectedValues = new String[] { "68 'D'", "67 'C'", "66 'B'", "65 'A'"};
	    for (int i = 0; i<children.length;i++) {
	    	final IExpressionDMContext child = children[i];
	    	
	    	getExpressionType(child, "char");
	    	
	    	Query<String> query = new Query<String>() {
	    		@Override
	    		protected void execute(final DataRequestMonitor<String> rm) {
	    			fExpService.getFormattedExpressionValue(
	    					fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
	    					new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
	    						@Override
	    						protected void handleCompleted() {
	    							rm.done(getData().getFormattedValue());
	    						}	
	    					});
	    		}
	    	};

	    	fSession.getExecutor().execute(query);
	    	String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	    	assertEquals(expectedValues[i], value);
	    }
	    
		
		// Now check that the casted type still remembers what its original type is
		assertEquals(castExprDmc.getParents()[0], exprDmc);
    }

    /**
     * This test verifies that we can cast an array to a different type and then revert.
     */
    @Test
    public void testCastToTypeOfArray() throws Throwable {
    	SyncUtil.runToLocation("testCasting");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "array_small");
	    
	    assertTrue("Expression service does not support casting", fExpService instanceof IExpressions2);

	    ICastedExpressionDMContext castExprDmc = 
	    		((IExpressions2)fExpService).createCastedExpression(exprDmc, new CastInfo("char[]"));
	    
	    getExpressionType(exprDmc, "int [4]");
	    getExpressionType(castExprDmc, "char [16]");
	    
	    assertChildrenCount(castExprDmc, 16);
	    // get children and their values
	    // The array index starts at 0 again because the cast to char[] creates a new array
	    final IExpressionDMContext[] children = 
	    		getChildren(castExprDmc, new String[] {"array_small[0]", "array_small[1]", "array_small[2]", "array_small[3]",
	    											   "array_small[4]", "array_small[5]", "array_small[6]", "array_small[7]",
	    											   "array_small[8]", "array_small[9]", "array_small[10]", "array_small[11]",
	    											   "array_small[12]", "array_small[13]", "array_small[14]", "array_small[15]"});
	    // Only check elements 4 through 7 for simplicity
	    String[] expectedValues = new String[] { "68 'D'", "67 'C'", "66 'B'", "65 'A'"};
	    for (int i = 4; i<8;i++) {
	    	final IExpressionDMContext child = children[i];
	    	
	    	getExpressionType(child, "char");
	    	
	    	Query<String> query = new Query<String>() {
	    		@Override
	    		protected void execute(final DataRequestMonitor<String> rm) {
	    			fExpService.getFormattedExpressionValue(
	    					fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
	    					new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
	    						@Override
	    						protected void handleCompleted() {
	    							rm.done(getData().getFormattedValue());
	    						}	
	    					});
	    		}
	    	};

	    	fSession.getExecutor().execute(query);
	    	String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	    	assertEquals(expectedValues[i-4], value);
	    }
	    
		
		// Now check that the casted type still remembers what its original type is
		assertEquals(castExprDmc.getParents()[0], exprDmc);
    }
    
    /**
     * This test verifies that we can cast to a type and then revert
     * when dealing with an array with partitions.
     */
    @Test
    public void testCastToTypeWithPartition() throws Throwable {
    	SyncUtil.runToLocation("testCasting");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "array_large");
	    
	    assertTrue("Expression service does not support casting", fExpService instanceof IExpressions2);
	    
	    ICastedExpressionDMContext castExprDmc = 
	    		((IExpressions2)fExpService).createCastedExpression(exprDmc, new CastInfo("char[]"));
	    
	    // Check type of original expression and new casted one
	    getExpressionType(exprDmc, "int [111]");
	    getExpressionType(castExprDmc, "char [444]");
	    
	    // get the 5 partition children
	    assertChildrenCount(castExprDmc, 5);
	    IExpressionDMContext[] children = getChildren(castExprDmc, new String[] {"*((((char[])(array_large)))+0)@100", "*((((char[])(array_large)))+100)@100",
	    																	     "*((((char[])(array_large)))+200)@100", "*((((char[])(array_large)))+300)@100",
	    																		 "*((((char[])(array_large)))+400)@44" });

	    // Now make sure the children of the partitions have the proper casting
	    final String[] expectedChildren = new String[100];
	    for (int i=0; i < expectedChildren.length; i++) {
	    	expectedChildren[i] = String.format("array_large[%d]", i);
	    }
	    IExpressionDMContext[] castedChildren = getChildren(children[0], expectedChildren);
	    assertEquals(100, castedChildren.length);
	    
	    // Check the type and value of a few of the first children
	    final String[] expectedValues = new String[] { "65 'A'", "0 '\\0'", "0 '\\0'", "0 '\\0'", "68 'D'", "67 'C'", "66 'B'", "65 'A'" };
	    for (int i = 0; i < expectedValues.length; i++) {
	    	final IExpressionDMContext child = castedChildren[i];
		    getExpressionType(child, "char");

	    	Query<String> query = new Query<String>() {
	    		@Override
	    		protected void execute(final DataRequestMonitor<String> rm) {
	    			fExpService.getFormattedExpressionValue(
	    					fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
	    					new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
	    						@Override
	    						protected void handleCompleted() {
	    							rm.done(getData().getFormattedValue());
	    						}	
	    					});
	    		}
	    	};

	    	fSession.getExecutor().execute(query);
	    	String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	    	assertEquals(expectedValues[i], value);
	    }	    
		
		// Now check that the casted type still remembers what its original type is
		assertEquals(castExprDmc.getParents()[0], exprDmc);
    }

    /**
     * This test verifies that we can display as array and then revert
     * when dealing with an array with partitions.
     */
    @Test
    public void testDisplayAsArrayWithPartition() throws Throwable {
    	SyncUtil.runToLocation("testCasting");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
        // The expression we will cast from int to char
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "array_large");
	    
	    assertTrue("Expression service does not support casting", fExpService instanceof IExpressions2);
	    
	    // Display as an array of 101 elements, starting at index 1 (we need at least 101 elements to get partitions)
	    ICastedExpressionDMContext castExprDmc = 
	    		((IExpressions2)fExpService).createCastedExpression(exprDmc, new CastInfo(1, 101));
	    
	    // Check type of original expression and new casted one
	    getExpressionType(exprDmc, "int [111]");
	    getExpressionType(castExprDmc, "int [101]");
	    
	    // Two partitions as children
	    assertChildrenCount(castExprDmc, 2);
	    IExpressionDMContext[] children = getChildren(castExprDmc, new String[] {"*(((*((array_large)+1)@101))+0)@100", "*(((*((array_large)+1)@101))+100)@1" });

	    assertTrue("Should have seen the child as a partition", children[0] instanceof IIndexedPartitionDMContext);
	    assertEquals("Wrong start index for partition", 0, ((IIndexedPartitionDMContext)children[0]).getIndex());
	    assertEquals("Wrong partition length", 100, ((IIndexedPartitionDMContext)children[0]).getLength());
	    assertTrue("Should have seen the child as a partition", children[1] instanceof IIndexedPartitionDMContext);
	    assertEquals("Wrong start index for partition", 100, ((IIndexedPartitionDMContext)children[1]).getIndex());
	    assertEquals("Wrong partition length", 1, ((IIndexedPartitionDMContext)children[1]).getLength());
	    
	    // Now make sure the children of the partitions have the proper casting and start at the proper index
	    final String[] expectedChildren = new String[100];
	    for (int i=0; i < expectedChildren.length; i++) {
	    	expectedChildren[i] = String.format("array_large[%d]", i+1);
	    }
	    IExpressionDMContext[] castedChildren = getChildren(children[0], expectedChildren);
	    assertEquals(100, castedChildren.length);
	    
	    // Check the type and value of a few of the first children
	    final String[] expectedValues = new String[] { "1094861636", "1162233672" };
	    for (int i = 0; i < expectedValues.length; i++) {
	    	final IExpressionDMContext child = castedChildren[i];
		    getExpressionType(child, "int");

	    	Query<String> query = new Query<String>() {
	    		@Override
	    		protected void execute(final DataRequestMonitor<String> rm) {
	    			fExpService.getFormattedExpressionValue(
	    					fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
	    					new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
	    						@Override
	    						protected void handleCompleted() {
	    							rm.done(getData().getFormattedValue());
	    						}	
	    					});
	    		}
	    	};

	    	fSession.getExecutor().execute(query);
	    	String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	    	assertEquals(expectedValues[i], value);
	    }	    
		
		// Now check that the casted type still remembers what its original type is
		assertEquals(castExprDmc.getParents()[0], exprDmc);
    }

    /**
     * This test verifies that we can display as array and cast to a type together
     * and then revert when dealing with an array with partitions.
     */
    @Test
    public void testDisplayAsArrayAndCastToTypeWithPartition() throws Throwable {
    	SyncUtil.runToLocation("testCasting");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);    	
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
	    IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "array_large");
	    
	    assertTrue("Expression service does not support casting", fExpService instanceof IExpressions2);
	    
	    ICastedExpressionDMContext castExprDmc = 
	    		((IExpressions2)fExpService).createCastedExpression(exprDmc, new CastInfo("char[]", 4, 101));
	    
	    // Check type of original expression and new casted one
	    getExpressionType(exprDmc, "int [111]");
	    getExpressionType(castExprDmc, "char [101]");
	    
	    // get the 5 partition children
	    assertChildrenCount(castExprDmc, 2);
	    IExpressionDMContext[] children = getChildren(castExprDmc, new String[] {"*(((*(((char[])(array_large))+4)@101))+0)@100", "*(((*(((char[])(array_large))+4)@101))+100)@1"});

	    assertTrue("Should have seen the child as a partition", children[0] instanceof IIndexedPartitionDMContext);
	    assertEquals("Wrong start index for partition", 0, ((IIndexedPartitionDMContext)children[0]).getIndex());
	    assertEquals("Wrong partition length", 100, ((IIndexedPartitionDMContext)children[0]).getLength());
	    assertTrue("Should have seen the child as a partition", children[1] instanceof IIndexedPartitionDMContext);
	    assertEquals("Wrong start index for partition", 100, ((IIndexedPartitionDMContext)children[1]).getIndex());
	    assertEquals("Wrong partition length", 1, ((IIndexedPartitionDMContext)children[1]).getLength());

	    // Now make sure the children of the partitions have the proper casting
	    final String[] expectedChildren = new String[100];
	    for (int i=0; i < expectedChildren.length; i++) {
	    	expectedChildren[i] = String.format("array_large[%d]", i+4);
	    }
	    IExpressionDMContext[] castedChildren = getChildren(children[0], expectedChildren);
	    assertEquals(100, castedChildren.length);
	    
	    // Check the type and value of a few of the first children
	    final String[] expectedValues = new String[] { "68 'D'", "67 'C'", "66 'B'", "65 'A'" };
	    for (int i = 0; i < expectedValues.length; i++) {
	    	final IExpressionDMContext child = castedChildren[i];
		    getExpressionType(child, "char");

	    	Query<String> query = new Query<String>() {
	    		@Override
	    		protected void execute(final DataRequestMonitor<String> rm) {
	    			fExpService.getFormattedExpressionValue(
	    					fExpService.getFormattedValueContext(child, IFormattedValues.NATURAL_FORMAT), 
	    					new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
	    						@Override
	    						protected void handleCompleted() {
	    							rm.done(getData().getFormattedValue());
	    						}	
	    					});
	    		}
	    	};

	    	fSession.getExecutor().execute(query);
	    	String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
	    	assertEquals(expectedValues[i], value);
	    }	    
		
		// Now check that the casted type still remembers what its original type is
		assertEquals(castExprDmc.getParents()[0], exprDmc);		
    }
    
    /**
     * This test verifies that we display the simple return value of a method after
     * a step-return operation, but only for the first stack frame.
     */
    @Test
    public void testDisplaySimpleReturnValueForStepReturn() throws Throwable {
    	SyncUtil.runToLocation("testSimpleReturn");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_RETURN);
    	
    	// Check the return value is shown when looking at the first frame
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	IVariableDMData[] result = SyncUtil.getLocals(frameDmc);
    	
    	assertEquals(3, result.length);  // Two variables and one return value

    	// Return value
    	assertEquals("$2", result[0].getName());
    	assertEquals("6", result[0].getValue());
    	// first variable
    	assertEquals("a",  result[1].getName());
    	assertEquals("10", result[1].getValue());
    	// Second variable
    	assertEquals("b", result[2].getName());
    	assertEquals("false", result[2].getValue());
    	
    	// Now check how the return value will be displayed to the user
    	final IExpressionDMContext returnExprDmc = SyncUtil.createExpression(frameDmc, "$2");
		Query<IExpressionDMData> query = new Query<IExpressionDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMData> rm) {
				fExpService.getExpressionData(returnExprDmc, rm);
			}
		};
		fSession.getExecutor().execute(query);
		IExpressionDMData data = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals("testSimpleReturn() returned", data.getName());

		// Now check the actual value using the expression service
		String value = SyncUtil.getExpressionValue(returnExprDmc, IFormattedValues.DECIMAL_FORMAT);
		assertEquals("6", value);
		
    	// Now make sure we don't show the return value for another frame
		final IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 1);
    	result = SyncUtil.getLocals(frameDmc2);

    	// only one variable
    	assertEquals(1, result.length);
    	assertEquals("b",  result[0].getName());
    }
    
    /**
     * This test verifies that we display the complex return value of a method after
     * a step-return operation, but only for the first stack frame.
     */
    @Test
    public void testDisplayComplexReturnValueForStepReturn() throws Throwable {
    	SyncUtil.runToLocation("testComplexReturn");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_RETURN);
    	
    	// Check the return value is show when looking at the first frame
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	IVariableDMData[] result = SyncUtil.getLocals(frameDmc);
    	
    	assertEquals(3, result.length);  // Two variables and one return value

    	// Return value
    	assertEquals("$2", result[0].getName());

    	// first variable
    	assertEquals("a",  result[1].getName());
    	assertEquals("10", result[1].getValue());
    	// Second variable
    	assertEquals("b", result[2].getName());
    	assertEquals("false", result[2].getValue());

    	// Now check how the return value will be displayed to the user
    	final IExpressionDMContext returnExprDmc = SyncUtil.createExpression(frameDmc, "$2");
    	Query<IExpressionDMData> query = new Query<IExpressionDMData>() {
			@Override
			protected void execute(final DataRequestMonitor<IExpressionDMData> rm) {
				fExpService.getExpressionData(returnExprDmc, rm);
			}
		};
		fSession.getExecutor().execute(query);
		IExpressionDMData data = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals("testComplexReturn() returned", data.getName());

		// Now check the content of the complex return expression
		doTestChildren(returnExprDmc);

    	// Now make sure we don't show the return value for another frame
		IFrameDMContext frameDmc2 = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 1);
    	result = SyncUtil.getLocals(frameDmc2);

    	// only one variable
    	assertEquals(1, result.length);
    	assertEquals("b",  result[0].getName());
    }

    /**
     * This test verifies that we properly display variables after a step-return operation
     * from a method returning void.
     */
    @Test
    public void testNoReturnValueForEmptyStepReturn() throws Throwable {
    	SyncUtil.runToLocation("noReturnValue");    	
    	MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_RETURN);
    	
    	// Check no return value is shown when looking at the first frame
        final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
    	IVariableDMData[] result = SyncUtil.getLocals(frameDmc);
    	
    	assertEquals(2, result.length);  // Two variables and one return value

    	// first variable
    	assertEquals("a",  result[0].getName());
    	assertEquals("10", result[0].getValue());
    	// Second variable
    	assertEquals("b", result[1].getName());
    	assertEquals("false", result[1].getValue());
    }

    /**
     * This tests verifies that we can obtain a child even though
     * is was already created directly.
     */
    @Test
    public void testExistingChild() throws Throwable {
        SyncUtil.runToLocation("testExistingChild");
    	MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
        final String PARENT_EXPR = "b";
        final String CHILD_EXPR = "((b).d)";
        final String CHILD__REL_EXPR = "d";

    	// Fetch the child directly
        final IExpressionDMContext childDmc = SyncUtil.createExpression(frameDmc, CHILD_EXPR);
    	Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(childDmc, IFormattedValues.NATURAL_FORMAT), 
						new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
							@Override
							protected void handleSuccess() {
								rm.done(getData().getFormattedValue());
							}	
						});
			}
    	};
    	
        fSession.getExecutor().execute(query);
        String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals("8", value);

    	// Now fetch the child through its parent
        final IExpressionDMContext parentDmc = SyncUtil.createExpression(frameDmc, PARENT_EXPR);
    	query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
    			fExpService.getSubExpressions(
    					parentDmc, 
    					new ImmediateDataRequestMonitor<IExpressionDMContext[]>(rm) {
    						@Override
    						protected void handleSuccess() {
    							if (getData().length != 2) {
    					            rm.done(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 
    					            		"Wrong number for children.  Expecting 2 but got " + getData().length, null));
    								return;
    							}
    							
    							MIExpressionDMC firstChildContext = (MIExpressionDMC)getData()[0];
    							if (firstChildContext.getExpression().equals(CHILD_EXPR) == false) {
    					            rm.done(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 
    					            		"Got wrong first child. Expected " + CHILD_EXPR + " but got " +  firstChildContext.getExpression(), null));
    								return;
    							}

    							if (firstChildContext.getRelativeExpression().equals(CHILD__REL_EXPR) == false) {
    					            rm.done(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 
    					            		"Got wrong relative expression. Expected " + CHILD__REL_EXPR + " but got " +  firstChildContext.getRelativeExpression(), null));
    								return;
    							}
    							
    							fExpService.getFormattedExpressionValue(
    									fExpService.getFormattedValueContext(firstChildContext, IFormattedValues.NATURAL_FORMAT), 
    									new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
    										@Override
    										protected void handleSuccess() {
    											rm.done(getData().getFormattedValue());
    										}	
    									});

    						}
    					});
			}
    	};
    	
        fSession.getExecutor().execute(query);
        value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals("8", value);
    }

    /**
     * This tests verifies that we can manually create a child of an expression
     * after that child was automatically created through the parent.
     * This case happens when selecting a child of an expression and using "Watch"
     * to create an expression automatically.
     */
    @Test
    public void testExplicitChildCreation() throws Throwable {
        SyncUtil.runToLocation("testExistingChild");
    	MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
        IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);
        
        final String PARENT_EXPR = "b";
        final String CHILD_EXPR = "((b).d)";
        final String CHILD__REL_EXPR = "d";

    	// First fetch the child through its parent
        final IExpressionDMContext parentDmc = SyncUtil.createExpression(frameDmc, PARENT_EXPR);
    	Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
    			fExpService.getSubExpressions(
    					parentDmc, 
    					new ImmediateDataRequestMonitor<IExpressionDMContext[]>(rm) {
    						@Override
    						protected void handleSuccess() {
    							if (getData().length != 2) {
    					            rm.done(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 
    					            		"Wrong number for children.  Expecting 2 but got " + getData().length, null));
    								return;
    							}
    							
    							MIExpressionDMC firstChildContext = (MIExpressionDMC)getData()[0];
    							if (firstChildContext.getExpression().equals(CHILD_EXPR) == false) {
    					            rm.done(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 
    					            		"Got wrong first child. Expected " + CHILD_EXPR + " but got " +  firstChildContext.getExpression(), null));
    								return;
    							}

    							if (firstChildContext.getRelativeExpression().equals(CHILD__REL_EXPR) == false) {
    					            rm.done(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID, 
    					            		"Got wrong relative expression. Expected " + CHILD__REL_EXPR + " but got " +  firstChildContext.getRelativeExpression(), null));
    								return;
    							}

    							fExpService.getFormattedExpressionValue(
    									fExpService.getFormattedValueContext(firstChildContext, IFormattedValues.NATURAL_FORMAT), 
    									new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
    										@Override
    										protected void handleSuccess() {
    											rm.done(getData().getFormattedValue());
    										}	
    									});

    						}
    					});
			}
    	};
    	
        fSession.getExecutor().execute(query);
        String value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals("8", value);

    	// Now access the child directly
        final IExpressionDMContext childDmc = SyncUtil.createExpression(frameDmc, CHILD_EXPR);
    	query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				fExpService.getFormattedExpressionValue(
						fExpService.getFormattedValueContext(childDmc, IFormattedValues.NATURAL_FORMAT), 
						new ImmediateDataRequestMonitor<FormattedValueDMData>(rm) {
							@Override
							protected void handleSuccess() {
								rm.done(getData().getFormattedValue());
							}	
						});
			}
    	};
    	
        fSession.getExecutor().execute(query);
        value = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals("8", value);
    }

	protected void assertChildrenCount(final IExpressionDMContext parentDmc,
			final int expectedCount) throws Throwable {
		Query<Integer> query = new Query<Integer>() {

			@Override
			protected void execute(DataRequestMonitor<Integer> rm) {
				fExpService.getSubExpressionCount(parentDmc, rm);
			}
		};

		fExpService.getExecutor().submit(query);

		int count = query.get().intValue();

		assertThat(count, is(expectedCount));
	}

    protected String getExpressionType(final IExpressionDMContext exprDmc, final String expectedType) throws Throwable {
    	
    	Query<String> query = new Query<String>() {
			@Override
			protected void execute(final DataRequestMonitor<String> rm) {
				fExpService.getExpressionData(
						exprDmc, 
						new ImmediateDataRequestMonitor<IExpressionDMData>(rm) {
							@Override
							protected void handleCompleted() {
								rm.done(getData().getTypeName());
							}	
						});
			}
    	};
    	
        fSession.getExecutor().execute(query);
        String type = query.get(TestsPlugin.massageTimeout(500), TimeUnit.MILLISECONDS);
		assertEquals(expectedType, type);
		return type;
    }
    
	// Slight change in GDB output to fix a bug, so we must change the test a
	// little
	// Bug 320277
	@Test
	public void testDeleteChildren_7_3() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_3);
		SyncUtil.runToLocation("testDeleteChildren");
		MIStoppedEvent stoppedEvent = SyncUtil.step(1, StepType.STEP_OVER);
		final IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		final AsyncCompletionWaitor wait = new AsyncCompletionWaitor();

		fExpService.getExecutor().submit(new Runnable() {
			@Override
			public void run() {

				// First create the var object and all its children
				IExpressionDMContext parentDmc = fExpService.createExpression(frameDmc, "f");

				fExpService.getSubExpressions(parentDmc,
						new DataRequestMonitor<IExpressionDMContext[]>(fExpService.getExecutor(), null) {
							@Override
							protected void handleCompleted() {
								if (!isSuccess()) {
									wait.waitFinished(getStatus());
								} else {
									if (getData().length != 5) {
										wait.waitFinished(new Status(IStatus.ERROR, TestsPlugin.PLUGIN_ID,
												"Failed getting children; expecting 5 got " + getData().length, null));
									} else {
										String childStr = "((class bar) f)";
										if (!getData()[0].getExpression().equals(childStr)) {
											wait.waitFinished(new Status(
													IStatus.ERROR, TestsPlugin.PLUGIN_ID, "Got child "
															+ getData()[0].getExpression() + " instead of " + childStr,
													null));
										} else {
											// Now list the children of the
											// first element
											fExpService.getSubExpressions(getData()[0],
													new DataRequestMonitor<IExpressionDMContext[]>(
															fExpService.getExecutor(), null) {
														@Override
														protected void handleCompleted() {
															if (!isSuccess()) {
																wait.waitFinished(getStatus());
															} else {
																if (getData().length != 2) {
																	wait.waitFinished(new Status(IStatus.ERROR,
																			TestsPlugin.PLUGIN_ID,
																			"Failed getting children; expecting 2 got "
																					+ getData().length,
																			null));
																} else {
																	String childStr = "((((class bar) f)).d)";
																	if (!getData()[0].getExpression()
																			.equals(childStr)) {
																		wait.waitFinished(new Status(IStatus.ERROR,
																				TestsPlugin.PLUGIN_ID,
																				"Got child "
																						+ getData()[0].getExpression()
																						+ " instead of " + childStr,
																				null));
																	} else {
																		wait.setReturnInfo(getData()[0]);
																		wait.waitFinished();
																	}
																}
															}
														}
													});
										}
									}
								}
							}
						});
			}
		});

		wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
		final IExpressionDMContext deletedChildDmc = (IExpressionDMContext) wait.getReturnInfo();

		wait.waitReset();

		fExpService.getExecutor().submit(new Runnable() {
			@Override
			public void run() {

				// Now create more than 1000 expressions to trigger the deletion
				// of the children
				// that were created above
				for (int i = 0; i < 1100; i++) {
					IExpressionDMContext dmc = fExpService.createExpression(frameDmc, "a[" + i + "]");

					wait.increment();
					fExpService.getExpressionData(dmc,
							new DataRequestMonitor<IExpressionDMData>(fExpService.getExecutor(), null) {
								@Override
								protected void handleCompleted() {
									if (!isSuccess()) {
										wait.waitFinished(getStatus());
									} else {
										wait.waitFinished();
									}
								}
							});
				}
			}
		});

		wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
		wait.waitReset();

		fExpService.getExecutor().submit(new Runnable() {
			@Override
			public void run() {

				// Evaluate the expression of a child that we know is deleted to
				// make sure
				// the expression service can handle that
				fExpService.getExpressionData(deletedChildDmc,
						new DataRequestMonitor<IExpressionDMData>(fExpService.getExecutor(), null) {
							@Override
							protected void handleCompleted() {
								if (!isSuccess()) {
									wait.waitFinished(getStatus());
								} else {
									wait.waitFinished();
								}
							}
						});
			}
		});

		wait.waitUntilDone(AsyncCompletionWaitor.WAIT_FOREVER);
		assertTrue(wait.getMessage(), wait.isOK());
		wait.waitReset();

	}

	/**
	 * This test verifies that there is proper RTTI support starting with GDB
	 * 7.5.
	 */
	@Test
	public void testRTTI_7_5() throws Throwable {
		assumeGdbVersionAtLeast(ITestConstants.SUFFIX_GDB_7_5);
		SyncUtil.runToLocation("testRTTI");
		MIStoppedEvent stoppedEvent = SyncUtil.step(3, StepType.STEP_OVER);
		IFrameDMContext frameDmc = SyncUtil.getStackFrame(stoppedEvent.getDMContext(), 0);

		// The expression we will follow as it changes types: derived.ptr
		IExpressionDMContext exprDmc = SyncUtil.createExpression(frameDmc, "derived.ptr");

		// Now, the expression should be type VirtualBase
		getExpressionType(exprDmc, "VirtualBase *");
		assertChildrenCount(exprDmc, 2);
		// get all children
		String[] expectedValues = new String[2];
		expectedValues[0] = "a";
		expectedValues[1] = "b";
		getChildren(exprDmc, expectedValues);

		// Make the type of our expression change
		SyncUtil.step(1, StepType.STEP_OVER);
		// Now, the expression should be type Derived
		getExpressionType(exprDmc, "Derived *");
		assertChildrenCount(exprDmc, 5);
		// get all children
		expectedValues = new String[5];
		expectedValues[0] = "VirtualBase";
		expectedValues[1] = "c";
		expectedValues[2] = "ptr";
		expectedValues[3] = "d";
		expectedValues[4] = "e";
		getChildren(exprDmc, expectedValues);

		// Make the type of our expression change
		SyncUtil.step(1, StepType.STEP_OVER);
		// Now, the expression should be type OtherDerived
		getExpressionType(exprDmc, "OtherDerived *");
		assertChildrenCount(exprDmc, 4);
		// get all children
		expectedValues = new String[4];
		expectedValues[0] = "VirtualBase";
		expectedValues[1] = "d";
		expectedValues[2] = "c";
		expectedValues[3] = "f";
		getChildren(exprDmc, expectedValues);
	}

}
