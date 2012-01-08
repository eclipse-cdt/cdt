/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.language;

import java.util.Collections;

import junit.framework.Test;

import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.internal.core.CContentTypes;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.content.IContentType;

/**
 * Tests for language inheritance computations.
 */
public class LanguageInheritanceTests extends BaseTestCase {
	
	private static final String BIN_FOLDER = "bin";
	private static final String FILE_NAME = "test.c";
	private static final IContentType[] EMPTY_CONTENT_TYPES = new IContentType[0];
	
	private ICProject fCProject;
	private IFile fFile;
	private LanguageManager fManager;
	private ILanguage fLanguage1;
	private ILanguage fLanguage2;
	
	private IProject fProject;
	private ICConfigurationDescription fConfiguration;
	
	public static Test suite() {
		return suite(LanguageInheritanceTests.class);
	}
	
	@Override
	protected void setUp() throws Exception {
		String name = getClass().getName() + "_" + getName();
		fCProject = CProjectHelper.createCCProject(name , BIN_FOLDER, IPDOMManager.ID_NO_INDEXER);
		fProject = fCProject.getProject();
		fFile = fProject.getFile(FILE_NAME);
		
		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(fProject, false);
		fConfiguration = projectDescription.getActiveConfiguration();
		
		fManager = LanguageManager.getInstance();
		fLanguage1 = fManager.getLanguage(GPPLanguage.ID);
		fLanguage2 = fManager.getLanguage(GCCLanguage.ID);
		
		// Ensure global language mappings are cleared.
		WorkspaceLanguageConfiguration config = fManager.getWorkspaceLanguageConfiguration();
		config.setWorkspaceMappings(Collections.EMPTY_MAP);
		fManager.storeWorkspaceLanguageConfiguration(EMPTY_CONTENT_TYPES);
	}

	@Override
	protected void tearDown() throws Exception {
		CProjectHelper.delete(fCProject);
	}
	
	public void testDirectFileMapping() throws Exception {
		ILanguage originalLanguage = fManager.getLanguageForFile(fFile, fConfiguration);
		assertDifferentLanguages(originalLanguage, fLanguage1);
		
		ProjectLanguageConfiguration config = fManager.getLanguageConfiguration(fCProject.getProject());
		config.addFileMapping(fConfiguration, fFile, GPPLanguage.ID);
		fManager.storeLanguageMappingConfiguration(fFile);
		
		assertSameLanguage(fLanguage1, fManager.getLanguageForFile(fFile, fConfiguration));
		
		config.removeFileMapping(fConfiguration, fFile);
		fManager.storeLanguageMappingConfiguration(fFile);
		
		assertSameLanguage(originalLanguage, fManager.getLanguageForFile(fFile, fConfiguration));
	}
	
	public void testDirectProjectContentTypeMapping() throws Exception {
		ILanguage originalLanguage = fManager.getLanguageForFile(fFile, fConfiguration);
		assertDifferentLanguages(originalLanguage, fLanguage1);
		
		String filename = fFile.getLocation().toString();
		IContentType contentType = CContentTypes.getContentType(fProject, filename);
		ProjectLanguageConfiguration config = fManager.getLanguageConfiguration(fCProject.getProject());
		config.addContentTypeMapping(fConfiguration, contentType.getId(), GPPLanguage.ID);
		fManager.storeLanguageMappingConfiguration(fProject, EMPTY_CONTENT_TYPES);

		assertSameLanguage(fLanguage1, fManager.getLanguageForFile(fFile, fConfiguration));

		config.removeContentTypeMapping(fConfiguration, contentType.getId());
		fManager.storeLanguageMappingConfiguration(fFile);
		
		assertSameLanguage(originalLanguage, fManager.getLanguageForFile(fFile, fConfiguration));
	}

	public void testDirectWorkspaceContentTypeMapping() throws Exception {
		ILanguage originalLanguage = fManager.getLanguageForFile(fFile, fConfiguration);
		assertDifferentLanguages(originalLanguage, fLanguage1);
		
		String filename = fFile.getLocation().toString();
		IContentType contentType = CContentTypes.getContentType(fProject, filename);
		WorkspaceLanguageConfiguration config = fManager.getWorkspaceLanguageConfiguration();
		config.addWorkspaceMapping(contentType.getId(), GPPLanguage.ID);
		fManager.storeWorkspaceLanguageConfiguration(EMPTY_CONTENT_TYPES);
		
		assertEquals(fLanguage1, fManager.getLanguageForFile(fFile, fConfiguration));
		
		config.removeWorkspaceMapping(contentType.getId());
		fManager.storeLanguageMappingConfiguration(fFile);
		
		assertEquals(originalLanguage, fManager.getLanguageForFile(fFile, fConfiguration));
	}

