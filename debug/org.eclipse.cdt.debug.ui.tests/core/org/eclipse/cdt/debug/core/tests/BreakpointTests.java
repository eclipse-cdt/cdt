package org.eclipse.cdt.debug.core.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002. All Rights Reserved.
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointManager;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.testplugin.CDebugHelper;
import org.eclipse.cdt.debug.testplugin.CProjectHelper;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Peter Graves
 * 
 * This file contains a set of generic tests for the CDI break point interfaces.
 * It will currenly use the mi implementation.
 *  
 */
public class BreakpointTests extends TestCase {

	IWorkspace workspace;
	IWorkspaceRoot root;
	static ICProject testProject = null;
	NullProgressMonitor monitor;
	static ICDISession session = null;
	static ICDITarget targets[] = null;

	/**
	 * Constructor for BreakpointTests
	 * 
	 * @param name
	 */
	public BreakpointTests(String name) {
		super(name);
		/***********************************************************************
		 * The tests assume that they have a working workspace and workspace
		 * root object to use to create projects/files in, so we need to get
		 * them setup first.
		 */
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if (workspace == null)
			fail("Workspace was not setup");
		if (root == null)
			fail("Workspace root was not setup");

	}

	/**
	 * Sets up the test fixture.
	 * 
	 * Called before every test case method.
	 * 
	 * Example code test the packages in the project
	 * "com.qnx.tools.ide.cdt.core"
	 */
	protected static void oneTimeSetUp() throws CoreException, InvocationTargetException, IOException {
		ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);
		/***********************************************************************
		 * Create a new project and import the test source.
		 */
		Path imputFile = new Path("resources/debugTest.zip");
		testProject = CProjectHelper.createCProjectWithImport("filetest", imputFile);
		if (testProject == null)
			fail("Unable to create project");
		/* Build the test project.. */

		testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
	}

	/**
	 * Tears down the test fixture.
	 * 
	 * Called after every test case method.
	 */
	protected void tearDown() throws CoreException {
		if (targets != null) {
			try {
				targets[0].terminate();
				targets = null;
			} catch (CDIException e) {
			}
		}
		if (session != null) {
			try {
				session.terminate();
				session = null;
			} catch (CDIException e) {
			}
		}
	}

	/**
	 * Tears down the test fixture.
	 * 
	 * Called after every test case method.
	 */
	protected static void oneTimeTearDown() throws CoreException {
		if (targets != null) {
			try {
				targets[0].terminate();
			} catch (CDIException e) {
			}
		}
		if (session != null) {
			try {
				session.terminate();
			} catch (CDIException e) {
			}
		}
		CProjectHelper.delete(testProject);

	}

	public static Test suite() {
		TestSuite suite = new TestSuite(BreakpointTests.class);
		/***********************************************************************
		 * Create a wrapper suite around the test suite we created above to
		 * allow us to only do the general setup once for all the tests. This is
		 * needed because the creation of the source and target projects takes a
		 * long time and we really only need to do it once. We could do the
		 * setup in the constructor, but we need to be able to remove everything
		 * when we are done.
		 */
		TestSetup wrapper = new TestSetup(suite) {

			protected void setUp() throws FileNotFoundException, IOException, InterruptedException, InvocationTargetException,
					CoreException {
				oneTimeSetUp();
			}

			protected void tearDown() throws FileNotFoundException, IOException, CoreException {
				oneTimeTearDown();
			}
		};
		return (wrapper);
	}

	/***************************************************************************
	 * A couple tests to make sure setting breakpoints on functions works as
	 * expected.
	 */
	public void testFunctionBreak() throws CoreException, MIException, IOException, CDIException, InterruptedException {
		ICDISession session;
		ICDIBreakpointManager breaks;
		ICDILocation location;
		boolean caught = false;
		session = CDebugHelper.createSession("main", testProject);
		assertNotNull(session);
		breaks = session.getBreakpointManager();
		assertNotNull(breaks);

		/***********************************************************************
		 * Create a break point on a generic function
		 **********************************************************************/

		location = breaks.createLocation(null, "func1", 0);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);

		/***********************************************************************
		 * Create a break point on main
		 **********************************************************************/

		location = breaks.createLocation(null, "main", 0);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);

		/***********************************************************************
		 * Try to create a break point on a function name that does not exist We
		 * expect that this will cause the setLocationBreakpoint to throw a
		 * CDIException
		 **********************************************************************/

		location = breaks.createLocation(null, "badname", 0);
		assertNotNull(location);
		try {
			breaks.setLocationBreakpoint(0, location, null, null);
		} catch (CDIException e) {
			caught = true;
		}
		assertTrue(caught);

		breaks.deleteAllBreakpoints();

		/***********************************************************************
		 * Create a break point on a generic function and see if it will get hit
		 * and stop program execution.
		 **********************************************************************/

		location = breaks.createLocation(null, "func1", 0);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);
		targets = session.getTargets();
		/*
		 * We better only have one target connected to this session or something
		 * is not right...
		 */
		assertTrue(targets.length == 1);
		/*
		 * Resume the target, this should cause it to run till it hits the
		 * breakpoint
		 */
		targets[0].resume();
		/**
		 * Give the process up to 10 seconds to become either terminated or
		 * suspended. It sould hit the breakponint almost immediatly so we
		 * should only sleep for max 100 ms
		 */
		for (int x = 0; x < 100; x++) {
			if (targets[0].isTerminated() || targets[0].isSuspended())
				break;
			Thread.sleep(100);
		}
		assertTrue(targets[0].isSuspended());
		location = targets[0].getCurrentThread().getStackFrames()[0].getLocation();
		assertTrue(location.getLineNumber() == 6);
		assertTrue(location.getFunction().equals("func1"));
		assertTrue(location.getFile().equals("main.c"));

		/* clean up the session */
		targets[0].terminate();
		int x = 0;
		while ((!targets[0].isTerminated()) && (x < 30)) {
			Thread.sleep(100);
		}
		if (!targets[0].isTerminated())
			targets[0].terminate();
		session.terminate();
		session = null;
		targets = null;

	}

	/***************************************************************************
	 * A couple tests to make sure setting breakpoints on line numbers works as
	 * expected.
	 */
	public void testLineBreak() throws CoreException, MIException, IOException, CDIException, InterruptedException {
		ICDIBreakpointManager breaks;
		ICDILocation location;
		boolean caught = false;
		session = CDebugHelper.createSession("main", testProject);
		assertNotNull(session);
		breaks = session.getBreakpointManager();
		assertNotNull(breaks);

		/***********************************************************************
		 * Create a break point in a generic function
		 **********************************************************************/
		location = breaks.createLocation("main.c", null, 7);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);

		/***********************************************************************
		 * Create a break point in main
		 **********************************************************************/
		location = breaks.createLocation("main.c", null, 18);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);

		/***********************************************************************
		 * Try to create a break point on a line that does not exist We expect
		 * that this will cause the setLocationBreakpoint to throw a
		 * CDIException
		 **********************************************************************/

		location = breaks.createLocation("main.c", null, 30);
		assertNotNull(location);
		try {
			breaks.setLocationBreakpoint(0, location, null, null);
		} catch (CDIException e) {
			caught = true;
		}
		assertTrue(caught);

		caught = false;
		/***********************************************************************
		 * Try to create a break point on a line that does not have code on it
		 **********************************************************************/

		location = breaks.createLocation("main.c", null, 11);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);

		/***********************************************************************
		 * Create a break point in a generic function without passing the source
		 * file name. At the time of writing this would just silently fail, so
		 * to make sure it works, we will do it once with a valid line number
		 * and once with an invalid line number, and the first should always
		 * succeed and the second should always throw an exception.
		 **********************************************************************/
		location = breaks.createLocation(null, null, 7);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);
		caught = false;
		location = breaks.createLocation(null, null, 30);
		assertNotNull(location);
		try {
			breaks.setLocationBreakpoint(0, location, null, null);
		} catch (CDIException e) {
			caught = true;
		}
		assertTrue("Ignoring line numbers with no file specified?", caught);

		breaks.deleteAllBreakpoints();

		/***********************************************************************
		 * Create a break point on a line number and see if it will get hit and
		 * stop program execution.
		 **********************************************************************/

		location = breaks.createLocation(null, null, 7);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);
		targets = session.getTargets();
		/*
		 * We better only have one target connected to this session or something
		 * is not right...
		 */
		assertTrue(targets.length == 1);
		/*
		 * Resume the target, this should cause it to run till it hits the
		 * breakpoint
		 */
		targets[0].resume();
		/**
		 * Give the process up to 10 seconds to become either terminated or
		 * suspended. It sould hit the breakponint almost immediatly so we
		 * should only sleep for max 100 ms
		 */
		for (int x = 0; x < 100; x++) {
			if (targets[0].isSuspended() || targets[0].isTerminated())
				break;
			Thread.sleep(100);
		}
		assertTrue("Suspended: " + targets[0].isSuspended() + " Termiunated: " + targets[0].isTerminated(), targets[0]
				.isSuspended());
		location = targets[0].getCurrentThread().getStackFrames()[0].getLocation();
		assertTrue(location.getLineNumber() == 7);
		assertTrue(location.getFunction().equals("func1"));
		assertTrue(location.getFile().equals("main.c"));

		/* clean up the session */
		session.terminate();
		session = null;
		targets = null;

	}

	/***************************************************************************
	 * A couple tests to make sure getting breakpoints works as expected
	 */
	public void testGetBreak() throws CoreException, MIException, IOException, CDIException {
		ICDIBreakpointManager breaks;
		ICDILocation location;
		ICDIBreakpoint[] breakpoints;
		ICDILocationBreakpoint curbreak;
		session = CDebugHelper.createSession("main", testProject);
		assertNotNull(session);
		breaks = session.getBreakpointManager();
		assertNotNull(breaks);

		/***********************************************************************
		 * Make sure initially we don't have any breakpoints
		 **********************************************************************/
		breakpoints = breaks.getBreakpoints();
		assertNotNull(breakpoints);
		assertTrue(breakpoints.length == 0);

		/***********************************************************************
		 * Make sure if we create a simple breakpoint, that we can get it back
		 * from the system
		 **********************************************************************/
		/* Create a break point on a generic function */
		location = breaks.createLocation("main.c", "func1", 0);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);

		breakpoints = breaks.getBreakpoints();
		assertNotNull(breakpoints);
		assertTrue(breakpoints.length == 1);
		if (breakpoints[0] instanceof ICDILocationBreakpoint) {
			curbreak = (ICDILocationBreakpoint) breakpoints[0];
		} else
			curbreak = null;
		assertNotNull(curbreak);

		assertTrue(curbreak.getLocation().equals(location));

		/***********************************************************************
		 * Make sure if we create multiple break points that we can still get
		 * them all back from the system,
		 **********************************************************************/
		/* Create another break point on main */
		location = breaks.createLocation("main.c", "main", 0);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);

		breakpoints = breaks.getBreakpoints();
		assertNotNull(breakpoints);
		assertTrue(breakpoints.length == 2);
		if (breakpoints[1] instanceof ICDILocationBreakpoint) {
			curbreak = (ICDILocationBreakpoint) breakpoints[1];
		} else
			curbreak = null;
		assertNotNull(curbreak);
		/*
		 * Make sure the location still looks like we expect it to.. .
		 */
		assertTrue(curbreak.getLocation().equals(location));

		breaks.deleteAllBreakpoints();

		/* clean up the session */
		session.terminate();
		session = null;

	}

	/***************************************************************************
	 * A couple tests to make sure deleting breakpoints works as expected
	 */
	public void testDelBreak() throws CoreException, MIException, IOException, CDIException {
		ICDIBreakpointManager breaks;
		ICDILocation location, savedLocation;
		ICDIBreakpoint[] breakpoints, savedbreakpoints;
		ICDILocationBreakpoint curbreak;

		session = CDebugHelper.createSession("main", testProject);
		assertNotNull(session);
		breaks = session.getBreakpointManager();
		assertNotNull(breaks);

		/* Make sure initially we don't have any breakpoints */
		breakpoints = breaks.getBreakpoints();
		assertNotNull(breakpoints);
		assertTrue(breakpoints.length == 0);

		/***********************************************************************
		 * 
		 * Test to make sure if we create a new breakpoint, we can delete it by
		 * passing a refrence to it to deleteBreakpoint()
		 *  
		 **********************************************************************/

		/* Create a break point on a generic function */
		location = breaks.createLocation("main.c", "func1", 0);
		assertNotNull(location);
		curbreak = breaks.setLocationBreakpoint(0, location, null, null);
		breaks.deleteBreakpoint(curbreak);
		pause();
		/**
		 * we should not have any breakpoints left.
		 */
		breakpoints = breaks.getBreakpoints();
		assertTrue(breakpoints.length == 0);

		/***********************************************************************
		 * 
		 * Test to make sure if we create multiple new breakpoint, we can delete
		 * one of them by passing a refrence to it to deleteBreakpoint()
		 *  
		 **********************************************************************/

		/* Create a break point on a generic function */
		location = breaks.createLocation("main.c", "func1", 0);
		assertNotNull(location);
		curbreak = breaks.setLocationBreakpoint(0, location, null, null);
		savedLocation = curbreak.getLocation();

		location = breaks.createLocation("main.c", "main", 0);
		assertNotNull(location);
		curbreak = breaks.setLocationBreakpoint(0, location, null, null);
		breaks.deleteBreakpoint(curbreak);
		pause();
		breakpoints = breaks.getBreakpoints();
		/***********************************************************************
		 * Make sure there is only 1 breakpoint left, and it's the one we expect
		 */
		assertTrue(breakpoints.length == 1);
		curbreak = (ICDILocationBreakpoint) breakpoints[0];
		assertNotNull(curbreak);
		assertTrue(curbreak.getLocation().equals(savedLocation));
		/***********************************************************************
		 * Then delete the other breakpoint.
		 */
		breaks.deleteBreakpoint(curbreak);
		pause();
		breakpoints = breaks.getBreakpoints();
		assertTrue(breakpoints.length == 0);

		/***********************************************************************
		 * Make sure deleteBreakpoints works when given 1 breakpoint to delete
		 **********************************************************************/
		savedbreakpoints = new ICDIBreakpoint[1];
		for (int x = 0; x < 10; x++) {
			location = breaks.createLocation("main.c", null, x + 1);
			savedbreakpoints[0] = breaks.setLocationBreakpoint(0, location, null, null);
			assertNotNull(savedbreakpoints[0]);
		}
		breaks.deleteBreakpoints(savedbreakpoints);
		pause();
		/* We should now have 9 breakpoints left. */
		breakpoints = breaks.getBreakpoints();
		assertTrue(breakpoints.length == 9);
		/* Make sure we have the correct 9 breakpoints left */
		for (int x = 0; x < breakpoints.length; x++) {
			curbreak = (ICDILocationBreakpoint) breakpoints[x];
			assertTrue(curbreak.getLocation().getLineNumber() == x + 1);
		}
		breaks.deleteAllBreakpoints();
		pause();
		assertTrue(breaks.getBreakpoints().length == 0);

		/***********************************************************************
		 * Make sure deleteBreakpoints works when given more then 1 but less
		 * then all breakpoints to delete
		 **********************************************************************/
		savedbreakpoints = new ICDIBreakpoint[4];
		for (int x = 0; x < 10; x++) {
			location = breaks.createLocation("main.c", null, x + 1);
			savedbreakpoints[x % 4] = breaks.setLocationBreakpoint(0, location, null, null);
			assertNotNull(savedbreakpoints[x % 4]);
		}
		breaks.deleteBreakpoints(savedbreakpoints);
		pause();

		/* We should now have 6 breakpoints left. */
		breakpoints = breaks.getBreakpoints();
		assertTrue(breakpoints.length == 6);
		/* Make sure we have the correct 6 breakpoints left */
		for (int x = 0; x < breakpoints.length; x++) {
			curbreak = (ICDILocationBreakpoint) breakpoints[x];
			assertTrue(curbreak.getLocation().getLineNumber() == x + 1);
		}
		breaks.deleteAllBreakpoints();
		pause();
		assertTrue(breaks.getBreakpoints().length == 0);

		/***********************************************************************
		 * Make sure deleteBreakpoints works when given all the breakpoints
		 **********************************************************************/
		savedbreakpoints = new ICDIBreakpoint[10];
		for (int x = 0; x < 10; x++) {
			location = breaks.createLocation("main.c", null, x + 1);
			savedbreakpoints[x] = breaks.setLocationBreakpoint(0, location, null, null);
			assertNotNull(savedbreakpoints[x]);
		}
		breaks.deleteBreakpoints(savedbreakpoints);
		pause();
		/* We should now have 0 breakpoints left. */
		breakpoints = breaks.getBreakpoints();
		assertTrue(breakpoints.length == 0);

		/***********************************************************************
		 * Make sure deleteAllBreakpoints works
		 **********************************************************************/

		for (int x = 0; x < 10; x++) {
			location = breaks.createLocation("main.c", null, x + 1);
			curbreak = breaks.setLocationBreakpoint(0, location, null, null);
			assertNotNull(curbreak);
		}
		breaks.deleteAllBreakpoints();
		pause();
		/* We should now have 0 breakpoints left. */
		breakpoints = breaks.getBreakpoints();
		assertTrue(breakpoints.length == 0);

		/* clean up the session */
		session.terminate();
		session = null;

	}

	/***************************************************************************
	 * A couple tests to make sure setting breakpoints with conditions seems to
	 * work as expected.
	 */
	public void testCondBreak() throws CoreException, MIException, IOException, CDIException, InterruptedException {
		ICDIBreakpointManager breaks;
		ICDILocation location;
		ICDICondition cond;
		boolean caught = false;
		session = CDebugHelper.createSession("main", testProject);
		assertNotNull(session);
		breaks = session.getBreakpointManager();
		assertNotNull(breaks);

		/***********************************************************************
		 * Create a break point on a generic function with an empty condition
		 **********************************************************************/
		cond = breaks.createCondition(0, "");
		location = breaks.createLocation(null, "func1", 0);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, cond, null);

		/***********************************************************************
		 * Create a break point on a generic function with an valid condition
		 **********************************************************************/
		cond = breaks.createCondition(0, "x<10");
		location = breaks.createLocation(null, "func1", 0);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, cond, null);

		/***********************************************************************
		 * Create a break point on a generic function with an invalid condition
		 * We expect to get a CDIException when we try to set the breakpoint.
		 **********************************************************************/
		cond = breaks.createCondition(0, "nonexist<10");
		location = breaks.createLocation(null, "func1", 0);
		assertNotNull(location);
		try {
			breaks.setLocationBreakpoint(0, location, cond, null);
		} catch (CDIException e) {
			caught = true;
		}
		assertTrue(caught);

		/***********************************************************************
		 * Create a break point on a line number with a condition and make sure
		 * it does not suspend execution of the application until the condition
		 * is true
		 **********************************************************************/
		breaks.deleteAllBreakpoints();
		location = breaks.createLocation(null, null, 23);
		assertNotNull(location);
		cond = breaks.createCondition(0, "a>10");

		breaks.setLocationBreakpoint(0, location, cond, null);
		targets = session.getTargets();
		/*
		 * We better only have one target connected to this session or something
		 * is not right...
		 */
		assertTrue(targets.length == 1);
		/*
		 * Resume the target, this should cause it to run till it hits the
		 * breakpoint
		 */
		targets[0].resume();
		/**
		 * Give the process up to 10 seconds to become either terminated or
		 * suspended. It sould hit the breakponint almost immediatly so we
		 * should only sleep for max 100 ms
		 */
		for (int x = 0; x < 100; x++) {
			if (targets[0].isSuspended() || targets[0].isTerminated())
				break;
			Thread.sleep(100);
		}
		assertTrue("Suspended: " + targets[0].isSuspended() + " Termiunated: " + targets[0].isTerminated(), targets[0]
				.isSuspended());
		location = targets[0].getCurrentThread().getStackFrames()[0].getLocation();
		assertTrue(location.getLineNumber() == 23);
		assertTrue(location.getFunction().equals("main"));
		assertTrue(location.getFile().equals("main.c"));
		/* Get the value of a and and make sure it is 11 */
		assertTrue(targets[0].evaluateExpressionToString("a"), targets[0].evaluateExpressionToString("a").equals("11"));

		/* clean up the session */
		session.terminate();
		session = null;
		targets = null;

	}

	void pause() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
		}
	}

}