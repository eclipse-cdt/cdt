/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.templateengine;

import java.io.File;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.TemplateEngine;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

public class TestProcesses extends BaseTestCase {
	private static final String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getRawLocation().toOSString();
	private static final String PROJECT_NAME = "TemplateEngineTestsProject"; //$NON-NLS-1$
	private static final String SOURCE_FOLDER = "Source"; //$NON-NLS-1$
	private static final String FILE_NAME = "File"; //$NON-NLS-1$
	private static final String LINK = "Link"; //$NON-NLS-1$
	private static final String CPP_EXT = ".cpp"; //$NON-NLS-1$
	private static final String H_EXT = ".h"; //$NON-NLS-1$

	private static final String PROJECT_TYPE = "org.eclipse.cdt.core.tests.projectType"; //$NON-NLS-1$

	@Override
	protected void setUp() throws Exception {
		TemplateEngineTestsHelper.turnOffAutoBuild();
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		IPath projectLocation = project.getRawLocation();
		
		if (project.exists()) {
			project.delete(true, true, null);
		}
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(project.getName());

		if ((projectLocation != null) && (!projectLocation.equals(Platform.getLocation()))) {
			description.setLocation(projectLocation);
		}

		CCorePlugin.getDefault().createCDTProject(description, project, null);
		if (!project.isOpen()) {
			project.open(null);
		}
		
	}
	
