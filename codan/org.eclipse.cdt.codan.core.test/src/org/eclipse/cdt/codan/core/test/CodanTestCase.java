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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import junit.framework.AssertionFailedError;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
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
	 * Override for c++ (i.e. at least one c++ test)
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

	protected void indexFiles() throws CoreException {
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
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(1000 * 60, // 1 min
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
		} finally {
			st.close();
		}
		return 0;
	}

	protected String getAboveComment() {
		return getContents(1)[0].toString();
	}

	protected StringBuffer[] getContents(int sections) {
		try {
			CodanCoreTestActivator plugin = CodanCoreTestActivator.getDefault();
			if (plugin == null)
				throw new AssertionFailedError(
						"This test must be run as a JUnit plugin test");
			return TestSourceReader.getContentsForTest(plugin.getBundle(),
					"src", getClass(), getName(), sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

	public File loadcode(String code, boolean cpp) {
		String ext = cpp ? ".cc" : ".c"; //$NON-NLS-1$ //$NON-NLS-2$
		File testFile = null;
		try {
			testFile = File.createTempFile("test", ext, tmpDir); //$NON-NLS-1$
		} catch (IOException e1) {
			fail(e1.getMessage());
			return null;
		}
		return loadcode(code, testFile);
	}

	public File loadcode(String code, String filename) {
		File testFile = new File(tmpDir, filename);
		return loadcode(code, testFile);
	}

	private File loadcode(String code, File testFile) {
		try {
			tempFiles.add(testFile);
			TestUtils.saveFile(
					new ByteArrayInputStream(code.trim().getBytes()), testFile);
			currentFile = testFile;
			try {
				cproject.getProject().refreshLocal(1, null);
			} catch (CoreException e) {
				// hmm
				fail(e.getMessage());
			}
			return testFile;
		} catch (IOException e) {
			fail("Cannot save test: " + testFile + ": " + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
	}

	public File loadcode_c(String code) {
		return loadcode(code, true);
	}

	public File loadcode_cpp(String code) {
		return loadcode(code, false);
	}

	public File loadcode(String code) {
		return loadcode(code, isCpp());
	}
}