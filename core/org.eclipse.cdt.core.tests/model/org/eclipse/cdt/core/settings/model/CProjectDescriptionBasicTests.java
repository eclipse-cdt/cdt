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
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
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
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

public class CProjectDescriptionBasicTests  extends BaseTestCase{
	private static final String PROJ_NAME_PREFIX = "CProjectDescriptionBasicTests_";
	IProject p1, p2, p3, p4;
	
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
	
	public void testModulesCopiedOnCreateNewConfig() throws Exception {
		ICProject p = CProjectHelper.createNewStileCProject(PROJ_NAME_PREFIX + "c", IPDOMManager.ID_NO_INDEXER);
		p3 = p.getProject();
		
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		ICProjectDescription des = mngr.getProjectDescription(p3);
		ICConfigurationDescription baseCfg = des.getConfigurations()[0];
		final String baseCfgId = baseCfg.getId();
		final String STORAGE_ID = "test.storage_id";
		final String ATTR = "testAttr";
		final String ATTR_VALUE = "testAttrValue";
		final String EL_NAME = "testElName";
		final String ATTR2 = "testAttr2";
		final String ATTR2_VALUE = "testAttr2Value";
		
		ICStorageElement el = baseCfg.getStorage(STORAGE_ID, false);
		assertNull(el);
		el = baseCfg.getStorage(STORAGE_ID, true);
		assertNotNull(el);
		assertNull(el.getAttribute(ATTR));
		el.setAttribute(ATTR, ATTR_VALUE);
		assertEquals(0, el.getChildren().length);
		ICStorageElement child = el.createChild(EL_NAME);
		child.setAttribute(ATTR2, ATTR2_VALUE);
		
		final String newCfgId1 = "cfg1.id";//CDataUtil.genId(null);
//		final String newCfgId2 = CDataUtil.genId(null);
		
		ICConfigurationDescription cfg1 = des.createConfiguration(newCfgId1, newCfgId1 + ".name", baseCfg);
		assertEquals(newCfgId1, cfg1.getId());
		el = cfg1.getStorage(STORAGE_ID, false);
		assertNotNull(el);
		assertEquals(ATTR_VALUE, el.getAttribute(ATTR));
		assertEquals(1, el.getChildren().length);
		child = el.getChildren()[0];
		assertEquals(EL_NAME, child.getName());
		assertEquals(ATTR2_VALUE, child.getAttribute(ATTR2));

		mngr.setProjectDescription(p3, des);
		
		des = mngr.getProjectDescription(p3, false);
		cfg1 = des.getConfigurationById(newCfgId1);
		el = cfg1.getStorage(STORAGE_ID, false);
		assertNotNull(el);
		assertEquals(ATTR_VALUE, el.getAttribute(ATTR));
		assertEquals(1, el.getChildren().length);
		child = el.getChildren()[0];
		assertEquals(EL_NAME, child.getName());
		assertEquals(ATTR2_VALUE, child.getAttribute(ATTR2));

		des = mngr.getProjectDescription(p3, true);
		cfg1 = des.getConfigurationById(newCfgId1);
		el = cfg1.getStorage(STORAGE_ID, false);
		assertNotNull(el);
		assertEquals(ATTR_VALUE, el.getAttribute(ATTR));
		assertEquals(1, el.getChildren().length);
		child = el.getChildren()[0];
		assertEquals(EL_NAME, child.getName());
		assertEquals(ATTR2_VALUE, child.getAttribute(ATTR2));

	}

	public void testSetInvalidCreatingDescription() throws Exception {
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
	
	public void testSetDescriptionWithRootIncompatibleRuleAquired() throws Exception {
		ICProject p = CProjectHelper.createNewStileCProject(PROJ_NAME_PREFIX + "4", IPDOMManager.ID_NO_INDEXER);
		p4 = p.getProject();
		
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		ICProjectDescription des = mngr.getProjectDescription(p4);
		ICConfigurationDescription baseCfg = des.getConfigurations()[0];
		
		baseCfg.setName("qwertyuiop");
		
		IJobManager jm = Job.getJobManager();
		boolean failed = false;
		try {
			jm.beginRule(p4, null);
			
			mngr.setProjectDescription(p4, des);
		} catch (CoreException e) {
			failed = true;
			assertTrue(e.getStatus().getException() instanceof IllegalArgumentException);
		} finally {
			jm.endRule(p4);
		}
		
		assertTrue(failed);
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
		try {
			if(p3 != null)
				p3.getProject().delete(true, null);
		} catch (CoreException e){
		}
		try {
			if(p4 != null)
				p4.getProject().delete(true, null);
		} catch (CoreException e){
		}
		super.tearDown();
	}

}
