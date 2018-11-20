/*******************************************************************************
 * Copyright (c) 2015 Patrick Hofer and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Patrick Hofer - initial API and implementation
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.ui.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.FileManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

public class UITestCaseWithProject extends BaseUITestCase {
	ArrayList<File> tempFiles = new ArrayList<>();
	protected File tmpDir;
	protected ICProject cproject;
	protected File currentFile;
	protected ICElement currentCElem;
	protected IFile currentIFile;
	IProgressMonitor monitor = new NullProgressMonitor();
	static FileManager fileManager = new FileManager();

	/**
	 * Override for c++ (i.e. at least one c++ test)
	 *
	 * @return is c++ tests
	 */
	public boolean isCpp() {
		return false;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		removeLeftOverProjects();
		cproject = createProject(isCpp());
		tmpDir = cproject.getProject().getLocation().makeAbsolute().toFile();
	}

	@Override
	protected void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					new NullProgressMonitor());
		}

		super.tearDown();
	}

	private void removeLeftOverProjects() throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject p = projects[i];
			if (p.getName().startsWith("Codan")) {
				p.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
			}
		}
	}

	protected ICProject createProject(final boolean cpp) throws CoreException {
		final ICProject cprojects[] = new ICProject[1];
		ModelJoiner mj = new ModelJoiner();
		try {
			// Create the cproject
			final String projectName = "CDTUIProjTest_" + System.currentTimeMillis();
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(new IWorkspaceRunnable() {
				@Override
				public void run(IProgressMonitor monitor) throws CoreException {
					// Create the cproject
					ICProject cproject = cpp
							? CProjectHelper.createCCProject(projectName, null, IPDOMManager.ID_NO_INDEXER)
							: CProjectHelper.createCProject(projectName, null, IPDOMManager.ID_NO_INDEXER);
					cprojects[0] = cproject;
				}
			}, null);
			mj.join();
		} finally {
			mj.dispose();
		}
		return cprojects[0];
	}

	protected void indexFiles() throws CoreException, InterruptedException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				cproject.getProject().refreshLocal(1, monitor);
			}
		}, null);
		// Index the cproject
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		CCorePlugin.getIndexManager().reindex(cproject);
		waitForIndexer(cproject);
	}

	protected int pos2Line(int pos) throws IOException {
		FileInputStream st = new FileInputStream(currentFile);
		try {
			int c;
			int line = 1;
			int cur = 0;
			while ((c = st.read()) != -1) {
				if (c == '\n')
					line++;
				if (cur >= pos)
					return line;
				cur++;
			}
		} finally {
			st.close();
		}
		return 0;
	}

	public File loadCode(String code, boolean cpp) {
		String fileKey = "@file:";
		int indf = code.indexOf(fileKey);
		if (indf >= 0) {
			int sep = code.indexOf('\n');
			if (sep != -1) {
				String line = code.substring(0, sep);
				code = code.substring(sep + 1);
				String fileName = line.substring(indf + fileKey.length()).trim();
				return loadCode(code, new File(tmpDir, fileName));
			}
		}
		String ext = cpp ? ".cpp" : ".c";
		File testFile = null;
		try {
			testFile = File.createTempFile("test", ext, tmpDir); //$NON-NLS-1$
		} catch (IOException e1) {
			fail(e1.getMessage());
			return null;
		}
		return loadCode(code, testFile);
	}

	public File loadCode(String code, String filename) {
		File testFile = new File(tmpDir, filename);
		return loadCode(code, testFile);
	}

	private File loadCode(String code, File testFile) {
		try {
			tempFiles.add(testFile);
			TestUtils.saveFile(new ByteArrayInputStream(code.trim().getBytes()), testFile);
			currentFile = testFile;
			try {
				cproject.getProject().refreshLocal(1, null);
			} catch (CoreException e) {
				fail(e.getMessage());
			}
			currentCElem = cproject.findElement(new Path(currentFile.toString()));
			currentIFile = (IFile) currentCElem.getResource();
			return testFile;
		} catch (IOException e) {
			fail("Cannot save test: " + testFile + ": " + e.getMessage());
			return null;
		} catch (CModelException e) {
			fail("Cannot find file: " + testFile + ": " + e.getMessage());
			return null;
		}
	}

	public File loadCodeC(String code) {
		return loadCode(code, true);
	}

	public File loadCodeCpp(String code) {
		return loadCode(code, false);
	}

	public File loadCode(String code) {
		return loadCode(code, isCpp());
	}
}
