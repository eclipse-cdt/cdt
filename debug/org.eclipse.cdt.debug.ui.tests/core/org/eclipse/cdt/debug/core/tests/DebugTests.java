package org.eclipse.cdt.debug.core.tests;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import java.io.FileNotFoundException;
import java.io.IOException;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.cdt.debug.testplugin.*;
import org.eclipse.cdt.core.model.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.cdt.debug.mi.core.*;
import org.eclipse.cdt.debug.core.cdi.*;

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
    protected void setUp() throws CoreException,FileNotFoundException {
            
        /***
         * Setup the various files, paths and projects that are needed by the
         * tests
         */
        testProject=CProjectHelper.createCProject("filetest", "none");
        if (testProject==null)
            fail("Unable to create project");
    }
    
     /**
     * Tears down the test fixture.
     *
     * Called after every test case method.
     */
    protected void tearDown() throws CoreException {
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
        ICDISession session;
        ICDISourceManager source;
        ICDIBreakpointManager breaks;
        ICDILocation location;
	
		session=CDebugHelper.createSession("main");
        assertNotNull(session);
        source=session.getSourceManager();
        assertNotNull(source);
		breaks=session.getBreakpointManager();
		assertNotNull(breaks);
		location=breaks.createLocation(null, "func1", 0);
		assertNotNull(location);
		breaks.setLocationBreakpoint(0, location, null, null);
		session.getCurrentTarget().resume();


   }
       

}
