/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.rewrite;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.cdt.core.tests.BaseTestFramework;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;

/**
 * @author Guido Zgraggen IFS
 */
public abstract class RewriteBaseTest extends BaseTestFramework implements ILogListener{
	protected static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
	
	protected TreeMap<String, TestSourceFile> fileMap = new TreeMap<String, TestSourceFile>();
	protected String fileWithSelection;
	protected TextSelection selection;

	protected RewriteBaseTest(String name) {
		super(name);
	}
	
	public RewriteBaseTest(String name, Vector<TestSourceFile> files) {
		super(name);
		for (TestSourceFile file : files) {
			fileMap.put(file.getName(), file);
		}
	}

	@Override
	protected abstract void runTest() throws Throwable;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		for (TestSourceFile testFile : fileMap.values()) {
			if (testFile.getSource().length() > 0) {
				importFile(testFile.getName(), testFile.getSource());
			}
		}
	}
	
	protected void assertEquals(TestSourceFile file, IFile file2) throws Exception {
		StringBuilder code = getCodeFromFile(file2);
		assertEquals(file.getExpectedSource(), TestHelper.unifyNewLines(code.toString()));
	}
	
	protected void compareFiles(Map<String, TestSourceFile> testResourceFiles) throws Exception {
		for (String fileName : testResourceFiles.keySet()) {
			TestSourceFile file = testResourceFiles.get(fileName);
			IFile iFile = project.getFile(new Path(fileName));
			StringBuilder code = getCodeFromFile(iFile);
			assertEquals(TestHelper.unifyNewLines(file.getExpectedSource()),
					TestHelper.unifyNewLines(code.toString()));
		}
	}

	protected StringBuilder getCodeFromFile(IFile file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			code.append(line);
			code.append('\n');
		}
		br.close();
		return code;
	}

	@Override
	protected void tearDown() throws Exception {
		System.gc();
		fileManager.closeAllFiles();
		super.tearDown();
	}

	public void logging(IStatus status, String plugin) {
		Throwable ex = status.getException();
		StringBuilder stackTrace = new StringBuilder();
		if (ex != null) {
			stackTrace.append('\n');
			for (StackTraceElement ste : ex.getStackTrace()) {
				stackTrace.append(ste.toString());
			}
		}
		fail("Log-Message: " + status.getMessage() + stackTrace.toString());		 //$NON-NLS-1$
	}

	public void setFileWithSelection(String fileWithSelection) {
		this.fileWithSelection = fileWithSelection;
	}

	public void setSelection(TextSelection selection) {
		this.selection = selection;
	}
}
