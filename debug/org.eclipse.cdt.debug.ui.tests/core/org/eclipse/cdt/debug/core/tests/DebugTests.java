package org.eclipse.cdt.debug.core.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISourceManager;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.cdt.debug.testplugin.CDebugHelper;
import org.eclipse.cdt.debug.testplugin.CProjectHelper;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the debug stuff. It currenly 
 * uses the mi debugger.
 *
 */
public class DebugTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    ICProject testProject;
    NullProgressMonitor monitor;
	ICDISession session;
    

    /**
     * Constructor for DebugTests
     * @param name
     */
    public DebugTests(String name) {
        super(name);
     /***
     * The assume that they have a working workspace
     * and workspace root object to use to create projects/files in, 
     * so we need to get them setup first.
     */
        workspace= ResourcesPlugin.getWorkspace();
        root= workspace.getRoot();
        monitor = new NullProgressMonitor();
        if (workspace==null) 
            fail("Workspace was not setup");
        if (root==null)
            fail("Workspace root was not setup");

    }
    
    /**
     * Sets up the test fixture.
     *
     * Called before every test case method.
     * 
     * Example code test the packages in the project 
     *  "com.qnx.tools.ide.cdt.core"
     */
    protected void setUp() throws CoreException, InvocationTargetException, IOException {
		ResourcesPlugin.getWorkspace().getDescription().setAutoBuilding(false);
		/***
		 * Create a new project and import the test source.
		 */
		IPath importFile = new Path("resources/debugTest.zip");
		testProject=CProjectHelper.createCProjectWithImport("filetest", importFile);
		if (testProject==null)
			fail("Unable to create project");
		/* Build the test project.. */

		testProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
            
    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() throws CoreException, CDIException {
    	if (session!=null) {
    		session.terminate();
    		session=null;
    	}
        CProjectHelper.delete(testProject);
    }
    
    public static TestSuite suite() {
    return new TestSuite(DebugTests.class);
    }
    
    public static void main (String[] args){
    junit.textui.TestRunner.run(suite());
    }


    /***
     * Can we setup a debug?
     * This is sort of a catch all sanity tests to make sure we can create a debug
     * session with a break point and start it without having any exceptions thrown.
     * It's not ment to be a real proper test.
     */
    public void testDebug() throws CoreException, MIException, IOException, CDIException {
        ICDISourceManager source;
        ICDITarget cdiTarget;
        ICDILocation location;
	
		session=CDebugHelper.createSession("main",testProject);
        assertNotNull(session);
        source=session.getSourceManager();
        assertNotNull(source);
		cdiTarget=session.getCurrentTarget();
		assertNotNull(cdiTarget);
		location=cdiTarget.createLocation(null, "func1", 0);
		assertNotNull(location);
		cdiTarget.setLocationBreakpoint(0, location, null, false);
		session.getCurrentTarget().resume();
		session.terminate();
		session=null;

   }
       

}
