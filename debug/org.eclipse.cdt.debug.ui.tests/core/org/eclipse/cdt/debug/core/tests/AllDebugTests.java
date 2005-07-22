/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 * AllDedbugTests.java
 * This is the main entry point for running this suite of JUnit tests
 * for all tests within the package "org.eclipse.cdt.debug.core"
 * 
 * @author Judy N. Green
 * @since Jul 19, 2002
 */
public class AllDebugTests {
	
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        // Just add more test cases here as you create them for 
        // each class being tested
        
        suite.addTest(DebugTests.suite());
        suite.addTest(BreakpointTests.suite());
        suite.addTest(LocationTests.suite());
        return suite;
        
        
    }
} // End of AllDebugTests.java

