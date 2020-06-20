/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.projectmodel.tests;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.managedbuilder.testplugin.ManagedBuildTestHelper;
import org.eclipse.core.resources.IProject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class BackwardCompatiblityTests extends TestCase {
	private static final String TEST_3X_STD_MAKE_PROJECTS = "test3xStdMakeProjects";

	private List<IProject> projList = new LinkedList<>();

	public static Test suite() {
		return new TestSuite(BackwardCompatiblityTests.class);
	}

	public void test3xStdMakeProject() {
		String PROJ_NAME = "std_cpp_1";

		//		String[] BIN_PARSERS = new String[]{
		//				"org.eclipse.cdt.core.ELF",
		//				"org.eclipse.cdt.core.PE",
		//				"org.eclipse.cdt.core.GNU_ELF",
		//				"org.eclipse.cdt.core.MachO"
		//		};
		//
		//		String[] ERR_PARSERS = new String[]{
		//				"org.eclipse.cdt.core.CWDLocator",
		//				"org.eclipse.cdt.core.GASErrorParser",
		//				"org.eclipse.cdt.core.VCErrorParser",
		//				"org.eclipse.cdt.core.GmakeErrorParser",
		//		};

		IProject project = loadStdProject(PROJ_NAME);
		projList.add(project);

		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		assertFalse(mngr.isNewStyleProject(project));
		ICProjectDescription des = mngr.getProjectDescription(project, false);
		checkDescription(des);

		des = mngr.getProjectDescription(project, true);
		checkDescription(des);

	}

	private void checkDescription(ICProjectDescription des) {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();

		assertFalse(mngr.isNewStyleProject(des));
		assertFalse(des.isCdtProjectCreating());
		assertEquals(1, des.getConfigurations().length);

	}

	private IProject loadStdProject(String name) {
		return ManagedBuildTestHelper.loadProject(name, TEST_3X_STD_MAKE_PROJECTS);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp(getName());
		for (Iterator<IProject> iter = projList.iterator(); iter.hasNext();) {
			IProject proj = iter.next();
			try {
				proj.delete(true, null);
			} catch (Exception e) {
			}
			iter.remove();
		}
		super.tearDown();
	}

}
