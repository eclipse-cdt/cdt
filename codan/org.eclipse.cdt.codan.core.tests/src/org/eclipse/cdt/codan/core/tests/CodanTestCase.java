/*******************************************************************************
 * Copyright (c) 2009, 2015 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
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
import org.eclipse.core.runtime.Plugin;

/**
 * TODO: add description
 */
@SuppressWarnings("nls")
public class CodanTestCase extends BaseTestCase {
	ArrayList<File> tempFiles = new ArrayList<>();
	protected File tmpDir;
	protected ICProject cproject;
	protected File currentFile;
	protected ICElement currentCElem;
	protected IFile currentIFile;
	protected ArrayList<Integer> errLines = new ArrayList<>();

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

	/**
	 * Override for header files
	 *
	 * @return is header tests
	 */
	public boolean isHeader() {
		return false;
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		removeLeftOverProjects();
		cproject = createProject(isCpp());
		tmpDir = cproject.getProject().getLocation().makeAbsolute().toFile();
		// this make CodanRunner to propagate all exceptions it normally just logs
		System.setProperty("codan.rethrow", "true"); // test can override setUp and unset this
	}

	@Override
	public void tearDown() throws Exception {
		if (cproject != null) {
			try {
				cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
						new NullProgressMonitor());
			} catch (CoreException e) {
				throw e;
			}
		}
		super.tearDown();
	}

	/**
	 * @throws CoreException
	 */
	private void removeLeftOverProjects() throws CoreException {
		try {
			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IProject[] projects = workspace.getRoot().getProjects();
			for (int i = 0; i < projects.length; i++) {
				IProject p = projects[i];
				if (p.getName().startsWith("Codan")) {
					p.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, new NullProgressMonitor());
				}
			}
		} catch (Throwable e) {
			// moving on...
		}
	}

	protected ICProject createProject(final boolean cpp) throws CoreException {
		final ICProject cprojects[] = new ICProject[1];
		ModelJoiner mj = new ModelJoiner();
		try {
			// Create the cproject
			final String projectName = "CodanProjTest_" + System.currentTimeMillis();
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
		// wait until the indexer is done
		waitForIndexer(cproject);
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
		return getContents(1)[0].toString().replaceAll("\\r\\n|\\n", NL);
	}

	protected StringBuilder[] getContents(int sections) {
		try {
			return TestSourceReader.getContentsForTest(getPlugin().getBundle(), getSourcePrefix(), getClass(),
					getName(), sections);
		} catch (IOException e) {
			fail(e.getMessage());
			return null;
		}
	}

	protected String getSourcePrefix() {
		return "src";
	}

	protected Plugin getPlugin() {
		CodanCoreTestActivator plugin = CodanCoreTestActivator.getDefault();
		return plugin;
	}

	public File loadcode(String code, boolean cpp) throws CoreException {
		String fileKey = "@file:";
		int indf = code.indexOf(fileKey);
		if (indf >= 0) {
			int sep = code.indexOf('\n');
			if (sep != -1) {
				String line = code.substring(0, sep);
				code = code.substring(sep + 1);
				String fileName = line.substring(indf + fileKey.length()).trim();
				return loadcode(code, new File(tmpDir, fileName));
			}
		}
		String ext;
		if (cpp) {
			if (isHeader())
				ext = ".hpp";
			else
				ext = ".cpp";
		} else {
			if (isHeader())
				ext = ".h";
			else
				ext = ".c";
		}
		File testFile = null;
		try {
			testFile = File.createTempFile("test", ext, tmpDir); //$NON-NLS-1$
		} catch (IOException e1) {
			fail(e1.getMessage());
			return null;
		}
		return loadcode(code, testFile);
	}

	private File loadcode(String code, File testFile) throws CoreException {
		try {
			tempFiles.add(testFile);
			loadErrorComments(code);
			TestUtils.saveFile(new ByteArrayInputStream(code.getBytes()), testFile);
			currentFile = testFile;
			try {
				cproject.getProject().refreshLocal(1, null);
				waitForIndexer(cproject);
			} catch (InterruptedException e) {
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

	private static Pattern COMMENT_TAG_PATTERN = Pattern.compile("//\\s*(err|ERR|ERROR|error)\\b");

	private void loadErrorComments(String trim) {
		String[] lines = trim.split("\n");
		for (int i = 0; i < lines.length; i++) {
			String string = lines[i];
			if (COMMENT_TAG_PATTERN.matcher(string).find()) {
				errLines.add(i + 1);
			}
		}
	}

	public File loadcode_c(String code) throws CoreException {
		return loadcode(code, true);
	}

	public File loadcode_cpp(String code) throws CoreException {
		return loadcode(code, false);
	}

	public File loadcode(String code) throws CoreException {
		return loadcode(code, isCpp());
	}

	public File loadcode(CharSequence... more) throws CoreException {
		File file = null;
		for (CharSequence cseq : more) {
			file = loadcode(cseq.toString(), isCpp());
		}
		return file;
	}
}