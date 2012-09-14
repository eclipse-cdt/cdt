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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContext;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptorProxy;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.RefactoringStatusEntry;
import org.eclipse.ltk.core.refactoring.history.RefactoringHistory;
import org.eclipse.ltk.internal.core.refactoring.history.RefactoringHistoryService;
import org.osgi.framework.Bundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.CRefactoringContext;

/**
 * Common base for refactoring tests.
 */
public abstract class RefactoringTestBase extends BaseTestCase {
	protected static final NullProgressMonitor NULL_PROGRESS_MONITOR = new NullProgressMonitor();

	/** Allows empty files to be created during test setup. */
	protected boolean createEmptyFiles = true;
	/** See {@link PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER} */
	protected boolean ascendingVisibilityOrder;
	/** Expected counts of errors, warnings and info messages */
	protected int expectedInitialErrors;
	protected int expectedInitialWarnings;
	protected int expectedFinalWarnings;
	protected int expectedFinalInfos;

	private boolean cpp = true;
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
		resetPreferences();
		cproject = cpp ?
				CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER) :
				CProjectHelper.createCProject(getName() + System.currentTimeMillis(), "bin", IPDOMManager.ID_NO_INDEXER);
		Bundle bundle = CTestPlugin.getDefault().getBundle();
		CharSequence[] testData = TestSourceReader.getContentsForTest(bundle, "ui", getClass(), getName(), 0);

		for (CharSequence contents : testData) {
			TestSourceFile testFile = null;
			boolean expectedResult = false;
			BufferedReader reader = new BufferedReader(new StringReader(contents.toString()));
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmedLine = line.trim();
				if (testFile == null) {
					assertTrue("Invalid file name \"" + trimmedLine + "\"", trimmedLine.matches("^(\\w+/)*\\w+\\.\\w+$"));
					testFile = new TestSourceFile(trimmedLine);
				} else if (isResultDelimiter(trimmedLine)) {
					expectedResult = true;
				} else if (expectedResult) {
					testFile.addLineToExpectedSource(line);
				} else {
					testFile.addLineToSource(line);
				}
			}
			reader.close();

			if (createEmptyFiles || !testFile.getSource().isEmpty()) {
				TestSourceReader.createFile(cproject.getProject(), new Path(testFile.getName()),
						testFile.getSource());
			}
			testFiles.add(testFile);
			if (testFile.getName().endsWith(".xml")) {
				historyScript = testFile;
			} else if (selection == null) {
				selection = testFile.getSelection();
				if (selection != null)
					selectedFile = testFile;
			}
		}
		if (selectedFile == null && !testFiles.isEmpty()) {
			selectedFile = testFiles.iterator().next();
		}
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		waitForIndexer(cproject);
	}

	@Override
	public void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT,
					NULL_PROGRESS_MONITOR);
		}
		resetPreferences();
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
		if (ascendingVisibilityOrder) {
			getPreferenceStore().setValue(PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER,
					ascendingVisibilityOrder);
		}
		if (historyScript != null) {
			executeHistoryRefactoring(expectedSuccess);
			return;
		}

		Refactoring refactoring = createRefactoring();
		RefactoringContext context;
		if (refactoring instanceof CRefactoring) {
			context = new CRefactoringContext((CRefactoring) refactoring);
		} else {
			context = new RefactoringContext(refactoring);
		}
		executeRefactoring(refactoring, context, true, expectedSuccess);
	}

	protected void executeRefactoring(Refactoring refactoring, RefactoringContext context,
			boolean withUserInput, boolean expectedSuccess) throws CoreException, Exception {
		try {
			RefactoringStatus initialStatus = refactoring.checkInitialConditions(NULL_PROGRESS_MONITOR);
			if (!expectedSuccess) {
				assertStatusFatalError(initialStatus);
				return;
			}
			if (expectedInitialErrors != 0) {
				assertStatusError(initialStatus, expectedInitialErrors);
			} else if (expectedInitialWarnings != 0) {
				assertStatusWarning(initialStatus, expectedInitialWarnings);
			} else {
				assertStatusOk(initialStatus);
			}

			if (withUserInput)
				simulateUserInput();
			RefactoringStatus finalStatus = refactoring.checkFinalConditions(NULL_PROGRESS_MONITOR);
			if (expectedFinalWarnings != 0) {
				assertStatusWarning(finalStatus, expectedFinalWarnings);
			} else if (expectedFinalInfos != 0) {
				assertStatusInfo(finalStatus, expectedFinalInfos);
			} else {
				assertStatusOk(finalStatus);
			}
			Change change = refactoring.createChange(NULL_PROGRESS_MONITOR);
			change.perform(NULL_PROGRESS_MONITOR);
		} finally {
			if (context != null)
				context.dispose();
		}
	}

	private void executeHistoryRefactoring(boolean expectedSuccess) throws Exception {
		URI uri= URIUtil.toURI(cproject.getProject().getLocation());
		String scriptSource = historyScript.getSource().replaceAll("\\$\\{projectPath\\}", uri.getPath());
		RefactoringHistory history = RefactoringHistoryService.getInstance().readRefactoringHistory(
				new ByteArrayInputStream(scriptSource.getBytes()), 0);
		for (RefactoringDescriptorProxy proxy : history.getDescriptors()) {
			RefactoringDescriptor descriptor = proxy.requestDescriptor(NULL_PROGRESS_MONITOR);
			RefactoringStatus status = new RefactoringStatus();
			RefactoringContext context = descriptor.createRefactoringContext(status);
			assertTrue(status.isOK());
			executeRefactoring(context.getRefactoring(), context, false, expectedSuccess);
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

	protected ITranslationUnit getSelectedTranslationUnit() {
		IFile file = getSelectedFile();
		if (file == null)
			return null;
		return (ITranslationUnit) CoreModel.getDefault().create(file);
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
			expectedSource= expectedSource.replace("\r\n", "\n");
			actualSource= actualSource.replace("\r\n", "\n");
			assertEquals(expectedSource, actualSource);
		}
	}

	protected String getFileContents(IFile file) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents(), "UTF-8"));
		StringBuilder buffer = new StringBuilder();
		char[] part= new char[2048];
		int read= 0;
		while ((read= reader.read(part)) != -1)
			buffer.append(part, 0, read);
		reader.close();
		return buffer.toString().replace("\r", "");
	}

	protected void resetPreferences() {
		getPreferenceStore().setToDefault(PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER);
	}

	protected IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}
}
