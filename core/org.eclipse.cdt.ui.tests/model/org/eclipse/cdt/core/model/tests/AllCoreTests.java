package org.eclipse.cdt.core.model.tests;
/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */


import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 * AllTests.java
 * This is the main entry point for running this suite of JUnit tests
 * for all tests within the package "org.eclipse.cdt.core.model"
 * 
 * @author Judy N. Green
 * @since Jul 19, 2002
 */
public class AllCoreTests {
	
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AllCoreTests.class.getName());

        // Just add more test cases here as you create them for 
        // each class being tested
		suite.addTest(AllLanguageInterfaceTests.suite());
        suite.addTest(CModelTests.suite());
        suite.addTest(CModelExceptionTest.suite());
        suite.addTest(FlagTests.suite());
        suite.addTest(ArchiveTests.suite());
        suite.addTest(TranslationUnitTests.suite());
        suite.addTest(DeclaratorsTests.suite());
		
        return suite;
        
    }
} // End of AllCoreTests.java