	public void testOverriddenWorkspaceContentTypeMapping1() throws Exception {
		ILanguage originalLanguage = fManager.getLanguageForFile(fFile, fConfiguration);
		assertDifferentLanguages(originalLanguage, fLanguage1);
		
		String filename = fFile.getLocation().toString();
		IContentType contentType = CContentTypes.getContentType(fProject, filename);

		// Set workspace mapping
		WorkspaceLanguageConfiguration config = fManager.getWorkspaceLanguageConfiguration();
		config.addWorkspaceMapping(contentType.getId(), GPPLanguage.ID);
		fManager.storeWorkspaceLanguageConfiguration(EMPTY_CONTENT_TYPES);
		
		// Override with project mapping
		ProjectLanguageConfiguration config2 = fManager.getLanguageConfiguration(fCProject.getProject());
		config2.addContentTypeMapping(fConfiguration, contentType.getId(), GCCLanguage.ID);
		fManager.storeLanguageMappingConfiguration(fProject, EMPTY_CONTENT_TYPES);
		
		assertSameLanguage(fLanguage2, fManager.getLanguageForFile(fFile, fConfiguration));
	}

	public void testOverriddenWorkspaceContentTypeMapping2() throws Exception {
		ILanguage originalLanguage = fManager.getLanguageForFile(fFile, fConfiguration);
		assertDifferentLanguages(originalLanguage, fLanguage1);
		
		String filename = fFile.getLocation().toString();
		IContentType contentType = CContentTypes.getContentType(fProject, filename);

		// Set workspace mapping
		WorkspaceLanguageConfiguration config = fManager.getWorkspaceLanguageConfiguration();
		config.addWorkspaceMapping(contentType.getId(), GPPLanguage.ID);
		fManager.storeWorkspaceLanguageConfiguration(EMPTY_CONTENT_TYPES);
		
		// Override with file mapping
		ProjectLanguageConfiguration config2 = fManager.getLanguageConfiguration(fCProject.getProject());
		config2.addFileMapping(fConfiguration, fFile, GCCLanguage.ID);
		fManager.storeLanguageMappingConfiguration(fFile);
		
		assertSameLanguage(fLanguage2, fManager.getLanguageForFile(fFile, fConfiguration));
	}
	
	public void testOverriddenProjectContentTypeMapping() throws Exception {
		ILanguage originalLanguage = fManager.getLanguageForFile(fFile, fConfiguration);
		assertDifferentLanguages(originalLanguage, fLanguage1);
		
		String filename = fFile.getLocation().toString();
		IContentType contentType = CContentTypes.getContentType(fProject, filename);

		// Set project mapping
		ProjectLanguageConfiguration config = fManager.getLanguageConfiguration(fCProject.getProject());
		config.addContentTypeMapping(fConfiguration, contentType.getId(), GPPLanguage.ID);
		fManager.storeLanguageMappingConfiguration(fProject, EMPTY_CONTENT_TYPES);
		
		// Override with file mapping
		ProjectLanguageConfiguration config2 = fManager.getLanguageConfiguration(fCProject.getProject());
		config2.addFileMapping(fConfiguration, fFile, GCCLanguage.ID);
		fManager.storeLanguageMappingConfiguration(fFile);
		
		assertSameLanguage(fLanguage2, fManager.getLanguageForFile(fFile, fConfiguration));
	}
	
	protected void assertSameLanguage(ILanguage expected, ILanguage actual) {
		if (expected != null) {
			assertNotNull(actual);
			assertEquals(expected.getId(), actual.getId());
		} else {
			assertNull(actual);
		}
	}
	
	protected void assertDifferentLanguages(ILanguage language1, ILanguage language2) {
		assertNotNull(language1);
		assertNotNull(language2);
		assertNotSame(language1.getId(), language2.getId());
	}
}
