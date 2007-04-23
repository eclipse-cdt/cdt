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

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

public class CfgSettingsTests extends BaseTestCase {
	private static final String PROJ_NAME_PREFIX = "sfgst_";
	ICProject p1;
	
	public static TestSuite suite() {
		return suite(CfgSettingsTests.class, "_");
	}
	
	protected void setUp() throws Exception {
	}
	
	public void testDefaultSettingConfig() throws Exception {
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		ICProjectDescriptionWorkspacePreferences prefs = mngr.getProjectDescriptionWorkspacePreferences(true);
		
		int wspRel = prefs.getConfigurationReltations();
		CoreModel model = CoreModel.getDefault();
		p1 = CProjectHelper.createNewStileCProject(PROJ_NAME_PREFIX + "a", IPDOMManager.ID_NO_INDEXER);
		IProject project = p1.getProject();
		ICProjectDescription des = model.getProjectDescription(project, false);
		assertEquals(wspRel, des.getConfigurationReltations());
		assertTrue(des.isDefaultConfigurationRelations());
		prefs.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_INDEPENDENT);
		assertEquals(wspRel, des.getConfigurationReltations());
		assertEquals(wspRel, mngr.getProjectDescriptionWorkspacePreferences(true).getConfigurationReltations());
		prefs.setConfigurationRelations(ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
		assertEquals(wspRel, des.getConfigurationReltations());
		assertEquals(wspRel, mngr.getProjectDescriptionWorkspacePreferences(true).getConfigurationReltations());
		assertTrue(des.isDefaultConfigurationRelations());
		wspRel = getChangedConfigRelStatus(wspRel);

		prefs.setConfigurationRelations(wspRel);
		
		mngr.setProjectDescriptionWorkspacePreferences(prefs, true, null);
		des = model.getProjectDescription(project, false);
		prefs = mngr.getProjectDescriptionWorkspacePreferences(true);
		assertEquals(wspRel, des.getConfigurationReltations());
		assertEquals(wspRel, prefs.getConfigurationReltations());
		assertTrue(des.isDefaultConfigurationRelations());
		
		des = mngr.getProjectDescription(project);
		assertTrue(des.isDefaultConfigurationRelations());
		wspRel = prefs.getConfigurationReltations();
		assertEquals(wspRel, des.getConfigurationReltations());
		wspRel = getChangedConfigRelStatus(wspRel);
		prefs.setConfigurationRelations(wspRel);
		assertTrue(wspRel != des.getConfigurationReltations());
		mngr.setProjectDescriptionWorkspacePreferences(prefs, false, null);
		assertEquals(wspRel, des.getConfigurationReltations());
		mngr.setProjectDescription(des.getProject(), des);
		des = mngr.getProjectDescription(project, false);
		assertEquals(wspRel, des.getConfigurationReltations());
		
		des = mngr.getProjectDescription(project);
		prefs = mngr.getProjectDescriptionWorkspacePreferences(false);
		assertEquals(des.getConfigurationReltations(), prefs.getConfigurationReltations());
		assertTrue(des.isDefaultConfigurationRelations());
		wspRel = prefs.getConfigurationReltations();
		int projRel = getChangedConfigRelStatus(wspRel);
		des.setConfigurationRelations(projRel);
		assertFalse(des.isDefaultConfigurationRelations());
		assertEquals(projRel, des.getConfigurationReltations());
		mngr.setProjectDescription(project, des);
		
		des = mngr.getProjectDescription(project, false);
		assertFalse(des.isDefaultConfigurationRelations());
		assertEquals(projRel, des.getConfigurationReltations());
		
		des = mngr.getProjectDescription(project, true);
		assertFalse(des.isDefaultConfigurationRelations());
		assertEquals(projRel, des.getConfigurationReltations());

		ICConfigurationDescription aCfg = des.getActiveConfiguration(); 
		ICConfigurationDescription sCfg = des.getDefaultSettingConfiguration();
		assertEquals(aCfg, sCfg);
		
		des.createConfiguration("qq.2", "test2", des.getConfigurations()[0]);

		assertEquals(aCfg, des.getActiveConfiguration());
		assertEquals(sCfg, des.getActiveConfiguration());
		
		projRel = getChangedConfigRelStatus(projRel);
		des.setConfigurationRelations(projRel);
		assertEquals(aCfg, des.getActiveConfiguration());
		assertEquals(sCfg, des.getActiveConfiguration());
		assertFalse(des.isDefaultConfigurationRelations());
		
		projRel = ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE;
		des.setConfigurationRelations(projRel);
		ICConfigurationDescription cfg2 = des.getConfigurationById("qq.2");
		assertNotNull(cfg2);
		des.setActiveConfiguration(cfg2);
		assertEquals(cfg2, des.getActiveConfiguration());
		assertEquals(cfg2, des.getDefaultSettingConfiguration());

		projRel = ICProjectDescriptionPreferences.CONFIGS_INDEPENDENT;
		des.setConfigurationRelations(projRel);
		des.setActiveConfiguration(aCfg);
		assertEquals(aCfg, des.getActiveConfiguration());
		assertEquals(cfg2, des.getDefaultSettingConfiguration());

		des.setDefaultSettingConfiguration(aCfg);
		assertEquals(aCfg, des.getActiveConfiguration());
		assertEquals(aCfg, des.getDefaultSettingConfiguration());

		des.setDefaultSettingConfiguration(cfg2);
		assertEquals(aCfg, des.getActiveConfiguration());
		assertEquals(cfg2, des.getDefaultSettingConfiguration());

		mngr.setProjectDescription(project, des);
		
		des = mngr.getProjectDescription(project, false);
		assertEquals(aCfg.getId(), des.getActiveConfiguration().getId());
		assertEquals(cfg2.getId(), des.getDefaultSettingConfiguration().getId());

		des = mngr.getProjectDescription(project, true);
		assertEquals(aCfg.getId(), des.getActiveConfiguration().getId());
		assertEquals(cfg2.getId(), des.getDefaultSettingConfiguration().getId());
	}
	
	private int getChangedConfigRelStatus(int status){
		if(status == ICProjectDescriptionPreferences.CONFIGS_INDEPENDENT)
			return ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE;
		return ICProjectDescriptionPreferences.CONFIGS_INDEPENDENT;
	}
	
	protected void tearDown() throws Exception {
		try {
			if(p1 != null){
				p1.getProject().delete(true, null);
				p1 = null;
			}
		} catch (CoreException e){
		}
	}
}
