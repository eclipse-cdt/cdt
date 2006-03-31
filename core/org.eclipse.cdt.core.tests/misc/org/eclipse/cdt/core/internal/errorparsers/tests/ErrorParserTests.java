package org.eclipse.cdt.core.internal.errorparsers.tests;

import junit.framework.Test;
import junit.framework.TestSuite;


public class ErrorParserTests {

	public static Test suite() {
        TestSuite suite = new TestSuite(ErrorParserTests.class.getName());

        // Just add more test cases here as you create them for
        // each class being tested
		suite.addTest(GCCErrorParserTests.suite());
        suite.addTest(FileBasedErrorParserTests.suite());
        return suite;
	}

}
