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
package org.eclipse.cdt.internal.index.tests;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.ICdtVariablesContributor;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.provider.IIndexProvider;
import org.eclipse.cdt.core.internal.index.provider.test.DummyProvider1;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.settings.model.ICBuildSetting;
import org.eclipse.cdt.core.settings.model.ICConfigExtensionReference;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICExternalSetting;
import org.eclipse.cdt.core.settings.model.ICFileDescription;
import org.eclipse.cdt.core.settings.model.ICFolderDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionPreferences;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.core.settings.model.ICSettingContainer;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingObject;
import org.eclipse.cdt.core.settings.model.ICSourceEntry;
import org.eclipse.cdt.core.settings.model.ICStorageElement;
import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
import org.eclipse.cdt.core.settings.model.WriteAccessException;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.core.settings.model.extension.impl.CDefaultConfigurationData;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.provider.IIndexFragmentProvider;
import org.eclipse.cdt.internal.core.index.provider.IndexProviderManager;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;

/**
 * Example usage and test for IIndexProvider
 */
public class IndexProviderManagerTest extends IndexTestBase {
	final CCorePlugin core= CCorePlugin.getDefault();
	
	public IndexProviderManagerTest() {
		super("IndexProviderManagerTest");
	}

	public static TestSuite suite() {
		return suite(IndexProviderManagerTest.class);
	}

