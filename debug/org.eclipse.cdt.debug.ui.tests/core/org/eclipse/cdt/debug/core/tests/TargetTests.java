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
 * This file contains a set of generic tests for the CDI Target interfaces.
 * It will currenly use the mi implementation.
 *
 */
public class TargetTests extends TestCase {
    IWorkspace workspace;
    IWorkspaceRoot root;
    ICProject testProject;
    NullProgressMonitor monitor;
    

    /**
     * Constructor for TargetTests
     * @param name
     */
    public TargetTests(String name) {
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
    return new TestSuite(TargetTests.class);
    }
    
    public static void main (String[] args){
    junit.textui.TestRunner.run(suite());
    }


    /***
     * A couple tests to make sure setting breakpoints on functions works as
     * expected.
     */
    public void testEvaluate() throws CoreException, MIException, IOException, CDIException, InterruptedException {
        ICDISession session;
        ICDIBreakpointManager breaks;
        ICDILocation location;
        ICDITarget targets[];
        session=CDebugHelper.createSession("main");
        assertNotNull(session);
        breaks=session.getBreakpointManager();
        assertNotNull(breaks);
    
        
        /**********************************************************************
         * Test to make sure if we call ICDITarget.evaluateExpressionToValue 
         * seems to work.
         **********************************************************************/    
   
        location=breaks.createLocation(null, "func1", 0);
        assertNotNull(location);
        breaks.setLocationBreakpoint(0, location, null, null);
        targets=session.getTargets(); 
        /* We better only have one target connected to this session or something 
         * is not right...
         */       
        assertTrue(targets.length==1);
        /* Resume the target, this should cause it to run till it hits the 
         * breakpoint
         */
        targets[0].resume();
        /**
         * Give the process up to 10 seconds to become either terminated or 
         * suspended.  It sould hit the breakponint almost immediatly so we 
         * should only sleep for max 100 ms
         */
        for (int x=0;x<100;x++) {
            if (targets[0].isTerminated() || targets[0].isSuspended()) 
                break;
            Thread.sleep(100);
        }
        assertTrue(targets[0].isSuspended());
        assertNotNull("PR:24183", targets[0].evaluateExpressionToValue("a"));
        /* clean up the session */
        session.terminate();

    
   }


}
