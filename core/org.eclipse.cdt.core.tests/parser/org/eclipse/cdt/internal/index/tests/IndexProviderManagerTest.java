/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Systems and others.
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
import org.eclipse.cdt.core.internal.index.provider.test.DummyProviderTraces;
import org.eclipse.cdt.core.internal.index.provider.test.Providers;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.settings.model.CConfigurationStatus;
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
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.cdt.internal.core.pdom.PDOMManager;
import org.eclipse.cdt.internal.core.pdom.indexer.DeltaAnalyzer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.osgi.service.resolver.VersionRange;

/**
 * Example usage and test for IIndexProvider
 */
public class IndexProviderManagerTest extends IndexTestBase {
	final static DummyProviderTraces DPT= DummyProviderTraces.getInstance();
	final static Class DP1= Providers.Dummy1.class;
	final static Class DP2= Providers.Dummy2.class;
	final static Class DP3= Providers.Dummy3.class;
	final static Class DP4= Providers.Dummy4.class;
	final static Class DP5= Providers.Dummy5.class;
	final static Class[] DPS= new Class[] {DP4, DP2, DP1, DP3, DP5};
	
	/*
	 * Fictional compatibility ranges for testing
	 */
	final static VersionRange VERSION_400= new VersionRange("36");
	final static VersionRange VERSION_401= new VersionRange("[36,37]");
	final static VersionRange VERSION_405= new VersionRange("[37,39]");
	final static VersionRange VERSION_502= new VersionRange("[89,91]");
	
	final CCorePlugin core= CCorePlugin.getDefault();
	
	public IndexProviderManagerTest() {
		super("IndexProviderManagerTest");
	}

