/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.CoreModelUtil;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationData;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.runtime.CoreException;


/**
 * Test ICConfigurationDescription reference behaviours
 */
public class CConfigurationDescriptionReferenceTests extends BaseTestCase {
	ICProject p1, p2, p3, p4;
	ICConfigurationDescription p1cd1, p1cd2, p1cd3;
	ICConfigurationDescription p2cd1, p2cd2, p2cd3;
	ICConfigurationDescription p3cd1, p3cd2, p3cd3;
	ICConfigurationDescription p4cd1, p4cd2, p4cd3;
	
	public static TestSuite suite() {
		return suite(CConfigurationDescriptionReferenceTests.class, "_");
	}
	
	@Override
	protected void setUp() throws Exception {
		p1 = CProjectHelper.createCCProject("p1", "bin");
		p2 = CProjectHelper.createCCProject("p2", "bin");
		p3 = CProjectHelper.createCCProject("p3", "bin");
		p4 = CProjectHelper.createCCProject("p4", "bin");
		
		CoreModel coreModel = CoreModel.getDefault();
		ICProjectDescription des1 = coreModel.getProjectDescription(p1.getProject());
		ICProjectDescription des2 = coreModel.getProjectDescription(p2.getProject());
		ICProjectDescription des3 = coreModel.getProjectDescription(p3.getProject());
		ICProjectDescription des4 = coreModel.getProjectDescription(p4.getProject());
		
		p1cd1 = newCfg(des1, "p1", "cd1");
		p1cd2 = newCfg(des1, "p1", "cd2");
		p1cd3 = newCfg(des1, "p1", "cd3");
		
		p2cd1 = newCfg(des2, "p2", "cd1");
		p2cd2 = newCfg(des2, "p2", "cd2");
		p2cd3 = newCfg(des2, "p2", "cd3");
		
		p3cd1 = newCfg(des3, "p3", "cd1");
		p3cd2 = newCfg(des3, "p3", "cd2");
		p3cd3 = newCfg(des3, "p3", "cd3");
		
		p4cd1 = newCfg(des4, "p4", "cd1");
		p4cd2 = newCfg(des4, "p4", "cd2");
		p4cd3 = newCfg(des4, "p4", "cd3");
		
		/*
		 * Setup references:
		 * 
		 * p1: cd1 cd2 cd3
		 *        \ | /
		 *         \|/
		 *          *
		 *         /|\
		 *        / | \
		 * p2: cd1 cd2 cd3
		 *      |   |   |
		 * p3: cd1 cd2 cd3
		 *        \ | /
		 *         \|/
		 * p4: cd1 cd2 cd3
		 */
		
		setRefs(p1cd1, new ICConfigurationDescription[] {p2cd3});
		setRefs(p1cd2, new ICConfigurationDescription[] {p2cd2});
		setRefs(p1cd3, new ICConfigurationDescription[] {p2cd1});

		setRefs(p2cd1, new ICConfigurationDescription[] {p3cd1});
		setRefs(p2cd2, new ICConfigurationDescription[] {p3cd2});
		setRefs(p2cd3, new ICConfigurationDescription[] {p3cd3});

		setRefs(p3cd1, new ICConfigurationDescription[] {p4cd2});
		setRefs(p3cd2, new ICConfigurationDescription[] {p4cd2});
		setRefs(p3cd3, new ICConfigurationDescription[] {p4cd2});
	
		coreModel.setProjectDescription(p1.getProject(), des1);
		coreModel.setProjectDescription(p2.getProject(), des2);
		coreModel.setProjectDescription(p3.getProject(), des3);
		coreModel.setProjectDescription(p4.getProject(), des4);
	}

	private void setRefs(ICConfigurationDescription node, ICConfigurationDescription[] refs) {
		Map p1RefData = new LinkedHashMap<String, String>();
		for (ICConfigurationDescription ref : refs) {
			String projectName = ref.getProjectDescription().getName();
			p1RefData.put(projectName, ref.getId());
		}
		node.setReferenceInfo(p1RefData);
	}

	private ICConfigurationDescription newCfg(ICProjectDescription des, String project, String config) throws CoreException {
		CDefaultConfigurationData data= new CDefaultConfigurationData(project+"."+config, project+" "+config+" name", null);
		data.initEmptyData();
		return des.createConfiguration(CCorePlugin.DEFAULT_PROVIDER_ID, data);		
	}
		
	public void testConfigurationDescriptionReference() throws CoreException {
		// references
		
		assertEdges(p1cd1, new ICConfigurationDescription[] {p2cd3}, true);
		assertEdges(p1cd2, new ICConfigurationDescription[] {p2cd2}, true);
		assertEdges(p1cd3, new ICConfigurationDescription[] {p2cd1}, true);
		
		assertEdges(p2cd1, new ICConfigurationDescription[] {p3cd1}, true);
		assertEdges(p2cd2, new ICConfigurationDescription[] {p3cd2}, true);
		assertEdges(p2cd3, new ICConfigurationDescription[] {p3cd3}, true);
		
		assertEdges(p3cd1, new ICConfigurationDescription[] {p4cd2}, true);
		assertEdges(p3cd2, new ICConfigurationDescription[] {p4cd2}, true);
		assertEdges(p3cd3, new ICConfigurationDescription[] {p4cd2}, true);
		
		assertEdges(p4cd1, new ICConfigurationDescription[] {}, true);
		assertEdges(p4cd2, new ICConfigurationDescription[] {}, true);
		assertEdges(p4cd3, new ICConfigurationDescription[] {}, true);
	}
	
