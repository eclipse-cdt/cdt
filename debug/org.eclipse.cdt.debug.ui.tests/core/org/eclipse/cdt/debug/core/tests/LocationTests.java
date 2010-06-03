/*******************************************************************************
 * Copyright (c) 2000, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.tests;

import java.io.IOException;

import junit.framework.Test;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.cdt.debug.mi.core.MIException;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Peter Graves
 *
 * This file contains a set of generic tests for the CDI Location interfaces.
 * It will currenly use the mi implementation.
 *
 */
public class LocationTests extends AbstractDebugTest {
    public static Test suite() {
		return new DebugTestWrapper(LocationTests.class){};
	}
    
    public static void main(String[] args) {
		junit.textui.TestRunner.run(suite());
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		createDebugSession();
		assertNotNull(currentTarget);
		currentTarget.deleteAllBreakpoints();
		pause();
	}

    /***
     * A couple tests to make sure comparing Locations works as expected.
     */
    public void testIsEquals() throws CoreException, MIException, IOException, CDIException {
    	ICDITarget cdiTarget = currentTarget;
        ICDILineLocation lineLocation, lineLocation2;
		ICDIFunctionLocation functionLocation, functionLocation2;
        ICDIBreakpoint[] breakpoints;
        ICDILocationBreakpoint curbreak;
       
        
        /**********************************************************************
         *  Simple test.. this should work.
         **********************************************************************/
        functionLocation=cdiTarget.createFunctionLocation("main.c", "func1");
        functionLocation2=cdiTarget.createFunctionLocation("main.c", "func1");
        assertTrue(functionLocation.equals(functionLocation2));
        /**********************************************************************
         *  Simple test.. this should work.
         **********************************************************************/
        lineLocation=cdiTarget.createLineLocation("main.c", 10);
        lineLocation2=cdiTarget.createLineLocation("main.c", 10);
        assertTrue(lineLocation.equals(lineLocation2));

        /**********************************************************************
         * make sure that the location returned from getLocation on the 
         * ICDILocationBreakpoint.getLocation that is returned from 
         * setLocationBreakpoint is the same as the breakpoint returned from
         * BreakpointManager.getBreakpoints.getLocation()
         **********************************************************************/
        functionLocation=cdiTarget.createFunctionLocation("main.c", "func1");
        assertNotNull(functionLocation);
        functionLocation2=cdiTarget.setFunctionBreakpoint(0, functionLocation, null, false).getLocator();
        
        breakpoints=cdiTarget.getBreakpoints();
        assertNotNull(breakpoints);
        assertTrue(breakpoints.length==1);
        if (breakpoints[0] instanceof ICDILocationBreakpoint) {
            curbreak=(ICDILocationBreakpoint) breakpoints[0];
        } else
            curbreak=null;
        assertNotNull(curbreak);
        
        assertTrue(curbreak.getLocator().equals(functionLocation2));
        cdiTarget.deleteAllBreakpoints();
        pause();
        /* Create a break point on a generic function with a file name that 
         * gdb will change to the relitive path of the source file.  This
         * should work, but at the time of writing (Sept 25, 2002) does not.
         */
        functionLocation=cdiTarget.createFunctionLocation("main.c", "func1");
        assertNotNull(functionLocation);
        cdiTarget.setFunctionBreakpoint(0, functionLocation, null, false);
        
        breakpoints=cdiTarget.getBreakpoints();
        assertNotNull(breakpoints);
        assertTrue(breakpoints.length==1);
        if (breakpoints[0] instanceof ICDILocationBreakpoint) {
            curbreak=(ICDILocationBreakpoint) breakpoints[0];
        } else
            curbreak=null;
        assertNotNull(curbreak);
        
        assertTrue("PR:23879",curbreak.getLocator().equals(functionLocation));
   
   }
       

}
