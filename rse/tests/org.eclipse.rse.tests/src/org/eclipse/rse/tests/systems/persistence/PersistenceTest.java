package org.eclipse.rse.tests.systems.persistence;

import org.eclipse.rse.core.model.ISystemProfile;
import org.eclipse.rse.tests.framework.AnnotatingTestCase;
import org.eclipse.rse.tests.systems.core.SystemTestUtil;

public class PersistenceTest extends AnnotatingTestCase {
	
	public PersistenceTest(String name) {
		super(name);
	}
	
	public void testHostCreation() throws Exception {
		ISystemProfile profile = SystemTestUtil.findProfile("TestProfile");
		if (profile == null) {
			SystemTestUtil.createProfile("TestProfile");
		}
		SystemTestUtil.createHost("TestProfile", "TestHost1", "localhost", "Unix", "userid", "password");
		SystemTestUtil.createHost("TestProfile", "TestHost2", "localhost", "Unix", "userid", "password");
		SystemTestUtil.createHost("TestProfile", "TestHost3", "localhost", "Unix", "userid", "password");
		SystemTestUtil.createHost("TestProfile", "TestHost4", "localhost", "Unix", "userid", "password");
		SystemTestUtil.createHost("TestProfile", "TestHost5", "localhost", "Unix", "userid", "password");
		SystemTestUtil.createHost("TestProfile", "TestHost6", "localhost", "Unix", "userid", "password");
	}
	
	public void testHostDeletion() throws Exception {
		SystemTestUtil.deleteHost("TestProfile", "TestHost1");
		SystemTestUtil.deleteHost("TestProfile", "TestHost2");
		SystemTestUtil.deleteHost("TestProfile", "TestHost3");
		SystemTestUtil.deleteHost("TestProfile", "TestHost4");
		SystemTestUtil.deleteHost("TestProfile", "TestHost5");
		SystemTestUtil.deleteHost("TestProfile", "TestHost6");
	}

}