	public static TestSuite suite() {
		return suite(IndexProviderManagerTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		DPT.enabled = true;
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		DPT.enabled = false;
		IndexProviderManager ipm= ((PDOMManager)CCorePlugin.getIndexManager()).getIndexProviderManager();
		ipm.reset(); ipm.startup();
	}
	
	public void testProvider_SimpleLifeCycle_200958() throws Exception {
		for (Class element : DPS)
			DPT.reset(element);
		
		List cprojects = new ArrayList(), expectedTrace = new ArrayList();
		try {
			for(int i=0; i<3; i++) {
				ICProject cproject = CProjectHelper.createCProject("P"+System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
				IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
				cprojects.add(cproject);
				expectedTrace.add(cproject);
			}
			for (Class element : DPS)
				assertEquals(expectedTrace, DPT.getProjectsTrace(element));
			for(int i=0; i<expectedTrace.size(); i++) {
				ICProject cproject = (ICProject) expectedTrace.get(i);
				IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
			}
			for (Class element : DPS)
				assertEquals(expectedTrace, DPT.getProjectsTrace(element));
		} finally {
			for(int i=0; i<cprojects.size(); i++) {
				ICProject cproject = (ICProject) expectedTrace.get(i);
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}

	public void testProvider_OverDeleteAndAdd() throws Exception {
		DPT.reset(DP1);
		
		List expectedTrace = new ArrayList();
		ICProject cproject = null;
		try {
			String name = "P"+System.currentTimeMillis();
			cproject = CProjectHelper.createCProject(name, "bin", IPDOMManager.ID_NO_INDEXER);
			IIndex index = CCorePlugin.getIndexManager().getIndex(cproject);
			expectedTrace.add(cproject);
			assertEquals(expectedTrace, DPT.getProjectsTrace(DP1));

			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			cproject = CProjectHelper.createCProject(name, "bin", IPDOMManager.ID_NO_INDEXER);
			index = CCorePlugin.getIndexManager().getIndex(cproject);
			expectedTrace.add(cproject);
			assertEquals(expectedTrace, DPT.getProjectsTrace(DP1));
		} finally {
			if(cproject!=null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}

	public void testProvider_OverMove() throws Exception {
		DPT.reset(DP1);
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
			assertEquals(expectedTrace, DPT.getProjectsTrace(DP1));

			// move the project to a random new location
			File newLocation = CProjectHelper.freshDir();
			IProjectDescription description = cproject.getProject().getDescription();
			description.setLocationURI(newLocation.toURI());
			cproject.getProject().move(description, IResource.FORCE | IResource.SHALLOW, new NullProgressMonitor());	

			index = CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(expectedTrace, DPT.getProjectsTrace(DP1));
		} finally {
			if(cproject!=null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}
	
	public void testVersioning_IncompatibleIgnored() throws Exception {
		IndexProviderManager ipm= ((PDOMManager)CCorePlugin.getIndexManager()).getIndexProviderManager();
		
		ICProject cproject = null;
		try {
			cproject= CProjectHelper.createCCProject("IndexFactoryConfigurationUsageTest", IPDOMManager.ID_NO_INDEXER);
			IProject project= cproject.getProject();
			
			
			MockState mockState = new MockState(cproject);
			mockState.setConfig(MockState.REL_V1_ID);
			
			IIndexProvider provider1= new IIndexFragmentProvider() {
				IIndexFragment[] fragments= new IIndexFragment[] {
					new MockPDOM("contentID.contentA", "36"),
					new MockPDOM("contentID.contentA", "37"),
					new MockPDOM("contentID.foo", "90"),
					new MockPDOM("contentID.bar", "91"),
					new MockPDOM("contentID.baz", "89")
				};
				@Override
				public IIndexFragment[] getIndexFragments(ICConfigurationDescription config) {
					return fragments;
				}
				@Override
				public boolean providesFor(ICProject project) throws CoreException {
					return true;
				}
			};
			IIndexProvider provider2= new IIndexFragmentProvider() {
				IIndexFragment[] fragments= new IIndexFragment[] {
						new MockPDOM("contentID.baz", "90"),
						new MockPDOM("contentID.contentA", "38"),
				};
				@Override
				public IIndexFragment[] getIndexFragments(ICConfigurationDescription config) {
					return fragments;
				}
				@Override
				public boolean providesFor(ICProject project) throws CoreException {
					return true;
				}
			};
			
			CCorePlugin.getIndexManager().joinIndexer(8000, npm()); // ensure IPM is called only once under test conditions
			setExpectedNumberOfLoggedNonOKStatusObjects(3); // foo, bar and baz have no compatible fragments available
			
			ipm.reset(VERSION_405); ipm.startup();
			ipm.addIndexProvider(provider1);  ipm.addIndexProvider(provider2);
			
			IIndexFragment[] actual = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(1, actual.length);
			assertFragmentPresent("contentID.contentA", "38", actual);
		} finally {
			if(cproject!=null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}
	
	public void testVersioning_NoCompatibleVersionsFound() throws Exception {
		IndexProviderManager ipm= ((PDOMManager)CCorePlugin.getIndexManager()).getIndexProviderManager();
		
		ICProject cproject = null;
		try {
			cproject= CProjectHelper.createCCProject("IndexFactoryConfigurationUsageTest", IPDOMManager.ID_NO_INDEXER);
			IProject project= cproject.getProject();
			
			
			MockState mockState = new MockState(cproject);
			mockState.setConfig(MockState.REL_V1_ID);
			
			IIndexProvider provider1= new IIndexFragmentProvider() {
				IIndexFragment[] fragments= new IIndexFragment[] {
					new MockPDOM("contentID.contentA", "36"),
					new MockPDOM("contentID.contentA", "37"),
					new MockPDOM("contentID.foo", "90"),
					new MockPDOM("contentID.bar", "91"),
					new MockPDOM("contentID.baz", "89")
				};
				@Override
				public IIndexFragment[] getIndexFragments(ICConfigurationDescription config) {
					return fragments;
				}
				@Override
				public boolean providesFor(ICProject project) throws CoreException {
					return true;
				}
			};
			IIndexProvider provider2= new IIndexFragmentProvider() {
				IIndexFragment[] fragments= new IIndexFragment[] {
					new MockPDOM("contentID.contentA", "41"),
				};
				@Override
				public IIndexFragment[] getIndexFragments(ICConfigurationDescription config) {
					return fragments;
				}
				@Override
				public boolean providesFor(ICProject project) throws CoreException {
					return true;
				}
			};
			
			CCorePlugin.getIndexManager().joinIndexer(8000, npm()); // ensure IPM is called only once under test conditions
			setExpectedNumberOfLoggedNonOKStatusObjects(1); // contentA has no compatible fragments available
			
			ipm.reset(VERSION_502); ipm.startup();
			ipm.addIndexProvider(provider1);  ipm.addIndexProvider(provider2);
			
			IIndexFragment[] actual = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(3, actual.length);
			assertFragmentPresent("contentID.foo", "90", actual);
			assertFragmentPresent("contentID.bar", "91", actual);
			assertFragmentPresent("contentID.baz", "89", actual);
		} finally {
			if(cproject!=null) {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}
	
	private void assertFragmentPresent(String id, String version, IIndexFragment[] fragments) throws Exception {
		for (IIndexFragment candidate : fragments) {
			String cid= null, csver= null;
			try {
				candidate.acquireReadLock();
				cid= candidate.getProperty(IIndexFragment.PROPERTY_FRAGMENT_ID);
				csver= candidate.getProperty(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION);
			} finally {
				candidate.releaseReadLock();
			}
			if(id.equals(cid) && version.equals(csver))
				return;
		}
		fail("Fragment matching (id="+id+",version="+version+") was not present");
	}
	
	public void testIndexFactoryConfigurationUsage() throws Exception {
		IIndex index;
		
		ICProject cproject = null;
		// Modifying the .project file triggers an indexer job, suppress that:
		DeltaAnalyzer.sSuppressPotentialTUs= true;
		try {
			cproject = CProjectHelper.createCCProject("IndexFactoryConfigurationUsageTest", IPDOMManager.ID_NO_INDEXER);
			IProject project= cproject.getProject();
			
			ICProjectDescription pd= core.getProjectDescription(project);
			ICConfigurationDescription cfg1= newCfg(pd, "project", "config1");
			ICConfigurationDescription cfg2= newCfg(pd, "project", "config2");
			core.setProjectDescription(project, pd);
			
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			CCorePlugin.getIndexManager().joinIndexer(8000, npm());
		
			DPT.reset(DP1);
			changeConfigRelations(project, ICProjectDescriptionPreferences.CONFIGS_LINK_SETTINGS_AND_ACTIVE);
			assertEquals(0, DPT.getProjectsTrace(DP1).size());
			assertEquals(0, DPT.getCfgsTrace(DP1).size());
			
			changeActiveConfiguration(project, cfg1);
			DPT.reset(DP1);
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(0, DPT.getProjectsTrace(DP1).size());
			assertEquals(1, DPT.getCfgsTrace(DP1).size());
			assertEquals("project.config1", ((ICConfigurationDescription)DPT.getCfgsTrace(DP1).get(0)).getId());
			
			changeActiveConfiguration(project, cfg2);
			DPT.reset(DP1);
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(0, DPT.getProjectsTrace(DP1).size());
			assertEquals(1, DPT.getCfgsTrace(DP1).size());
			assertEquals("project.config2", ((ICConfigurationDescription)DPT.getCfgsTrace(DP1).get(0)).getId());
			
			DPT.reset(DP1);
			changeConfigRelations(project, ICProjectDescriptionPreferences.CONFIGS_INDEPENDENT);
			assertEquals(0, DPT.getProjectsTrace(DP1).size());
			assertEquals(0, DPT.getCfgsTrace(DP1).size());
			
			changeActiveConfiguration(project, cfg1);
			DPT.reset(DP1);
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(0, DPT.getProjectsTrace(DP1).size());
			assertEquals(1, DPT.getCfgsTrace(DP1).size());
			// should still be config2, as the change in active configuration does not matter
			assertEquals("project.config2", ((ICConfigurationDescription)DPT.getCfgsTrace(DP1).get(0)).getId());
			
			changeActiveConfiguration(project, cfg2);
			DPT.reset(DP1);
			index= CCorePlugin.getIndexManager().getIndex(cproject);
			assertEquals(0, DPT.getProjectsTrace(DP1).size());
			assertEquals(1, DPT.getCfgsTrace(DP1).size());
			// there should be no change from the previous state (also config2)
			assertEquals("project.config2", ((ICConfigurationDescription)DPT.getCfgsTrace(DP1).get(0)).getId());
		} finally {
			DeltaAnalyzer.sSuppressPotentialTUs= false;
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
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[0]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			mockState.setConfig(MockState.DBG_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[3]));

			mockState.setConfig(MockState.DBG_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(0, fragments.length);

			mockState.setConfig(MockState.REL_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[1]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[1]));

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[0]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			provider1.invert();

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			mockState.setConfig(MockState.DBG_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[0]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[3]));

			mockState.setConfig(MockState.DBG_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(1, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[1]));

			mockState.setConfig(MockState.REL_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(1, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[1]));

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			provider2.invert();

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[3]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[3]));

			mockState.setConfig(MockState.DBG_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[0]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[0]));

			mockState.setConfig(MockState.DBG_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(2, fragments.length);
			assertTrue(ArrayUtil.contains(fragments, provider1.fragments[1]));
			assertTrue(ArrayUtil.contains(fragments, provider2.fragments[1]));

			mockState.setConfig(MockState.REL_V2_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
			assertEquals(0, fragments.length);

			mockState.setConfig(MockState.REL_V1_ID);
			fragments = ipm.getProvidedIndexFragments(mockState.getCurrentConfig(), true);
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
		CCorePlugin.getIndexManager().joinIndexer(8000, npm());
	}
	
	private void changeConfigRelations(IProject project, int option) throws CoreException {
		ICProjectDescription pd= core.getProjectDescription(project);
		pd.setConfigurationRelations(option);
		core.setProjectDescription(project, pd);
		CCorePlugin.getIndexManager().joinIndexer(8000, npm());
	}
}

class MockStateIndexProvider implements IIndexProvider {
	protected ICProject targetProject;

	public MockStateIndexProvider(ICProject cproject) {
		this.targetProject = cproject;
	}

	@Override
	public boolean providesFor(ICProject cproject) throws CoreException {
		return this.targetProject.equals(cproject);
	}
}

class MockStateIndexFragmentProvider extends MockStateIndexProvider implements IIndexFragmentProvider {
	private boolean invert;
	final IIndexFragment[] fragments;
	
	public void invert() {
		invert = !invert;
	}

	public MockStateIndexFragmentProvider(ICProject cproject) {
		super(cproject);

		fragments = new IIndexFragment[MockState.states.size()];
		for(int i=0; i<MockState.states.size(); i++) {
			fragments[i] = new MockPDOM("mock.test.index."+System.identityHashCode(this)+"."+i, PDOM.versionString(PDOM.getDefaultVersion()));
		}
	}

	@Override
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
}

class MockConfig implements ICConfigurationDescription {
	String id;
	IProject project;

	MockConfig(String id, IProject project) {
		this.id= id;
		this.project= project;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public ICConfigExtensionReference create(String extensionPoint,
			String extension) throws CoreException {
		return null;
	}

	@Override
	public ICExternalSetting createExternalSetting(String[] languageIDs,
			String[] contentTypeIds, String[] extensions,
			ICSettingEntry[] entries) throws WriteAccessException {
		return null;
	}

	@Override
	public ICFileDescription createFileDescription(IPath path,
			ICResourceDescription base) throws CoreException,
			WriteAccessException {
		return null;
	}

	@Override
	public ICFolderDescription createFolderDescription(IPath path,
			ICFolderDescription base) throws CoreException,
			WriteAccessException {
		return null;
	}

	@Override
	public ICConfigExtensionReference[] get(String extensionPointID) {
		return null;
	}

	@Override
	public ICBuildSetting getBuildSetting() {
		return null;
	}

	@Override
	public String getBuildSystemId() {
		return null;
	}

	@Override
	public ICdtVariablesContributor getBuildVariablesContributor() {
		return null;
	}

	@Override
	public CConfigurationData getConfigurationData() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public ICExternalSetting[] getExternalSettings() {
		return null;
	}

	@Override
	public ICFileDescription[] getFileDescriptions() {
		return null;
	}

	@Override
	public ICFolderDescription[] getFolderDescriptions() {
		return null;
	}

	@Override
	public ICProjectDescription getProjectDescription() {
		return CoreModel.getDefault().getProjectDescription(project);
	}

	@Override
	public Map getReferenceInfo() {
		return null;
	}

	@Override
	public ICResourceDescription getResourceDescription(IPath path,
			boolean exactPath) {
		return null;
	}

	@Override
	public ICResourceDescription[] getResourceDescriptions() {
		return null;
	}

	@Override
	public ICFolderDescription getRootFolderDescription() {return null;}
	@Override
	public Object getSessionProperty(QualifiedName name) {return null;}
	@Override
	public ICSourceEntry[] getSourceEntries() {return null;}
	@Override
	public ICTargetPlatformSetting getTargetPlatformSetting() {return null;}
	@Override
	public boolean isActive() {return false;}
	@Override
	public boolean isModified() {return false;}
	@Override
	public boolean isPreferenceConfiguration() {return false;}
	@Override
	public void remove(ICConfigExtensionReference ext) throws CoreException {}
	@Override
	public void remove(String extensionPoint) throws CoreException {}
	@Override
	public void removeExternalSetting(ICExternalSetting setting) throws WriteAccessException {}
	@Override
	public void removeExternalSettings() throws WriteAccessException {}
	@Override
	public void removeResourceDescription(ICResourceDescription des)
	throws CoreException, WriteAccessException {}
	@Override
	public void setActive() throws WriteAccessException {}
	@Override
	public void setConfigurationData(String buildSystemId,
			CConfigurationData data) throws WriteAccessException {}
	@Override
	public void setDescription(String des) throws WriteAccessException {}
	@Override
	public void setName(String name) throws WriteAccessException {}
	@Override
	public void setReferenceInfo(Map<String, String> refs) throws WriteAccessException {}
	@Override
	public void setSessionProperty(QualifiedName name, Object value) {}
	@Override
	public void setSourceEntries(ICSourceEntry[] entries) throws CoreException,
	WriteAccessException {}
	@Override
	public ICSettingObject[] getChildSettings() {return null;}
	@Override
	public ICConfigurationDescription getConfiguration() {return null;}
	@Override
	public String getName() {return null;}
	@Override
	public ICSettingContainer getParent() {return null;}
	@Override
	public int getType() {return 0;}
	@Override
	public boolean isReadOnly() {return false;}
	@Override
	public boolean isValid() {return false;}
	@Override
	public ICStorageElement getStorage(String id, boolean create) throws CoreException {
		return null;
	}
	@Override
	public ICStorageElement importStorage(String id, ICStorageElement storage) {
		return null;
	}

	@Override
	public void removeStorage(String id) throws CoreException {
	}

	@Override
	public ICLanguageSetting getLanguageSettingForFile(IPath path, boolean ignoreExludeStatus) {
		return null;
	}

	@Override
	public String[] getExternalSettingsProviderIds() {
		return null;
	}

	@Override
	public void setExternalSettingsProviderIds(String[] ids) {}

	@Override
	public void updateExternalSettingsProviders(String[] ids) {}

	@Override
	public ICSourceEntry[] getResolvedSourceEntries() {
		return null;
	}

	@Override
	public CConfigurationStatus getConfigurationStatus() {
		return CConfigurationStatus.CFG_STATUS_OK;
	}

	@Override
	public void setReadOnly(boolean readOnly, boolean keepModify) {}
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

class MockPDOM extends EmptyIndexFragment {
	String id;
	String version;
	
	MockPDOM(String id, String version) {
		this.id= id;
		this.version= version;
	}
	
	@Override
	public String getProperty(String propertyName) throws CoreException {
		if(IIndexFragment.PROPERTY_FRAGMENT_ID.equals(propertyName)) {
			return id;
		}
		if(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_ID.equals(propertyName)) {
			return PDOM.FRAGMENT_PROPERTY_VALUE_FORMAT_ID;
		}
		if(IIndexFragment.PROPERTY_FRAGMENT_FORMAT_VERSION.equals(propertyName)) {
			return version;
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "[Mock index fragment "+id+"."+System.identityHashCode(this)+"]";
	}
}