	public void testProvider_SimpleLifeCycle() throws Exception {
		DummyProvider1.reset();
		List cprojects = new ArrayList(), expectedTrace = new ArrayList();
		try {
			for(int i=0; i<3; i++) {
				ICProject cproject = CProjectHelper.createCProject("P"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
				IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
				cprojects.add(cproject);
				expectedTrace.add(cproject);
			}
			assertEquals(expectedTrace, DummyProvider1.getProjectsTrace());
			for(int i=0; i<expectedTrace.size(); i++) {
				ICProject cproject = (ICProject) expectedTrace.get(i);
				IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
			}
			assertEquals(expectedTrace, DummyProvider1.getProjectsTrace());
		} finally {
			for(int i=0; i<cprojects.size(); i++) {
				ICProject cproject = (ICProject) expectedTrace.get(i);
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}

	public void testProvider_OverDeleteAndAdd() throws Exception {
		DummyProvider1.reset();
		List expectedTrace = new ArrayList();
		ICProject cproject = null;
		try {
			String name = "P"+System.currentTimeMillis();
			cproject = CProjectHelper.createCProject(name, "bin", IPDOMManager.ID_NO_INDEXER);
			IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
			expectedTrace.add(cproject);
			assertEquals(expectedTrace, DummyProvider1.getProjectsTrace());

			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			cproject = CProjectHelper.createCProject(name, "bin", IPDOMManager.ID_NO_INDEXER);
			index = CCorePlugin.getIndexManager().getIndex(cproject);
			expectedTrace.add(cproject);
			assertEquals(expectedTrace, DummyProvider1.getProjectsTrace());
		} finally {
			if(cproject!=null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}

	public void testProvider_OverMove() throws Exception {
		DummyProvider1.reset();
		List cprojects = new ArrayList();
		List expectedTrace = new ArrayList();

		/* n.b. here we test for the current implementation expected behaviour,
		 * not the contract of IIndexProvider.providesFor
		 */

		ICProject cproject = null;
		try {
			String name = "P"+System.currentTimeMillis();
			cproject = CProjectHelper.createCProject(name, "bin", IPDOMManager.ID_NO_INDEXER);
			IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
			expectedTrace.add(cproject);
			assertEquals(expectedTrace, DummyProvider1.getProjectsTrace());

			// move the project to a random new location
			File newLocation = CProjectHelper.freshDir();
			IProjectDescription description = cproject.getProject().getDescription();
			description.setLocationURI(newLocation.toURI());
			cproject.getProject().move(description, IResource.FORCE | IResource.SHALLOW, new NullProgressMonitor());	

			index = CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(expectedTrace, DummyProvider1.getProjectsTrace());
		} finally {
			if(cproject!=null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}
	
	public void testIndexFactoryConfigurationUsage() throws Exception {
		IIndex index;
		
		ICProject cproject= CProjectHelper.createCCProject("IndexFactoryConfigurationUsageTest", IPDOMManager.ID_NO_INDEXER);
		IProject project= cproject.getProject();
		
		ICProjectDescription pd= core.getProjectDescription(project);
		ICConfigurationDescription cfg1= newCfg(pd, "project", "config1");
		ICConfigurationDescription cfg2= newCfg(pd, "project", "config2");
		core.setProjectDescription(project, pd);
		
		index= CCorePlugin.getIndexManager().getIndex(cproject);
		CCorePlugin.getIndexManager().joinIndexer(8000, NPM);
		
		try {			
			DummyProvider1.reset();
			changeConfigRelations(project, ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
			assertEquals(0, DummyProvider1.getProjectsTrace().size());
			assertEquals(0, DummyProvider1.getCfgsTrace().size());
			
			changeActiveConfiguration(project, cfg1);
			DummyProvider1.reset();
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(0, DummyProvider1.getProjectsTrace().size());
			assertEquals(1, DummyProvider1.getCfgsTrace().size());
			assertEquals("project.config1", ((ICConfigurationDescription)DummyProvider1.getCfgsTrace().get(0)).getId());
			
			changeActiveConfiguration(project, cfg2);
			DummyProvider1.reset();
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(0, DummyProvider1.getProjectsTrace().size());
			assertEquals(1, DummyProvider1.getCfgsTrace().size());
			assertEquals("project.config2", ((ICConfigurationDescription)DummyProvider1.getCfgsTrace().get(0)).getId());
			
			DummyProvider1.reset();
			changeConfigRelations(project, ICProjectDescriptionPreferences.CONFIGS_INDEPENDENT);
			assertEquals(0, DummyProvider1.getProjectsTrace().size());
			assertEquals(0, DummyProvider1.getCfgsTrace().size());
			
			changeActiveConfiguration(project, cfg1);
			DummyProvider1.reset();
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(0, DummyProvider1.getProjectsTrace().size());
			assertEquals(1, DummyProvider1.getCfgsTrace().size());
			// should still be config2, as the change in active configuration does not matter
			assertEquals("project.config2", ((ICConfigurationDescription)DummyProvider1.getCfgsTrace().get(0)).getId());
			
			changeActiveConfiguration(project, cfg2);
			DummyProvider1.reset();
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(0, DummyProvider1.getProjectsTrace().size());
			assertEquals(1, DummyProvider1.getCfgsTrace().size());
			// there should be no change from the previous state (also config2)
			assertEquals("project.config2", ((ICConfigurationDescription)DummyProvider1.getCfgsTrace().get(0)).getId());
		} finally {
			if (cproject != null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}
	
	public void testGetProvidedFragments() throws Exception {
		ICProject cproject= CProjectHelper.createCProject("IndexProviderManagerTest", "bin", IPDOMManager.ID_NO_INDEXER);

		try {
			MockState mockState = new MockState(cproject);
			MockStateIndexFragmentProvider provider1 = new MockStateIndexFragmentProvider(cproject);
			MockStateIndexFragmentProvider provider2 = new MockStateIndexFragmentProvider(cproject);

			IndexProviderManager ipm = ((PDOMManager)CCorePlugin.getIndexManager()).getIndexProviderManager();
			ipm.addIndexProvider(provider1);
			ipm.addIndexProvider(provider2);

			IIndexFragment[] fragments;
			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[0]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			mockState.setConfig(MockState.DBG_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[3]));

			mockState.setConfig(MockState.DBG_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(0, fragments.length);

			mockState.setConfig(MockState.REL_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[1]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[1]));

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[0]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			provider1.invert();

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			mockState.setConfig(MockState.DBG_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[0]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[3]));

			mockState.setConfig(MockState.DBG_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(1, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[1]));

			mockState.setConfig(MockState.REL_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(1, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[1]));

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			provider2.invert();

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[3]));

			mockState.setConfig(MockState.DBG_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[0]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			mockState.setConfig(MockState.DBG_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[1]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[1]));

			mockState.setConfig(MockState.REL_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(0, fragments.length);

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig());
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[3]));
		} finally {
			if (cproject != null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}
	
	private ICConfigurationDescription newCfg(ICProjectDescription des, String project, String config) throws CoreException {
		CDefaultConfigurationData data= new CDefaultConfigurationData(project+"."+config, project+" "+config+" name", null);
		data.initEmptyData();
		return des.createConfiguration(CCorePlugin.DEFAULT_PROVIDER_ID, data);		
	}
	
	private void changeActiveConfiguration(IProject project, ICConfigurationDescription cfg) throws CoreException {
		ICProjectDescription pd= core.getProjectDescription(project);
		pd.setActiveConfiguration(pd.getConfigurationById(cfg.getId()));
		core.setProjectDescription(project, pd);
		CCorePlugin.getIndexManager().joinIndexer(8000, NPM);
	}
	
	private void changeConfigRelations(IProject project, int option) throws CoreException {
		ICProjectDescription pd= core.getProjectDescription(project);
		pd.setConfigurationRelations(option);
		core.setProjectDescription(project, pd);
		CCorePlugin.getIndexManager().joinIndexer(8000, NPM);
	}
}

class MockStateIndexProvider implements IIndexProvider {
	protected ICProject targetProject;

	public MockStateIndexProvider(ICProject cproject) {
		this.targetProject = cproject;
	}

	public boolean providesFor(ICProject cproject) throws CoreException {
		return this.targetProject.equals(cproject);
	}
}

class MockStateIndexFragmentProvider extends MockStateIndexProvider implements IIndexFragmentProvider {
	IIndexFragment[] fragments;
	int[] mcounts;
	boolean invert;

	public void invert() {
		invert = !invert;
	}

	public MockStateIndexFragmentProvider(ICProject cproject) {
		super(cproject);

		fragments = new IIndexFragment[MockState.states.size()];
		mcounts = new int[MockState.states.size()];
		for(int i=0; i<MockState.states.size(); i++) {
			fragments[i] = createMockFragment(mcounts, i);
		}
	}

	public IIndexFragment[] getIndexFragments(ICConfigurationDescription config) throws CoreException {
		int index = MockState.states.indexOf(config.getId());
		index = invert ? (fragments.length-1)-index : index;

		// nb. we're checking this after inverting on purpose
		if(index == MockState.states.indexOf(MockState.DBG_V1_ID)) {
			return new IIndexFragment[0];
		} else {
			return new IIndexFragment[] {fragments[index]};
		}
	}

	public IIndexFragment createMockFragment(final int[] mcounts, final int index) {
		return (IIndexFragment) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {IIndexFragment.class}, new InvocationHandler(){
			public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
				Object result = null;
				if(method.getReturnType().isArray()) {
					result = Array.newInstance(method.getReturnType().getComponentType(), 0);
				}
				if(method.getName().equals("toString")) {
					return "[Mock index fragment #"+index+"]";
				}
				mcounts[index]++;
				return result;
			}
		});
	}
}

class MockConfig implements ICConfigurationDescription {
	String id;
	IProject project;

	MockConfig(String id, IProject project) {
		this.id= id;
		this.project= project;
	}

	public String getId() {
		return id;
	}

	public ICConfigExtensionReference create(String extensionPoint,
			String extension) throws CoreException {
		return null;
	}

	public ICExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIds, String[] extensions,
			ICSettingEntry[] entries) throws WriteAccessException {
		return null;
	}

	public ICFileDescription createFileDescription(IPath path,
			ICResourceDescription base) throws CoreException,
			WriteAccessException {
		return null;
	}

	public ICFolderDescription createFolderDescription(IPath path,
			ICFolderDescription base) throws CoreException,
			WriteAccessException {
		return null;
	}

	public ICConfigExtensionReference[] get(String extensionPointID) {
		return null;
	}

	public ICBuildSetting getBuildSetting() {
		return null;
	}

	public String getBuildSystemId() {
		return null;
	}

	public ICdtVariablesContributor getBuildVariablesContributor() {
		return null;
	}

	public CConfigurationData getConfigurationData() {
		return null;
	}

	public String getDescription() {
		return null;
	}

	public ICExternalSetting[] getExternalSettings() {
		return null;
	}

	public ICFileDescription[] getFileDescriptions() {
		return null;
	}

	public ICFolderDescription[] getFolderDescriptions() {
		return null;
	}

	public ICProjectDescription getProjectDescription() {
		return CoreModel.getDefault().getProjectDescription(project);
	}

	public Map getReferenceInfo() {
		return null;
	}

	public ICResourceDescription getResourceDescription(IPath path,
			boolean exactPath) {
		return null;
	}

	public ICResourceDescription[] getResourceDescriptions() {
		return null;
	}

	public ICFolderDescription getRootFolderDescription() {return null;}
	public Object getSessionProperty(QualifiedName name) {return null;}
	public ICSourceEntry[] getSourceEntries() {return null;}
	public ICTargetPlatformSetting getTargetPlatformSetting() {return null;}
	public boolean isActive() {return false;}
	public boolean isModified() {return false;}
	public boolean isPreferenceConfiguration() {return false;}
	public void remove(ICConfigExtensionReference ext) throws CoreException {}
	public void remove(String extensionPoint) throws CoreException {}
	public void removeExternalSetting(ICExternalSetting setting) throws WriteAccessException {}
	public void removeExternalSettings() throws WriteAccessException {}
	public void removeResourceDescription(ICResourceDescription des)
	throws CoreException, WriteAccessException {}
	public void setActive() throws WriteAccessException {}
	public void setConfigurationData(String buildSystemId,
			CConfigurationData data) throws WriteAccessException {}
	public void setDescription(String des) throws WriteAccessException {}
	public void setName(String name) throws WriteAccessException {}
	public void setReferenceInfo(Map refs) throws WriteAccessException {}
	public void setSessionProperty(QualifiedName name, Object value) {}
	public void setSourceEntries(ICSourceEntry[] entries) throws CoreException,
	WriteAccessException {}
	public ICSettingObject[] getChildSettings() {return null;}
	public ICConfigurationDescription getConfiguration() {return null;}
	public String getName() {return null;}
	public ICSettingContainer getParent() {return null;}
	public int getType() {return 0;}
	public boolean isReadOnly() {return false;}
	public boolean isValid() {return false;}
	public ICStorageElement getStorage(String id, boolean create)
	throws CoreException {
		return null;
	}

	public void removeStorage(String id) throws CoreException {
	}

	public ICLanguageSetting getLanguageSettingForFile(IPath path, boolean ignoreExludeStatus) {
		return null;
	}

	public String[] getExternalSettingsProviderIds() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setExternalSettingsProviderIds(String[] ids) {
		// TODO Auto-generated method stub
		
	}

	public void updateExternalSettingsProviders(String[] ids) {
		// TODO Auto-generated method stub
		
	}

	public ICSourceEntry[] getResolvedSourceEntries() {
		// TODO Auto-generated method stub
		return null;
	}
}

/*
 * This represents a project state, here we use configuration IDs as the only state variable
 */
class MockState {
	public static final String REL_V1_ID = "rel_v1";
	public static final String REL_V2_ID = "rel_v2";
	public static final String DBG_V1_ID = "dbg_v1";
	public static final String DBG_V2_ID = "dbg_v2";
	public static final List states = new ArrayList(Arrays.asList(new String[]{REL_V1_ID, REL_V2_ID, DBG_V1_ID, DBG_V2_ID}));

	private IProject project;
	private String currentConfig;

	public MockState(ICProject cproject) {
		this.currentConfig = REL_V1_ID;
		this.project= cproject.getProject();
	}

	public ICConfigurationDescription getCurrentConfig() {
		return new MockConfig(currentConfig, project);
	}

	public void setConfig(String newConfig) throws CoreException {
		currentConfig = newConfig;
	}
}

