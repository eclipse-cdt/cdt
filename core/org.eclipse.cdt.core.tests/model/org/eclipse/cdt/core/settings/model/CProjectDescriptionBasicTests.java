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
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class CProjectDescriptionBasicTests  extends BaseTestCase{
	private static final String PROJ_NAME_PREFIX = "CProjectDescriptionBasicTests_";
	IProject p1;
	
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

	protected void tearDown() throws Exception {
		try {
			p1.getProject().delete(true, null);
		} catch (CoreException e){
		}
		super.tearDown();
	}

}
