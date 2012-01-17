/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.tests;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import junit.extensions.TestSetup;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public abstract class AbstractDebugTest extends TestCase {
	IWorkspace workspace;
	IWorkspaceRoot root;
	NullProgressMonitor monitor;
	static ICProject testProject = null;
	static ICDISession session = null;
	static ICDITarget targets[] = null;
	ICDITarget currentTarget;
	static boolean oneTimeSetupDone = false;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (oneTimeSetupDone == false) {
			oneTimeSetUp(); // this can happened when run junit failed test from UI, without invoking suite()
			oneTimeSetupDone = false; // re-set it back so tarDownOnes will run
		}
		/***********************************************************************
		 * The tests assume that they have a working workspace and workspace
		 * root object to use to create projects/files in, so we need to get
		 * them setup first.
		 */
		workspace = ResourcesPlugin.getWorkspace();
		root = workspace.getRoot();
		monitor = new NullProgressMonitor();
		if (workspace == null)
			fail("Workspace was not setup"); //$NON-NLS-1$
		if (root == null)
			fail("Workspace root was not setup"); //$NON-NLS-1$
	}

	void createDebugSession() throws IOException, MIException, CModelException {
		session = CDebugHelper.createSession(getProjectBinary(), testProject);
		assertNotNull(session);
		targets = session.getTargets();
		assertNotNull(targets);
		assertTrue(targets.length > 0);
		currentTarget = targets[0];
	}

	/**
	 * Sets up the test fixture.
	 * 
	 * Called before every test case method.
	 * 
	 * Example code test the packages in the project
	 * "com.qnx.tools.ide.cdt.core"
	 */
	protected void oneTimeSetUp() throws CoreException, InvocationTargetException, IOException {
		ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);
		/***********************************************************************
		 * Create a new project and import the test source.
		 */
		Path imputFile = new Path(getProjectZip()); 
		testProject = CProjectHelper.createCProjectWithImport(getProjectName(), imputFile); 
		if (testProject == null)
			fail("Unable to create project"); //$NON-NLS-1$
		/* Build the test project.. */

		testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		oneTimeSetupDone = true;
	}

	protected String getProjectName() {
		return "filetest";
	}

	protected String getProjectZip() {
		return "resources/debugTest.zip";
	}

	protected String getProjectBinary() {
		return "main";
	}


	public static Display getDisplay() {
		return PlatformUI.getWorkbench().getDisplay();
	}
	/**
	 * Dispatch ui events for at least msec - milliseconds
	 * 
	 * @param msec -
	 *            milliseconds delay
	 */
	public static void uimsleep(int msec) {
		long cur = System.currentTimeMillis();
		long pass = 0;
		Display display = getDisplay();
		while (pass < msec) {
			if (!display.readAndDispatch())
				display.sleep();
			pass = System.currentTimeMillis() - cur;
		}
	}
	/**
	 * Tears down the test fixture.
	 * 
	 * Called after every test case method.
	 */
	protected void oneTimeTearDown() throws CoreException {
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
		if (oneTimeSetupDone == false) {
			oneTimeTearDown(); // this can happened when run junit failed test from UI, without invoking suite()
		}

	}

	static class DebugTestWrapper extends TestSetup {
		private AbstractDebugTest newInstance;

		public DebugTestWrapper(Class clazz) {
			super(new TestSuite(clazz));
			/***********************************************************************
			 * Create a wrapper suite around the test suite we created above to
			 * allow us to only do the general setup once for all the tests. This is
			 * needed because the creation of the source and target projects takes a
			 * long time and we really only need to do it once. We could do the
			 * setup in the constructor, but we need to be able to remove everything
			 * when we are done.
			 */
			try {
				newInstance = (AbstractDebugTest) clazz.newInstance();
			} catch (Exception e) {
				throw new IllegalArgumentException(e);
			}
		}

		@Override
		protected void setUp() throws FileNotFoundException, IOException, InterruptedException,
				InvocationTargetException, CoreException {
			newInstance.oneTimeSetUp();
		}

		@Override
		protected void tearDown() throws FileNotFoundException, IOException, CoreException {
			newInstance.oneTimeTearDown();
		}
	}

	void pause() {
		uimsleep(200);
	}

	public ICDILocator getCurrentLocator() throws CDIException {
		return getCurrentFrame().getLocator();
	}

	public ICDIStackFrame getCurrentFrame() throws CDIException {
		return currentTarget.getCurrentThread().getStackFrames()[0];
	}
	
	void waitSuspend(ICDITarget currentTarget) {
		int loop;
		loop = 0;
		while ((currentTarget.isSuspended() == false) && (currentTarget.isTerminated() == false) && (loop < 20)) {
			uimsleep(500);
			loop++;
		}
		assertFalse("Target should be suspended, but it is terminated " + currentTarget.isTerminated(), currentTarget
				.isTerminated());
		assertTrue("Target should be suspended but it is not", currentTarget.isSuspended());

	}
	
	public void resumeCurrentTarget() throws CDIException{
		currentTarget.resume(false);
	}

	void setBreakOnMain() throws CDIException {
		ICDILocation location = null;
		location = currentTarget.createFunctionLocation("", "main"); //$NON-NLS-1$	
		currentTarget.setFunctionBreakpoint(ICBreakpointType.TEMPORARY, (ICDIFunctionLocation) location, null, false);
	}

	@Override
	protected void tearDown() throws Exception {
		/* clean up the session */
		if (session == null) {
			session.terminate();
			session = null;
		}
		super.tearDown();
	}
}
