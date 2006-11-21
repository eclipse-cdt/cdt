package org.eclipse.rse.tests.systems.persistence;

import junit.framework.TestSuite;

import org.eclipse.rse.tests.framework.ITestSuiteProvider;

public class PersistenceTestSuite implements ITestSuiteProvider {

	public TestSuite getSuite(String argument) {
		TestSuite suite = new TestSuite("Persistence Test Suite");
		if (argument == null || argument.equals("create")) {
			suite.addTest(new PersistenceTest("testHostCreation"));
		}
		if (argument == null || argument.equals("delete")) {
			suite.addTest(new PersistenceTest("testHostDeletion"));
		}
		return suite;
	}

}
