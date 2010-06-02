/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.projectmodel.tests;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;

public class BackwardCompatiblityTests extends TestCase {
	private static final String TEST_3X_STD_MAKE_PROJECTS = "test3xStdMakeProjects";
	
	private List projList = new LinkedList();
	
	public static Test suite() {
		return new TestSuite(BackwardCompatiblityTests.class);
	}
	
	public void test3xStdMakeProject(){
		String PROJ_NAME = "std_cpp_1";
		
		String[] BIN_PARSERS = new String[]{
				"org.eclipse.cdt.core.ELF",
				"org.eclipse.cdt.core.PE",
				"org.eclipse.cdt.core.GNU_ELF",
				"org.eclipse.cdt.core.MachO"
		};
		
		String[] ERR_PARSERS = new String[]{
				"org.eclipse.cdt.core.CWDLocator",
				"org.eclipse.cdt.core.GASErrorParser",
				"org.eclipse.cdt.core.VCErrorParser",
				"org.eclipse.cdt.core.GmakeErrorParser",
		};
		
		IProject project = loadStdProject(PROJ_NAME);
		projList.add(project);

		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		assertFalse(mngr.isNewStyleProject(project));
		ICProjectDescription des = mngr.getProjectDescription(project, false);
		checkDescription(des);

		des = mngr.getProjectDescription(project, true);
		checkDescription(des);
		
	}
	
	private void checkDescription(ICProjectDescription des){
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();

		assertFalse(mngr.isNewStyleProject(des));
		assertFalse(des.isCdtProjectCreating());
		assertEquals(1, des.getConfigurations().length);
		
	}
	
	private IProject loadStdProject(String name){
		return ManagedBuildTestHelper.loadProject(name, TEST_3X_STD_MAKE_PROJECTS);
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		for(Iterator iter = projList.iterator(); iter.hasNext();){
			IProject proj = (IProject)iter.next();
			try {
				proj.delete(true, null);
			} catch (Exception e){
			}
			iter.remove();
		}
		super.tearDown();
	}
	
	
}
