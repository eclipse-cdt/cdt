/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
		Map p1RefData = new HashMap();
		for(int i=0; i<refs.length; i++) {
			String projectName = refs[i].getProjectDescription().getName();
			p1RefData.put(projectName, refs[i].getId());
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
	
	protected void assertEdges(ICConfigurationDescription cfgDes, ICConfigurationDescription[] expected, boolean references) {
		ICConfigurationDescription[] actual;
		
		if(references) {
			actual= CoreModelUtil.getReferencedConfigurationDescriptions(cfgDes, false);
		} else {
			actual= CoreModelUtil.getReferencingConfigurationDescriptions(cfgDes, false);
		}
		
		assertEquals(expected.length, actual.length);
		
		List actualIds = new ArrayList();
		for(int i=0; i<actual.length; i++) {
			actualIds.add(actual[i].getId());
		}
		// check for each ID, don't use a Set so we detect duplicates
		for(int i=0; i<expected.length; i++) {
			assertTrue(expected[i].getId()+" is missing", actualIds.contains(expected[i].getId()));
		}
	}
	
	protected void tearDown() throws Exception {
		for(Iterator i = Arrays.asList(new ICProject[]{p1,p2,p3,p4}).iterator(); i.hasNext(); ) {
			ICProject project = (ICProject) i.next();
			try {
				project.getProject().delete(true, NPM);
			} catch(CoreException ce) {
				// try next one..
			}
		}
	}
}
