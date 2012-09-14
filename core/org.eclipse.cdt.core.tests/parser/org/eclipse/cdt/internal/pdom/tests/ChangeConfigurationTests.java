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
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.CTestPlugin;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ChangeConfigurationTests extends PDOMTestBase {
	
	public static Test suite() {
		return suite(ChangeConfigurationTests.class);
	}
	
	private void changeConfigRelations(ICProject project, int option) throws CoreException, InterruptedException {
		ICProjectDescription pd= CCorePlugin.getDefault().getProjectDescription(project.getProject());
		pd.setConfigurationRelations(option);
		CCorePlugin.getDefault().setProjectDescription(project.getProject(), pd);
		waitForIndexer(project);
	}
	
	// Emulates ChangeConfigAction
	private void changeProjectConfiguration(IProject project, String configName) throws CoreException, InterruptedException {
		ICProjectDescription prjd = CCorePlugin.getDefault().getProjectDescriptionManager().getProjectDescription(project);
		ICConfigurationDescription[] configs = prjd.getConfigurations(); 
		if (configs != null && configs.length > 0) {
			for (ICConfigurationDescription config : configs) {
				if (config.getName().equals(configName)) {
					config.setActive();
					CoreModel.getDefault().setProjectDescription(project, prjd);
					break;
				}
			}
		}
	}

	//#ifdef MACRO1
	//void testFunc1();
	//#endif
	//#ifdef MACRO2
	//void testFunc2();
	//#endif
	public void testRepeatedlyChangeConfig_bug375226() throws Exception {
		ModelJoiner mj = new ModelJoiner();
		ICProject cProject = CProjectHelper.createNewStileCProject("testChangeConfiguration", IPDOMManager.ID_FAST_INDEXER);
		IProject project = cProject.getProject();
		StringBuilder[] contents= TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "parser", getClass(), getName(), 1);
		IFile file= TestSourceReader.createFile(cProject.getProject(), new Path("test.c"), contents[0].toString());
		mj.join();
		mj.dispose();
		changeConfigRelations(cProject, ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
		
		ICProjectDescription prjd = CCorePlugin.getDefault().getProjectDescriptionManager().getProjectDescription(project);
		ICConfigurationDescription configuration1 = prjd.getConfigurations()[0];
		String firstConfigName = configuration1.getName();
		for(ICLanguageSetting languageSetting : configuration1.getRootFolderDescription().getLanguageSettings())
		{
			languageSetting.setSettingEntries(ICSettingEntry.MACRO, new ICLanguageSettingEntry[] { CDataUtil.createCMacroEntry("MACRO1", null, 0)});
		}

		ICConfigurationDescription configuration2 = prjd.createConfiguration("id2", "Configuration2", configuration1);
		String secondConfigName = configuration2.getName();
		for(ICLanguageSetting languageSetting : configuration2.getRootFolderDescription().getLanguageSettings())
		{
			languageSetting.setSettingEntries(ICSettingEntry.MACRO, new ICLanguageSettingEntry[] { CDataUtil.createCMacroEntry("MACRO2", null, 0)} );
		}
		
		CoreModel.getDefault().setProjectDescription(project, prjd);
		CCorePlugin.getIndexManager().reindex(cProject);
		waitForIndexer(cProject);
		
		Pattern testFunc1 = Pattern.compile("testFunc1");
		Pattern testFunc2 = Pattern.compile("testFunc2");
		int i = 0, noTrials = 50;
		do {
			IIndex index = CCorePlugin.getIndexManager().getIndex(cProject);
			index.acquireReadLock();
			boolean isFirstConfig = i % 2 == 0;
			IBinding[] bindings = index.findBindings(isFirstConfig ? testFunc1 : testFunc2, true, IndexFilter.ALL, new NullProgressMonitor());
			IBinding[] noBindings = index.findBindings(isFirstConfig ? testFunc2 : testFunc1, true, IndexFilter.ALL, new NullProgressMonitor());
			assertEquals(1, bindings.length);
			assertEquals(0, noBindings.length);
			index.releaseReadLock();
			
			String nextConfig = isFirstConfig ? secondConfigName : firstConfigName;
			changeProjectConfiguration(project, nextConfig);
			waitForIndexer(cProject);
			
			i++;
		} while (i < noTrials);
	}
}
