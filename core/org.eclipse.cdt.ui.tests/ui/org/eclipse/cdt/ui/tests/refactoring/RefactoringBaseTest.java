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
package org.eclipse.cdt.ui.tests.refactoring;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

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
public abstract class RefactoringBaseTest extends BaseTestFramework implements ILogListener{
	protected static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();
	
	protected TreeMap<String, TestSourceFile> fileMap = new TreeMap<String, TestSourceFile>();
	protected String fileWithSelection;
	protected TextSelection selection;

	protected RefactoringBaseTest(String name) {
		super(name);
	}
	
	public RefactoringBaseTest(String name, Collection<TestSourceFile> files) {
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
			if(testFile.getSource().length() > 0) {
				importFile(testFile.getName(), testFile.getSource());
			}
		}
	}
	
	protected void assertEquals(TestSourceFile file, IFile file2) throws Exception {
		String code = getCodeFromIFile(file2);
		assertEquals(file.getExpectedSource(), TestHelper.unifyNewLines(code));
	}
	
	protected void compareFiles(Map<String,TestSourceFile> testResourceFiles) throws Exception {
		for (String fileName : testResourceFiles.keySet()) {
			String expectedSource = testResourceFiles.get(fileName).getExpectedSource();
			IFile iFile = project.getFile(new Path(fileName));
			String code = getCodeFromIFile(iFile);
			assertEquals(TestHelper.unifyNewLines(expectedSource), TestHelper.unifyNewLines(code));
		}
	}

	protected String getCodeFromIFile(IFile file) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			code.append(line);
			code.append('\n');
		}
		br.close();
		return code.toString();
	}

	@Override
	protected void tearDown() throws Exception {
		System.gc();
		fileManager.closeAllFiles();
		super.tearDown();
	}

	@Override
	public void logging(IStatus status, String plugin) {
		Throwable ex = status.getException();
		StringBuffer stackTrace = new StringBuffer();
		if(ex != null) {
			stackTrace.append('\n');
			for(StackTraceElement ste : ex.getStackTrace()) {
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
