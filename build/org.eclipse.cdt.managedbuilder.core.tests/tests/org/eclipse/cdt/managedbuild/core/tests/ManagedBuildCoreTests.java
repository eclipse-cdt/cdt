/**********************************************************************
 * Copyright (c) 2004 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Intel Corporation - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.managedbuild.core.tests;

import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IOptionCategory;
import org.eclipse.cdt.managedbuilder.core.ITargetPlatform;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;

public class ManagedBuildCoreTests extends TestCase {
	private static final boolean boolVal = true;
		
	public ManagedBuildCoreTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(ManagedBuildCoreTests.class.getName());
		
		suite.addTest(new ManagedBuildCoreTests("testLoadManifest"));
		suite.addTest(new ManagedBuildCoreTests("cleanup"));
		
		return suite;
	}

	/**
	 * Navigates through the CDT 2.1 manifest file and verifies that the
	 * definitions are loaded correctly. 
	 */
	public void testLoadManifest() throws Exception {
		IProjectType exeType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu.exe");
		assertNotNull(exeType);
		IProjectType libType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu.lib");
		assertNotNull(libType);
		IProjectType dllType = ManagedBuildManager.getProjectType("cdt.managedbuild.target.testgnu.so");
		assertNotNull(dllType);
	}
	
	/**
	 * Remove all the project information associated with the project used during test.
	 */
	public void cleanup() {
		//removeProject(projectName);
	}
	
}

