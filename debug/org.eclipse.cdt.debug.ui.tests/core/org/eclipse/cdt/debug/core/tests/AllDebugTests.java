package org.eclipse.cdt.debug.core.tests;
/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */



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
        suite.addTest(TargetTests.suite());
        return suite;
        
        
    }
} // End of AllDebugTests.java

