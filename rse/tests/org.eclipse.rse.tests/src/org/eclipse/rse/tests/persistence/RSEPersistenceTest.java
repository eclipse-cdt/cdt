package org.eclipse.rse.tests.persistence;

import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.tests.RSETestsPlugin;
import org.eclipse.rse.tests.core.RSECoreTestCase;
import org.eclipse.rse.tests.core.connection.RSEConnectionTestUtil;

public class RSEPersistenceTest extends RSECoreTestCase {
	
	public RSEPersistenceTest(String name) {
		super(name);
	}
	
	public void testHostCreation() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("RSEPersistenceTest.testHostCreation")) return; //$NON-NLS-1$

		ISystemProfile profile = RSEConnectionTestUtil.findProfile("TestProfile"); //$NON-NLS-1$
		if (profile == null) {
			RSEConnectionTestUtil.createProfile("TestProfile"); //$NON-NLS-1$
		}
		RSEConnectionTestUtil.createHost("TestProfile", "TestHost1", "localhost", "Unix", "userid", "password"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		RSEConnectionTestUtil.createHost("TestProfile", "TestHost2", "localhost", "Unix", "userid", "password"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		RSEConnectionTestUtil.createHost("TestProfile", "TestHost3", "localhost", "Unix", "userid", "password"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		RSEConnectionTestUtil.createHost("TestProfile", "TestHost4", "localhost", "Unix", "userid", "password"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		RSEConnectionTestUtil.createHost("TestProfile", "TestHost5", "localhost", "Unix", "userid", "password"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
		RSEConnectionTestUtil.createHost("TestProfile", "TestHost6", "localhost", "Unix", "userid", "password"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	}
	
	public void testHostDeletion() throws Exception {
		if (!RSETestsPlugin.isTestCaseEnabled("RSEPersistenceTest.testHostDeletion")) return; //$NON-NLS-1$

		RSEConnectionTestUtil.deleteHost("TestProfile", "TestHost1"); //$NON-NLS-1$ //$NON-NLS-2$
		RSEConnectionTestUtil.deleteHost("TestProfile", "TestHost2"); //$NON-NLS-1$ //$NON-NLS-2$
		RSEConnectionTestUtil.deleteHost("TestProfile", "TestHost3"); //$NON-NLS-1$ //$NON-NLS-2$
		RSEConnectionTestUtil.deleteHost("TestProfile", "TestHost4"); //$NON-NLS-1$ //$NON-NLS-2$
		RSEConnectionTestUtil.deleteHost("TestProfile", "TestHost5"); //$NON-NLS-1$ //$NON-NLS-2$
		RSEConnectionTestUtil.deleteHost("TestProfile", "TestHost6"); //$NON-NLS-1$ //$NON-NLS-2$
	}

}