	public void testConfigurationDescriptionReferencing() throws CoreException {
		// referencing
		
		assertEdges(p1cd1, new ICConfigurationDescription[] {}, false);
		assertEdges(p1cd2, new ICConfigurationDescription[] {}, false);
		assertEdges(p1cd3, new ICConfigurationDescription[] {}, false);
		
		assertEdges(p2cd1, new ICConfigurationDescription[] {p1cd3}, false);
		assertEdges(p2cd2, new ICConfigurationDescription[] {p1cd2}, false);
		assertEdges(p2cd3, new ICConfigurationDescription[] {p1cd1}, false);
		
		assertEdges(p3cd1, new ICConfigurationDescription[] {p2cd1}, false);
		assertEdges(p3cd2, new ICConfigurationDescription[] {p2cd2}, false);
		assertEdges(p3cd3, new ICConfigurationDescription[] {p2cd3}, false);
		
		assertEdges(p4cd1, new ICConfigurationDescription[] {}, false);
		assertEdges(p4cd2, new ICConfigurationDescription[] {p3cd1, p3cd2, p3cd3}, false);
		assertEdges(p4cd3, new ICConfigurationDescription[] {}, false);
	}

	/**
	 * Test that the the referencing mechanism preserves order
	 */
	public void testDependencyOrder() throws CoreException {
		ICProject p1 = null;
		ICProject p2 = null;
		ICProject p3 = null;
		try {
			String p1Name = "referenceDependency";
			String p2Name = "refereeDependency";
			String p3Name = "referee2Dependency";
			p1 = CProjectHelper.createCCProject(p1Name, "bin");
			p2 = CProjectHelper.createCCProject(p2Name, "bin");
			p3 = CProjectHelper.createCCProject(p3Name, "bin");

			CoreModel coreModel = CoreModel.getDefault();
			ICProjectDescription des1 = coreModel.getProjectDescription(p1.getProject());
			ICProjectDescription des2 = coreModel.getProjectDescription(p2.getProject());
			ICProjectDescription des3 = coreModel.getProjectDescription(p3.getProject());
			ICConfigurationDescription p1cd1 = newCfg(des1, p1Name, "p1cd1");
			ICConfigurationDescription p2cd1 = newCfg(des2, p2Name, "p2cd1");
			ICConfigurationDescription p3cd1 = newCfg(des3, p2Name, "p3cd1");

			/* Setup references:
			 *
			 * p1: cd1
			 *      | \
			 *      |  \
			 * p2: cd1  \
			 * p3:      cd1
			 */
			setRefs(p1cd1, new ICConfigurationDescription[] {p2cd1, p3cd1});
			coreModel.setProjectDescription(p1.getProject(), des1);
			coreModel.setProjectDescription(p2.getProject(), des2);
			coreModel.setProjectDescription(p3.getProject(), des3);

			// Check that the order is persisted
			ICConfigurationDescription[] cfgs;
			cfgs = CoreModelUtil.getReferencedConfigurationDescriptions(p1cd1, false);
			assertTrue(cfgs.length == 2);
			assertEquals(cfgs[0].getId(), p2cd1.getId());
			assertEquals(cfgs[1].getId(), p3cd1.getId());

			// Swap them round and check that the order is still persisted...
			setRefs(p1cd1, new ICConfigurationDescription[] {p3cd1, p2cd1});
			coreModel.setProjectDescription(p1.getProject(), des1);
			cfgs = CoreModelUtil.getReferencedConfigurationDescriptions(p1cd1, false);
			assertTrue(cfgs.length == 2);
			assertEquals(cfgs[0].getId(), p3cd1.getId());
			assertEquals(cfgs[1].getId(), p2cd1.getId());
		} finally {
			if (p1 != null)
				try {
					p1.getProject().delete(true, npm());
				} catch (CoreException e){}
			if (p2 != null)
				try {
					p2.getProject().delete(true, npm());
				} catch (CoreException e){}
			if (p3 != null)
				try {
					p3.getProject().delete(true, npm());
				} catch (CoreException e){}
		}
	}

	protected void assertEdges(ICConfigurationDescription cfgDes, ICConfigurationDescription[] expected, boolean references) {
		ICConfigurationDescription[] actual;
		
		if(references) {
			actual= CoreModelUtil.getReferencedConfigurationDescriptions(cfgDes, false);
		} else {
			actual= CoreModelUtil.getReferencingConfigurationDescriptions(cfgDes, false);
		}
		
		assertEquals(expected.length, actual.length);
		
		List actualIds = new ArrayList();
		for (ICConfigurationDescription element : actual) {
			actualIds.add(element.getId());
		}
		// check for each ID, don't use a Set so we detect duplicates
		for (ICConfigurationDescription element : expected) {
			assertTrue(element.getId()+" is missing", actualIds.contains(element.getId()));
		}
	}
	
	@Override
	protected void tearDown() throws Exception {
		for (Object element : Arrays.asList(new ICProject[]{p1,p2,p3,p4})) {
			ICProject project = (ICProject) element;
			try {
				project.getProject().delete(true, npm());
			} catch(CoreException ce) {
				// try next one..
			}
		}
	}
}
