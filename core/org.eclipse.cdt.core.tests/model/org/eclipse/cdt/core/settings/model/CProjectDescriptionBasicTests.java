/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

public class CProjectDescriptionBasicTests  extends BaseTestCase{
	private static final String PROJ_NAME_PREFIX = "CProjectDescriptionBasicTests_";
	IProject p1, p2;
	
	public static TestSuite suite() {
		return suite(CProjectDescriptionBasicTests.class, "_");
	}
	
	public void testSetInvalidDescription() throws Exception {
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot();
		
		p1 = root.getProject(PROJ_NAME_PREFIX + "1");
		p1.create(null);
		p1.open(null);
		
		CProjectHelper.addNatureToProject(p1, CProjectNature.C_NATURE_ID, null);
		
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		ICProjectDescription des = mngr.createProjectDescription(p1, false);
		
		assertFalse(des.isValid());
		
		boolean failed = false;
		try {
			mngr.setProjectDescription(p1, des);
		} catch (CoreException e){
			failed = true;
		}
		
		assertTrue(failed);
	}
	
	public void remove_prefix_testSetInvalidCreatingDescription() throws Exception {
		IWorkspace wsp = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = wsp.getRoot();
		
		p2 = root.getProject(PROJ_NAME_PREFIX + "2");
		p2.create(null);
		p2.open(null);
		
		CProjectHelper.addNatureToProject(p2, CProjectNature.C_NATURE_ID, null);
		
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		ICProjectDescription des = mngr.createProjectDescription(p2, false, true);
		des.setSessionProperty(new QualifiedName(CTestPlugin.PLUGIN_ID, "tmp"), "tmp");
		
		assertFalse(des.isValid());
		
		boolean failed = false;
		try {
			mngr.setProjectDescription(p2, des);
		} catch (CoreException e){
			failed = true;
		}
		
		assertFalse(failed);
		
		assertNotNull(mngr.getProjectDescription(p2, false));
		assertNotNull(mngr.getProjectDescription(p2, true));
		
		des = mngr.getProjectDescription(p2, true);
		ICConfigurationDescription cfg = mngr.getPreferenceConfiguration(TestCfgDataProvider.PROVIDER_ID, true);
		cfg = des.createConfiguration(CDataUtil.genId(null), CDataUtil.genId(null), cfg);
		mngr.setProjectDescription(p2, des);
		
	}

	protected void tearDown() throws Exception {
		try {
			if(p1 != null)
				p1.getProject().delete(true, null);
		} catch (CoreException e){
		}
		try {
			if(p2 != null)
				p2.getProject().delete(true, null);
		} catch (CoreException e){
		}
		super.tearDown();
	}

}
