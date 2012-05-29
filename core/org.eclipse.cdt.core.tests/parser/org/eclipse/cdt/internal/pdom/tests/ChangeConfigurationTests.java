/*******************************************************************************
 * Copyright (c) 2012 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.pdom.tests;

import java.util.regex.Pattern;

import junit.framework.Test;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexName;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.suite.ProjectCreator;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ChangeConfigurationTests extends BaseTestCase {
	
	public static Test suite() {
		return suite(ChangeConfigurationTests.class);
	}
	
	private void changeConfigRelations(IProject project, int option) throws CoreException {
		ICProjectDescription pd= CCorePlugin.getDefault().getProjectDescription(project);
		pd.setConfigurationRelations(option);
		CCorePlugin.getDefault().setProjectDescription(project, pd);
		CCorePlugin.getIndexManager().joinIndexer(8000, npm());
	}
	
	private void changeProjectConfiguration(IProject project, String configName) throws CoreException, InterruptedException {
		ICProjectDescription prjd = CCorePlugin.getDefault().getProjectDescriptionManager().getProjectDescription(project);
		ICConfigurationDescription[] configs = prjd.getConfigurations(); 
		if (configs != null && configs.length > 0) {
			for (ICConfigurationDescription config : configs) {
				if (config.getName().equals(configName)) {
					config.setActive();
					// Emulates CDTPropertyManager.performOk(null); in ChangeConfigAction
					CoreModel.getDefault().setProjectDescription(project, prjd);
					break;
				}
			}
		}
	}
	
	public void testRepeatedlyChangeConfig_bug375226() throws Exception {
		ModelJoiner mj = new ModelJoiner();
		IProject project = ProjectCreator.createProject(new Path("resources/pdomtests/changeConfiguration.zip"), "testChangeConfiguration");
		ICProject cProject = CModelManager.getDefault().create(project);
		changeConfigRelations(project, ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
		mj.join();
		mj.dispose();
		waitForIndexer(cProject);
		
		Pattern pattern = Pattern.compile("foo");
		int i = 0, noTrials = 50;
		do {
			IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
			index.acquireReadLock();
			IBinding[] bindings = index.findBindings(pattern, true, IndexFilter.ALL, new NullProgressMonitor());
			assertEquals(1, bindings.length);
			IIndexName[] references = index.findReferences(bindings[0]);
			assertEquals(1, references.length);
			index.releaseReadLock();
			i++;
			changeProjectConfiguration(project, i % 2 == 0 ? "Debug" : "Release");
			waitForIndexer(cProject);
		} while (i < noTrials);		
	}
}
