package org.eclipse.remote.jsch.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class RemoteCoreTestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite(RemoteCoreTestSuite.class.getName());

		suite.addTestSuite(ConnectionTests.class);
		suite.addTestSuite(FileStoreTests.class);
		suite.addTestSuite(ProcessTests.class);
		return suite;
	}

}
