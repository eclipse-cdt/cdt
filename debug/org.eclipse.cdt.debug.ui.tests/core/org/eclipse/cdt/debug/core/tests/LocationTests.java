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
import org.eclipse.cdt.debug.core.cdi.model.*;

/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the CDI Location interfaces.
 * It will currenly use the mi implementation.
 *
 */
public class LocationTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    ICProject testProject;
    NullProgressMonitor monitor;
    

    /**
     * Constructor for LocationTests
     * @param name
     */
    public LocationTests(String name) {
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
    return new TestSuite(LocationTests.class);
    }
    
    public static void main (String[] args){
    junit.textui.TestRunner.run(suite());
    }


    /***
     * A couple tests to make sure comparing Locations works as expected.
     */
    public void testIsEquals() throws CoreException, MIException, IOException, CDIException {
        ICDISession session;
        ICDIBreakpointManager breaks;
        ICDILocation location, location2;
        ICDIBreakpoint[] breakpoints;
        ICDILocationBreakpoint curbreak;
        session=CDebugHelper.createSession("main");
        assertNotNull(session);
        breaks=session.getBreakpointManager();
        assertNotNull(breaks);
        
        /**********************************************************************
         *  Simple test.. this should work.
         **********************************************************************/
        location=breaks.createLocation("main.c", "func1", 0);
        location2=breaks.createLocation("main.c", "func1", 0);
        assertTrue(location.equals(location2));
        /**********************************************************************
         *  Simple test.. this should work.
         **********************************************************************/
        location=breaks.createLocation("main.c", null, 10);
        location2=breaks.createLocation("main.c", null, 10);
        assertTrue(location.equals(location2));

        /**********************************************************************
         * make sure that the location returned from getLocation on the 
         * ICDILocationBreakpoint.getLocation that is returned from 
         * setLocationBreakpoint is the same as the breakpoint returned from
         * BreakpointManager.getBreakpoints.getLocation()
         **********************************************************************/
        location=breaks.createLocation("main.c", "func1", 0);
        assertNotNull(location);
        location2=breaks.setLocationBreakpoint(0, location, null, null).getLocation();
        
        breakpoints=breaks.getBreakpoints();
        assertNotNull(breakpoints);
        assertTrue(breakpoints.length==1);
        if (breakpoints[0] instanceof ICDILocationBreakpoint) {
            curbreak=(ICDILocationBreakpoint) breakpoints[0];
        } else
            curbreak=null;
        assertNotNull(curbreak);
        
        assertTrue(curbreak.getLocation().equals(location2));
        breaks.deleteAllBreakpoints();
        /* Create a break point on a generic function with a file name that 
         * gdb will change to the relitive path of the source file.  This
         * should work, but at the time of writing (Sept 25, 2002) does not.
         */
        location=breaks.createLocation("main.c", "func1", 0);
        assertNotNull(location);
        breaks.setLocationBreakpoint(0, location, null, null);
        
        breakpoints=breaks.getBreakpoints();
        assertNotNull(breakpoints);
        assertTrue(breakpoints.length==1);
        if (breakpoints[0] instanceof ICDILocationBreakpoint) {
            curbreak=(ICDILocationBreakpoint) breakpoints[0];
        } else
            curbreak=null;
        assertNotNull(curbreak);
        
        assertTrue("PR:23879",curbreak.getLocation().equals(location));

        
        /* clean up the session */
        session.terminate();

    
   }
       

}
