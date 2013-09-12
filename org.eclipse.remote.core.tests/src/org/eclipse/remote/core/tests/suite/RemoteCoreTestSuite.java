package org.eclipse.remote.core.tests.suite;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.remote.core.tests.ProcessTests;

public class RemoteCoreTestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite(RemoteCoreTestSuite.class.getName());

		// suite.addTestSuite(ConnectionTests.class);
		// suite.addTestSuite(FileStoreTests.class);
		suite.addTestSuite(ProcessTests.class);
		return suite;
	}

}
