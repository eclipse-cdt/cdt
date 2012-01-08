/*******************************************************************************
 * Copyright (c) 2009, 2010 Andrew Gvozdev (Quoin Inc.) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Gvozdev (Quoin Inc.) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestSuite;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.core.testplugin.ResourceHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class CProjectDescriptionDeltaTests  extends BaseTestCase{

	private MockListener listener;
	
	private class MockListener implements ICProjectDescriptionListener {
		private boolean fIsNotified;
		private String fProjName;
		private ICDescriptionDelta fDelta;
		
		public MockListener(String projName){
			fProjName = projName;
			fIsNotified = false;
			fDelta = null;
		}
		
		@Override
		public void handleEvent(CProjectDescriptionEvent event) {
			if(!event.getProject().getName().equals(fProjName))
				return;
			fIsNotified = true;
			fDelta = event.getProjectDelta();
		}
		
		boolean isNotified(){
			return fIsNotified;
		}

		void clearNotified(){
			fIsNotified = false;
			fDelta = null;
		}

		public ICDescriptionDelta getDelta() {
			return fDelta;
		}
	}
	
	public static TestSuite suite() {
		return suite(CProjectDescriptionDeltaTests.class, "_");
	}

	@Override
	protected void setUp() throws Exception {
	}
	
	@Override
	protected void tearDown() throws Exception {
		ResourceHelper.cleanUp();
	}

	
	private void initListener(String projName){
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		if(listener != null)
			mngr.removeCProjectDescriptionListener(listener);
		listener = new MockListener(projName);
		mngr.addCProjectDescriptionListener(listener, CProjectDescriptionEvent.APPLIED);
		
		assertFalse(listener.isNotified());

	}
	
	private static List<ICDescriptionDelta> findDeltas(ICDescriptionDelta delta, int type) {
		List<ICDescriptionDelta> list = new ArrayList<ICDescriptionDelta>();
		if ((delta.getChangeFlags()&type)!=0) {
			list.add(delta);
		}
		
		ICDescriptionDelta[] children = delta.getChildren();
		for (ICDescriptionDelta d : children) {
			list.addAll(findDeltas(d, type));
		}
		return list;
	}

	public void testDelta_ACTIVE_CFG() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProject(projName, null,
				new String[] {"test.configuration.1", "test.configuration.2"});
		
		// Get writable project description and its configurations
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		assertEquals(2, prjDescription.getConfigurations().length);
		
		ICConfigurationDescription cfgDescription0 = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription0);
		assertSame(cfgDescription0, prjDescription.getActiveConfiguration());
		
		ICConfigurationDescription cfgDescription1 = prjDescription.getConfigurations()[1];
		assertNotNull(cfgDescription1);
		assertNotSame(cfgDescription1, prjDescription.getActiveConfiguration());
		
		// ACTIVE_CFG: Change active configuration
		prjDescription.setActiveConfiguration(cfgDescription1);
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.ACTIVE_CFG);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICProjectDescription);
		ICProjectDescription oldSetting = (ICProjectDescription)delta.getOldSetting();
		assertEquals(cfgDescription0.getName(), oldSetting.getActiveConfiguration().getName());
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICProjectDescription);
		ICProjectDescription newSetting = (ICProjectDescription)delta.getNewSetting();
		assertEquals(cfgDescription1.getName(), newSetting.getActiveConfiguration().getName());
	}
	
	public void testDelta_NAME() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		String oldName = cfgDescription.getName();
		
		// Modification ICDescriptionDelta.NAME
		String newName = "New name";
		cfgDescription.setName(newName);
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.NAME);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.NAME, delta.getChangeFlags());
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
		assertEquals(oldName, oldSetting.getName());
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
		assertEquals(newName, newSetting.getName());
	}
	
	public void testDelta_DESCRIPTION() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		String oldDescription = cfgDescription.getDescription();
		
		// Modification ICDescriptionDelta.DESCRIPTION
		cfgDescription.setDescription("New description");
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.DESCRIPTION);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.DESCRIPTION, delta.getChangeFlags());
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
		assertEquals(oldDescription, oldSetting.getDescription());
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
		assertEquals(cfgDescription.getDescription(), newSetting.getDescription());
		
	}

	public void testDelta_LANGUAGE_ID() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		IFile file = ResourceHelper.createFile(project, "test.cpp");
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification LANGUAGE_ID
		ICLanguageSetting langSetting = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), false);
		String oldLanguageId = langSetting.getLanguageId();
		String newLanguageId = "test.language.id";
		assertTrue(!newLanguageId.equals(oldLanguageId));
		langSetting.setLanguageId(newLanguageId);
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.LANGUAGE_ID);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.LANGUAGE_ID, delta.getChangeFlags());

		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICLanguageSetting);
		ICLanguageSetting oldSetting = (ICLanguageSetting)delta.getOldSetting();
		assertEquals(oldLanguageId, oldSetting.getLanguageId());
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICLanguageSetting);
		ICLanguageSetting newSetting = (ICLanguageSetting)delta.getNewSetting();
		assertEquals(newLanguageId, newSetting.getLanguageId());
		
	}
	
	public void testDelta_SOURCE_CONTENT_TYPE() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		IFile file = ResourceHelper.createFile(project, "test.cpp");
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification SOURCE_CONTENT_TYPE
		final String testContentType = "test.content.type";
		ICLanguageSetting langSetting = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), false);
		langSetting.setSourceContentTypeIds(new String[] {testContentType});
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.SOURCE_CONTENT_TYPE);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICLanguageSetting);
		ICLanguageSetting oldSetting = (ICLanguageSetting)delta.getOldSetting();
		List<String> oldContentTypes = Arrays.asList(oldSetting.getSourceContentTypeIds());
		assertTrue(!oldContentTypes.contains(testContentType));
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICLanguageSetting);
		ICLanguageSetting newSetting = (ICLanguageSetting)delta.getNewSetting();
		List<String> newContentTypes = Arrays.asList(newSetting.getSourceContentTypeIds());
		assertTrue(newContentTypes.contains(testContentType));
		
	}
	
	public void testDelta_SOURCE_EXTENSIONS() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		IFile file = ResourceHelper.createFile(project, "test.cpp");
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification SOURCE_EXTENSIONS
		ICLanguageSetting langSetting = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), false);
		final String testContentType = CCorePlugin.CONTENT_TYPE_ASMSOURCE;
		langSetting.setSourceContentTypeIds(new String[] {testContentType});
		String[] exts = CDataUtil.getExtensionsFromContentTypes(project, new String[] {testContentType});
		assertTrue(exts.length>0);
		final String testSourceExtension = exts[0];

		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.SOURCE_EXTENSIONS);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
//		assertEquals(ICDescriptionDelta.SOURCE_EXTENSIONS, delta.getChangeFlags());
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICLanguageSetting);
		ICLanguageSetting oldSetting = (ICLanguageSetting)delta.getOldSetting();
		List<String> oldSourceExtensions = Arrays.asList(oldSetting.getSourceExtensions());
		assertTrue(!oldSourceExtensions.contains(testSourceExtension));
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICLanguageSetting);
		ICLanguageSetting newSetting = (ICLanguageSetting)delta.getNewSetting();
		List<String> newSourceExtensions = Arrays.asList(newSetting.getSourceExtensions());
		assertTrue(newSourceExtensions.contains(testSourceExtension));
		
	}
	
	public void testDelta_SETTING_ENTRIES() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		IFile file = ResourceHelper.createFile(project, "test.cpp");
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification SETTING_ENTRIES
		ICLanguageSettingEntry testSettingEntry = new CIncludePathEntry("/path", 0);
		ICLanguageSetting langSetting = cfgDescription.getLanguageSettingForFile(file.getProjectRelativePath(), false);
		ICLanguageSettingEntry[] entries = new ICLanguageSettingEntry[] {testSettingEntry};
		langSetting.setSettingEntries(ICSettingEntry.INCLUDE_PATH, entries);
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.SETTING_ENTRIES);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.SETTING_ENTRIES, delta.getChangeFlags());
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICLanguageSetting);
		ICLanguageSetting oldSetting = (ICLanguageSetting)delta.getOldSetting();
		List<ICLanguageSettingEntry> oldSettingEntries = oldSetting.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
		assertTrue(!oldSettingEntries.contains(testSettingEntry));
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICLanguageSetting);
		ICLanguageSetting newSetting = (ICLanguageSetting)delta.getNewSetting();
		List<ICLanguageSettingEntry> newSettingEntries = newSetting.getSettingEntriesList(ICSettingEntry.INCLUDE_PATH);
		assertTrue(newSettingEntries.contains(testSettingEntry));
	}
	
	public void testDelta_BINARY_PARSER_IDS() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification BINARY_PARSER_IDS 
		final String testBinaryParserId = "test.binary.parser.id";
		ICTargetPlatformSetting targetPlatformSetting = cfgDescription.getTargetPlatformSetting();
		targetPlatformSetting.setBinaryParserIds(new String[] {testBinaryParserId});
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.BINARY_PARSER_IDS);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.BINARY_PARSER_IDS, delta.getChangeFlags());
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICTargetPlatformSetting);
		ICTargetPlatformSetting oldSetting = (ICTargetPlatformSetting)delta.getOldSetting();
		List<String> oldBinaryParserIds = Arrays.asList(oldSetting.getBinaryParserIds());
		assertTrue(!oldBinaryParserIds.contains(testBinaryParserId));
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICTargetPlatformSetting);
		ICTargetPlatformSetting newSetting = (ICTargetPlatformSetting)delta.getNewSetting();
		List<String> newBinaryParserIds = Arrays.asList(newSetting.getBinaryParserIds());
		assertTrue(newBinaryParserIds.contains(testBinaryParserId));
		
	}
	
	public void testDelta_ERROR_PARSER_IDS() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification ERROR_PARSER_IDS
		String testErrorParserId = "test.error.parser.id";
		cfgDescription.getBuildSetting().setErrorParserIDs(new String[] {testErrorParserId});
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.ERROR_PARSER_IDS);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.ERROR_PARSER_IDS, delta.getChangeFlags());

		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICBuildSetting);
		ICBuildSetting oldSetting = (ICBuildSetting)delta.getOldSetting();
		List<String> oldErrorParserIds = Arrays.asList(oldSetting.getErrorParserIDs());
		assertTrue(!oldErrorParserIds.contains(testErrorParserId));
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICBuildSetting);
		ICBuildSetting newSetting = (ICBuildSetting)delta.getNewSetting();
		List<String> newErrorParserIds = Arrays.asList(newSetting.getErrorParserIDs());
		assertTrue(newErrorParserIds.contains(testErrorParserId));
		
	}
	
	public void testDelta_EXCLUDE() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		IFile file = ResourceHelper.createFile(project, "test.cpp");
		
		{
			// Prepare file descriptions
			ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			assertNotNull(cfgDescription);
			
			cfgDescription.createFileDescription(file.getProjectRelativePath(),cfgDescription.getRootFolderDescription());
			ICFileDescription[] fileDescriptions = cfgDescription.getFileDescriptions();
			assertTrue(fileDescriptions.length>0);
			
			mngr.setProjectDescription(project, prjDescription);
		}
		
		{
			// Get writable project description and its configuration
			ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			assertNotNull(cfgDescription);
			
			// Modification EXCLUDE
			ICFileDescription[] fileDescriptions = cfgDescription.getFileDescriptions();
			assertTrue(fileDescriptions.length>0);
			ICFileDescription fileDescription = fileDescriptions[0];
			fileDescription.setExcluded(true);
			
			// Write project description
			listener.clearNotified();
			mngr.setProjectDescription(project, prjDescription);
			assertEquals(true, listener.isNotified());
			
			// Analyze delta
			ICDescriptionDelta rootDelta = listener.getDelta();
			assertNotNull(rootDelta);
			List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.EXCLUDE);
			assertEquals(1, deltas.size());
			ICDescriptionDelta delta = deltas.get(0);
			assertNotNull(delta);
			assertEquals(ICDescriptionDelta.EXCLUDE, delta.getChangeFlags());

			// Check old setting
			assertTrue(delta.getOldSetting() instanceof ICFileDescription);
			ICFileDescription oldSetting = (ICFileDescription)delta.getOldSetting();
			assertTrue(!oldSetting.isExcluded());
			
			// Check new setting
			assertTrue(delta.getNewSetting() instanceof ICFileDescription);
			ICFileDescription newSetting = (ICFileDescription)delta.getNewSetting();
			assertTrue(newSetting.isExcluded());
		}
		
	}
	
	public void testDelta_SOURCE_ADDED() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification SOURCE_ADDED
		List<ICSourceEntry> sourceEntries = new ArrayList<ICSourceEntry>(Arrays.asList(cfgDescription.getSourceEntries()));
		ICSourceEntry testSourceEntry = new CSourceEntry(project.getFullPath().append("test_src"), null, ICSettingEntry.RESOLVED);
		sourceEntries.add(testSourceEntry);
		cfgDescription.setSourceEntries(sourceEntries.toArray(new ICSourceEntry[0]));
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.SOURCE_ADDED);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
//		assertEquals(ICDescriptionDelta.SOURCE_ADDED, delta.getChangeFlags());
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
		List<ICSourceEntry> oldSourceEntries = Arrays.asList(oldSetting.getSourceEntries());
		assertTrue(!oldSourceEntries.contains(testSourceEntry));
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
		List<ICSourceEntry> newSourceEntries = Arrays.asList(newSetting.getSourceEntries());
		assertTrue(newSourceEntries.contains(testSourceEntry));
	}
	
	public void testDelta_SOURCE_REMOVED() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		ICSourceEntry testSourceEntry = new CSourceEntry(project.getFullPath().append("test_src"), null, ICSettingEntry.RESOLVED);
		{
			// Add some source entry to remove it during the test
			ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			assertNotNull(cfgDescription);
			
			List<ICSourceEntry> sourceEntries = new ArrayList<ICSourceEntry>(Arrays.asList(cfgDescription.getSourceEntries()));
			sourceEntries.add(testSourceEntry);
			cfgDescription.setSourceEntries(sourceEntries.toArray(new ICSourceEntry[0]));
			
			mngr.setProjectDescription(project, prjDescription);
		}
		
		{
			// Get writable project description and its configuration
			ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			assertNotNull(cfgDescription);
			
			// Modification SOURCE_REMOVED
			cfgDescription.setSourceEntries(new ICSourceEntry[0]);
			
			// Write project description
			listener.clearNotified();
			mngr.setProjectDescription(project, prjDescription);
			assertEquals(true, listener.isNotified());
			
			// Analyze delta
			ICDescriptionDelta rootDelta = listener.getDelta();
			assertNotNull(rootDelta);
			List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.SOURCE_REMOVED);
			assertEquals(1, deltas.size());
			ICDescriptionDelta delta = deltas.get(0);
			assertNotNull(delta);
//			assertEquals(ICDescriptionDelta.SOURCE_REMOVED, delta.getChangeFlags());
			
			// Check old setting
			assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
			ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
			List<ICSourceEntry> oldSourceEntries = Arrays.asList(oldSetting.getSourceEntries());
			assertTrue(oldSourceEntries.contains(testSourceEntry));
			
			// Check new setting
			assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
			ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
			List<ICSourceEntry> newSourceEntries = Arrays.asList(newSetting.getSourceEntries());
			assertTrue(!newSourceEntries.contains(testSourceEntry));
		}
		
	}
	
	public void testDelta_EXTERNAL_SETTINGS_ADDED() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification EXTERNAL_SETTINGS_ADDED
		ICSettingEntry testSettingEntry = new CIncludePathEntry("/path", 0);
		ICExternalSetting testExternalSetting = cfgDescription.createExternalSetting(null, null, null, new ICSettingEntry[] {testSettingEntry});
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.EXTERNAL_SETTINGS_ADDED);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.EXTERNAL_SETTINGS_ADDED, delta.getChangeFlags());
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
		List<ICExternalSetting> oldExternalSettings = Arrays.asList(oldSetting.getExternalSettings());
		assertEquals(0, oldExternalSettings.size());
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
		List<ICExternalSetting> newExternalSettings = Arrays.asList(newSetting.getExternalSettings());
		assertEquals(1, newExternalSettings.size());
		List<ICSettingEntry> newSettingEntries = Arrays.asList(newExternalSettings.get(0).getEntries());
		assertTrue(newSettingEntries.contains(testSettingEntry));
	}
	
	public void testDelta_EXTERNAL_SETTINGS_REMOVED() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		ICSettingEntry testSettingEntry = new CIncludePathEntry("/path", 0);
		
		{
			// Add some external setting to remove it during the test
			ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			assertNotNull(cfgDescription);
			
			ICExternalSetting testExternalSetting = cfgDescription.createExternalSetting(null, null, null, new ICSettingEntry[] {testSettingEntry});
			mngr.setProjectDescription(project, prjDescription);
		}
		
		{
			// Get writable project description and its configuration
			ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			assertNotNull(cfgDescription);
			
			// Modification EXTERNAL_SETTINGS_REMOVED
			cfgDescription.removeExternalSettings();
			
			// Write project description
			listener.clearNotified();
			mngr.setProjectDescription(project, prjDescription);
			assertEquals(true, listener.isNotified());
			
			// Analyze delta
			ICDescriptionDelta rootDelta = listener.getDelta();
			assertNotNull(rootDelta);
			List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.EXTERNAL_SETTINGS_REMOVED);
			assertEquals(1, deltas.size());
			ICDescriptionDelta delta = deltas.get(0);
			assertNotNull(delta);
			assertEquals(ICDescriptionDelta.EXTERNAL_SETTINGS_REMOVED, delta.getChangeFlags());

			// Check old setting
			assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
			ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
			List<ICExternalSetting> oldExternalSettings = Arrays.asList(oldSetting.getExternalSettings());
			assertEquals(1, oldExternalSettings.size());
			List<ICSettingEntry> oldSettingEntries = Arrays.asList(oldExternalSettings.get(0).getEntries());
			assertTrue(oldSettingEntries.contains(testSettingEntry));
			
			// Check new setting
			assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
			ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
			List<ICExternalSetting> newExternalSettings = Arrays.asList(newSetting.getExternalSettings());
			assertEquals(0, newExternalSettings.size());
		}
		
	}
	
	public void testDelta_CFG_REF_ADDED() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification CFG_REF_ADDED
		String testKey = "key";
		String testValue = "value";
		Map<String, String> refs = new HashMap<String, String>();
		refs.put(testKey, testValue);
		cfgDescription.setReferenceInfo(refs);
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.CFG_REF_ADDED);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.CFG_REF_ADDED, delta.getChangeFlags());
		
		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
		Map<String, String> oldReferenceInfo = oldSetting.getReferenceInfo();
		assertEquals(0, oldReferenceInfo.size());
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
		Map<String, String> newReferenceInfo = newSetting.getReferenceInfo();
		assertEquals(1, newReferenceInfo.size());
		assertEquals(testValue, newReferenceInfo.get(testKey));
	}
	
	public void testDelta_CFG_REF_REMOVED() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		String testKey = "key";
		String testValue = "value";
		
		{
			// Add some reference info to remove it during the test
			ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			assertNotNull(cfgDescription);
			
			Map<String, String> refs = new HashMap<String, String>();
			refs.put(testKey, testValue);
			cfgDescription.setReferenceInfo(refs);
			mngr.setProjectDescription(project, prjDescription);
		}
		
		{
			// Get writable project description and its configuration
			ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
			assertNotNull(prjDescription);
			ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
			assertNotNull(cfgDescription);
			
			// Modification CFG_REF_REMOVED
			cfgDescription.setReferenceInfo(new HashMap<String, String>());
			
			// Write project description
			listener.clearNotified();
			mngr.setProjectDescription(project, prjDescription);
			assertEquals(true, listener.isNotified());
			
			// Analyze delta
			ICDescriptionDelta rootDelta = listener.getDelta();
			assertNotNull(rootDelta);
			List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.CFG_REF_REMOVED);
			assertEquals(1, deltas.size());
			ICDescriptionDelta delta = deltas.get(0);
			assertNotNull(delta);
			assertEquals(ICDescriptionDelta.CFG_REF_REMOVED, delta.getChangeFlags());
			
			// Check old setting
			assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
			ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
			Map<String, String> oldReferenceInfo = oldSetting.getReferenceInfo();
			assertEquals(1, oldReferenceInfo.size());
			assertEquals(testValue, oldReferenceInfo.get(testKey));
			
			// Check new setting
			assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
			ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
			Map<String, String> newReferenceInfo = newSetting.getReferenceInfo();
			assertEquals(0, newReferenceInfo.size());
		}
		
	}
	
	public void testDelta_EXT_REF() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProjectWithConfig(projName);
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		ICConfigurationDescription cfgDescription = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription);
		
		// Modification EXT_REF, currently binary parsers are represented as CConfigExtensionReference
		final String testBinaryParserId = "test.binary.parser.id";
		ICTargetPlatformSetting targetPlatformSetting = cfgDescription.getTargetPlatformSetting();
		targetPlatformSetting.setBinaryParserIds(new String[] {testBinaryParserId});
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.EXT_REF);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.EXT_REF, delta.getChangeFlags());

		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription oldSetting = (ICConfigurationDescription)delta.getOldSetting();
		ICTargetPlatformSetting oldTargetPlatformSetting = oldSetting.getTargetPlatformSetting();
		List<String> oldBinaryParserIds = Arrays.asList(oldTargetPlatformSetting.getBinaryParserIds());
		assertTrue(!oldBinaryParserIds.contains(testBinaryParserId));
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICConfigurationDescription);
		ICConfigurationDescription newSetting = (ICConfigurationDescription)delta.getNewSetting();
		ICTargetPlatformSetting newTargetPlatformSetting = newSetting.getTargetPlatformSetting();
		List<String> newBinaryParserIds = Arrays.asList(newTargetPlatformSetting.getBinaryParserIds());
		assertTrue(newBinaryParserIds.contains(testBinaryParserId));
	}
	
	public void testDelta_INDEX_CFG() throws Exception {
		String projName = getName();
		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		
		initListener(projName);
		IProject project = ResourceHelper.createCDTProject(projName, null,
				new String[] {"test.configuration.1", "test.configuration.2"});
		
		// Get writable project description and its configuration
		ICProjectDescription prjDescription = mngr.getProjectDescription(project, true);
		assertNotNull(prjDescription);
		assertEquals(2, prjDescription.getConfigurations().length);
		
		ICConfigurationDescription cfgDescription0 = prjDescription.getConfigurations()[0];
		assertNotNull(cfgDescription0);
		assertSame(cfgDescription0, prjDescription.getDefaultSettingConfiguration());
		
		ICConfigurationDescription cfgDescription1 = prjDescription.getConfigurations()[1];
		assertNotNull(cfgDescription1);
		assertNotSame(cfgDescription1, prjDescription.getDefaultSettingConfiguration());
		
		// Modification INDEX_CFG
		prjDescription.setDefaultSettingConfiguration(cfgDescription1);
		
		// Write project description
		listener.clearNotified();
		mngr.setProjectDescription(project, prjDescription);
		assertEquals(true, listener.isNotified());
		
		// Analyze delta
		ICDescriptionDelta rootDelta = listener.getDelta();
		assertNotNull(rootDelta);
		List<ICDescriptionDelta> deltas = findDeltas(rootDelta, ICDescriptionDelta.INDEX_CFG);
		assertEquals(1, deltas.size());
		ICDescriptionDelta delta = deltas.get(0);
		assertNotNull(delta);
		assertEquals(ICDescriptionDelta.INDEX_CFG, delta.getChangeFlags());

		// Check old setting
		assertTrue(delta.getOldSetting() instanceof ICProjectDescription);
		ICProjectDescription oldSetting = (ICProjectDescription)delta.getOldSetting();
		assertEquals(cfgDescription0.getName(), oldSetting.getDefaultSettingConfiguration().getName());
		
		// Check new setting
		assertTrue(delta.getNewSetting() instanceof ICProjectDescription);
		ICProjectDescription newSetting = (ICProjectDescription)delta.getNewSetting();
		assertEquals(cfgDescription1.getName(), newSetting.getDefaultSettingConfiguration().getName());
	}

}
