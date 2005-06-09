/**********************************************************************
 * Copyright (c) 2005 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.managedbuilder.core.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;

/**
 * 
 * */
public class ManagedBuildTCSupportedTest extends TestCase {

	public ManagedBuildTCSupportedTest() {	super(); }
	public ManagedBuildTCSupportedTest(String name) { super(name); }
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildTCSupportedTest.class.getName());
		suite.addTest(new ManagedBuildTCSupportedTest("testIsSupported"));  //$NON-NLS-1$
		return suite;
	}
	
	/**
	 * testIsSupported() - 
	 */
	public void testIsSupported() {
		ManagedBuildMacrosTests.createManagedProject("Merde"); //$NON-NLS-1$
		
		IManagedProject mproj = ManagedBuildMacrosTests.mproj;	
		assertNotNull(mproj);
		IProjectType pt = mproj.getProjectType();
		assertNotNull(pt);
		IConfiguration[] cfgs = mproj.getConfigurations();
		assertNotNull(cfgs);
		IToolChain tc = cfgs[0].getToolChain();
		assertNotNull(tc);
		// all 4 toolchains are not supported now
		for (int i=0; i<cfgs.length; i++) 
			TestMacro.supported[i] = false;
		for (int i=0; i< cfgs.length; i++) {
			assertFalse(cfgs[i].getToolChain().isSupported());
			assertFalse(cfgs[i].isSupported());
		}
		// so project type should be not supported
	    assertFalse(pt.isSupported());
		// 1 of 4 toolChains made supported
		TestMacro.supported[0] = true;
		assertTrue(tc.isSupported());
		assertTrue(cfgs[0].isSupported());
		for (int i=1; i< cfgs.length; i++) 
			assertFalse(cfgs[i].isSupported());
	    assertTrue(pt.isSupported());
		// all 4 toolchains are supported now
		for (int i=0; i<cfgs.length; i++) 
			TestMacro.supported[i] = true;
		for (int i=0; i<4; i++) 
			assertTrue(cfgs[i].isSupported());
		assertFalse(cfgs[4].isSupported());
	    assertTrue(pt.isSupported());
	}
}