	public void testAddFile() {
		TemplateCore template = TemplateEngine.getDefault().getFirstTemplate(PROJECT_TYPE, null, ".*AddFile"); //$NON-NLS-1$
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", PROJECT_NAME); //$NON-NLS-1$
		valueStore.put("projectType", PROJECT_TYPE); //$NON-NLS-1$
		valueStore.put("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("isCProject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("baseName", FILE_NAME); //$NON-NLS-1$
		
		if (TemplateEngineTestsHelper.failIfErrorStatus(template.executeTemplateProcesses(null, false))) {
			return;
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		assertTrue(project.exists());
		IFile file = project.getFile(FILE_NAME + CPP_EXT);
		assertTrue(file.exists());
		file = project.getFile(FILE_NAME + H_EXT);
		assertTrue(file.exists());
	}

	public void testAddFiles() {
		TemplateCore template = TemplateEngine.getDefault().getFirstTemplate(PROJECT_TYPE, null, ".*AddFiles"); //$NON-NLS-1$
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", PROJECT_NAME); //$NON-NLS-1$
		valueStore.put("projectType", PROJECT_TYPE); //$NON-NLS-1$
		valueStore.put("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("isCProject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("baseName", FILE_NAME); //$NON-NLS-1$
		
		if (TemplateEngineTestsHelper.failIfErrorStatus(template.executeTemplateProcesses(null, false))) {
			return;
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		assertTrue(project.exists());
		IFile file = project.getFile(FILE_NAME + CPP_EXT);
		assertTrue(file.exists());
		file = project.getFile(FILE_NAME + H_EXT);
		assertTrue(file.exists());
	}

	public void testAddLink() {
		TemplateCore template = TemplateEngine.getDefault().getFirstTemplate(PROJECT_TYPE, null, ".*AddLink"); //$NON-NLS-1$
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", PROJECT_NAME); //$NON-NLS-1$
		valueStore.put("projectType", PROJECT_TYPE); //$NON-NLS-1$
		valueStore.put("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("isCProject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("baseName", FILE_NAME); //$NON-NLS-1$

		if (TemplateEngineTestsHelper.failIfErrorStatus(template.executeTemplateProcesses(null, false))) {
			return;
		}
		
		try {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
			assertTrue(project.exists());
			project.refreshLocal(1, null);
			IFile file = project.getFile(FILE_NAME + CPP_EXT);
			assertTrue(file.exists());
			file = project.getFile(FILE_NAME + LINK + CPP_EXT);
			assertTrue(file.exists());
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	public void testAppend() {
		TemplateCore template = TemplateEngine.getDefault().getFirstTemplate(PROJECT_TYPE, null, ".*Append"); //$NON-NLS-1$
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", PROJECT_NAME); //$NON-NLS-1$
		valueStore.put("projectType", PROJECT_TYPE); //$NON-NLS-1$
		valueStore.put("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("isCProject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("baseName", FILE_NAME); //$NON-NLS-1$
		
		valueStore.put("targetSourceName", workspaceLocation + File.separator + PROJECT_NAME + File.separator + FILE_NAME + CPP_EXT); //$NON-NLS-1$
		valueStore.put("targetHeaderName", workspaceLocation + File.separator + PROJECT_NAME + File.separator + FILE_NAME + H_EXT); //$NON-NLS-1$
		
		if (TemplateEngineTestsHelper.failIfErrorStatus(template.executeTemplateProcesses(null, false))) {
			return;
		}
		
		try {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
			assertTrue(project.exists());
			project.refreshLocal(1, null);
			IFile file = project.getFile(FILE_NAME + CPP_EXT);
			assertTrue(file.exists());
			file = project.getFile(FILE_NAME + H_EXT);
			assertTrue(file.exists());
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	public void testAppendCreate() {
		TemplateCore template = TemplateEngine.getDefault().getFirstTemplate(PROJECT_TYPE, null, ".*AppendCreate"); //$NON-NLS-1$
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", PROJECT_NAME); //$NON-NLS-1$
		valueStore.put("projectType", PROJECT_TYPE); //$NON-NLS-1$
		valueStore.put("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("isCProject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("baseName", FILE_NAME); //$NON-NLS-1$
		
		valueStore.put("targetSourceName", FILE_NAME + CPP_EXT); //$NON-NLS-1$
		valueStore.put("targetHeaderName", FILE_NAME + H_EXT); //$NON-NLS-1$
		
		if (TemplateEngineTestsHelper.failIfErrorStatus(template.executeTemplateProcesses(null, false))) {
			return;
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		assertTrue(project.exists());
		IFile file = project.getFile(FILE_NAME + CPP_EXT);
		assertTrue(file.exists());
		file = project.getFile(FILE_NAME + H_EXT);
		assertTrue(file.exists());
	}

	public void testCopy() {
		TemplateCore template = TemplateEngine.getDefault().getFirstTemplate(PROJECT_TYPE, null, ".*Copy"); //$NON-NLS-1$
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", PROJECT_NAME); //$NON-NLS-1$
		valueStore.put("projectType", PROJECT_TYPE); //$NON-NLS-1$
		valueStore.put("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("isCProject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("baseName", FILE_NAME); //$NON-NLS-1$
		
		valueStore.put("targetSourceName", workspaceLocation + File.separator + PROJECT_NAME + File.separator + FILE_NAME + CPP_EXT); //$NON-NLS-1$
		valueStore.put("targetHeaderName", workspaceLocation + File.separator + PROJECT_NAME + File.separator + FILE_NAME + H_EXT); //$NON-NLS-1$
		
		if (TemplateEngineTestsHelper.failIfErrorStatus(template.executeTemplateProcesses(null, false))) {
			return;
		}
		
		try {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
			assertTrue(project.exists());
			project.refreshLocal(1, null);
			IFile file = project.getFile(FILE_NAME + CPP_EXT);
			assertTrue(file.exists());
			file = project.getFile(FILE_NAME + H_EXT);
			assertTrue(file.exists());
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	public void testCreateResourceIdentifier() {
		TemplateCore template = TemplateEngine.getDefault().getFirstTemplate(PROJECT_TYPE, null, ".*CreateResourceIdentifier"); //$NON-NLS-1$
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", PROJECT_NAME); //$NON-NLS-1$
		valueStore.put("projectType", PROJECT_TYPE); //$NON-NLS-1$
		valueStore.put("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("isCProject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("valueName1", "baseName1"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("appName1", "Hello"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("valueName2", "baseName2"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("appName2", "He"); //$NON-NLS-1$ //$NON-NLS-2$

		if (TemplateEngineTestsHelper.failIfErrorStatus(template.executeTemplateProcesses(null, false))) {
			return;
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		assertTrue(project.exists());
		IFile file = project.getFile("HELL" + CPP_EXT); //$NON-NLS-1$
		assertTrue(file.exists());
		file = project.getFile("HEXX" + CPP_EXT); //$NON-NLS-1$
		assertTrue(file.exists());
	}

	public void testCreateSourceFolder() {
		TemplateCore template = TemplateEngine.getDefault().getFirstTemplate(PROJECT_TYPE, null, ".*CreateSourceFolder"); //$NON-NLS-1$
		Map<String, String> valueStore = template.getValueStore();
		valueStore.put("projectName", PROJECT_NAME); //$NON-NLS-1$
		valueStore.put("projectType", PROJECT_TYPE); //$NON-NLS-1$
		valueStore.put("location", ""); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("isCProject", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		valueStore.put("sourceDir1", SOURCE_FOLDER + 1); //$NON-NLS-1$
		valueStore.put("sourceDir2", SOURCE_FOLDER + 2); //$NON-NLS-1$
		
		if (TemplateEngineTestsHelper.failIfErrorStatus(template.executeTemplateProcesses(null, false))) {
			return;
		}
		
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
		assertTrue(project.exists());
		IFolder folder = project.getFolder(SOURCE_FOLDER + 1);
		assertTrue(folder.exists());
		folder = project.getFolder(SOURCE_FOLDER + 2);
		assertTrue(folder.exists());
	}
		
}
