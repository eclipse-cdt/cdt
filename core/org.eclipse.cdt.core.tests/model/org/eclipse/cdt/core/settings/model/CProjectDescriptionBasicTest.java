/*******************************************************************************
 * Copyright (c) 2007, 2015 Intel Corporation and others.
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
package org.eclipse.cdt.core.settings.model;

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
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;

import junit.framework.TestSuite;

public class CProjectDescriptionBasicTests extends BaseTestCase {
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
		} catch (CoreException e) {
			failed = true;
		}

		assertTrue(failed);
	}

	public void testModulesCopiedOnCreateNewConfig() throws Exception {
		ICProject p = CProjectHelper.createNewStyleCProject(PROJ_NAME_PREFIX + "c", IPDOMManager.ID_NO_INDEXER);
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

	public void testCreateProjectDescriptionInvalidProject() throws Exception {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("someProject");
		assertTrue(!project.isAccessible());

		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		boolean exception = false;
		try {
			mngr.createProjectDescription(null, false, true);
		} catch (CoreException e) {
			exception = true;
		}
		assertTrue(exception);

		exception = false;
		try {
			mngr.createProjectDescription(project, false, true);
		} catch (CoreException e) {
			exception = true;
		}
		assertTrue(exception);
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
		} catch (CoreException e) {
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
		ICProject p = CProjectHelper.createNewStyleCProject(PROJ_NAME_PREFIX + "4", IPDOMManager.ID_NO_INDEXER);
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

	public void testBug242955() throws Exception {
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescriptionManager mngr = coreModel.getProjectDescriptionManager();

		String projectName = "testBug242955";

		String defaultConfigurationName = "Default";
		String newConfigurationName = "NEW-NAME";

		// Emulate entering Eclipse first time
		{
			// Create model project and accompanied descriptions
			ICProject cproject = CProjectHelper.createNewStyleCProject(projectName, IPDOMManager.ID_NO_INDEXER);
			IProject project = cproject.getProject();

			// Initial project description after opening a project
			ICProjectDescription initialProjectDescription = mngr.getProjectDescription(project);
			assertNotNull("createDescription returned null!", initialProjectDescription);
			assertEquals(1, initialProjectDescription.getConfigurations().length);

			// Initial configuration description
			ICConfigurationDescription initialDefaultConfigurationDescription = initialProjectDescription
					.getConfigurations()[0];
			initialDefaultConfigurationDescription.setName(defaultConfigurationName);
			assertEquals(defaultConfigurationName, initialDefaultConfigurationDescription.getName());
			mngr.setProjectDescription(project, initialProjectDescription);

			// Properties window: get project description: prjd
			ICProjectDescription propertyProjectDescription = CoreModel.getDefault().getProjectDescription(project);

			// Dialog Manage-configurations-New-"NEW-NAME", from "Default" configuration
			final String newConfigurationId = newConfigurationName + ".id";
			ICConfigurationDescription propertyDefaultConfigurationDescription = propertyProjectDescription
					.getConfigurations()[0];
			// creating new configuration in "Property" project description
			ICConfigurationDescription propertyNewConfigurationDescription = propertyProjectDescription
					.createConfiguration(newConfigurationId, newConfigurationName,
							propertyDefaultConfigurationDescription);
			assertNotNull(propertyNewConfigurationDescription);
			assertEquals(2, propertyProjectDescription.getConfigurations().length);
			assertEquals(defaultConfigurationName, propertyProjectDescription.getConfigurations()[0].getName());
			assertEquals(newConfigurationName, propertyProjectDescription.getConfigurations()[1].getName());

			// Apply button, local_prjd: copy "Property" new configuration description to "Applied" project description
			ICProjectDescription applyButtonProjectDescription = coreModel.getProjectDescription(project);
			ICConfigurationDescription applyButtonNewConfigurationDescription = applyButtonProjectDescription
					.createConfiguration(propertyNewConfigurationDescription.getId(),
							propertyNewConfigurationDescription.getName(), propertyNewConfigurationDescription);

			// OK button, persist the property project description prjd.
			coreModel.setProjectDescription(project, propertyProjectDescription);
			assertEquals(2, propertyProjectDescription.getConfigurations().length);
			assertEquals(defaultConfigurationName, propertyProjectDescription.getConfigurations()[0].getName());
			assertEquals(newConfigurationName, propertyProjectDescription.getConfigurations()[1].getName());

			// Close project
			project.close(null);
		}

		// Emulate re-entering Eclipse
		{
			// Re-open project and re-load project description
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();

			IWorkspaceDescription workspaceDesc = workspace.getDescription();
			workspaceDesc.setAutoBuilding(false);
			workspace.setDescription(workspaceDesc);

			IProject project = root.getProject(projectName);
			project.open(null);
			assertEquals(true, project.isOpen());

			// project description after reopening the project
			ICProjectDescription reopenedProjectDescription = coreModel.getProjectDescription(project, false);
			assertEquals(2, reopenedProjectDescription.getConfigurations().length);
			assertEquals(defaultConfigurationName, reopenedProjectDescription.getConfigurations()[0].getName());
			assertEquals(newConfigurationName, reopenedProjectDescription.getConfigurations()[1].getName());

			project.close(null);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (p1 != null)
				p1.getProject().delete(true, null);
		} catch (CoreException e) {
		}
		try {
			if (p2 != null)
				p2.getProject().delete(true, null);
		} catch (CoreException e) {
		}
		try {
			if (p3 != null)
				p3.getProject().delete(true, null);
		} catch (CoreException e) {
		}
		try {
			if (p4 != null)
				p4.getProject().delete(true, null);
		} catch (CoreException e) {
		}
		super.tearDown();
	}

}
