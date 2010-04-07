/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * TODO: add description
 */
public class CodanTestCase extends BaseTestCase {
	ArrayList<File> tempFiles = new ArrayList<File>();
	protected File tmpDir;
	protected ICProject cproject;
	protected File currentFile;

	/**
	 * 
	 */
	public CodanTestCase() {
		super();
	}

	/**
	 * @param name
	 */
	public CodanTestCase(String name) {
		super(name);
	}

	/**
	 * Override for c++
	 * 
	 * @return is c++ tests
	 */
	public boolean isCpp() {
		return false;
	}

	public void setUp() throws Exception {
		super.setUp();
		removeLeftOverProjects();
		cproject = createProject(isCpp());
		tmpDir = cproject.getProject().getLocation().makeAbsolute().toFile();
	}

	public File load(String file) {
		Class clazz = getClass();
		InputStream st = null;
		try {
			st = TestUtils.getJavaFileText(clazz);
		} catch (IOException e) {
			fail("Cannot find java file: " + clazz + ": " + e.getMessage());
		}
		try {
			File testFile = new File(tmpDir, file);
			tempFiles.add(testFile);
			TestUtils.saveFile(st, testFile);
			st.close();
			currentFile = testFile;
			try {
				cproject.getProject().refreshLocal(1, null);
			} catch (CoreException e) {
				// hmm
			}
			return testFile;
		} catch (IOException e) {
			fail("Cannot save test: " + file + ": " + e.getMessage());
			return null;
		}
	}

	public void tearDown() throws CoreException {
		if (cproject != null) {
			try {
				cproject.getProject().delete(
						IResource.FORCE
								| IResource.ALWAYS_DELETE_PROJECT_CONTENT,
						new NullProgressMonitor());
			
			} catch (CoreException e) {
				throw e;
			}
		}
	}

	/**
	 * @throws CoreException
	 */
	private void removeLeftOverProjects() throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
			IProject p = projects[i];
			if (p.getName().startsWith("Codan")) {
				p.delete(IResource.FORCE
						| IResource.ALWAYS_DELETE_PROJECT_CONTENT,
						new NullProgressMonitor());
			}
		}
	}

	protected ICProject createProject(final boolean cpp) throws CoreException {
		final ICProject cprojects[] = new ICProject[1];
		ModelJoiner mj = new ModelJoiner();
		try {
			// Create the cproject
			final String projectName = "CodanProjTest_"
					+ System.currentTimeMillis();
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					// Create the cproject
					ICProject cproject = cpp ? CProjectHelper.createCCProject(
							projectName, null, IPDOMManager.ID_NO_INDEXER)
							: CProjectHelper.createCProject(projectName, null,
									IPDOMManager.ID_NO_INDEXER);
					cprojects[0] = cproject;
				}
			}, null);
			mj.join();
			// Index the cproject
			CCorePlugin.getIndexManager().setIndexerId(cprojects[0],
					IPDOMManager.ID_FAST_INDEXER);
			// wait until the indexer is done
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000,
					new NullProgressMonitor()));
		} finally {
			mj.dispose();
		}
		return cprojects[0];
	}

	protected void loadFiles() throws CoreException {
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				cproject.getProject().refreshLocal(1, monitor);
			}
		}, null);
		// Index the cproject
		CCorePlugin.getIndexManager().setIndexerId(cproject,
				IPDOMManager.ID_FAST_INDEXER);
		// wait until the indexer is done
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(1000*60, // 1 min
				new NullProgressMonitor()));
		return;
	}

	/**
	 * @param pos
	 * @return
	 * @throws IOException
	 */
	protected int pos2line(int pos) throws IOException {
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
			;
		} finally {
			st.close();
		}
		return 0;
	}
}