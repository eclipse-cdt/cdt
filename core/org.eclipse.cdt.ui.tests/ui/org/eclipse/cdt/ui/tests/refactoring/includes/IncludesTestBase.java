/*******************************************************************************
 * Copyright (c) 2013, 2014 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.TestScannerProvider;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.ui.refactoring.includes.IHeaderChooser;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.cdt.ui.tests.refactoring.TestSourceFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;

/**
 * Common base for include-related tests.
 */
public abstract class IncludesTestBase extends BaseTestCase {
	protected final String LINE_DELIMITER = "\n";
	// Same as in CCorePlugin#SCANNER_INFO_PROVIDER2_NAME.
	private static final String SCANNER_INFO_PROVIDER2_NAME = "ScannerInfoProvider2"; //$NON-NLS-1$

	protected static class FirstHeaderChooser implements IHeaderChooser {
		@Override
		public IPath chooseHeader(String bindingName, Collection<IPath> headers) {
			return headers.isEmpty() ? null : headers.iterator().next();
		}
	}

	/** Expected counts of errors, warnings and info messages */
	protected int expectedInitialErrors;
	protected int expectedInitialWarnings;
	protected int expectedFinalWarnings;
	protected int expectedFinalInfos;

	protected IIndex index;
	protected ICProject cproject;
	protected IASTTranslationUnit ast;
	protected TestSourceFile selectedFile;
	private StringBuilder[] testData;
	private boolean cpp = true;
	private final Set<TestSourceFile> testFiles = new LinkedHashSet<>();

	protected IncludesTestBase() {
		super();
	}

	protected IncludesTestBase(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		resetPreferences();

		cproject = cpp
				? CProjectHelper.createCCProject(getName() + System.currentTimeMillis(), "bin",
						IPDOMManager.ID_NO_INDEXER)
				: CProjectHelper.createCProject(getName() + System.currentTimeMillis(), "bin",
						IPDOMManager.ID_NO_INDEXER);
		IProject project = cproject.getProject();
		TestScannerProvider.sLocalIncludes = new String[] { project.getLocation().toOSString() };
		QualifiedName scannerInfoProviderName = new QualifiedName(CCorePlugin.PLUGIN_ID, SCANNER_INFO_PROVIDER2_NAME);
		project.setSessionProperty(scannerInfoProviderName, new TestScannerProvider());

		Bundle bundle = CTestPlugin.getDefault().getBundle();
		CharSequence[] testData = TestSourceReader.getContentsForTest(bundle, "ui", getClass(), getName(), 0);

		IFile sourceFile = null;
		for (CharSequence contents : testData) {
			TestSourceFile testFile = null;
			boolean expectedResult = false;
			BufferedReader reader = new BufferedReader(new StringReader(contents.toString()));
			String line;
			while ((line = reader.readLine()) != null) {
				String trimmedLine = line.trim();
				if (testFile == null) {
					assertTrue("Invalid file name \"" + trimmedLine + "\"",
							trimmedLine.matches("^(\\w+/)*\\w+\\.\\w+$"));
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

			sourceFile = TestSourceReader.createFile(project, new Path(testFile.getName()), testFile.getSource());
			testFiles.add(testFile);
			selectedFile = testFile;
		}
		CCorePlugin.getIndexManager().setIndexerId(cproject, IPDOMManager.ID_FAST_INDEXER);
		waitForIndexer(cproject);

		index = CCorePlugin.getIndexManager().getIndex(cproject);

		index.acquireReadLock();
		ast = TestSourceReader.createIndexBasedAST(index, cproject, sourceFile);
	}

	@Override
	public void tearDown() throws Exception {
		if (cproject != null) {
			cproject.getProject().delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, npm());
		}
		resetPreferences();
		super.tearDown();
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

	protected void resetPreferences() {
	}

	protected IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}
}
