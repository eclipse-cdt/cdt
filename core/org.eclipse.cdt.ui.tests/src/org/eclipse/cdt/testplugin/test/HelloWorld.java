/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.cdt.testplugin.test;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.testplugin.CProjectHelper;
import org.eclipse.cdt.testplugin.TestPluginLauncher;


public class HelloWorld extends TestCase {
	
	private ICProject fCProject;
	
	public static void main(String[] args) {
		TestPluginLauncher.run(TestPluginLauncher.getLocationFromProperties(), HelloWorld.class, args);
	}
	
	public static Test suite() {
		TestSuite suite= new TestSuite(HelloWorld.class.getName());
		suite.addTest(new HelloWorld("test1"));
		return suite;
	}		
	
	public HelloWorld(String name) {
		super(name);
	}
	
	protected void setUp() throws Exception {
			fCProject= CProjectHelper.createCProject("TestProject1", "bin");
	}


	protected void tearDown() throws Exception {
		CProjectHelper.delete(fCProject);
	}	
		
	public void test1() throws Exception {

		assertFalse("Exception to test", 0 != 0);
		
	}		

}