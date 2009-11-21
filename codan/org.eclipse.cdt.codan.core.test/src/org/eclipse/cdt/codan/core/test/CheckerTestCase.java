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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.IProblemReporter;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Alena
 * 
 */
public class CheckerTestCase extends BaseTestCase {
	ArrayList<File> tempFiles = new ArrayList<File>();
	protected File tmpDir;
	private ICProject cproject;
	private IMarker[] markers;
	private File currentFile;

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
		CodanCoreTestActivator plugin = CodanCoreTestActivator.getDefault();
		String name = getClass().getName();
		String classFile = name.replaceAll("\\.", "/");
		classFile += ".java";
		InputStream st = null;
		File f = null;
		try {
			if (plugin != null) {
				URL resource = plugin.getBundle().getResource(
						"src/" + classFile);
				st = resource.openStream();
			} else {
				st = getClass().getResourceAsStream(classFile);
			}
		} catch (IOException e) {
			fail("Cannot find java file: " + classFile);
		}
		try {
			f = saveFile(st, file);
			st.close();
			currentFile = f;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
	static final Pattern filePattern = Pattern.compile("file=\"(.*)\"");

	/**
	 * @param st
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private File saveFile(InputStream st, String file) throws IOException {
		BufferedReader r = new BufferedReader(new InputStreamReader(st));
		String line;
		File testFile = new File(tmpDir, file);
		tempFiles.add(testFile);
		PrintStream wr = new PrintStream(testFile);
		try {
			boolean print = false;
			while ((line = r.readLine()) != null) {
				if (line.contains("<code ")) {
					Matcher m = filePattern.matcher(line);
					if (m.find()) {
						String userFile = m.group(1);
						if (userFile.equals(file)) {
							print = true;
						}
					}
				} else if (line.contains("</code>")) {
					print = false;
				} else if (print) {
					wr.println(line);
				}
			}
		} finally {
			wr.close();
		}
		return testFile;
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
		ModelJoiner mj = new ModelJoiner();
		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					cproject.getProject().refreshLocal(1, monitor);
				}
			}, null);
			mj.join();
			// Index the cproject
			CCorePlugin.getIndexManager().setIndexerId(cproject,
					IPDOMManager.ID_FAST_INDEXER);
			// wait until the indexer is done
			assertTrue(CCorePlugin.getIndexManager().joinIndexer(360000,
					new NullProgressMonitor()));
		} finally {
			mj.dispose();
		}
		return;
	}

	/**
	 * @param i
	 *            - line
	 */
	public void checkErrorLine(int i) {
		assertTrue(markers != null);
		assertTrue(markers.length > 0);
		boolean found = false;
		for (int j = 0; j < markers.length; j++) {
			IMarker m = markers[j];
			Object line = null;
			Object pos;
			try {
				line = m.getAttribute(IMarker.LINE_NUMBER);
				if (line == null || line.equals(-1)) {
					pos = m.getAttribute(IMarker.CHAR_START);
					line = new Integer(pos2line(((Integer) pos).intValue()));
				}
			} catch (CoreException e) {
				fail(e.getMessage());
			} catch (IOException e) {
				fail(e.getMessage());
			}
			if (line.equals(i)) {
				found = true;
			}
		}
		assertTrue("Error on line " + i + " not found ", found);
	}

	/**
	 * @param pos
	 * @return
	 * @throws IOException
	 */
	private int pos2line(int pos) throws IOException {
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

	public void checkNoErrors() {
		assertTrue("Found errors but should not", markers == null
				|| markers.length == 0);
	}

	/**
	 * 
	 */
	public void runOnFile() {
		try {
			loadFiles();
		} catch (CoreException e) {
			fail(e.getMessage());
		}
		CodanRuntime.getInstance().getBuilder().processResource(
				cproject.getProject(), NPM);
		try {
			markers = cproject.getProject()
					.findMarkers(
							IProblemReporter.GENERIC_CODE_ANALYSIS_MARKER_TYPE,
							true, 1);
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}
}
