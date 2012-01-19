/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.osgi.framework.Bundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.ui.refactoring.RefactoringASTCache;

/**
 * Common base for refactoring tests.
 */
public abstract class RefactoringTestBase extends BaseTestCase {
	private static final int INDEXER_TIMEOUT_SEC = 360;
	protected static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();

	private boolean cpp = true;
	private RefactoringASTCache astCache;
	private ICProject cproject;
	private final Set<TestSourceFile> testFiles = new LinkedHashSet<TestSourceFile>();
	private TestSourceFile selectedFile;
	private TextSelection selection;
	private TestSourceFile historyScript;

    protected RefactoringTestBase() {
		super();
	}

    protected RefactoringTestBase(String name) {
		super(name);
	}
	
	@Override
	public void setUp() throws Exception {
		super.setUp();
		cproject = cpp ?
				CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER) : 
				CProjectHelper.createCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
		Bundle bundle = CTestPlugin.getDefault().getBundle();
		CharSequence[] testData = TestSourceReader.getContentsForTest(bundle, "ui", getClass(), getName(), 0);

		for (int i = 0; i < testData.length; i++) {
			CharSequence contents = testData[i];
			TestSourceFile testFile = null;
			boolean expectedResult = false;
			BufferedReader reader = new BufferedReader(new StringReader(contents.toString()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (testFile == null) {
					testFile = new TestSourceFile(line.trim());
				} else if (isResultDelimiter(line.trim())) {
					expectedResult = true;
				} else if (expectedResult) {
					testFile.addLineToExpectedSource(line);
				} else {
					testFile.addLineToSource(line);
				}
			}
			reader.close();
			
			TestSourceReader.createFile(cproject.getProject(), new Path(testFile.getName()),
					testFile.getSource());
			testFiles.add(testFile);
			if (testFile.getName().endsWith(".xml")) {
				historyScript = testFile;
			} else if (selection == null) {
				selection = testFile.getSelection();
				if (selection != null)
					selectedFile = testFile;
			}
		}
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		assertTrue(CCorePlugin.getIndexManager().joinIndexer(INDEXER_TIMEOUT_SEC * 1000,
				NULL_PROGRESS_MONITOR));
		astCache = new RefactoringASTCache();
	}

	@Override
	public void tearDown() throws Exception {
		astCache.dispose();
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					NULL_PROGRESS_MONITOR);
		}
		super.tearDown();
	}

	protected void assertRefactoringSuccess() throws Exception {
		executeRefactoring(true);
		compareFiles();
	}

	protected void assertRefactoringFailure() throws Exception {
		executeRefactoring(false);
	}

	private void executeRefactoring(boolean expectedSuccess) throws Exception {
		if (historyScript != null) {
			executeHistoryRefactoring(expectedSuccess);
			return;
		}

		Refactoring refactoring = createRefactoring();
		executeRefactoring(refactoring, true, expectedSuccess);
	}

	protected void executeRefactoring(Refactoring refactoring, boolean withUserInput,
			boolean expectedSuccess) throws CoreException, Exception {
		RefactoringStatus initialStatus = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
		if (!expectedSuccess) {
			assertStatusFatalError(initialStatus);
			return;
		}

		assertStatusOk(initialStatus);
		if (withUserInput)
			simulateUserInput();
		RefactoringStatus finalStatus = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
		assertStatusOk(finalStatus);
		Change change = refactoring.createChange(NULL_PROGRESS_MONITOR);
		change.perform(NULL_PROGRESS_MONITOR);
	}

	private void executeHistoryRefactoring(boolean expectedSuccess) throws Exception {
		URI uri= URIUtil.toURI(cproject.getProject().getLocation());
		String scriptSource = historyScript.getSource().replaceAll("\\$\\{projectPath\\}", uri.getPath());
		RefactoringHistory history = RefactoringHistoryService.getInstance().readRefactoringHistory(
				new ByteArrayInputStream(scriptSource.getBytes()), 0);
		for (RefactoringDescriptorProxy proxy : history.getDescriptors()) {
			RefactoringStatus status = new RefactoringStatus();
			Refactoring refactoring =
					proxy.requestDescriptor(NULL_PROGRESS_MONITOR).createRefactoring(status);
			assertTrue(status.isOK());
			executeRefactoring(refactoring, false, expectedSuccess);
		}
	}

	/**
	 * Creates a refactoring object.
	 */
	protected abstract Refactoring createRefactoring();

	/**
	 * Subclasses can override to simulate user input.
	 */
	protected void simulateUserInput() {
	}

	protected ICProject getCProject() {
		return cproject;
	}

	protected TestSourceFile getSelectedTestFile() {
		return selectedFile;
	}

	protected IFile getSelectedFile() {
		if (selectedFile == null)
			return null;
		return cproject.getProject().getFile(new Path(selectedFile.getName()));
	}

	protected TestSourceFile getHistoryScriptFile() {
		return historyScript;
	}

	protected TextSelection getSelection() {
		return selection;
	}

	protected boolean isCpp() {
		return cpp;
	}

	protected void setCpp(boolean cpp) {
		this.cpp = cpp;
	}

	private boolean isResultDelimiter(String str) {
		if (str.isEmpty())
			return false;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) != '=')
				return false;
		}
		return true;
	}

	protected void assertStatusOk(RefactoringStatus status) {
		if (!status.isOK())
			fail("Error or warning status: " + status.getEntries()[0].getMessage());
	}

	protected void assertStatusWarning(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Warning status expected", status.hasWarning());
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isWarning()) {
				++count;
			}
		}
		assertEquals("Found " + count + " warnings instead of expected " + number, number, count);
	}

	protected void assertStatusInfo(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Info status expected", status.hasInfo());
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isInfo()) {
				++count;
			}
		}
		assertEquals("Found " + count + " informational messages instead of expected " + number, number, count);
	}

	protected void assertStatusError(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Error status expected", status.hasError());
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isError()) {
				++count;
			}
		}
		assertEquals("Found " + count + " errors instead of expected " + number, number, count);
	}

	protected void assertStatusFatalError(RefactoringStatus status, int number) {
		if (number > 0) {
			assertTrue("Fatal error status expected", status.hasFatalError());
		}
		RefactoringStatusEntry[] entries = status.getEntries();
		int count = 0;
		for (RefactoringStatusEntry entry : entries) {
			if (entry.isFatalError()) {
				++count;
			}
		}
		assertEquals("Found " + count + " fatal errors instead of expected " + number, number, count);
	}

	protected void assertStatusFatalError(RefactoringStatus status) {
		assertTrue("Fatal error status expected", status.hasFatalError());
	}

	protected void assertEquals(TestSourceFile testFile, IFile file) throws Exception {
		String actualSource = getFileContents(file);
		assertEquals(testFile.getExpectedSource(), actualSource);
	}
	
	protected void compareFiles() throws Exception {
		for (TestSourceFile testFile : testFiles) {
			String expectedSource = testFile.getExpectedSource();
			IFile file = cproject.getProject().getFile(new Path(testFile.getName()));
			String actualSource = getFileContents(file);
			assertEquals(expectedSource, actualSource);
		}
	}

	protected String getFileContents(IFile file) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
		StringBuilder code = new StringBuilder();
		String line;
		while ((line = reader.readLine()) != null) {
			code.append(line);
			code.append('\n');
		}
		reader.close();
		return code.toString();
	}
}
